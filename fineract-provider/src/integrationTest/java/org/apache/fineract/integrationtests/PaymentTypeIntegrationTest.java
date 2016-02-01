/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.PaymentTypeDomain;
import org.mifosplatform.integrationtests.common.PaymentTypeHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class PaymentTypeIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testPaymentType() {
        String name = PaymentTypeHelper.randomNameGenerator("P_T", 5);
        String description = PaymentTypeHelper.randomNameGenerator("PT_Desc", 15);
        Boolean isCashPayment = true;
        Integer position = 1;

        Integer paymentTypeId = PaymentTypeHelper.createPaymentType(requestSpec, responseSpec, name, description, isCashPayment, position);
        Assert.assertNotNull(paymentTypeId);
        PaymentTypeHelper.verifyPaymentTypeCreatedOnServer(requestSpec, responseSpec, paymentTypeId);
        PaymentTypeDomain paymentTypeResponse = PaymentTypeHelper.retrieveById(requestSpec, responseSpec, paymentTypeId);
        Assert.assertEquals(name, paymentTypeResponse.getName());
        Assert.assertEquals(description, paymentTypeResponse.getDescription());
        Assert.assertEquals(isCashPayment, paymentTypeResponse.getIsCashPayment());
        Assert.assertEquals(position, paymentTypeResponse.getPosition());

        // Update Payment Type
        String newName = PaymentTypeHelper.randomNameGenerator("P_TU", 5);
        String newDescription = PaymentTypeHelper.randomNameGenerator("PTU_Desc", 15);
        Boolean isCashPaymentUpdatedValue = false;
        Integer newPosition = 2;

        HashMap request = new HashMap();
        request.put("name", newName);
        request.put("description", newDescription);
        request.put("isCashPayment", isCashPaymentUpdatedValue);
        request.put("position", newPosition);
        PaymentTypeHelper.updatePaymentType(paymentTypeId, request, requestSpec, responseSpec);
        PaymentTypeDomain paymentTypeUpdatedResponse = PaymentTypeHelper.retrieveById(requestSpec, responseSpec, paymentTypeId);
        Assert.assertEquals(newName, paymentTypeUpdatedResponse.getName());
        Assert.assertEquals(newDescription, paymentTypeUpdatedResponse.getDescription());
        Assert.assertEquals(isCashPaymentUpdatedValue, paymentTypeUpdatedResponse.getIsCashPayment());
        Assert.assertEquals(newPosition, paymentTypeUpdatedResponse.getPosition());

        // Delete
        Integer deletedPaymentTypeId = PaymentTypeHelper.deletePaymentType(paymentTypeId, requestSpec, responseSpec);
        Assert.assertEquals(paymentTypeId, deletedPaymentTypeId);
        ResponseSpecification responseSpecification = new ResponseSpecBuilder().expectStatusCode(404).build();
        PaymentTypeHelper.retrieveById(requestSpec, responseSpecification, paymentTypeId);

    }

}
