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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanCapitalizedIncomeTest extends BaseLoanIntegrationTest {

    @BeforeAll
    public void setup() {
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "CHECK_DUE_INSTALLMENTS", "UPDATE_LOAN_ARREARS_AGING",
                "ADD_PERIODIC_ACCRUAL_ENTRIES", "ACCRUAL_ACTIVITY_POSTING", "CAPITALIZED_INCOME_AMORTIZATION",
                "LOAN_INTEREST_RECALCULATION", "EXTERNAL_ASSET_OWNER_TRANSFER");
    }

    @Test
    public void testLoanCapitalizedIncomeAmortization() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0);
        });
        runAt("2 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income", "01 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "01 January 2024") //
            );
        });
        runAt("3 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income", "01 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.03, "Accrual", "02 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "02 January 2024") //
            );

            verifyJournalEntries(loanId, //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, fundSource, "CREDIT"), //
                    journalEntry(50, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50, deferredIncomeLiabilityAccount, "CREDIT"), //
                    journalEntry(0.55, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(0.55, feeIncomeAccount, "CREDIT"), //
                    journalEntry(0.03, interestReceivableAccount, "DEBIT"), //
                    journalEntry(0.03, interestIncomeAccount, "CREDIT"), //
                    journalEntry(0.55, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(0.55, feeIncomeAccount, "CREDIT") //
            );
        });
    }

    @Test
    public void testLoanDisbursementWithCapitalizedIncome() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE).overAppliedNumber(3));

        runAt("1 April 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(300), "1 January 2024");
            PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = loanTransactionHelper.addCapitalizedIncome(loanId,
                    "1 January 2024", 50.0);

            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> disburseLoan(loanId, BigDecimal.valueOf(200), "1 February 2024"));

            Assertions.assertTrue(callFailedRuntimeException.getMessage()
                    .contains("Loan disbursal amount can't be greater than maximum applied loan amount calculation"));
        });
    }

    @Test
    public void testLoanCapitalizedIncomeAdjustment() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        runAt("1 April 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = loanTransactionHelper.addCapitalizedIncome(loanId,
                    "1 January 2024", 50.0);
            capitalizedIncomeIdRef.set(capitalizedIncomeResponse.getResourceId());

            loanTransactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "1 April 2024", 50.0);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income Adjustment", "01 April 2024") //
            );

            verifyJournalEntries(loanId, //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, fundSource, "CREDIT"), //
                    journalEntry(50, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50, deferredIncomeLiabilityAccount, "CREDIT"), //
                    journalEntry(50.0, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(49.71, loansReceivableAccount, "CREDIT"), //
                    journalEntry(0.29, interestReceivableAccount, "CREDIT") //
            );
        });
    }

    @Test
    public void testLoanCapitalizedIncomeAdjustmentValidations() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        runAt("3 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = loanTransactionHelper.addCapitalizedIncome(loanId,
                    "3 January 2024", 50.0);
            capitalizedIncomeIdRef.set(capitalizedIncomeResponse.getResourceId());

            // Amount more than remaining
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanTransactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "3 January 2024", 60.0));

            loanTransactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "3 January 2024", 30.0);
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanTransactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "3 January 2024", 30.0));

            // Capitalized income transaction with given id doesn't exist for this loan
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanTransactionHelper.capitalizedIncomeAdjustment(loanId, 1L, "3 January 2024", 30.0));

            // Cannot be earlier than capitalized income transaction
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanTransactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "2 January 2024", 30.0));
        });
    }

    @Test
    public void testLoanCapitalizedIncomeAdjustmentWithAmortizationAccounting() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = loanTransactionHelper.addCapitalizedIncome(loanId,
                    "1 January 2024", 100.0);
            capitalizedIncomeIdRef.set(capitalizedIncomeResponse.getResourceId());
        });
        runAt("2 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024") //
            );
        });
        runAt("3 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.04, "Accrual", "02 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "02 January 2024") //
            );

            verifyJournalEntries(loanId, //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, fundSource, "CREDIT"), //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, deferredIncomeLiabilityAccount, "CREDIT"), //
                    journalEntry(1.10, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(1.10, feeIncomeAccount, "CREDIT"), //
                    journalEntry(0.04, interestReceivableAccount, "DEBIT"), //
                    journalEntry(0.04, interestIncomeAccount, "CREDIT"), //
                    journalEntry(1.10, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(1.10, feeIncomeAccount, "CREDIT") //
            );

            Long capitalizedIncomeAdjustmentTransactionId = loanTransactionHelper
                    .capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "3 January 2024", 100.0).getResourceId();

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.04, "Accrual", "02 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "02 January 2024"), //
                    transaction(100.0, "Capitalized Income Adjustment", "03 January 2024") //
            );

            verifyJournalEntries(loanId, //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, fundSource, "CREDIT"), //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, deferredIncomeLiabilityAccount, "CREDIT"), //
                    journalEntry(1.10, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(1.10, feeIncomeAccount, "CREDIT"), //
                    journalEntry(0.04, interestReceivableAccount, "DEBIT"), //
                    journalEntry(0.04, interestIncomeAccount, "CREDIT"), //
                    journalEntry(1.10, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(1.10, feeIncomeAccount, "CREDIT"), //
                    journalEntry(99.92, loansReceivableAccount, "CREDIT"), //
                    journalEntry(0.08, interestReceivableAccount, "CREDIT"), //
                    journalEntry(2.20, feeIncomeAccount, "DEBIT"), //
                    journalEntry(97.80, deferredIncomeLiabilityAccount, "DEBIT") //
            );

            // Reverse-replay
            addRepaymentForLoan(loanId, 67.45, "2 January 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Optional<GetLoansLoanIdTransactions> replayedCapitalizedIncomeAdjustmentOpt = loanDetails.getTransactions().stream()
                    .filter(t -> t.getType().getCapitalizedIncomeAdjustment()).findFirst();
            Assertions.assertTrue(replayedCapitalizedIncomeAdjustmentOpt.isPresent(), "Capitalized income adjustment not found");

            verifyTRJournalEntries(replayedCapitalizedIncomeAdjustmentOpt.get().getId(), //
                    journalEntry(99.98, loansReceivableAccount, "CREDIT"), //
                    journalEntry(0.02, interestReceivableAccount, "CREDIT"), //
                    journalEntry(2.20, feeIncomeAccount, "DEBIT"), //
                    journalEntry(97.80, deferredIncomeLiabilityAccount, "DEBIT") //
            );

            verifyTRJournalEntries(capitalizedIncomeAdjustmentTransactionId, //
                    journalEntry(99.92, loansReceivableAccount, "CREDIT"), //
                    journalEntry(0.08, interestReceivableAccount, "CREDIT"), //
                    journalEntry(2.20, feeIncomeAccount, "DEBIT"), //
                    journalEntry(97.80, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(99.92, loansReceivableAccount, "DEBIT"), //
                    journalEntry(0.08, interestReceivableAccount, "DEBIT"), //
                    journalEntry(2.20, feeIncomeAccount, "CREDIT"), //
                    journalEntry(97.80, deferredIncomeLiabilityAccount, "CREDIT") //
            );
        });
    }

    @Test
    public void testCapitalizedIncomeTransactionsNotInFuture() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            // Capitalized income cannot be in the future
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanTransactionHelper.addCapitalizedIncome(loanId, "1 February 2024", 100.0));

            Long capitalizedIncomeId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 100.0).getResourceId();

            // Capitalized income adjustment cannot be in the future
            Assertions.assertThrows(RuntimeException.class,
                    () -> loanTransactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeId, "1 February 2024", 10.0));
        });
    }

    @Test
    public void testCapitalizedIncomeAmortizationShouldNotHappensForFutureBalances() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            PostLoansLoanIdTransactionsResponse capitalizedIncomeResponse = loanTransactionHelper.addCapitalizedIncome(loanId,
                    "1 January 2024", 100.0);
            capitalizedIncomeIdRef.set(capitalizedIncomeResponse.getResourceId());

            // random midday COB run
            executeInlineCOB(loanId);

            // verify no early amortization was created
            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024") //
            );
        });
        runAt("2 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024") //
            );
        });
    }
}
