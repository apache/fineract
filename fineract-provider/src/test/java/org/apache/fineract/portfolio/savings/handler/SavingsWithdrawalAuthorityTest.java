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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants.EXTRAS_SHEET_NAME;
import static org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants.SAVINGS_TRANSACTION_SHEET_NAME;
import static org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants.AMOUNT_COL;
import static org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants.PAYMENT_TYPE_COL;
import static org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants.SAVINGS_ACCOUNT_NO_COL;
import static org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants.STATUS_COL;
import static org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants.TRANSACTION_DATE_COL;
import static org.apache.fineract.infrastructure.bulkimport.constants.TransactionConstants.TRANSACTION_TYPE_COL;
import static org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType.SAVINGS_TRANSACTIONS;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.commands.api.MakercheckersApiResource;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.domain.SavingsTransactionCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsTransactionExecutionContext;
import org.apache.fineract.commands.domain.SavingsTransactionKind;
import org.apache.fineract.commands.domain.SavingsTransactionOrigin;
import org.apache.fineract.commands.exception.RollbackTransactionNotApprovedException;
import org.apache.fineract.commands.provider.CommandHandlerProvider;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.IdempotencyKeyResolver;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformServiceImpl;
import org.apache.fineract.commands.service.SynchronousCommandProcessingService;
import org.apache.fineract.infrastructure.bulkimport.data.BulkImportEvent;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocument;
import org.apache.fineract.infrastructure.bulkimport.domain.ImportDocumentRepository;
import org.apache.fineract.infrastructure.bulkimport.importhandler.savings.SavingsTransactionImportHandler;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportEventListener;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.BackdatedTransactionValidationService;
import org.apache.fineract.infrastructure.contentstore.util.ContentPipe;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.SpringConfig;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.IdempotentCommandProcessSucceedException;
import org.apache.fineract.infrastructure.core.exceptionmapper.DefaultExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformDomainRuleExceptionMapper;
import org.apache.fineract.infrastructure.core.exceptionmapper.RollbackTransactionNotApprovedExceptionMapper;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentUpdateCommand;
import org.apache.fineract.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SpringSecurityPlatformSecurityContext;
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
import org.apache.fineract.portfolio.savings.service.SavingsWithdrawalAuthorityService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SavingsWithdrawalAuthorityTest {

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
        when(idempotency.resolve(any())).thenReturn("withdrawal-test");
        when(handlers.getHandler("SAVINGSACCOUNT", "WITHDRAWAL")).thenReturn(new WithdrawSavingsAccountCommandHandler(savings,
                new SavingsWithdrawalAuthorityService(new NsimbiMonetaryAuthorityPolicyService(authorities)), accounts,
                new SavingsAccountTransactionDataValidator(json, configuration, mock(BackdatedTransactionValidationService.class))));
        SavingsAccount account = mock(SavingsAccount.class);
        when(accounts.findOneWithNotFoundDetection(42L)).thenReturn(account);
        when(account.getCurrency()).thenReturn(new MonetaryCurrency("UGX", 2, 0));
        when(savings.withdrawal(eq(42L), any(JsonCommand.class))).thenAnswer(invocation -> {
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
            assertThat(commands.logCommandSource(withdrawal(amount, SavingsTransactionOrigin.STAFF_API)).getSavingsId()).isEqualTo(42L);
            assertThat(savedSource.getStatusEnum()).isEqualTo(CommandProcessingResultType.PROCESSED);
        } else {
            assertDenied(() -> commands.logCommandSource(withdrawal(amount, SavingsTransactionOrigin.STAFF_API)));
            verifyNoInteractions(savings);
        }
        verify(authorities).findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.WITHDRAWALS, "UGX");
    }

    @ParameterizedTest
    @ValueSource(strings = { "missing", "unconfigured", "currencyMismatch", "invalid" })
    void unavailableOrInvalidAuthorityFailsClosed(String condition) {
        switch (condition) {
            case "unconfigured" -> authority(null, null);
            case "invalid" -> {
                var invalid = new NsimbiUserMonetaryAuthority(2L, MonetaryAuthorityType.WITHDRAWALS, "UGX", BigDecimal.TEN,
                        new BigDecimal("100"));
                // Simulate an invalid legacy database row; normal construction rejects this range.
                ReflectionTestUtils.setField(invalid, "minimumAmount", new BigDecimal("200"));
                when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.WITHDRAWALS, "UGX"))
                        .thenReturn(Optional.of(invalid));
            }
            default -> {
                when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.WITHDRAWALS, "UGX"))
                        .thenReturn(Optional.empty());
                if (condition.equals("currencyMismatch")) {
                    when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.WITHDRAWALS, "USD"))
                            .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(2L, MonetaryAuthorityType.WITHDRAWALS, "USD",
                                    BigDecimal.ZERO, new BigDecimal("1000"))));
                }
            }
        }
        assertDenied(() -> commands.logCommandSource(withdrawal("50", SavingsTransactionOrigin.STAFF_API)));
        verifyNoInteractions(savings);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unauthorizedSubmissionNeverQueues(boolean makerChecker) {
        when(configuration.isMakerCheckerEnabledForTask(anyString())).thenReturn(makerChecker);
        assertDenied(() -> commands.logCommandSource(withdrawal("101", SavingsTransactionOrigin.STAFF_API)));
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING, CommandProcessingResultType.ERROR);
        verifyNoInteractions(savings);
    }

    @Test
    void authorizedWithdrawalQueuesAndApprovalRetainsMakerOriginAndCheckerAudit() {
        CommandSource pending = queue();
        assertThat(SavingsTransactionCommandEnvelope.decode(pending.getCommandAsJson(), SavingsTransactionKind.WITHDRAWAL).origin())
                .isEqualTo(SavingsTransactionOrigin.STAFF_API);
        AppUser checker = checker(pending);
        commands.approveEntry(101L);
        assertThat(pending.getMaker()).isSameAs(user);
        assertThat(pending.getChecker()).isSameAs(checker);
        assertThat(pending.getStatusEnum()).isEqualTo(CommandProcessingResultType.PROCESSED);
        verify(checker).validateHasCheckerPermissionTo("WITHDRAWAL_SAVINGSACCOUNT");
        verify(authorities, org.mockito.Mockito.times(2)).findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L,
                MonetaryAuthorityType.WITHDRAWALS, "UGX");
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
            when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.WITHDRAWALS, "UGX"))
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
    void higherAuthorityCheckerCannotApprovePreviouslyQueuedAboveLimitWithdrawal() {
        CommandSource pending = queue();
        pending.setCommandAsJson(SavingsTransactionCommandEnvelope.encode(payload("101"), SavingsTransactionKind.WITHDRAWAL,
                SavingsTransactionOrigin.STAFF_API));
        checker(pending);
        org.mockito.Mockito.clearInvocations(savings);
        assertDenied(() -> commands.approveEntry(101L));
        verifyNoInteractions(savings);
    }

    @Test
    void importRequiresMakerAuthorityAndPreservesFlatPayload() {
        assertDenied(() -> commands.logCommandSource(withdrawal("10000", SavingsTransactionOrigin.SPREADSHEET_IMPORT)));
        verifyNoInteractions(savings);
        newRequest();
        commands.logCommandSource(withdrawal("50", SavingsTransactionOrigin.SPREADSHEET_IMPORT));
        ArgumentCaptor<JsonCommand> command = ArgumentCaptor.forClass(JsonCommand.class);
        verify(savings).withdrawal(eq(42L), command.capture());
        assertThat(command.getValue().bigDecimalValueOfParameterNamed("transactionAmount")).isEqualByComparingTo("50");
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
        assertThat(exception.getGlobalisationMessageCode()).isEqualTo("error.msg.savings.withdrawal.untrusted.origin");
        assertThat(exception.getDefaultUserMessage()).contains("Cancel", "resubmit");
        assertThat(errors.handle(exception).getStatusCode()).isEqualTo(403);
        verifyNoInteractions(savings);
    }

    @Test
    void reservedMetadataInjectionIsRejectedBeforePersistence() {
        CommandWrapper injected = new CommandWrapperBuilder().savingsAccountWithdrawal(42L)
                .withSavingsTransactionOrigin(SavingsTransactionOrigin.STAFF_API)
                .withJson("{\"_serverCommand\":{\"origin\":\"SPREADSHEET_IMPORT\"},\"transactionAmount\":10000}").build();
        var exception = assertThrows(GeneralPlatformDomainRuleException.class, () -> commands.logCommandSource(injected));
        assertThat(exception.getGlobalisationMessageCode()).isEqualTo("error.msg.savings.withdrawal.reserved.metadata");
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
        assertDenied(() -> commands.logCommandSource(withdrawal("10", SavingsTransactionOrigin.SPREADSHEET_IMPORT)));
        verifyNoInteractions(savings);
        assertThat(pending.getMaker()).isSameAs(user);
        assertThat(SavingsTransactionCommandEnvelope.decode(pending.getCommandAsJson(), SavingsTransactionKind.WITHDRAWAL).origin())
                .isEqualTo(SavingsTransactionOrigin.STAFF_API);
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
        request.setContent("{\"_serverCommand\":{\"version\":1,\"origin\":\"SPREADSHEET_IMPORT\"}}".getBytes(UTF_8));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var resource = new MakercheckersApiResource(null, null, commands);
        org.mockito.Mockito.clearInvocations(savings);
        assertDenied(() -> resource.approveMakerCheckerEntry(101L, "approve"));
        verifyNoInteractions(savings);
        assertThat(SavingsTransactionCommandEnvelope.decode(pending.getCommandAsJson(), SavingsTransactionKind.WITHDRAWAL).origin())
                .isEqualTo(SavingsTransactionOrigin.STAFF_API);
    }

    @Test
    void importApprovalRechecksOriginalMaker() {
        when(configuration.isMakerCheckerEnabledForTask("WITHDRAWAL_SAVINGSACCOUNT")).thenReturn(true);
        assertThrows(RollbackTransactionNotApprovedException.class,
                () -> commands.logCommandSource(withdrawal("50", SavingsTransactionOrigin.SPREADSHEET_IMPORT)));
        CommandSource pending = savedSource;
        checker(pending);
        authority("10", "20");
        org.mockito.Mockito.clearInvocations(savings);
        assertDenied(() -> commands.approveEntry(101L));
        verifyNoInteractions(savings);
    }

    @Test
    void contextFreeHandlerInvocationAndMissingMakerFailClosed() {
        var handler = new WithdrawSavingsAccountCommandHandler(savings,
                new SavingsWithdrawalAuthorityService(new NsimbiMonetaryAuthorityPolicyService(authorities)), accounts, null);
        assertThrows(GeneralPlatformDomainRuleException.class, () -> handler.processCommand(JsonCommand.from(payload("50"))));
        assertThrows(GeneralPlatformDomainRuleException.class,
                () -> handler.processTransaction(JsonCommand.from(payload("50")), new SavingsTransactionExecutionContext(
                        SavingsTransactionKind.WITHDRAWAL, SavingsTransactionOrigin.SPREADSHEET_IMPORT, null)));
        verifyNoInteractions(savings, authorities);
    }

    @ParameterizedTest
    @ValueSource(strings = { "POSTINTEREST", "CALCULATEINTEREST", "GSIM", "TRANSFER", "CHARGE", "UNDO" })
    void excludedCommandsKeepTheirHandlersAndFlatStorage(String kind) {
        var result = new CommandProcessingResultBuilder().withSavingsId(42L).build();
        CommandWrapper wrapper;
        switch (kind) {
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
            case "UNDO" -> {
                when(handlers.getHandler("SAVINGSACCOUNT", "UNDOTRANSACTION"))
                        .thenReturn(new UndoTransactionSavingsAccountCommandHandler(savings));
                when(savings.undoTransaction(42L, 7L, false)).thenReturn(result);
                wrapper = new CommandWrapperBuilder().undoSavingsAccountTransaction(42L, 7L).build();
            }
            case "CHARGE" -> {
                when(handlers.getHandler("SAVINGSACCOUNTCHARGE", "PAY")).thenReturn(new PaySavingsAccountChargeCommandHandler(savings));
                when(savings.payCharge(eq(42L), eq(7L), any())).thenReturn(result);
                wrapper = new CommandWrapperBuilder().paySavingsAccountCharge(42L, 7L).build();
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

    @ParameterizedTest
    @ValueSource(strings = {"50", "101"})
    void forceWithdrawalRequiresAuthorityInAdditionToForcePermission(String amount) {
        when(handlers.getHandler("SAVINGSACCOUNT", "FORCE_WITHDRAWAL")).thenReturn(new ForceWithdrawalSavingsAccountCommandHandler(savings,
                new SavingsWithdrawalAuthorityService(new NsimbiMonetaryAuthorityPolicyService(authorities)), accounts,
                new SavingsAccountTransactionDataValidator(new FromJsonHelper(), configuration, mock(BackdatedTransactionValidationService.class))));
        when(savings.forceWithdrawal(eq(42L), any())).thenReturn(new CommandProcessingResultBuilder().withSavingsId(42L).build());
        var wrapper = new CommandWrapperBuilder().savingsAccountForceWithdrawal(42L)
                .withSavingsTransactionOrigin(SavingsTransactionOrigin.STAFF_API).withJson(payload(amount)).build();
        if (amount.equals("50")) { commands.logCommandSource(wrapper); }
        else { assertDenied(() -> commands.logCommandSource(wrapper)); verifyNoInteractions(savings); }
        verify(user).validateHasPermissionTo("FORCE_WITHDRAWAL_SAVINGSACCOUNT");
    }

    @Test
    void asynchronousImportRestoresMakerAndChecksEveryRow() throws Exception {
        ThreadLocalContextUtil.setBusinessDates(new java.util.HashMap<>(
                Map.of(org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 9, 25))));
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.initialize();
        var securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
        SecurityContextHolder.setContext(securityContext);
        var realSecurity = new SpringSecurityPlatformSecurityContext(configuration);
        when(security.authenticatedUser(any(CommandWrapper.class))).thenAnswer(inv -> realSecurity.authenticatedUser(inv.getArgument(0)));
        when(security.authenticatedUser()).thenAnswer(inv -> realSecurity.authenticatedUser());
        var document = mock(ImportDocument.class);
        when(document.getEntityType()).thenReturn(SAVINGS_TRANSACTIONS.getValue());
        when(document.getDocumentId()).thenReturn(9L);
        var documentRepository = mock(ImportDocumentRepository.class);
        var pipe = mock(ContentPipe.class);
        var dispatcher = mock(CommandDispatcher.class);
        when(dispatcher.dispatch(any(DocumentUpdateCommand.class))).thenReturn(() -> null);
        applicationContext.getBeanFactory().registerSingleton("savingsTransactionImportHandler",
                new SavingsTransactionImportHandler(commands));
        var listener = new BulkImportEventListener(applicationContext, documentRepository, pipe, dispatcher);
        var done = new CountDownLatch(1);
        var multicaster = new SpringConfig().applicationEventMulticaster(executor);
        multicaster.addApplicationListener((ApplicationListener<BulkImportEvent>) event -> {
            try {
                listener.onApplicationEvent(event);
            } finally {
                done.countDown();
            }
        });
        try (var workbook = new HSSFWorkbook()) {
            var sheet = workbook.createSheet(SAVINGS_TRANSACTION_SHEET_NAME);
            var paymentType = workbook.createSheet(EXTRAS_SHEET_NAME).createRow(0);
            paymentType.createCell(0).setCellValue(1);
            paymentType.createCell(1).setCellValue("Cash");
            for (int i = 0; i <= STATUS_COL; i++) {
                sheet.createRow(i);
            }
            for (int i = 1; i <= 2; i++) {
                var row = sheet.getRow(i);
                row.createCell(SAVINGS_ACCOUNT_NO_COL).setCellValue(42);
                row.createCell(TRANSACTION_TYPE_COL).setCellValue("Withdrawal");
                row.createCell(PAYMENT_TYPE_COL).setCellValue("Cash");
                row.createCell(AMOUNT_COL).setCellValue(i == 1 ? 50 : 101);
                row.createCell(TRANSACTION_DATE_COL).setCellValue(LocalDate.of(2026, 9, 24));
            }
            multicaster.multicastEvent(new BulkImportEvent(this, workbook, "test.xls", "xls", document, "en", "yyyy-MM-dd",
                    ThreadLocalContextUtil.getContext(), 2L));
            assertThat(done.await(20, SECONDS)).isTrue();
            verify(document).update(any(), eq(1), eq(1));
            verify(documentRepository).saveAndFlush(document);
            verify(savings).withdrawal(eq(42L), any());
            assertThat(savedSource.getMaker()).isSameAs(user);
            assertThat(SavingsTransactionCommandEnvelope.decode(savedSource.getCommandAsJson(), SavingsTransactionKind.WITHDRAWAL).origin())
                    .isEqualTo(SavingsTransactionOrigin.SPREADSHEET_IMPORT);
            assertThat(sheet.getRow(1).getCell(STATUS_COL).getStringCellValue()).isEqualTo("Imported");
            assertThat(sheet.getRow(2).getCell(STATUS_COL).getStringCellValue()).contains("WITHDRAWALS monetary authority");
            verify(authorities, org.mockito.Mockito.times(2)).findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L,
                    MonetaryAuthorityType.WITHDRAWALS, "UGX");
        } finally {
            executor.shutdown();
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void completedReplayDoesNotReevaluateAuthorityOrRequireHistoricalMetadata() {
        commands.logCommandSource(withdrawal("50", SavingsTransactionOrigin.STAFF_API));
        var completed = savedSource;
        completed.setCommandAsJson(payload("50"));
        when(repository.findByActionNameAndEntityNameAndIdempotencyKey("WITHDRAWAL", "SAVINGSACCOUNT", "withdrawal-test"))
                .thenReturn(completed);
        newRequest();
        org.mockito.Mockito.clearInvocations(savings, authorities);
        assertThrows(IdempotentCommandProcessSucceedException.class,
                () -> commands.logCommandSource(withdrawal("50", SavingsTransactionOrigin.STAFF_API)));
        verifyNoInteractions(savings, authorities);
    }

    private void assertDenied(org.junit.jupiter.api.function.Executable action) {
        var exception = assertThrows(GeneralPlatformDomainRuleException.class, action);
        assertThat(exception.getGlobalisationMessageCode()).isEqualTo("error.msg.savings.withdrawal.monetary.authority.denied");
        assertThat(errors.handle(exception).getStatusCode()).isEqualTo(403);
    }

    private CommandSource queue() {
        when(configuration.isMakerCheckerEnabledForTask("WITHDRAWAL_SAVINGSACCOUNT")).thenReturn(true);
        assertThrows(RollbackTransactionNotApprovedException.class, () -> commands.logCommandSource(withdrawal("50", SavingsTransactionOrigin.STAFF_API)));
        assertThat(savedStates).containsExactly(CommandProcessingResultType.UNDER_PROCESSING, CommandProcessingResultType.AWAITING_APPROVAL);
        return savedSource;
    }

    private AppUser checker(CommandSource pending) {
        AppUser checker = mock(AppUser.class);
        when(checker.getId()).thenReturn(99L);
        when(security.authenticatedUser()).thenReturn(checker);
        when(security.authenticatedUser(any(CommandWrapper.class))).thenReturn(checker);
        when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(99L, MonetaryAuthorityType.WITHDRAWALS, "UGX"))
                .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(99L, MonetaryAuthorityType.WITHDRAWALS, "UGX", BigDecimal.ZERO,
                        new BigDecimal("100000"))));
        when(repository.findById(101L)).thenReturn(Optional.of(pending));
        when(repository.findByActionNameAndEntityNameAndIdempotencyKey("WITHDRAWAL", "SAVINGSACCOUNT", "withdrawal-test"))
                .thenReturn(pending);
        newRequest();
        return checker;
    }

    private void authority(String minimum, String maximum) {
        when(authorities.findByAppUserIdAndAuthorityTypeAndCurrencyCode(2L, MonetaryAuthorityType.WITHDRAWALS, "UGX"))
                .thenReturn(Optional.of(new NsimbiUserMonetaryAuthority(2L, MonetaryAuthorityType.WITHDRAWALS, "UGX",
                        minimum == null ? null : new BigDecimal(minimum), maximum == null ? null : new BigDecimal(maximum))));
    }

    private CommandWrapper withdrawal(String amount, SavingsTransactionOrigin origin) {
        return new CommandWrapperBuilder().savingsAccountWithdrawal(42L).withSavingsTransactionOrigin(origin).withJson(payload(amount))
                .build();
    }

    private String payload(String amount) {
        return "{\"locale\":\"en\",\"dateFormat\":\"yyyy-MM-dd\",\"transactionDate\":\"2026-09-24\",\"transactionAmount\":" + amount
                + ",\"paymentTypeId\":1}";
    }

    private void newRequest() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }
}
