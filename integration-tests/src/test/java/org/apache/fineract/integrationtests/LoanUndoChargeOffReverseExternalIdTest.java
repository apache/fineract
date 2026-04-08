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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanUndoChargeOffReverseExternalIdTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private Account assetAccount;
    private Account incomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.assetAccount = this.accountHelper.createAssetAccount();
        this.incomeAccount = this.accountHelper.createIncomeAccount();
        this.expenseAccount = this.accountHelper.createExpenseAccount();
        this.overpaymentAccount = this.accountHelper.createLiabilityAccount();
    }

    @Test
    public void loanUndoChargeOffReverseExternalIdTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("6 September 2022").locale("en")
                        .transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // undo charge-off
        String reverseTransactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse undoChargeOffTxResponse = loanTransactionHelper.undoChargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().reversalExternalId(reverseTransactionExternalId));
        assertNotNull(undoChargeOffTxResponse);

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertFalse(loanDetails.getChargedOff());

        GetLoansLoanIdTransactionsTransactionIdResponse chargeOffTransactionDetails = loanTransactionHelper
                .getLoanTransactionDetails((long) loanId, transactionExternalId);
        assertNotNull(chargeOffTransactionDetails);
        assertTrue(chargeOffTransactionDetails.getManuallyReversed());
        assertEquals(reverseTransactionExternalId, chargeOffTransactionDetails.getReversalExternalId());
    }

    /**
     * Test scenario: - Charge-off is performed. - Charge-off is then undone. - A new charge-off is performed with an
     * earlier transaction date. This verifies that reversed transactions are properly excluded so that the new
     * charge-off is allowed.
     */
    @Test
    public void loanChargeOffAfterUndoWithEarlierDateTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // make Repayment
        final PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("28 March 2025").locale("en")
                        .transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = this.loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // Perform first charge-off with date "29 March 2025"
        String randomText1 = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId1 = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText1, 1);
        String transactionExternalId1 = UUID.randomUUID().toString();
        loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("29 March 2025")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId1).chargeOffReasonId((long) chargeOffReasonId1));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // Undo the charge-off
        String reverseTransactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse undoChargeOffTxResponse = loanTransactionHelper.undoChargeOffLoan((long) loanId,
                new PostLoansLoanIdTransactionsRequest().reversalExternalId(reverseTransactionExternalId));
        assertNotNull(undoChargeOffTxResponse);

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertFalse(loanDetails.getChargedOff());

        // Perform a new charge-off with an earlier date ("28 March 2025") than the first charge-off
        String randomText2 = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Integer chargeOffReasonId2 = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText2, 1);
        String transactionExternalId2 = UUID.randomUUID().toString();
        loanTransactionHelper.chargeOffLoan((long) loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("28 March 2025")
                .locale("en").dateFormat("dd MMMM yyyy").externalId(transactionExternalId2).chargeOffReasonId((long) chargeOffReasonId2));

        loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
        // After the new charge-off, the loan should be charged off
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // Verify the new charge-off transaction details
        GetLoansLoanIdTransactionsTransactionIdResponse newChargeOffTransactionDetails = loanTransactionHelper
                .getLoanTransactionDetails((long) loanId, transactionExternalId2);
        assertNotNull(newChargeOffTransactionDetails);
    }

    private Integer createLoanAccount(final Integer clientID, final Integer loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private Integer createLoanProductWithPeriodicAccrualAccounting(final Account... accounts) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(accounts).withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                .build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
