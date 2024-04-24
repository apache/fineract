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
package org.apache.fineract.commands.mvc.service;

import static org.apache.fineract.commands.domain.CommandProcessingResultType.ERROR;
import static org.apache.fineract.commands.domain.CommandProcessingResultType.PROCESSED;
import static org.apache.http.HttpStatus.SC_OK;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.resilience4j.retry.annotation.Retry;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.commands.mvc.domain.CommandTypeWrapper;
import org.apache.fineract.commands.mvc.handler.NewCommandSourceHandlerType;
import org.apache.fineract.commands.mvc.provider.CommandHandlerProvider;
import org.apache.fineract.commands.service.IdempotencyKeyResolver;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.mvc.ProfileMvc;
import org.apache.fineract.infrastructure.core.api.mvc.TypeCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessFailedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessSucceedException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.hooks.event.HookEvent;
import org.apache.fineract.infrastructure.hooks.event.HookEventSource;
import org.apache.fineract.infrastructure.security.service.mvc.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@ProfileMvc
@Service("mvcSynchronousCommandProcessingService")
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
    private final CommandSourceService commandSourceService;

    private final FineractRequestContextHolder fineractRequestContextHolder;
    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    @Override
    @Retry(name = "executeCommand", fallbackMethod = "fallbackExecuteCommand")
    public <T> CommandProcessingResult executeCommand(final CommandTypeWrapper<T> typeWrapper, final TypeCommand<T> typeCommand,
            final boolean isApprovedByChecker) {
        final CommandWrapper wrapper = typeWrapper.getJsonWrapper(); // TODO: Remove after MVC migration
        final JsonCommand command = typeCommand.getJsonCommand();
        // Do not store the idempotency key because of the exception handling
        setIdempotencyKeyStoreFlag(false);

        Long commandId = (Long) fineractRequestContextHolder.getAttribute(COMMAND_SOURCE_ID, null);
        boolean isRetry = commandId != null;
        boolean isEnclosingTransaction = BatchRequestContextHolder.isEnclosingTransaction();

        CommandSource commandSource = null;
        String idempotencyKey;
        if (isRetry) {
            commandSource = commandSourceService.getCommandSource(commandId);
            idempotencyKey = commandSource.getIdempotencyKey();
        } else if ((commandId = command.commandId()) != null) { // action on the command itself
            commandSource = commandSourceService.getCommandSource(commandId);
            idempotencyKey = commandSource.getIdempotencyKey();
        } else {
            idempotencyKey = idempotencyKeyResolver.resolve(wrapper);
        }
        exceptionWhenTheRequestAlreadyProcessed(wrapper, idempotencyKey, isRetry);

        AppUser user = context.authenticatedUser(typeWrapper);
        if (commandSource == null) {
            if (isEnclosingTransaction) {
                commandSource = commandSourceService.getInitialCommandSource(wrapper, command, user, idempotencyKey);
            } else {
                commandSource = commandSourceService.saveInitialNewTransaction(wrapper, command, user, idempotencyKey);
                commandId = commandSource.getId();
            }
        }
        if (commandId != null) {
            storeCommandIdInContext(commandSource); // Store command id as a request attribute
        }

        boolean isMakerChecker = configurationDomainService.isMakerCheckerEnabledForTask(wrapper.taskPermissionName());
        if (isApprovedByChecker || (isMakerChecker && user.isCheckerSuperUser())) {
            commandSource.markAsChecked(user);
        }
        setIdempotencyKeyStoreFlag(true);

        final CommandProcessingResult result;
        try {
            result = commandSourceService.processCommand(findCommandHandler(typeWrapper), typeCommand, commandSource, user,
                    isApprovedByChecker, isMakerChecker);
        } catch (Throwable t) { // NOSONAR
            RuntimeException mappable = ErrorHandler.getMappable(t);
            ErrorInfo errorInfo = commandSourceService.generateErrorInfo(mappable);
            Integer statusCode = errorInfo.getStatusCode();
            commandSource.setResultStatusCode(statusCode);
            commandSource.setResult(errorInfo.getMessage());
            if (statusCode != SC_OK) {
                commandSource.setStatus(ERROR);
            }
            if (!isEnclosingTransaction) { // TODO: temporary solution
                commandSource = commandSourceService.saveResultNewTransaction(commandSource);
            }
            // must not throw any exception; must persist in new transaction as the current transaction was already
            // marked as rollback
            publishHookErrorEvent(wrapper, command, errorInfo);
            throw mappable;
        }

        commandSource.setResultStatusCode(SC_OK);
        commandSource.updateForAudit(result);
        commandSource.setResult(toApiJsonSerializer.serializeResult(result));
        commandSource.setStatus(PROCESSED);
        commandSource = commandSourceService.saveResultSameTransaction(commandSource);
        storeCommandIdInContext(commandSource); // Store command id as a request attribute

        result.setRollbackTransaction(null);
        publishHookEvent(wrapper.entityName(), wrapper.actionName(), command, result); // TODO must be performed in a
                                                                                       // new transaction
        return result;
    }

    private void storeCommandIdInContext(CommandSource savedCommandSource) {
        if (savedCommandSource.getId() == null) {
            throw new IllegalStateException("Command source not saved");
        }
        // Idempotency filters and retry need this
        fineractRequestContextHolder.setAttribute(COMMAND_SOURCE_ID, savedCommandSource.getId());
    }

    private void publishHookErrorEvent(CommandWrapper wrapper, JsonCommand command, ErrorInfo errorInfo) {
        publishHookEvent(wrapper.entityName(), wrapper.actionName(), command, gson.toJson(errorInfo));
    }

    private void exceptionWhenTheRequestAlreadyProcessed(CommandWrapper wrapper, String idempotencyKey, boolean retry) {
        CommandSource command = commandSourceService.findCommandSource(wrapper, idempotencyKey);
        if (command == null) {
            return;
        }
        CommandProcessingResultType status = CommandProcessingResultType.fromInt(command.getStatus());
        switch (status) {
            case UNDER_PROCESSING -> throw new IdempotentCommandProcessUnderProcessingException(wrapper, idempotencyKey);
            case PROCESSED -> throw new IdempotentCommandProcessSucceedException(wrapper, idempotencyKey, command);
            case ERROR -> {
                if (!retry) {
                    throw new IdempotentCommandProcessFailedException(wrapper, idempotencyKey, command);
                }
            }
            default -> {
            }
        }
    }

    private void setIdempotencyKeyStoreFlag(boolean flag) {
        fineractRequestContextHolder.setAttribute(IDEMPOTENCY_KEY_STORE_FLAG, flag);
    }

    @SuppressWarnings("unused")
    public CommandProcessingResult fallbackExecuteCommand(Exception e) {
        throw ErrorHandler.getMappable(e);
    }

    @SuppressWarnings("unchecked")
    private <T> NewCommandSourceHandlerType<T> findCommandHandler(final CommandTypeWrapper<T> wrapper) {
        NewCommandSourceHandlerType<T> handler;

        if (wrapper.isDatatableResource()) {
            if (wrapper.isCreateDatatable()) {
                handler = applicationContext.getBean("createDatatableCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isDeleteDatatable()) {
                handler = applicationContext.getBean("deleteDatatableCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isUpdateDatatable()) {
                handler = applicationContext.getBean("updateDatatableCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createDatatableEntryCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isUpdateMultiple()) {
                handler = applicationContext.getBean("updateOneToManyDatatableEntryCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isUpdateOneToOne()) {
                handler = applicationContext.getBean("updateOneToOneDatatableEntryCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isDeleteMultiple()) {
                handler = applicationContext.getBean("deleteOneToManyDatatableEntryCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isDeleteOneToOne()) {
                handler = applicationContext.getBean("deleteOneToOneDatatableEntryCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isRegisterDatatable()) {
                handler = applicationContext.getBean("registerDatatableCommandHandler", NewCommandSourceHandlerType.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isNoteResource()) {
            if (wrapper.isCreate()) {
                handler = applicationContext.getBean("createNoteCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isUpdate()) {
                handler = applicationContext.getBean("updateNoteCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isDelete()) {
                handler = applicationContext.getBean("deleteNoteCommandHandler", NewCommandSourceHandlerType.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isSurveyResource()) {
            if (wrapper.isRegisterSurvey()) {
                handler = applicationContext.getBean("registerSurveyCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.isFullFilSurvey()) {
                handler = applicationContext.getBean("fullFilSurveyCommandHandler", NewCommandSourceHandlerType.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else if (wrapper.isLoanDisburseDetailResource()) {
            if (wrapper.isUpdateDisbursementDate()) {
                handler = applicationContext.getBean("updateLoanDisburseDateCommandHandler", NewCommandSourceHandlerType.class);
            } else if (wrapper.addAndDeleteDisbursementDetails()) {
                handler = applicationContext.getBean("addAndDeleteLoanDisburseDetailsCommandHandler", NewCommandSourceHandlerType.class);
            } else {
                throw new UnsupportedCommandException(wrapper.commandName());
            }
        } else {
            handler = commandHandlerProvider.getHandler(wrapper.entityName(), wrapper.actionName());
        }

        return handler;
    }

    @Override
    public boolean validateRollbackCommand(final CommandTypeWrapper<?> commandWrapper, final AppUser user) {
        user.validateHasPermissionTo(commandWrapper.getTaskPermissionName());
        boolean isMakerChecker = configurationDomainService.isMakerCheckerEnabledForTask(commandWrapper.taskPermissionName());
        return isMakerChecker && !user.isCheckerSuperUser();
    }

    protected void publishHookEvent(final String entityName, final String actionName, JsonCommand command, final Object result) {

        final AppUser appUser = context.authenticatedUser(CommandTypeWrapper.wrap(actionName, entityName, null, null));

        final HookEventSource hookEventSource = new HookEventSource(entityName, actionName);

        // TODO: Add support for publishing array events
        if (command.json() != null) {
            Type type = new TypeToken<Map<String, Object>>() {

            }.getType();

            Map<String, Object> myMap;

            try {
                myMap = gson.fromJson(command.json(), type);
            } catch (Exception e) {
                throw new PlatformApiDataValidationException("error.msg.invalid.json", "The provided JSON is invalid.", new ArrayList<>(),
                        e);
            }

            Map<String, Object> reqmap = new HashMap<>();
            reqmap.put("entityName", entityName);
            reqmap.put("actionName", actionName);
            reqmap.put("createdBy", context.authenticatedUser().getId());
            reqmap.put("createdByName", context.authenticatedUser().getUsername());
            reqmap.put("createdByFullName", context.authenticatedUser().getDisplayName());

            reqmap.put("request", myMap);
            if (result instanceof CommandProcessingResult) {
                CommandProcessingResult resultCopy = CommandProcessingResult.fromCommandProcessingResult((CommandProcessingResult) result);

                reqmap.put("officeId", resultCopy.getOfficeId());
                reqmap.put("clientId", resultCopy.getClientId());
                resultCopy.setOfficeId(null);
                reqmap.put("response", resultCopy);
            } else if (result instanceof ErrorInfo ex) {
                reqmap.put("status", "Exception");

                Map<String, Object> errorMap = new HashMap<>();

                try {
                    errorMap = gson.fromJson(ex.getMessage(), type);
                } catch (Exception e) {
                    errorMap.put("errorMessage", ex.getMessage());
                }

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
    }
}
