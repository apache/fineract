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

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PaymentTypeData;
import org.apache.fineract.client.models.PaymentTypeUpdateRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignPaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PaymentTypeIntegrationTest extends FeignIntegrationTest {

    private FeignPaymentTypeHelper paymentTypeHelper;

    @BeforeAll
    public void setup() {
        paymentTypeHelper = new FeignPaymentTypeHelper(fineractClient());
    }

    @Test
    public void testPaymentType() {
        // 1. Setup Data
        String name = Utils.randomStringGenerator("P_T", 5);
        String description = Utils.randomStringGenerator("PT_Desc", 15);
        Boolean isCashPayment = true;
        Long position = 1L;

        // 2. Create Payment Type
        var paymentTypesResponse = paymentTypeHelper.createPaymentType(
                new PaymentTypeCreateRequest().name(name).description(description).isCashPayment(isCashPayment).position(position));

        Long paymentTypeId = paymentTypesResponse.getResourceId();
        Assertions.assertNotNull(paymentTypeId, "Payment Type Resource ID should not be null");

        // 3. Verify Creation
        assertEquals(paymentTypeId, paymentTypeHelper.retrievePaymentType(paymentTypeId).getId(), "ERROR IN CREATING THE PAYMENT TYPE");

        // 4. Retrieve and Assert
        PaymentTypeData paymentTypeResponse = paymentTypeHelper.retrievePaymentType(paymentTypeId);
        Assertions.assertEquals(name, paymentTypeResponse.getName(), "Name mismatch after creation");

        // 5. Update Payment Type
        String newName = Utils.randomStringGenerator("P_TU", 5);
        paymentTypeHelper.updatePaymentType(paymentTypeId,
                new PaymentTypeUpdateRequest().name(newName).description(description).isCashPayment(isCashPayment).position(position));

        // 6. Verify Update
        var paymentTypeUpdatedResponse = paymentTypeHelper.retrievePaymentType(paymentTypeId);
        Assertions.assertEquals(newName, paymentTypeUpdatedResponse.getName(), "Name mismatch after update");

        // 7. Delete Payment Type
        var responseDelete = paymentTypeHelper.deletePaymentType(paymentTypeId);
        Assertions.assertEquals(paymentTypeId, responseDelete.getResourceId(), "Deleted Resource ID mismatch");

        final CallFailedRuntimeException exception = paymentTypeHelper.retrievePaymentTypeExpectingError(paymentTypeId);

        assertEquals(404, exception.getStatus());
    }
}
