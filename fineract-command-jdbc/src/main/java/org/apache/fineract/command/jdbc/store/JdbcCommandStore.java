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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.jdbc.JdbcCommandProperties;
import org.apache.fineract.command.jdbc.store.domain.CommandEntity;
import org.apache.fineract.command.jdbc.store.domain.JdbcCommandRepository;
import org.apache.fineract.command.jdbc.store.mapping.JdbcCommandMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnMissingBean(value = CommandStore.class, ignored = JdbcCommandStore.class)
public class JdbcCommandStore implements CommandStore {

    private final JdbcCommandMapper mapper;
    private final JdbcCommandRepository repository;
    private final ObjectMapper objectMapper;
    private final JdbcCommandProperties properties;

    @Override
    public Command<Object> getById(Long id) {
        return repository.findById(id).map(mapper::map).orElse(null);
    }

    @Override
    public Command<Object> getByKey(String key) {
        return repository.findOneByIdempotencyKey(key).map(mapper::map).orElse(null);
    }

    @Override
    public boolean existsByKey(String key) {
        return repository.existsByIdempotencyKey(key);
    }

    @Override
    public boolean checkRequestInstanceByKey(String key, Class<?> clazz) {
        return repository.findOneByIdempotencyKey(key).map(CommandEntity::getResponse)
                .map(json -> clazz.getCanonicalName().equals(json.get(COMMAND_JSON_CLASS_ATTRIBUTE).asText())).orElse(false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getResponseByKey(String key) {
        return (T) repository.findOneByIdempotencyKey(key).map(CommandEntity::getResponse)
                .map(json -> objectMapper.convertValue(json, forName(json.get(COMMAND_JSON_CLASS_ATTRIBUTE).asText()))).orElse(null);
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

    @SneakyThrows
    private Class<?> forName(String clazz) {
        return Class.forName(clazz);
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
