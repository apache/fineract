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
package org.apache.fineract.integrationtests.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PaymentTypeCreateResponse;
import org.apache.fineract.client.models.PaymentTypeData;
import org.apache.fineract.client.models.PaymentTypeDeleteResponse;
import org.apache.fineract.client.models.PaymentTypeUpdateRequest;
import org.apache.fineract.client.models.PaymentTypeUpdateResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

public class PaymentTypeTest extends IntegrationTest {

    @Test
    public void testPaymentTypeCrud() {
        // 1. Setup Data
        String name = Utils.randomStringGenerator("P_T_", 5);
        String description = Utils.randomStringGenerator("PT_Desc_", 15);
        Boolean isCashPayment = true;
        Long position = 1L;

        // 2. Create Payment Type
        PaymentTypeCreateRequest createRequest = new PaymentTypeCreateRequest().name(name).description(description)
                .isCashPayment(isCashPayment).position(position);

        PaymentTypeCreateResponse paymentTypesResponse = ok(fineractClient().paymentTypes.createPaymentType(createRequest));
        Long paymentTypeId = paymentTypesResponse.getResourceId();
        assertNotNull(paymentTypeId, "Payment Type Resource ID should not be null");

        // 3. Retrieve and Assert
        PaymentTypeData paymentTypeResponse = ok(fineractClient().paymentTypes.retrieveOnePaymentType(paymentTypeId));
        assertEquals(paymentTypeId, paymentTypeResponse.getId(), "Payment Type ID mismatch");
        assertEquals(name, paymentTypeResponse.getName(), "Name mismatch after creation");
        assertEquals(description, paymentTypeResponse.getDescription(), "Description mismatch after creation");
        assertEquals(isCashPayment, paymentTypeResponse.getIsCashPayment(), "isCashPayment mismatch after creation");
        assertEquals(position, paymentTypeResponse.getPosition(), "Position mismatch after creation");

        // 4. Update Payment Type
        String newName = Utils.randomStringGenerator("P_TU_", 5);
        String newDescription = Utils.randomStringGenerator("PT_Desc_U_", 15);
        Long newPosition = 2L;
        PaymentTypeUpdateRequest updateRequest = new PaymentTypeUpdateRequest().name(newName).description(newDescription)
                .isCashPayment(isCashPayment).position(newPosition);

        PaymentTypeUpdateResponse updateResponse = ok(fineractClient().paymentTypes.updatePaymentType(paymentTypeId, updateRequest));
        assertNotNull(updateResponse.getResourceId(), "Updated resource ID should not be null");

        // 5. Verify Update
        PaymentTypeData paymentTypeUpdatedResponse = ok(fineractClient().paymentTypes.retrieveOnePaymentType(paymentTypeId));
        assertEquals(newName, paymentTypeUpdatedResponse.getName(), "Name mismatch after update");
        assertEquals(newDescription, paymentTypeUpdatedResponse.getDescription(), "Description mismatch after update");
        assertEquals(newPosition, paymentTypeUpdatedResponse.getPosition(), "Position mismatch after update");

        // 6. Delete Payment Type
        PaymentTypeDeleteResponse responseDelete = ok(fineractClient().paymentTypes.deleteCodePaymentType(paymentTypeId));
        assertEquals(paymentTypeId, responseDelete.getResourceId(), "Deleted Resource ID mismatch");

        // 7. Verify 404 on Deleted Payment Type
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> {
            ok(fineractClient().paymentTypes.retrieveOnePaymentType(paymentTypeId));
        });
        assertEquals(404, exception.getResponse().code());
    }

    @Test
    public void testGetAllPaymentTypes() {
        String name = Utils.randomStringGenerator("P_T_ALL_", 5);
        String description = Utils.randomStringGenerator("PT_Desc_ALL_", 15);

        PaymentTypeCreateRequest createRequest = new PaymentTypeCreateRequest().name(name).description(description).isCashPayment(false)
                .position(10L);

        PaymentTypeCreateResponse paymentTypesResponse = ok(fineractClient().paymentTypes.createPaymentType(createRequest));
        Long paymentTypeId = paymentTypesResponse.getResourceId();
        assertNotNull(paymentTypeId);

        List<PaymentTypeData> paymentTypes = ok(fineractClient().paymentTypes.getAllPaymentTypes(false));
        assertNotNull(paymentTypes);
        assertTrue(paymentTypes.stream().anyMatch(pt -> paymentTypeId.equals(pt.getId())));

        // Clean up
        ok(fineractClient().paymentTypes.deleteCodePaymentType(paymentTypeId));
    }
}
