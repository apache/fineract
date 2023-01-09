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

import static io.restassured.RestAssured.given;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.File;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditBureauIntegrationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauIntegrationHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public CreditBureauIntegrationHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static Object getCreditReport(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String creditBureauId, String nrc) {
        LOG.info("---------------------------------CREATING A CREDIT_BUREAU_CONFIGURATION---------------------------------------------");
        final String CREDITBUREAU_CONFIGURATION_URL = "/fineract-provider/api/v1/creditBureauIntegration/creditReport?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, CREDITBUREAU_CONFIGURATION_URL,
                createGetCreditReportAsJson(creditBureauId, nrc), null);
    }

    public static String uploadCreditReport(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String creditBureauId, File file) {
        LOG.info("---------------------------------CREATING A CREDIT_BUREAU_CONFIGURATION---------------------------------------------");
        final String CREDITBUREAU_CONFIGURATION_URL = "/fineract-provider/api/v1/creditBureauIntegration/addCreditReport?"
                + Utils.TENANT_IDENTIFIER;
        return given().spec(requestSpec).queryParam("creditBureauId", creditBureauId).contentType("multipart/form-data")
                .multiPart("file", file).expect().spec(responseSpec).log().ifError().when().post(CREDITBUREAU_CONFIGURATION_URL).andReturn()
                .asString();
    }

    public static String createGetCreditReportAsJson(final String creditBureauId, final String nrc) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("creditBureauID", creditBureauId);
        map.put("NRC", nrc);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

}
