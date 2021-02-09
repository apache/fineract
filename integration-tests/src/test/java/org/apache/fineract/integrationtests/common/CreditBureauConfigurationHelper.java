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
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditBureauConfigurationHelper {

    private static final String CREATE_CREDITBUREAUCONFIGURATION_URL = "/fineract-provider/api/v1/CreditBureauConfiguration/configuration?"
            + Utils.TENANT_IDENTIFIER;
    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauConfigurationHelper.class);
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public CreditBureauConfigurationHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static Integer createCreditBureauConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return createCreditBureauConfiguration(requestSpec, responseSpec, "1");
    }

    public static Integer createCreditBureauConfiguration(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String creditBureauId) {
        LOG.info("---------------------------------CREATING A CREDIT_BUREAU_CONFIGURATION---------------------------------------------");
        final String CREDITBUREAU_CONFIGURATION_URL = " /fineract-provider/api/v1/CreditBureauConfiguration/configuration/" + creditBureauId
                + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, CREDITBUREAU_CONFIGURATION_URL,
                creditBureauConfigurationAsJson("testConfigKey", "testConfigKeyValue", "description"), "resourceId");
    }

    /*
     * public static Object updateCreditBureauConfiguration(final RequestSpecification requestSpec, final
     * ResponseSpecification responseSpec, final Integer ConfigurationId) { return
     * updateCreditBureauConfiguration(requestSpec, responseSpec, ConfigurationId); }
     */

    public static String updateCreditBureauConfiguration(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer ConfigurationId) {

        Object configurationObject = updateCreditBureauConfiguration(requestSpec, responseSpec, ConfigurationId, "updateConfigKeyValue");
        // Convert the Object to String and fetch updated value
        Gson gson = new Gson();
        String result = gson.toJson(configurationObject);
        JsonObject reportObject = JsonParser.parseString(result).getAsJsonObject();
        String value = reportObject.get("value").getAsString();

        return value;
    }

    public static Object updateCreditBureauConfiguration(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer ConfigurationId, final String updateConfigKeyValue) {
        LOG.info("---------------------------------UPDATING A CREDIT_BUREAU_CONFIGURATION---------------------------------------------");
        final String CREDITBUREAU_CONFIGURATION_URL = " /fineract-provider/api/v1/CreditBureauConfiguration/configuration/"
                + ConfigurationId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, CREDITBUREAU_CONFIGURATION_URL,
                updateCreditBureauConfigurationAsJson("updateConfigKeyValue", "description"), "changes");
    }

    public static String creditBureauConfigurationAsJson(final String configkey, final String value, final String description) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("configkey", configkey);
        map.put("value", value);
        map.put("description", description);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public static String updateCreditBureauConfigurationAsJson(final String value, final String description) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("value", value);
        map.put("description", description);
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

}
