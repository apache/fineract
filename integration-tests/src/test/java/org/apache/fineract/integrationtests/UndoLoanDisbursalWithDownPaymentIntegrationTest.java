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

import static java.lang.Boolean.TRUE;
import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class UndoLoanDisbursalWithDownPaymentIntegrationTest extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void testUndoDisbursalForLoanWithSingleDisbursalAutoDownPaymentEnabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, false);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    // original entries reverted
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT")); //

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithSingleDisbursalAutoDownPaymentEnabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, false);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // make a repayment
            addRepaymentForLoan(loanId, 100.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Repayment", "01 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // repayment entries
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //

                    // original entries compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // repayment entries compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithSingleDisbursalAutoDownPaymentDisabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, false);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // original entries are compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT") //
            );

            // verify repayment entries are reverted
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithSingleDisbursalAutoDownPaymentDisabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, false);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // An extra Manual Repayment after the down-payment
            addRepaymentForLoan(loanId, 100.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(100.0, "Repayment", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // original entries compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // manual partial repayment of the first installment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //

                    // manual partial repayment of the first installment compensation after undoDisburse
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoLastDisbursalForLoanWithSingleDisbursalAutoDownPaymentEnabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, false);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            verifyUndoLastDisbursalShallFail(loanId, "error.msg.loan.product.does.not.support.multiple.disbursals.cannot.undo.last");

        });
    }

    @Test
    public void testUndoLastDisbursalForLoanWithMultiDisbursalAutoDownPaymentEnabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            verifyUndoLastDisbursalShallFail(loanId, "error.msg.tranches.should.be.disbursed.more.than.one.to.undo.last.disbursal");
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalAutoDownPaymentEnabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    // original entries reverted
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalAutoDownPaymentEnabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // make a repayment
            addRepaymentForLoan(loanId, 100.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Repayment", "01 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // repayment entries
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //

                    // original entries compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // repayment entries compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalAutoDownPaymentDisabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023")//
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // original entries are compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalAutoDownPaymentDisabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023"));//

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // An extra Manual Repayment after the down-payment
            addRepaymentForLoan(loanId, 100.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(100.0, "Repayment", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // original entries compensated
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // manual partial repayment of the first installment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //

                    // manual partial repayment of the first installment compensation after undoDisburse
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023"));//
        });
    }

    @Test
    public void testUndoLastDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentEnabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Down Payment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            // undoLastDisbursal
            loanTransactionHelper.undoLastDisbursal(loanId.intValue());

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId,
                    // first disbursement + down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // second disbursement + down-payment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT"), //

                    // compensation of second disbursement + down-payment
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT"), //
                    journalEntry(400.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(400.0, fundSource, "DEBIT") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoLastDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentEnabledAndNoManualTransactionsWithExtraRepayment() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("10 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            addRepaymentForLoan(loanId, 300.0, "10 January 2023");

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(300.0, "Repayment", "10 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(300.0, "Repayment", "10 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023"), //
                    transaction(100.0, "Down Payment", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(300.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(300.0, fundSource, "DEBIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            // undoLastDisbursal
            loanTransactionHelper.undoLastDisbursal(loanId.intValue());

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(300.0, "Repayment", "10 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId,
                    // first disbursement + down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // repayment
                    journalEntry(300.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(300.0, fundSource, "DEBIT"), //

                    // second disbursement + down-payment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT"), //

                    // compensation of second disbursement + down-payment
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT"), //
                    journalEntry(400.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(400.0, fundSource, "DEBIT") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoLastDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentDisabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 100.0, "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Repayment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            // undoLastDisbursal
            loanTransactionHelper.undoLastDisbursal(loanId.intValue());

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId,
                    // first disbursement + down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // second disbursement + down-payment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT"), //

                    // compensation of second disbursement + down-payment
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT"), //
                    journalEntry(400.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(400.0, fundSource, "DEBIT") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );
        });
    }

    @Test
    public void testUndoLastDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentEnabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Down Payment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("20 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // make an additional repayment after the 2nd disbursal
            addRepaymentForLoan(loanId, 50.0, "20 January 2023");

            // undo last disbursal shall fail
            verifyUndoLastDisbursalShallFail(loanId, "error.msg.cannot.undo.last.disbursal.after.repayments or waivers");
        });
    }

    @Test
    public void testUndoLastDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentDisabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 100.0, "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Repayment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("20 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // make an additional repayment after the 2nd disbursal
            addRepaymentForLoan(loanId, 50.0, "20 January 2023");

            // undo last disbursal shall fail
            verifyUndoLastDisbursalShallFail(loanId, "error.msg.cannot.undo.last.disbursal.after.repayments or waivers");
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentEnabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Down Payment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            verifyNoTransactions(loanId);

            // verify journal entries
            verifyJournalEntries(loanId,
                    // 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT"), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT"), //
                    journalEntry(400.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(400.0, fundSource, "DEBIT") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentDisabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 100.0, "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Repayment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            verifyNoTransactions(loanId);

            // verify journal entries
            verifyJournalEntries(loanId,
                    // 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT"), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT"), //
                    journalEntry(400.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(400.0, fundSource, "DEBIT") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentEnabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Down Payment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("20 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // make an additional repayment after the 2nd disbursal
            addRepaymentForLoan(loanId, 50.0, "20 January 2023");

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            verifyNoTransactions(loanId);

            // verify journal entries
            verifyJournalEntries(loanId,
                    // 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT"), //

                    // manual repayment
                    journalEntry(50.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(50.0, fundSource, "DEBIT"), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT"), //
                    journalEntry(400.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(400.0, fundSource, "DEBIT"), //

                    // compensation of repayment
                    journalEntry(50.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50.0, fundSource, "CREDIT") //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentDisabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(false, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 250.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("15 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(400.0), "15 January 2023");

            // Manual down-payment
            addRepaymentForLoan(loanId, 100.0, "15 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(100.0, "Repayment", "15 January 2023"), //
                    transaction(400.0, "Disbursement", "15 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BUSINESS_DATE.getName()).date("20 January 2023")
                    .dateFormat(DATETIME_PATTERN).locale("en"));

            // make an additional repayment after the 2nd disbursal
            addRepaymentForLoan(loanId, 50.0, "20 January 2023");

            // undoDisbursal
            loanTransactionHelper.undoDisbursal(loanId.intValue());

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(1125.0, false, "31 January 2023") //
            );

            verifyNoTransactions(loanId);

            // verify journal entries
            verifyJournalEntries(loanId,
                    // 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(100.0, fundSource, "DEBIT"), //
                    journalEntry(400.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(400.0, fundSource, "CREDIT"), //

                    // manual repayment
                    journalEntry(50.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(50.0, fundSource, "DEBIT"), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(1000.0, fundSource, "DEBIT"), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100.0, fundSource, "CREDIT"), //
                    journalEntry(400.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(400.0, fundSource, "DEBIT"), //

                    // compensation of repayment
                    journalEntry(50.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50.0, fundSource, "CREDIT") //
            );
        });
    }

    private Long createLoanProductWith25PctDownPayment(boolean autoDownPaymentEnabled, boolean multiDisburseEnabled) {
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0, getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;
    }

}
