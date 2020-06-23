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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.funds.FundsHelper;
import org.apache.fineract.integrationtests.common.funds.FundsResourceHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Funds Integration Test for checking Funds Application.
 */
public class FundsIntegrationTest {

    private ResponseSpecification statusOkResponseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.statusOkResponseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testCreateFund() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);
    }

    @Test
    public void testCreateFundWithEmptyName() {
        FundsHelper fh = FundsHelper.create(null).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        final Long fundID = createFund(jsonData, this.requestSpec, responseSpec);
        Assertions.assertNull(fundID);
    }

    @Test
    public void testCreateFundWithEmptyExternalId() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(null).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);
    }

    @Test
    public void testCreateFundWithDuplicateName() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        FundsHelper fh2 = FundsHelper.create(fh.getName()).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        jsonData = fh2.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
        final Long fundID2 = createFund(jsonData, this.requestSpec, responseSpec);
        Assertions.assertNull(fundID2);
    }

    @Test
    public void testCreateFundWithDuplicateExternalId() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        FundsHelper fh2 = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(fh.getExternalId()).build();
        jsonData = fh2.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
        final Long fundID2 = createFund(jsonData, this.requestSpec, responseSpec);
        Assertions.assertNull(fundID2);
    }

    @Test
    public void testCreateFundWithInvalidName() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 120)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        final Long fundID = createFund(jsonData, this.requestSpec, responseSpec);
        Assertions.assertNull(fundID);
    }

    @Test
    public void testCreateFundWithInvalidExternalId() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 120)).build();
        String jsonData = fh.toJSON();

        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        final Long fundID = createFund(jsonData, this.requestSpec, responseSpec);
        Assertions.assertNull(fundID);
    }

    @Test
    public void testRetrieveFund() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        jsonData = FundsResourceHandler.retrieveFund(fundID, this.requestSpec, this.statusOkResponseSpec);
        FundsHelper fh2 = FundsHelper.fromJSON(jsonData);

        assertEquals(fh.getName(), fh2.getName());
    }

    @Test
    public void testRetrieveAllFunds() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        List<FundsHelper> fhList = FundsResourceHandler.retrieveAllFunds(this.requestSpec, this.statusOkResponseSpec);

        Assertions.assertNotNull(fhList);
        assertThat(fhList.size(), greaterThanOrEqualTo(1));
        assertThat(fhList, hasItem(fh));
    }

    @Test
    public void testRetrieveUnknownFund() {
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        String jsonData = FundsResourceHandler.retrieveFund(Long.MAX_VALUE, this.requestSpec, responseSpec);
        HashMap<String, Object> map = new Gson().fromJson(jsonData, new TypeToken<HashMap<String, Object>>() {}.getType());
        assertEquals("error.msg.resource.not.found", map.get("userMessageGlobalisationCode"));
    }

    @Test
    public void testUpdateFund() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 10);
        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, newExternalId, this.requestSpec, this.statusOkResponseSpec);

        Assertions.assertEquals(newName, fh2.getName());
        Assertions.assertEquals(newExternalId, fh2.getExternalId());
    }

    @Test
    public void testUpdateUnknownFund() {
        String newName = Utils.randomNameGenerator("", 10);
        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(404).build();
        FundsHelper fh = FundsResourceHandler.updateFund(Long.MAX_VALUE, newName, newExternalId, this.requestSpec, responseSpec);
        Assertions.assertNull(fh);
    }

    @Test
    public void testUpdateFundWithInvalidNewName() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 120);
        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, newExternalId, this.requestSpec, responseSpec);

        Assertions.assertNull(fh2);
    }

    @Test
    public void testUpdateFundWithNewExternalId() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        String newExternalId = Utils.randomNameGenerator("fund-", 5);
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, null, newExternalId, this.requestSpec, this.statusOkResponseSpec);

        Assertions.assertEquals(newExternalId, fh2.getExternalId());
    }

    @Test
    public void testUpdateFundWithInvalidNewExternalId() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 10);
        String newExternalId = Utils.randomNameGenerator("fund-", 120);
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, newExternalId, this.requestSpec, responseSpec);

        Assertions.assertNull(fh2);
    }

    @Test
    public void testUpdateFundWithNewName() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        String newName = Utils.randomNameGenerator("", 10);
        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, newName, null, this.requestSpec, this.statusOkResponseSpec);

        Assertions.assertEquals(newName, fh2.getName());
    }

    @Test
    public void testUpdateFundWithEmptyParams() {
        FundsHelper fh = FundsHelper.create(Utils.randomNameGenerator("", 10)).externalId(Utils.randomNameGenerator("fund-", 5)).build();
        String jsonData = fh.toJSON();

        final Long fundID = createFund(jsonData, this.requestSpec, this.statusOkResponseSpec);
        Assertions.assertNotNull(fundID);

        FundsHelper fh2 = FundsResourceHandler.updateFund(fundID, null, null, this.requestSpec, this.statusOkResponseSpec);

        Assertions.assertNull(fh2.getName());
        Assertions.assertNull(fh2.getExternalId());

        // assert that there was no change in
        // the name and external ID of the fund
        jsonData = FundsResourceHandler.retrieveFund(fundID, this.requestSpec, this.statusOkResponseSpec);
        FundsHelper fh3 = new Gson().fromJson(jsonData, FundsHelper.class);

        Assertions.assertEquals(fh.getName(), fh3.getName());
        Assertions.assertEquals(fh.getExternalId(), fh3.getExternalId());
    }

    private Long createFund(final String fundJSON, final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        String fundId = String.valueOf(FundsResourceHandler.createFund(fundJSON, requestSpec, responseSpec));
        if (fundId.equals("null")) {
            // Invalid JSON data parameters
            return null;
        }

        return Long.valueOf(fundId);
    }

}
