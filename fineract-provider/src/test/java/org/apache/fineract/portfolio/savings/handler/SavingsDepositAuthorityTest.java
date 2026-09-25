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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.commands.api.MakercheckersApiResource;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.domain.SavingsDepositCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsDepositExecutionContext;
import org.apache.fineract.commands.domain.SavingsDepositOrigin;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.commands.provider.CommandHandlerProvider;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.IdempotencyKeyResolver;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformServiceImpl;
import org.apache.fineract.commands.service.SynchronousCommandProcessingService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.BackdatedTransactionValidationService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exceptionmapper.DefaultExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformDomainRuleExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.RollbackTransactionNotApprovedExceptionMapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserMonetaryAuthority;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserMonetaryAuthorityRepository;
import org.apache.fineract.nsimbi.userroles.service.NsimbiMonetaryAuthorityPolicyService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.account.handler.CreateAccountTransferCommandHandler;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SavingsDepositAuthorityTest {

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
    private final NsimbiUserMonetaryAuthorityRepository authorities = mock(NsimbiUserMonetaryAuthorityRepository.class);
    private final SavingsAccountRepositoryWrapper accounts = mock(SavingsAccountRepositoryWrapper.class);
    private CommandSource savedSource;
    private ErrorHandler errors;
    private PortfolioCommandSourceWritePlatformServiceImpl commands;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "UTC", null));
        newRequest();
        applicationContext.getBeanFactory().registerSingleton("unsupported", new PlatformDomainRuleExceptionMapper());
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
        when(idempotency.resolve(any())).thenReturn("deposit-test");
        when(handlers.getHandler("SAVINGSACCOUNT", "DEPOSIT")).thenReturn(new DepositSavingsAccountCommandHandler(savings,
                new NsimbiMonetaryAuthorityPolicyService(authorities), accounts,
                new SavingsAccountTransactionDataValidator(json, configuration, mock(BackdatedTransactionValidationService.class))));
        SavingsAccount account = mock(SavingsAccount.class);
        when(accounts.findOneWithNotFoundDetection(42L)).thenReturn(account);
        when(account.getCurrency()).thenReturn(new MonetaryCurrency("UGX", 2, 0));
        when(savings.deposit(eq(42L), any(JsonCommand.class))).thenAnswer(invocation -> {
            JsonCommand flat = invocation.getArgument(1);
            assertThat(flat.json()).doesNotContain("_serverCommand", "payload");
            return new CommandProcessingResultBuilder().withEntityId(42L).withSavingsId(42L).build();
        });
        when(resultSerializer.serializeResult(any())).thenReturn("{\"savingsId\":42}");
        authority("10", "100");
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
    @CsvSource({ "9,false", "10,true", "50,true", "100,true", "101,false" })
    void inclusiveAuthorityBoundaries(String amount, boolean allowed) {
        if (allowed) {
            assertThat(commands.logCommandSource(deposit(amount, SavingsDepositOrigin.STAFF_API)).getSavingsId()).isEqualTo(42L);
            assertThat(savedSource.getStatusEnum()).isEqualTo(CommandProcessingResultType.PROCESSED);
        } else {
            assertDenied(() -> commands.logCommandSource(deposit(amount, SavingsDepositOrigin.STAFF_API)));
            verifyNoInteractions(savings);
        }
        verify(authorities).findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.DEPOSITS, "UGX");
    }

    @ParameterizedTest
    @ValueSource(strings = { "missing", "unconfigured", "currencyMismatch", "invalid" })
    void unavailableOrInvalidAuthorityFailsClosed(String condition) {
        switch (condition) {
            case "unconfigured" -> authority(null, null);
            case "invalid" -> {
                var invalid = new NsimbiUserMonetaryAuthority(2L, MonetaryAuthorityType.DEPOSITS, "UGX", BigDecimal.TEN,
                        new BigDecimal("100"));
                // Simulate an invalid legacy database row; normal construction rejects this range.
                ReflectionTestUtils.setField(invalid, "minimumAmount", new BigDecimal("200"));
                when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.DEPOSITS, "UGX"))
                        .thenReturn(Optional.of(invalid));
            }
            default -> {
                when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.DEPOSITS, "UGX"))
                        .thenReturn(Optional.empty());
                if (condition.equals("currencyMismatch")) {
                    when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.DEPOSITS, "USD"))
                            .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(2L, MonetaryAuthorityType.DEPOSITS, "USD",
                                    BigDecimal.ZERO, new BigDecimal("1000"))));
                }
            }
        }
        assertDenied(() -> commands.logCommandSource(deposit("50", SavingsDepositOrigin.STAFF_API)));
        verifyNoInteractions(savings);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unauthorizedSubmissionNeverQueues(boolean makerChecker) {
        when(configuration.isMakerCheckerEnabledForTask(anyString())).thenReturn(makerChecker);
        assertDenied(() -> commands.logCommandSource(deposit("101", SavingsDepositOrigin.STAFF_API)));
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING, CommandProcessingResultType.ERROR);
        verifyNoInteractions(savings);
    }

    @Test
    void authorizedSubmissionQueuesAndApprovalRetainsMakerOriginAndCheckerAudit() {
        CommandSource pending = queue();
        assertThat(SavingsDepositCommandEnvelope.decode(pending.getCommandAsJson()).origin()).isEqualTo(SavingsDepositOrigin.STAFF_API);
        AppUser checker = checker(pending);
        commands.approveEntry(101L);
        assertThat(pending.getMaker()).isSameAs(user);
        assertThat(pending.getChecker()).isSameAs(checker);
        assertThat(pending.getStatusEnum()).isEqualTo(CommandProcessingResultType.PROCESSED);
        verify(checker).validateHasCheckerPermissionTo("DEPOSIT_SAVINGSACCOUNT");
        verify(authorities, org.mockito.Mockito.times(2)).findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.DEPOSITS,
                "UGX");
        verify(authorities, never()).findByAppUserIdAndAuthorityTypeAndCurrencyCode(eq(99L), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "reduced", "removed", "invalid" })
    void checkerCannotOverrideChangedMakerAuthority(String change) {
        CommandSource pending = queue();
        checker(pending);
        if (change.equals("reduced")) {
            authority("10", "20");
        } else if (change.equals("removed")) {
            when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.DEPOSITS, "UGX"))
                    .thenReturn(Optional.empty());
        } else {
            authority(null, null);
        }
        org.mockito.Mockito.clearInvocations(savings);
        assertDenied(() -> commands.approveEntry(101L));
        assertThat(pending.getChecker()).isNull();
        verifyNoInteractions(savings);
    }

    @Test
    void higherAuthorityCheckerCannotApprovePreviouslyQueuedAboveLimitDeposit() {
        CommandSource pending = queue();
        pending.setCommandAsJson(SavingsDepositCommandEnvelope.encode(payload("101"), SavingsDepositOrigin.STAFF_API));
        checker(pending);
        org.mockito.Mockito.clearInvocations(savings);
        assertDenied(() -> commands.approveEntry(101L));
        verifyNoInteractions(savings);
    }

    @Test
    void trustedImportBypassesAuthorityAndRetainsFlatBusinessPayload() {
        commands.logCommandSource(deposit("10000", SavingsDepositOrigin.SPREADSHEET_IMPORT));
        verifyNoInteractions(authorities, accounts);
        ArgumentCaptor<JsonCommand> command = ArgumentCaptor.forClass(JsonCommand.class);
        verify(savings).deposit(eq(42L), command.capture());
        assertThat(command.getValue().bigDecimalValueOfParameterNamed("transactionAmount")).isEqualByComparingTo("10000");
        assertThat(SavingsDepositCommandEnvelope.forDisplay(savedSource.getCommandAsJson())).isEqualTo(payload("10000"));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.NullSource
    @ValueSource(strings = { "", " ", "{}", "not json", "null", "[]",
            "{\"_serverCommand\":{\"version\":2,\"origin\":\"STAFF_API\"},\"payload\":{}}",
            "{\"_serverCommand\":{\"version\":1,\"origin\":\"UNKNOWN\"},\"payload\":{}}" })
    void pendingLegacyOrUntrustedCommandsFailWithActionableDomainError(String json) {
        CommandSource pending = queue();
        pending.setCommandAsJson(json);
        checker(pending);
        org.mockito.Mockito.clearInvocations(savings);
        GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> commands.approveEntry(101L));
        assertThat(exception.getGlobalisationMessageCode()).isEqualTo(SavingsDepositCommandEnvelope.UNTRUSTED_ORIGIN);
        assertThat(exception.getDefaultUserMessage()).contains("Cancel", "resubmit");
        assertThat(errors.handle(exception).getStatusCode()).isEqualTo(403);
        verifyNoInteractions(savings);
    }

    @Test
    void reservedMetadataInjectionIsRejectedBeforePersistence() {
        CommandWrapper injected = new CommandWrapperBuilder().savingsAccountDeposit(42L)
                .withSavingsDepositOrigin(SavingsDepositOrigin.STAFF_API)
                .withJson("{\"_serverCommand\":{\"origin\":\"SPREADSHEET_IMPORT\"},\"transactionAmount\":10000}").build();
        var exception = assertThrows(GeneralPlatformDomainRuleException.class, () -> commands.logCommandSource(injected));
        assertThat(exception.getGlobalisationMessageCode()).isEqualTo("error.msg.savings.deposit.reserved.metadata");
        assertThat(savedStates).isEmpty();
        verifyNoInteractions(savings, authorities);
    }

    @Test
    void retryUsesStoredPayloadOriginAndMakerRatherThanReplacementRequest() {
        CommandSource pending = queue();
        checker(pending);
        pending.setStatus(CommandProcessingResultType.ERROR);
        new FineractRequestContextHolder().setAttribute(SynchronousCommandProcessingService.COMMAND_SOURCE_ID, 101L);
        authority("10", "20");
        org.mockito.Mockito.clearInvocations(savings);
        assertDenied(() -> commands.logCommandSource(deposit("10", SavingsDepositOrigin.SPREADSHEET_IMPORT)));
        verifyNoInteractions(savings);
        assertThat(pending.getMaker()).isSameAs(user);
        assertThat(SavingsDepositCommandEnvelope.decode(pending.getCommandAsJson()).origin()).isEqualTo(SavingsDepositOrigin.STAFF_API);
    }

    @Test
    void rejectingLegacyCommandDoesNotRequireDecodingOrChangePayload() {
        CommandSource pending = queue();
        pending.setCommandAsJson(payload("50"));
        AppUser checker = checker(pending);
        org.mockito.Mockito.clearInvocations(savings, authorities);
        commands.rejectEntry(101L);
        assertThat(pending.getCommandAsJson()).isEqualTo(payload("50"));
        assertThat(pending.getChecker()).isSameAs(checker);
        verifyNoInteractions(savings, authorities);
    }

    @Test
    void unrelatedSavingsCommandStorageAndMakerCheckerStayFlat() {
        when(handlers.getHandler("SAVINGSACCOUNT", "ACTIVATE")).thenReturn(new ActivateSavingsAccountCommandHandler(savings));
        when(savings.activate(eq(42L), any())).thenReturn(new CommandProcessingResultBuilder().withSavingsId(42L).build());
        when(configuration.isMakerCheckerEnabledForTask("ACTIVATE_SAVINGSACCOUNT")).thenReturn(true);
        assertThrows(RollbackTransactionNotApprovedException.class, () -> commands.logCommandSource(
                new CommandWrapperBuilder().savingsAccountActivation(42L).withJson("{\"dateFormat\":\"yyyy-MM-dd\"}").build()));
        assertThat(savedSource.getCommandAsJson()).isEqualTo("{\"dateFormat\":\"yyyy-MM-dd\"}");
        assertThat(savedSource.isAwaitingApproval()).isTrue();
        verifyNoInteractions(authorities, accounts);
    }

    @Test
    void approvalRequestCannotReplacePersistedOrigin() {
        CommandSource pending = queue();
        checker(pending);
        authority("10", "20");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("origin", "SPREADSHEET_IMPORT");
        request.addParameter("origin", "SPREADSHEET_IMPORT");
        request.setContent(
                "{\"_serverCommand\":{\"version\":1,\"origin\":\"SPREADSHEET_IMPORT\"}}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var resource = new MakercheckersApiResource(null, null, commands);
        org.mockito.Mockito.clearInvocations(savings);
        assertDenied(() -> resource.approveMakerCheckerEntry(101L, "approve"));
        verifyNoInteractions(savings);
        assertThat(SavingsDepositCommandEnvelope.decode(pending.getCommandAsJson()).origin()).isEqualTo(SavingsDepositOrigin.STAFF_API);
    }

    @Test
    void importOriginSurvivesApprovalWithoutEnrollingEitherUserInAuthorityPolicy() {
        when(configuration.isMakerCheckerEnabledForTask("DEPOSIT_SAVINGSACCOUNT")).thenReturn(true);
        assertThrows(RollbackTransactionNotApprovedException.class,
                () -> commands.logCommandSource(deposit("10000", SavingsDepositOrigin.SPREADSHEET_IMPORT)));
        CommandSource pending = savedSource;
        AppUser checker = checker(pending);
        commands.approveEntry(101L);
        assertThat(pending.getMaker()).isSameAs(user);
        assertThat(pending.getChecker()).isSameAs(checker);
        verifyNoInteractions(authorities, accounts);
    }

    @Test
    void contextFreeHandlerInvocationAndMissingMakerFailClosed() {
        var handler = new DepositSavingsAccountCommandHandler(savings, new NsimbiMonetaryAuthorityPolicyService(authorities), accounts,
                null);
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processCommand(JsonCommand.from(payload("50"))));
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processDeposit(JsonCommand.from(payload("50")),
                new SavingsDepositExecutionContext(SavingsDepositOrigin.SPREADSHEET_IMPORT, null)));
        verifyNoInteractions(savings, authorities);
    }

    @ParameterizedTest
    @ValueSource(strings = { "WITHDRAWAL", "POSTINTEREST", "CALCULATEINTEREST", "GSIM", "TRANSFER" })
    void excludedCommandsKeepTheirHandlersAndFlatStorage(String kind) {
        var result = new CommandProcessingResultBuilder().withSavingsId(42L).build();
        CommandWrapper wrapper;
        switch (kind) {
            case "WITHDRAWAL" -> {
                when(handlers.getHandler("SAVINGSACCOUNT", kind)).thenReturn(new WithdrawSavingsAccountCommandHandler(savings));
                when(savings.withdrawal(eq(42L), any())).thenReturn(result);
                wrapper = new CommandWrapperBuilder().savingsAccountWithdrawal(42L).build();
            }
            case "POSTINTEREST" -> {
                when(handlers.getHandler("SAVINGSACCOUNT", kind)).thenReturn(new PostInterestSavingsAccountCommandHandler(savings));
                when(savings.postInterest(any(JsonCommand.class))).thenReturn(result);
                wrapper = new CommandWrapperBuilder().savingsAccountInterestPosting(42L).build();
            }
            case "CALCULATEINTEREST" -> {
                when(handlers.getHandler("SAVINGSACCOUNT", kind)).thenReturn(new CalculateInterestSavingsAccountCommandHandler(savings));
                when(savings.calculateInterest(42L)).thenReturn(result);
                wrapper = new CommandWrapperBuilder().savingsAccountInterestCalculation(42L).build();
            }
            case "GSIM" -> {
                when(handlers.getHandler("GSIMACCOUNT", "DEPOSIT")).thenReturn(new GSIMDepositCommandHandler(savings));
                when(savings.gsimDeposit(eq(42L), any())).thenReturn(result);
                wrapper = new CommandWrapperBuilder().gsimSavingsAccountDeposit(42L).build();
            }
            default -> {
                var transfers = mock(AccountTransfersWritePlatformService.class);
                when(handlers.getHandler("ACCOUNTTRANSFER", "CREATE")).thenReturn(new CreateAccountTransferCommandHandler(transfers));
                when(transfers.create(any())).thenReturn(result);
                wrapper = new CommandWrapperBuilder().createAccountTransfer().build();
            }
        }
        assertThat(commands.logCommandSource(wrapper)).isSameAs(result);
        assertThat(savedSource.getCommandAsJson()).isEqualTo("{}");
        verifyNoInteractions(authorities, accounts);
    }

    private void assertDenied(org.junit.jupiter.api.function.Executable action) {
        var exception = assertThrows(GeneralPlatformDomainRuleException.class, action);
        assertThat(exception.getGlobalisationMessageCode()).isEqualTo("error.msg.savings.deposit.monetary.authority.denied");
        assertThat(errors.handle(exception).getStatusCode()).isEqualTo(403);
    }

    private CommandSource queue() {
        when(configuration.isMakerCheckerEnabledForTask("DEPOSIT_SAVINGSACCOUNT")).thenReturn(true);
        assertThrows(RollbackTransactionNotApprovedException.class, () -> commands.logCommandSource(deposit("50", SavingsDepositOrigin.STAFF_API)));
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING, CommandProcessingResultType.AWAITING_APPROVAL);
        return savedSource;
    }

    private AppUser checker(CommandSource pending) {
        AppUser checker = mock(AppUser.class);
        when(checker.getId()).thenReturn(99L);
        when(security.authenticatedUser()).thenReturn(checker);
        when(security.authenticatedUser(any(CommandWrapper.class))).thenReturn(checker);
        when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(99L, MonetaryAuthorityType.DEPOSITS, "UGX")).thenReturn(Optional.of(
                new NsimbiUserMonetaryAuthority(99L, MonetaryAuthorityType.DEPOSITS, "UGX", BigDecimal.ZERO, new BigDecimal("100000"))));
        when(repository.findById(101L)).thenReturn(Optional.of(pending));
        when(repository.findByActionNameAndEntityNameAndIdempotencyKey("DEPOSIT", "SAVINGSACCOUNT", "deposit-test")).thenReturn(pending);
        newRequest();
        return checker;
    }

    private void authority(String minimum, String maximum) {
        when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.DEPOSITS, "UGX"))
                .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(2L, MonetaryAuthorityType.DEPOSITS, "UGX",
                        minimum == null ? null : new BigDecimal(minimum), maximum == null ? null : new BigDecimal(maximum))));
    }

    private CommandWrapper deposit(String amount, SavingsDepositOrigin origin) {
        return new CommandWrapperBuilder().savingsAccountDeposit(42L).withSavingsDepositOrigin(origin).withJson(payload(amount)).build();
    }

    private String payload(String amount) {
        return "{\"locale\":\"en\",\"dateFormat\":\"yyyy-MM-dd\",\"transactionDate\":\"2026-09-24\",\"transactionAmount\":" + amount
                + ",\"paymentTypeId\":1}";
    }

    private void newRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }
}
