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
package org.apache.fineract.portfolio.workingcapitalloan.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import org.apache.fineract.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.junit.jupiter.api.Test;

class ProjectedAmortizationScheduleMapperTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("EUR", 2, null);
    private static final LocalDate DISBURSEMENT = LocalDate.of(2026, 1, 1);
    private static final BigDecimal DISCOUNT = new BigDecimal("1000");
    private static final BigDecimal NET = new BigDecimal("9000");

    private final ProjectedAmortizationScheduleMapper mapper = new ProjectedAmortizationScheduleMapper();

    @Test
    void paymentAmountSchedule_DisclosesTheStrategyAndItsInput() {
        final ProjectedAmortizationScheduleData data = mapper.toData(ProjectedAmortizationScheduleModel.generateFromPaymentAmount(
                WorkingCapitalAmortizationType.EIR, DISCOUNT, NET, new BigDecimal("47.22"), 360, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT));

        assertEquals("PAYMENT_AMOUNT", data.getPaymentAmountCalculationStrategy().getId());
        assertEquals(0, new BigDecimal("47.22").compareTo(data.getPaymentAmount()));
        assertNull(data.getAnnualEir());
        assertNull(data.getTotalPaymentVolume());
        assertNull(data.getPeriodPaymentRate());
    }

    @Test
    void annualEirSchedule_DisclosesTheStrategyAndItsInput() {
        final ProjectedAmortizationScheduleData data = mapper
                .toData(ProjectedAmortizationScheduleModel.generateFromAnnualEir(WorkingCapitalAmortizationType.EIR, DISCOUNT, NET,
                        new BigDecimal("46.8451"), 360, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT));

        assertEquals("ANNUAL_EIR", data.getPaymentAmountCalculationStrategy().getId());
        assertEquals(0, new BigDecimal("46.8451").compareTo(data.getAnnualEir()));
        assertNull(data.getPaymentAmount());
        assertNull(data.getTotalPaymentVolume());
    }

    @Test
    void tpvSchedule_DisclosesTheStrategy() {
        final ProjectedAmortizationScheduleData data = mapper
                .toData(ProjectedAmortizationScheduleModel.generate(WorkingCapitalAmortizationType.EIR, DISCOUNT, NET,
                        new BigDecimal("100000"), new BigDecimal("18"), 360, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT));

        assertEquals("TPV", data.getPaymentAmountCalculationStrategy().getId());
        assertNull(data.getAnnualEir());
        assertNull(data.getPaymentAmount());
    }
}
