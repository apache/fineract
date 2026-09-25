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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAmountCalculationStrategy;
import org.junit.jupiter.api.Test;

class ProjectedAmortizationSchedulePaymentAmountTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, null);
    private static final MonetaryCurrency USD = new MonetaryCurrency("USD", 2, null);
    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final int DAY_COUNT = 360;
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2019, 1, 1);

    private static ProjectedAmortizationScheduleModel generate(final WorkingCapitalAmortizationType type, final BigDecimal paymentAmount) {
        return ProjectedAmortizationScheduleModel.generateFromPaymentAmount(type, DISCOUNT_FEE, NET_DISBURSEMENT, paymentAmount, DAY_COUNT,
                DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE);
    }

    @Test
    void evenPayment_sizesTheSamePlanAsTheEquivalentTpvProduct() {
        final ProjectedAmortizationScheduleModel model = generate(WorkingCapitalAmortizationType.EIR, new BigDecimal("50"));
        final ProjectedAmortizationScheduleModel tpv = ProjectedAmortizationScheduleModel.generate(WorkingCapitalAmortizationType.EIR,
                DISCOUNT_FEE, NET_DISBURSEMENT, new BigDecimal("100000"), new BigDecimal("18"), DAY_COUNT, DISBURSEMENT_DATE, MC, CURRENCY,
                DISBURSEMENT_DATE);

        assertEquals(200, model.originalPaymentNumber());
        assertEquals(0, new BigDecimal("50.00").compareTo(model.expectedPaymentAmount().getAmount()));
        assertEquals(0, new BigDecimal("50.00").compareTo(model.finalPaymentAmount().getAmount()));
        assertEquals(0, tpv.effectiveInterestRate().compareTo(model.effectiveInterestRate()));
        assertEquals(0, tpv.calculatedAnnualEir().compareTo(model.calculatedAnnualEir()));
        assertEquals(WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT, model.paymentAmountCalculationStrategy());
        assertEquals(0, new BigDecimal("50").compareTo(model.paymentAmount()));
        assertNull(model.periodPaymentRate());
        assertNull(model.annualEir());
        assertNull(model.totalPaymentVolume());
    }

    @Test
    void unevenPayment_closesOnTheRemainder() {
        final ProjectedAmortizationScheduleModel model = generate(WorkingCapitalAmortizationType.EIR, new BigDecimal("47.22"));

        // 10000 / 47.22 = 211.77 -> 212 days; 10000 - 211 x 47.22 = 36.58
        assertEquals(212, model.originalPaymentNumber());
        assertEquals(0, new BigDecimal("47.22").compareTo(model.expectedPaymentAmount().getAmount()));
        assertEquals(0, new BigDecimal("36.58").compareTo(model.finalPaymentAmount().getAmount()));
        final ProjectedPayment last = model.projectedPayments().getLast();
        assertEquals(212, last.paymentNo());
        assertEquals(0, new BigDecimal("36.58").compareTo(last.expectedPaymentAmount().getAmount()));
        assertEquals(0, last.expectedDiscountFeeBalance().getAmount().signum(), "the fee is fully earned on the closing day");
    }

    @Test
    void flatAmortization_hasNoRateAndEarnsTheFeeInProportion() {
        final ProjectedAmortizationScheduleModel model = generate(WorkingCapitalAmortizationType.FLAT, new BigDecimal("50"));

        assertEquals(200, model.originalPaymentNumber());
        assertNull(model.effectiveInterestRate());
        // 1000 / 10000 x 50 = 5.00 earned per day
        assertEquals(0, new BigDecimal("5.00").compareTo(model.projectedPayments().get(1).expectedAmortizationAmount().getAmount()));
    }

    @Test
    void regenerate_keepsThePaymentAmountStrategy() {
        final ProjectedAmortizationScheduleModel model = generate(WorkingCapitalAmortizationType.EIR, new BigDecimal("47.22"));

        final ProjectedAmortizationScheduleModel regenerated = model.regenerate(new BigDecimal("500"), NET_DISBURSEMENT, DISBURSEMENT_DATE,
                DISBURSEMENT_DATE);

        assertEquals(WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT, regenerated.paymentAmountCalculationStrategy());
        assertEquals(0, new BigDecimal("47.22").compareTo(regenerated.expectedPaymentAmount().getAmount()));
        // 9500 / 47.22 = 201.19 -> 202 days
        assertEquals(202, regenerated.originalPaymentNumber());
    }

    @Test
    void subCentInputs_areRejectedByThePreCheckAndByGenerate() {
        final BigDecimal payment = new BigDecimal("50");
        assertTrue(ProjectedAmortizationScheduleModel.isPaymentAmountCalculable(WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE,
                NET_DISBURSEMENT, payment, DAY_COUNT, USD, MC));
        assertFalse(ProjectedAmortizationScheduleModel.isPaymentAmountCalculable(WorkingCapitalAmortizationType.EIR,
                new BigDecimal("0.004"), NET_DISBURSEMENT, payment, DAY_COUNT, USD, MC), "sub-cent discount is nothing to earn");
        assertFalse(ProjectedAmortizationScheduleModel.isPaymentAmountCalculable(WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE,
                new BigDecimal("0.004"), payment, DAY_COUNT, USD, MC), "sub-cent disbursement is nothing to disburse");
        assertThrows(IllegalArgumentException.class,
                () -> ProjectedAmortizationScheduleModel.generateFromPaymentAmount(WorkingCapitalAmortizationType.EIR,
                        new BigDecimal("0.004"), NET_DISBURSEMENT, payment, DAY_COUNT, DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE));
    }

    @Test
    void paymentTooSmallForTheCalculableTerm_isNotCalculable() {
        // 10000 / 0.09 = 111112 days, above the 100000-day cap
        assertFalse(ProjectedAmortizationScheduleModel.isPaymentAmountCalculable(WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE,
                NET_DISBURSEMENT, new BigDecimal("0.09"), DAY_COUNT, USD, MC));
        assertFalse(ProjectedAmortizationScheduleModel.isPaymentAmountCalculable(WorkingCapitalAmortizationType.EIR, DISCOUNT_FEE,
                NET_DISBURSEMENT, BigDecimal.ZERO, DAY_COUNT, USD, MC));
    }

    @Test
    void planCursor_solvesFromTheKnownPayment() {
        final PlanCursor cursor = PlanCursor.forPaymentAmount(WorkingCapitalAmortizationType.EIR, null, NET_DISBURSEMENT, DISCOUNT_FEE,
                new BigDecimal("47.22"), DAY_COUNT, 2, MC);
        final AmortizationParams.Solved expected = AmortizationParams.solveFromKnownPayment(WorkingCapitalAmortizationType.EIR,
                NET_DISBURSEMENT, DISCOUNT_FEE, new BigDecimal("47.22"), MC, DAY_COUNT, 2);

        assertEquals(212, cursor.solved().term());
        assertEquals(0, new BigDecimal("47.22").compareTo(cursor.solved().dailyPayment()));
        assertEquals(0, new BigDecimal("36.58").compareTo(cursor.solved().closingPayment()));
        assertEquals(0, expected.eir().compareTo(cursor.solved().eir()));
        assertEquals(0, expected.calculatedAnnualEir().compareTo(cursor.solved().calculatedAnnualEir()));
    }
}
