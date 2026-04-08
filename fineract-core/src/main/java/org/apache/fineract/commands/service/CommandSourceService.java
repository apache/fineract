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

import static org.apache.fineract.commands.domain.CommandProcessingResultType.UNDER_PROCESSING;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.CommandNotFoundException;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaSystemException;
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

    public static final String COMMAND_MASK_VALUE = "***";
    public static final String COMMAND_SANITIZE_ALL = "SANITIZE_ALL";

    private final ConfigurationDomainService configurationDomainService;
    private final CommandSourceRepository commandSourceRepository;
    private final ErrorHandler errorHandler;
    private final FromJsonHelper fromApiJsonHelper;

    @NonNull
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public CommandSource saveInitialNewTransaction(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        return saveInitial(wrapper, jsonCommand, maker, idempotencyKey);
    }

    @NonNull
    @Transactional(propagation = Propagation.REQUIRED)
    public CommandSource saveInitialSameTransaction(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        return saveInitial(wrapper, jsonCommand, maker, idempotencyKey);
    }

    @NonNull
    private CommandSource saveInitial(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        try {
            CommandSource initialCommandSource = getInitialCommandSource(wrapper, jsonCommand, maker, idempotencyKey);
            return commandSourceRepository.saveAndFlush(initialCommandSource);
        } catch (JpaSystemException jse) {
            final String message = (jse.getRootCause() != null) ? jse.getRootCause().getMessage() : null;
            if (message != null && message.toUpperCase().contains("UNIQUE_PORTFOLIO_COMMAND_SOURCE")) {
                throw new IdempotentCommandProcessUnderProcessingException(wrapper, idempotencyKey, jse);
            }
            throw jse;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public CommandSource saveResultNewTransaction(@NonNull CommandSource commandSource) {
        return saveResult(commandSource);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CommandSource saveResultSameTransaction(@NonNull CommandSource commandSource) {
        return saveResult(commandSource);
    }

    @NonNull
    private CommandSource saveResult(@NonNull CommandSource commandSource) {
        return commandSourceRepository.saveAndFlush(commandSource);
    }

    public ErrorInfo generateErrorInfo(Throwable t) {
        return errorHandler.handle(ErrorHandler.getMappable(t));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CommandSource getCommandSource(Long commandSourceId) {
        return commandSourceRepository.findById(commandSourceId).orElseThrow(() -> new CommandNotFoundException(commandSourceId));
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CommandSource findCommandSource(CommandWrapper wrapper, String idempotencyKey) {
        return commandSourceRepository.findByActionNameAndEntityNameAndIdempotencyKey(wrapper.actionName(), wrapper.entityName(),
                idempotencyKey);
    }

    public CommandSource getInitialCommandSource(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        CommandSource commandSourceResult = CommandSource.fullEntryFrom(wrapper, jsonCommand, maker, idempotencyKey,
                UNDER_PROCESSING.getValue(), false);
        sanitizeJson(commandSourceResult, wrapper.getSanitizeJsonKeys());
        if (commandSourceResult.getCommandAsJson() == null) {
            commandSourceResult.setCommandAsJson("{}");
        }
        return commandSourceResult;
    }

    @Transactional
    public CommandProcessingResult processCommand(NewCommandSourceHandler handler, JsonCommand command, CommandSource commandSource,
            AppUser user, boolean isApprovedByChecker) {
        final CommandProcessingResult result = handler.processCommand(command);

        String permission = commandSource.getPermissionCode();
        boolean isMakerChecker = configurationDomainService.isMakerCheckerEnabledForTask(permission);
        if (isMakerChecker || result.isRollbackTransaction()) {
            if (isApprovedByChecker || user.isCheckerSuperUser()) {
                commandSource.markAsChecked(user);
            } else {
                if (commandSource.isSanitized()) {
                    throw new GeneralPlatformDomainRuleException("error.msg.invalid.sanitization",
                            "Maker-checker command can not be sanitized, please change the permission configuration", permission);
                }
                commandSource.markAsAwaitingApproval();
                throw new RollbackTransactionNotApprovedException(commandSource.getId(), commandSource.getResourceId());
            }
        }
        return result;
    }

    private void sanitizeJson(@NonNull CommandSource commandSource, Set<String> sanitizeKeys) {
        if (sanitizeKeys == null || sanitizeKeys.isEmpty()) {
            return;
        }
        String commandAsJson = commandSource.getCommandAsJson();
        if (commandAsJson == null || commandAsJson.isEmpty()) {
            return;
        }
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandAsJson);
        if (!parsedCommand.isJsonObject()) {
            return;
        }
        String sanitizedJson;
        if (sanitizeKeys.contains(COMMAND_SANITIZE_ALL)) {
            sanitizedJson = "";
        } else {
            JsonObject jsonObject = parsedCommand.getAsJsonObject();
            for (String key : sanitizeKeys) {
                if (jsonObject.has(key)) {
                    jsonObject.addProperty(key, COMMAND_MASK_VALUE);
                }
            }
            sanitizedJson = jsonObject.toString();
        }
        commandSource.setCommandAsJson(sanitizedJson);
        commandSource.setSanitized(true);
    }
}
