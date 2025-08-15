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

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutLoansApprovedAmountRequest;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

public class UserLoanPermissionTest extends BaseLoanIntegrationTest {

    Long clientId;
    Long loanProductId;
    private Long loanId;

    @BeforeEach
    public void setup() {
        if (clientId == null) {
            clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        }
        if (loanProductId == null) {
            loanProductId = loanProductHelper.createLoanProduct(create4IProgressiveWithCapitalizedIncome()
                    .addSupportedInterestRefundTypesItem(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND)
                    .overAppliedCalculationType(null).overAppliedNumber(null).allowApprovedDisbursedAmountsOverApplied(false)
                    .enableBuyDownFee(true).buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                    .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                    .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE)
                    .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())
                    .receivableFeeAccountId(feeReceivableAccount.getAccountID().longValue())
                    .receivablePenaltyAccountId(penaltyReceivableAccount.getAccountID().longValue())
                    .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue())
                    .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue())
                    .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())).getResourceId();
        }
        runAt("1 January 2025", () -> {

            PostLoansResponse postLoansResponse = loanTransactionHelper
                    .applyLoan(applyLP2ProgressiveLoanRequest(clientId, loanProductId, "1 January 2025", 10000.0, 12.0, 4, null));

            loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(), approveLoanRequest(2000.0, "1 January 2025"));

            loanId = postLoansResponse.getResourceId();
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "1 January 2025");
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
            final Long merchantIssuedRefundId = loanTransactionHelper
                    .makeMerchantIssuedRefund(loanId,
                            new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).locale("en")
                                    .transactionDate("01 February 2025").transactionAmount(100.0D).interestRefundCalculation(false))
                    .getResourceId();

            performPermissionTestForRequest("MANUAL_INTEREST_REFUND_TRANSACTION_LOAN",
                    fineractClient -> fineractClient.loanTransactions.adjustLoanTransaction(loanId, merchantIssuedRefundId,
                            new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).locale("en")
                                    .transactionAmount(1.20D),
                            "interest-refund"));
        });
    }

    @Test
    public void testUpdateApprovedAmountPermission() {
        runAt("1 January 2025", () -> {
            // disbursement should be rejected upon validation error
            Response<PostLoansLoanIdResponse> response = Calls.executeU(
                    fineractClient().loans.stateTransitions(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2025")
                            .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(2000.0)).locale("en"), "disburse"));

            Assertions.assertEquals(403, response.code());

            // update approved amount
            performPermissionTestForRequest("UPDATE_APPROVED_AMOUNT_LOAN",
                    fineractClient -> fineractClient.loans.modifyLoanApprovedAmount(loanId,
                            new PutLoansApprovedAmountRequest().amount(BigDecimal.valueOf(4000.0d)).locale("en")));

            // disbursement should be performed without error
            Calls.ok(fineractClient().loans.stateTransitions(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2025")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(2000.0)).locale("en"), "disburse"));
        });
    }

    @Test
    public void testContractTerminationAndUndoContractTerminationPermission() {

        runAt("2 January 2025", () -> {
            performPermissionTestForRequest("CONTRACT_TERMINATION_LOAN", fineractClient -> fineractClient.loans.stateTransitions(loanId,
                    new PostLoansLoanIdRequest().note(""), "contractTermination"));

            performPermissionTestForRequest("CONTRACT_TERMINATION_UNDO_LOAN",
                    fineractClient -> fineractClient.loans.stateTransitions(loanId,
                            new PostLoansLoanIdRequest().note("Contract Termination Undo Test Note"), "undoContractTermination"));
        });
    }
}
