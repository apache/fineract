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

    private ResponseSpecification statusOkResponseSpec;
    private RequestSpecification requestSpec;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.statusOkResponseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testCreateFund() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);
    }

    @Test
    public void testCreateFundWithEmptyName() {
        FundsHelper fh = FundsHelper
                         .create(null)
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        final Long fundID = createFund(jsonData, this.requestSpec, responseSpec);
        Assert.assertNull(fundID);
    }

    @Test
    public void testCreateFundWithEmptyExternalId() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(null)
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);
    }

    @Test
    public void testCreateFundWithDuplicateName() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        FundsHelper fh2 = FundsHelper
                         .create(fh.getName())
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        jsonData = fh2.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
        final Long fundID2 = createFund(jsonData, this.requestSpec, responseSpec);
        Assert.assertNull(fundID2);
    }

    @Test
    public void testCreateFundWithDuplicateExternalId() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        FundsHelper fh2 = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(fh.getExternalId())
                         .build();
        jsonData = fh2.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
        final Long fundID2 = createFund(jsonData, this.requestSpec, responseSpec);
        Assert.assertNull(fundID2);
    }

    @Test
    public void testCreateFundWithInvalidName() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 120))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        final Long fundID = createFund(jsonData, this.requestSpec, responseSpec);
        Assert.assertNull(fundID);
    }

    @Test
    public void testCreateFundWithInvalidExternalId() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 120))
                         .build();
        String jsonData = fh.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        final Long fundID = createFund(jsonData, this.requestSpec, responseSpec);
        Assert.assertNull(fundID);
    }

    @Test
    public void testRetrieveFund() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        jsonData = FundsResourceHandler.retrieveFund(fundID, this.requestSpec, this.statusOkResponseSpec);
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

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        List<FundsHelper> fhList = FundsResourceHandler.retrieveAllFunds(this.requestSpec, this.statusOkResponseSpec);

        Assert.assertNotNull(fhList);
        Assert.assertThat(fhList.size(), greaterThanOrEqualTo(1));
        Assert.assertThat(fhList, hasItem(fh));
    }

    @Test
    public void testRetrieveUnknownFund() {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        String jsonData = FundsResourceHandler.retrieveFund(Long.MAX_VALUE, this.requestSpec, responseSpec);
        HashMap<String, String> map = new Gson().fromJson(jsonData, HashMap.class);
        assertEquals(map.get("userMessageGlobalisationCode"), "error.msg.resource.not.found");
    }

    @Test
    public void testUpdateFund() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 10);
        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, newExternalId, this.requestSpec, this.statusOkResponseSpec);

        Assert.assertEquals(newName, fh2.getName());
        Assert.assertEquals(newExternalId, fh2.getExternalId());
    }

    @Test
    public void testUpdateUnknownFund() {
        String newName = Utils.randomNameGenerator("", 10);
        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        FundsHelper fh = FundsResourceHandler.updateFund(Long.MAX_VALUE, newName, newExternalId, this.requestSpec,
                responseSpec);
        Assert.assertNull(fh);
    }

    @Test
    public void testUpdateFundWithInvalidNewName() {
        FundsHelper fh = FundsHelper
                        .create(Utils.randomNameGenerator("", 10))
                        .externalId(Utils.randomNameGenerator("fund-", 5))
                        .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 120);
        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, newExternalId, this.requestSpec,
                responseSpec);

        Assert.assertNull(fh2);
    }

    @Test
    public void testUpdateFundWithNewExternalId() {
        FundsHelper fh = FundsHelper
                         .create(Utils.randomNameGenerator("", 10))
                         .externalId(Utils.randomNameGenerator("fund-", 5))
                         .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, null, newExternalId, this.requestSpec, this.statusOkResponseSpec);

        Assert.assertEquals(newExternalId, fh2.getExternalId());
    }

    @Test
    public void testUpdateFundWithInvalidNewExternalId() {
        FundsHelper fh = FundsHelper
                .create(Utils.randomNameGenerator("", 10))
                .externalId(Utils.randomNameGenerator("fund-", 5))
                .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 10);
        String newExternalId = Utils.randomNameGenerator("fund-", 120);
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, newExternalId, this.requestSpec,
                responseSpec);

        Assert.assertNull(fh2);
    }

    @Test
    public void testUpdateFundWithNewName() {
        FundsHelper fh = FundsHelper
                .create(Utils.randomNameGenerator("", 10))
                .externalId(Utils.randomNameGenerator("fund-", 5))
                .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 10);
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, null, this.requestSpec, this.statusOkResponseSpec);

        Assert.assertEquals(newName, fh2.getName());
    }

    @Test
    public void testUpdateFundWithEmptyParams() {
        FundsHelper fh = FundsHelper
                .create(Utils.randomNameGenerator("", 10))
                .externalId(Utils.randomNameGenerator("fund-", 5))
                .build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assert.assertNotNull(fundID);

        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, null, null, this.requestSpec, this.statusOkResponseSpec);

        Assert.assertNull(fh2.getName());
        Assert.assertNull(fh2.getExternalId());

        // assert that there was no change in
        // the name and external ID of the fund
        jsonData = FundsResourceHandler.retrieveFund(fundID, this.requestSpec, this.statusOkResponseSpec);
        FundsHelper fh3 = new Gson().fromJson(jsonData, FundsHelper.class);

        Assert.assertEquals(fh.getName(), fh3.getName());
        Assert.assertEquals(fh.getExternalId(), fh3.getExternalId());
    }

    private Long createFund(final String fundJSON,
                            final RequestSpecification requestSpec,
                            final ResponseSpecification responseSpec) {
        String fundId = String.valueOf(FundsResourceHandler.createFund(fundJSON, requestSpec, responseSpec));
        if (fundId.equals("null")) {
            // Invalid JSON data parameters
            return null;
        }

        return new Long(fundId);
    }

}
