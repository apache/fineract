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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.PostDataTablesRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDatatableHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignStaffHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MakercheckerTest extends FeignIntegrationTest {

    private static final String START_DATE_STRING = "03 June 2023";
    private static final String TRANSACTION_DATE_STRING = "05 June 2023";
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private FeignClientHelper clientHelper;
    private FeignStaffHelper staffHelper;
    private FeignSavingsProductHelper savingsProductHelper;
    private FeignSavingsHelper savingsHelper;
    private FeignSavingsTransactionHelper savingsTransactionHelper;
    private FeignGlobalConfigurationHelper globalConfigurationHelper;

    @BeforeAll
    public void setup() {
        this.clientHelper = new FeignClientHelper(fineractClient());
        this.staffHelper = new FeignStaffHelper(fineractClient());
        this.savingsProductHelper = new FeignSavingsProductHelper(fineractClient());
        this.savingsHelper = new FeignSavingsHelper(fineractClient());
        this.savingsTransactionHelper = new FeignSavingsTransactionHelper(fineractClient());
        this.globalConfigurationHelper = new FeignGlobalConfigurationHelper(fineractClient());
    }

    @Test
    public void testMakercheckerInboxList() {
        // given
        // when
        List<AuditData> makerCheckerList = retrieveCommands(Map.of());
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
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_CLIENT", false));
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("ACTIVATE_CLIENT", false));

            Long roleId = FeignRoleHelper.createRole();
            Map<String, Boolean> permissionMap = Map.of("CREATE_CLIENT", true, "CREATE_CLIENT_CHECKER", true, "ACTIVATE_CLIENT", true,
                    "ACTIVATE_CLIENT_CHECKER", true, "WITHDRAWAL_SAVINGSACCOUNT", true, "WITHDRAWAL_SAVINGSACCOUNT_CHECKER", true);
            FeignRoleHelper.addPermissionsToRole(roleId, permissionMap);
            final Long staffId = staffHelper.createStaff().getResourceId();
            // create maker user
            String maker = Utils.uniqueRandomStringGenerator("user", 8);
            final Long makerUserId = FeignUserHelper.createUser(roleId, staffId, maker, PASSWORD).getResourceId();

            // create client - maker-checker disabled
            FineractFeignClient makerClient = FineractFeignClientHelper.createNewFineractFeignClient(maker, PASSWORD);
            FeignClientHelper makerClientHelper = new FeignClientHelper(makerClient);
            Long clientId = makerClientHelper.createClient();
            assertNotNull(clientId);
            assertEquals(clientId, clientHelper.getClient(clientId).getId());

            final Long savingsId = createApproveActivateSavingsAccountDailyPosting(clientId, START_DATE_STRING);
            assertNotNull(savingsId);
            Long transactionId = savingsTransactionHelper.deposit(savingsId, "1000", TRANSACTION_DATE_STRING).getResourceId();
            assertNotNull(transactionId);

            // client and saving permission - maker-checker enabled
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("ACTIVATE_CLIENT", true));
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("WITHDRAWAL_SAVINGSACCOUNT", true));

            // create client - maker-checker enabled
            clientId = makerClientHelper.createClient();
            assertNull(clientId, "Client is created on the server");

            List<AuditData> auditDetails = retrieveCommands(
                    Map.of("actionName", "CREATE", "entityName", "CLIENT", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size(), "More than one command exists");
            Long clientCommandId = auditDetails.get(0).getId();

            // savings withdrawal - maker-checker enabled
            FeignSavingsTransactionHelper makerSavingsHelper = new FeignSavingsTransactionHelper(makerClient);
            Long withdrawalId = makerSavingsHelper.withdraw(savingsId, "100", TRANSACTION_DATE_STRING).getResourceId();
            assertNull(withdrawalId, "Withdrawal performed on the server");

            auditDetails = retrieveCommands(
                    Map.of("actionName", "WITHDRAWAL", "entityName", "SAVINGSACCOUNT", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size(), "More than one command exists");
            Long savingCommandId = auditDetails.get(0).getId();

            // check by the same user should fail
            assertEquals(400, fail(() -> approve(makerClient, clientCommandId)).getStatus());
            assertEquals(400, fail(() -> approve(makerClient, savingCommandId)).getStatus());

            // create checker user
            String checker = Utils.uniqueRandomStringGenerator("user", 8);
            final Long checkerUserId = FeignUserHelper.createUser(roleId, staffId, checker, PASSWORD).getResourceId();
            FineractFeignClient checkerClient = FineractFeignClientHelper.createNewFineractFeignClient(checker, PASSWORD);

            // check by another checker user should succeed
            CommandProcessingResult response = ok(() -> approve(checkerClient, clientCommandId));
            assertNotNull(response);
            clientId = response.getClientId();
            assertNotNull(clientId);
            assertEquals(clientId, clientHelper.getClient(clientId).getId());

            response = ok(() -> approve(checkerClient, savingCommandId));
            assertNotNull(response);
            withdrawalId = response.getResourceId();
            assertNotNull(withdrawalId);

            // add checker superuser permission - actions are performed in one step
            permissionMap = Map.of("CHECKER_SUPER_USER", true);
            FeignRoleHelper.addPermissionsToRole(roleId, permissionMap);
            clientId = makerClientHelper.createClient();
            assertNotNull(clientId);
            assertEquals(clientId, clientHelper.getClient(clientId).getId());

            withdrawalId = makerSavingsHelper.withdraw(savingsId, "100", TRANSACTION_DATE_STRING).getResourceId();
            assertNotNull(withdrawalId);
        } finally {

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));

            updatePermissions(new PutPermissionsRequest().putPermissionsItem("WITHDRAWAL_SAVINGSACCOUNT", false));
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("ACTIVATE_CLIENT", false));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "m_client", "m_group", "m_center", "m_loan", "m_office", "m_savings_account" })
    public void testRejectDatatableCreationCleansUpOrphanedTable(String apptableName) {

        // enable maker-checker globally
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(false));

        try {
            // enable maker-checker for datatable creation
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_DATATABLE", true));

            // create role with permissions for maker and checker
            Long roleId = FeignRoleHelper.createRole();
            Map<String, Boolean> permissionMap = Map.of("CREATE_DATATABLE", true, "CREATE_DATATABLE_CHECKER", true);
            FeignRoleHelper.addPermissionsToRole(roleId, permissionMap);

            // create maker user
            Long staffId = staffHelper.createStaff().getResourceId();
            String maker = Utils.uniqueRandomStringGenerator("user", 8);
            Long makerUserId = FeignUserHelper.createUser(roleId, staffId, maker, PASSWORD).getResourceId();

            // create checker user
            String checker = Utils.uniqueRandomStringGenerator("user", 8);
            FeignUserHelper.createUser(roleId, staffId, checker, PASSWORD);

            FineractFeignClient makerClient = FineractFeignClientHelper.createNewFineractFeignClient(maker, PASSWORD);

            // maker creates datatable with maker-checker enabled, this creates the physical table but queues for
            // approval
            PostDataTablesRequest datatableRequest = FeignDatatableHelper.testDatatableRequest(apptableName);
            String datatableName = datatableRequest.getDatatableName();
            new FeignDatatableHelper(makerClient).createDatatable(datatableRequest);

            // find the pending command
            List<AuditData> auditDetails = retrieveCommands(
                    Map.of("actionName", "CREATE", "entityName", "DATATABLE", "makerId", makerUserId.toString()));
            assertEquals(1, auditDetails.size(), "Error: Expected only one pending CREATE DATATABLE command");
            Long commandId = auditDetails.get(0).getId();

            // checker rejects the command which should drop the orphaned table
            FineractFeignClient checkerClient = FineractFeignClientHelper.createNewFineractFeignClient(checker, PASSWORD);
            ok(() -> checkerClient.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(commandId, "reject"));

            // verify the datatable no longer exists by trying to create it again
            // verify without maker checker, so transaction rollback in postgres doesn't break the test
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_DATATABLE", false));

            FeignDatatableHelper adminDatatableHelper = new FeignDatatableHelper(fineractClient());
            String recreatedName = adminDatatableHelper.createDatatable(datatableRequest).getResourceIdentifier();
            assertEquals(datatableName, recreatedName, "Error: Was not able to recreate datatable after rejection cleanup");

            // cleanup after test
            adminDatatableHelper.deleteDatatable(datatableName);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));

            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_DATATABLE", false));
        }
    }

    @Test
    public void testMakerCheckerUsernameFilter() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(false));

        try {
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_CLIENT", true));

            Long roleId = FeignRoleHelper.createRole();
            Map<String, Boolean> permissionMap = Map.of("CREATE_CLIENT", true, "CREATE_CLIENT_CHECKER", true, "ACTIVATE_CLIENT", true);
            FeignRoleHelper.addPermissionsToRole(roleId, permissionMap);
            final Long staffId = staffHelper.createStaff().getResourceId();

            String maker1 = Utils.uniqueRandomStringGenerator("user", 8);
            String maker2 = Utils.uniqueRandomStringGenerator("user", 8);
            FeignUserHelper.createUser(roleId, staffId, maker1, PASSWORD);
            FeignUserHelper.createUser(roleId, staffId, maker2, PASSWORD);

            new FeignClientHelper(FineractFeignClientHelper.createNewFineractFeignClient(maker1, PASSWORD)).createClient();
            new FeignClientHelper(FineractFeignClientHelper.createNewFineractFeignClient(maker2, PASSWORD)).createClient();

            List<AuditData> maker1Results = retrieveCommands(Map.of("username", maker1, "actionName", "CREATE", "entityName", "CLIENT"));
            assertEquals(1, maker1Results.size(), "Username filter should return only maker1's commands");
            assertEquals(maker1, maker1Results.get(0).getMaker());

            List<AuditData> maker2Results = retrieveCommands(Map.of("username", maker2, "actionName", "CREATE", "entityName", "CLIENT"));
            assertEquals(1, maker2Results.size(), "Username filter should return only maker2's commands");
            assertEquals(maker2, maker2Results.get(0).getMaker());

            List<AuditData> noResults = retrieveCommands(Map.of("username", "nonexistentuserxyz_999"));
            assertEquals(0, noResults.size(), "Unknown username should return no results");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_CLIENT", false));
        }
    }

    @Test
    public void testMakerCheckerDateFilterWithDayMonthYearFormat() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                new PutGlobalConfigurationsRequest().enabled(false));

        try {
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_CLIENT", true));

            Long roleId = FeignRoleHelper.createRole();
            Map<String, Boolean> permissionMap = Map.of("CREATE_CLIENT", true, "CREATE_CLIENT_CHECKER", true, "ACTIVATE_CLIENT", true);
            FeignRoleHelper.addPermissionsToRole(roleId, permissionMap);
            final Long staffId = staffHelper.createStaff().getResourceId();

            String maker = Utils.uniqueRandomStringGenerator("user", 8);
            final Long makerUserId = FeignUserHelper.createUser(roleId, staffId, maker, PASSWORD).getResourceId();

            new FeignClientHelper(FineractFeignClientHelper.createNewFineractFeignClient(maker, PASSWORD)).createClient();

            // "dd MMMM yyyy" format without dateFormat/locale — previously caused 500 error
            List<AuditData> fromOnly = retrieveCommands(Map.of("makerId", makerUserId.toString(), "makerDateTimeFrom", "01 January 2020"));
            assertEquals(1, fromOnly.size(), "'dd MMMM yyyy' from-date filter should include today's pending command");

            List<AuditData> fromAndTo = retrieveCommands(Map.of("makerId", makerUserId.toString(), "makerDateTimeFrom", "01 January 2020",
                    "makerDateTimeTo", "31 December 2030"));
            assertEquals(1, fromAndTo.size(), "'dd MMMM yyyy' date range filter should include today's pending command");

            List<AuditData> pastRange = retrieveCommands(Map.of("makerId", makerUserId.toString(), "makerDateTimeFrom", "01 January 2020",
                    "makerDateTimeTo", "31 December 2020"));
            assertEquals(0, pastRange.size(), "Past date range should exclude today's pending command");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updatePermissions(new PutPermissionsRequest().putPermissionsItem("CREATE_CLIENT", false));
        }
    }

    private List<AuditData> retrieveCommands(Map<String, Object> queryParams) {
        return ok(() -> fineractClient().makerCheckerOr4EyeFunctionality().retrieveCommandsUniversal(queryParams));
    }

    private static CommandProcessingResult approve(FineractFeignClient client, Long commandId) {
        return client.makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(commandId, "approve");
    }

    private void updatePermissions(PutPermissionsRequest request) {
        ok(() -> fineractClient().permissions().updatePermissions(request));
    }

    private Long createSavingsProductDailyPosting() {
        return savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                        SavingsTestData.InterestPostingPeriodType.DAILY, SavingsTestData.InterestCalculationType.DAILY_BALANCE))
                .getResourceId();
    }

    private Long createApproveActivateSavingsAccountDailyPosting(final Long clientID, final String startDate) {
        final Long savingsProductID = createSavingsProductDailyPosting();
        assertNotNull(savingsProductID);
        return savingsHelper.createApproveActivateSavings(clientID, savingsProductID, startDate);
    }
}
