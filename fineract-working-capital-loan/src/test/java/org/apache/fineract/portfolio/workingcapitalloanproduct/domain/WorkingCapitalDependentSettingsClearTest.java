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
package org.apache.fineract.portfolio.workingcapitalloanproduct.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import org.junit.jupiter.api.Test;

class WorkingCapitalDependentSettingsClearTest {

    @Test
    void clearDelinquencySettings_onProductDetail_nullsGraceAndStartType() {
        final WorkingCapitalLoanProductRelatedDetail detail = productDetailWithDelinquency(3,
                WorkingCapitalLoanDelinquencyStartType.LOAN_CREATION);
        final Map<String, Object> changes = new HashMap<>();

        detail.clearDelinquencySettings(changes);

        assertNull(detail.getDelinquencyGraceDays());
        assertNull(detail.getDelinquencyStartType());
        assertEquals(2, changes.size());
        assertTrue(changes.containsKey(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName));
        assertTrue(changes.containsKey(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName));
        assertNull(changes.get(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName));
        assertNull(changes.get(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName));
    }

    @Test
    void clearBreachSettings_onProductDetail_nullsGraceAndStartType() {
        final WorkingCapitalLoanProductRelatedDetail detail = productDetailWithBreach(5, WorkingCapitalLoanBreachStartType.LOAN_CREATION);
        final Map<String, Object> changes = new HashMap<>();

        detail.clearBreachSettings(changes);

        assertNull(detail.getBreachGraceDays());
        assertNull(detail.getBreachStartType());
        assertEquals(2, changes.size());
        assertTrue(changes.containsKey(WorkingCapitalLoanProductConstants.breachGraceDaysParamName));
        assertTrue(changes.containsKey(WorkingCapitalLoanProductConstants.breachStartTypeParamName));
    }

    @Test
    void clearDelinquencySettings_onLoanDetails_nullsGraceAndStartType() {
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setDelinquencyGraceDays(7);
        details.setDelinquencyStartType(WorkingCapitalLoanDelinquencyStartType.DISBURSEMENT);
        final Map<String, Object> changes = new HashMap<>();

        details.clearDelinquencySettings(changes);

        assertNull(details.getDelinquencyGraceDays());
        assertNull(details.getDelinquencyStartType());
        assertEquals(2, changes.size());
    }

    @Test
    void clearBreachSettings_onLoanDetails_nullsGraceAndStartType() {
        final WorkingCapitalLoanProductRelatedDetails details = new WorkingCapitalLoanProductRelatedDetails();
        details.setBreachGraceDays(4);
        details.setBreachStartType(WorkingCapitalLoanBreachStartType.DISBURSEMENT);
        final Map<String, Object> changes = new HashMap<>();

        details.clearBreachSettings(changes);

        assertNull(details.getBreachGraceDays());
        assertNull(details.getBreachStartType());
        assertEquals(2, changes.size());
    }

    @Test
    void clearDelinquencySettings_whenAlreadyNull_doesNotAddChanges() {
        final WorkingCapitalLoanProductRelatedDetail detail = productDetailWithDelinquency(null, null);
        final Map<String, Object> changes = new HashMap<>();

        detail.clearDelinquencySettings(changes);

        assertTrue(changes.isEmpty());
    }

    private static WorkingCapitalLoanProductRelatedDetail productDetailWithDelinquency(final Integer graceDays,
            final WorkingCapitalLoanDelinquencyStartType startType) {
        return new WorkingCapitalLoanProductRelatedDetail(WorkingCapitalAmortizationType.EIR,
                WorkingCapitalPaymentAmountCalculationStrategy.TPV, null, null, 360, null, null, 1,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, null, graceDays, startType, null, null);
    }

    private static WorkingCapitalLoanProductRelatedDetail productDetailWithBreach(final Integer graceDays,
            final WorkingCapitalLoanBreachStartType startType) {
        return new WorkingCapitalLoanProductRelatedDetail(WorkingCapitalAmortizationType.EIR,
                WorkingCapitalPaymentAmountCalculationStrategy.TPV, null, null, 360, null, null, 1,
                WorkingCapitalLoanPeriodFrequencyType.DAYS, null, null, null, graceDays, startType);
    }
}
