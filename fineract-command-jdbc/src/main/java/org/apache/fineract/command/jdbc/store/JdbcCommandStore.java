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
import static org.apache.fineract.command.core.CommandState.UNKNOWN;

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
import org.apache.fineract.command.core.CommandState;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.jdbc.JdbcCommandProperties;
import org.apache.fineract.command.jdbc.store.domain.CommandEntity;
import org.apache.fineract.command.jdbc.store.domain.CommandRepository;
import org.apache.fineract.command.jdbc.store.mapping.CommandMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnMissingBean(value = CommandStore.class, ignored = JdbcCommandStore.class)
public class JdbcCommandStore implements CommandStore {

    private final CommandMapper mapper;
    private final CommandRepository repository;
    private final ObjectMapper objectMapper;
    private final JdbcCommandProperties properties;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getRequestById(Long id) {
        return (T) repository.findById(id).map(CommandEntity::getRequest)
                .map(json -> objectMapper.convertValue(json, forName(json.get(COMMAND_JSON_CLASS_ATTRIBUTE).asText()))).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getResponseById(Long id) {
        return (T) repository.findById(id).map(CommandEntity::getResponse)
                .map(json -> objectMapper.convertValue(json, forName(json.get(COMMAND_JSON_CLASS_ATTRIBUTE).asText()))).orElse(null);
    }

    @Override
    public org.apache.fineract.command.core.CommandState getStateById(Long id) {
        return repository.findById(id).map(CommandEntity::getState).orElse(UNKNOWN);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getRequestByKey(String key) {
        return (T) repository.findOneByIdempotencyKey(key).map(CommandEntity::getRequest)
                .map(json -> objectMapper.convertValue(json, forName(json.get(COMMAND_JSON_CLASS_ATTRIBUTE).asText()))).orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getResponseByKey(String key) {
        return (T) repository.findOneByIdempotencyKey(key).map(CommandEntity::getResponse)
                .map(json -> objectMapper.convertValue(json, forName(json.get(COMMAND_JSON_CLASS_ATTRIBUTE).asText()))).orElse(null);
    }

    @Override
    public CommandState getStateByKey(String key) {
        return repository.findOneByIdempotencyKey(key).map(CommandEntity::getState).orElse(UNKNOWN);
    }

    @Override
    @Retry(name = "commandStore", fallbackMethod = "fallback")
    public void store(Command<?> command, Object response, CommandState state) {
        final var commandEntity = isNull(response) ? mapper.map(command) : mapper.map(command, response);

        if (state != null) {
            commandEntity.setState(state);
        }

        repository.save(commandEntity);

        command.setCommandId(commandEntity.getId());
    }

    void fallback(Command<?> command, Object response, CommandState state, Throwable t) throws Exception {
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
}
