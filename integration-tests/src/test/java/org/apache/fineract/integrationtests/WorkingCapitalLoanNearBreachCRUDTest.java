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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachData;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloannearbreach.WorkingCapitalLoanNearBreachHelper;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanNearBreachCRUDTest {

    private final WorkingCapitalLoanNearBreachHelper nearBreachHelper = new WorkingCapitalLoanNearBreachHelper();

    @Test
    public void testCreateRetrieveUpdateDeleteAndListEndpoints() {
        String nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final WorkingCapitalLoanNearBreachRequest request = new WorkingCapitalLoanNearBreachRequest() //
                .nearBreachName(nearBreachName) //
                .nearBreachFrequency(15) //
                .nearBreachFrequencyType("DAYS") //
                .nearBreachThreshold(BigDecimal.valueOf(7.5)); //
        final Long nearBreachId = nearBreachHelper.create(request).getResourceId();
        assertNotNull(nearBreachId);

        final WorkingCapitalLoanNearBreachData created = nearBreachHelper.retrieveOne(nearBreachId);
        assertEquals(nearBreachName, created.getName());
        assertEquals(15, created.getFrequency());
        assertEquals("DAYS", created.getFrequencyType().getId());
        assertEquals(0, BigDecimal.valueOf(7.5).compareTo(created.getThreshold()));

        final boolean found = nearBreachHelper.retrieveAll().stream().anyMatch(nearBreach -> nearBreachId.equals(nearBreach.getId()));
        assertTrue(found);

        nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final WorkingCapitalLoanNearBreachRequest request2 = new WorkingCapitalLoanNearBreachRequest() //
                .nearBreachName(nearBreachName) //
                .nearBreachFrequency(20) //
                .nearBreachFrequencyType("MONTHS") //
                .nearBreachThreshold(BigDecimal.valueOf(80)); //
        final Long updatedId = nearBreachHelper.update(nearBreachId, request2).getResourceId();
        assertEquals(nearBreachId, updatedId);

        final WorkingCapitalLoanNearBreachData updated = nearBreachHelper.retrieveOne(nearBreachId);
        assertEquals(nearBreachName, updated.getName());
        assertEquals(20, updated.getFrequency());
        assertEquals("MONTHS", updated.getFrequencyType().getId());
        assertEquals(0, BigDecimal.valueOf(80).compareTo(updated.getThreshold()));

        final Long deletedId = nearBreachHelper.delete(nearBreachId).getResourceId();
        assertEquals(nearBreachId, deletedId);
    }

    @Test
    public void testNegativeCreateNearBreachWithSameName() {
        // Given
        final String nearBreachName = Utils.randomStringGenerator("NearBreach", 20);
        final WorkingCapitalLoanNearBreachRequest request = new WorkingCapitalLoanNearBreachRequest() //
                .nearBreachName(nearBreachName) //
                .nearBreachFrequency(15) //
                .nearBreachFrequencyType("DAYS") //
                .nearBreachThreshold(BigDecimal.valueOf(7.5)); //
        final Long nearBreachId = nearBreachHelper.create(request).getResourceId();
        assertNotNull(nearBreachId);

        // When
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> nearBreachHelper.create(request));

        // Then
        assertThat(exception.getStatus()).isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).contains("Data integrity issue with resource");
    }
}
