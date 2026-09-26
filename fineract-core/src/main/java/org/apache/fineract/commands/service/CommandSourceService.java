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
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.domain.SavingsDepositExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.exception.CommandNotFoundException;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.commands.handler.SavingsDepositCommandHandler;
import org.apache.fineract.commands.handler.SavingsTransactionCommandHandler;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.jspecify.annotations.NonNull;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command source persistence and transactional command execution helpers. The initial command source is persisted
 * separately for idempotency, while command execution and successful result persistence run in one transaction.
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
    public CommandSource saveInitial(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        try {
            CommandSource initialCommandSource = getInitialCommandSource(wrapper, jsonCommand, maker, idempotencyKey);
            return commandSourceRepository.saveAndFlush(initialCommandSource);
        } catch (JpaSystemException jse) {
            final String message = (jse.getRootCause() != null) ? jse.getRootCause().getMessage() : null;
            if (message != null && message.toUpperCase(Locale.ROOT).contains("UNIQUE_PORTFOLIO_COMMAND_SOURCE")) {
                throw new IdempotentCommandProcessUnderProcessingException(wrapper, idempotencyKey, jse);
            }
            throw jse;
        }
    }

    @Transactional
    public void saveResult(Long commandSourceId, Integer response, String body) {
        commandSourceRepository.findById(commandSourceId).ifPresent(commandSource -> {
            commandSource.setResultStatusCode(response);
            commandSource.setResult(body);
            saveResult(commandSource);
        });
    }

    @NonNull
    public CommandSource saveResult(@NonNull CommandSource commandSource) {
        return commandSourceRepository.saveAndFlush(commandSource);
    }

    public ErrorInfo generateErrorInfo(Throwable t) {
        return errorHandler.handle(ErrorHandler.getMappable(t));
    }

    public CommandSource getCommandSource(Long commandSourceId) {
        return commandSourceRepository.findById(commandSourceId).orElseThrow(() -> new CommandNotFoundException(commandSourceId));
    }

    public CommandSource findCommandSource(CommandWrapper wrapper, String idempotencyKey) {
        return commandSourceRepository.findByActionNameAndEntityNameAndIdempotencyKey(wrapper.actionName(), wrapper.entityName(),
                idempotencyKey);
    }

    public CommandSource getInitialCommandSource(CommandWrapper wrapper, JsonCommand jsonCommand, AppUser maker, String idempotencyKey) {
        CommandSource commandSourceResult = CommandSource.fullEntryFrom(wrapper, jsonCommand, maker, idempotencyKey,
                UNDER_PROCESSING.getValue(), false);
        if (SavingsTransactionCommandEnvelope.appliesTo(wrapper.actionName(), wrapper.entityName())) {
            // Reject client metadata before any audit masking could remove the reserved key.
            SavingsTransactionCommandEnvelope.clientPayload(jsonCommand.json(),
                    SavingsTransactionKind.fromCommand(wrapper.actionName(), wrapper.entityName()));
        }
        sanitizeJson(commandSourceResult, wrapper.getSanitizeJsonKeys());
        if (commandSourceResult.getCommandAsJson() == null) {
            commandSourceResult.setCommandAsJson("{}");
        }
        if (SavingsTransactionCommandEnvelope.appliesTo(wrapper.actionName(), wrapper.entityName())) {
            commandSourceResult.setCommandAsJson(SavingsTransactionCommandEnvelope.encode(commandSourceResult.getCommandAsJson(),
                    SavingsTransactionKind.fromCommand(wrapper.actionName(), wrapper.entityName()), wrapper.getSavingsTransactionOrigin()));
        }
        return commandSourceResult;
    }

    @Transactional
    public CommandExecutionResult processCommandAndSaveResult(NewCommandSourceHandler handler, JsonCommand command,
            CommandSource commandSource, AppUser user, boolean isApprovedByChecker,
            BiConsumer<CommandSource, CommandProcessingResult> resultUpdater) {
        final CommandProcessingResult result;
        if (SavingsTransactionCommandEnvelope.appliesTo(commandSource.getActionName(), commandSource.getEntityName())) {
            var kind = SavingsTransactionKind.fromCommand(commandSource.getActionName(), commandSource.getEntityName());
            var decoded = SavingsTransactionCommandEnvelope.decode(commandSource.getCommandAsJson(), kind);
            JsonCommand flatCommand = JsonCommand.fromExistingCommand(command.commandId(), decoded.payload().toString(), decoded.payload(),
                    fromApiJsonHelper, commandSource.getEntityName(), commandSource.getResourceId(), commandSource.getSubResourceId(),
                    commandSource.getGroupId(), commandSource.getClientId(), commandSource.getLoanId(), commandSource.getSavingsId(),
                    commandSource.getTransactionId(), commandSource.getResourceGetUrl(), commandSource.getProductId(),
                    commandSource.getCreditBureauId(), commandSource.getOrganisationCreditBureauId(), commandSource.getJobName(),
                    commandSource.getLoanExternalId());
            if (kind == SavingsTransactionKind.DEPOSIT && handler instanceof SavingsDepositCommandHandler depositHandler) {
                result = depositHandler.processDeposit(flatCommand,
                        new SavingsDepositExecutionContext(
                                org.apache.fineract.commands.domain.SavingsDepositOrigin.valueOf(decoded.origin().name()),
                                commandSource.getMaker()));
            } else if (handler instanceof SavingsTransactionCommandHandler transactionHandler) {
                result = transactionHandler.processTransaction(flatCommand,
                        new SavingsTransactionExecutionContext(kind, decoded.origin(), commandSource.getMaker()));
            } else {
                throw SavingsTransactionCommandEnvelope.untrustedOrigin(kind);
            }
        } else {
            result = handler.processCommand(command);
        }
        validateMakerChecker(commandSource, user, isApprovedByChecker, result);
        resultUpdater.accept(commandSource, result);
        return new CommandExecutionResult(result, saveResult(commandSource));
    }

    private void validateMakerChecker(CommandSource commandSource, AppUser user, boolean isApprovedByChecker,
            CommandProcessingResult result) {
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
    }

    public record CommandExecutionResult(CommandProcessingResult result, CommandSource commandSource) {
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
