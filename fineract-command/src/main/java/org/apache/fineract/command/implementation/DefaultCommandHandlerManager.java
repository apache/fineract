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

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.command.core.CommandHandlerManager;
import org.apache.fineract.command.core.exception.CommandHandlerDuplicateException;
import org.apache.fineract.command.core.exception.CommandHandlerInvalidRequestTypeException;
import org.apache.fineract.command.core.exception.CommandHandlerNotFoundException;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnMissingBean(value = CommandHandlerManager.class, ignored = DefaultCommandHandlerManager.class)
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DefaultCommandHandlerManager implements CommandHandlerManager {

    private final List<CommandHandler> handlers;

    @PostConstruct
    public void init() {
        ensureCommandHandlerUniqueness(this.handlers);
    }

    @Override
    public <REQ, RES> RES handle(Command<REQ> command) {
        CommandHandler<REQ, RES> handler = lookup(command);

        return handler.handle(command);
    }

    private <REQ, RES> CommandHandler<REQ, RES> lookup(Command<REQ> command) {
        if (command == null) {
            throw new CommandHandlerNotFoundException(command);
        }

        return handlers.stream().filter(handler -> handler.matches(command)).findFirst()
                .orElseThrow(() -> new CommandHandlerNotFoundException(command));
    }

    private void ensureCommandHandlerUniqueness(List<CommandHandler> commandHandlers) {
        final Set<Class<?>> registeredRequestTypes = new HashSet<>();

        for (CommandHandler handler : commandHandlers) {
            Class<?> userClass = AopProxyUtils.ultimateTargetClass(handler);

            Class<?> requestType = ResolvableType.forClass(userClass).as(CommandHandler.class).getGeneric(0).resolve();

            if (requestType == null) {
                log.error("Warning: Could not resolve request payload type for handler: {}", userClass.getName());
                throw new CommandHandlerInvalidRequestTypeException(userClass);
            }

            if (!registeredRequestTypes.add(requestType)) {

                log.error("Critical Configuration ERROR: Duplicate CommandHandlers detected for request type [{}]. Conflict found for: {}",
                        requestType.getName(), userClass.getSimpleName());

                throw new CommandHandlerDuplicateException(requestType, userClass);
            }
        }

        log.info("CommandHandler validation successful. Verified {} handler(s) uniquely map to their respective requests.",
                commandHandlers.size());
    }
}
