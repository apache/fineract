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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.AuditHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.commands.MakercheckersHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "unused" })
public class MakercheckerTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private MakercheckersHelper makercheckersHelper;
    private RolesHelper rolesHelper;
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
        this.rolesHelper = new RolesHelper();
        this.auditHelper = new AuditHelper(requestSpec, responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void testMakercheckerInboxList() {
        // given
        // when
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
            // client permission - maker-checker disabled
            PutPermissionsRequest putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("CREATE_CLIENT", false);
            rolesHelper.updatePermissions(putPermissionsRequest);
            putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("ACTIVATE_CLIENT", false);
            rolesHelper.updatePermissions(putPermissionsRequest);

            Integer roleId = RolesHelper.createRole(requestSpec, responseSpec);
            Map<String, Boolean> permissionMap = Map.of("CREATE_CLIENT", true, "CREATE_CLIENT_CHECKER", true, "ACTIVATE_CLIENT", true,
                    "ACTIVATE_CLIENT_CHECKER", true, "WITHDRAWAL_SAVINGSACCOUNT", true, "WITHDRAWAL_SAVINGSACCOUNT_CHECKER", true);
            RolesHelper.addPermissionsToRole(requestSpec, responseSpec, roleId, permissionMap);
            final Integer staffId = StaffHelper.createStaff(this.requestSpec, this.responseSpec);
            // create maker user
            String maker = Utils.uniqueRandomStringGenerator("user", 8);
            final Integer makerUserId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, maker,
                    "A1b2c3d4e5f$", "resourceId");

            // create client - maker-checker disabled
            RequestSpecification makerRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build()
                    .header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(maker, "A1b2c3d4e5f$"));
            Integer clientId = ClientHelper.createClient(makerRequestSpec, this.responseSpec);
            assertNotNull(clientId);
            ClientHelper.verifyClientCreatedOnServer(requestSpec, this.responseSpec, clientId);

            final Integer savingsId = createApproveActivateSavingsAccountDailyPosting(clientId, START_DATE_STRING);
            assertNotNull(savingsId);
            Integer transactionId = (Integer) savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", TRANSACTION_DATE_STRING,
                    CommonConstants.RESPONSE_RESOURCE_ID);
            assertNotNull(transactionId);

            // client and saving permission - maker-checker enabled
            putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("ACTIVATE_CLIENT", true);
            rolesHelper.updatePermissions(putPermissionsRequest);
            putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("WITHDRAWAL_SAVINGSACCOUNT", true);
            rolesHelper.updatePermissions(putPermissionsRequest);

            // create client - maker-checker enabled
            clientId = ClientHelper.createClient(makerRequestSpec, this.responseSpec);
            assertNull(clientId, "Client is created on the server");

            List<Map<String, Object>> auditDetails = makercheckersHelper
                    .getMakerCheckerList(Map.of("actionName", "CREATE", "entityName", "CLIENT", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size(), "More than one command exists");
            Long clientCommandId = ((Double) auditDetails.get(0).get("id")).longValue();

            // savings withdrawal - maker-checker enabled
            SavingsAccountHelper makerSavingsHelper = new SavingsAccountHelper(makerRequestSpec, this.responseSpec);
            Integer withdrawalId = (Integer) makerSavingsHelper.withdrawalFromSavingsAccount(savingsId, "100", TRANSACTION_DATE_STRING,
                    CommonConstants.RESPONSE_RESOURCE_ID);
            assertNull(withdrawalId, "Withdrawal performed on the server");

            auditDetails = makercheckersHelper.getMakerCheckerList(
                    Map.of("actionName", "WITHDRAWAL", "entityName", "SAVINGSACCOUNT", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size(), "More than one command exists");
            Long savingCommandId = ((Double) auditDetails.get(0).get("id")).longValue();

            // check by the same user should fail
            ResponseSpecification failedResponseSpec = new ResponseSpecBuilder().expectStatusCode(400).build();
            MakercheckersHelper.approveMakerCheckerEntry(makerRequestSpec, failedResponseSpec, clientCommandId);
            MakercheckersHelper.approveMakerCheckerEntry(makerRequestSpec, failedResponseSpec, savingCommandId);

            // create checker user
            String checker = Utils.uniqueRandomStringGenerator("user", 8);
            final Integer checkerUserId = (Integer) UserHelper.createUser(this.requestSpec, this.responseSpec, roleId, staffId, checker,
                    "A1b2c3d4e5f$", "resourceId");
            RequestSpecification checkerRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build()
                    .header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(checker, "A1b2c3d4e5f$"));

            // check by another checker user should succeed
            HashMap<?, ?> response = MakercheckersHelper.approveMakerCheckerEntry(checkerRequestSpec, responseSpec, clientCommandId);
            assertNotNull(response);
            clientId = (Integer) response.get("clientId");
            assertNotNull(clientId);
            ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientId);

            response = MakercheckersHelper.approveMakerCheckerEntry(checkerRequestSpec, responseSpec, savingCommandId);
            assertNotNull(response);
            withdrawalId = (Integer) response.get("resourceId");
            assertNotNull(withdrawalId);

            // add checker superuser permission - actions are performed in one step
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

            PutPermissionsRequest putPermissionsRequest = new PutPermissionsRequest().putPermissionsItem("WITHDRAWAL_SAVINGSACCOUNT",
                    false);
            rolesHelper.updatePermissions(putPermissionsRequest);
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
