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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedPayment;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleGenerateRequest;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodPaymentRateChange;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanEirNotCalculableException;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanPaymentAmountNotCalculableException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAmountCalculationStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

class WorkingCapitalLoanAmortizationScheduleWriteServiceImplTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("EUR", 2, null);
    private static final LocalDate DISBURSEMENT = LocalDate.of(2026, 1, 1);

    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper = mock(
            ProjectedAmortizationScheduleRepositoryWrapper.class);
    private final WorkingCapitalLoanPeriodPaymentRateChangeRepository rateChangeRepository = mock(
            WorkingCapitalLoanPeriodPaymentRateChangeRepository.class);
    private final WorkingCapitalLoanRepository loanRepository = mock(WorkingCapitalLoanRepository.class);
    private final WorkingCapitalLoanAmortizationScheduleWriteServiceImpl service = new WorkingCapitalLoanAmortizationScheduleWriteServiceImpl(
            loanRepository, scheduleRepositoryWrapper, rateChangeRepository, mock(WorkingCapitalLoanTransactionRepository.class));

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        MoneyHelper.initializeTenantRoundingMode("default", 6);
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, DISBURSEMENT)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    /**
     * A strategy input that cannot be resolved is a 400 naming it, never a 500 from a null check, whatever the
     * strategy.
     */
    @ParameterizedTest
    @CsvSource({ "PAYMENT_AMOUNT, paymentAmount, , ", "ANNUAL_EIR, annualEir, , ", "TPV, totalPaymentVolume, , 18",
            "TPV, periodPaymentRate, 100000, " })
    void missingStrategyInput_ShouldBeAValidationError(final WorkingCapitalPaymentAmountCalculationStrategy strategy,
            final String paramName, final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate) {
        final WorkingCapitalLoan loan = loan(strategy, totalPaymentVolume, periodPaymentRate);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.generateAndSaveAmortizationScheduleOnDisbursement(loan, new BigDecimal("9000"), LocalDate.of(2026, 1, 1)));

        assertEquals(List.of("validation.msg." + WorkingCapitalLoanConstants.WCL_RESOURCE_NAME + "." + paramName + ".cannot.be.blank"),
                exception.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList());
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());
    }

    /** A zero volume is present but unusable: the same not-calculable domain error as any other unpayable input. */
    @ParameterizedTest
    @CsvSource({ "0, 18", "100000, 0" })
    void unusableTpvInput_ShouldBeNotCalculable(final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate) {
        final WorkingCapitalLoan loan = loan(WorkingCapitalPaymentAmountCalculationStrategy.TPV, totalPaymentVolume, periodPaymentRate);

        assertThrows(WorkingCapitalLoanEirNotCalculableException.class,
                () -> service.generateAndSaveAmortizationScheduleOnDisbursement(loan, new BigDecimal("9000"), LocalDate.of(2026, 1, 1)));
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());
    }

    /**
     * A rate change re-solves the segment it opens against the balance and the unearned fee reached on its effective
     * date, so a rate steep enough to close that segment in a day solves to an EIR above what the schedule can report.
     * The reconstruction has no feasibility pre-check to run beforehand - only it knows the position the new rate is
     * solved against - so it is the solve that has to answer, and it answers with the same not-calculable domain error
     * every other entry point gives, leaving the stored schedule untouched.
     */
    @Test
    void aRateChangeSolvingAboveTheEirCapIsNotCalculableRatherThanAServerError() {
        final WorkingCapitalLoanPeriodPaymentRateChange steepChange = rateChange(new BigDecimal("9000"));
        final WorkingCapitalLoan loan = loanWithRateChange(steepChange);

        assertThrows(WorkingCapitalLoanEirNotCalculableException.class, () -> service.regenerateAmortizationScheduleOnRateChange(loan));
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());

        // The counter-proof: the same loan and the same replay path accept a rate the segment can carry, so what the
        // assertion above rejected is the rate rather than the fixture.
        when(steepChange.getNewRate()).thenReturn(new BigDecimal("20"));
        assertDoesNotThrow(() -> service.regenerateAmortizationScheduleOnRateChange(loan));
        verify(scheduleRepositoryWrapper).writeModel(any(), any());
    }

    /**
     * Every other rebuild replays the same stored rate changes against the position it restates - a discount fee
     * adjustment or its undo, and the rebuild after transaction reprocessing - so a segment solving above the EIR cap
     * there must be the same not-calculable domain error, not the solver's own exception escaping as a server error.
     */
    @Test
    void otherRebuildsReplayingARateChangeAboveTheEirCapAreNotCalculableRatherThanAServerError() {
        final WorkingCapitalLoanPeriodPaymentRateChange steepChange = rateChange(new BigDecimal("9000"));
        final WorkingCapitalLoan loan = loanWithRateChange(steepChange);

        assertThrows(WorkingCapitalLoanEirNotCalculableException.class, () -> service.applyDiscountFeeAdjustment(loan));
        assertThrows(WorkingCapitalLoanEirNotCalculableException.class,
                () -> service.rebuildScheduleFromPrincipalPayments(loan, List.of(), List.of()));
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());

        when(steepChange.getNewRate()).thenReturn(new BigDecimal("20"));
        assertDoesNotThrow(() -> service.applyDiscountFeeAdjustment(loan));
        assertDoesNotThrow(() -> service.rebuildScheduleFromPrincipalPayments(loan, List.of(), List.of()));
    }

    /** A disbursed TPV loan whose stored schedule is about to be rebuilt with {@code change} replayed into it. */
    private WorkingCapitalLoan loanWithRateChange(final WorkingCapitalLoanPeriodPaymentRateChange change) {
        final WorkingCapitalLoan loan = loan(WorkingCapitalPaymentAmountCalculationStrategy.TPV, new BigDecimal("100000"),
                new BigDecimal("18"), new BigDecimal("8000"));
        when(loan.getId()).thenReturn(1L);
        when(loan.getFirstActualDisbursementAmount()).thenReturn(new BigDecimal("9000"));
        when(loan.getFirstActualDisbursementDate()).thenReturn(DISBURSEMENT);
        when(scheduleRepositoryWrapper.readModel(any(), any(), any()))
                .thenReturn(Optional.of(ProjectedAmortizationScheduleModel.generate(WorkingCapitalAmortizationType.EIR,
                        new BigDecimal("8000"), new BigDecimal("9000"), new BigDecimal("100000"), new BigDecimal("18"), 360,
                        WorkingCapitalLoanPeriodFrequencyType.DAYS, 1, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT)));
        when(rateChangeRepository.findByWorkingCapitalLoanIdAndReversedFalse(1L)).thenReturn(List.of(change));
        return loan;
    }

    private static WorkingCapitalLoanPeriodPaymentRateChange rateChange(final BigDecimal newRate) {
        final WorkingCapitalLoanPeriodPaymentRateChange change = mock(WorkingCapitalLoanPeriodPaymentRateChange.class);
        when(change.getEffectiveDate()).thenReturn(DISBURSEMENT.plusDays(1));
        when(change.getNewRate()).thenReturn(newRate);
        when(change.getCreatedDate()).thenReturn(Optional.empty());
        when(change.getId()).thenReturn(7L);
        return change;
    }

    /** YEARS used to be accepted on products, so disbursement must fail on the frequency, not as uncalculable. */
    @Test
    void productStillStoringYears_ShouldBeAFrequencyTypeValidationError() {
        final WorkingCapitalLoan loan = loan(WorkingCapitalPaymentAmountCalculationStrategy.TPV, new BigDecimal("100000"),
                new BigDecimal("18"));
        final WorkingCapitalLoanProductRelatedDetail productDetail = loan.getLoanProduct().getRelatedDetail();
        when(productDetail.getRepaymentFrequencyType()).thenReturn(WorkingCapitalLoanPeriodFrequencyType.YEARS);
        when(productDetail.getRepaymentEvery()).thenReturn(1);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.generateAndSaveAmortizationScheduleOnDisbursement(loan, new BigDecimal("9000"), LocalDate.of(2026, 1, 1)));

        assertEquals(
                List.of("validation.msg." + WorkingCapitalLoanConstants.WCL_RESOURCE_NAME
                        + ".repaymentFrequencyType.invalid.period.frequency.type"),
                exception.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList());
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());
    }

    /**
     * The term solves to ten periods, but a month interval this large dates the eighth past the calendar: not
     * calculable rather than a 500.
     */
    @Test
    void dueDatesPastTheCalendar_ShouldBeNotCalculable() {
        final WorkingCapitalLoan loan = loan(WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT, null, null);
        final WorkingCapitalLoanProductRelatedDetails loanDetails = loan.getLoanProductRelatedDetails();
        when(loanDetails.getAmortizationType()).thenReturn(WorkingCapitalAmortizationType.FLAT);
        when(loanDetails.getPaymentAmount()).thenReturn(new BigDecimal("1000"));
        when(loanDetails.getRepaymentFrequencyType()).thenReturn(WorkingCapitalLoanPeriodFrequencyType.MONTHS);
        when(loanDetails.getRepaymentEvery()).thenReturn(1_500_000_000);

        final WorkingCapitalLoanPaymentAmountNotCalculableException exception = assertThrows(
                WorkingCapitalLoanPaymentAmountNotCalculableException.class,
                () -> service.generateAndSaveAmortizationScheduleOnDisbursement(loan, new BigDecimal("9000"), LocalDate.of(2026, 1, 1)));

        assertInstanceOf(ArithmeticException.class, exception.getCause());
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());
    }

    /**
     * The internal generate endpoint resolves the same stored frequency, so a YEARS left on a product is a 400 there
     * too.
     */
    @Test
    void internalGenerateWithAStoredYearsFrequency_ShouldBeAFrequencyTypeValidationError() {
        final WorkingCapitalLoan loan = loanStoringFrequency(WorkingCapitalLoanPeriodFrequencyType.YEARS, 1);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        final ProjectedAmortizationScheduleGenerateRequest request = new ProjectedAmortizationScheduleGenerateRequest();
        request.setDiscountFeeAmount(new BigDecimal("1000"));
        request.setNetDisbursementAmount(new BigDecimal("9000"));
        request.setTotalPaymentVolume(new BigDecimal("100000"));
        request.setPeriodPaymentRate(new BigDecimal("18"));
        request.setNpvDayCount(360);
        request.setExpectedDisbursementDate(DISBURSEMENT);

        final PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.generateAndSaveAmortizationSchedule(1L, request));

        assertEquals(
                List.of("validation.msg." + WorkingCapitalLoanConstants.WCL_RESOURCE_NAME
                        + ".repaymentFrequencyType.invalid.period.frequency.type"),
                exception.getErrors().stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList());
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());
    }

    /** A rebuild keeps the dates the stored schedule was built on, even when the loan records another frequency. */
    @Test
    void rebuildOfAStoredDailySchedule_ShouldStayDailyWhateverTheLoanStores() {
        final WorkingCapitalLoan loan = loanStoringFrequency(WorkingCapitalLoanPeriodFrequencyType.DAYS, 30);
        when(scheduleRepositoryWrapper.readModel(any(), any(), any()))
                .thenReturn(Optional.of(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 1)));

        service.rebuildScheduleFromPrincipalPayments(loan, List.of(), List.of());

        final ProjectedAmortizationScheduleModel written = writtenModel(loan);
        assertEquals(WorkingCapitalLoanPeriodFrequencyType.DAYS, written.repaymentFrequencyType());
        assertEquals(1, written.repaymentEvery());
        assertEquals(LocalDate.of(2026, 1, 2), written.projectedPayments().get(1).date());
        assertEquals(dueDates(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 1)), dueDates(written));
    }

    @Test
    void rebuildWithoutAStoredSchedule_ShouldUseTheLoansFrequency() {
        final WorkingCapitalLoan loan = loanStoringFrequency(WorkingCapitalLoanPeriodFrequencyType.DAYS, 30);

        service.rebuildScheduleFromPrincipalPayments(loan, List.of(), List.of());

        final ProjectedAmortizationScheduleModel written = writtenModel(loan);
        assertEquals(WorkingCapitalLoanPeriodFrequencyType.DAYS, written.repaymentFrequencyType());
        assertEquals(30, written.repaymentEvery());
        assertEquals(LocalDate.of(2026, 1, 31), written.projectedPayments().get(1).date());
        assertEquals(dueDates(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 30)), dueDates(written));
    }

    /** Rate changes and discount adjustments restate the stored schedule, so they keep its frequency as well. */
    @Test
    void rateChangeAndDiscountRebuilds_ShouldKeepTheStoredSchedulesFrequency() {
        final WorkingCapitalLoan loan = loanStoringFrequency(WorkingCapitalLoanPeriodFrequencyType.DAYS, 30);
        when(scheduleRepositoryWrapper.readModel(any(), any(), any()))
                .thenReturn(Optional.of(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.WEEKS, 1)));

        final ProjectedAmortizationScheduleModel onRateChange = service.regenerateAmortizationScheduleOnRateChange(loan);
        service.applyDiscountFeeAdjustment(loan);

        final ArgumentCaptor<ProjectedAmortizationScheduleModel> written = ArgumentCaptor
                .forClass(ProjectedAmortizationScheduleModel.class);
        verify(scheduleRepositoryWrapper, times(2)).writeModel(any(), written.capture());
        final List<LocalDate> weekly = dueDates(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.WEEKS, 1));
        assertEquals(LocalDate.of(2026, 1, 8), weekly.get(1));
        assertEquals(weekly, dueDates(onRateChange));
        assertEquals(weekly, dueDates(written.getAllValues().get(1)));
    }

    /** Disbursement re-dates the schedule approval built; a loan approved before frequencies applied stays daily. */
    @Test
    void disbursementOverAStoredSchedule_ShouldKeepItsFrequency() {
        final WorkingCapitalLoan loan = loanStoringFrequency(WorkingCapitalLoanPeriodFrequencyType.DAYS, 30);
        when(scheduleRepositoryWrapper.readModel(any(), any(), any()))
                .thenReturn(Optional.of(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 1)));

        service.generateAndSaveAmortizationScheduleOnDisbursement(loan, new BigDecimal("9000"), DISBURSEMENT);

        final ProjectedAmortizationScheduleModel written = writtenModel(loan);
        assertEquals(1, written.repaymentEvery());
        assertEquals(dueDates(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 1)), dueDates(written));
    }

    /**
     * Approval builds the schedule afresh from the loan, which may have been modified since an undone approval left a
     * schedule behind; undoing a disbursement rebuilds the schedule the loan already had.
     */
    @Test
    void approval_ShouldUseTheLoansFrequencyWhileUndoDisbursalKeepsTheStoredOne() {
        final WorkingCapitalLoan loan = approvedLoanStoringFrequency(WorkingCapitalLoanPeriodFrequencyType.DAYS, 30);
        when(scheduleRepositoryWrapper.readModel(any(), any(), any()))
                .thenReturn(Optional.of(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 1)));

        service.generateAndSaveAmortizationScheduleOnApproval(loan);
        service.regenerateAmortizationScheduleOnUndoDisbursal(loan);

        final ArgumentCaptor<ProjectedAmortizationScheduleModel> written = ArgumentCaptor
                .forClass(ProjectedAmortizationScheduleModel.class);
        verify(scheduleRepositoryWrapper, times(2)).writeModel(any(), written.capture());
        assertEquals(30, written.getAllValues().get(0).repaymentEvery());
        assertEquals(dueDates(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 30)), dueDates(written.getAllValues().get(0)));
        assertEquals(1, written.getAllValues().get(1).repaymentEvery());
        assertEquals(dueDates(referenceSchedule(WorkingCapitalLoanPeriodFrequencyType.DAYS, 1)), dueDates(written.getAllValues().get(1)));
    }

    private static ProjectedAmortizationScheduleModel referenceSchedule(final WorkingCapitalLoanPeriodFrequencyType type, final int every) {
        return ProjectedAmortizationScheduleModel.generate(WorkingCapitalAmortizationType.EIR, new BigDecimal("1000"),
                new BigDecimal("9000"), new BigDecimal("100000"), new BigDecimal("18"), 360, type, every, DISBURSEMENT, MC, CURRENCY,
                DISBURSEMENT);
    }

    private static List<LocalDate> dueDates(final ProjectedAmortizationScheduleModel model) {
        return model.projectedPayments().stream().map(ProjectedPayment::date).toList();
    }

    private ProjectedAmortizationScheduleModel writtenModel(final WorkingCapitalLoan loan) {
        final ArgumentCaptor<ProjectedAmortizationScheduleModel> written = ArgumentCaptor
                .forClass(ProjectedAmortizationScheduleModel.class);
        verify(scheduleRepositoryWrapper).writeModel(any(), written.capture());
        return written.getValue();
    }

    /** A disbursed TPV loan of 9000 net, 1000 discount fee, whose own details record {@code type}/{@code every}. */
    private static WorkingCapitalLoan loanStoringFrequency(final WorkingCapitalLoanPeriodFrequencyType type, final int every) {
        final WorkingCapitalLoan loan = loan(WorkingCapitalPaymentAmountCalculationStrategy.TPV, new BigDecimal("100000"),
                new BigDecimal("18"), new BigDecimal("1000"));
        when(loan.getId()).thenReturn(1L);
        when(loan.getFirstActualDisbursementAmount()).thenReturn(new BigDecimal("9000"));
        when(loan.getFirstActualDisbursementDate()).thenReturn(DISBURSEMENT);
        when(loan.getLoanProductRelatedDetails().getRepaymentFrequencyType()).thenReturn(type);
        when(loan.getLoanProductRelatedDetails().getRepaymentEvery()).thenReturn(every);
        return loan;
    }

    private static WorkingCapitalLoan approvedLoanStoringFrequency(final WorkingCapitalLoanPeriodFrequencyType type, final int every) {
        final WorkingCapitalLoan loan = loanStoringFrequency(type, every);
        final WorkingCapitalLoanDisbursementDetails disbursement = mock(WorkingCapitalLoanDisbursementDetails.class);
        when(disbursement.getExpectedDisbursementDate()).thenReturn(DISBURSEMENT);
        when(loan.getDisbursementDetails()).thenReturn(List.of(disbursement));
        when(loan.getApprovedPrincipal()).thenReturn(new BigDecimal("9000"));
        return loan;
    }

    private static WorkingCapitalLoan loan(final WorkingCapitalPaymentAmountCalculationStrategy strategy,
            final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate) {
        return loan(strategy, totalPaymentVolume, periodPaymentRate, new BigDecimal("1000"));
    }

    /** A loan whose product carries no default for any strategy input, so only the loan's own values count. */
    private static WorkingCapitalLoan loan(final WorkingCapitalPaymentAmountCalculationStrategy strategy,
            final BigDecimal totalPaymentVolume, final BigDecimal periodPaymentRate, final BigDecimal discount) {
        final WorkingCapitalLoanProductRelatedDetails loanDetails = mock(WorkingCapitalLoanProductRelatedDetails.class);
        when(loanDetails.getPaymentAmountCalculationStrategy()).thenReturn(strategy);
        when(loanDetails.getPeriodPaymentRate()).thenReturn(periodPaymentRate);
        when(loanDetails.getNpvDayCount()).thenReturn(360);
        when(loanDetails.getDiscount()).thenReturn(discount);
        final WorkingCapitalLoanProduct product = mock(WorkingCapitalLoanProduct.class);
        when(product.getRelatedDetail()).thenReturn(mock(WorkingCapitalLoanProductRelatedDetail.class));
        when(product.getCurrency()).thenReturn(new MonetaryCurrency("EUR", 2, null));
        final WorkingCapitalLoan loan = mock(WorkingCapitalLoan.class);
        when(loan.getLoanProductRelatedDetails()).thenReturn(loanDetails);
        when(loan.getLoanProduct()).thenReturn(product);
        when(loan.getTotalPaymentVolume()).thenReturn(totalPaymentVolume);
        return loan;
    }
}
