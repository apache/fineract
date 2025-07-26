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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.CapitalizedIncomeDetails;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.LoanDeferredIncomeData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.externalevents.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.integrationtests.common.externalevents.LoanBusinessEvent;
import org.apache.fineract.integrationtests.common.externalevents.LoanTransactionBusinessEvent;
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
            final LoanDeferredIncomeData loanDeferredIncomeData = loanTransactionHelper.fetchDeferredIncomeDetails(loanId);
            assertTrue(loanDeferredIncomeData.getCapitalizedIncomeData().size() > 0);
            final CapitalizedIncomeDetails capitalizedIncomeData = loanDeferredIncomeData.getCapitalizedIncomeData().get(0);
            assertNotNull(capitalizedIncomeData);
            assertEquals(50.0, Utils.getDoubleValue(capitalizedIncomeData.getAmount()));
            assertEquals(0.55, Utils.getDoubleValue(capitalizedIncomeData.getAmortizedAmount()));
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
            final LoanDeferredIncomeData loanDeferredIncomeData = loanTransactionHelper.fetchDeferredIncomeDetails(loanId);
            assertTrue(loanDeferredIncomeData.getCapitalizedIncomeData().size() > 0);
            final CapitalizedIncomeDetails capitalizedIncomeData = loanDeferredIncomeData.getCapitalizedIncomeData().get(0);
            assertNotNull(capitalizedIncomeData);
            assertEquals(50.0, Utils.getDoubleValue(capitalizedIncomeData.getAmount()));
            assertEquals(1.1, Utils.getDoubleValue(capitalizedIncomeData.getAmortizedAmount()));
            assertEquals(48.90, Utils.getDoubleValue(capitalizedIncomeData.getUnrecognizedAmount()));

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

            PostLoansLoanIdTransactionsResponse capitalizedIncomeAdjustmentResponse = loanTransactionHelper
                    .capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "1 April 2024", 50.0);
            assertNotNull(capitalizedIncomeAdjustmentResponse.getLoanId());
            assertNotNull(capitalizedIncomeAdjustmentResponse.getClientId());
            assertNotNull(capitalizedIncomeAdjustmentResponse.getOfficeId());

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income Adjustment", "01 April 2024") //
            );
            final LoanDeferredIncomeData loanDeferredIncomeData = loanTransactionHelper.fetchDeferredIncomeDetails(loanId);
            assertTrue(loanDeferredIncomeData.getCapitalizedIncomeData().size() > 0);
            final CapitalizedIncomeDetails capitalizedIncomeData = loanDeferredIncomeData.getCapitalizedIncomeData().get(0);
            assertNotNull(capitalizedIncomeData);
            assertEquals(50.0, Utils.getDoubleValue(capitalizedIncomeData.getAmount()));
            assertEquals(50.0, Utils.getDoubleValue(capitalizedIncomeData.getAmountAdjustment()));

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
        final AtomicReference<Long> capitalizedIncomeAdjustmentTransactionIdRef = new AtomicReference<>();

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
            capitalizedIncomeAdjustmentTransactionIdRef.set(capitalizedIncomeAdjustmentTransactionId);
        });
        runAt("4 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.04, "Accrual", "02 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "02 January 2024"), //
                    transaction(100.0, "Capitalized Income Adjustment", "03 January 2024"), //
                    transaction(0.04, "Accrual", "03 January 2024"), //
                    transaction(2.20, "Capitalized Income Amortization Adjustment", "03 January 2024") //
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
                    journalEntry(100.0, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(0.04, interestReceivableAccount, "DEBIT"), //
                    journalEntry(0.04, interestIncomeAccount, "CREDIT"), //
                    journalEntry(2.20, feeIncomeAccount, "DEBIT"), //
                    journalEntry(2.20, deferredIncomeLiabilityAccount, "CREDIT") //
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
                    journalEntry(100.0, deferredIncomeLiabilityAccount, "DEBIT") //
            );

            verifyTRJournalEntries(capitalizedIncomeAdjustmentTransactionIdRef.get(), //
                    journalEntry(99.92, loansReceivableAccount, "CREDIT"), //
                    journalEntry(0.08, interestReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(99.92, loansReceivableAccount, "DEBIT"), //
                    journalEntry(0.08, interestReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, deferredIncomeLiabilityAccount, "CREDIT") //
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
            assertNotNull(capitalizedIncomeResponse.getLoanId());
            assertNotNull(capitalizedIncomeResponse.getClientId());
            assertNotNull(capitalizedIncomeResponse.getOfficeId());
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

    @Test
    public void testLoanCapitalizedIncomeReversal() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeTransactionIdRef = new AtomicReference<>();

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
            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0)
                    .getResourceId();
            capitalizedIncomeTransactionIdRef.set(capitalizedIncomeTransactionId);
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

            loanTransactionHelper.reverseLoanTransaction(loanId, capitalizedIncomeTransactionIdRef.get(), "3 January 2024");

        });
        runAt("4 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income", "01 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.03, "Accrual", "02 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "02 January 2024"), //
                    transaction(0.01, "Accrual", "03 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization Adjustment", "03 January 2024") //
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
                    journalEntry(0.55, feeIncomeAccount, "CREDIT"), //
                    journalEntry(50, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(50, loansReceivableAccount, "CREDIT"), //
                    journalEntry(0.01, interestReceivableAccount, "DEBIT"), //
                    journalEntry(0.01, interestIncomeAccount, "CREDIT"), //
                    journalEntry(1.10, feeIncomeAccount, "DEBIT"), //
                    journalEntry(1.10, deferredIncomeLiabilityAccount, "CREDIT") //
            );
        });
    }

    @Test
    public void testLoanCapitalizedIncomeAdjustmentReversal() {
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

            final PostLoansLoanIdTransactionsResponse capitalizedIncomeAdjustmentResponse = loanTransactionHelper
                    .capitalizedIncomeAdjustment(loanId, capitalizedIncomeIdRef.get(), "1 April 2024", 50.0);
            final Long capitalizedIncomeAdjustmentTransactionId = capitalizedIncomeAdjustmentResponse.getResourceId();

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

            loanTransactionHelper.reverseLoanTransaction(loanId, capitalizedIncomeAdjustmentTransactionId, "1 April 2024");

            verifyJournalEntries(loanId, //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, fundSource, "CREDIT"), //
                    journalEntry(50, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50, deferredIncomeLiabilityAccount, "CREDIT"), //
                    journalEntry(50.0, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(49.71, loansReceivableAccount, "CREDIT"), //
                    journalEntry(0.29, interestReceivableAccount, "CREDIT"), //
                    journalEntry(50.0, deferredIncomeLiabilityAccount, "CREDIT"), //
                    journalEntry(49.71, loansReceivableAccount, "DEBIT"), //
                    journalEntry(0.29, interestReceivableAccount, "DEBIT") //
            );
        });
    }

    @Test
    public void testLoanCapitalizedIncomeReversalFailsIfAdjustmentExistsForIt() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeTransactionIdRef = new AtomicReference<>();

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
            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0)
                    .getResourceId();
            capitalizedIncomeTransactionIdRef.set(capitalizedIncomeTransactionId);
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

            loanTransactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeTransactionIdRef.get(), "3 January 2024", 40.0);

            Assertions.assertThrows(RuntimeException.class, () -> {
                loanTransactionHelper.reverseLoanTransaction(loanId, capitalizedIncomeTransactionIdRef.get(), "3 January 2024");
            });
        });
    }

    @Test
    public void testLoanCapitalizedIncomeOnLoanClosed() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeTransactionIdRef = new AtomicReference<>();

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
            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0)
                    .getResourceId();
            capitalizedIncomeTransactionIdRef.set(capitalizedIncomeTransactionId);
        });
        runAt("1 February 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 February 2024");
        });
        runAt("1 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 March 2024");
        });
        runAt("15 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "15 March 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 151.59, 0.0, 150.0, 0.15);

            loanTransactionHelper.makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 March 2024").locale("en").transactionAmount(0.15));

            // Validate Loan is Closed
            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 151.59, 0.0, 150.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 49.71, 49.71, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 49.99, 49.99, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2024, 4, 1), 50.30, 50.30, 0.0, 50.43, 0.0);

            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
        });
        runAt("16 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "16 March 2024", 50.0).getResourceId();

            verifyTRJournalEntries(capitalizedIncomeTransactionId, journalEntry(50, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50, deferredIncomeLiabilityAccount, "CREDIT") //
            );

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 50.15, 151.59, 50.00, 150.00, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 49.71, 49.71, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 49.99, 49.99, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2024, 4, 1), 100.30, 50.3, 50.00, 50.43, 0.0);
            // Validate Loan is Active
            assertTrue(loanDetails.getStatus().getActive());
        });
    }

    @Test
    public void testLoanCapitalizedIncomeOnLoanOverpaid() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeTransactionIdRef = new AtomicReference<>();

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
            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0)
                    .getResourceId();
            capitalizedIncomeTransactionIdRef.set(capitalizedIncomeTransactionId);
        });
        runAt("1 February 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 February 2024");
        });
        runAt("1 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 March 2024");
        });
        runAt("15 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "15 March 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 151.59, 0.0, 150.0, 0.15);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 49.71, 49.71, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 49.99, 49.99, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2024, 4, 1), 50.30, 50.30, 0.0, 50.43, 0.0);
            // Validate Loan is Overpaid
            assertTrue(loanDetails.getStatus().getOverpaid());
        });
        runAt("16 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "16 March 2024", 50.0).getResourceId();

            verifyTRJournalEntries(capitalizedIncomeTransactionId, journalEntry(50, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50, deferredIncomeLiabilityAccount, "CREDIT") //
            );

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 50.0, 151.74, 49.85, 150.15, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 49.71, 49.71, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 49.99, 49.99, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2024, 4, 1), 100.30, 50.45, 49.85, 50.58, 0.0);

            assertTrue(loanDetails.getStatus().getActive());
        });
    }

    @Test
    public void testLoanCapitalizedIncomeAdjustmentOnLoanOverpaid() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeTransactionIdRef = new AtomicReference<>();

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
            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0)
                    .getResourceId();
            capitalizedIncomeTransactionIdRef.set(capitalizedIncomeTransactionId);
        });
        runAt("1 February 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 February 2024");
        });
        runAt("1 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 March 2024");
        });
        runAt("1 April 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 60.6, "1 April 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            // Validate Loan is Overpaid
            assertTrue(loanDetails.getStatus().getOverpaid());
        });
        runAt("5 April 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            loanTransactionHelper.makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("5 April 2024").locale("en").transactionAmount(10.00));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            // Validate Loan remains Overpaid
            assertTrue(loanDetails.getStatus().getOverpaid());
        });
        runAt("15 April 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            Long capitalizedIncomeAdjustmentTransactionId = loanTransactionHelper
                    .capitalizedIncomeAdjustment(loanId, capitalizedIncomeTransactionIdRef.get(), "15 April 2024", 15.0).getResourceId();
            verifyTRJournalEntries(capitalizedIncomeAdjustmentTransactionId, journalEntry(15, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(15, overpaymentAccount, "CREDIT") //
            );

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            // Validate Loan remains Overpaid
            assertTrue(loanDetails.getStatus().getOverpaid());
            validateLoanSummaryBalances(loanDetails, 0.0, 151.75, 0.0, 150.00, 15.01);
        });
    }

    @Test
    public void testLoanCapitalizedIncomeAdjustmentOnLoanClosed() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeTransactionIdRef = new AtomicReference<>();

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
            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0)
                    .getResourceId();
            capitalizedIncomeTransactionIdRef.set(capitalizedIncomeTransactionId);
        });
        runAt("1 February 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 February 2024");
        });
        runAt("1 March 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.58, "1 March 2024");
        });
        runAt("1 April 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            addRepaymentForLoan(loanId, 50.59, "1 April 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 151.75, 0.0, 150.00, null);
            // Validate Loan goes to Closed
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
        });
        runAt("15 April 2024", () -> {
            Long loanId = loanIdRef.get();
            Long capitalizedIncomeAdjustmentTransactionId = loanTransactionHelper
                    .capitalizedIncomeAdjustment(loanId, capitalizedIncomeTransactionIdRef.get(), "15 April 2024", 15.0).getResourceId();
            verifyTRJournalEntries(capitalizedIncomeAdjustmentTransactionId, journalEntry(15, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(15.00, overpaymentAccount, "CREDIT") //
            );

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 151.75, 0.0, 150.00, 15.0);
            // Validate Loan goes to Overpaid
            assertTrue(loanDetails.getStatus().getOverpaid());
        });
    }

    @Test
    public void testOverpaymentAmountWhenCapitalizedIncomeTransactionsAreReversed() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        runAt("01 March 2023", () -> {
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                    .createLoanProduct(create4IProgressiveWithCapitalizedIncome());
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(client.getClientId(),
                    loanProductsResponse.getResourceId(), "01 March 2023", 10000.00, 12.00, 4, null));
            Long loanId = postLoansResponse.getLoanId();
            loanIdRef.set(loanId);

            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(10000.00, "01 March 2023"));
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            loanTransactionHelper.addCapitalizedIncome(loanId, "01 March 2023", 500.00);
            PostLoansLoanIdTransactionsResponse transactionsResponse = loanTransactionHelper.addCapitalizedIncome(loanId, "01 March 2023",
                    500.00);

            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "1 March 2023", 2000.00);
            loanTransactionHelper.reverseLoanTransaction(loanId, transactionsResponse.getResourceId(), "1 March 2023");
        });

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal thousand = BigDecimal.valueOf(1000.0);
        BigDecimal fiveHundred = BigDecimal.valueOf(500.0);
        BigDecimal thousandFiveHundred = BigDecimal.valueOf(1500.0);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanIdRef.get());
        Assertions.assertEquals(thousand, loanDetails.getPrincipal().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(thousand, loanDetails.getSummary().getPrincipalDisbursed().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(fiveHundred, loanDetails.getSummary().getTotalCapitalizedIncome().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(thousandFiveHundred, loanDetails.getSummary().getTotalPrincipal().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(zero, loanDetails.getSummary().getPrincipalOutstanding().setScale(0, RoundingMode.HALF_UP));

        Assertions.assertEquals(fiveHundred, loanDetails.getTotalOverpaid().setScale(1, RoundingMode.HALF_UP));
    }

    @Test
    public void testOverpaymentAmountCorrectlyCalculatedWhenBackdatedRepaymentIsMade() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        runAt("01 March 2023", () -> {
            final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                    .createLoanProduct(create4IProgressiveWithCapitalizedIncome());
            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applyLP2ProgressiveLoanRequest(client.getClientId(),
                    loanProductsResponse.getResourceId(), "01 March 2023", 10000.00, 12.00, 4, null));
            Long loanId = postLoansResponse.getLoanId();
            loanIdRef.set(loanId);

            loanTransactionHelper.approveLoan(loanId, approveLoanRequest(10000.00, "01 March 2023"));
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");
        });

        runAt("15 March 2023", () -> {
            loanTransactionHelper.addCapitalizedIncome(loanIdRef.get(), "15 March 2023", 500.00);
            loanTransactionHelper.makeLoanRepayment(loanIdRef.get(), "Repayment", "1 March 2023", 1500.00);
        });

        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal thousand = BigDecimal.valueOf(1000.0);
        BigDecimal fiveHundred = BigDecimal.valueOf(500.0);
        BigDecimal thousandFiveHundred = BigDecimal.valueOf(1500.0);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanIdRef.get());
        Assertions.assertEquals(thousand, loanDetails.getPrincipal().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(thousand, loanDetails.getSummary().getPrincipalDisbursed().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(fiveHundred, loanDetails.getSummary().getTotalCapitalizedIncome().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(thousandFiveHundred, loanDetails.getSummary().getTotalPrincipal().setScale(1, RoundingMode.HALF_UP));
        Assertions.assertEquals(zero, loanDetails.getSummary().getPrincipalOutstanding().setScale(0, RoundingMode.HALF_UP));
    }

    @Test
    public void testCapitalizedIncomeEvents() {
        externalEventHelper.enableBusinessEvent("LoanCapitalizedIncomeTransactionCreatedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanCapitalizedIncomeAmortizationAdjustmentTransactionCreatedBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanAdjustTransactionBusinessEvent");
        externalEventHelper.enableBusinessEvent("LoanBalanceChangedBusinessEvent");

        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        final AtomicReference<Long> capitalizedIncomeTransactionIdRef = new AtomicReference<>();

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

            deleteAllExternalEvents();

            Long capitalizedIncomeTransactionId = loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 100.0)
                    .getResourceId();
            capitalizedIncomeTransactionIdRef.set(capitalizedIncomeTransactionId);

            verifyBusinessEvents(
                    new LoanTransactionBusinessEvent("LoanCapitalizedIncomeTransactionCreatedBusinessEvent", "01 January 2024", 100.0,
                            200.0, 100.0, 0.0, 0.0, 0.0),
                    new LoanBusinessEvent("LoanBalanceChangedBusinessEvent", "01 January 2024", 300, 100.0, 200.0));
        });
        runAt("2 January 2024", () -> {
            Long loanId = loanIdRef.get();

            deleteAllExternalEvents();

            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024") //
            );
            verifyBusinessEvents(new LoanTransactionBusinessEvent("LoanCapitalizedIncomeAmortizationTransactionCreatedBusinessEvent",
                    "01 January 2024", 1.10, 0.0, 0.0, 0.0, 1.10, 0.0));
        });
        runAt("3 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            deleteAllExternalEvents();

            Long capitalizedIncomeAdjustmentTransactionId = loanTransactionHelper
                    .capitalizedIncomeAdjustment(loanId, capitalizedIncomeTransactionIdRef.get(), "3 January 2024", 50.0).getResourceId();

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.04, "Accrual", "02 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "02 January 2024"), //
                    transaction(50.0, "Capitalized Income Adjustment", "03 January 2024") //
            );

            verifyBusinessEvents(
                    new LoanTransactionBusinessEvent("LoanCapitalizedIncomeAdjustmentTransactionCreatedBusinessEvent", "03 January 2024",
                            50.0, 150.0, 50.0, 0.0, 0.0, 0.0),
                    new LoanBusinessEvent("LoanBalanceChangedBusinessEvent", "03 January 2024", 300, 100.0, 150.0));

            deleteAllExternalEvents();

            loanTransactionHelper.reverseLoanTransaction(loanId, capitalizedIncomeAdjustmentTransactionId, "3 January 2024");

            verifyBusinessEvents(new LoanAdjustTransactionBusinessEvent("LoanAdjustTransactionBusinessEvent", "03 January 2024",
                    "loanTransactionType.capitalizedIncomeAdjustment", "2024-01-03"));
        });
        runAt("4 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            deleteAllExternalEvents();

            loanTransactionHelper.reverseLoanTransaction(loanId, capitalizedIncomeTransactionIdRef.get(), "3 January 2024");

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Capitalized Income", "01 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.04, "Accrual", "02 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "02 January 2024"), //
                    transaction(0.04, "Accrual", "03 January 2024"), //
                    transaction(1.10, "Capitalized Income Amortization", "02 January 2024"), //
                    transaction(50.0, "Capitalized Income Adjustment", "03 January 2024") //
            );

            verifyBusinessEvents(new LoanAdjustTransactionBusinessEvent("LoanAdjustTransactionBusinessEvent", "04 January 2024",
                    "loanTransactionType.capitalizedIncome", "2024-01-01") //
            );
        });
    }
}
