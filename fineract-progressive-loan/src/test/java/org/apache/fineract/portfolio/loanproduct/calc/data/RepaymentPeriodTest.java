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

class RepaymentPeriodTest {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final CurrencyData USD = new CurrencyData("USD", "US Dollar", 2, 1, "$", "USD");
    private static final Money ZERO = Money.of(USD, BigDecimal.ZERO, MC);

    private static MockedStatic<MoneyHelper> moneyHelper;
    private static LoanProductMinimumRepaymentScheduleRelatedDetail loanProductRelatedDetail;

    @BeforeAll
    static void init() {
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(MC);

        loanProductRelatedDetail = mock(LoanProductMinimumRepaymentScheduleRelatedDetail.class);
        when(loanProductRelatedDetail.getCurrencyData()).thenReturn(USD);
        when(loanProductRelatedDetail.getInterestMethod()).thenReturn(InterestMethod.DECLINING_BALANCE);
        when(loanProductRelatedDetail.getInterestCalculationPeriodMethod()).thenReturn(InterestCalculationPeriodMethod.DAILY);
        when(loanProductRelatedDetail.isAllowPartialPeriodInterestCalculation()).thenReturn(true);
    }

    @AfterAll
    static void tearDown() {
        moneyHelper.close();
    }

    @Test
    void testGettersNeverReturnNull() {
        // Create a minimal RepaymentPeriod
        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        Money emi = ZERO;
        RepaymentPeriod period = RepaymentPeriod.create(null, now, now.plusMonths(1), emi, MC, loanProductRelatedDetail);

        // Test all Money getters
        assertNotNull(period.getEmi());
        assertNotNull(period.getOriginalEmi());
        assertNotNull(period.getPaidPrincipal());
        assertNotNull(period.getPaidInterest());
        assertNotNull(period.getFutureUnrecognizedInterest());

        // Test BigDecimal getters
        assertNotNull(period.getTotalDisbursedAmount());
        assertNotNull(period.getTotalCapitalizedIncomeAmount());
    }

    @Test
    void testMethodsDoNotThrowNPE() {
        LocalDate now = LocalDate.now(ZoneId.of("UTC"));
        Money emi = ZERO;
        RepaymentPeriod period = RepaymentPeriod.create(null, now, now.plusMonths(1), emi, MC, loanProductRelatedDetail);

        // Add an interest period
        InterestPeriod interestPeriod = InterestPeriod.withEmptyAmounts(period, now, now.plusMonths(1));
        period.getInterestPeriods().add(interestPeriod);

        // Test methods that perform calculations
        assertDoesNotThrow(period::getCalculatedDueInterest);
        assertDoesNotThrow(period::getDueInterest);
        assertDoesNotThrow(period::getCalculatedDuePrincipal);
        assertDoesNotThrow(period::getDuePrincipal);
        assertDoesNotThrow(period::getTotalCreditedAmount);
        assertDoesNotThrow(period::getTotalPaidAmount);
        assertDoesNotThrow(period::getUnrecognizedInterest);
        assertDoesNotThrow(period::getCreditedAmounts);
        assertDoesNotThrow(period::getOutstandingLoanBalance);
        assertDoesNotThrow(period::getInitialBalanceForEmiRecalculation);
    }

    @Test
    void testEmptyRepaymentPeriod() {
        // Create an empty repayment period with all null fields
        RepaymentPeriod period = new RepaymentPeriod(null, // previous
                null, // fromDate
                null, // dueDate
                null, // interestPeriods
                null, // emi
                null, // originalEmi
                null, // paidPrincipal
                null, // paidInterest
                null, // futureUnrecognizedInterest
                MC, // mc
                loanProductRelatedDetail);

        // Test that getters don't throw and return non-null
        assertDoesNotThrow(period::getEmi);
        assertDoesNotThrow(period::getOriginalEmi);
        assertDoesNotThrow(period::getPaidPrincipal);
        assertDoesNotThrow(period::getPaidInterest);
        assertDoesNotThrow(period::getFutureUnrecognizedInterest);
        assertDoesNotThrow(period::getTotalDisbursedAmount);
        assertDoesNotThrow(period::getTotalCapitalizedIncomeAmount);
    }
}
