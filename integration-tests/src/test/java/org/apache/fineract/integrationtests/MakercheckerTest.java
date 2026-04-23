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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.AuditHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.commands.MakercheckersHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.system.DatatableHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MakercheckerTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private MakercheckersHelper makercheckersHelper;
    private AuditHelper auditHelper;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private static final String START_DATE_STRING = "03 June 2023";
    private static final String TRANSACTION_DATE_STRING = "05 June 2023";
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.makercheckersHelper = new MakercheckersHelper(this.requestSpec, this.responseSpec);
        this.auditHelper = new AuditHelper(requestSpec, responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void testMakercheckerInboxList() {
        List<Map<String, Object>> makerCheckerList = this.makercheckersHelper.getMakerCheckerList(null);
        assertNotNull(makerCheckerList);
    }

    @Test
    public void testMakerCheckerOn() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(false));

        try {
            // Direct static calls to bypass bridge and instance misuse
            PutPermissionsRequest putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("CREATE_CLIENT", false);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, putPermissionsRequest.getPermissions());

            putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("ACTIVATE_CLIENT", false);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, putPermissionsRequest.getPermissions());

            Integer roleId = RolesHelper.createRole(requestSpec, responseSpec);
            Map<String, Boolean> permissionMap = Map.of("CREATE_CLIENT", true, "CREATE_CLIENT_CHECKER", true, "ACTIVATE_CLIENT", true,
                    "ACTIVATE_CLIENT_CHECKER", true, "WITHDRAWAL_SAVINGSACCOUNT", true, "WITHDRAWAL_SAVINGSACCOUNT_CHECKER", true);
            RolesHelper.addPermissionsToRole(requestSpec, responseSpec, roleId, permissionMap);

            final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
            String maker = Utils.uniqueRandomStringGenerator("user", 8);
            final Integer makerUserId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, maker,
                    "QwE!SrTy#9uP0", "resourceId");

            RequestSpecification makerRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build()
                    .header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(maker, "QwE!SrTy#9uP0"));
            Integer clientId = ClientHelper.createClient(makerRequestSpec, this.responseSpec);
            assertNotNull(clientId);
            ClientHelper.verifyClientCreatedOnServer(requestSpec, this.responseSpec, clientId);

            final Integer savingsId = createApproveActivateSavingsAccountDailyPosting(clientId, START_DATE_STRING);
            assertNotNull(savingsId);
            Integer transactionId = (Integer) savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", TRANSACTION_DATE_STRING,
                    CommonConstants.RESPONSE_RESOURCE_ID);
            assertNotNull(transactionId);

            putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("ACTIVATE_CLIENT", true);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, putPermissionsRequest.getPermissions());

            putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("WITHDRAWAL_SAVINGSACCOUNT", true);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, putPermissionsRequest.getPermissions());

            clientId = ClientHelper.createClient(makerRequestSpec, this.responseSpec);
            assertNull(clientId, "Client is created on the server");

            List<Map<String, Object>> auditDetails = makercheckersHelper
                    .getMakerCheckerList(Map.of("actionName", "CREATE", "entityName", "CLIENT", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size());
            Long clientCommandId = ((Double) auditDetails.get(0).get("id")).longValue();

            SavingsAccountHelper makerSavingsHelper = new SavingsAccountHelper(makerRequestSpec, this.responseSpec);
            Integer withdrawalId = (Integer) makerSavingsHelper.withdrawalFromSavingsAccount(savingsId, "100", TRANSACTION_DATE_STRING,
                    CommonConstants.RESPONSE_RESOURCE_ID);
            assertNull(withdrawalId);

            auditDetails = makercheckersHelper.getMakerCheckerList(
                    Map.of("actionName", "WITHDRAWAL", "entityName", "SAVINGSACCOUNT", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size());
            Long savingCommandId = ((Double) auditDetails.get(0).get("id")).longValue();

            ResponseSpecification failedResponseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
            MakercheckersHelper.approveMakerCheckerEntry(makerRequestSpec, failedResponseSpec, clientCommandId);
            MakercheckersHelper.approveMakerCheckerEntry(makerRequestSpec, failedResponseSpec, savingCommandId);

            String checker = Utils.uniqueRandomStringGenerator("user", 8);
            UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, checker, "QwE!SrTy#9uP0", "resourceId");

            RequestSpecification checkerRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build()
                    .header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(checker, "QwE!SrTy#9uP0"));

            Map<?, ?> approveResponse = MakercheckersHelper.approveMakerCheckerEntry(checkerRequestSpec, responseSpec, clientCommandId);
            assertNotNull(approveResponse);
            clientId = (Integer) approveResponse.get("clientId");
            assertNotNull(clientId);
            ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientId);

            approveResponse = MakercheckersHelper.approveMakerCheckerEntry(checkerRequestSpec, responseSpec, savingCommandId);
            assertNotNull(approveResponse);
            withdrawalId = (Integer) approveResponse.get("resourceId");
            assertNotNull(withdrawalId);

            permissionMap = Map.of("CHECKER_SUPER_USER", true);
            RolesHelper.addPermissionsToRole(requestSpec, responseSpec, roleId, permissionMap);
            clientId = ClientHelper.createClient(makerRequestSpec, this.responseSpec);
            assertNotNull(clientId);
            ClientHelper.verifyClientCreatedOnServer(requestSpec, this.responseSpec, clientId);

            withdrawalId = (Integer) makerSavingsHelper.withdrawalFromSavingsAccount(savingsId, "100", TRANSACTION_DATE_STRING,
                    CommonConstants.RESPONSE_RESOURCE_ID);
            assertNotNull(withdrawalId);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));

            PutPermissionsRequest finalCleanupRequest = new PutPermissionsRequest().putPermissionsItem("WITHDRAWAL_SAVINGSACCOUNT", false);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, finalCleanupRequest.getPermissions());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "m_client", "m_group", "m_center", "m_loan", "m_office", "m_savings_account" })
    public void testRejectDatatableCreationCleansUpOrphanedTable(String apptableName) {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(false));

        try {
            PutPermissionsRequest putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("CREATE_DATATABLE", true);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, putPermissionsRequest.getPermissions());

            Integer roleId = RolesHelper.createRole(requestSpec, responseSpec);
            Map<String, Boolean> permissionMap = Map.of("CREATE_DATATABLE", true, "CREATE_DATATABLE_CHECKER", true);
            RolesHelper.addPermissionsToRole(requestSpec, responseSpec, roleId, permissionMap);

            Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
            String maker = Utils.uniqueRandomStringGenerator("user", 8);
            Integer makerUserId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, maker,
                    "QwE!SrTy#9uP0", "resourceId");

            String checker = Utils.uniqueRandomStringGenerator("user", 8);
            UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, checker, "QwE!SrTy#9uP0", "resourceId");

            RequestSpecification makerRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build()
                    .header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(maker, "QwE!SrTy#9uP0"));

            DatatableHelper makerDatatableHelper = new DatatableHelper(makerRequestSpec, this.responseSpec);
            String datatableJson = DatatableHelper.getTestDatatableAsJSON(apptableName, false);
            String datatableName = com.google.gson.JsonParser.parseString(datatableJson).getAsJsonObject().get("datatableName")
                    .getAsString();
            makerDatatableHelper.createDatatable(datatableJson, "");

            List<Map<String, Object>> auditDetails = makercheckersHelper
                    .getMakerCheckerList(Map.of("actionName", "CREATE", "entityName", "DATATABLE", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size());
            Long commandId = ((Double) auditDetails.get(0).get("id")).longValue();

            MakercheckersHelper.rejectMakerCheckerEntry(FineractClientHelper.createNewFineractClient(checker, "QwE!SrTy#9uP0"), commandId);

            putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("CREATE_DATATABLE", false);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, putPermissionsRequest.getPermissions());

            DatatableHelper adminDatatableHelper = new DatatableHelper(this.requestSpec, this.responseSpec);
            String recreatedName = adminDatatableHelper.createDatatable(datatableJson, "resourceIdentifier");
            assertEquals(datatableName, recreatedName);

            adminDatatableHelper.deleteDatatable(datatableName);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));

            PutPermissionsRequest finalCleanupRequest = new PutPermissionsRequest().putPermissionsItem("CREATE_DATATABLE", false);
            RolesHelper.updatePermissions(RolesHelper.SUPER_USER_ROLE_ID, finalCleanupRequest.getPermissions());
        }
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Integer createApproveActivateSavingsAccountDailyPosting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        assertNotNull(savingsProductID);
        return savingsAccountHelper.createApproveActivateSavingsAccount(clientID, savingsProductID, startDate);
    }
}
