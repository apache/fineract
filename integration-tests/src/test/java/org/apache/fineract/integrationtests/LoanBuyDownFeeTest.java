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
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Integration tests for Buy Down Fee functionality in Progressive Loans
 */
@Slf4j
@ExtendWith({ LoanTestLifecycleExtension.class })
public class LoanBuyDownFeeTest extends BaseLoanIntegrationTest {

    private Long clientId;
    private Long loanId;

    // Buy Down Fee accounts for accrual-based accounting
    private final Account buyDownExpenseAccount = accountHelper.createExpenseAccount();
    private final Account deferredIncomeLiabilityAccount = accountHelper.createLiabilityAccount();

    // Additional receivable accounts required for accrual-based accounting
    private final Account interestReceivableAccount = accountHelper.createAssetAccount();
    private final Account feeReceivableAccount = accountHelper.createAssetAccount();
    private final Account penaltyReceivableAccount = accountHelper.createAssetAccount();

    @BeforeEach
    public void beforeEach() {
        runAt("01 September 2024", () -> {
            clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                    .createLoanProduct(createProgressiveLoanProductWithBuyDownFee());

            // Apply for the loan with proper progressive loan settings
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(clientId,
                    loanProductsResponse.getResourceId(), "01 September 2024", 1000.0, 10.0, 12, null));
            loanId = postLoansResponse.getLoanId();
            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(1000.0, "01 September 2024"));
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 September 2024");
        });
    }

    @Test
    public void testBuyDownFeeOnProgressiveLoan() {
        runAt("02 September 2024", () -> {
            // Verify loan product has buy down fee enabled
            final GetLoansLoanIdResponse loanDetailsBeforeTransaction = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetailsBeforeTransaction);
            log.info("Loan Product: {}", loanDetailsBeforeTransaction.getLoanProductName());

            // Create buy down fee transaction
            Long buyDownFeeTransactionId = addBuyDownFeeForLoan(loanId, 500.0, "02 September 2024");

            assertNotNull(buyDownFeeTransactionId);

            // Verify transaction was created in loan details
            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertNotNull(loanDetails);

            // Find the buy down fee transaction
            boolean buyDownFeeFound = false;
            for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
                if (transaction.getType() != null && transaction.getType().getId() != null && transaction.getType().getId().equals(40L)) {
                    buyDownFeeFound = true;
                    assertEquals(0, BigDecimal.valueOf(500.0).compareTo(transaction.getAmount()));
                    assertEquals(Long.valueOf(40), transaction.getType().getId());
                    assertEquals("Buy Down Fee", transaction.getType().getValue());
                    break;
                }
            }
            assertTrue(buyDownFeeFound, "Buy down fee transaction should be found in loan transactions");
        });
    }

    @Test
    public void testBuyDownFeeWithNote() {
        runAt("03 September 2024", () -> {
            String externalId = UUID.randomUUID().toString();
            String noteText = "Buy Down Fee - Test Note";

            PostLoansLoanIdTransactionsResponse response = loanTransactionHelper.makeLoanBuyDownFee(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("03 September 2024").locale("en")
                            .transactionAmount(250.0).externalId(externalId).note(noteText));

            assertNotNull(response.getResourceId());

            // Verify transaction details
            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            GetLoansLoanIdTransactions buyDownFeeTransaction = loanDetails.getTransactions().stream()
                    .filter(t -> t.getType() != null && t.getType().getId() != null && t.getType().getId().equals(40L))
                    .filter(t -> externalId.equals(t.getExternalId())).findFirst().orElse(null);

            assertNotNull(buyDownFeeTransaction, "Buy down fee transaction should exist");
            assertEquals(0, BigDecimal.valueOf(250.0).compareTo(buyDownFeeTransaction.getAmount()));
            assertEquals(externalId, buyDownFeeTransaction.getExternalId());
        });
    }

    @Test
    public void testMultipleBuyDownFees() {
        runAt("04 September 2024", () -> {
            // Add first buy down fee
            Long firstBuyDownFeeId = addBuyDownFeeForLoan(loanId, 200.0, "04 September 2024");

            // Add second buy down fee
            Long secondBuyDownFeeId = addBuyDownFeeForLoan(loanId, 150.0, "04 September 2024");

            assertNotNull(firstBuyDownFeeId);
            assertNotNull(secondBuyDownFeeId);

            // Verify both transactions exist
            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            long buyDownFeeCount = loanDetails.getTransactions().stream()
                    .filter(t -> t.getType() != null && t.getType().getId() != null && t.getType().getId().equals(40L)).count();

            assertEquals(2, buyDownFeeCount, "Should have 2 buy down fee transactions");
        });
    }

    @Test
    public void testBuyDownFeeAccountingEntries() {
        runAt("04 September 2024", () -> {
            // Add Buy Down fee transaction
            Long buyDownFeeTransactionId = addBuyDownFeeForLoan(loanId, 250.0, "04 September 2024");
            assertNotNull(buyDownFeeTransactionId);

            final GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            GetLoansLoanIdTransactions buyDownFeeTransaction = loanDetails.getTransactions().stream()
                    .filter(t -> t.getType() != null && t.getType().getId() != null && t.getType().getId().equals(40L))
                    .filter(t -> buyDownFeeTransactionId.equals(t.getId())).findFirst().orElse(null);

            assertNotNull(buyDownFeeTransaction, "Buy down fee transaction should exist");
            assertEquals(0, BigDecimal.valueOf(250.0).compareTo(buyDownFeeTransaction.getAmount()));

            verifyTRJournalEntries(buyDownFeeTransactionId, debit(buyDownExpenseAccount, 250.0), // DR: Buy Down Expense
                    credit(deferredIncomeLiabilityAccount, 250.0) // CR: Deferred Income Liability
            );

            log.info("Buy Down Fee transaction created successfully (accounting validation pending client model regeneration)");
        });
    }

    @Test
    public void testBuyDownFeeValidation() {
        runAt("05 September 2024", () -> {
            // Test with negative amount (should fail)
            try {
                PostLoansLoanIdTransactionsResponse response = loanTransactionHelper.makeLoanBuyDownFee(loanId,
                        new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2024")
                                .locale("en").transactionAmount(-100.0).note("Invalid negative amount"));
                assertTrue(false, "Buy down fee with negative amount should have failed");
            } catch (Exception e) {
                // Expected: validation should prevent negative amounts
                log.info("Expected validation error for negative amount: {}", e.getMessage());
            }

            // Test with zero amount (should fail)
            try {
                PostLoansLoanIdTransactionsResponse response = loanTransactionHelper.makeLoanBuyDownFee(loanId,
                        new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 September 2024")
                                .locale("en").transactionAmount(0.0).note("Invalid zero amount"));
                assertTrue(false, "Buy down fee with zero amount should have failed");
            } catch (Exception e) {
                // Expected: validation should prevent zero amounts
                log.info("Expected validation error for zero amount: {}", e.getMessage());
            }
        });
    }

    /**
     * Creates a progressive loan product with buy down fee enabled
     */
    private PostLoanProductsRequest createProgressiveLoanProductWithBuyDownFee() {
        // Create a progressive loan product with accrual-based accounting and proper GL mappings
        return new PostLoanProductsRequest().name(Utils.uniqueRandomStringGenerator("BUY_DOWN_FEE_PROGRESSIVE_", 6))
                .shortName(Utils.uniqueRandomStringGenerator("", 4)).description("Progressive loan product with buy down fee enabled")
                .includeInBorrowerCycle(false).useBorrowerCycle(false).currencyCode("USD").digitsAfterDecimal(2).principal(1000.0)
                .minPrincipal(100.0).maxPrincipal(10000.0).numberOfRepayments(12).minNumberOfRepayments(6).maxNumberOfRepayments(24)
                .repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.MONTHS_L).interestRatePerPeriod(10.0)
                .minInterestRatePerPeriod(0.0).maxInterestRatePerPeriod(120.0).interestRateFrequencyType(InterestRateFrequencyType.YEARS)
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS).interestType(InterestType.DECLINING_BALANCE)
                .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY).allowPartialPeriodInterestCalcualtion(false)
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")
                .paymentAllocation(List.of(createDefaultPaymentAllocation("NEXT_INSTALLMENT"))).creditAllocation(List.of())
                .daysInMonthType(30).daysInYearType(360).isInterestRecalculationEnabled(false).accountingRule(3) // Accrual-based
                                                                                                                 // accounting
                // GL Account Mappings for Accrual-Based Accounting
                .fundSourceAccountId(fundSource.getAccountID().longValue())
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())
                .incomeFromPenaltyAccountId(penaltyIncomeAccount.getAccountID().longValue())
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())
                // Receivable accounts required for accrual-based accounting
                .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())
                .receivableFeeAccountId(feeReceivableAccount.getAccountID().longValue())
                .receivablePenaltyAccountId(penaltyReceivableAccount.getAccountID().longValue())
                .buyDownExpenseAccountId(buyDownExpenseAccount.getAccountID().longValue())
                .incomeFromBuyDownAccountId(feeIncomeAccount.getAccountID().longValue())
                .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue()).loanScheduleType("PROGRESSIVE")
                .loanScheduleProcessingType("HORIZONTAL").enableBuyDownFee(true)
                .buyDownFeeCalculationType(PostLoanProductsRequest.BuyDownFeeCalculationTypeEnum.FLAT)
                .buyDownFeeStrategy(PostLoanProductsRequest.BuyDownFeeStrategyEnum.EQUAL_AMORTIZATION)
                .buyDownFeeIncomeType(PostLoanProductsRequest.BuyDownFeeIncomeTypeEnum.FEE).locale("en").dateFormat("dd MMMM yyyy");
    }

    /**
     * Helper method to add buy down fee for a loan
     *
     * @param loanId
     *            the ID of the loan to add the buy down fee to
     * @param amount
     *            the amount of the buy down fee
     * @param date
     *            the transaction date in format specified by DATETIME_PATTERN
     * @return the ID of the created buy down fee transaction
     */
    private Long addBuyDownFeeForLoan(Long loanId, Double amount, String date) {
        String buyDownFeeExternalId = UUID.randomUUID().toString();
        PostLoansLoanIdTransactionsResponse response = loanTransactionHelper.makeLoanBuyDownFee(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(date).locale("en")
                        .transactionAmount(amount).externalId(buyDownFeeExternalId).note("Buy Down Fee Transaction"));
        return response.getResourceId();
    }
}
