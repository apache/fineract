/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.funds.FundsHelper;
import org.mifosplatform.integrationtests.common.funds.FundsResourceHandler;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

/**
 * Funds Integration Test for checking Funds Application.
 */
public class FundsIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().build();
    }

    @Test
    public void testRetrieveFund() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).build();
        String jsonData = fh.toJSON();

        final Integer fundID = createFund(jsonData, this.requestSpec, this.responseSpec);
        Assert.assertNotNull(fundID);

        jsonData = FundsResourceHandler.retrieveFund(fundID, this.requestSpec, this.responseSpec);
        FundsHelper fh2 = FundsHelper.fromJSON(jsonData);

        assertEquals(fh.getName(), fh2.getName());
    }

    @Test
    public void testRetrieveAllFunds() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).build();
        String jsonData = fh.toJSON();

        final Integer fundID = createFund(jsonData, this.requestSpec, this.responseSpec);
        Assert.assertNotNull(fundID);

        List<HashMap> list = FundsResourceHandler.retrieveAllFunds(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(list);
    }

    @Test
    public void testFundUnknown() {
        String jsonData = FundsResourceHandler.retrieveFund(Integer.MAX_VALUE, this.requestSpec, this.responseSpec);
        HashMap<String, String> map = new Gson().fromJson(jsonData, HashMap.class);
        assertEquals(map.get("userMessageGlobalisationCode"), "error.msg.resource.not.found");
    }

    private Integer createFund(final String fundJSON,
                               final RequestSpecification requestSpec,
                               final ResponseSpecification responseSpec) {
        System.out.println("------------------ CREATING NEW FUND  ------------------");
        return FundsResourceHandler.createFund(fundJSON, requestSpec, responseSpec);
    }

}
