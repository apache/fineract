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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationIncomeType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationPeriodType;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanChargeValidator;
import org.apache.fineract.portfolio.tax.service.ChargeTaxApplicationService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanChargeAmortizationCopyTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 1, 15);
    private static MockedStatic<MoneyHelper> moneyHelperMock;
    private static MockedStatic<DateUtils> dateUtilsMock;

    @BeforeAll
    static void setUpStatics() {
        moneyHelperMock = mockStatic(MoneyHelper.class);
        moneyHelperMock.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelperMock.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        dateUtilsMock = mockStatic(DateUtils.class);
        dateUtilsMock.when(DateUtils::getBusinessLocalDate).thenReturn(BUSINESS_DATE);
    }

    @AfterAll
    static void tearDownStatics() {
        moneyHelperMock.close();
        dateUtilsMock.close();
    }

    @Test
    void createCopiesAmortizationConfigurationFromChargeDefinition() {
        final Charge charge = charge(ChargeAmortizationPeriodType.NUMBER_OF_DAYS, 30, ChargeAmortizationCalculationType.FLAT,
                ChargeAmortizationIncomeType.INTEREST);

        final LoanCharge loanCharge = service().create(mock(Loan.class), charge, BigDecimal.valueOf(1000), BigDecimal.valueOf(50), null,
                null, null, null, null, null, ExternalId.empty());

        assertThat(loanCharge.getAmortizationPeriodType()).isEqualTo(ChargeAmortizationPeriodType.NUMBER_OF_DAYS);
        assertThat(loanCharge.getAmortizationDays()).isEqualTo(30);
        assertThat(loanCharge.getAmortizationCalculationType()).isEqualTo(ChargeAmortizationCalculationType.FLAT);
        assertThat(loanCharge.getAmortizationIncomeType()).isEqualTo(ChargeAmortizationIncomeType.INTEREST);
        assertThat(loanCharge.isAmortized()).isTrue();
    }

    @Test
    void createDefaultsToNoneWhenChargeIsNotAmortized() {
        final Charge charge = charge(ChargeAmortizationPeriodType.NONE, null, null, null);

        final LoanCharge loanCharge = service().create(mock(Loan.class), charge, BigDecimal.valueOf(1000), BigDecimal.valueOf(50), null,
                null, null, null, null, null, ExternalId.empty());

        assertThat(loanCharge.getAmortizationPeriodType()).isEqualTo(ChargeAmortizationPeriodType.NONE);
        assertThat(loanCharge.getAmortizationDays()).isNull();
        assertThat(loanCharge.getAmortizationCalculationType()).isNull();
        assertThat(loanCharge.getAmortizationIncomeType()).isNull();
        assertThat(loanCharge.isAmortized()).isFalse();
    }

    @Test
    void toDataExposesCopiedAmortizationConfiguration() {
        final Charge charge = charge(ChargeAmortizationPeriodType.FULL_LOAN_TERM, null, ChargeAmortizationCalculationType.EFFECTIVE_RATE,
                ChargeAmortizationIncomeType.FEE);
        when(charge.getName()).thenReturn("Amortized fee");
        when(charge.getId()).thenReturn(9L);
        when(charge.toData()).thenReturn(org.apache.fineract.portfolio.charge.data.ChargeData.builder().build());

        final Loan loan = mock(Loan.class);
        when(loan.getId()).thenReturn(1L);

        final LoanCharge loanCharge = service().create(loan, charge, BigDecimal.valueOf(1000), BigDecimal.valueOf(50), null, null, null,
                null, null, null, ExternalId.empty());

        assertThat(loanCharge.toData().getAmortizationPeriodType().getId()).isEqualTo("FULL_LOAN_TERM");
        assertThat(loanCharge.toData().getAmortizationCalculationType().getId()).isEqualTo("EFFECTIVE_RATE");
        assertThat(loanCharge.toData().getAmortizationIncomeType().getId()).isEqualTo("FEE");
    }

    @Test
    void createValidatesChargeAmortizationWhenLoanIsPresent() {
        final Charge charge = charge(ChargeAmortizationPeriodType.FULL_LOAN_TERM, null, ChargeAmortizationCalculationType.FLAT,
                ChargeAmortizationIncomeType.FEE);
        final Loan loan = mock(Loan.class);
        final LoanChargeValidator validator = mock(LoanChargeValidator.class);

        service(validator).create(loan, charge, BigDecimal.valueOf(1000), BigDecimal.valueOf(50), null, null, null, null, null, null,
                ExternalId.empty());

        verify(validator).validateChargeAmortizationAllowed(loan, charge);
    }

    @Test
    void createSkipsChargeAmortizationValidationWhenLoanIsNull() {
        final Charge charge = charge(ChargeAmortizationPeriodType.FULL_LOAN_TERM, null, ChargeAmortizationCalculationType.FLAT,
                ChargeAmortizationIncomeType.FEE);
        final LoanChargeValidator validator = mock(LoanChargeValidator.class);

        service(validator).create(null, charge, BigDecimal.valueOf(1000), BigDecimal.valueOf(50), null, null, null, null, null, null,
                ExternalId.empty());

        verify(validator, never()).validateChargeAmortizationAllowed(any(), any());
    }

    private static LoanChargeService service() {
        return service(mock(LoanChargeValidator.class));
    }

    private static LoanChargeService service(final LoanChargeValidator validator) {
        return new LoanChargeService(validator, mock(LoanTransactionProcessingService.class),
                mock(org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine.class), mock(LoanBalanceService.class),
                mock(LoanScheduleGeneratorService.class), mock(ChargeTaxApplicationService.class));
    }

    private static Charge charge(final ChargeAmortizationPeriodType periodType, final Integer days,
            final ChargeAmortizationCalculationType calculationType, final ChargeAmortizationIncomeType incomeType) {
        final Charge charge = mock(Charge.class);
        when(charge.isPenalty()).thenReturn(false);
        when(charge.getMinCap()).thenReturn(null);
        when(charge.getMaxCap()).thenReturn(null);
        when(charge.getChargeTimeType()).thenReturn(ChargeTimeType.DISBURSEMENT.getValue());
        when(charge.getChargeCalculation()).thenReturn(ChargeCalculationType.FLAT.getValue());
        when(charge.getAmount()).thenReturn(BigDecimal.valueOf(50));
        when(charge.getChargePaymentMode()).thenReturn(ChargePaymentMode.REGULAR.getValue());
        when(charge.getAmortizationPeriodType()).thenReturn(periodType);
        when(charge.getAmortizationDays()).thenReturn(days);
        when(charge.getAmortizationCalculationType()).thenReturn(calculationType);
        when(charge.getAmortizationIncomeType()).thenReturn(incomeType);
        return charge;
    }
}
