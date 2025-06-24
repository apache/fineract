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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.CommandResultPersistenceException;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.commands.provider.CommandHandlerProvider;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessUnderProcessingException;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class SynchronousCommandProcessingServiceTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ApplicationContext applicationContext;
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

        var persistenceRetry = impl.getRetryConfigurationForCommandResultPersistence();
        when(retryConfigurationAssembler.getRetryConfigurationForCommandResultPersistence()).thenReturn(persistenceRetry);
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
        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResultSameTransaction(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.PROCESSED.getValue());
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false))
                .thenThrow(new RetryException()).thenThrow(new RetryException()).thenReturn(commandProcessingResult);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        assertEquals(CommandProcessingResultType.PROCESSED.getValue(), commandSource.getStatus());
        assertEquals(commandProcessingResult, actualCommandProcessingResult);
        // verify 2x throw before success
        verify(commandSourceService, times(2)).generateErrorInfo(any());
        verify(commandSourceService).saveResultSameTransaction(commandSource);
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
        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResultSameTransaction(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue());
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false))
                .thenThrow(new RetryException()).thenThrow(new RetryException()).thenReturn(commandProcessingResult);

        when(retryConfigurationAssembler.getLastException()).thenReturn(null)
                .thenAnswer((i) -> IdempotentCommandProcessUnderProcessingException.class)
                .thenAnswer((i) -> IdempotentCommandProcessUnderProcessingException.class);

        assertThrows(IdempotentCommandProcessUnderProcessingException.class,
                () -> underTest.executeCommand(commandWrapper, jsonCommand, false));

        verify(commandSource, times(3)).getStatus();
        assertEquals(CommandProcessingResultType.UNDER_PROCESSING.getValue(), commandSource.getStatus());
        verify(commandSourceService, times(0)).generateErrorInfo(any());
        verify(commandSourceService, times(0)).saveResultSameTransaction(commandSource);
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
        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResultSameTransaction(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue()) //
                .thenReturn(CommandProcessingResultType.UNDER_PROCESSING.getValue()) //
                // Is it possible???
                .thenReturn(CommandProcessingResultType.AWAITING_APPROVAL.getValue()) //
        ;
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false))
                .thenReturn(commandProcessingResult);

        when(retryConfigurationAssembler.getLastException()).thenReturn(null)
                .thenAnswer((i) -> IdempotentCommandProcessUnderProcessingException.class);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSource, times(3)).getStatus();
        verify(commandSourceService, times(0)).generateErrorInfo(any());
        verify(commandSourceService).saveResultSameTransaction(commandSource);
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
        when(commandHandler.processCommand(jsonCommand)).thenReturn(commandProcessingResult);

        when(commandHandlerProvider.getHandler(Mockito.any(), Mockito.any())).thenReturn(commandHandler);

        when(configurationDomainService.isMakerCheckerEnabledForTask(Mockito.any())).thenReturn(false);
        String idk = "idk";
        when(idempotencyKeyResolver.resolve(commandWrapper)).thenReturn(idk);
        CommandSource commandSource = Mockito.mock(CommandSource.class);
        when(commandSource.getId()).thenReturn(commandId);

        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        AppUser appUser = Mockito.mock(AppUser.class);
        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResultSameTransaction(commandSource)).thenReturn(commandSource);

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

        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false))
                // first time
                .thenThrow(new RetryException())
                // look like stuck and fails
                .thenThrow(new RetryException())
                // look like stuck and pass
                .thenReturn(commandProcessingResult);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSource, times(2)).getStatus();
        assertEquals(CommandProcessingResultType.UNDER_PROCESSING.getValue(), commandSource.getStatus());
        verify(commandSourceService, times(2)).generateErrorInfo(any());
        verify(commandSourceService).saveResultSameTransaction(commandSource);
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
        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);
        when(commandSourceService.saveResultSameTransaction(commandSource)).thenReturn(commandSource);
        when(commandSource.getStatus()).thenReturn(CommandProcessingResultType.PROCESSED.getValue());
        when(context.authenticatedUser(Mockito.any(CommandWrapper.class))).thenReturn(appUser);

        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false))
                .thenReturn(commandProcessingResult);

        CommandProcessingResult actualCommandProcessingResult = underTest.executeCommand(commandWrapper, jsonCommand, false);

        verify(commandSourceService).getCommandSource(commandId);
        assertEquals(CommandProcessingResultType.PROCESSED.getValue(), commandSource.getStatus());
        verify(commandSourceService).saveResultSameTransaction(commandSource);

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
        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idk)).thenReturn(commandSource);

        CommandSource initialCommandSource = Mockito.mock(CommandSource.class);

        when(commandSourceService.findCommandSource(commandWrapper, idk)).thenReturn(initialCommandSource);

        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false)).thenThrow(runtimeException);

        assertThrows(RuntimeException.class, () -> {
            underTest.executeCommand(commandWrapper, jsonCommand, false);
        });

        verify(commandSourceService).getCommandSource(commandId);
        verify(commandSourceService).generateErrorInfo(runtimeException);
    }

    @NotNull
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

        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idempotencyKey))
                .thenReturn(commandSource);
        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false))
                .thenReturn(commandProcessingResult);

        final AtomicInteger saveAttempts = new AtomicInteger(0);

        doAnswer(invocation -> {
            int attempt = saveAttempts.incrementAndGet();
            if (attempt == 1) {
                throw new RuntimeException("Database error on first attempt");
            }
            return commandSource;
        }).when(commandSourceService).saveResultSameTransaction(any(CommandSource.class));

        // When fetching the command source after failure, return the same mock
        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        // Execute the command
        CommandProcessingResult result = underTest.executeCommand(commandWrapper, jsonCommand, false);

        assertEquals(2, saveAttempts.get(), "Expected 2 save attempts");

        verify(commandSource, atLeast(1)).setResultStatusCode(200);
        verify(commandSource, atLeast(1)).updateForAudit(commandProcessingResult);
        verify(commandSource, atLeast(1)).setResult(any());
        verify(commandSource, atLeast(1)).setStatus(CommandProcessingResultType.PROCESSED);

        assertEquals(commandProcessingResult, result);
    }

    @Test
    public void testExecuteCommandWithMaxRetryFailure() {
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

        when(commandSourceService.saveInitialNewTransaction(commandWrapper, jsonCommand, appUser, idempotencyKey))
                .thenReturn(commandSource);
        when(commandSourceService.processCommand(commandHandler, jsonCommand, commandSource, appUser, false))
                .thenReturn(commandProcessingResult);

        final AtomicInteger saveAttempts = new AtomicInteger(0);

        // Simulate persistent save failure - all retry attempts fail
        RuntimeException persistentException = new RuntimeException("Database error persists");
        doAnswer(invocation -> {
            // Count the number of attempts
            saveAttempts.incrementAndGet();
            // Always throw the exception to trigger retry
            throw persistentException;
        }).when(commandSourceService).saveResultSameTransaction(any(CommandSource.class));

        when(commandSourceService.getCommandSource(commandId)).thenReturn(commandSource);

        CommandResultPersistenceException exception = assertThrows(CommandResultPersistenceException.class, () -> {
            underTest.executeCommand(commandWrapper, jsonCommand, false);
        });

        assertEquals(persistentException, exception.getCause());

        assertTrue(saveAttempts.get() >= 3, "Expected at least 3 save attempts, but got: " + saveAttempts.get());
    }
}
