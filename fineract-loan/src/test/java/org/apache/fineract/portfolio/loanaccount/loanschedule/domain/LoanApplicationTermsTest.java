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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
import static org.apache.fineract.organisation.monetary.domain.MonetaryCurrency.fromApplicationCurrency;
import static org.apache.fineract.organisation.workingdays.domain.RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY;
import static org.apache.fineract.portfolio.common.domain.DayOfWeekType.INVALID;
import static org.apache.fineract.portfolio.common.domain.PeriodFrequencyType.MONTHS;
import static org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.CUMULATIVE;
import static org.apache.fineract.portfolio.loanproduct.domain.AmortizationMethod.EQUAL_PRINCIPAL;
import static org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
import static org.apache.fineract.portfolio.loanproduct.domain.InterestMethod.FLAT;
import static org.apache.fineract.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy.NONE;
import static org.apache.fineract.portfolio.loanproduct.domain.RepaymentStartDateType.DISBURSEMENT_DATE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.common.domain.DaysInMonthType;
import org.apache.fineract.portfolio.common.domain.DaysInYearType;
import org.apache.fineract.portfolio.loanaccount.data.HolidayDetailDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanApplicationTermsTest {

    private static final int NUMBER_OF_REPAYMENTS = 4;
    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_EVEN);

    private ApplicationCurrency currency;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        MoneyHelper.initializeTenantRoundingMode("default", 6);
        currency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void flatLoan_principalVariationOnLastInstallment_principalPortionsSumToLoanPrincipal() {
        LoanApplicationTerms terms = flatLoanTerms();
        Money cumulativePrincipal = zero();

        for (int period = 1; period < NUMBER_OF_REPAYMENTS; period++) {
            Money principal = terms.calculateTotalPrincipalForPeriod(null, terms.getPrincipal(), period, MC, zero());
            cumulativePrincipal = cumulativePrincipal.plus(principal);
        }

        terms.setCurrentPeriodFixedPrincipalAmount(BigDecimal.valueOf(30));
        Money lastPeriodPrincipal = terms.calculateTotalPrincipalForPeriod(null, terms.getPrincipal(), NUMBER_OF_REPAYMENTS, MC, zero());
        assertThat(lastPeriodPrincipal.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(30));

        cumulativePrincipal = cumulativePrincipal.plus(lastPeriodPrincipal);
        Money adjustedLastPeriodPrincipal = terms.adjustPrincipalIfLastRepaymentPeriod(lastPeriodPrincipal, cumulativePrincipal,
                NUMBER_OF_REPAYMENTS);

        Money totalPrincipal = cumulativePrincipal.minus(lastPeriodPrincipal).plus(adjustedLastPeriodPrincipal);
        assertThat(totalPrincipal.getAmount()).isEqualByComparingTo(terms.getPrincipal().getAmount());
    }

    @Test
    void flatLoan_principalVariationOnMiddleInstallment_spreadsAdjustment() {
        LoanApplicationTerms terms = flatLoanTerms();
        terms.setCurrentPeriodFixedPrincipalAmount(BigDecimal.valueOf(40));

        Money fixedPeriod = terms.calculateTotalPrincipalForPeriod(null, terms.getPrincipal(), 2, MC, zero());
        assertThat(fixedPeriod.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(40));

        // (25 - 40) / (4 - 2) = -7.50 is carried into the following installments
        terms.setCurrentPeriodFixedPrincipalAmount(null);
        Money nextPeriod = terms.calculateTotalPrincipalForPeriod(null, terms.getPrincipal(), 3, MC, zero());
        assertThat(nextPeriod.getAmount()).isEqualByComparingTo(new BigDecimal("17.50"));
    }

    private Money zero() {
        return Money.zero(fromApplicationCurrency(currency));
    }

    private LoanApplicationTerms flatLoanTerms() {
        HolidayDetailDTO holidayDetailDTO = new HolidayDetailDTO(false, emptyList(),
                new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(), false, false),
                false, false);
        Money principalAmount = Money.of(fromApplicationCurrency(currency), BigDecimal.valueOf(100));
        LocalDate disbursementDate = LocalDate.of(2024, 1, 1);
        LocalDate firstRepaymentDate = LocalDate.of(2024, 2, 1);
        return LoanApplicationTerms.assembleFrom(currency.toData(), 1, MONTHS, NUMBER_OF_REPAYMENTS, 1, MONTHS, null, INVALID,
                EQUAL_PRINCIPAL, FLAT, ZERO, MONTHS, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount, disbursementDate, null,
                firstRepaymentDate, null, null, null, null, null, Money.of(fromApplicationCurrency(currency), ZERO), false, null,
                emptyList(), BigDecimal.valueOf(36_000L), null, DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null,
                null, null, ZERO, null, NONE, null, ZERO, emptyList(), true, 0, false, holidayDetailDTO, false, false, false, null, false,
                false, null, false, DISBURSEMENT_DATE, disbursementDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, null, false,
                null, null, false, null, false, null, null, null, false, null, null, null, false, false, null);
    }
}
