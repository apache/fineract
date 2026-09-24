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
package org.apache.fineract.portfolio.savings.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.batch.exception.ErrorInfo;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.commands.exception.UnsupportedCommandException;
import org.apache.fineract.commands.provider.CommandHandlerProvider;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.IdempotencyKeyResolver;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformServiceImpl;
import org.apache.fineract.commands.service.SynchronousCommandProcessingService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exceptionmapper.DefaultExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.RollbackTransactionNotApprovedExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.UnsupportedCommandExceptionMapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SavingsAnnualFeeMakerCheckerTest {

    private final ConfigurationDomainService configuration = mock(ConfigurationDomainService.class);
    private final CommandSourceRepository repository = mock(CommandSourceRepository.class);
    private final PlatformSecurityContext security = mock(PlatformSecurityContext.class);
    private final CommandHandlerProvider handlers = mock(CommandHandlerProvider.class);
    private final SavingsAccountWritePlatformService savings = mock(SavingsAccountWritePlatformService.class);
    private final AppUser user = mock(AppUser.class);
    private final IdempotencyKeyResolver idempotency = mock(IdempotencyKeyResolver.class);
    @SuppressWarnings("unchecked")
    private final ToApiJsonSerializer<CommandProcessingResult> resultSerializer = mock(ToApiJsonSerializer.class);
    private final List<CommandProcessingResultType> savedStates = new ArrayList<>();
    private final StaticApplicationContext applicationContext = new StaticApplicationContext();
    private CommandSource savedSource;
    private ErrorHandler errors;
    private PortfolioCommandSourceWritePlatformServiceImpl commands;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "UTC", null));
        newRequest();
        applicationContext.getBeanFactory().registerSingleton("unsupported", new UnsupportedCommandExceptionMapper());
        applicationContext.getBeanFactory().registerSingleton("awaitingApproval", new RollbackTransactionNotApprovedExceptionMapper());
        errors = new ErrorHandler(applicationContext, new DefaultExceptionMapper());
        FromJsonHelper json = new FromJsonHelper();
        CommandSourceService sources = new CommandSourceService(configuration, repository, errors, json);
        RetryConfigurationAssembler retries = mock(RetryConfigurationAssembler.class);
        when(retries.getRetryConfigurationForExecuteCommand()).thenReturn(Retry.of("test", RetryConfig.custom().maxAttempts(1).build()));
        @SuppressWarnings("unchecked")
        ToApiJsonSerializer<Map<String, Object>> hookSerializer = mock(ToApiJsonSerializer.class);
        SynchronousCommandProcessingService processing = new SynchronousCommandProcessingService(security, applicationContext,
                mock(TransactionBoundApplicationEventPublisher.class), hookSerializer, resultSerializer, configuration, handlers,
                idempotency, sources, retries, new FineractRequestContextHolder());
        commands = new PortfolioCommandSourceWritePlatformServiceImpl(security, repository, json, processing,
                mock(SchedulerJobRunnerReadService.class), configuration, List.of());
        when(security.authenticatedUser(any(CommandWrapper.class))).thenReturn(user);
        when(security.authenticatedUser()).thenReturn(user);
        when(user.getId()).thenReturn(2L);
        when(idempotency.resolve(any())).thenReturn("annual-fee-test");
        when(handlers.getHandler("SAVINGSACCOUNT", "APPLYANNUALFEE")).thenReturn(new ApplyAnnualFeeSavingsAccountCommandHandler(savings));
        when(repository.saveAndFlush(any(CommandSource.class))).thenAnswer(invocation -> {
            savedSource = invocation.getArgument(0);
            if (savedSource.getId() == null) {
                savedSource.setId(101L);
            }
            // Snapshot now: the same entity is mutated again before the final save.
            savedStates.add(savedSource.getStatusEnum());
            return savedSource;
        });
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        ThreadLocalContextUtil.reset();
        applicationContext.close();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void annualFeeRejectsBeforeMakerCheckerValidationAndNeverQueues(boolean makerChecker) {
        when(configuration.isMakerCheckerEnabledForTask(anyString())).thenReturn(makerChecker);

        UnsupportedCommandException exception = assertThrows(UnsupportedCommandException.class,
                () -> commands.logCommandSource(annualFee()));

        assertUnsupported(exception);
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING, CommandProcessingResultType.ERROR);
        assertThat(savedSource.isAwaitingApproval()).isFalse();
        assertThat(savedSource.getResultStatusCode()).isEqualTo(400);
        assertThat(savedSource.getResult()).contains("error.msg.command.unsupported");
        assertThat(savedSource.getChecker()).isNull();
        verify(configuration, never()).isMakerCheckerEnabledForTask(anyString());
        verify(user).validateHasPermissionTo("APPLYANNUALFEE_SAVINGSACCOUNT");
        verifyNoInteractions(savings, resultSerializer);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void approvingPreviouslyQueuedAnnualFeeRejectsWithoutSuccessfulResult(boolean makerChecker) {
        when(configuration.isMakerCheckerEnabledForTask(anyString())).thenReturn(makerChecker);
        AppUser originalMaker = mock(AppUser.class);
        when(originalMaker.getId()).thenReturn(1L);
        CommandSource pending = CommandSource.builder().actionName("APPLYANNUALFEE").entityName("SAVINGSACCOUNT")
                .resourceId(42L).savingsId(42L).resourceGetUrl(annualFee().getHref()).commandAsJson("{}").maker(originalMaker).idempotencyKey("previously-queued")
                .status(CommandProcessingResultType.AWAITING_APPROVAL.getValue()).build();
        pending.setId(101L);
        when(repository.findById(101L)).thenReturn(Optional.of(pending));
        when(repository.findByActionNameAndEntityNameAndIdempotencyKey("APPLYANNUALFEE", "SAVINGSACCOUNT", "previously-queued"))
                .thenReturn(pending);

        UnsupportedCommandException exception = assertThrows(UnsupportedCommandException.class, () -> commands.approveEntry(101L));

        assertUnsupported(exception);
        assertThat(savedStates).containsExactly(CommandProcessingResultType.ERROR);
        assertThat(pending.isAwaitingApproval()).isFalse();
        assertThat(pending.isChecked()).isFalse();
        assertThat(pending.getChecker()).isNull();
        assertThat(pending.getResultStatusCode()).isEqualTo(400);
        verify(user).validateHasCheckerPermissionTo("APPLYANNUALFEE_SAVINGSACCOUNT");
        verify(configuration, never()).isMakerCheckerEnabledForTask(anyString());
        verifyNoInteractions(idempotency, savings, resultSerializer);
    }

    @Test
    void supportedActivationWithoutMakerCheckerStillSucceeds() {
        configureActivation(false);
        CommandProcessingResult result = commands.logCommandSource(activation());
        assertThat(result.getSavingsId()).isEqualTo(42L);
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING, CommandProcessingResultType.PROCESSED);
        assertThat(savedSource.getResultStatusCode()).isEqualTo(200);
        verify(savings).activate(eq(42L), any(JsonCommand.class));
    }

    @Test
    void supportedActivationStillQueuesAndCanBeApprovedWithMakerChecker() {
        configureActivation(true);
        RollbackTransactionNotApprovedException awaiting = assertThrows(RollbackTransactionNotApprovedException.class,
                () -> commands.logCommandSource(activation()));
        assertThat(errors.handle(awaiting).getStatusCode()).isEqualTo(200);
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING,
                CommandProcessingResultType.AWAITING_APPROVAL);
        assertThat(savedSource.isAwaitingApproval()).isTrue();
        CommandSource pending = savedSource;
        when(repository.findById(101L)).thenReturn(Optional.of(pending));
        when(repository.findByActionNameAndEntityNameAndIdempotencyKey("ACTIVATE", "SAVINGSACCOUNT", "annual-fee-test"))
                .thenReturn(pending);
        when(configuration.isSameMakerCheckerEnabled()).thenReturn(true);
        newRequest();

        CommandProcessingResult approved = commands.approveEntry(101L);

        assertThat(approved.getSavingsId()).isEqualTo(42L);
        assertThat(pending.isChecked()).isTrue();
        assertThat(pending.getChecker()).isSameAs(user);
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING, CommandProcessingResultType.AWAITING_APPROVAL,
                CommandProcessingResultType.PROCESSED);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void legacyNullHandlerReachedMakerCheckerOnlyAfterExecuting(boolean makerChecker) {
        when(configuration.isMakerCheckerEnabledForTask(anyString())).thenReturn(makerChecker);
        boolean[] executed = { false };
        when(handlers.getHandler("SAVINGSACCOUNT", "APPLYANNUALFEE")).thenReturn(command -> {
            executed[0] = true;
            assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING);
            return null;
        });
        RuntimeException exception = assertThrows(RuntimeException.class, () -> commands.logCommandSource(annualFee()));
        assertThat(executed[0]).isTrue();
        if (makerChecker) {
            assertThat(exception).isInstanceOf(RollbackTransactionNotApprovedException.class);
            assertThat(errors.handle(exception).getStatusCode()).isEqualTo(200);
            assertThat(savedSource.isAwaitingApproval()).isTrue();
        } else {
            assertThat(exception).isInstanceOf(NullPointerException.class);
            assertThat(errors.handle(exception).getStatusCode()).isEqualTo(500);
            assertThat(savedSource.getStatusEnum()).isEqualTo(CommandProcessingResultType.ERROR);
        }
    }

    private void configureActivation(boolean makerChecker) {
        when(configuration.isMakerCheckerEnabledForTask("ACTIVATE_SAVINGSACCOUNT")).thenReturn(makerChecker);
        when(handlers.getHandler("SAVINGSACCOUNT", "ACTIVATE")).thenReturn(new ActivateSavingsAccountCommandHandler(savings));
        when(savings.activate(eq(42L), any(JsonCommand.class)))
                .thenAnswer(invocation -> new CommandProcessingResultBuilder().withEntityId(42L).withSavingsId(42L).build());
        when(resultSerializer.serializeResult(any())).thenReturn("{\"savingsId\":42}");
    }

    private void assertUnsupported(UnsupportedCommandException exception) {
        ErrorInfo error = errors.handle(exception);
        assertThat(error.getStatusCode()).isEqualTo(400);
        assertThat(error.getMessage()).contains("validation.msg.validation.errors.exist", "error.msg.command.unsupported");
    }

    private CommandWrapper annualFee() {
        return new CommandWrapperBuilder().withJson("{}").savingsAccountApplyAnnualFees(42L).build();
    }

    private CommandWrapper activation() {
        return new CommandWrapperBuilder().withJson("{}").savingsAccountActivation(42L).build();
    }

    private void newRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }
}
