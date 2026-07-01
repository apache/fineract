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
package org.apache.fineract.integrationtests.client.feign.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

/**
 * Uses raw HashMap payloads via {@link FeignRawHttpHelper} instead of the generated Feign request models
 * (PostGroupsRequest, PostCentersRequest, PostStaffRequest) because those models don't expose fields this helper needs
 * (e.g. externalId, activationDate, dateFormat, locale) -- a Swagger/OpenAPI spec gap, not an oversight.
 */
public final class FeignGroupCenterHelper {

    private static final Gson GSON = new Gson();

    private static final String OFFICE_ID = "officeId";
    private static final String NAME = "name";
    private static final String LOCALE = "locale";
    private static final String DATE_FORMAT = "dateFormat";
    private static final String ACTIVE = "active";
    private static final String ACTIVATION_DATE = "activationDate";
    private static final String EXTERNAL_ID = "externalId";
    private static final String DEFAULT_ACTIVATION_DATE = "04 March 2011";
    private static final String DEFAULT_JOINING_DATE = "20 September 2011";

    private FeignGroupCenterHelper() {}

    public static Long createGroup(int officeId) {
        Map<String, Object> map = new HashMap<>();
        map.put(OFFICE_ID, officeId);
        map.put(NAME, Utils.uniqueRandomStringGenerator("Group_Name_", 5));
        map.put(EXTERNAL_ID, UUID.randomUUID().toString());
        map.put(DATE_FORMAT, LoanTestData.DATETIME_PATTERN);
        map.put(LOCALE, LoanTestData.LOCALE);
        map.put(ACTIVE, true);
        map.put(ACTIVATION_DATE, DEFAULT_ACTIVATION_DATE);
        return extractResourceId(FeignRawHttpHelper.post("/groups", GSON.toJson(map)));
    }

    public static Long createStaff(int officeId) {
        Map<String, Object> map = new HashMap<>();
        map.put(LOCALE, LoanTestData.LOCALE);
        map.put(DATE_FORMAT, LoanTestData.DATETIME_PATTERN);
        map.put("joiningDate", DEFAULT_JOINING_DATE);
        map.put(OFFICE_ID, officeId);
        map.put("firstname", Utils.uniqueRandomStringGenerator("michael_", 5));
        map.put("lastname", Utils.uniqueRandomStringGenerator("Doe_", 4));
        map.put("isLoanOfficer", true);
        return extractResourceId(FeignRawHttpHelper.post("/staff", GSON.toJson(map)));
    }

    public static Long createCenter(String name, int officeId, String externalId, int staffId, long[] groupMembers, String activationDate) {
        Map<String, Object> map = new HashMap<>();
        map.put(NAME, name);
        map.put(OFFICE_ID, officeId);
        map.put(EXTERNAL_ID, externalId);
        map.put("staffId", staffId);
        map.put("groupMembers", groupMembers);
        map.put(ACTIVE, true);
        map.put(LOCALE, LoanTestData.LOCALE);
        map.put(DATE_FORMAT, LoanTestData.DATETIME_PATTERN);
        map.put(ACTIVATION_DATE, activationDate);
        return extractResourceId(FeignRawHttpHelper.post("/centers", GSON.toJson(map)));
    }

    public static JsonObject retrieveCenter(long centerId) {
        return JsonParser.parseString(FeignRawHttpHelper.get("/centers/" + centerId + "?associations=groupMembers")).getAsJsonObject();
    }

    public static void associateClientToGroup(long groupId, long clientId) {
        Map<String, List<String>> map = Map.of("clientMembers", List.of(String.valueOf(clientId)));
        FeignRawHttpHelper.post("/groups/" + groupId + "?command=associateClients", GSON.toJson(map));
    }

    public static Long createCollateralProduct() {
        Map<String, String> map = new HashMap<>();
        map.put(NAME, Utils.randomStringGenerator("COLLATERAL_PRODUCT", 5));
        map.put("currency", "USD");
        map.put("unitType", "acre");
        map.put("quality", "agriculture");
        map.put("pctToBase", BigDecimal.valueOf(40).toString());
        map.put("basePrice", BigDecimal.valueOf(100000000).toString());
        map.put(LOCALE, LoanTestData.LOCALE);
        return extractResourceId(FeignRawHttpHelper.post("/collateral-management", GSON.toJson(map)));
    }

    public static Long createClientCollateral(long clientId, long collateralId) {
        Map<String, String> map = new HashMap<>();
        map.put("collateralId", String.valueOf(collateralId));
        map.put("quantity", BigDecimal.valueOf(100).toString());
        map.put("locale", LoanTestData.LOCALE);
        return extractResourceId(FeignRawHttpHelper.post("/clients/" + clientId + "/collaterals", GSON.toJson(map)));
    }

    private static Long extractResourceId(String response) {
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        return json.get("resourceId").getAsLong();
    }
}
