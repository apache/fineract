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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.junit.jupiter.api.Test;

public class UndoLoanDisbursalWithDownPaymentIntegrationTest extends FeignLoanTestBase {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void testUndoDisbursalForLoanWithSingleDisbursalAutoDownPaymentEnabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    // original entries reverted
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name())); //

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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // repayment entries
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // original entries compensated
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // repayment entries compensated
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // undoDisbursal
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // original entries are compensated
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // original entries compensated
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // manual partial repayment of the first installment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // manual partial repayment of the first installment compensation after undoDisburse
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId, //
                    // original entries
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    // original entries reverted
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // repayment entries
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // original entries compensated
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // repayment entries compensated
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // undoDisbursal
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // original entries are compensated
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            // undoDisbursal
            undoDisbursement(loanId);

            // Verify that all transactions are reverted
            verifyNoTransactions(loanId);

            // verify journal entries are compensated after undo disbursal
            verifyJournalEntries(loanId,
                    // original entries
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // original entries compensated
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // manual partial repayment of the first installment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // manual partial repayment of the first installment compensation after undoDisburse
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            Long clientId = createClient();

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
            verifyJournalEntries(loanId,
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            undoLastDisbursement(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId,
                    // first disbursement + down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // second disbursement + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // compensation of second disbursement + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
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
            Long clientId = createClient();

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
            verifyJournalEntries(loanId,
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("10 January 2023");

            addRepaymentForLoan(loanId, 300.0, "10 January 2023");

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(300.0, "Repayment", "10 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(300.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(300.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            undoLastDisbursement(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(300.0, "Repayment", "10 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId,
                    // first disbursement + down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // repayment
                    journalEntry(300.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(300.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // second disbursement + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // compensation of second disbursement + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            undoLastDisbursement(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Repayment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId,
                    // first disbursement + down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // second disbursement + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // compensation of second disbursement + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            updateBusinessDate("20 January 2023");

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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            updateBusinessDate("20 January 2023");

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
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            undoDisbursement(loanId);

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentDisabledAndNoManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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
            undoDisbursement(loanId);

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentEnabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            updateBusinessDate("20 January 2023");

            // make an additional repayment after the 2nd disbursal
            addRepaymentForLoan(loanId, 50.0, "20 January 2023");

            // undoDisbursal
            undoDisbursement(loanId);

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // manual repayment
                    journalEntry(50.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(50.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of repayment
                    journalEntry(50.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(50.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );
        });
    }

    @Test
    public void testUndoDisbursalForLoanWithMultiDisbursalWith2DisburseAutoDownPaymentDisabledAndHasManualTransactions() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = createClient();

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(750.0, false, "31 January 2023") //
            );

            updateBusinessDate("15 January 2023");

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
            verifyJournalEntries(loanId,
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(400.0, null, "15 January 2023"), //
                    installment(100.0, true, "15 January 2023"), //
                    installment(1050.0, false, "31 January 2023") //
            );

            updateBusinessDate("20 January 2023");

            // make an additional repayment after the 2nd disbursal
            addRepaymentForLoan(loanId, 50.0, "20 January 2023");

            // undoDisbursal
            undoDisbursement(loanId);

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
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //

                    // manual repayment
                    journalEntry(50.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(50.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of the 1st disbursal + down-payment
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of the 2nd disbursal + down-payment
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(100.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(400.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()), //

                    // compensation of repayment
                    journalEntry(50.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(50.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()) //
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

        Long loanProductId = createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = retrieveLoanProduct(loanProductId);

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0, getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;
    }

}
