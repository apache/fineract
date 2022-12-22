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
package org.apache.fineract.commands.service;

import static org.apache.fineract.commands.domain.CommandProcessingResultType.ERROR;
import static org.apache.fineract.commands.domain.CommandProcessingResultType.UNDER_PROCESSING;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.exception.ErrorHandler;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.CommandNotFoundException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Two phase transactional command processing: save initial...work...finish/failed to handle idempotent requests. As the
 * default isolation level for MYSQL is REPEATABLE_READ and a lower value READ_COMMITED for postgres, we can force to
 * use the same for both database backends to be consistent.
 */
@Component
@RequiredArgsConstructor
public class CommandSourceService {

    private final CommandSourceRepository commandSourceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public CommandSource saveInitial(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        CommandSource initialCommandSource = getInitialCommandSource(wrapper, jsonCommand, maker, idempotencyKey);

        if (initialCommandSource.getCommandJson() == null) {
            initialCommandSource.setCommandJson("{}");
        }

        return commandSourceRepository.saveAndFlush(initialCommandSource);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void saveFailed(CommandSource commandSource) {
        commandSource.setStatus(ERROR.getValue());
        commandSourceRepository.saveAndFlush(commandSource);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public CommandSource saveResult(CommandSource commandSource) {
        return commandSourceRepository.saveAndFlush(commandSource);
    }

    public ErrorInfo generateErrorException(Throwable t) {
        if (t instanceof final RuntimeException e) {
            return ErrorHandler.handler(e);
        } else {
            return new ErrorInfo(500, 9999, "{\"Exception\": " + t.toString() + "}");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public CommandSource findCommandSource(CommandWrapper wrapper, String idempotencyKey) {
        return commandSourceRepository.findByActionNameAndEntityNameAndIdempotencyKey(wrapper.actionName(), wrapper.entityName(),
                idempotencyKey);
    }

    private CommandSource getInitialCommandSource(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        CommandSource commandSourceResult;
        if (jsonCommand.commandId() != null) {
            commandSourceResult = commandSourceRepository.findById(jsonCommand.commandId())
                    .orElseThrow(() -> new CommandNotFoundException(jsonCommand.commandId()));
            commandSourceResult.markAsChecked(maker);
        } else {
            commandSourceResult = CommandSource.fullEntryFrom(wrapper, jsonCommand, maker, idempotencyKey, UNDER_PROCESSING.getValue());
        }
        return commandSourceResult;
    }
}
