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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests to verify that office hierarchy access control is properly enforced on money-moving operations
 * (transfers, withdrawals, deposits) and single-account reads.
 *
 * This test class addresses the security vulnerability where a low-privileged user in one office could perform
 * operations on accounts belonging to a different office (cross-office financial fraud).
 *
 * Test Strategy: 1. Setup multiple offices (Office A, Office B, and Office C as a child of A). 2. Create a restricted
 * user (Teller A) in Office A with standard transaction permissions. 3. Create savings accounts in each office. 4.
 * Attempt cross-office operations as Teller A and assert they fail with 403 Forbidden. 5. Attempt same-office and
 * parent-child operations as Teller A and assert they succeed (200 OK).
 */
public class OfficeHierarchyAccessControlTest {

    private static final Logger LOG = LoggerFactory.getLogger(OfficeHierarchyAccessControlTest.class);

    private RequestSpecification adminSpec;
    private ResponseSpec responseSpec200;
    private ResponseSpec responseSpec403;

    private OfficeHelper officeHelper;
    private SavingsProductHelper savingsProductHelper;

    private Integer officeAId;
    private Integer officeBId;
    private Integer officeCId; // Child of Office A

    private Integer roleId;
    private Integer userId;

    private Integer savingsProductId;
    private Integer savingsAccountAId;
    private Integer savingsAccountBId;
    private Integer savingsAccountCId;

    @BeforeEach
    public void setup() {
        // Initialize RestAssured and Spring context for integration tests
        Utils.initializeRESTAssured();

        // Admin spec for initial setup (authenticated as 'mifos' by default via Utils)
        this.adminSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).setHeader("Fineract-Platform-TenantId", "default")
                .build();

        this.responseSpec200 = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec403 = new ResponseSpecBuilder().expectStatusCode(403).build();

        this.officeHelper = new OfficeHelper(this.adminSpec, this.responseSpec200);
        this.savingsProductHelper = new SavingsProductHelper();

        // 1. Create Offices
        this.officeAId = this.officeHelper.createOffice("OfficeA-" + System.currentTimeMillis());
        this.officeBId = this.officeHelper.createOffice("OfficeB-" + System.currentTimeMillis());
        // Create Office C as a child of Office A to test hierarchy inheritance
        this.officeCId = this.officeHelper.createOffice("OfficeC-" + System.currentTimeMillis(), this.officeAId);

        // 2. Create Savings Product
        this.savingsProductId = createSavingsProduct();

        // 3. Create Role with specific branch-level permissions
        List<String> permissions = new ArrayList<>();
        permissions.add("CREATE_ACCOUNTTRANSFER");
        permissions.add("WITHDRAWAL_SAVINGSACCOUNT");
        permissions.add("DEPOSIT_SAVINGSACCOUNT");
        permissions.add("READ_SAVINGSACCOUNT");
        permissions.add("APPROVE_SAVINGSACCOUNT");
        permissions.add("ACTIVATE_SAVINGSACCOUNT");
        permissions.add("CREATE_SAVINGSACCOUNT");
        permissions.add("READ_CLIENT");

        this.roleId = RolesHelper.createRole(this.adminSpec, this.responseSpec200, "TellerRole-" + System.currentTimeMillis(), permissions);

        // 4. Create User in Office A
        this.userId = UserHelper.createUser(this.adminSpec, this.responseSpec200, this.roleId, this.officeAId, "tellerA", "password1");

        // 5. Create and Activate Savings Accounts in each office using Admin
        this.savingsAccountAId = createAndActivateSavingsAccount(this.savingsProductId, this.officeAId);
        this.savingsAccountBId = createAndActivateSavingsAccount(this.savingsProductId, this.officeBId);
        this.savingsAccountCId = createAndActivateSavingsAccount(this.savingsProductId, this.officeCId);
    }

    private Integer createSavingsProduct() {
        String productName = "SavingsProduct-" + System.currentTimeMillis();
        // Note: Adjust helper method signature if your Fineract version requires additional parameters
        return this.savingsProductHelper.createSavingsProduct(productName, this.adminSpec, this.responseSpec200);
    }

    private Integer createAndActivateSavingsAccount(Integer productId, Integer officeId) {
        // Create a client in the specific office
        Integer clientId = ClientHelper.createClient(this.adminSpec, this.responseSpec200, officeId);

        // Apply for savings account
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(this.adminSpec, this.responseSpec200);
        Integer savingsId = savingsAccountHelper.applyForSavingsApplication(clientId, productId, "01 January 2020");

        // Approve and activate the account
        savingsAccountHelper.approveSavings(savingsId, "01 January 2020");
        savingsAccountHelper.activateSavings(savingsId, "01 January 2020");

        return savingsId;
    }

    /**
     * Helper to create an authenticated RequestSpecification for a specific user.
     */
    private RequestSpecification getAuthenticatedSpec(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return new RequestSpecBuilder().setContentType(ContentType.JSON).setHeader("Fineract-Platform-TenantId", "default")
                .setHeader("Authorization", "Basic " + encodedCredentials).build();
    }

    // ==========================================
    // SECURITY TESTS: CROSS-OFFICE OPERATIONS
    // ==========================================

    @Test
    public void testCrossOfficeWithdrawalIsBlocked() {
        LOG.info("Testing cross-office withdrawal...");
        RequestSpecification tellerASpec = getAuthenticatedSpec("tellerA", "password1");

        // We expect a 403 Forbidden for cross-office operations
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(tellerASpec, this.responseSpec403);

        // Attempt withdrawal from Office B's account as Teller A (who belongs to Office A)
        // If the vulnerability exists, the server returns 200, and RestAssured will throw an AssertionError
        // because it expected 403.
        savingsAccountHelper.withdrawal(this.savingsAccountBId, "100.0", "01 January 2023");
    }

    @Test
    public void testCrossOfficeDepositIsBlocked() {
        LOG.info("Testing cross-office deposit...");
        RequestSpecification tellerASpec = getAuthenticatedSpec("tellerA", "password1");
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(tellerASpec, this.responseSpec403);

        // Attempt deposit to Office B's account as Teller A
        savingsAccountHelper.deposit(this.savingsAccountBId, "100.0", "01 January 2023");
    }

    @Test
    public void testCrossOfficeTransferIsBlocked() {
        LOG.info("Testing cross-office transfer...");
        RequestSpecification tellerASpec = getAuthenticatedSpec("tellerA", "password1");

        // Attempt to transfer funds from Office B's account to Office A's account
        String transferJson = String.format(
                "{\"fromAccountId\":%d,\"fromAccountType\":2,\"toAccountId\":%d,\"toAccountType\":2,\"transferAmount\":100.0,\"transferDate\":\"01 January 2023\",\"fromOfficeId\":%d,\"toOfficeId\":%d}",
                this.savingsAccountBId, this.savingsAccountAId, this.officeBId, this.officeAId);

        io.restassured.RestAssured.given().spec(tellerASpec).body(transferJson).expect().spec(this.responseSpec403) // Must
                                                                                                                    // fail
                                                                                                                    // with
                                                                                                                    // 403
                .when().post("/fineract-provider/api/v1/accounttransfers");
    }

    @Test
    public void testCrossOfficeReadIsBlocked() {
        LOG.info("Testing cross-office single-account read...");
        RequestSpecification tellerASpec = getAuthenticatedSpec("tellerA", "password1");

        // Attempt to read Office B's account details as Teller A
        // This addresses the secondary vulnerability (retrieveOne lacking office filter)
        io.restassured.RestAssured.given().spec(tellerASpec).expect().spec(this.responseSpec403) // Or 404 depending on
                                                                                                 // exact
                                                                                                 // implementation, but
                                                                                                 // 403 is expected for
                                                                                                 // access denied
                .when().get("/fineract-provider/api/v1/savingsaccounts/" + this.savingsAccountBId);
    }

    // ==========================================
    // FUNCTIONAL TESTS: VALID OPERATIONS
    // ==========================================

    @Test
    public void testSameOfficeOperationsSucceed() {
        LOG.info("Testing same-office operations...");
        RequestSpecification tellerASpec = getAuthenticatedSpec("tellerA", "password1");

        // Expect 200 OK for operations within the user's own office
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(tellerASpec, this.responseSpec200);

        // Deposit to Office A's account as Teller A -> Should succeed
        savingsAccountHelper.deposit(this.savingsAccountAId, "100.0", "01 January 2023");

        // Withdrawal from Office A's account as Teller A -> Should succeed
        savingsAccountHelper.withdrawal(this.savingsAccountAId, "50.0", "02 January 2023");
    }

    @Test
    public void testParentChildOfficeHierarchySucceeds() {
        LOG.info("Testing parent-child office hierarchy operations...");
        RequestSpecification tellerASpec = getAuthenticatedSpec("tellerA", "password1");

        // Expect 200 OK because Office C is a child of Office A (hierarchy match)
        SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(tellerASpec, this.responseSpec200);

        // Deposit to Office C's account as Teller A -> Should succeed
        savingsAccountHelper.deposit(this.savingsAccountCId, "100.0", "01 January 2023");

        // Withdrawal from Office C's account as Teller A -> Should succeed
        savingsAccountHelper.withdrawal(this.savingsAccountCId, "50.0", "02 January 2023");
    }
}
