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
package org.apache.fineract.integrationtests.useradministration.roles;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.useradministration.data.PermissionData;

public final class RolesHelper extends IntegrationTest {

    public static final long SUPER_USER_ROLE_ID = 1L; // This is hardcoded into the initial Liquibase migration

    public RolesHelper() {

    }

    private static final String CREATE_ROLE_URL = "/fineract-provider/api/v1/roles?" + Utils.TENANT_IDENTIFIER;
    private static final String ROLE_URL = "/fineract-provider/api/v1/roles";
    private static final String PERMISSIONS_URL = "/fineract-provider/api/v1/permissions";
    private static final String DISABLE_ROLE_COMMAND = "disable";
    private static final String ENABLE_ROLE_COMMAND = "enable";

    private static final Gson GSON = new JSON().getGson();

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_ROLE_URL, getTestCreateRoleAsJSON(), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getTestCreateRoleAsJSON() {
        final HashMap<String, String> map = new HashMap<>();
        map.put("name", Utils.uniqueRandomStringGenerator("Role_Name_", 5));
        map.put("description", Utils.randomStringGenerator("Role_Description_", 10));
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static HashMap<String, Object> getRoleDetails(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer roleId) {
        final String GET_ROLE_URL = "/fineract-provider/api/v1/roles/" + roleId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, GET_ROLE_URL, "");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer disableRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer roleId) {
        return Utils.performServerPost(requestSpec, responseSpec, createRoleOperationURL(DISABLE_ROLE_COMMAND, roleId), "", "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer enableRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer roleId) {
        return Utils.performServerPost(requestSpec, responseSpec, createRoleOperationURL(ENABLE_ROLE_COMMAND, roleId), "", "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer deleteRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer roleId) {
        return Utils.performServerDelete(requestSpec, responseSpec, createRoleOperationURL(ENABLE_ROLE_COMMAND, roleId), "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String addPermissionsToRole(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer roleId, final Map<String, Boolean> permissionMap) {
        return Utils.performServerPut(requestSpec, responseSpec, ROLE_URL + "/" + roleId + "/permissions?" + Utils.TENANT_IDENTIFIER,
                getAddPermissionsToRoleJSON(permissionMap));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<PermissionData> getPermissions(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            boolean makerCheckerable) {
        String response = Utils.performServerGet(requestSpec, responseSpec,
                PERMISSIONS_URL + "?" + makerCheckerable + "=" + makerCheckerable);
        final Type listType = new TypeToken<List<PermissionData>>() {}.getType();
        return GSON.fromJson(response, listType);
    }

    public CommandProcessingResult updatePermissions(PutPermissionsRequest request) {
        return ok(fineract().permissions.updatePermissionsDetails(request));
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String getAddPermissionsToRoleJSON(Map<String, Boolean> permissionMap) {
        final HashMap<String, Map<String, Boolean>> map = new HashMap<>();
        map.put("permissions", permissionMap);
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String createRoleOperationURL(final String command, final Integer roleId) {
        return ROLE_URL + "/" + roleId + "?command=" + command + "&" + Utils.TENANT_IDENTIFIER;
    }
}
