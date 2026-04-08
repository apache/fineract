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
package org.apache.fineract.portfolio.loanaccount.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class LoanRepaymentScheduleInstallmentTest {

    private static final int SCALE = 6;
    private static final int PRECISION = 19;
    private static final MockedStatic<MoneyHelper> MONEY_HELPER = mockStatic(MoneyHelper.class);
    private static final MathContext MATH_CONTEXT = new MathContext(PRECISION, RoundingMode.HALF_EVEN);

    private LoanRepaymentScheduleInstallment installment;

    @BeforeAll
    static void init() {
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(MATH_CONTEXT);
    }

    @BeforeEach
    void setUp() {
        final Loan loan = mock(Loan.class);
        installment = new LoanRepaymentScheduleInstallment(loan, 1, LocalDate.now(ZoneId.systemDefault()),
                LocalDate.now(ZoneId.systemDefault()).plusMonths(1), BigDecimal.valueOf(1000), BigDecimal.valueOf(100),
                BigDecimal.valueOf(50), BigDecimal.valueOf(25), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false,
                false, false);
    }

    @AfterAll
    static void tearDown() {
        MONEY_HELPER.close();
    }

    @Test
    void testPrincipalSetters() {
        testBigDecimalSetter(installment::setPrincipal, installment::getPrincipal);
    }

    @Test
    void testPrincipalCompletedSetters() {
        testBigDecimalSetter(installment::setPrincipalCompleted, installment::getPrincipalCompleted);
    }

    @Test
    void testPrincipalWrittenOffSetters() {
        testBigDecimalSetter(installment::setPrincipalWrittenOff, installment::getPrincipalWrittenOff);
    }

    @Test
    void testInterestChargedSetters() {
        testBigDecimalSetter(installment::setInterestCharged, installment::getInterestCharged);
    }

    @Test
    void testInterestPaidSetters() {
        testBigDecimalSetter(installment::setInterestPaid, installment::getInterestPaid);
    }

    @Test
    void testInterestWaivedSetters() {
        testBigDecimalSetter(installment::setInterestWaived, installment::getInterestWaived);
    }

    @Test
    void testInterestWrittenOffSetters() {
        testBigDecimalSetter(installment::setInterestWrittenOff, installment::getInterestWrittenOff);
    }

    @Test
    void testInterestAccruedSetters() {
        testBigDecimalSetter(installment::setInterestAccrued, installment::getInterestAccrued);
    }

    @Test
    void testRescheduleInterestPortionSetters() {
        testBigDecimalSetter(installment::setRescheduleInterestPortion, installment::getRescheduleInterestPortion);
    }

    @Test
    void testFeeChargesChargedSetters() {
        testBigDecimalSetter(installment::setFeeChargesCharged, installment::getFeeChargesCharged);
    }

    @Test
    void testFeeChargesPaidSetters() {
        testBigDecimalSetter(installment::setFeeChargesPaid, installment::getFeeChargesPaid);
    }

    @Test
    void testFeeChargesWrittenOffSetters() {
        testBigDecimalSetter(installment::setFeeChargesWrittenOff, installment::getFeeChargesWrittenOff);
    }

    @Test
    void testFeeChargesWaivedSetters() {
        testBigDecimalSetter(installment::setFeeChargesWaived, installment::getFeeChargesWaived);
    }

    @Test
    void testFeeAccruedSetters() {
        testBigDecimalSetter(installment::setFeeAccrued, installment::getFeeAccrued);
    }

    @Test
    void testPenaltyChargesSetters() {
        testBigDecimalSetter(installment::setPenaltyCharges, installment::getPenaltyCharges);
    }

    @Test
    void testPenaltyChargesPaidSetters() {
        testBigDecimalSetter(installment::setPenaltyChargesPaid, installment::getPenaltyChargesPaid);
    }

    @Test
    void testPenaltyChargesWrittenOffSetters() {
        testBigDecimalSetter(installment::setPenaltyChargesWrittenOff, installment::getPenaltyChargesWrittenOff);
    }

    @Test
    void testPenaltyChargesWaivedSetters() {
        testBigDecimalSetter(installment::setPenaltyChargesWaived, installment::getPenaltyChargesWaived);
    }

    @Test
    void testPenaltyAccruedSetters() {
        testBigDecimalSetter(installment::setPenaltyAccrued, installment::getPenaltyAccrued);
    }

    @Test
    void testTotalPaidInAdvanceSetters() {
        testBigDecimalSetter(installment::setTotalPaidInAdvance, installment::getTotalPaidInAdvance);
    }

    @Test
    void testTotalPaidLateSetters() {
        testBigDecimalSetter(installment::setTotalPaidLate, installment::getTotalPaidLate);
    }

    @Test
    void testCreditedAmountsSetters() {
        testBigDecimalSetter(installment::setCreditedPrincipal, installment::getCreditedPrincipal);
        testBigDecimalSetter(installment::setCreditedInterest, installment::getCreditedInterest);
        testBigDecimalSetter(installment::setCreditedFee, installment::getCreditedFee);
        testBigDecimalSetter(installment::setCreditedPenalty, installment::getCreditedPenalty);
    }

    @Test
    void testPrecisionAndScale() {
        final BigDecimal value = new BigDecimal("123456789.123456789");

        // Test that value is properly scaled
        installment.setPrincipal(value);
        assertEquals(SCALE, installment.getPrincipal().scale());

        // Test that value is properly rounded
        final BigDecimal expected = new BigDecimal("123456789.123457");
        assertEquals(expected, installment.getPrincipal());
    }

    private void testBigDecimalSetter(final Consumer<BigDecimal> setter, final Supplier<BigDecimal> getter) {
        // Test non-zero value
        final BigDecimal value = new BigDecimal("123.456789");
        setter.accept(value);
        assertEquals(value.setScale(SCALE, RoundingMode.HALF_EVEN), getter.get());

        // Test zero value
        setter.accept(BigDecimal.ZERO);
        assertNull(getter.get());

        // Test null value
        setter.accept(null);
        assertNull(getter.get());
    }
}
