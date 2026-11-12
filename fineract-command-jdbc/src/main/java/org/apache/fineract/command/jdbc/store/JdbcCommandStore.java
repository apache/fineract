/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.command.jdbc.store;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static org.apache.fineract.command.core.CommandConstants.COMMAND_JSON_CLASS_ATTRIBUTE;
import static org.apache.fineract.command.core.CommandState.UNDER_PROCESSING;
import static org.apache.fineract.command.core.CommandState.UNKNOWN;

import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandReplay;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.jdbc.JdbcCommandProperties;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.jdbc.store.mapping.JdbcCommandMapper;
import org.apache.fineract.command.jdbc.store.mapping.JsonCommandMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnMissingBean(value = CommandStore.class, ignored = JdbcCommandStore.class)
public class JdbcCommandStore implements CommandStore {

    private final JdbcCommandMapper mapper;
    private final JsonCommandMapper jsonMapper;
    private final JdbcCommandRepository repository;
    private final ObjectMapper objectMapper;
    private final JdbcCommandProperties properties;

    @Override
    public Command<Object> getById(Long id) {
        return repository.findById(id).map(mapper::map).orElse(null);
    }

    @Override
    public Command<Object> getByKey(String key, String distinctor) {
        return repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, distinctor).map(mapper::map).orElse(null);
    }

    @Override
    public boolean existsByKey(String key, String distinctor) {
        return repository.existsByIdempotencyKeyAndCustomIdempotencyDistinctor(key, distinctor);
    }

    @Override
    public CommandReplay getReplayByKey(String key, String distinctor) {
        return repository.findOneByIdempotencyKeyAndCustomIdempotencyDistinctor(key, distinctor)
                .map(entity -> new CommandReplay(entity.getState(), entity.getResponseStatusCode(),
                        replayResponse(key, distinctor, entity.getResponse()), entity.getCreatedAt()))
                .orElseGet(() -> new CommandReplay(UNKNOWN, null, null, null));
    }

    /**
     * What a repeat is answered with. Normally the command's own result, rehydrated into the object it was, so that the
     * web layer renders the replay with the very serializer that rendered the original answer - the two are identical
     * because they are the same object rendered the same way, not because two serializers were made to agree.
     *
     * <p>
     * A command that produced no result of its own leaves only what was captured as it went out, and that is replayed
     * as the text it was stored as. Re-rendering it would change the bytes: a decimal written as 1.50 comes back 1.5.
     */
    private Object replayResponse(String key, String distinctor, JsonNode stored) {
        if (isNull(stored)) {
            return null;
        }

        if (stored instanceof ObjectNode object && object.has(COMMAND_JSON_CLASS_ATTRIBUTE)) {
            return jsonMapper.map(stored);
        }

        return repository.findResponseTextByIdempotencyKey(key, distinctor).orElse(null);
    }

    @Override
    @Retry(name = "commandStore")
    public void recordResponse(String key, String distinctor, int statusCode, String body) {
        var updated = repository.updateResponse(key, distinctor, statusCode, body);

        log.debug("Recorded response {} for idempotency key {} (distinctor '{}', {} row(s))", statusCode, key, distinctor, updated);
    }

    @Override
    @Retry(name = "commandStore")
    public boolean claim(Command<?> command) {
        // the claim's age is what tells an abandoned claim from a running one, so it must never be stored without one.
        // The timestamp hook normally sets it as the request arrives, but it can be switched off; set on the command
        // rather than only on the row, so that the audit hooks rewriting the row later keep the same value
        if (isNull(command.getCreatedAt())) {
            command.setCreatedAt(Instant.now());
        }

        final var commandEntity = mapper.map(command);

        commandEntity.setState(UNDER_PROCESSING);

        try {
            repository.save(commandEntity);
            command.setCommandId(commandEntity.getId());

            log.debug("Claimed idempotency key {} (distinctor '{}') with command id {}", command.getIdempotencyKey(),
                    command.getCustomIdempotencyDistinctor(), commandEntity.getId());

            return true;
        } catch (RuntimeException e) {
            if (isDuplicateKey(e)) {
                log.debug("Idempotency key {} (distinctor '{}') is already claimed", command.getIdempotencyKey(),
                        command.getCustomIdempotencyDistinctor());

                return false;
            }

            throw e;
        }
    }

    @Override
    public boolean matchesStoredRequest(String key, String distinctor, Object payload) {
        if (isNull(payload)) {
            return false;
        }

        // compared as text rather than as parsed nodes: the stored text was written by this same mapper, so an
        // identical request produces identical text, while parsing would round a decimal through a double and make
        // two amounts that differ only in their last digits look like the same request
        return repository.findRequestTextByIdempotencyKey(key, distinctor)
                .map(stored -> stored.equals(objectMapper.writeValueAsString(jsonMapper.map(payload)))).orElse(false);
    }

    @Override
    @Retry(name = "commandStore", fallbackMethod = "fallback")
    public void store(CommandContext<?, ?> ctx) {
        final long startedAt = System.nanoTime();

        final var command = ctx.getCommand();
        final var response = ctx.getResponse();
        final var state = ctx.getState();

        final var commandEntity = isNull(response) ? mapper.map(command) : mapper.map(command, response);

        if (state != null) {
            commandEntity.setState(state);
        }

        log.debug("Storing command idempotencyKey={}, state={}, payloadType={}, thread={}", command.getIdempotencyKey(),
                commandEntity.getState(), payloadType(command), Thread.currentThread().getName());

        try {
            repository.save(commandEntity);
            command.setCommandId(commandEntity.getId());
            log.debug("Stored command id={}, idempotencyKey={}, state={}, elapsedMs={}", commandEntity.getId(), command.getIdempotencyKey(),
                    commandEntity.getState(), elapsedMillis(startedAt));
        } catch (RuntimeException e) {
            log.warn("Command store save failed idempotencyKey={}, state={}, payloadType={}, elapsedMs={}; retry/fallback may follow",
                    command.getIdempotencyKey(), commandEntity.getState(), payloadType(command), elapsedMillis(startedAt), e);
            throw e;
        }
    }

    void fallback(CommandContext<?, ?> ctx, Throwable t) throws Exception {
        final var command = ctx.getCommand();
        final var state = ctx.getState();

        // a duplicate key is never a "store it later" problem: swallowing it would let a second request carry on as if
        // its command had been recorded, and execute again
        if (isDuplicateKey(t)) {
            log.error("Command store rejected a duplicate idempotency key {}", command.getIdempotencyKey(), t);

            throw t instanceof Exception e ? e : new IllegalStateException(t);
        }

        log.warn("Command store fallback idempotencyKey={}, state={}, payloadType={}, deadLetterQueueEnabled={}",
                command.getIdempotencyKey(), state, payloadType(command), properties.getFileDeadLetterQueueEnabled(), t);
        if (Boolean.TRUE.equals(properties.getFileDeadLetterQueueEnabled())) {
            write(command);
        }
    }

    @EventListener(ApplicationStartedEvent.class)
    void onStartup() {
        try {
            var created = Path.of(properties.getFileDeadLetterQueuePath()).toFile().mkdirs();
            log.info("Created command dead-letter queue: {} ({})", properties.getFileDeadLetterQueuePath(), created);
        } catch (Exception e) {
            log.error("Unable to initialize command dead-letter queue:", e);
        }
    }

    // Spring Data JDBC wraps the translated exception in a DbActionExecutionException, so the whole chain is inspected
    private static boolean isDuplicateKey(Throwable t) {
        for (var current = t; current != null; current = current.getCause()) {
            if (current instanceof DuplicateKeyException) {
                return true;
            }
        }

        return false;
    }

    private void write(Command<?> command) throws IOException {
        var file = Path
                .of(properties.getFileDeadLetterQueuePath(),
                        command.getCreatedAt().toEpochMilli() + "-"
                                + Optional.ofNullable(command.getIdempotencyKey()).orElseGet(() -> UUID.randomUUID().toString()) + ".json")
                .toFile();
        FileUtils.write(file, objectMapper.writeValueAsString(command), UTF_8);
    }

    private static long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    private static String payloadType(Command<?> command) {
        return Optional.ofNullable(command.getPayload()).map(Object::getClass).map(Class::getName).orElse("null");
    }
}
