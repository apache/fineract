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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The as-of reading of the schedule: what the amortization had earned by the end of a given day, measured against the
 * discount that was in force on that day. It is what a replayed COB day compares itself against, so it has to answer
 * for the day it is asked about and not for the schedule's latest state.
 */
class ProjectedAmortizationScheduleAsOfTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, null);

    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal REDUCED_DISCOUNT_FEE = new BigDecimal("600");
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final BigDecimal RATE = new BigDecimal("18");
    private static final BigDecimal NEW_RATE = new BigDecimal("12");
    private static final int DAY_COUNT = 360;

    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIRST_PAYMENT_DATE = LocalDate.of(2026, 1, 3);
    private static final LocalDate AS_OF_DATE = LocalDate.of(2026, 1, 5);
    private static final LocalDate LATER_PAYMENT_DATE = LocalDate.of(2026, 1, 8);
    private static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("4000");

    @BeforeEach
    void setBusinessDate() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, DISBURSEMENT_DATE)));
    }

    @AfterEach
    void resetContext() {
        ThreadLocalContextUtil.reset();
    }

    private ProjectedAmortizationScheduleModel model(final BigDecimal discountFee) {
        return ProjectedAmortizationScheduleModel.generateEir(discountFee, NET_DISBURSEMENT, TPV, RATE, DAY_COUNT, DISBURSEMENT_DATE, MC,
                CURRENCY, DISBURSEMENT_DATE);
    }

    // --- fast path: the discount asked about is the one the live schedule carries ---

    @Test
    void asOfTheDiscountInForce_countsOnlyThePaymentsUpToThatDay() {
        final ProjectedAmortizationScheduleModel model = model(DISCOUNT_FEE);
        model.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        model.applyPayment(LATER_PAYMENT_DATE, PAYMENT_AMOUNT);

        final ProjectedAmortizationScheduleModel withoutTheLaterPayment = model(DISCOUNT_FEE);
        withoutTheLaterPayment.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        withoutTheLaterPayment.acknowledgeElapsedPeriods(AS_OF_DATE);

        assertEquals(0,
                model.totalActualAmortizationAsOf(DISCOUNT_FEE, AS_OF_DATE).compareTo(withoutTheLaterPayment.totalActualAmortization()));
        assertTrue(model.totalActualAmortizationAsOf(DISCOUNT_FEE, AS_OF_DATE).compareTo(model.totalActualAmortization()) < 0);
    }

    @Test
    void asOfADayAfterEveryPayment_isTheWholeSchedule() {
        final ProjectedAmortizationScheduleModel model = model(DISCOUNT_FEE);
        model.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        model.applyPayment(LATER_PAYMENT_DATE, PAYMENT_AMOUNT);

        assertEquals(0, model.totalActualAmortizationAsOf(DISCOUNT_FEE, LATER_PAYMENT_DATE).compareTo(model.totalActualAmortization()));
    }

    // --- rebuild path: an adjustment landed after the day being asked about ---

    @Test
    void asOfADiscountTheScheduleNoLongerCarries_rebuildsAtThatDiscount() {
        // The live schedule has been restated down to 600 by an adjustment dated after AS_OF_DATE, so every one of its
        // rows - including the ones before the adjustment - now reports what it would have earned under 600.
        final ProjectedAmortizationScheduleModel restated = model(REDUCED_DISCOUNT_FEE);
        restated.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        restated.applyPayment(LATER_PAYMENT_DATE, PAYMENT_AMOUNT);

        final ProjectedAmortizationScheduleModel asItStoodThen = model(DISCOUNT_FEE);
        asItStoodThen.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        asItStoodThen.acknowledgeElapsedPeriods(AS_OF_DATE);

        assertEquals(0, restated.totalActualAmortizationAsOf(DISCOUNT_FEE, AS_OF_DATE).compareTo(asItStoodThen.totalActualAmortization()));
        // Filtering the restated rows would have answered the wrong question; the rebuild is what makes them differ.
        assertNotEquals(0, restated.totalActualAmortizationAsOf(DISCOUNT_FEE, AS_OF_DATE)
                .compareTo(restated.totalActualAmortizationAsOf(REDUCED_DISCOUNT_FEE, AS_OF_DATE)));
    }

    @Test
    void rebuildingAsOf_keepsARateChangeThatWasAlreadyInForce() {
        final ProjectedAmortizationScheduleModel restated = model(REDUCED_DISCOUNT_FEE);
        restated.applyRateChange(NEW_RATE, FIRST_PAYMENT_DATE, FIRST_PAYMENT_DATE);
        restated.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        restated.applyPayment(LATER_PAYMENT_DATE, PAYMENT_AMOUNT);

        final ProjectedAmortizationScheduleModel asItStoodThen = model(DISCOUNT_FEE);
        asItStoodThen.applyRateChange(NEW_RATE, FIRST_PAYMENT_DATE, FIRST_PAYMENT_DATE);
        asItStoodThen.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        asItStoodThen.acknowledgeElapsedPeriods(AS_OF_DATE);

        final ProjectedAmortizationScheduleModel neverRerated = model(DISCOUNT_FEE);
        neverRerated.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        neverRerated.acknowledgeElapsedPeriods(AS_OF_DATE);

        assertEquals(0, restated.totalActualAmortizationAsOf(DISCOUNT_FEE, AS_OF_DATE).compareTo(asItStoodThen.totalActualAmortization()));
        // A rebuild that dropped the rate change would have amortized the re-rated loan at its original rate.
        assertNotEquals(0, asItStoodThen.totalActualAmortization().compareTo(neverRerated.totalActualAmortization()));
    }

    @Test
    void rebuildingAsOf_leavesOutARateChangeThatWasNotInForceYet() {
        final ProjectedAmortizationScheduleModel restated = model(REDUCED_DISCOUNT_FEE);
        restated.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        restated.applyRateChange(NEW_RATE, LATER_PAYMENT_DATE, LATER_PAYMENT_DATE);
        restated.applyPayment(LATER_PAYMENT_DATE, PAYMENT_AMOUNT);

        final ProjectedAmortizationScheduleModel neverRerated = model(REDUCED_DISCOUNT_FEE);
        neverRerated.applyPayment(FIRST_PAYMENT_DATE, PAYMENT_AMOUNT);
        neverRerated.applyPayment(LATER_PAYMENT_DATE, PAYMENT_AMOUNT);

        assertEquals(0, restated.totalActualAmortizationAsOf(DISCOUNT_FEE, AS_OF_DATE)
                .compareTo(neverRerated.totalActualAmortizationAsOf(DISCOUNT_FEE, AS_OF_DATE)));
    }
}
