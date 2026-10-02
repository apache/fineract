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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodPaymentRateChange;
import org.apache.fineract.portfolio.workingcapitalloan.exception.WorkingCapitalLoanEirNotCalculableException;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanPeriodPaymentRateChangeRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanTransactionRepository;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAmountCalculationStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WorkingCapitalLoanAmortizationScheduleWriteServiceImplTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("EUR", 2, null);
    private static final LocalDate DISBURSEMENT = LocalDate.of(2026, 1, 1);

    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper = mock(
            ProjectedAmortizationScheduleRepositoryWrapper.class);
    private final WorkingCapitalLoanPeriodPaymentRateChangeRepository rateChangeRepository = mock(
            WorkingCapitalLoanPeriodPaymentRateChangeRepository.class);
    private final WorkingCapitalLoanAmortizationScheduleWriteServiceImpl service = new WorkingCapitalLoanAmortizationScheduleWriteServiceImpl(
            mock(WorkingCapitalLoanRepository.class), scheduleRepositoryWrapper, rateChangeRepository,
            mock(WorkingCapitalLoanTransactionRepository.class));

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
        final WorkingCapitalLoan loan = loan(WorkingCapitalPaymentAmountCalculationStrategy.TPV, new BigDecimal("100000"),
                new BigDecimal("18"), new BigDecimal("8000"));
        when(loan.getId()).thenReturn(1L);
        when(loan.getFirstActualDisbursementAmount()).thenReturn(new BigDecimal("9000"));
        when(loan.getFirstActualDisbursementDate()).thenReturn(DISBURSEMENT);
        when(scheduleRepositoryWrapper.readModel(any(), any(), any()))
                .thenReturn(Optional.of(ProjectedAmortizationScheduleModel.generateEir(new BigDecimal("8000"), new BigDecimal("9000"),
                        new BigDecimal("100000"), new BigDecimal("18"), 360, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT)));

        final WorkingCapitalLoanPeriodPaymentRateChange steepChange = mock(WorkingCapitalLoanPeriodPaymentRateChange.class);
        when(steepChange.getEffectiveDate()).thenReturn(DISBURSEMENT.plusDays(1));
        when(steepChange.getNewRate()).thenReturn(new BigDecimal("9000"));
        when(steepChange.getCreatedDate()).thenReturn(Optional.empty());
        when(steepChange.getId()).thenReturn(7L);
        when(rateChangeRepository.findByWorkingCapitalLoanIdAndReversedFalse(1L)).thenReturn(List.of(steepChange));

        assertThrows(WorkingCapitalLoanEirNotCalculableException.class, () -> service.regenerateAmortizationScheduleOnRateChange(loan));
        verify(scheduleRepositoryWrapper, never()).writeModel(any(), any());

        // The counter-proof: the same loan and the same replay path accept a rate the segment can carry, so what the
        // assertion above rejected is the rate rather than the fixture.
        when(steepChange.getNewRate()).thenReturn(new BigDecimal("20"));
        assertDoesNotThrow(() -> service.regenerateAmortizationScheduleOnRateChange(loan));
        verify(scheduleRepositoryWrapper).writeModel(any(), any());
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
