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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel.RateChangeSolve;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectedAmortizationScheduleRateChangeSolveTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, null);

    private static final BigDecimal DISCOUNT_FEE = new BigDecimal("1000");
    private static final BigDecimal NET_DISBURSEMENT = new BigDecimal("9000");
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final BigDecimal RATE = new BigDecimal("18");
    private static final BigDecimal NEW_RATE = new BigDecimal("17");
    private static final int DAY_COUNT = 360;
    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2019, 1, 1);
    private static final LocalDate RATE_CHANGE_DATE = LocalDate.of(2019, 1, 25);

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

    private ProjectedAmortizationScheduleModel modelWithRateChange() {
        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(DISCOUNT_FEE, NET_DISBURSEMENT, TPV,
                RATE, DAY_COUNT, DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE);
        model.applyRateChange(NEW_RATE, RATE_CHANGE_DATE, RATE_CHANGE_DATE);
        return model;
    }

    @Test
    void rateChangeSolveOn_effectiveDateOfTheChange_returnsWhatItWasSolvedTo() {
        final ProjectedAmortizationScheduleModel model = modelWithRateChange();

        final RateChangeSolve solve = model.rateChangeSolveOn(RATE_CHANGE_DATE);

        assertNotNull(solve);
        assertEquals(RATE_CHANGE_DATE, solve.effectiveDate());
        // (100000 x 17) / 360 / 100
        assertEquals(0, new BigDecimal("47.22").compareTo(solve.dailyPayment().getAmount()));
        assertTrue(solve.term() > 1);
        assertNotNull(solve.eir());
    }

    @Test
    void rateChangeSolveOn_dayTheChangeCoversButDidNotStartOn_returnsNullRatherThanTheNeighbour() {
        final ProjectedAmortizationScheduleModel model = modelWithRateChange();
        final RateChangeSolve solve = model.rateChangeSolveOn(RATE_CHANGE_DATE);
        final LocalDate coveredDay = RATE_CHANGE_DATE.plusDays(1);

        assertTrue(solve.term() > 1);

        assertNull(model.rateChangeSolveOn(coveredDay));
    }

    @Test
    void rateChangeSolveOn_changeDatedPastTheScheduleEnd_isRecordedOnTheLastScheduledDay() {
        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(DISCOUNT_FEE, NET_DISBURSEMENT, TPV,
                RATE, DAY_COUNT, DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE);
        final LocalDate lastScheduledDay = model.scheduledMaturityDate();
        final LocalDate pastTheEnd = lastScheduledDay.plusDays(30);
        // A higher rate bills more per day than the closing remainder, so the change re-prices the last day alone
        model.applyRateChange(new BigDecimal("20"), pastTheEnd, DISBURSEMENT_DATE);

        final RateChangeSolve solve = model.rateChangeSolveOn(lastScheduledDay);

        assertNotNull(solve);
        assertEquals(lastScheduledDay, solve.effectiveDate());
        assertEquals(1, solve.term());
        assertEquals(0, new BigDecimal("55.56").compareTo(solve.dailyPayment().getAmount()));
        assertNull(model.rateChangeSolveOn(pastTheEnd));
    }

    @Test
    void rateChangeSolveOn_scheduleWithNoRateChange_returnsNull() {
        final ProjectedAmortizationScheduleModel model = ProjectedAmortizationScheduleModel.generate(DISCOUNT_FEE, NET_DISBURSEMENT, TPV,
                RATE, DAY_COUNT, DISBURSEMENT_DATE, MC, CURRENCY, DISBURSEMENT_DATE);

        assertNull(model.rateChangeSolveOn(RATE_CHANGE_DATE));
    }
}
