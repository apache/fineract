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

package org.apache.fineract.integrationtests.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PaymentTypeCreateResponse;
import org.apache.fineract.client.models.PaymentTypeData;
import org.apache.fineract.client.models.PaymentTypeDeleteResponse;
import org.apache.fineract.client.models.PaymentTypeUpdateRequest;
import org.apache.fineract.client.models.PaymentTypeUpdateResponse;
import org.apache.fineract.client.util.Calls;

@Slf4j
@SuppressWarnings("HideUtilityClassConstructor")
public final class PaymentTypeHelper {

    public PaymentTypeHelper() {}

    public static List<PaymentTypeData> getAllPaymentTypes(final Boolean onlyWithCode) {
        log.info("-------------------------------GETTING ALL PAYMENT TYPES-------------------------------------------");
        return Calls.ok(FineractClientHelper.getFineractClient().paymentTypes.getAllPaymentTypes(onlyWithCode));
    }

    public static PaymentTypeCreateResponse createPaymentType(final PaymentTypeCreateRequest request) {
        log.info("---------------------------------CREATING A PAYMENT TYPE---------------------------------------------");
        return Calls.ok(FineractClientHelper.getFineractClient().paymentTypes.createPaymentType(request));
    }

    public static void verifyPaymentTypeCreatedOnServer(final Long generatedPaymentTypeID) {
        log.info("-------------------------------CHECK PAYMENT DETAILS-------------------------------------------");
        PaymentTypeData response = Calls
                .ok(FineractClientHelper.getFineractClient().paymentTypes.retrieveOnePaymentType(generatedPaymentTypeID));
        assertEquals(generatedPaymentTypeID, response.getId(), "ERROR IN CREATING THE PAYMENT TYPE");
    }

    public static Object retrieveById(RequestSpecification requestSpec, ResponseSpecification responseSpec, final Long paymentTypeId) {
        log.info("-------------------------------GETTING PAYMENT TYPE (COMPATIBILITY)-------------------------------------------");
        return Calls.ok(FineractClientHelper.getFineractClient().paymentTypes.retrieveOnePaymentType(paymentTypeId));
    }

    public static PaymentTypeData retrieveById(final Long paymentTypeId) {
        return Calls.ok(FineractClientHelper.getFineractClient().paymentTypes.retrieveOnePaymentType(paymentTypeId));
    }

    public static PaymentTypeUpdateResponse updatePaymentType(final Long paymentTypeId, PaymentTypeUpdateRequest request) {
        log.info("-------------------------------UPDATING PAYMENT TYPE-------------------------------------------");
        return Calls.ok(FineractClientHelper.getFineractClient().paymentTypes.updatePaymentType(paymentTypeId, request));
    }

    public static PaymentTypeDeleteResponse deletePaymentType(final Long paymentTypeId) {
        log.info("-------------------------------DELETING PAYMENT TYPE-------------------------------------------");
        return Calls.ok(FineractClientHelper.getFineractClient().paymentTypes.deleteCodePaymentType(paymentTypeId));
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix);
    }
}
