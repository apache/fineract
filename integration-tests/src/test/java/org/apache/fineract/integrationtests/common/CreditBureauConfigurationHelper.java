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
import java.util.HashMap;
import org.apache.fineract.client.services.CreditBureauConfigurationApi;
import org.apache.fineract.client.util.Calls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreditBureauConfigurationHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauConfigurationHelper.class);

    private CreditBureauConfigurationHelper() {}

    private static CreditBureauConfigurationApi api() {
        return FineractClientHelper.getFineractClient().creditBureauConfiguration;
    }

    public static String getOrganisationCreditBureauConfiguration() {
        LOG.info(
                "---------------------------------GET ORGANISATION CREDIT BUREAU CONFIGURATION---------------------------------------------");
        return Calls.ok(api().getOrganisationCreditBureau());
    }

    public static String getCreditBureauConfiguration(Long organisationCreditBureauId) {
        LOG.info("---------------------------------GET CREDIT BUREAU CONFIGURATION---------------------------------------------");
        return Calls.ok(api().getConfiguration(organisationCreditBureauId));
    }

    public static String createCreditBureauConfiguration(Long creditBureauId, String configKey, String value, String description) {
        LOG.info("---------------------------------CREATING A CREDIT BUREAU CONFIGURATION---------------------------------------------");
        final HashMap<String, String> map = new HashMap<>();
        map.put("configkey", configKey);
        map.put("value", value);
        map.put("description", description);
        return Calls.ok(api().createCreditBureauConfiguration(creditBureauId, new Gson().toJson(map)));
    }

    public static String updateCreditBureauConfiguration(Long configurationId, String configKey, String value) {
        LOG.info("---------------------------------UPDATING A CREDIT BUREAU CONFIGURATION---------------------------------------------");
        final HashMap<String, String> map = new HashMap<>();
        if (configKey != null) {
            map.put("configkey", configKey);
        }
        map.put("value", value);
        return Calls.ok(api().updateCreditBureauConfiguration(configurationId, new Gson().toJson(map)));
    }

    public static String addOrganisationCreditBureau(Long creditBureauId, String alias, boolean isActive) {
        LOG.info("---------------------------------CREATING ORGANISATION CREDIT BUREAU---------------------------------------------");
        final HashMap<String, Object> map = new HashMap<>();
        map.put("alias", alias);
        map.put("isActive", isActive);
        return Calls.ok(api().addOrganisationCreditBureau(creditBureauId, new Gson().toJson(map)));
    }

    public static String updateOrganisationCreditBureau(String creditBureauId, boolean isActive) {
        LOG.info("---------------------------------UPDATING ORGANISATION CREDIT BUREAU---------------------------------------------");
        final HashMap<String, Object> map = new HashMap<>();
        map.put("creditBureauId", creditBureauId);
        map.put("isActive", isActive);
        return Calls.ok(api().updateCreditBureau(new Gson().toJson(map)));
    }

    public static String createCreditBureauConfigurationRaw(Long creditBureauId, String jsonBody) {
        return Calls.ok(api().createCreditBureauConfiguration(creditBureauId, jsonBody));
    }

    public static String addOrganisationCreditBureauRaw(Long creditBureauId, String jsonBody) {
        return Calls.ok(api().addOrganisationCreditBureau(creditBureauId, jsonBody));
    }

    public static String createLoanProductMappingRaw(Long organisationCreditBureauId, String jsonBody) {
        return Calls.ok(api().createCreditBureauLoanProductMapping(organisationCreditBureauId, jsonBody));
    }
}
