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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetSavingsAccountTransactionsPageItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.SavingsAccountTransactionsSearchResponse;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanAccountDisbursementToSavingsWithAutoDownPaymentTest extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void loanDisbursementToSavingsWithAutoDownPaymentAndStandingInstructionsTest() {
        runAt("01 March 2023", () -> {

            // loan external Id
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment();

            SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);

            // Create approve and activate savings account
            Integer savingsAccountId = createApproveActivateSavingsAccountDailyPosting(clientId.intValue(), "01 March 2023",
                    savingsAccountHelper);

            // create Financial Activity Mapping for Liability Transfer
            mapLiabilityTransferFinancialActivity(loanProductId);

            // Apply and Approve Loan
            Long loanId = createLoanWithLinkedAccountAndStandingInstructions(clientId.intValue(), loanProductId, savingsAccountId,
                    loanExternalIdStr);

            // disburse to savings
            PostLoansLoanIdResponse responseLoanDisburseToSavings = loanTransactionHelper.disburseToSavingsLoan(loanExternalIdStr,
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 March 2023").transactionAmount(new BigDecimal("1000"))
                            .locale("en").dateFormat("dd MMMM yyyy"));

            assertEquals(loanExternalIdStr, responseLoanDisburseToSavings.getResourceExternalId());

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // verify Disbursement Transaction is account transfer
            verifyTransactionIsAccountTransfer(LocalDate.of(2023, 3, 1), 1000.0f, loanId.intValue(), "disbursement");

            // verify Down payment Transaction is account transfer
            verifyTransactionIsAccountTransfer(LocalDate.of(2023, 3, 1), 250.0f, loanId.intValue(), "downPayment");

            // verify savings transactions
            verifySavingsTransactions(savingsAccountId, savingsAccountHelper);

        });
    }

    private void verifySavingsTransactions(final Integer savingsId, final SavingsAccountHelper savingsAccountHelper) {
        Map<String, Object> queryParams = new HashMap<>();
        SavingsAccountTransactionsSearchResponse transactionsResponse = savingsAccountHelper.searchSavingsTransactions(savingsId,
                queryParams);

        Assertions.assertNotNull(transactionsResponse);
        assertEquals(2, transactionsResponse.getTotal());
        Assertions.assertNotNull(transactionsResponse.getContent());
        List<GetSavingsAccountTransactionsPageItem> pageItemsList = List.copyOf(transactionsResponse.getContent());
        assertEquals(2, pageItemsList.size());

        // check withdrawal
        GetSavingsAccountTransactionsPageItem withDrawalTransaction = pageItemsList.get(0);
        assertEquals("savingsAccountTransactionType.withdrawal", withDrawalTransaction.getTransactionType().getCode());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(250), withDrawalTransaction.getAmount()));
        assertEquals("DEBIT", withDrawalTransaction.getEntryType().getValue());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(750), withDrawalTransaction.getRunningBalance()));

        // check deposit
        GetSavingsAccountTransactionsPageItem depositTransaction = pageItemsList.get(1);
        assertEquals("savingsAccountTransactionType.deposit", depositTransaction.getTransactionType().getCode());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(1000), depositTransaction.getAmount()));
        assertEquals("CREDIT", depositTransaction.getEntryType().getValue());
        assertTrue(MathUtil.isEqualTo(BigDecimal.valueOf(1000), depositTransaction.getRunningBalance()));

    }

    private void mapLiabilityTransferFinancialActivity(Long loanProductId) {
        FinancialActivityAccountHelper financialActivityAccountHelper = new FinancialActivityAccountHelper(requestSpec);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper.retrieveLoanProductById(loanProductId);
        Integer financialActivityAccountId = (Integer) financialActivityAccountHelper.createFinancialActivityAccount(
                AccountingConstants.FinancialActivity.LIABILITY_TRANSFER.getValue(),
                getLoanProductsProductIdResponse.getAccountingMappings().getFundSourceAccount().getId().intValue(), responseSpec,
                CommonConstants.RESPONSE_RESOURCE_ID);
        assertNotNull(financialActivityAccountId);
    }

    private Long createLoanWithLinkedAccountAndStandingInstructions(final Integer clientID, final Long loanProductID,
            final Integer savingsId, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("45")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("3").withRepaymentEveryAfter("15").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("01 March 2023")
                .withSubmittedOnDate("01 March 2023").withLoanType("individual").withExternalId(externalId)
                .withCreateStandingInstructionAtDisbursement().build(clientID.toString(), loanProductID.toString(), savingsId.toString());

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("01 March 2023", "1000", loanId, null);
        return loanId.longValue();
    }

    private Integer createApproveActivateSavingsAccountDailyPosting(final Integer clientID, final String startDate,
            final SavingsAccountHelper savingsAccountHelper) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        assertNotNull(savingsProductID);
        return savingsAccountHelper.createApproveActivateSavingsAccount(clientID, savingsProductID, startDate);
    }

    private Integer createSavingsProductDailyPosting() {
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment() {
        boolean multiDisburseEnabled = true;
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setNumberOfRepayments(3);
        product.setRepaymentEvery(15);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(true);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());
        assertNotNull(getLoanProductsProductIdResponse);
        return loanProductResponse.getResourceId();

    }

    private void verifyTransactionIsAccountTransfer(final LocalDate transactionDate, final Float transactionAmount, final Integer loanID,
            final String transactionOfType) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(requestSpec, responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isTransaction = (Boolean) transactionType.get(transactionOfType);

            if (isTransaction) {
                ArrayList<Integer> transactionDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate transactionEntryDate = LocalDate.of(transactionDateAsArray.get(0), transactionDateAsArray.get(1),
                        transactionDateAsArray.get(2));

                if (transactionDate.isEqual(transactionEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(transactionAmount, Float.valueOf(String.valueOf(transactions.get(i).get("amount"))),
                            "Mismatch in transaction amounts");

                    // verify transfer details
                    assertNotNull(transactions.get(i).get("transfer"));

                    final HashMap<String, Object> actualTransferMap = (HashMap) transactions.get(i).get("transfer");

                    assertEquals(transactionAmount, Float.valueOf(String.valueOf(actualTransferMap.get("transferAmount"))));

                    ArrayList<Integer> transferDate = (ArrayList<Integer>) actualTransferMap.get("transferDate");

                    LocalDate dateOfTransfer = LocalDate.of(transferDate.get(0), transferDate.get(1), transferDate.get(2));
                    assertTrue(transactionDate.isEqual(dateOfTransfer));

                    break;
                }
            }
        }

        assertTrue(isTransactionFound, "No Transaction entries are posted");

    }

    /**
     * Delete the Financial activities
     */
    @AfterEach
    public void tearDown() {
        FinancialActivityAccountHelper financialActivityAccountHelper = new FinancialActivityAccountHelper(requestSpec);
        List<HashMap> financialActivities = financialActivityAccountHelper.getAllFinancialActivityAccounts(responseSpec);
        for (HashMap financialActivity : financialActivities) {
            Integer financialActivityAccountId = (Integer) financialActivity.get("id");
            Integer deletedFinancialActivityAccountId = financialActivityAccountHelper
                    .deleteFinancialActivityAccount(financialActivityAccountId, responseSpec, CommonConstants.RESPONSE_RESOURCE_ID);
            Assertions.assertNotNull(deletedFinancialActivityAccountId);
            Assertions.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);
        }
    }
}
