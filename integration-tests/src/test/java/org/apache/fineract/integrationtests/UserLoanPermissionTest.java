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

import java.math.BigDecimal;
import java.util.function.Function;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostRolesRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.client.models.PutLoansApprovedAmountRequest;
import org.apache.fineract.client.models.PutRolesRoleIdPermissionsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.SupportedInterestRefundTypesItem;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserLoanPermissionTest extends FeignLoanTestBase {

    private static final String TEST_USER_PASSWORD = "AKleRbDhK421$";
    private static final Long HEAD_OFFICE_ID = 1L;

    Long clientId;
    Long loanProductId;
    private Long loanId;

    @BeforeEach
    public void setup() {
        if (clientId == null) {
            clientId = createClient();
        }
        if (loanProductId == null) {
            loanProductId = createLoanProduct(create4IProgressiveWithCapitalizedIncome()
                    .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND)
                    .overAppliedCalculationType(null).overAppliedNumber(null).allowApprovedDisbursedAmountsOverApplied(false)
                    .enableBuyDownFee(true).buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .receivableInterestAccountId(getAccounts().getInterestReceivableAccount().getAccountID().longValue())
                    .receivableFeeAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())
                    .receivablePenaltyAccountId(getAccounts().getPenaltyReceivableAccount().getAccountID().longValue())
                    .buyDownExpenseAccountId(getAccounts().getBuyDownExpenseAccount().getAccountID().longValue())
                    .incomeFromBuyDownAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())
                    .deferredIncomeLiabilityAccountId(getAccounts().getDeferredIncomeLiabilityAccount().getAccountID().longValue()));
        }
        runAt("1 January 2025", () -> {
            loanId = applyForLoan(applyLP2ProgressiveLoanRequest(clientId, loanProductId, "1 January 2025", 10000.0, 12.0, 4, null));

            approveLoan(loanId, approveLoanRequest(2000.0, "1 January 2025"));

            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(1000.0, "1 January 2025"));
        });
    }

    @Test
    public void testCapitalizedIncomeAndCapitalizedIncomeAdjustmentPermissions() {
        runAt("1 January 2025", () -> {
            Long capitalizedIncomeId = makeLoanTransactionWithPermissionVerification(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).locale("en").transactionAmount(50.0).transactionDate("01 January 2025"),
                    "capitalizedIncome", "CAPITALIZEDINCOME_LOAN").getResourceId();

            adjustLoanTransactionWithPermissionVerification(
                    loanId, capitalizedIncomeId, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                            .locale("en").transactionAmount(50.0).transactionDate("1 January 2025"),
                    "capitalizedIncomeAdjustment", "CAPITALIZEDINCOMEADJUSTMENT_LOAN");

        });

    }

    @Test
    public void testBuyDownFeeAndBuyDownFeeAdjustmentPermissions() {
        runAt("1 January 2025", () -> {
            final Long buyDownFeeTransactionId = makeLoanTransactionWithPermissionVerification(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("01 January 2025").locale("en")
                            .transactionAmount(100.0d),
                    "buyDownFee", "BUYDOWNFEE_LOAN").getResourceId();

            adjustLoanTransactionWithPermissionVerification(
                    loanId, buyDownFeeTransactionId, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                            .transactionDate("01 January 2025").locale("en").transactionAmount(100.0d),
                    "buyDownFeeAdjustment", "BUYDOWNFEEADJUSTMENT_LOAN");
        });
    }

    @Test
    public void testManualInterestRefundPermission() {
        runAt("1 February 2025", () -> {
            final Long merchantIssuedRefundId = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).locale("en").transactionDate("01 February 2025")
                            .transactionAmount(100.0D).interestRefundCalculation(false))
                    .getResourceId();

            performPermissionTestForRequest("MANUAL_INTEREST_REFUND_TRANSACTION_LOAN",
                    fineractClient -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, merchantIssuedRefundId,
                            new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).locale("en")
                                    .transactionAmount(1.20D),
                            "interest-refund"));
        });
    }

    @Test
    public void testUpdateApprovedAmountPermission() {
        runAt("1 January 2025", () -> {
            // disbursement should be rejected upon validation error
            CallFailedRuntimeException exception = fail(
                    () -> fineractClient().loans()
                            .handleCommandsLoan(
                                    loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2025")
                                            .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(2000.0)).locale("en"),
                                    "disburse"));
            assertEquals(403, exception.getStatus());

            // update approved amount
            performPermissionTestForRequest("UPDATE_APPROVED_AMOUNT_LOAN",
                    fineractClient -> fineractClient.loans().updateApprovedAmountLoan(loanId,
                            new PutLoansApprovedAmountRequest().amount(BigDecimal.valueOf(4000.0d)).locale("en")));

            // disbursement should be performed without error
            ok(() -> fineractClient().loans().handleCommandsLoan(loanId,
                    new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2025").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(2000.0)).locale("en"),
                    "disburse"));
        });
    }

    @Test
    public void testContractTerminationAndUndoContractTerminationPermission() {

        runAt("2 January 2025", () -> {
            performPermissionTestForRequest("CONTRACT_TERMINATION_LOAN", fineractClient -> fineractClient.loans().handleCommandsLoan(loanId,
                    new PostLoansLoanIdRequest().note(""), "contractTermination"));

            performPermissionTestForRequest("CONTRACT_TERMINATION_UNDO_LOAN",
                    fineractClient -> fineractClient.loans().handleCommandsLoan(loanId,
                            new PostLoansLoanIdRequest().note("Contract Termination Undo Test Note"), "undoContractTermination"));
        });
    }

    /**
     * Executes a loan transaction request via a freshly created user that lacks the given permission (asserting a 403),
     * then grants the permission and re-executes it (asserting success). Returns the successful response body.
     */
    private PostLoansLoanIdTransactionsResponse makeLoanTransactionWithPermissionVerification(final Long loanId,
            final PostLoansLoanIdTransactionsRequest postLoansLoanIdTransactionsRequest, final String command, final String permission) {
        return performPermissionTestForRequest(permission, fineractClient -> fineractClient.loanTransactions()
                .handleCommandsLoanTransaction(loanId, postLoansLoanIdTransactionsRequest, command));
    }

    /**
     * Executes a loan transaction adjustment via a freshly created user that lacks the given permission (asserting a
     * 403), then grants the permission and re-executes it (asserting success).
     */
    private void adjustLoanTransactionWithPermissionVerification(final Long loanId, final Long transactionIdToAdjust,
            final PostLoansLoanIdTransactionsTransactionIdRequest postLoansLoanIdTransactionsRequest, final String command,
            final String permission) {
        performPermissionTestForRequest(permission, fineractClient -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                transactionIdToAdjust, postLoansLoanIdTransactionsRequest, command));
    }

    /**
     * Creates a role with the given permission disabled and a user holding that role. Runs {@code callback} as that
     * user, asserting it fails with a 403; then enables the permission and runs it again, asserting success and
     * returning the response body.
     */
    private <T> T performPermissionTestForRequest(final String permission, final Function<FineractFeignClient, T> callback) {
        final FineractFeignClient adminClient = fineractClient();

        final String roleName = Utils.uniqueRandomStringGenerator("TEST_ROLE_", 10);
        final Long roleId = ok(
                () -> adminClient.roles().createRole(new PostRolesRequest().name(roleName).description("Test role Description")))
                .getResourceId();
        ok(() -> adminClient.roles().updateRolePermissions(roleId,
                new PutRolesRoleIdPermissionsRequest().putPermissionsItem(permission, false)));

        final String firstname = Utils.randomFirstNameGenerator();
        final String lastname = Utils.randomLastNameGenerator();
        final String userName = Utils.uniqueRandomStringGenerator("testUserName", 4);
        final String email = firstname + "." + lastname + "@whatever.mifos.org";
        ok(() -> adminClient.users()
                .createUser(new PostUsersRequest().addRolesItem(roleId).email(email).firstname(firstname).lastname(lastname)
                        .repeatPassword(TEST_USER_PASSWORD).sendPasswordToEmail(false).officeId(HEAD_OFFICE_ID).username(userName)
                        .password(TEST_USER_PASSWORD)));

        final FineractFeignClient userClient = FineractFeignClientHelper.createNewFineractFeignClient(userName, TEST_USER_PASSWORD);

        // try to make transaction - should fail
        final CallFailedRuntimeException exception = fail(() -> callback.apply(userClient));
        assertEquals(403, exception.getStatus());

        // edit role to have permission for transaction
        ok(() -> adminClient.roles().updateRolePermissions(roleId,
                new PutRolesRoleIdPermissionsRequest().putPermissionsItem(permission, true)));

        // try to make transaction - should pass
        final T grantedResponse = callback.apply(userClient);
        // A non-2xx status already throws, but a 2xx with no body would mean the command never ran. The pre-migration
        // test asserted the status was exactly 200; asserting a body is present is the typed-client equivalent.
        assertNotNull(grantedResponse, "Expected a response body once '" + permission + "' was granted, but the command returned none");
        return grantedResponse;
    }
}
