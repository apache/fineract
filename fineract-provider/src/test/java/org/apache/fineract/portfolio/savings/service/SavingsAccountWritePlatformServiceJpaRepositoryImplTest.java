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
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.holiday.domain.HolidayRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.StandingInstructionRepository;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import org.apache.fineract.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SavingsAccountWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private SavingsAccountDataValidator fromApiJsonDeserializer;
    @Mock
    private SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper;
    @Mock
    private StaffRepositoryWrapper staffRepository;
    @Mock
    private SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    @Mock
    private SavingsAccountAssembler savingAccountAssembler;
    @Mock
    private SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator;
    @Mock
    private SavingsAccountChargeDataValidator savingsAccountChargeDataValidator;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;
    @Mock
    private SavingsAccountDomainService savingsAccountDomainService;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @Mock
    private AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    @Mock
    private ChargeRepositoryWrapper chargeRepository;
    @Mock
    private SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository;
    @Mock
    private HolidayRepositoryWrapper holidayRepository;
    @Mock
    private WorkingDaysRepositoryWrapper workingDaysRepository;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository;
    @Mock
    private EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    @Mock
    private AppUserRepositoryWrapper appuserRepository;
    @Mock
    private StandingInstructionRepository standingInstructionRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private GSIMRepositoy gsimRepository;
    @Mock
    private SavingsAccountInterestPostingService savingsAccountInterestPostingService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private ErrorHandler errorHandler;

    @InjectMocks
    private SavingsAccountWritePlatformServiceJpaRepositoryImpl service;

    private Method validateTransactionsForTransfer;

    @BeforeEach
    void setUp() throws Exception {
        validateTransactionsForTransfer = SavingsAccountWritePlatformServiceJpaRepositoryImpl.class
                .getDeclaredMethod("validateTransactionsForTransfer", SavingsAccount.class, LocalDate.class);
        validateTransactionsForTransfer.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        MoneyHelper.clearCache();
    }

    @Test
    void validateTransactionsForTransfer_nullTransferDate_doesNotThrowNullPointerException() {
        LocalDate transactionDate = LocalDate.of(2024, 1, 10);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(transactionDate);
        when(transaction.getSubmittedOnDate()).thenReturn(transactionDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, null))
                .isInstanceOf(InvocationTargetException.class).hasCauseInstanceOf(GeneralPlatformDomainRuleException.class)
                .satisfies(e -> assertThat(e.getCause()).isNotInstanceOf(NullPointerException.class));
    }

    @Test
    void validateTransactionsForTransfer_transactionWithNullDate_doesNotThrow() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(null);
        when(transaction.getSubmittedOnDate()).thenReturn(null);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatNoException().isThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate));
    }

    @Test
    void validateTransactionsForTransfer_transactionDateEqualsTransferDate_throwsGeneralPlatformDomainRuleException() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(transferDate);
        when(transaction.getSubmittedOnDate()).thenReturn(transferDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate))
                .isInstanceOf(InvocationTargetException.class).hasCauseInstanceOf(GeneralPlatformDomainRuleException.class);
    }

    @Test
    void validateTransactionsForTransfer_transactionDateAfterTransferDate_throwsGeneralPlatformDomainRuleException() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);
        LocalDate futureDate = transferDate.plusDays(1);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(futureDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate))
                .isInstanceOf(InvocationTargetException.class).hasCauseInstanceOf(GeneralPlatformDomainRuleException.class);
    }

    @Test
    void validateTransactionsForTransfer_transactionDateBeforeTransferDate_doesNotThrow() {
        LocalDate transferDate = LocalDate.of(2024, 1, 15);
        LocalDate pastDate = transferDate.minusDays(1);

        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getTransactionDate()).thenReturn(pastDate);
        when(transaction.getSubmittedOnDate()).thenReturn(pastDate);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getTransactions()).thenReturn(List.of(transaction));

        assertThatNoException().isThrownBy(() -> validateTransactionsForTransfer.invoke(service, savingsAccount, transferDate));
    }

    @Test
    void holdAmount_shouldUpdateTransactionExternalId() {
        Long savingsId = 1L;
        LocalDate transactionDate = LocalDate.of(2024, 4, 2);
        BigDecimal amount = BigDecimal.TEN;
        ExternalId externalId = new ExternalId("hold-external-id");

        setupTenantContext(transactionDate);

        AppUser submittedBy = mock(AppUser.class);
        JsonCommand command = mock(JsonCommand.class);
        SavingsAccount account = mock(SavingsAccount.class);
        SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);

        when(context.authenticatedUser()).thenReturn(submittedBy);
        when(savingAccountAssembler.getPivotConfigStatus()).thenReturn(false);
        when(savingAccountAssembler.assembleFrom(savingsId, false)).thenReturn(account);
        when(command.localDateValueOfParameterNamed("transactionDate")).thenReturn(transactionDate);
        when(command.bigDecimalValueOfParameterNamed("transactionAmount")).thenReturn(amount);
        when(command.booleanPrimitiveValueOfParameterNamed("lienAllowed")).thenReturn(false);
        when(command.stringValueOfParameterNamed("reasonForBlock")).thenReturn("manual hold");
        when(command.json()).thenReturn("{\"externalId\":\"hold-external-id\"}");
        when(externalIdFactory.createFromCommand(command, "externalId")).thenReturn(externalId);
        when(account.getClient()).thenReturn(null);
        when(account.group()).thenReturn(null);
        when(account.getCurrency()).thenReturn(currency);
        when(account.getAccountBalance()).thenReturn(BigDecimal.valueOf(100));
        when(account.getSavingsHoldAmount()).thenReturn(null);
        when(account.officeId()).thenReturn(1L);
        when(account.clientId()).thenReturn(2L);
        when(account.groupId()).thenReturn(3L);
        when(savingsAccountDomainService.handleHold(account, amount, transactionDate, false)).thenReturn(transaction);
        when(transaction.getTransactionDate()).thenReturn(transactionDate);
        when(transaction.getId()).thenReturn(44L);

        service.holdAmount(savingsId, command);

        verify(transaction).updateExternalId(externalId);
        verify(savingsAccountTransactionRepository).saveAndFlush(transaction);
    }

    @Test
    void releaseAmount_shouldUpdateTransactionExternalId() {
        Long savingsId = 1L;
        Long holdTransactionId = 7L;
        LocalDate transactionDate = LocalDate.of(2024, 4, 3);
        ExternalId externalId = new ExternalId("release-external-id");

        setupTenantContext(transactionDate);

        JsonCommand command = mock(JsonCommand.class);
        SavingsAccount account = mock(SavingsAccount.class);
        SavingsAccountTransaction holdTransaction = mock(SavingsAccountTransaction.class);
        SavingsAccountTransaction releaseTransaction = mock(SavingsAccountTransaction.class);
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);

        when(context.authenticatedUser()).thenReturn(mock(AppUser.class));
        when(savingsAccountTransactionRepository.findOneByIdAndSavingsAccountId(holdTransactionId, savingsId)).thenReturn(holdTransaction);
        when(savingsAccountTransactionDataValidator.validateReleaseAmountAndAssembleForm(holdTransaction)).thenReturn(releaseTransaction);
        when(externalIdFactory.createFromCommand(command, "externalId")).thenReturn(externalId);
        when(savingAccountAssembler.getPivotConfigStatus()).thenReturn(false);
        when(savingAccountAssembler.assembleFrom(savingsId, false)).thenReturn(account);
        when(account.getClient()).thenReturn(null);
        when(account.group()).thenReturn(null);
        when(account.getCurrency()).thenReturn(currency);
        when(account.getAccountBalance()).thenReturn(BigDecimal.valueOf(100));
        when(account.getSavingsHoldAmount()).thenReturn(BigDecimal.TEN);
        when(account.officeId()).thenReturn(1L);
        when(account.clientId()).thenReturn(2L);
        when(account.groupId()).thenReturn(3L);
        when(account.getId()).thenReturn(savingsId);
        when(releaseTransaction.getAmount()).thenReturn(BigDecimal.TEN);
        when(releaseTransaction.getTransactionDate()).thenReturn(transactionDate);
        when(releaseTransaction.getId()).thenReturn(88L);

        service.releaseAmount(savingsId, holdTransactionId, command);

        verify(savingsAccountTransactionDataValidator).validateReleaseAmount(command);
        verify(releaseTransaction).updateExternalId(externalId);
        verify(savingsAccountTransactionRepository).saveAndFlush(releaseTransaction);
        verify(holdTransaction).updateReleaseId(88L);
    }

    @Test
    void releaseAmount_shouldThrowNotFoundWhenTransactionDoesNotBelongToSavingsAccount() {
        Long savingsId = 1L;
        Long holdTransactionId = 7L;
        JsonCommand command = mock(JsonCommand.class);

        when(context.authenticatedUser()).thenReturn(mock(AppUser.class));
        when(command.json()).thenReturn("{\"externalId\":\"release-external-id\"}");
        when(savingsAccountTransactionRepository.findOneByIdAndSavingsAccountId(holdTransactionId, savingsId)).thenReturn(null);

        assertThatThrownBy(() -> service.releaseAmount(savingsId, holdTransactionId, command))
                .isInstanceOf(SavingsAccountTransactionNotFoundException.class);
    }

    @Test
    void postInterest_shouldValidateRequestAndUpdateManualInterestPostingExternalId() {
        Long savingsId = 1L;
        LocalDate transactionDate = LocalDate.of(2024, 4, 5);
        ExternalId externalId = new ExternalId("interest-posting-external-id");

        setupTenantContext(transactionDate);

        JsonCommand command = mock(JsonCommand.class);
        SavingsAccount account = mock(SavingsAccount.class);
        SavingsAccountTransaction postingTransaction = mock(SavingsAccountTransaction.class);
        MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);
        List<SavingsAccountTransaction> transactions = new ArrayList<>();

        when(command.getSavingsId()).thenReturn(savingsId);
        when(command.booleanPrimitiveValueOfParameterNamed("isPostInterestAsOn")).thenReturn(true);
        when(command.localDateValueOfParameterNamed("transactionDate")).thenReturn(transactionDate);
        when(externalIdFactory.createFromCommand(command, "externalId")).thenReturn(externalId);
        when(savingAccountAssembler.getPivotConfigStatus()).thenReturn(false);
        when(savingAccountAssembler.assembleFrom(savingsId, false)).thenReturn(account);
        when(account.getClient()).thenReturn(null);
        when(account.group()).thenReturn(null);
        when(account.getTransactions()).thenReturn(transactions);
        when(account.accountSubmittedOrActivationDate()).thenReturn(transactionDate.minusDays(1));
        when(account.getNominalAnnualInterestRate()).thenReturn(BigDecimal.ONE);
        when(account.allowOverdraft()).thenReturn(false);
        when(account.findExistingTransactionIds()).thenReturn(new HashSet<>());
        when(account.findExistingReversedTransactionIds()).thenReturn(new HashSet<>());
        when(account.getCurrency()).thenReturn(currency);
        when(account.deriveAccountingBridgeData(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(Collections.emptyMap());
        when(account.officeId()).thenReturn(1L);
        when(account.clientId()).thenReturn(2L);
        when(account.groupId()).thenReturn(3L);
        when(configurationDomainService.isSavingsInterestPostingAtCurrentPeriodEnd()).thenReturn(false);
        when(configurationDomainService.retrieveFinancialYearBeginningMonth()).thenReturn(1);
        when(postingTransaction.getId()).thenReturn(null);
        when(postingTransaction.getDateOf()).thenReturn(transactionDate);
        when(postingTransaction.getTransactionDate()).thenReturn(transactionDate);
        when(postingTransaction.isInterestPosting()).thenReturn(true);
        when(postingTransaction.isManualTransaction()).thenReturn(true);

        Mockito.doAnswer(invocation -> {
            transactions.add(postingTransaction);
            return null;
        }).when(account).postInterest(any(), any(), anyBoolean(), anyBoolean(), any(), any(), anyBoolean(), anyBoolean());

        service.postInterest(command);

        verify(savingsAccountTransactionDataValidator).validatePostInterest(command);
        verify(postingTransaction).updateExternalId(externalId);
        verify(savingsAccountTransactionRepository).save(postingTransaction);
    }

    private void setupTenantContext(final LocalDate businessDate) {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "UTC", null));
        MoneyHelper.initializeTenantRoundingMode("test", 6);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));
    }

    @Test
    void payCharge_shouldReturnTransactionIdInResult() {
        // Given
        final Long savingsAccountId = 1L;
        final Long chargeId = 2L;
        final Long expectedTransactionId = 42L;
        final LocalDate transactionDate = LocalDate.of(2024, 3, 15);

        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, transactionDate)));

        JsonCommand command = mock(JsonCommand.class);
        when(command.json()).thenReturn("{}");
        when(command.extractLocale()).thenReturn(java.util.Locale.ENGLISH);
        when(command.dateFormat()).thenReturn("dd MMMM yyyy");
        when(command.bigDecimalValueOfParameterNamed("amount")).thenReturn(BigDecimal.TEN);
        when(command.localDateValueOfParameterNamed("dueAsOfDate")).thenReturn(transactionDate);
        when(command.stringValueOfParameterNamed("note")).thenReturn(null);

        SavingsAccount savingsAccount = mock(SavingsAccount.class);
        when(savingsAccount.getId()).thenReturn(savingsAccountId);
        when(savingsAccount.officeId()).thenReturn(1L);
        when(savingsAccount.clientId()).thenReturn(1L);
        when(savingsAccount.groupId()).thenReturn(1L);
        when(savingsAccount.findExistingTransactionIds()).thenReturn(new HashSet<>());
        when(savingsAccount.findExistingReversedTransactionIds()).thenReturn(new HashSet<>());
        when(savingsAccount.getOnHoldFunds()).thenReturn(BigDecimal.ZERO);
        when(savingsAccount.isBeforeLastPostingPeriod(any(LocalDate.class), anyBoolean())).thenReturn(false);
        MonetaryCurrency currency = mock(MonetaryCurrency.class);
        when(currency.getCode()).thenReturn("USD");
        when(savingsAccount.getCurrency()).thenReturn(currency);
        when(savingsAccount.deriveAccountingBridgeData(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(new HashMap<>());

        SavingsAccountCharge savingsAccountCharge = mock(SavingsAccountCharge.class);
        when(savingsAccountCharge.getId()).thenReturn(chargeId);
        when(savingsAccountCharge.savingsAccount()).thenReturn(savingsAccount);

        SavingsAccountTransaction chargeTransaction = mock(SavingsAccountTransaction.class);
        when(chargeTransaction.getId()).thenReturn(expectedTransactionId);

        when(savingsAccountChargeRepository.findOneWithNotFoundDetection(chargeId, savingsAccountId)).thenReturn(savingsAccountCharge);
        when(configurationDomainService.allowTransactionsOnHolidayEnabled()).thenReturn(true);
        when(configurationDomainService.allowTransactionsOnNonWorkingDayEnabled()).thenReturn(true);
        when(configurationDomainService.isSavingsInterestPostingAtCurrentPeriodEnd()).thenReturn(false);
        when(configurationDomainService.retrieveFinancialYearBeginningMonth()).thenReturn(1);
        when(savingsAccountTransactionRepository.findBySavingsAccountIdAndLessThanDateOfAndReversedIsFalse(anyLong(), any(LocalDate.class),
                any())).thenReturn(Collections.emptyList());
        when(savingsAccount.payCharge(any(), any(), any(), any(), anyBoolean(), any())).thenReturn(chargeTransaction);

        // When
        CommandProcessingResult result = service.payCharge(savingsAccountId, chargeId, command);

        // Then
        assertThat(result.getTransactionId()).isEqualTo(expectedTransactionId.toString());
    }
}
