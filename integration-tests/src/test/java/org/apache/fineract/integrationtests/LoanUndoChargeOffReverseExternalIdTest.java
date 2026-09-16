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

import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanUndoChargeOffReverseExternalIdTest extends FeignLoanTestBase {

    private Account assetAccount;
    private Account incomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;

    @BeforeEach
    public void setupAccounts() {
        this.assetAccount = accountHelper.createAssetAccount();
        this.incomeAccount = accountHelper.createIncomeAccount();
        this.expenseAccount = accountHelper.createExpenseAccount();
        this.overpaymentAccount = accountHelper.createLiabilityAccount();
    }

    @Test
    public void loanUndoChargeOffReverseExternalIdTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Long clientId = createClient();
        final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // make Repayment
        makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                .transactionDate("6 September 2022").locale(LoanTestData.LOCALE).transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // set loan as chargeoff
        String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
        String transactionExternalId = UUID.randomUUID().toString();
        chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("7 September 2022").locale(LoanTestData.LOCALE)
                .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // undo charge-off
        String reverseTransactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse undoChargeOffTxResponse = transactionHelper.undoChargeOff(loanId,
                new PostLoansLoanIdTransactionsRequest().reversalExternalId(reverseTransactionExternalId));
        assertNotNull(undoChargeOffTxResponse);

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertFalse(loanDetails.getChargedOff());

        GetLoansLoanIdTransactionsTransactionIdResponse chargeOffTransactionDetails = getLoanTransactionDetails(loanId,
                transactionExternalId);
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

        final Long loanProductID = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);
        final Long clientId = createClient();
        final Long loanId = createLoanAccount(clientId, loanProductID, loanExternalIdStr);

        // make Repayment
        makeLoanRepayment(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                .transactionDate("28 March 2025").locale(LoanTestData.LOCALE).transactionAmount(100.0));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());

        // Perform first charge-off with date "29 March 2025"
        String randomText1 = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId1 = codeHelper.createChargeOffCodeValue(randomText1, 1);
        String transactionExternalId1 = UUID.randomUUID().toString();
        chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("29 March 2025").locale(LoanTestData.LOCALE)
                .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId1).chargeOffReasonId(chargeOffReasonId1));

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // Undo the charge-off
        String reverseTransactionExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse undoChargeOffTxResponse = transactionHelper.undoChargeOff(loanId,
                new PostLoansLoanIdTransactionsRequest().reversalExternalId(reverseTransactionExternalId));
        assertNotNull(undoChargeOffTxResponse);

        loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getActive());
        assertFalse(loanDetails.getChargedOff());

        // Perform a new charge-off with an earlier date ("28 March 2025") than the first charge-off
        String randomText2 = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5);
        Long chargeOffReasonId2 = codeHelper.createChargeOffCodeValue(randomText2, 1);
        String transactionExternalId2 = UUID.randomUUID().toString();
        chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("28 March 2025").locale(LoanTestData.LOCALE)
                .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId2).chargeOffReasonId(chargeOffReasonId2));

        loanDetails = getLoanDetails(loanId);
        // After the new charge-off, the loan should be charged off
        assertTrue(loanDetails.getStatus().getActive());
        assertTrue(loanDetails.getChargedOff());

        // Verify the new charge-off transaction details
        GetLoansLoanIdTransactionsTransactionIdResponse newChargeOffTransactionDetails = getLoanTransactionDetails(loanId,
                transactionExternalId2);
        assertNotNull(newChargeOffTransactionDetails);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 September 2022", 1000.0, 1)//
                .expectedDisbursementDate("03 September 2022")//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .externalId(externalId);

        final Long loanId = loanHelper.applyForLoan(application).getLoanId();
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "02 September 2022"));
        disburseLoan(loanId, "03 September 2022", 1000.0);
        return loanId;
    }

    private Long createLoanProductWithPeriodicAccrualAccounting(final Account... accounts) {
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1").withNumberOfRepayments("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").buildRequest(null));
    }

}
