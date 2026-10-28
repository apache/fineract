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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import org.apache.fineract.accounting.journalentry.data.AccountingBridgeDataDTO;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.SingleLoanChargeRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanChargeWritePlatformServiceImplTest {

    private static final Long LOAN_ID = 1L;
    private static final Integer SPECIFIED_DUE_DATE = 2;
    private static final LocalDate MATURITY_DATE = LocalDate.of(2024, 2, 15);
    private static final LocalDate BUSINESS_DATE_AFTER = LocalDate.of(2024, 2, 26);
    private static final LocalDate BUSINESS_DATE_ON = MATURITY_DATE;
    private static final LocalDate BUSINESS_DATE_BEFORE = LocalDate.of(2024, 2, 14);
    private static final String CURRENCY_CODE = "USD";

    @InjectMocks
    private LoanChargeWritePlatformServiceImpl loanChargeWritePlatformService;

    @Mock
    private JsonCommand jsonCommand;

    @Mock
    private LoanChargeApiJsonValidator loanChargeApiJsonValidator;

    @Mock
    private LoanAssembler loanAssembler;

    @Mock
    private Loan loan;

    @Mock
    private ChargeRepositoryWrapper chargeRepository;

    @Mock
    private Charge chargeDefinition;

    @Mock
    private LoanChargeAssembler loanChargeAssembler;

    @Mock
    private LoanCharge loanCharge;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private LoanProductRelatedDetail loanRepaymentScheduleDetail;

    @Mock
    private LoanChargeRepository loanChargeRepository;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private LoanTransactionRepository loanTransactionRepository;

    @Mock
    private LoanTransaction loanTransaction;

    @Mock
    private LoanAccountDomainService loanAccountDomainService;

    @Mock
    private MonetaryCurrency monetaryCurrency;

    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Mock
    private LoanChargeValidator loanChargeValidator;

    @Mock
    private LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;

    @Mock
    private ReprocessLoanTransactionsService reprocessLoanTransactionsService;

    @Mock
    private LoanAccountService loanAccountService;

    @Mock
    private LoanChargeService loanChargeService;

    @Mock
    private ChargeCalculationType chargeCalculationType;

    @Mock
    private SingleLoanChargeRepaymentScheduleProcessingWrapper wrapper;

    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;

    @Mock
    private LoanJournalEntryPoster journalEntryPoster;

    @Mock
    private LoanScheduleService loanScheduleService;

    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;

    @Mock
    private FromJsonHelper fromApiJsonHelper;

    @Mock
    private LoanChargeReadPlatformService loanChargeReadPlatformService;

    @Mock
    private LoanUtilService loanUtilService;

    @Mock
    private ExternalIdFactory externalIdFactory;

    @BeforeEach
    void setUp() {
        when(loanAssembler.assembleFromInUserScope(LOAN_ID)).thenReturn(loan);
        when(chargeRepository.findOneWithNotFoundDetection(anyLong())).thenReturn(chargeDefinition);
        when(chargeDefinition.getChargeTimeType()).thenReturn(SPECIFIED_DUE_DATE);
        when(chargeDefinition.getCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(loanChargeAssembler.createNewFromJson(loan, chargeDefinition, jsonCommand)).thenReturn(loanCharge);
        when(loan.getLoanProductRelatedDetail()).thenReturn(loanRepaymentScheduleDetail);
        when(loanRepaymentScheduleDetail.getLoanScheduleType()).thenReturn(LoanScheduleType.CUMULATIVE);
        when(loan.getLoanRepaymentScheduleDetail()).thenReturn(loanRepaymentScheduleDetail);
        when(loan.hasCurrencyCodeOf(CURRENCY_CODE)).thenReturn(true);
        when(loanCharge.getChargePaymentMode()).thenReturn(ChargePaymentMode.REGULAR);
        when(loanCharge.getChargeCalculation()).thenReturn(chargeCalculationType);
        when(chargeCalculationType.isPercentageBased()).thenReturn(false);
        when(loanCharge.amountOrPercentage()).thenReturn(BigDecimal.TEN);
        when(loan.getStatus()).thenReturn(LoanStatus.ACTIVE);
        when(loanChargeRepository.saveAndFlush(any(LoanCharge.class))).thenReturn(loanCharge);
        when(loan.getCurrency()).thenReturn(monetaryCurrency);
        when(monetaryCurrency.getCode()).thenReturn(CURRENCY_CODE);
        when(loanAccountService.saveAndFlushLoanWithDataIntegrityViolationChecks(any())).thenReturn(loan);

        when(loan.getLoanCharges()).thenReturn(new HashSet<>());
        when(loan.getDisbursementDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()));
        when(loan.getRepaymentScheduleInstallments()).thenReturn(new ArrayList<>());
        when(loanChargeService.calculateAmountPercentageAppliedTo(any(Loan.class), any(LoanCharge.class))).thenReturn(BigDecimal.TEN);
        when(loan.fetchNumberOfInstallmentsAfterExceptions()).thenReturn(5);
        when(loan.updateSummaryWithTotalFeeChargesDueAtDisbursement(any(BigDecimal.class))).thenReturn(null);
        when(loan.deriveSumTotalOfChargesDueAtDisbursement()).thenReturn(BigDecimal.ZERO);
        when(loanCharge.getDueLocalDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()));
        when(loanCharge.getEffectiveDueDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()));

        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(false);
        when(loan.isCashBasedAccountingEnabledOnLoanProduct()).thenReturn(false);
        when(loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()).thenReturn(false);

        doNothing().when(journalEntryWritePlatformService).createJournalEntriesForLoan(any(AccountingBridgeDataDTO.class));
        doNothing().when(loanChargeService).addLoanCharge(any(Loan.class), any(LoanCharge.class));
    }

    @ParameterizedTest
    @MethodSource("loanChargeAccrualTestCases")
    void shouldHandleAccrualBasedOnConfigurationAndDates(boolean isAccrualEnabled, LocalDate businessDate, LocalDate maturityDate, boolean isAccrualExpected) {
        when(configurationDomainService.isImmediateChargeAccrualPostMaturityEnabled()).thenReturn(isAccrualEnabled);
        when(loan.getMaturityDate()).thenReturn(maturityDate);
        when(loanChargeService.handleChargeAppliedTransaction(loan, loanCharge, null)).thenReturn(loanTransaction);
        when(loanChargeService.createChargeAppliedTransaction(loan, loanCharge, null)).thenReturn(loanTransaction);

        if (isAccrualExpected) {
            when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        }

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
             MockedStatic<MoneyHelper> mockedMoneyHelper = mockStatic(MoneyHelper.class)) {

            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(businessDate);
            mockedDateUtils.when(() -> DateUtils.isBeforeBusinessDate(any(LocalDate.class))).thenReturn(false);
            mockedMoneyHelper.when(MoneyHelper::getMathContext).thenReturn(MathContext.DECIMAL64);
            mockedMoneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);

            loanChargeWritePlatformService.addLoanCharge(LOAN_ID, jsonCommand);
        }

        if (isAccrualExpected) {
            verify(loanAccrualsProcessingService, times(1)).postChargeAccrual(loanTransaction);
        } else {
            verify(loanAccrualsProcessingService, never()).postChargeAccrual(any(LoanTransaction.class));
        }
    }

    @Test
    @DisplayName("Post-maturity immediate accrual is skipped and the charge is handed to real-time accrual when real-time applies")
    void shouldDelegateToRealTimeAccrualInsteadOfPostMaturityAccrual_WhenBothApply() {
        when(configurationDomainService.isImmediateChargeAccrualPostMaturityEnabled()).thenReturn(true);
        when(loan.getMaturityDate()).thenReturn(MATURITY_DATE);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loanAccrualsProcessingService.isRealTimeChargeAccrualApplicable(loan)).thenReturn(true);
        when(loanChargeService.createChargeAppliedTransaction(loan, loanCharge, null)).thenReturn(loanTransaction);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
                MockedStatic<MoneyHelper> mockedMoneyHelper = mockStatic(MoneyHelper.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE_AFTER);
            mockedDateUtils.when(() -> DateUtils.isBeforeBusinessDate(any(LocalDate.class))).thenReturn(false);
            mockedMoneyHelper.when(MoneyHelper::getMathContext).thenReturn(MathContext.DECIMAL64);
            mockedMoneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);

            loanChargeWritePlatformService.addLoanCharge(LOAN_ID, jsonCommand);
        }

        verify(loanChargeService, never()).createChargeAppliedTransaction(any(Loan.class), any(LoanCharge.class), any());
        verify(loanAccrualsProcessingService, never()).postChargeAccrual(any(LoanTransaction.class));
        verify(loanAccrualsProcessingService, times(1)).processRealTimeChargeAccruals(loan, List.of(loanCharge));
    }

    @Test
    @DisplayName("Post-maturity immediate accrual posts the charge accrual itself when real-time accrual does not apply")
    void shouldPostImmediatePostMaturityAccrual_WhenRealTimeAccrualIsNotApplicable() {
        when(configurationDomainService.isImmediateChargeAccrualPostMaturityEnabled()).thenReturn(true);
        when(loan.getMaturityDate()).thenReturn(MATURITY_DATE);
        when(loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()).thenReturn(true);
        when(loanAccrualsProcessingService.isRealTimeChargeAccrualApplicable(loan)).thenReturn(false);
        when(loanChargeService.createChargeAppliedTransaction(loan, loanCharge, null)).thenReturn(loanTransaction);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class);
                MockedStatic<MoneyHelper> mockedMoneyHelper = mockStatic(MoneyHelper.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE_AFTER);
            mockedDateUtils.when(() -> DateUtils.isBeforeBusinessDate(any(LocalDate.class))).thenReturn(false);
            mockedMoneyHelper.when(MoneyHelper::getMathContext).thenReturn(MathContext.DECIMAL64);
            mockedMoneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);

            loanChargeWritePlatformService.addLoanCharge(LOAN_ID, jsonCommand);
        }

        InOrder inOrder = inOrder(loanChargeService, loanAccrualsProcessingService);
        inOrder.verify(loanChargeService).createChargeAppliedTransaction(loan, loanCharge, null);
        inOrder.verify(loanAccrualsProcessingService).postChargeAccrual(loanTransaction);
    }

    @Test
    @DisplayName("Applying an overdue penalty hands the applied penalty to real-time accrual after the loan is reprocessed")
    void shouldHandAppliedOverduePenaltiesToRealTimeAccrualAfterReprocessing() {
        LocalDate overdueDueDate = LocalDate.of(2024, 1, 15);
        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loan.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.getCharges()).thenReturn(List.of(chargeDefinition));
        when(chargeDefinition.getChargeTimeType()).thenReturn(ChargeTimeType.OVERDUE_INSTALLMENT.getValue());
        when(chargeDefinition.isLoanCharge()).thenReturn(true);
        when(chargeDefinition.feeFrequency()).thenReturn(null);
        when(loan.isProgressiveSchedule()).thenReturn(true);
        LoanRepaymentScheduleInstallment overdueInstallment = mock(LoanRepaymentScheduleInstallment.class);
        when(overdueInstallment.getDueDate()).thenReturn(overdueDueDate);
        when(loan.fetchRepaymentScheduleInstallment(anyInt())).thenReturn(overdueInstallment);
        when(fromApiJsonHelper.extractLocalDateNamed(eq("dueDate"), any())).thenReturn(overdueDueDate);
        LoanCharge penalty = mock(LoanCharge.class);
        when(penalty.amount()).thenReturn(BigDecimal.TEN);
        when(penalty.getChargePaymentMode()).thenReturn(ChargePaymentMode.REGULAR);
        when(loanChargeAssembler.createNewFromJson(eq(loan), eq(chargeDefinition), any(JsonCommand.class), eq(overdueDueDate)))
                .thenReturn(penalty);
        OverdueLoanScheduleData overdueInstallmentData = new OverdueLoanScheduleData(LOAN_ID, 1L, "15 January 2024", BigDecimal.TEN,
                "dd MMMM yyyy", "en", BigDecimal.TEN, BigDecimal.ZERO, 1);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE_AFTER);

            loanChargeWritePlatformService.applyOverdueChargesForLoan(LOAN_ID, List.of(overdueInstallmentData));
        }

        InOrder inOrder = inOrder(reprocessLoanTransactionsService, loanAccountService, loanAccrualsProcessingService);
        inOrder.verify(reprocessLoanTransactionsService).reprocessTransactions(loan);
        inOrder.verify(loanAccountService).saveAndFlushLoanWithDataIntegrityViolationChecks(loan);
        inOrder.verify(loanAccrualsProcessingService).processRealTimeChargeAccruals(loan, List.of(penalty));
        verify(loanAccrualsProcessingService, never()).postChargeAccrual(any(LoanTransaction.class));
    }

    @Test
    @DisplayName("Adding a percentage-of-disbursement charge hands every created tranche charge to real-time accrual")
    void shouldHandAllTrancheChargesToRealTimeAccrual_WhenChargeIsPercentageOfDisbursement() {
        LoanDisbursementDetails firstTranche = pendingTranche(LocalDate.of(2024, 3, 1));
        LoanDisbursementDetails secondTranche = pendingTranche(LocalDate.of(2024, 4, 1));
        when(loan.getDisbursementDetails()).thenReturn(List.of(firstTranche, secondTranche));
        when(chargeDefinition.isPercentageOfDisbursementAmount()).thenReturn(true);
        when(externalIdFactory.createFromCommand(jsonCommand, "externalId")).thenReturn(ExternalId.empty());
        when(externalIdFactory.create()).thenReturn(ExternalId.empty());
        LoanCharge firstTrancheCharge = trancheCharge(firstTranche);
        LoanCharge secondTrancheCharge = trancheCharge(secondTranche);
        when(loanChargeRepository.saveAndFlush(firstTrancheCharge)).thenReturn(firstTrancheCharge);
        when(loanChargeRepository.saveAndFlush(secondTrancheCharge)).thenReturn(secondTrancheCharge);

        try (MockedStatic<DateUtils> mockedDateUtils = mockStatic(DateUtils.class)) {
            mockedDateUtils.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE_BEFORE);
            mockedDateUtils.when(() -> DateUtils.isBeforeBusinessDate(any(LocalDate.class))).thenReturn(false);

            loanChargeWritePlatformService.addLoanCharge(LOAN_ID, jsonCommand);
        }

        verify(loanAccrualsProcessingService, times(1)).processRealTimeChargeAccruals(loan,
                List.of(firstTrancheCharge, secondTrancheCharge));
    }

    private static LoanDisbursementDetails pendingTranche(final LocalDate expectedDisbursementDate) {
        LoanDisbursementDetails tranche = mock(LoanDisbursementDetails.class);
        when(tranche.actualDisbursementDate()).thenReturn(null);
        when(tranche.expectedDisbursementDateAsLocalDate()).thenReturn(expectedDisbursementDate);
        when(tranche.getPrincipal()).thenReturn(BigDecimal.valueOf(500));
        return tranche;
    }

    private LoanCharge trancheCharge(final LoanDisbursementDetails tranche) {
        LocalDate dueDate = tranche.expectedDisbursementDateAsLocalDate();
        LoanCharge trancheCharge = mock(LoanCharge.class);
        when(trancheCharge.getChargePaymentMode()).thenReturn(ChargePaymentMode.REGULAR);
        when(trancheCharge.getDueLocalDate()).thenReturn(dueDate);
        when(trancheCharge.getEffectiveDueDate()).thenReturn(dueDate);
        when(loanChargeAssembler.createNewWithoutLoan(eq(chargeDefinition), eq(BigDecimal.valueOf(500)), any(), any(), any(), eq(dueDate),
                any(), any(), any(ExternalId.class))).thenReturn(trancheCharge);
        return trancheCharge;
    }

    private static Stream<Arguments> loanChargeAccrualTestCases() {
        return Stream.of(Arguments.of(true, BUSINESS_DATE_AFTER, MATURITY_DATE, true),
                Arguments.of(false, BUSINESS_DATE_AFTER, MATURITY_DATE, false), Arguments.of(true, BUSINESS_DATE_ON, MATURITY_DATE, false),
                Arguments.of(true, BUSINESS_DATE_BEFORE, MATURITY_DATE, false));
    }
}
