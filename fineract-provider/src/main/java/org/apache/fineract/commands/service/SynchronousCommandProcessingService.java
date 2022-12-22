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
import static org.apache.fineract.commands.domain.CommandProcessingResultType.PROCESSED;
import static org.apache.fineract.commands.domain.CommandProcessingResultType.UNDER_PROCESSING;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.resilience4j.retry.annotation.Retry;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.commands.provider.CommandHandlerProvider;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessFailedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessSucceedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.hooks.event.HookEvent;
import org.apache.fineract.infrastructure.hooks.event.HookEventSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SynchronousCommandProcessingService implements CommandProcessingService {

    public static final String IDEMPOTENCY_KEY_STORE_FLAG = "idempotencyKeyStoreFlag";

    public static final String IDEMPOTENCY_KEY_ATTRIBUTE = "IdempotencyKeyAttribute";
    public static final String COMMAND_SOURCE_ID = "commandSourceId";
    private final PlatformSecurityContext context;
    private final ApplicationContext applicationContext;
    private final ToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer;
    private final ToApiJsonSerializer<CommandProcessingResult> toApiResultJsonSerializer;
    private final ConfigurationDomainService configurationDomainService;
    private final CommandHandlerProvider commandHandlerProvider;
    private final IdempotencyKeyResolver idempotencyKeyResolver;
    private final IdempotencyKeyGenerator idempotencyKeyGenerator;
    private final CommandSourceService commandSourceService;

    private final FineractRequestContextHolder fineractRequestContextHolder;
    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @Override
    @Retry(name = "executeCommand", fallbackMethod = "fallbackExecuteCommand")
    public CommandProcessingResult executeCommand(final CommandWrapper wrapper, final JsonCommand command,
            final boolean isApprovedByChecker) {
        // Do not store the idempotency key because of the exception handling
        setIdempotencyKeyStoreFlag(false);

        final boolean rollbackTransaction = configurationDomainService.isMakerCheckerEnabledForTask(wrapper.taskPermissionName());
        String idempotencyKey = idempotencyKeyResolver.resolve(wrapper);
        exceptionWhenTheRequestAlreadyProcessed(wrapper, idempotencyKey);

        // Store idempotency key to the request attribute

        CommandSource savedCommandSource = commandSourceService.saveInitial(wrapper, command, context.authenticatedUser(wrapper),
                idempotencyKey);
        storeCommandToIdempotentFilter(savedCommandSource);
        setIdempotencyKeyStoreFlag(true);

        final CommandProcessingResult result;
        try {
            result = findCommandHandler(wrapper).processCommand(command);
        } catch (Throwable t) { // NOSONAR
            commandSourceService.saveFailed(commandSourceService.findCommandSource(wrapper, idempotencyKey));
            publishHookErrorEvent(wrapper, command, t);
            throw t;
        }

        CommandSource initialCommandSource = commandSourceService.findCommandSource(wrapper, idempotencyKey);
        initialCommandSource.setResult(toApiJsonSerializer.serializeResult(result));
        initialCommandSource.updateResourceId(result.getResourceId());
        initialCommandSource.updateForAudit(result);

        boolean rollBack = (rollbackTransaction || result.isRollbackTransaction()) && !isApprovedByChecker;
        if (result.hasChanges() && !rollBack) {
            initialCommandSource.setCommandJson(toApiJsonSerializer.serializeResult(result.getChanges()));
        }

        initialCommandSource.setStatus(CommandProcessingResultType.PROCESSED.getValue());
        commandSourceService.saveResult(initialCommandSource);

        if ((rollbackTransaction || result.isRollbackTransaction()) && !isApprovedByChecker) {
            /*
             * JournalEntry will generate a new transactionId every time. Updating the transactionId with old
             * transactionId, because as there are no entries are created with new transactionId, will throw an error
             * when checker approves the transaction
             */
            initialCommandSource.updateTransaction(command.getTransactionId());
            /*
             * Update CommandSource json data with JsonCommand json data, line 77 and 81 may update the json data
             */
            initialCommandSource.setCommandJson(command.json());
            throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(initialCommandSource);
        }
        result.setRollbackTransaction(null);

        publishHookEvent(wrapper.entityName(), wrapper.actionName(), command, result);

        return result;
    }

    private void storeCommandToIdempotentFilter(CommandSource savedCommandSource) {
        if (savedCommandSource.getId() == null) {
            throw new IllegalStateException("Command source not saved");
        }
        saveCommandToRequest(savedCommandSource);
    }

    private void saveCommandToRequest(CommandSource savedCommandSource) {
        fineractRequestContextHolder.setAttribute(COMMAND_SOURCE_ID, savedCommandSource.getId());
    }

    private void publishHookErrorEvent(CommandWrapper wrapper, JsonCommand command, Throwable t) {
        ErrorInfo ex = commandSourceService.generateErrorException(t);
        publishHookEvent(wrapper.entityName(), wrapper.actionName(), command, gson.toJson(ex));
    }

    private void exceptionWhenTheRequestAlreadyProcessed(CommandWrapper wrapper, String idempotencyKey) {
        CommandSource existingCommand = commandSourceService.findCommandSource(wrapper, idempotencyKey);
        if (existingCommand != null) {
            idempotentExceptionByStatus(UNDER_PROCESSING, existingCommand,
                    command -> new IdempotentCommandProcessUnderProcessingException(wrapper));
            idempotentExceptionByStatus(ERROR, existingCommand, command -> new IdempotentCommandProcessFailedException(wrapper, command));
            idempotentExceptionByStatus(PROCESSED, existingCommand,
                    command -> new IdempotentCommandProcessSucceedException(wrapper, command.getResult()));
        }
    }

    private void idempotentExceptionByStatus(CommandProcessingResultType status, CommandSource command,
            Function<CommandSource, AbstractIdempotentCommandException> exceptionMapper) {
        if (status.getValue().equals(command.getStatus())) {
            throw exceptionMapper.apply(command);
        }
    }

    private void setIdempotencyKeyStoreFlag(boolean flag) {
        fineractRequestContextHolder.setAttribute(IDEMPOTENCY_KEY_STORE_FLAG, flag);
    }

    @Transactional
    @Override
    public CommandProcessingResult logCommand(CommandSource commandSourceResult) {
        commandSourceResult.markAsAwaitingApproval();
        if (commandSourceResult.getIdempotencyKey() == null) {
            commandSourceResult.setIdempotencyKey(idempotencyKeyGenerator.create());
        }
        commandSourceResult = commandSourceService.saveResult(commandSourceResult);

        return new CommandProcessingResultBuilder().withCommandId(commandSourceResult.getId())
                .withEntityId(commandSourceResult.getResourceId()).build();
    }

    @SuppressWarnings("unused")
    private CommandProcessingResult fallbackExecuteCommand(Exception e) throws Exception {
        if (e instanceof RollbackTransactionAsCommandIsNotApprovedByCheckerException ex) {
            return logCommand(ex.getCommandSourceResult());
        }

        throw e;
    }

    private NewCommandSourceHandler findCommandHandler(final CommandWrapper wrapper) {
        NewCommandSourceHandler handler;

        if (wrapper.isDatatableResource()) {
            if (wrapper.isCreateDatatable()) {
                handler = applicationContext.getBean("createDatatableCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteDatatable()) {
                handler = applicationContext.getBean("deleteDatatableCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateDatatable()) {
                handler = applicationContext.getBean("updateDatatableCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateMultiple()) {
                handler = applicationContext.getBean("updateOneToManyDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdateOneToOne()) {
                handler = applicationContext.getBean("updateOneToOneDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteMultiple()) {
                handler = applicationContext.getBean("deleteOneToManyDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDeleteOneToOne()) {
                handler = applicationContext.getBean("deleteOneToOneDatatableEntryCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isRegisterDatatable()) {
                handler = applicationContext.getBean("registerDatatableCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isNoteResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createNoteCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateNoteCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteNoteCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isSurveyResource()) {
            if (wrapper.isRegisterSurvey()) {
                handler = applicationContext.getBean("registerSurveyCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.isFullFilSurvey()) {
                handler = applicationContext.getBean("fullFilSurveyCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanDisburseDetailResource()) {
            if (wrapper.isUpdateDisbursementDate()) {
                handler = applicationContext.getBean("updateLoanDisburseDateCommandHandler", NewCommandSourceHandler.class);
            } else if (wrapper.addAndDeleteDisbursementDetails()) {
                handler = applicationContext.getBean("addAndDeleteLoanDisburseDetailsCommandHandler", NewCommandSourceHandler.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else {
            handler = commandHandlerProvider.getHandler(wrapper.entityName(), wrapper.actionName());
        }

        return handler;
    }

    @Override
    public boolean validateCommand(final CommandWrapper commandWrapper, final AppUser user) {
        boolean rollbackTransaction = configurationDomainService.isMakerCheckerEnabledForTask(commandWrapper.taskPermissionName());
        user.validateHasPermissionTo(commandWrapper.getTaskPermissionName());
        return rollbackTransaction;
    }

    private void publishHookEvent(final String entityName, final String actionName, JsonCommand command, final Object result) {

        try {
            final AppUser appUser = context.authenticatedUser(CommandWrapper.wrap(actionName, entityName, null, null));

            final HookEventSource hookEventSource = new HookEventSource(entityName, actionName);

            // TODO: Add support for publishing array events
            if (command.json() != null && command.json().startsWith("{")) {
                Type type = new TypeToken<Map<String, Object>>() {

                }.getType();
                Map<String, Object> myMap = gson.fromJson(command.json(), type);

                Map<String, Object> reqmap = new HashMap<>();
                reqmap.put("entityName", entityName);
                reqmap.put("actionName", actionName);
                reqmap.put("createdBy", context.authenticatedUser().getId());
                reqmap.put("createdByName", context.authenticatedUser().getUsername());
                reqmap.put("createdByFullName", context.authenticatedUser().getDisplayName());

                reqmap.put("request", myMap);
                if (result instanceof CommandProcessingResult) {
                    CommandProcessingResult resultCopy = CommandProcessingResult
                            .fromCommandProcessingResult((CommandProcessingResult) result);

                    reqmap.put("officeId", resultCopy.getOfficeId());
                    reqmap.put("clientId", resultCopy.getClientId());
                    resultCopy.setOfficeId(null);
                    reqmap.put("response", resultCopy);
                } else if (result instanceof ErrorInfo ex) {
                    reqmap.put("status", "Exception");

                    Map<String, Object> errorMap = gson.fromJson(ex.getMessage(), type);
                    errorMap.put("errorCode", ex.getErrorCode());
                    errorMap.put("statusCode", ex.getStatusCode());

                    reqmap.put("response", errorMap);
                }

                reqmap.put("timestamp", Instant.now().toString());

                final String serializedResult = toApiResultJsonSerializer.serialize(reqmap);

                final HookEvent applicationEvent = new HookEvent(hookEventSource, serializedResult, appUser,
                        ThreadLocalContextUtil.getContext());

                applicationContext.publishEvent(applicationEvent);
            }
        } catch (Exception e) {
            log.error("Error", e);
        }
    }
}
