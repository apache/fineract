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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollateralManagementHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CollateralManagementHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public CollateralManagementHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createClientCollateral(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String clientId, final Integer collateralId) {
        LOG.info("---------------------------------CREATING A CLIENT_COLLATERAL---------------------------------------------");
        final String CLIENT_COLLATERAL_URL = "/fineract-provider/api/v1/clients/" + clientId + "/collaterals" + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, CLIENT_COLLATERAL_URL,
                clientCollateralAsJson(collateralId, BigDecimal.valueOf(100)), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Object getClientCollateralData(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer collateralId, final String clientId) {
        final String CLIENT_COLLATERAL_URL = "/fineract-provider/api/v1/clients" + clientId + "/collaterals/" + collateralId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, CLIENT_COLLATERAL_URL, "quantity");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String clientCollateralAsJson(final Integer collateralId, final BigDecimal quantity) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("collateralId", collateralId.toString());
        map.put("quantity", quantity.toString());
        map.put("locale", "en");
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createCollateralProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        LOG.info("---------------------------------CREATING A COLLATERAL_PRODUCT---------------------------------------------");
        final String COLLATERAL_PRODUCT_URL = "/fineract-provider/api/v1/collateral-management" + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, COLLATERAL_PRODUCT_URL,
                collateralProductAsJson(Utils.randomStringGenerator("COLLATERAL_PRODUCT", 5), "USD", "acre", "agriculture",
                        BigDecimal.valueOf(40), BigDecimal.valueOf(100000000), "en"),
                "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String collateralProductAsJson(final String name, final String currency, final String unitType, final String quality,
            final BigDecimal pctToBase, final BigDecimal baseAmount, final String locale) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("currency", currency);
        map.put("unitType", unitType);
        map.put("quality", quality);
        map.put("pctToBase", pctToBase.toString());
        map.put("basePrice", baseAmount.toString());
        map.put("locale", locale);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateCollateralProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer collateralId) {
        LOG.info("---------------------------------UPDATING A COLLATERAL_PRODUCT---------------------------------------------");
        final String COLLATERAL_PRODUCT_URL = "/fineract-provider/api/v1/collateral-management/" + collateralId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, COLLATERAL_PRODUCT_URL,
                updateCollateralProductAsJson(Utils.randomStringGenerator("COLLATERAL_PRODUCT", 5), "USD", "acre", "agriculture",
                        BigDecimal.valueOf(30), BigDecimal.valueOf(100000), "en"),
                "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String updateCollateralProductAsString(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer collateralId) {

        Object updateCollateralObject = updateCollateralProduct(requestSpec, responseSpec, collateralId);
        // Convert the Object to String and fetch updated value
        Gson gson = new Gson();
        String result = gson.toJson(updateCollateralObject);
        JsonObject reportObject = JsonParser.parseString(result).getAsJsonObject();
        String value = reportObject.get("pctToBase").getAsString();

        return value;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String updateCollateralProductAsJson(final String name, final String currency, final String unitType,
            final String quality, final BigDecimal pctToBase, final BigDecimal baseAmount, final String locale) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("currency", currency);
        map.put("unitType", unitType);
        map.put("quality", quality);
        map.put("pctToBase", pctToBase.toString());
        map.put("basePrice", baseAmount.toString());
        map.put("locale", locale);
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap updateClientCollateral(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer collateralId) {
        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientID);
        LOG.info("---------------------------------UPDATING A CLIENT COLLATERAL---------------------------------------------");
        final String CLIENT_COLLATERAL_URL = "/fineract-provider/api/v1/clients/" + clientID + "/collaterals/" + collateralId + "?"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, CLIENT_COLLATERAL_URL, updateClientCollateralAsJson(BigDecimal.valueOf(1)),
                "changes");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String updateClientCollateralAsString(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer collateralId) {

        Object clientCollateralObject = updateClientCollateral(requestSpec, responseSpec, collateralId);
        // Convert the Object to String and fetch updated value
        Gson gson = new Gson();
        String result = gson.toJson(clientCollateralObject);
        JsonObject reportObject = JsonParser.parseString(result).getAsJsonObject();
        String value = reportObject.get("quantity").getAsString();

        return value;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String updateClientCollateralAsJson(final BigDecimal quantity) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("quantity", quantity.toString());
        map.put("locale", "en");
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

}
