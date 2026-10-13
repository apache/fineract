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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.AuditData;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PutPermissionsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRoleHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * FINERACT-2688: maker-checker support for checker-only users on loan disbursement.
 * <ol>
 * <li>Maker submits a disbursement: it is queued as awaiting approval</li>
 * <li>Maker submits it again: rejected as a duplicate pending submission</li>
 * <li>Checker-only user disburses a loan with no pending submission: rejected</li>
 * <li>Checker-only user disburses the loan with a pending submission: the pending submission is approved</li>
 * <li>The /makercheckers/{id}?command=approve flow is unaffected</li>
 * </ol>
 */
public class LoanDisbursementMakerCheckerIntegrationTest extends FeignLoanTestBase {

    private static final String DISBURSE_LOAN_PERMISSION = "DISBURSE_LOAN";
    private static final String PASSWORD = "A1b2c3d4e5f$";
    private static final double PRINCIPAL = 1000.0;

    @Test
    public void testLoanDisbursementMakerCheckerFlow() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long clientId = createClient();
        Long productId = createLoanProduct(onePeriod30DaysNoInterest());
        Long loanIdA = loanHelper.applyAndApproveLoan(clientId, productId, today, PRINCIPAL, 1).getLoanId();
        Long loanIdB = loanHelper.applyAndApproveLoan(clientId, productId, today, PRINCIPAL, 1).getLoanId();
        Long loanIdC = loanHelper.applyAndApproveLoan(clientId, productId, today, PRINCIPAL, 1).getLoanId();

        String makerUsername = Utils.uniqueRandomStringGenerator("mkr", 8);
        Long makerUserId = createUser(makerUsername, DISBURSE_LOAN_PERMISSION);
        String checkerUsername = Utils.uniqueRandomStringGenerator("ckr", 8);
        createUser(checkerUsername, DISBURSE_LOAN_PERMISSION + "_CHECKER");
        FineractFeignClient maker = FineractFeignClientHelper.createNewFineractFeignClient(makerUsername, PASSWORD);
        FineractFeignClient checker = FineractFeignClientHelper.createNewFineractFeignClient(checkerUsername, PASSWORD);

        try {
            enableMakerCheckerForDisbursement(true);

            assertNull(ok(() -> disburse(maker, loanIdA, today)).getLoanId(), "Maker disbursement must be queued for approval");
            assertEquals(1, pendingDisbursements(loanIdA, makerUserId).size());

            assertRejected(() -> disburse(maker, loanIdA, today), "error.msg.maker.checker.duplicate.pending.submission");

            assertRejected(() -> disburse(checker, loanIdB, today), "error.msg.maker.checker.checker.only.cannot.initiate");

            assertNotNull(ok(() -> disburse(checker, loanIdA, today)).getLoanId(),
                    "Checker-only user must approve the pending disbursement");
            verifyLoanStatus(getLoanDetails(loanIdA), GetLoansLoanIdStatus::getActive);

            assertNull(ok(() -> disburse(maker, loanIdC, today)).getLoanId(), "Maker disbursement must be queued for approval");
            List<AuditData> pendingC = pendingDisbursements(loanIdC, makerUserId);
            assertEquals(1, pendingC.size());
            ok(() -> fineractClient().makerCheckerOr4EyeFunctionality().approveMakerCheckerEntry(pendingC.get(0).getId(), "approve"));
            verifyLoanStatus(getLoanDetails(loanIdC), GetLoansLoanIdStatus::getActive);
        } finally {
            enableMakerCheckerForDisbursement(false);
        }
    }

    private void enableMakerCheckerForDisbursement(boolean enabled) {
        globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.MAKER_CHECKER, enabled);
        globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.ENABLE_SAME_MAKER_CHECKER, false);
        ok(() -> fineractClient().permissions()
                .updatePermissionsDetails(new PutPermissionsRequest().putPermissionsItem(DISBURSE_LOAN_PERMISSION, enabled)));
    }

    private Long createUser(String username, String permission) {
        Long roleId = FeignRoleHelper.createRole();
        FeignRoleHelper.addPermissionsToRole(roleId, Map.of(permission, true, "READ_LOAN", true));
        return FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname("Test").lastname("User")
                .email("test@localhost").officeId(FeignOfficeHelper.HEAD_OFFICE_ID).roles(List.of(roleId)).password(PASSWORD)
                .repeatPassword(PASSWORD).sendPasswordToEmail(false)).getResourceId();
    }

    private static PostLoansLoanIdResponse disburse(FineractFeignClient client, Long loanId, String date) {
        return client.loans()
                .handleCommandsLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(date)
                        .transactionAmount(BigDecimal.valueOf(PRINCIPAL)).locale("en").dateFormat("dd MMMM yyyy"),
                        Map.of("command", "disburse"));
    }

    private List<AuditData> pendingDisbursements(Long loanId, Long makerId) {
        return ok(() -> fineractClient().makerCheckerOr4EyeFunctionality().retrieveCommandsUniversal(
                Map.of("actionName", "DISBURSE", "entityName", "LOAN", "resourceId", loanId, "makerId", makerId), Map.of()));
    }

    private static void assertRejected(Runnable call, String errorCode) {
        CallFailedRuntimeException failure = FeignCalls.failVoid(call);
        assertEquals(403, failure.getStatus());
        assertTrue(failure.getResponseBody().contains(errorCode), "Expected " + errorCode + " but was: " + failure.getResponseBody());
    }
}
