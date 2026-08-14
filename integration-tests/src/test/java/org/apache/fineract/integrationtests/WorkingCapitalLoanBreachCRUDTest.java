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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachData;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachTemplateResponse;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalLoanBreachHelper;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanBreachCRUDTest {

    private final WorkingCapitalLoanBreachHelper breachHelper = new WorkingCapitalLoanBreachHelper();

    @Test
    public void testTemplateEndpoint() {
        final WorkingCapitalLoanBreachTemplateResponse template = breachHelper.retrieveTemplate();
        assertNotNull(template.getBreachFrequencyTypeOptions());
        assertNotNull(template.getBreachAmountCalculationTypeOptions());
        assertFalse(template.getBreachFrequencyTypeOptions().isEmpty());
        assertFalse(template.getBreachAmountCalculationTypeOptions().isEmpty());
    }

    @Test
    public void testCreateRetrieveUpdateDeleteAndListEndpoints() {
        final WorkingCapitalLoanBreachRequest createBody = breachHelper.createBreachRequest("Default WCL Breach", 15, "DAYS", "PERCENTAGE",
                BigDecimal.valueOf(7.5));
        final Long breachId = breachHelper.create(createBody);
        assertNotNull(breachId);

        final WorkingCapitalLoanBreachData created = breachHelper.retrieveOne(breachId);
        assertEquals("Default WCL Breach", created.getName());
        assertEquals(15, created.getBreachFrequency());
        assert created.getBreachFrequencyType() != null;
        assertEquals("DAYS", created.getBreachFrequencyType().getId());
        assert created.getBreachAmountCalculationType() != null;
        assertEquals("PERCENTAGE", created.getBreachAmountCalculationType().getId());
        assertEquals(0, BigDecimal.valueOf(7.5).compareTo(created.getBreachAmount()));

        final List<WorkingCapitalLoanBreachData> all = breachHelper.retrieveAll();
        boolean found = false;
        for (WorkingCapitalLoanBreachData workingCapitalBreachData : all) {
            if (Objects.equals(workingCapitalBreachData.getId(), breachId)) {
                assertEquals("Default WCL Breach", workingCapitalBreachData.getName());
                found = true;
                break;
            }
        }
        assertTrue(found);

        final WorkingCapitalLoanBreachRequest updateBody = breachHelper.createBreachRequest("Updated WCL Breach", 20, "MONTHS", "FLAT",
                BigDecimal.valueOf(111));
        final Long updatedId = breachHelper.update(breachId, updateBody);
        assertEquals(breachId, updatedId);

        final WorkingCapitalLoanBreachData updated = breachHelper.retrieveOne(breachId);
        assertEquals("Updated WCL Breach", updated.getName());
        assertEquals(20, updated.getBreachFrequency());
        assert updated.getBreachFrequencyType() != null;
        assertEquals("MONTHS", updated.getBreachFrequencyType().getId());
        assert updated.getBreachAmountCalculationType() != null;
        assertEquals("FLAT", updated.getBreachAmountCalculationType().getId());
        assertEquals(0, BigDecimal.valueOf(111).compareTo(updated.getBreachAmount()));

        final Long deletedId = breachHelper.delete(breachId);
        assertEquals(breachId, deletedId);
    }
}
