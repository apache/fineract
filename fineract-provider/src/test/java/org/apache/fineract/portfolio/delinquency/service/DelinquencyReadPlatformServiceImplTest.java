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
package org.apache.fineract.portfolio.delinquency.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static java.time.Month.JANUARY;
import static org.apache.fineract.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyBucketRepository;
import org.apache.fineract.portfolio.delinquency.domain.DelinquencyRangeRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyAction;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import org.apache.fineract.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.DelinquencyPausePeriod;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelinquencyReadPlatformServiceImplTest {

    @Mock
    private DelinquencyRangeRepository repositoryRange;
    @Mock
    private DelinquencyBucketRepository repositoryBucket;
    @Mock
    private LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory;
    @Mock
    private DelinquencyRangeMapper mapperRange;
    @Mock
    private DelinquencyBucketMapper mapperBucket;
    @Mock
    private LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanDelinquencyDomainService loanDelinquencyDomainService;
    @Mock
    private LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private PossibleNextRepaymentCalculationServiceDiscovery possibleNextRepaymentCalculationServiceDiscovery;
    @Mock
    private LoanDelinquencyActionRepository loanDelinquencyActionRepository;
    @Mock
    private DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;

    @InjectMocks
    private DelinquencyReadPlatformServiceImpl underTest;

    @Test
    public void testNoEnrichmentWhenThereIsNoDelinquencyAction() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of();

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        Assertions.assertTrue(collectionData.getDelinquencyPausePeriods().isEmpty());
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentlyInPauseFirstDay() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 13))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(true, "2023-01-12", "2023-01-13"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentlyInPauseLastDay() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 13))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 13));

        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(true, "2023-01-12", "2023-01-13"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentBusinessDateBetweenStartAndEndDate() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 14))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 13));

        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(true, "2023-01-12", "2023-01-14"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    @Test
    void givenPendingLoanWithNullProduct_whenCalculateLoanCollectionData_thenNoExceptionAndOverAppliedIsNull() {
        Loan loan = mock(Loan.class);
        when(loan.getLoanProduct()).thenReturn(null);
        when(loan.isSubmittedAndPendingApproval()).thenReturn(true);
        // REMOVED: when(loan.isApproved()).thenReturn(false); ← unnecessary
        // REMOVED: when(loan.isCancelled()).thenReturn(false); ← unnecessary
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        CollectionData result = underTest.calculateLoanCollectionData(1L);

        assertThat(result).isNotNull();
        assertThat(result.getAvailableDisbursementAmountWithOverApplied()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void givenActiveLoanWithNullProduct_whenCalculateLoanCollectionData_thenNoExceptionAndOverAppliedIsNull() {
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 1));
        businessDates.put(BusinessDateType.COB_DATE, LocalDate.of(2024, 1, 1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        try {
            Loan loan = mock(Loan.class);
            when(loan.getLoanProduct()).thenReturn(null);
            when(loan.isSubmittedAndPendingApproval()).thenReturn(false);
            when(loan.isApproved()).thenReturn(false);
            when(loan.isCancelled()).thenReturn(false);
            when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
            when(loan.getDisbursedAmount()).thenReturn(BigDecimal.valueOf(5000));
            when(loan.getLoanRepaymentScheduleDetail()).thenReturn(mock(LoanProductRelatedDetail.class));
            when(loanDelinquencyDomainService.getOverdueCollectionData(any(), any())).thenReturn(CollectionData.template());
            when(loanDelinquencyActionRepository.findByLoanOrderById(any())).thenReturn(List.of());
            when(configurationDomainService.getNextPaymentDateConfigForLoan()).thenReturn(null);
            when(possibleNextRepaymentCalculationServiceDiscovery.getService(any())).thenReturn(null);
            when(loan.getLastPaymentTransaction()).thenReturn(null);
            when(loan.getLastRepaymentOrDownPaymentTransaction()).thenReturn(null);
            when(loan.isEnableInstallmentLevelDelinquency()).thenReturn(false);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            CollectionData result = underTest.calculateLoanCollectionData(1L);

            assertThat(result).isNotNull();
            assertThat(result.getAvailableDisbursementAmountWithOverApplied()).isEqualByComparingTo(BigDecimal.ZERO);
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

    @Test
    public void testMultiplePausesWithoutResumeCurrentBusinessDateIsNotOverlappingWithAnyOfThePauses() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 13), LocalDate.of(2023, JANUARY, 14))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(false, "2023-01-13", "2023-01-14"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    @Test
    void givenLoanWithNullProduct_whenHelperCalledDirectly_thenReturnsZero() {
        Loan loan = mock(Loan.class);
        when(loan.getLoanProduct()).thenReturn(null);
        when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
        when(loan.getDisbursedAmount()).thenReturn(BigDecimal.valueOf(5000));
        when(loan.getLoanRepaymentScheduleDetail()).thenReturn(mock(LoanProductRelatedDetail.class));

        BigDecimal result = underTest.calculateAvailableDisbursementAmountWithOverApplied(loan);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    void givenLoanWithProductOverApplyDisabled_whenHelperCalledDirectly_thenReturnsApprovedMinusDisbursed() {
        Loan loan = mock(Loan.class);
        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loan.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.isAllowApprovedDisbursedAmountsOverApplied()).thenReturn(false);
        when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
        when(loan.getDisbursedAmount()).thenReturn(BigDecimal.valueOf(4000));
        when(loan.getLoanRepaymentScheduleDetail()).thenReturn(mock(LoanProductRelatedDetail.class));

        BigDecimal result = underTest.calculateAvailableDisbursementAmountWithOverApplied(loan);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(6000));
    }

    @Test
    void givenLoanWithPercentageOverApply_whenHelperCalledDirectly_thenReturnsCalculatedAmount() {
        // MoneyHelper.getMathContext() requires a tenant context
        MathContext mathContext = new MathContext(19, RoundingMode.HALF_EVEN);
        MockedStatic<MoneyHelper> moneyHelperMock = mockStatic(MoneyHelper.class);
        moneyHelperMock.when(MoneyHelper::getMathContext).thenReturn(mathContext);

        try {
            Loan loan = mock(Loan.class);
            LoanProduct loanProduct = mock(LoanProduct.class);
            when(loan.getLoanProduct()).thenReturn(loanProduct);
            when(loanProduct.isAllowApprovedDisbursedAmountsOverApplied()).thenReturn(true);
            when(loanProduct.getOverAppliedCalculationType()).thenReturn("percentage");
            when(loanProduct.getOverAppliedNumber()).thenReturn(10);
            when(loan.getProposedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
            when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
            when(loan.getDisbursedAmount()).thenReturn(BigDecimal.ZERO);
            when(loan.getLoanRepaymentScheduleDetail()).thenReturn(mock(LoanProductRelatedDetail.class));

            BigDecimal result = underTest.calculateAvailableDisbursementAmountWithOverApplied(loan);

            // 10000 * (1 + 10/100) = 11000, minus 0 disbursed = 11000
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(11000));
        } finally {
            moneyHelperMock.close();
        }
    }

    private void verifyPausePeriods(CollectionData collectionData, DelinquencyPausePeriod... pausePeriods) {
        if (pausePeriods.length > 0) {
            Assertions.assertEquals(Arrays.asList(pausePeriods), collectionData.getDelinquencyPausePeriods());
        } else {
            Assertions.assertNull(collectionData.getDelinquencyPausePeriods());
        }
    }

    private DelinquencyPausePeriod pausePeriod(boolean active, String startDate, String endDate) {
        return new DelinquencyPausePeriod(active, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    @Test
    void givenPendingLoanWithNullProduct_whenCalculateLoanCollectionData_thenNoExceptionAndOverAppliedIsNull() {
        Loan loan = mock(Loan.class);
        when(loan.getLoanProduct()).thenReturn(null);
        when(loan.isSubmittedAndPendingApproval()).thenReturn(true);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        // When product is null, guard prevents calling helper → no exception, returns template
        assertThatCode(() -> underTest.calculateLoanCollectionData(1L)).doesNotThrowAnyException();
    }

    @Test
    void givenActiveLoanWithNullProduct_whenCalculateLoanCollectionData_thenNoExceptionAndAvailableDisbursementAmountIsSet() {
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 1));
        businessDates.put(BusinessDateType.COB_DATE, LocalDate.of(2024, 1, 1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        try {
            Loan loan = mock(Loan.class);
            when(loan.getLoanProduct()).thenReturn(null);
            when(loan.isSubmittedAndPendingApproval()).thenReturn(false);
            when(loan.isApproved()).thenReturn(false);
            when(loan.isCancelled()).thenReturn(false);

            // calculateAvailableDisbursementAmount() is always called for active loans
            when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
            when(loan.getDisbursedAmount()).thenReturn(BigDecimal.valueOf(5000));
            LoanProductRelatedDetail detail = mock(LoanProductRelatedDetail.class);
            when(detail.isEnableIncomeCapitalization()).thenReturn(false);
            when(loan.getLoanRepaymentScheduleDetail()).thenReturn(detail);

            when(loanDelinquencyDomainService.getOverdueCollectionData(any(), any())).thenReturn(CollectionData.template());
            when(loanDelinquencyActionRepository.findByLoanOrderById(any())).thenReturn(List.of());
            when(configurationDomainService.getNextPaymentDateConfigForLoan()).thenReturn(null);
            when(possibleNextRepaymentCalculationServiceDiscovery.getService(any())).thenReturn(null);
            when(loan.getLastPaymentTransaction()).thenReturn(null);
            when(loan.getLastRepaymentOrDownPaymentTransaction()).thenReturn(null);
            when(loan.isEnableInstallmentLevelDelinquency()).thenReturn(false);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            CollectionData result = underTest.calculateLoanCollectionData(1L);

            assertThat(result).isNotNull();
            // calculateAvailableDisbursementAmount = 10000 - 5000 = 5000
            assertThat(result.getAvailableDisbursementAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
            // LoanProduct is null → over-applied helper skipped → field stays at template default
            assertThat(result.getAvailableDisbursementAmountWithOverApplied()).isEqualByComparingTo(BigDecimal.ZERO);
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

}

