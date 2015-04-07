/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PaymentTypeHelper {

    private static final String CREATE_PAYMENTTYPE_URL = "/mifosng-provider/api/v1/paymenttypes?" + Utils.TENANT_IDENTIFIER;
    private static final String PAYMENTTYPE_URL = "/mifosng-provider/api/v1/paymenttypes";

    public static Integer createPaymentType(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String name, final String description, final Boolean isCashPayment, final Integer position) {
        System.out.println("---------------------------------CREATING A PAYMENT TYPE---------------------------------------------");
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

        System.out.println("------------------------CREATING PAYMENT TYPE-------------------------" + hm);
        return new Gson().toJson(hm);
    }

    public static void verifyPaymentTypeCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedPaymentTypeID) {
        System.out.println("------------------------------CHECK PAYMENT DETAILS------------------------------------\n");
        final String GET_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + generatedPaymentTypeID + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responsePaymentTypeID = Utils.performServerGet(requestSpec, responseSpec, GET_PAYMENTTYPE_URL, "id");
        assertEquals("ERROR IN CREATING THE PAYMENT TYPE", generatedPaymentTypeID, responsePaymentTypeID);
    }

    public static PaymentTypeDomain retrieveById(RequestSpecification requestSpec, ResponseSpecification responseSpec,
            final Integer paymentTypeId) {
        final String GET_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + paymentTypeId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------GET PAYMENT TYPE---------------------------------------------");
        final String jsonData = new Gson().toJson(Utils.performServerGet(requestSpec, responseSpec, GET_PAYMENTTYPE_URL, ""));
        return new Gson().fromJson(jsonData, new TypeToken<PaymentTypeDomain>() {}.getType());

    }

    public static HashMap<String, String> updatePaymentType(final int id, HashMap request, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String UPDATE_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + id + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE PAYMENT TYPE " + id + "---------------------------------------------");
        HashMap<String, String> hash = Utils.performServerPut(requestSpec, responseSpec, UPDATE_PAYMENTTYPE_URL,
                new Gson().toJson(request), "changes");
        return hash;
    }

    public static Integer deletePaymentType(final int id, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String DELETE_PAYMENTTYPE_URL = PAYMENTTYPE_URL + "/" + id + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------DELETING PAYMENT TYPE " + id + "--------------------------------------------");
        return Utils.performServerDelete(requestSpec, responseSpec, DELETE_PAYMENTTYPE_URL, "resourceId");
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix);
    }

}
