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

import static org.junit.Assert.assertEquals;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PaymentTypeHelper {
    private final static Logger LOG = LoggerFactory.getLogger(PaymentTypeHelper.class);
    private static final String CREATE_PAYMENTTYPE_URL = "/fineract-provider/api/v1/paymenttypes?" + Utils.TENANT_IDENTIFIER;
    private static final String PAYMENTTYPE_URL = "/fineract-provider/api/v1/paymenttypes";

    public static Integer createPaymentType(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String name, final String description, final Boolean isCashPayment, final Integer position) {
        LOG.info("---------------------------------CREATING A PAYMENT TYPE---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_PAYMENTTYPE_URL,
                getJsonToCreatePaymentType(name, description, isCashPayment, position), "resourceId");
    }

    public static String getJsonToCreatePaymentType(final String name, final String description, final Boolean isCashPayment,
            final Integer position) {
        HashMap hm = new HashMap();
        hm.put("name", name);
        if (description != null) hm.put("description", description);
        hm.put("isCashPayment", isCashPayment);
        if (position != null) hm.put("position", position);

        LOG.info("------------------------CREATING PAYMENT TYPE-------------------------" + hm);
        return new Gson().toJson(hm);
    }

    public static void verifyPaymentTypeCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedPaymentTypeID) {
        LOG.info("------------------------------CHECK PAYMENT DETAILS------------------------------------\n");
        final String GET_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + generatedPaymentTypeID + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responsePaymentTypeID = Utils.performServerGet(requestSpec, responseSpec, GET_PAYMENTTYPE_URL, "id");
        assertEquals("ERROR IN CREATING THE PAYMENT TYPE", generatedPaymentTypeID, responsePaymentTypeID);
    }

    public static PaymentTypeDomain retrieveById(RequestSpecification requestSpec, ResponseSpecification responseSpec,
            final Integer paymentTypeId) {
        final String GET_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + paymentTypeId + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------GET PAYMENT TYPE---------------------------------------------");
        Object get = Utils.performServerGet(requestSpec, responseSpec, GET_PAYMENTTYPE_URL, "");
        final String jsonData = new Gson().toJson(get);
        return new Gson().fromJson(jsonData, new TypeToken<PaymentTypeDomain>() {}.getType());

    }

    public static HashMap<String, String> updatePaymentType(final int id, HashMap request, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String UPDATE_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + id + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------UPDATE PAYMENT TYPE " + id + "---------------------------------------------");
        HashMap<String, String> hash = Utils.performServerPut(requestSpec, responseSpec, UPDATE_PAYMENTTYPE_URL,
                new Gson().toJson(request), "changes");
        return hash;
    }

    public static Integer deletePaymentType(final int id, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String DELETE_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + id + "?" + Utils.TENANT_IDENTIFIER;
        LOG.info("---------------------------------DELETING PAYMENT TYPE " + id + "--------------------------------------------");
        return Utils.performServerDelete(requestSpec, responseSpec, DELETE_PAYMENTTYPE_URL, "resourceId");
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix);
    }

}
