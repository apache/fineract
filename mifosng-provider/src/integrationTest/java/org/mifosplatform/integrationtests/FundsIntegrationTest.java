/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import static org.hamcrest.Matchers.*;
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

import java.util.*;

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
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.responseSpec);
        Assert.assertNotNull(fundID);

        jsonData = FundsResourceHandler.retrieveFund(fundID, this.requestSpec, this.responseSpec);
        FundsHelper fh2 = FundsHelper.fromJSON(jsonData);

        assertEquals(fh.getName(), fh2.getName());
    }

    @Test
    public void testRetrieveAllFunds() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.responseSpec);
        Assert.assertNotNull(fundID);

        List<FundsHelper> fhList = FundsResourceHandler.retrieveAllFunds(this.requestSpec, this.responseSpec);

        Assert.assertNotNull(fhList);
        Assert.assertThat(fhList.size(), greaterThanOrEqualTo(1));
        Assert.assertThat(fhList, hasItem(fh));
    }

    @Test
    public void testFundUnknown() {
        String jsonData = FundsResourceHandler.retrieveFund(Long.MAX_VALUE, this.requestSpec, this.responseSpec);
        HashMap<String, String> map = new Gson().fromJson(jsonData, HashMap.class);
        assertEquals(map.get("userMessageGlobalisationCode"), "error.msg.resource.not.found");
    }

    private Long createFund(final String fundJSON,
                            final RequestSpecification requestSpec,
                            final ResponseSpecification responseSpec) {
        return new Long(String.valueOf(FundsResourceHandler.createFund(fundJSON, requestSpec, responseSpec)));
    }

}
