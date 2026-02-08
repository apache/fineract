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
package org.apache.fineract.command.implementation;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.fineract.command.core.CommandConstants.COMMAND_JSON_CLASS_ATTRIBUTE;
import static org.apache.fineract.command.persistence.domain.CommandEntityState.ERROR;
import static org.apache.fineract.command.persistence.domain.CommandEntityState.PROCESSED;
import static org.apache.fineract.command.persistence.domain.CommandEntityState.UNDER_PROCESSING;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandAuditor;
import org.apache.fineract.command.core.CommandProperties;
import org.apache.fineract.command.persistence.domain.CommandEntity;
import org.apache.fineract.command.persistence.domain.CommandRepository;
import org.apache.fineract.command.persistence.mapping.CommandMapper;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultCommandAuditor implements CommandAuditor {

    private final CommandMapper mapper;
    private final CommandRepository repository;
    private final CommandProperties properties;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public Object getResponseByIdempotencyKey(String key) {
        final var result = repository.findOneByIdempotencyKey(key);
        final var response = result.map(CommandEntity::getResponse).orElse(null);

        if (response != null) {
            final var clazz = response.get(COMMAND_JSON_CLASS_ATTRIBUTE).asText();

            return objectMapper.convertValue(response, Class.forName(clazz));
        }

        return null;
    }

    @Retry(name = "commandAuditProcessing", fallbackMethod = "fallback")
    @Override
    public void processing(Command<?> command) {
        if (Boolean.TRUE.equals(properties.getAuditable())) {
            final var now = Instant.now();
            final var commandEntity = mapper.map(command);

            commandEntity.setCreatedAt(now);
            commandEntity.setUpdatedAt(now);
            commandEntity.setState(UNDER_PROCESSING);

            repository.save(commandEntity);

            command.setCreatedAt(now);
            command.setUpdatedAt(now);
            command.setCommandId(commandEntity.getId());
        }
    }

    @Retry(name = "commandAuditProcessed", fallbackMethod = "fallback")
    @Override
    public void processed(Command<?> command, Object response) {
        if (Boolean.TRUE.equals(properties.getAuditable())) {
            final var now = Instant.now();
            final var commandEntity = mapper.map(command, response);

            // TODO: improve when maker-checker functionality lands
            commandEntity.setExecutedByUsername(command.getInitiatedByUsername());
            commandEntity.setUpdatedAt(now);
            commandEntity.setExecutedAt(now);
            commandEntity.setState(PROCESSED);

            repository.save(commandEntity);

            command.setUpdatedAt(now);
            command.setExecutedAt(now);
        }
    }

    @Retry(name = "commandAuditError", fallbackMethod = "fallback")
    @Override
    public void error(Command<?> command) {
        if (Boolean.TRUE.equals(properties.getAuditable())) {
            final var now = Instant.now();
            final var commandEntity = mapper.map(command);

            commandEntity.setExecutedAt(now);
            commandEntity.setUpdatedAt(now);
            commandEntity.setState(ERROR);

            repository.save(commandEntity);

            command.setUpdatedAt(now);
            command.setExecutedAt(now);
        }
    }

    @EventListener(ApplicationStartedEvent.class)
    void onStartup() {
        if (Boolean.TRUE.equals(properties.getFileDeadLetterQueueEnabled())) {
            try {
                var created = Path.of(properties.getFileDeadLetterQueuePath()).toFile().mkdirs();
                log.info("Created command dead-letter queue: {} ({})", properties.getFileDeadLetterQueuePath(), created);
            } catch (Exception e) {
                log.error("Unable to initialize command dead-letter queue:", e);
            }
        }
    }

    @Retry(name = "commandAuditFallback", fallbackMethod = "fatal")
    @SuppressWarnings("UnusedMethod")
    void fallback(Command<?> command, Throwable t) throws Exception {
        if (Boolean.TRUE.equals(properties.getFileDeadLetterQueueEnabled())) {
            fallback(command, null, t);
        } else {
            fatal(command, t);
        }
    }

    @Retry(name = "commandAuditFallback", fallbackMethod = "fatal")
    @SuppressWarnings("UnusedMethod")
    void fallback(Command<?> command, Object response, Throwable t) throws Exception {
        if (Boolean.TRUE.equals(properties.getFileDeadLetterQueueEnabled())) {
            setError(command, t);
            write(command);
        } else {
            fatal(command, t);
        }
    }

    @SuppressWarnings("UnusedMethod")
    void fatal(Command<?> command, Throwable t) {
        fatal(command, null, t);
    }

    @SuppressWarnings("UnusedMethod")
    void fatal(Command<?> command, Object response, Throwable t) {
        // note: last line of defense; if this fails then all is lost
        setError(command, t);
        log.error("Command audit error: {}", command, t);
    }

    private void setError(Command<?> command, Throwable t) {
        if (command != null && t != null) {
            command.setUpdatedAt(Instant.now());
            command.setError(t.getMessage());
        }
    }

    private void write(Command<?> command) throws IOException {
        var file = Path.of(properties.getFileDeadLetterQueuePath(), command.getCreatedAt().toEpochMilli() + ".json").toFile();
        FileUtils.write(file, objectMapper.writeValueAsString(command), UTF_8);
    }
}
