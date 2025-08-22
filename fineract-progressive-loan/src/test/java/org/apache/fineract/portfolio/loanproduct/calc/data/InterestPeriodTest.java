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
package org.apache.fineract.portfolio.loanproduct.calc.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductMinimumRepaymentScheduleRelatedDetail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class InterestPeriodTest {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final CurrencyData USD = new CurrencyData("USD", "US Dollar", 2, 1, "$", "USD");
    private static final Money ZERO = Money.of(USD, BigDecimal.ZERO, MC);

    private static MockedStatic<MoneyHelper> moneyHelper;

    @BeforeAll
    static void init() {
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(MC);
    }

    @AfterAll
    static void tearDown() {
        moneyHelper.close();
    }

    @Test
    void testGettersNeverReturnNull() {
        // Create an empty InterestPeriod with all null Money fields
        RepaymentPeriod repaymentPeriod = createMinimalRepaymentPeriod();
        InterestPeriod period = InterestPeriod.empty(repaymentPeriod, MC);

        // Test all Money getters
        assertNotNull(period.getCreditedPrincipal());
        assertNotNull(period.getCreditedInterest());
        assertNotNull(period.getDisbursementAmount());
        assertNotNull(period.getBalanceCorrectionAmount());
        assertNotNull(period.getOutstandingLoanBalance());
        assertNotNull(period.getCapitalizedIncomePrincipal());

        // Test BigDecimal getters
        assertNotNull(period.getRateFactor());
        assertNotNull(period.getRateFactorTillPeriodDueDate());
    }

    @Test
    void testMethodsDoNotThrowNPE() {
        RepaymentPeriod repaymentPeriod = createMinimalRepaymentPeriod();
        InterestPeriod period = InterestPeriod.empty(repaymentPeriod, MC);
        when(repaymentPeriod.getInterestPeriods()).thenReturn(List.of(period));
        when(repaymentPeriod.getFirstInterestPeriod()).thenReturn(period);
        LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail = mock(
                LoanProductMinimumRepaymentScheduleRelatedDetail.class);
        when(loanProductRelatedDetail.getCurrencyData()).thenReturn(USD);
        when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
        when(loanProductRelatedDetail.getInterestCalculationPeriodMethod()).thenReturn(InterestCalculationPeriodMethod.DAILY);
        when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);
        when(repaymentPeriod.getLoanProductRelatedDetail()).thenReturn(loanProductRelatedDetail);
        // Set some test data
        period.setFromDate(LocalDate.now(ZoneId.of("UTC")));
        period.setDueDate(LocalDate.now(ZoneId.of("UTC")).plusDays(30));
        period.setRateFactor(BigDecimal.valueOf(0.01));
        period.setRateFactorTillPeriodDueDate(BigDecimal.valueOf(0.01));

        // Test methods that perform calculations
        assertDoesNotThrow(period::updateOutstandingLoanBalance);
        assertDoesNotThrow(period::getCreditedAmounts);
        assertDoesNotThrow(() -> period.getCalculatedDueInterest());
        assertDoesNotThrow(() -> period.getCalculatedDueInterest(InterestMethod.DECLINING_BALANCE, 30));
        assertDoesNotThrow(period::getLength);
        assertDoesNotThrow(period::getLengthTillPeriodDueDate);
    }

    @Test
    void testWithNullFields() {
        RepaymentPeriod repaymentPeriod = createMinimalRepaymentPeriod();
        InterestPeriod period = new InterestPeriod(repaymentPeriod, null, // fromDate
                null, // dueDate
                null, // rateFactor
                null, // rateFactorTillPeriodDueDate
                null, // creditedPrincipal
                null, // creditedInterest
                null, // disbursementAmount
                null, // balanceCorrectionAmount
                null, // outstandingLoanBalance
                null, // capitalizedIncomePrincipal
                MC, false // isPaused
        );

        // Test that getters don't throw and return non-null
        assertDoesNotThrow(period::getCreditedPrincipal);
        assertDoesNotThrow(period::getCreditedInterest);
        assertDoesNotThrow(period::getDisbursementAmount);
        assertDoesNotThrow(period::getBalanceCorrectionAmount);
        assertDoesNotThrow(period::getOutstandingLoanBalance);
        assertDoesNotThrow(period::getCapitalizedIncomePrincipal);
    }

    private RepaymentPeriod createMinimalRepaymentPeriod() {
        RepaymentPeriod repaymentPeriod = mock(RepaymentPeriod.class);
        when(repaymentPeriod.getZero()).thenReturn(ZERO);
        when(repaymentPeriod.getDueDate()).thenReturn(LocalDate.now(ZoneId.of("UTC")).plusMonths(1));
        when(repaymentPeriod.getCurrency()).thenReturn(ZERO.getCurrency());
        when(repaymentPeriod.getMc()).thenReturn(MC);
        return repaymentPeriod;
    }
}
