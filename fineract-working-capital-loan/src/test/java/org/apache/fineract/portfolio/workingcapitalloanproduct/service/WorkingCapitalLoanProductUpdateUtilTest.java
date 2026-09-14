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
package org.apache.fineract.portfolio.workingcapitalloanproduct.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanBreachStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductMinMaxConstraints;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAmountCalculationStrategy;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanProductUpdateUtilTest {

    private final WorkingCapitalLoanProductUpdateUtil updateUtil = new WorkingCapitalLoanProductUpdateUtil();

    private static WorkingCapitalLoanProductMinMaxConstraints allBounds() {
        return new WorkingCapitalLoanProductMinMaxConstraints(BigDecimal.valueOf(1000), BigDecimal.valueOf(10000), BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(60), BigDecimal.valueOf(40), BigDecimal.valueOf(60));
    }

    @Test
    void switchToTpv_clearsTheAnnualEirAndPaymentAmountBounds() {
        final WorkingCapitalLoanProductMinMaxConstraints bounds = allBounds();

        final Map<String, Object> changes = updateUtil.clearMinMaxIncompatibleWithPaymentStrategy(bounds,
                WorkingCapitalPaymentAmountCalculationStrategy.TPV);

        assertEquals(0, BigDecimal.ONE.compareTo(bounds.getMinPeriodPaymentRate()));
        assertEquals(0, BigDecimal.TEN.compareTo(bounds.getMaxPeriodPaymentRate()));
        assertNull(bounds.getMinAnnualEir());
        assertNull(bounds.getMaxAnnualEir());
        assertNull(bounds.getMinPaymentAmount());
        assertNull(bounds.getMaxPaymentAmount());
        assertEquals(4, changes.size());
        assertTrue(changes.containsKey(WorkingCapitalLoanProductConstants.minPaymentAmountParamName));
        assertTrue(changes.containsKey(WorkingCapitalLoanProductConstants.maxPaymentAmountParamName));
    }

    @Test
    void switchToPaymentAmount_clearsTheRateAndAnnualEirBounds() {
        final WorkingCapitalLoanProductMinMaxConstraints bounds = allBounds();

        final Map<String, Object> changes = updateUtil.clearMinMaxIncompatibleWithPaymentStrategy(bounds,
                WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT);

        assertNull(bounds.getMinPeriodPaymentRate());
        assertNull(bounds.getMaxPeriodPaymentRate());
        assertNull(bounds.getMinAnnualEir());
        assertNull(bounds.getMaxAnnualEir());
        assertEquals(0, BigDecimal.valueOf(40).compareTo(bounds.getMinPaymentAmount()));
        assertEquals(0, BigDecimal.valueOf(60).compareTo(bounds.getMaxPaymentAmount()));
        assertEquals(0, BigDecimal.valueOf(1000).compareTo(bounds.getMinPrincipal()), "principal bounds apply to every strategy");
        assertEquals(4, changes.size());
    }

    @Test
    void switchFromPaymentAmountToTpv_clearsTheStoredPaymentAmount() {
        final WorkingCapitalLoanProductRelatedDetail detail = mock(WorkingCapitalLoanProductRelatedDetail.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));
        detail.setAmortizationType(WorkingCapitalAmortizationType.EIR);
        detail.setRepaymentFrequencyType(WorkingCapitalLoanPeriodFrequencyType.DAYS);
        detail.setDelinquencyStartType(WorkingCapitalLoanDelinquencyStartType.DISBURSEMENT);
        detail.setBreachStartType(WorkingCapitalLoanBreachStartType.LOAN_CREATION);
        detail.setPaymentAmountCalculationStrategy(WorkingCapitalPaymentAmountCalculationStrategy.PAYMENT_AMOUNT);
        detail.setPaymentAmount(BigDecimal.valueOf(47.22));
        detail.setDiscount(BigDecimal.valueOf(1000));

        final JsonObject json = new JsonObject();
        json.addProperty(WorkingCapitalLoanProductConstants.paymentAmountCalculationStrategyParamName, "TPV");
        json.addProperty(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, BigDecimal.ONE);
        final FromJsonHelper jsonHelper = new FromJsonHelper();
        final JsonCommand command = JsonCommand.fromJsonElement(1L, JsonParser.parseString(json.toString()), jsonHelper);

        final Map<String, Object> changes = updateUtil.updateRelatedDetail(detail, command);

        assertEquals(WorkingCapitalPaymentAmountCalculationStrategy.TPV, detail.getPaymentAmountCalculationStrategy());
        assertNull(detail.getPaymentAmount());
        assertEquals(0, BigDecimal.ONE.compareTo(detail.getPeriodPaymentRate()));
        assertTrue(changes.containsKey(WorkingCapitalLoanProductConstants.paymentAmountParamName));
        assertNull(changes.get(WorkingCapitalLoanProductConstants.paymentAmountParamName));
    }
}
