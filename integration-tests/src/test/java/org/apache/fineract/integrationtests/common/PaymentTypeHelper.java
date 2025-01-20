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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.DeletePaymentTypesPaymentTypeIdResponse;
import org.apache.fineract.client.models.GetPaymentTypesPaymentTypeIdResponse;
import org.apache.fineract.client.models.GetPaymentTypesResponse;
import org.apache.fineract.client.models.PostPaymentTypesRequest;
import org.apache.fineract.client.models.PostPaymentTypesResponse;
import org.apache.fineract.client.models.PutPaymentTypesPaymentTypeIdRequest;
import org.apache.fineract.client.models.PutPaymentTypesPaymentTypeIdResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
public final class PaymentTypeHelper extends IntegrationTest {

    public PaymentTypeHelper() {

    }

    private static final String PAYMENTTYPE_URL = "/fineract-provider/api/v1/paymenttypes";
    private static final String CREATE_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "?" + Utils.TENANT_IDENTIFIER;

    public List<GetPaymentTypesResponse> getAllPaymentTypes(final Boolean onlyWithCode) {
        log.info("-------------------------------GETTING ALL PAYMENT TYPES-------------------------------------------");
        return ok(fineract().paymentTypes.getAllPaymentTypes(onlyWithCode));
    }

    public PostPaymentTypesResponse createPaymentType(final PostPaymentTypesRequest postPaymentTypesRequest) {
        log.info("---------------------------------CREATING A PAYMENT TYPE---------------------------------------------");
        return ok(fineract().paymentTypes.createPaymentType(postPaymentTypesRequest));
    }

    public void verifyPaymentTypeCreatedOnServer(final Long generatedPaymentTypeID) {
        log.info("-------------------------------CHECK PAYMENT DETAILS-------------------------------------------");
        GetPaymentTypesPaymentTypeIdResponse response = ok(fineract().paymentTypes.retrieveOnePaymentType(generatedPaymentTypeID));
        Long responsePaymentTypeID = response.getId();
        assertEquals(generatedPaymentTypeID, responsePaymentTypeID, "ERROR IN CREATING THE PAYMENT TYPE");
    }

    public GetPaymentTypesPaymentTypeIdResponse retrieveById(final Long paymentTypeId) {
        log.info("-------------------------------GETTING PAYMENT TYPE-------------------------------------------");
        return ok(fineract().paymentTypes.retrieveOnePaymentType(paymentTypeId));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public PaymentTypeDomain retrieveById(RequestSpecification requestSpec, ResponseSpecification responseSpec, final Long paymentTypeId) {
        final String GET_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + paymentTypeId + "?" + Utils.TENANT_IDENTIFIER;
        log.info("-------------------------------GETTING PAYMENT TYPE-------------------------------------------");
        Object get = Utils.performServerGet(requestSpec, responseSpec, GET_PAYMENTTYPE_URL, "");
        final String jsonData = new Gson().toJson(get);
        return new Gson().fromJson(jsonData, new TypeToken<PaymentTypeDomain>() {}.getType());
    }

    public PutPaymentTypesPaymentTypeIdResponse updatePaymentType(final Long paymentTypeId,
            PutPaymentTypesPaymentTypeIdRequest putPaymentTypesPaymentTypeIdRequest) {
        log.info("-------------------------------UPDATING PAYMENT TYPE-------------------------------------------");
        return ok(fineract().paymentTypes.updatePaymentType(paymentTypeId, putPaymentTypesPaymentTypeIdRequest));
    }

    public DeletePaymentTypesPaymentTypeIdResponse deletePaymentType(final Long paymentTypeId) {
        log.info("-------------------------------DELETING PAYMENT TYPE-------------------------------------------");
        return ok(fineract().paymentTypes.deleteCode1(paymentTypeId));
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix);
    }
}
