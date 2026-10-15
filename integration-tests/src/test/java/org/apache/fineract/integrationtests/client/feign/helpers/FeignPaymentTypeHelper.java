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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PaymentTypeCreateResponse;
import org.apache.fineract.client.models.PaymentTypeData;
import org.apache.fineract.client.models.PaymentTypeDeleteResponse;
import org.apache.fineract.client.models.PaymentTypeUpdateRequest;
import org.apache.fineract.client.models.PaymentTypeUpdateResponse;

public class FeignPaymentTypeHelper {

    private final FineractFeignClient fineractClient;

    public FeignPaymentTypeHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PaymentTypeCreateResponse createPaymentType(PaymentTypeCreateRequest request) {
        return ok(() -> fineractClient.paymentType().createPaymentType(request));
    }

    public PaymentTypeData retrievePaymentType(Long paymentTypeId) {
        return ok(() -> fineractClient.paymentType().retrieveOnePaymentType(paymentTypeId));
    }

    public CallFailedRuntimeException retrievePaymentTypeExpectingError(Long paymentTypeId) {
        return fail(() -> fineractClient.paymentType().retrieveOnePaymentType(paymentTypeId));
    }

    public PaymentTypeUpdateResponse updatePaymentType(Long paymentTypeId, PaymentTypeUpdateRequest request) {
        return ok(() -> fineractClient.paymentType().updatePaymentType(paymentTypeId, request));
    }

    public PaymentTypeDeleteResponse deletePaymentType(Long paymentTypeId) {
        return ok(() -> fineractClient.paymentType().deleteCodePaymentType(paymentTypeId));
    }
}
