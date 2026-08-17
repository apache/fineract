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
import java.util.List;
import java.util.Optional;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.junit.jupiter.api.Test;

public class LoanChargeSpecificDueDateTest extends FeignLoanTestBase {

    private static final String principalAmount = "1000.00";

    @Test
    public void testApplyLoanSpecificDueDateFeeWithDisbursementDate() {
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        final Long clientId = createClient("01 January 2012");
        // Client and Loan account creation
        final Long loanProductId = createLoanProduct(testLoanProduct());

        final LocalDate transactionDate = todaysDate;
        // Older date to have more than one overdue installment
        final String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 12);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        // Get loan details
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        final Long loanChargeId = chargesHelper.createLoanSpecifiedDueDateCharge(10.0).getResourceId();
        assertNotNull(loanChargeId);
        addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(loanChargeId, 10.0, operationDate));

        // Apply Loan Charge with specific due date
        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("10.00"), false);

        runPeriodicAccrualAccounting(operationDate);
        loanDetails = getLoanDetails(loanId);

        // Get loan details expecting to have a delinquency classification
        final Long transactionId = evaluateLastLoanTransactionData(loanDetails, "loanTransactionType.accrual", operationDate,
                Double.valueOf("10.00"));
        assertNotNull(transactionId);

        // Run Accruals
        final GetJournalEntriesTransactionIdResponse journalEntriesResponse = getJournalEntries("L" + transactionId);
        assertNotNull(journalEntriesResponse);
        final List<JournalEntryTransactionItem> journalEntries = journalEntriesResponse.getPageItems();
        assertEquals(2, journalEntries.size());
        assertEquals(10, journalEntries.get(0).getAmount());
        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(transactionDate, journalEntries.get(0).getTransactionDate());
        assertEquals(transactionDate, journalEntries.get(1).getTransactionDate());

        // Make a full repayment to close the Loan
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeRepayment(operationDate, 1010.00f, loanId);
        assertNotNull(loanIdTransactionsResponse);

        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf("0.00"), Double.valueOf("0.00"), false);
    }

    @Test
    public void testApplyLoanSpecificDueDatePenaltyWithDisbursementDate() {
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        final Long clientId = createClient("01 January 2012");
        // Client and Loan account creation
        final Long loanProductId = createLoanProduct(testLoanProduct());

        final LocalDate transactionDate = todaysDate;
        // Older date to have more than one overdue installment
        final String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 12);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        // Get loan details
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        final Long loanChargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0).getResourceId();
        assertNotNull(loanChargeId);
        addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(loanChargeId, 10.0, operationDate));

        // Apply Loan Charge with specific due date
        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        runPeriodicAccrualAccounting(operationDate);
        loanDetails = getLoanDetails(loanId);

        // Get loan details expecting to have a delinquency classification
        final Long transactionId = evaluateLastLoanTransactionData(loanDetails, "loanTransactionType.accrual", operationDate,
                Double.valueOf("10.00"));
        assertNotNull(transactionId);

        // Run Accruals
        final GetJournalEntriesTransactionIdResponse journalEntriesResponse = getJournalEntries("L" + transactionId);
        assertNotNull(journalEntriesResponse);
        final List<JournalEntryTransactionItem> journalEntries = journalEntriesResponse.getPageItems();
        assertEquals(2, journalEntries.size());
        assertEquals(10, journalEntries.get(0).getAmount());
        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(transactionDate, journalEntries.get(0).getTransactionDate());
        assertEquals(transactionDate, journalEntries.get(1).getTransactionDate());

        // Make a full repayment to close the Loan
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeRepayment(operationDate, 1010.00f, loanId);
        assertNotNull(loanIdTransactionsResponse);

        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf("0.00"), Double.valueOf("0.00"), true);
        validateLoanStatus(loanDetails, "loanStatusType.closed.obligations.met");
    }

    @Test
    public void testApplyAndWaiveInstallmentFee() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        updateBusinessDate(Utils.dateFormatter.format(todaysDate));

        final Long clientId = createClient("01 January 2012");
        // Client and Loan account creation
        final Long loanProductId = createLoanProduct(testLoanProduct());

        final LocalDate transactionDate = todaysDate;
        // Older date to have more than one overdue installment
        final String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 1);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        // Get loan details
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        // Apply Loan Charge with specific due date
        final Long chargeId = chargesHelper.createCharge(installmentFee()).getResourceId();
        assertNotNull(chargeId);

        makeRepayment(operationDate, 5.00f, loanId);

        final Long loanChargeId = addChargesForLoan(loanId, installmentCharge(chargeId, 1.5)).getResourceId();
        assertNotNull(loanChargeId);

        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("10.00"), false);

        // Waive the Loan Charge
        waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

        loanDetails = getLoanDetails(loanId);
        // evaluate the outstanding
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        Optional<GetLoansLoanIdTransactions> waiveTransaction = loanDetails.getTransactions().stream()
                .filter(transaction -> transaction.getType().getWaiveCharges() != null && transaction.getType().getWaiveCharges())
                .findFirst();
        assertTrue(waiveTransaction.isPresent());
        assertEquals(transactionDate, waiveTransaction.get().getDate());

        // Make a full repayment to close the Loan
        makeRepayment(operationDate, 1000.00f, loanId);

        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.closed.obligations.met");
    }

    @Test
    public void testApplyAndWaiveInstallmentFeeAnotherDueDate() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        updateBusinessDate(Utils.dateFormatter.format(todaysDate));

        final Long clientId = createClient("01 January 2012");
        // Client and Loan account creation
        final Long loanProductId = createLoanProduct(testLoanProduct());

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        final String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 1);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        // Get loan details
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        // Apply Loan Charge with specific due date
        final Long chargeId = chargesHelper.createCharge(installmentFee()).getResourceId();
        assertNotNull(chargeId);

        makeRepayment(operationDate, 5.00f, loanId);
        transactionDate = todaysDate.plusDays(32);
        updateBusinessDate(Utils.dateFormatter.format(transactionDate));

        final Long loanChargeId = addChargesForLoan(loanId, installmentCharge(chargeId, 1.5)).getResourceId();
        assertNotNull(loanChargeId);

        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("10.00"), false);
        // Waive the Loan Charge
        LocalDate repaymentDueDate = loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate();

        // evaluate the outstanding
        waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        Optional<GetLoansLoanIdTransactions> waiveTransaction = loanDetails.getTransactions().stream()
                .filter(transaction -> transaction.getType().getWaiveCharges() != null && transaction.getType().getWaiveCharges())
                .findFirst();
        assertTrue(waiveTransaction.isPresent());
        assertEquals(repaymentDueDate, waiveTransaction.get().getDate());

        // Make a full repayment to close the Loan
        makeRepayment(operationDate, 1000.00f, loanId);

        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.closed.obligations.met");
    }

    @Test
    public void testApplyAndWaiveLoanSpecificDueDatePenaltyWithDisbursementDate() {
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        final Long clientId = createClient("01 January 2012");
        // Client and Loan account creation
        final Long loanProductId = createLoanProduct(testLoanProduct());

        final LocalDate transactionDate = todaysDate;
        // Older date to have more than one overdue installment
        final String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 12);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        // Get loan details
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        // Apply Loan Charge with specific due date
        final Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0).getResourceId();
        assertNotNull(chargeId);

        final Long loanChargeId = addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(chargeId, 10.0, operationDate))
                .getResourceId();
        assertNotNull(loanChargeId);

        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        // Waive the Loan Charge
        waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

        loanDetails = getLoanDetails(loanId);
        // evaluate the outstanding
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        // Make a full repayment to close the Loan
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeRepayment(operationDate, 1000.00f, loanId);
        assertNotNull(loanIdTransactionsResponse);

        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.closed.obligations.met");
    }

    @Test
    public void testApplyFeeAccrualOnClosedDate() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            updateBusinessDate(Utils.dateFormatter.format(todaysDate));

            final Long clientId = createClient("01 January 2012");
            // Client and Loan account creation
            final Long loanProductId = createLoanProduct(testLoanProduct());

            LocalDate transactionDate = LocalDate.of(Utils.getLocalDateOfTenant().getYear(), 1, 1);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 1);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            // Get loan details
            validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

            // Apply Loan Charge with specific due date
            String feeAmount = "10.00";
            final Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(Double.parseDouble(feeAmount)).getResourceId();
            assertNotNull(chargeId);

            // First Loan Charge
            transactionDate = transactionDate.plusDays(1);
            updateBusinessDate(Utils.dateFormatter.format(transactionDate));
            operationDate = Utils.dateFormatter.format(transactionDate);
            final Long loanChargeId01 = addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(chargeId, 10.0, operationDate))
                    .getResourceId();
            assertNotNull(loanChargeId01);

            runPeriodicAccrualAccounting(operationDate);
            // Run Accruals
            loanDetails = getLoanDetails(loanId);
            evaluateLoanTransactionData(loanDetails, "loanTransactionType.accrual", Double.valueOf("10.00"));

            // Repay the first charge fully, 10
            transactionDate = transactionDate.plusDays(40);
            updateBusinessDate(Utils.dateFormatter.format(transactionDate));
            operationDate = Utils.dateFormatter.format(transactionDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeRepayment(operationDate, 10.00f, loanId);
            assertNotNull(loanIdTransactionsResponse);

            // Second Loan Charge
            transactionDate = transactionDate.plusDays(1);
            updateBusinessDate(Utils.dateFormatter.format(transactionDate));
            operationDate = Utils.dateFormatter.format(transactionDate);
            final Long loanChargeId02 = addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(chargeId, 15.0, operationDate))
                    .getResourceId();
            assertNotNull(loanChargeId02);

            loanDetails = getLoanDetails(loanId);
            validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("15.00"), true);

            runPeriodicAccrualAccounting(operationDate);
            // Run Accruals
            loanDetails = getLoanDetails(loanId);
            evaluateLoanTransactionData(loanDetails, "loanTransactionType.accrual", Double.valueOf("25.00"));

            // Third Loan Charge
            transactionDate = transactionDate.plusDays(1);
            updateBusinessDate(Utils.dateFormatter.format(transactionDate));
            operationDate = Utils.dateFormatter.format(transactionDate);
            final Long loanChargeId03 = addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(chargeId, 25.0, operationDate))
                    .getResourceId();
            assertNotNull(loanChargeId03);

            loanDetails = getLoanDetails(loanId);
            validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("40.00"), true);
            evaluateLoanTransactionData(loanDetails, "loanTransactionType.accrual", Double.valueOf("25.00"));

            loanIdTransactionsResponse = makeRepayment(operationDate, 1040.00f, loanId);
            assertNotNull(loanIdTransactionsResponse);

            loanDetails = getLoanDetails(loanId);
            validateLoanStatus(loanDetails, "loanStatusType.closed.obligations.met");
            evaluateLoanTransactionData(loanDetails, "loanTransactionType.accrual", Double.valueOf("50.00"));

            final Long transactionId = evaluateLastLoanTransactionData(loanDetails, "loanTransactionType.accrual", operationDate,
                    Double.valueOf("25.00"));
            assertNotNull(transactionId);

            final GetJournalEntriesTransactionIdResponse journalEntriesResponse = getJournalEntries("L" + transactionId);
            assertNotNull(journalEntriesResponse);
            final List<JournalEntryTransactionItem> journalEntries = journalEntriesResponse.getPageItems();
            assertEquals(2, journalEntries.size());
            assertEquals(25, journalEntries.get(0).getAmount());
            assertEquals(25, journalEntries.get(1).getAmount());
            assertEquals(transactionDate, journalEntries.get(0).getTransactionDate());
            assertEquals(transactionDate, journalEntries.get(1).getTransactionDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testApplyFeeAccrualWhenLoanOverpaid() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            updateBusinessDate(Utils.dateFormatter.format(todaysDate));

            final Long clientId = createClient("01 January 2012");
            // Client and Loan account creation
            final Long loanProductId = createLoanProduct(testLoanProduct());

            LocalDate transactionDate = LocalDate.of(Utils.getLocalDateOfTenant().getYear(), 1, 1);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            // Create Loan Account
            final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 1);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            // Get loan details
            validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

            // Apply Loan Charge with specific due date
            String feeAmount = "10.00";
            final Long chargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(Double.parseDouble(feeAmount)).getResourceId();
            assertNotNull(chargeId);

            // First Loan Charge
            transactionDate = transactionDate.plusDays(1);
            updateBusinessDate(Utils.dateFormatter.format(transactionDate));
            operationDate = Utils.dateFormatter.format(transactionDate);
            final Long loanChargeId01 = addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(chargeId, 10.0, operationDate))
                    .getResourceId();
            assertNotNull(loanChargeId01);

            transactionDate = transactionDate.plusDays(2);
            updateBusinessDate(Utils.dateFormatter.format(transactionDate));
            operationDate = Utils.dateFormatter.format(transactionDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeRepayment(operationDate, 1020.00f, loanId);
            assertNotNull(loanIdTransactionsResponse);

            loanDetails = getLoanDetails(loanId);
            validateLoanStatus(loanDetails, "loanStatusType.overpaid");

            final Long transactionId = evaluateLastLoanTransactionData(loanDetails, "loanTransactionType.accrual", operationDate,
                    Double.valueOf("10.00"));
            assertNotNull(transactionId);

            final GetJournalEntriesTransactionIdResponse journalEntriesResponse = getJournalEntries("L" + transactionId);
            assertNotNull(journalEntriesResponse);
            final List<JournalEntryTransactionItem> journalEntries = journalEntriesResponse.getPageItems();
            assertEquals(2, journalEntries.size());
            assertEquals(10, journalEntries.get(0).getAmount());
            assertEquals(10, journalEntries.get(1).getAmount());
            assertEquals(transactionDate, journalEntries.get(0).getTransactionDate());
            assertEquals(transactionDate, journalEntries.get(1).getTransactionDate());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testApplyLoanSpecificDueDatePenaltyWithDisbursementDateWithMultipleDisbursement() {
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        final Long clientId = createClient("01 January 2012");
        // Client and Loan account creation
        final Long loanProductId = createLoanProduct(testLoanProduct());

        // Older date to have more than one overdue installment
        final LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 12);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        // Get loan details
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        final Long loanChargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0).getResourceId();
        assertNotNull(loanChargeId);
        addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(loanChargeId, 10.0, operationDate));

        // Apply Loan Charge with specific due date
        runPeriodicAccrualAccounting(operationDate);

        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(operationDate).transactionAmount(new BigDecimal("1000"))
                .locale("en").dateFormat(DATETIME_PATTERN));

        // Get loan details expecting to have a delinquency classification
        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.parseDouble(principalAmount) * 2, Double.valueOf("10.00"), true);

        operationDate = Utils.dateFormatter.format(transactionDate.plusMonths(1));
        addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(loanChargeId, 10.0, operationDate));

        loanDetails = getLoanDetails(loanId);
        // Get loan details expecting to have a delinquency classification
        validateLoanAccount(loanDetails, Double.parseDouble(principalAmount) * 2, Double.valueOf("20.00"), true);
    }

    @Test
    public void testApplyLoanSpecificDueDatePenaltyAccrualWithDisbursementDateWithMultipleDisbursement() {
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        final Long clientId = createClient("01 January 2012");
        // Client and Loan account creation
        final Long loanProductId = createLoanProduct(testLoanProduct());

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate.minusDays(2);
        String operationDate = Utils.dateFormatter.format(transactionDate);

        // Create Loan Account
        final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, 12);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        // Get loan details
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        final Long loanChargeId = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0).getResourceId();
        assertNotNull(loanChargeId);
        addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(loanChargeId, 10.0, operationDate));

        // Apply Loan Charge with specific due date
        runPeriodicAccrualAccounting(operationDate);

        loanDetails = getLoanDetails(loanId);
        // Get loan details expecting to have a delinquency classification
        validateLoanAccount(loanDetails, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        transactionDate = transactionDate.plusDays(1);
        operationDate = Utils.dateFormatter.format(transactionDate);

        disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(operationDate).transactionAmount(new BigDecimal("1000"))
                .locale("en").dateFormat(DATETIME_PATTERN));

        loanDetails = getLoanDetails(loanId);
        // Get loan details expecting to have a delinquency classification
        validateLoanAccount(loanDetails, Double.parseDouble(principalAmount) * 2, Double.valueOf("10.00"), true);

        runPeriodicAccrualAccounting(operationDate);

        operationDate = Utils.dateFormatter.format(transactionDate.plusMonths(1));
        addChargesForLoan(loanId, LoanRequestBuilders.addLoanCharge(loanChargeId, 10.0, operationDate));

        transactionDate = transactionDate.plusDays(1);
        operationDate = Utils.dateFormatter.format(transactionDate);
        runPeriodicAccrualAccounting(operationDate);

        // Get loan details expecting to have a delinquency classification
        loanDetails = getLoanDetails(loanId);
        validateLoanAccount(loanDetails, Double.parseDouble(principalAmount) * 2, Double.valueOf("20.00"), true);
    }

    private PostLoanProductsRequest testLoanProduct() {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .numberOfRepayments(1)//
                .maxNumberOfRepayments(30)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(1000);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String operationDate, final int repayments) {
        final Long loanId = applyForLoan(
                new PostLoansRequest().clientId(clientId).productId(loanProductId).principal(new BigDecimal(principalAmount))
                        .loanTermFrequency(repayments).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .numberOfRepayments(repayments).repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .interestRatePerPeriod(BigDecimal.ZERO).interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                        .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                        .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).expectedDisbursementDate(operationDate)
                        .submittedOnDate(operationDate).transactionProcessingStrategyCode("mifos-standard-strategy").loanType("individual")
                        .dateFormat(DATETIME_PATTERN).locale("en"));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(Double.valueOf(principalAmount), operationDate));
        disburseLoan(loanId, LoanRequestBuilders.disburseLoan(Double.valueOf(principalAmount), operationDate));
        return loanId;
    }

    private static ChargeRequest installmentFee() {
        return ChargeRequestBuilders.loanInstallmentFee(1.5)
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue()).penalty(false);
    }

    private PostLoansLoanIdChargesRequest installmentCharge(Long chargeId, double amount) {
        return new PostLoansLoanIdChargesRequest().chargeId(chargeId).locale("en").amount(amount);
    }

    private PostLoansLoanIdTransactionsResponse makeRepayment(String date, float amount, Long loanId) {
        return makeLoanRepayment(loanId, "repayment", date, (double) amount);
    }

    private void validateLoanAccount(GetLoansLoanIdResponse loanDetails, Double principal, Double fees, boolean isPenalty) {
        assertNotNull(loanDetails);
        final GetLoansLoanIdSummary summary = loanDetails.getSummary();
        assertNotNull(summary);
        assertEquals(principal, Utils.getDoubleValue(summary.getPrincipalOutstanding()));
        if (isPenalty) {
            assertEquals(fees, Utils.getDoubleValue(summary.getPenaltyChargesOutstanding()));
        } else {
            assertEquals(fees, Utils.getDoubleValue(summary.getFeeChargesOutstanding()));
        }
        assertEquals(principal + fees, Utils.getDoubleValue(summary.getTotalOutstanding()));
    }

    private Long evaluateLastLoanTransactionData(GetLoansLoanIdResponse loanDetails, String transactionType, String transactionExpected,
            Double amountExpected) {
        GetLoansLoanIdTransactions lastTransaction = null;
        for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
            if (transactionType.equals(transaction.getType().getCode())) {
                lastTransaction = transaction;
            }
        }
        assertNotNull(lastTransaction);
        assertEquals(transactionExpected, Utils.dateFormatter.format(lastTransaction.getDate()));
        assertEquals(amountExpected, Utils.getDoubleValue(lastTransaction.getAmount()));
        return lastTransaction.getId();
    }

    private void evaluateLoanTransactionData(GetLoansLoanIdResponse loanDetails, String transactionType, Double amountExpected) {
        Double transactionsAmount = 0.0;
        for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
            if (transactionType.equals(transaction.getType().getCode())) {
                transactionsAmount += Utils.getDoubleValue(transaction.getAmount());
            }
        }
        assertEquals(amountExpected, transactionsAmount);
    }
}
