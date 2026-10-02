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
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.commands.MakercheckersHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.organisation.StaffHelper;
import org.apache.fineract.integrationtests.useradministration.roles.RolesHelper;
import org.apache.fineract.integrationtests.useradministration.users.UserHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for the CBS-509 / FINERACT-2688 fix: maker-checker support for checker-only users on loan
 * disbursement.
 *
 * <p>
 * Covers the complete flow:
 * <ol>
 * <li>Maker submits disbursement → queued as AWAITING_APPROVAL (HTTP 200, loanId null)</li>
 * <li>Maker submits again → HTTP 403 duplicate.pending.submission</li>
 * <li>Checker-only user submits for loan with no pending entry → HTTP 403 checker.only.cannot.initiate</li>
 * <li>Checker-only user submits for loan A (has pending entry) → auto-approves and disburses (loanId not null)</li>
 * <li>Traditional /makerchecker/{id}?command=approve flow still works correctly</li>
 * </ol>
 */
public class LoanDisbursementMakerCheckerIntegrationTest {

    private static final String DISBURSAL_DATE = "01 January 2024";
    private static final String LOAN_SUBMISSION_DATE = "01 January 2024";
    private static final String LOAN_TERM_FREQUENCY = "12";
    private static final String LOAN_REPAYMENT_PERIOD = "1";
    private static final String LOAN_PRINCIPAL = "10000.00";
    private static final String DISBURSE_LOAN_PERMISSION = "DISBURSE_LOAN";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private MakercheckersHelper makercheckersHelper;
    private RolesHelper rolesHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
        this.makercheckersHelper = new MakercheckersHelper(this.requestSpec, this.responseSpec);
        this.rolesHelper = new RolesHelper();
    }

    @Test
    public void testLoanDisbursementMakerCheckerFlow() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));
            // Enable maker-checker for loan disbursement
            rolesHelper.updatePermissions(new PutPermissionsRequest().putPermissionsItem(DISBURSE_LOAN_PERMISSION, true));

            // Maker role: base permission only (no checker permission)
            Integer makerRoleId = RolesHelper.createRole(requestSpec, responseSpec);
            RolesHelper.addPermissionsToRole(requestSpec, responseSpec, makerRoleId, Map.of(DISBURSE_LOAN_PERMISSION, true));

            // Checker-only role: checker permission only (no base permission)
            Integer checkerRoleId = RolesHelper.createRole(requestSpec, responseSpec);
            RolesHelper.addPermissionsToRole(requestSpec, responseSpec, checkerRoleId, Map.of(DISBURSE_LOAN_PERMISSION + "_CHECKER", true));

            Integer staffId = StaffHelper.createStaff(requestSpec, responseSpec);
            String makerUsername = Utils.uniqueRandomStringGenerator("mkr", 8);
            Integer makerUserId = (Integer) UserHelper.createUser(requestSpec, responseSpec, makerRoleId, staffId, makerUsername,
                    "A1b2c3d4e5f$", "resourceId");
            String checkerUsername = Utils.uniqueRandomStringGenerator("ckr", 8);
            UserHelper.createUser(requestSpec, responseSpec, checkerRoleId, staffId, checkerUsername, "A1b2c3d4e5f$", "resourceId");

            RequestSpecification makerRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build().header(
                    "Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(makerUsername, "A1b2c3d4e5f$"));
            RequestSpecification checkerRequestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build().header(
                    "Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey(checkerUsername, "A1b2c3d4e5f$"));

            // Admin creates product, client, and three approved loans
            Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
            assertNotNull(clientId);
            Integer loanProductId = loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().withPrincipal(LOAN_PRINCIPAL)
                    .withRepaymentTypeAsMonth().withRepaymentAfterEvery(LOAN_REPAYMENT_PERIOD).withNumberOfRepayments(LOAN_TERM_FREQUENCY)
                    .withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment()
                    .withInterestTypeAsFlat().build(null));

            Integer loanIdA = createAndApproveLoan(clientId, loanProductId);
            Integer loanIdB = createAndApproveLoan(clientId, loanProductId);
            Integer loanIdC = createAndApproveLoan(clientId, loanProductId);

            LoanTransactionHelper makerLoanHelper = new LoanTransactionHelper(makerRequestSpec, responseSpec);
            LoanTransactionHelper checkerLoanHelper = new LoanTransactionHelper(checkerRequestSpec, responseSpec);
            ResponseSpecification forbiddenSpec = new ResponseSpecBuilder().expectStatusCode(403).build();

            // Scenario 1: Maker submits disbursement for loan A → queued as AWAITING_APPROVAL
            HashMap makerResponseA = makerLoanHelper.disburseLoan(DISBURSAL_DATE, loanIdA, LOAN_PRINCIPAL, null);
            assertNull(makerResponseA.get("loanId"), "Disbursement should be queued for checker approval, not immediately processed");

            List<Map<String, Object>> pendingEntries = makercheckersHelper
                    .getMakerCheckerList(Map.of("actionName", "DISBURSE", "entityName", "LOAN", "makerId", makerUserId.toString()));
            assertEquals(1, pendingEntries.size(), "Exactly one pending DISBURSE_LOAN command should exist");

            // Scenario 2: Maker submits again for loan A → 403 duplicate pending submission
            Object duplicateResult = makerLoanHelper.disburseLoanWithTransactionAmount(DISBURSAL_DATE, loanIdA, LOAN_PRINCIPAL,
                    forbiddenSpec);
            List<Map<String, Object>> duplicateErrors = (List<Map<String, Object>>) duplicateResult;
            assertEquals("error.msg.maker.checker.duplicate.pending.submission",
                    duplicateErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE), "Duplicate submission should be rejected");

            // Scenario 3: Checker-only user submits for loan B (no pending entry) → 403 cannot initiate
            Object noEntryResult = checkerLoanHelper.disburseLoanWithTransactionAmount(DISBURSAL_DATE, loanIdB, LOAN_PRINCIPAL,
                    forbiddenSpec);
            List<Map<String, Object>> noEntryErrors = (List<Map<String, Object>>) noEntryResult;
            assertEquals("error.msg.maker.checker.checker.only.cannot.initiate",
                    noEntryErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE),
                    "Checker-only user cannot initiate a new disbursement");

            // Scenario 4: Checker-only user submits for loan A (has pending entry) → auto-approves and disburses
            HashMap checkerResponseA = checkerLoanHelper.disburseLoan(DISBURSAL_DATE, loanIdA, LOAN_PRINCIPAL, null);
            assertNotNull(checkerResponseA.get("loanId"), "Loan A should be disbursed after checker-only user auto-approval");

            // Scenario 5: Traditional /makerchecker/{id}?command=approve flow is unaffected
            // Maker queues disbursement for loan C
            HashMap makerResponseC = makerLoanHelper.disburseLoan(DISBURSAL_DATE, loanIdC, LOAN_PRINCIPAL, null);
            assertNull(makerResponseC.get("loanId"), "Loan C disbursement should be queued");

            List<Map<String, Object>> pendingEntriesC = makercheckersHelper.getMakerCheckerList(Map.of("actionName", "DISBURSE",
                    "entityName", "LOAN", "makerId", makerUserId.toString(), "resourceId", loanIdC.toString()));
            assertEquals(1, pendingEntriesC.size(), "Exactly one pending entry for loan C");
            Long pendingCommandIdC = ((Double) pendingEntriesC.get(0).get("id")).longValue();

            // Admin approves via the existing /makerchecker/{id}?command=approve endpoint
            HashMap approveResult = MakercheckersHelper.approveMakerCheckerEntry(requestSpec, responseSpec, pendingCommandIdC);
            assertNotNull(approveResult);
            assertNotNull(approveResult.get("loanId"), "Loan C should be disbursed after traditional checker approval");

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER,
                    new PutGlobalConfigurationsRequest().enabled(true));
            rolesHelper.updatePermissions(new PutPermissionsRequest().putPermissionsItem(DISBURSE_LOAN_PERMISSION, false));
        }
    }

    private Integer createAndApproveLoan(Integer clientId, Integer loanProductId) {
        String loanJson = new LoanApplicationTestBuilder().withPrincipal(LOAN_PRINCIPAL).withLoanTermFrequency(LOAN_TERM_FREQUENCY)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(LOAN_TERM_FREQUENCY).withRepaymentEveryAfter(LOAN_REPAYMENT_PERIOD)
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(DISBURSAL_DATE).withSubmittedOnDate(LOAN_SUBMISSION_DATE).withLoanType("individual")
                .build(clientId.toString(), loanProductId.toString(), null);
        Integer loanId = loanTransactionHelper.getLoanId(loanJson);
        assertNotNull(loanId);
        loanTransactionHelper.approveLoan(DISBURSAL_DATE, loanId);
        return loanId;
    }
}
