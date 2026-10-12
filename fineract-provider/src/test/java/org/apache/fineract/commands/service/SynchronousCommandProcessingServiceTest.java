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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.commands.provider.CommandHandlerProvider;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SynchronousCommandProcessingServiceTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private TransactionBoundApplicationEventPublisher eventPublisher;
    @Mock
    private ToApiJsonSerializer<Map<String, Object>> toApiJsonSerializer;
    @Mock
    private ToApiJsonSerializer<CommandProcessingResult> toApiResultJsonSerializer;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private CommandHandlerProvider commandHandlerProvider;
    @Mock
    private IdempotencyKeyResolver idempotencyKeyResolver;
    @Mock
    private CommandSourceService commandSourceService;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private FineractProperties fineractProperties;

    @Mock
    private RetryConfigurationAssembler retryConfigurationAssembler;

    @Spy
    private FineractRequestContextHolder fineractRequestContextHolder;

    @InjectMocks
    private SynchronousCommandProcessingService underTest;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        RequestContextHolder.resetRequestAttributes();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ErrorInfo errorInfo = mock(ErrorInfo.class);
        when(errorInfo.getMessage()).thenReturn("Failed");
        when(errorInfo.getStatusCode()).thenReturn(500);
        when(commandSourceService.generateErrorInfo(any())).thenReturn(errorInfo);

        FineractProperties.RetryProperties settings = new FineractProperties.RetryProperties();
        settings.setInstances(new FineractProperties.RetryProperties.InstancesProperties());
        settings.getInstances().setExecuteCommand(new FineractProperties.RetryProperties.InstancesProperties.ExecuteCommandProperties());
        settings.getInstances().getExecuteCommand().setMaxAttempts(3);
        settings.getInstances().getExecuteCommand().setWaitDuration(Duration.ofMillis(1));
        settings.getInstances().getExecuteCommand().setEnableExponentialBackoff(false);
        settings.getInstances().getExecuteCommand()
                .setRetryExceptions(new Class[] { RetryException.class, IdempotentCommandProcessUnderProcessingException.class });
        when(fineractProperties.getRetry()).thenReturn(settings);
        when(retryRegistry.retry(anyString(), any(RetryConfig.class)))
                .thenAnswer(i -> Retry.of((String) i.getArgument(0), (RetryConfig) i.getArgument(1)));

        var impl = new RetryConfigurationAssembler(retryRegistry, fineractProperties, fineractRequestContextHolder);
        var retry = impl.getRetryConfigurationForExecuteCommand();
        when(retryConfigurationAssembler.getRetryConfigurationForExecuteCommand()).thenReturn(retry);

        when(commandSourceService.processCommandAndSaveResult(any(NewCommandSourceHandler.class), any(JsonCommand.class),
                any(CommandSource.class), any(AppUser.class), Mockito.anyBoolean(),
                Mockito.<BiConsumer<CommandSource, CommandProcessingResult>>any())).thenAnswer(invocation -> {
                    NewCommandSourceHandler handler = invocation.getArgument(0);
                    JsonCommand command = invocation.getArgument(1);
                    CommandSource commandSource = invocation.getArgument(2);
                    BiConsumer<CommandSource, CommandProcessingResult> resultUpdater = invocation.getArgument(5);

                    CommandProcessingResult result = handler.processCommand(command);
                    resultUpdater.accept(commandSource, result);
                    CommandSource savedCommandSource = commandSourceService.saveResult(commandSource);
                    return new CommandSourceService.CommandExecutionResult(result, savedCommandSource);
                });
    }

    @AfterEach
    public void teardown() {
        reset(context);
        reset(applicationContext);
        reset(toApiJsonSerializer);
        reset(toApiResultJsonSerializer);
        reset(configurationDomainService);
        reset(commandHandlerProvider);
        reset(idempotencyKeyResolver);
        reset(commandSourceService);
        reset(retryConfigurationAssembler);
    }

    @Test
    public void testExecuteCommandSuccessAfter2Fails() {
        CommandWrapper commandWrapper = getCommandWrapper();

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenThrow(new RetryException()).thenThrow(new RetryException())
                .thenReturn(commandProcessingResult);

        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);
        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(null);
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.PROCESSED.getValue());
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        assertEquals(CommandProcessingResultType.PROCESSED.getValue(), commandSource.getStatus());
        assertEquals(commandProcessingResult, actualCommandProcessingResult);
        verify(commandSourceService, never()).generateErrorInfo(any());
        verify(commandSourceService).saveResult(commandSource);
    }

    /**
     * Test that an instance picked up an already under processing command. We assume that during retry timeouts it
     * stays in the same status therefor it should fail after reaching max retry count.
     */
    @Test
    public void executeCommandShouldFailAfterRetriesWithIdempotentCommandProcessUnderProcessingException() {
        CommandWrapper commandWrapper = Mockito.mock(CommandWrapper.class);
        when(commandWrapper.isDatatableResource()).thenReturn(false);
        when(commandWrapper.isNoteResource()).thenReturn(false);
        when(commandWrapper.isSurveyResource()).thenReturn(false);
        when(commandWrapper.isLoanDisburseDetailResource()).thenReturn(false);

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);

        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);

        when(commandSourceService.findCommandSource(any(), any())).thenReturn(commandSource);

        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue());
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(retryConfigurationAssembler.getLastException()).thenReturn(null)
                .thenAnswer((i) -> IdempotentCommandProcessUnderProcessingException.class)
                .thenAnswer((i) -> IdempotentCommandProcessUnderProcessingException.class);

        assertThrows(IdempotentCommandProcessUnderProcessingException.class,
                () -> underTest.executeCommand(commandWrapper, jsonCommand, false));

        verify(commandSource, times(3)).getStatus();
        assertEquals(CommandProcessingResultType.UNDER_PROCESSING.getValue(), commandSource.getStatus());
        verify(commandSourceService, times(0)).generateErrorInfo(any());
        verify(commandSourceService, times(0)).saveResult(commandSource);
    }

    /**
     * Test that an instance picked up an already under processing command. We assume that during retry timeouts it is
     * moved out from retry and the process can pick it up. We expect 2 fails then the third time the command is
     * processable.
     */
    @Test
    public void executeCommandShouldPassAfter1retryFailsByIdempotentCommandProcessUnderProcessingException() {
        CommandWrapper commandWrapper = Mockito.mock(CommandWrapper.class);
        when(commandWrapper.isDatatableResource()).thenReturn(false);
        when(commandWrapper.isNoteResource()).thenReturn(false);
        when(commandWrapper.isSurveyResource()).thenReturn(false);
        when(commandWrapper.isLoanDisburseDetailResource()).thenReturn(false);

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);

        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);

        when(commandSourceService.findCommandSource(any(), any())).thenReturn(commandSource);

        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue()) //
                .thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue()) //
                // Is it possible???
                .thenReturn(CommandProcessingResultType.AWAITING_APPROVAL.getValue()) //
        ;
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(retryConfigurationAssembler.getLastException()).thenReturn(null)
                .thenAnswer((i) -> IdempotentCommandProcessUnderProcessingException.class);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSource, times(3)).getStatus();
        verify(commandSourceService, times(0)).generateErrorInfo(any());
        verify(commandSourceService).saveResult(commandSource);
        assertEquals(actualCommandProcessingResult, commandProcessingResult);
    }

    /**
     * Test that an instance picked up a new command. During first processing, we expect a retryable exception to
     * happen, but commandSource should have already UNDER_PROCESSING status. We should try to reprocess. After 2nd time
     * fail, status should be still the same. On 3rd try it should result no error.
     */
    @Test
    public void executeCommandShouldPassAfter2RetriesOnRetryExceptionAndWithStuckStatus() {
        CommandWrapper commandWrapper = Mockito.mock(CommandWrapper.class);
        when(commandWrapper.isDatatableResource()).thenReturn(false);
        when(commandWrapper.isNoteResource()).thenReturn(false);
        when(commandWrapper.isSurveyResource()).thenReturn(false);
        when(commandWrapper.isLoanDisburseDetailResource()).thenReturn(false);

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand))
                // first time
                .thenThrow(new RetryException())
                // look like stuck and fails
                .thenThrow(new RetryException())
                // look like stuck and pass
                .thenReturn(commandProcessingResult);

        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);

        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);

        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.findCommandSource(any(), any())).thenReturn(null) // simulate new Command
                .thenReturn(commandSource) // simulate stuck Command
                .thenReturn(commandSource); // simulate stuck Command

        when(commandSource.getStatus())
                // on first hit we don't have a command source because it is new.
                // on 2nd hit we have a stuck one
                .thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue()) //
                .thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue()); //

        when(retryConfigurationAssembler.getLastException()).thenAnswer((i) -> RetryException.class)
                .thenAnswer((i) -> RetryException.class);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSource, times(2)).getStatus();
        assertEquals(CommandProcessingResultType.UNDER_PROCESSING.getValue(), commandSource.getStatus());
        verify(commandSourceService, never()).generateErrorInfo(any());
        verify(commandSourceService).saveResult(commandSource);
        assertEquals(actualCommandProcessingResult, commandProcessingResult);
    }

    @Test
    public void testExecuteCommandSuccess() {
        CommandWrapper commandWrapper = getCommandWrapper();

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);
        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(null);
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.PROCESSED.getValue());
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSourceService).getCommandSource(commandId);
        assertEquals(CommandProcessingResultType.PROCESSED.getValue(), commandSource.getStatus());
        verify(commandSourceService).saveResult(commandSource);

        assertEquals(commandProcessingResult, actualCommandProcessingResult);
    }

    @Test
    public void testExecuteCommandFails() {
        CommandWrapper commandWrapper = getCommandWrapper();
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        Long commandId = jsonCommand.commandId();

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(1L);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        RuntimeException runtimeException = new RuntimeException("foo");
        when(commandHandler.processCommand(jsonCommand)).thenThrow(runtimeException);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(null);
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);

        CommandSource initialCommandSource = Mockito.mock(CommandSource.class);

        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(initialCommandSource);

        assertThrows(RuntimeException.class, () -> {
            underTest.executeCommand(commandWrapper, jsonCommand, false);
        });

        verify(commandSourceService).getCommandSource(commandId);
        verify(commandSourceService).generateErrorInfo(runtimeException);
    }

    @NonNull
    private static CommandWrapper getCommandWrapper() {
        CommandWrapper commandWrapper = Mockito.mock(CommandWrapper.class);
        when(commandWrapper.isDatatableResource()).thenReturn(false);
        when(commandWrapper.isNoteResource()).thenReturn(false);
        when(commandWrapper.isSurveyResource()).thenReturn(false);
        when(commandWrapper.isLoanDisburseDetailResource()).thenReturn(false);
        return commandWrapper;
    }

    @Test
    public void publishHookEventHandlesInvalidJson() {
        String entityName = "entity";
        String actionName = "action";
        JsonCommand command = Mockito.mock(JsonCommand.class);
        String invalidJson = "{ invalidJson }";

        when(command.json()).thenReturn(invalidJson);

        // Test that no exception is thrown (exceptions are caught and logged)
        assertDoesNotThrow(() -> {
            underTest.publishHookEvent(entityName, actionName, command, Object.class);
        });
    }

    private static final class RetryException extends RuntimeException {}

    @Test
    public void testExecuteCommandWithRetry() {
        CommandWrapper commandWrapper = getCommandWrapper();
        when(commandWrapper.isInterestPauseResource()).thenReturn(false);

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenThrow(new RetryException()).thenReturn(commandProcessingResult);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idempotencyKey = "test-idempotency-key";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idempotencyKey);

        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);
        when(commandSourceService.findCommandSource(commandWrapper, idempotencyKey)).thenReturn(null);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idempotencyKey)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);

        // When fetching the command source after failure, return the same mock
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        // Execute the command
        CommandProcessingResult result = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSource, atLeast(1)).setResultStatusCode(200);
        verify(commandSource, atLeast(1)).updateForAudit(commandProcessingResult);
        verify(commandSource, atLeast(1)).setResult(any());
        verify(commandSource, atLeast(1)).setStatus(CommandProcessingResultType.PROCESSED);
        verify(commandSourceService, times(2)).processCommandAndSaveResult(any(NewCommandSourceHandler.class), any(JsonCommand.class),
                any(CommandSource.class), any(AppUser.class), Mockito.anyBoolean(),
                Mockito.<BiConsumer<CommandSource, CommandProcessingResult>>any());
        verify(commandSourceService).saveResult(commandSource);
        verify(commandSourceService, never()).generateErrorInfo(any());

        assertEquals(commandProcessingResult, result);
    }

    @Test
    public void testExecuteCommandWithMaxRetryFailureStoresFinalErrorResult() {
        CommandWrapper commandWrapper = getCommandWrapper();
        when(commandWrapper.isInterestPauseResource()).thenReturn(false);

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idempotencyKey = "test-idempotency-key";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idempotencyKey);

        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);
        when(commandSourceService.findCommandSource(commandWrapper, idempotencyKey)).thenReturn(null);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idempotencyKey)).thenReturn(commandSource);

        RetryException persistentException = new RetryException();
        when(commandHandler.processCommand(jsonCommand)).thenThrow(persistentException);

        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        RetryException exception = assertThrows(RetryException.class, () -> {
            underTest.executeCommand(commandWrapper, jsonCommand, false);
        });

        assertEquals(persistentException, exception);

        verify(commandSourceService, times(3)).processCommandAndSaveResult(any(NewCommandSourceHandler.class), any(JsonCommand.class),
                any(CommandSource.class), any(AppUser.class), Mockito.anyBoolean(),
                Mockito.<BiConsumer<CommandSource, CommandProcessingResult>>any());
        verify(commandSourceService, times(3)).getCommandSource(commandId);
        verify(commandSourceService).generateErrorInfo(persistentException);
        verify(commandSource).setResultStatusCode(500);
        verify(commandSource).setResult("Failed");
        verify(commandSource).setStatus(CommandProcessingResultType.ERROR);
        verify(commandSourceService).saveResult(commandSource);
    }

    @Test
    public void testMakerCheckerRollbackStoresAuditResultOutsideRolledBackCommandTransaction() {
        CommandWrapper commandWrapper = getCommandWrapper();
        when(commandWrapper.isInterestPauseResource()).thenReturn(false);

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        String idempotencyKey = "test-idempotency-key";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idempotencyKey);

        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);
        when(commandSourceService.findCommandSource(commandWrapper, idempotencyKey)).thenReturn(null);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(appUser.getId()).thenReturn(1L);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idempotencyKey)).thenReturn(commandSource);
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        RollbackTransactionNotApprovedException rollbackException = new RollbackTransactionNotApprovedException(commandId, 99L);
        ErrorInfo makerCheckerErrorInfo = mock(ErrorInfo.class);
        when(makerCheckerErrorInfo.getMessage()).thenReturn("maker-checker-result");
        when(makerCheckerErrorInfo.getStatusCode()).thenReturn(200);
        when(commandSourceService.processCommandAndSaveResult(any(NewCommandSourceHandler.class), any(JsonCommand.class),
                any(CommandSource.class), any(AppUser.class), Mockito.anyBoolean(),
                Mockito.<BiConsumer<CommandSource, CommandProcessingResult>>any())).thenThrow(rollbackException);
        when(commandSourceService.generateErrorInfo(rollbackException)).thenReturn(makerCheckerErrorInfo);

        RollbackTransactionNotApprovedException actual = assertThrows(RollbackTransactionNotApprovedException.class,
                () -> underTest.executeCommand(commandWrapper, jsonCommand, false));

        assertEquals(rollbackException, actual);
        verify(commandSource).setResultStatusCode(200);
        verify(commandSource).setResult("maker-checker-result");
        verify(commandSourceService).saveResult(commandSource);
        verify(commandSourceService).generateErrorInfo(rollbackException);
    }

    /**
     * Test that when running inside an enclosing batch transaction, hook events are NOT published immediately but
     * deferred to afterCommit. This prevents webhooks (e.g. SMS notifications) from firing for commands that are
     * subsequently rolled back when a later command in the batch fails.
     */
    @Test
    public void testHookEventDeferredInEnclosingTransaction() {
        CommandWrapper commandWrapper = getCommandWrapper();

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);
        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(null);
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.PROCESSED.getValue());

        // Simulate enclosing batch transaction with active synchronization
        BatchRequestContextHolder.setIsEnclosingTransaction(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            underTest.executeCommand(commandWrapper, jsonCommand, false);

            // The hook event should be deferred via TransactionSynchronization registered on the current transaction
            assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
            // Verify hook event was NOT published immediately (deferred to afterCommit)
            verify(eventPublisher, never()).publishEvent(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            BatchRequestContextHolder.resetIsEnclosingTransaction();
        }
    }

    /**
     * Test that when NOT in an enclosing transaction, no TransactionSynchronization is registered. The code takes the
     * immediate path (not the deferred path), preserving existing behaviour for non-batch single-request commands.
     */
    @Test
    public void testHookEventNotDeferredOutsideEnclosingTransaction() {
        CommandWrapper commandWrapper = getCommandWrapper();

        long commandId = 1L;
        JsonCommand jsonCommand = Mockito.mock(JsonCommand.class);
        when(jsonCommand.commandId()).thenReturn(commandId);
        when(jsonCommand.json()).thenReturn(null); // null causes publishHookEvent to exit early; avoids mocking the
                                                   // full hook-serialisation chain

        NewCommandSourceHandler commandHandler = Mockito.mock(NewCommandSourceHandler.class);
        CommandProcessingResult commandProcessingResult = Mockito.mock(CommandProcessingResult.class);
        when(commandProcessingResult.isRollbackTransaction()).thenReturn(false);
        when(commandHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);
        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);
        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(null);
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);
        when(commandSourceService.saveInitial(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResult(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.PROCESSED.getValue());

        // Not in enclosing transaction (default)
        BatchRequestContextHolder.resetIsEnclosingTransaction();

        underTest.executeCommand(commandWrapper, jsonCommand, false);

        // The deferred path was not taken: no TransactionSynchronization should have been registered.
        assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
    }
}
