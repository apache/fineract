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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanChargeSpecificDueDateTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;

    private static final String principalAmount = "1000.00";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(this.requestSpec, this.responseSpec);
        accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testApplyLoanSpecificDueDateFeeWithDisbursementDate() {

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithPeriodicAccrual(loanTransactionHelper,
                null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "12", "0");

        // Get loan details
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        // Apply Loan Charge with specific due date

        final String feeAmount = "10.00";
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount, false);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), operationDate, feeAmount);
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), false);

        // Run Accruals
        log.info("Running Periodic Accrual for date {}", transactionDate);
        periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);

        final Long transactionId = loanTransactionHelper.evaluateLastLoanTransactionData(getLoansLoanIdResponse,
                "loanTransactionType.accrual", operationDate, Double.valueOf("10.00"));
        assertNotNull(transactionId);
        log.info("transactionId {}", transactionId);

        final GetJournalEntriesTransactionIdResponse journalEntriesResponse = journalEntryHelper.getJournalEntries("L" + transactionId);
        assertNotNull(journalEntriesResponse);
        final List<JournalEntryTransactionItem> journalEntries = journalEntriesResponse.getPageItems();
        assertEquals(2, journalEntries.size());
        assertEquals(10, journalEntries.get(0).getAmount());
        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(transactionDate, journalEntries.get(0).getTransactionDate());
        assertEquals(transactionDate, journalEntries.get(1).getTransactionDate());

        // Make a full repayment to close the Loan
        Float amount = Float.valueOf("1010.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf("0.00"), Double.valueOf("0.00"), false);

    }

    @Test
    public void testApplyLoanSpecificDueDatePenaltyWithDisbursementDate() {

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithPeriodicAccrual(loanTransactionHelper,
                null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "12", "0");

        // Get loan details
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        // Apply Loan Charge with specific due date

        final String feeAmount = "10.00";
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount, true);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();
        assertNotNull(loanChargeId);

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), operationDate, feeAmount);
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        // Run Accruals
        log.info("Running Periodic Accrual for date {}", transactionDate);
        periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);

        final Long transactionId = loanTransactionHelper.evaluateLastLoanTransactionData(getLoansLoanIdResponse,
                "loanTransactionType.accrual", operationDate, Double.valueOf("10.00"));
        assertNotNull(transactionId);
        log.info("transactionId {}", transactionId);

        final GetJournalEntriesTransactionIdResponse journalEntriesResponse = journalEntryHelper.getJournalEntries("L" + transactionId);
        assertNotNull(journalEntriesResponse);
        final List<JournalEntryTransactionItem> journalEntries = journalEntriesResponse.getPageItems();
        assertEquals(2, journalEntries.size());
        assertEquals(10, journalEntries.get(0).getAmount());
        assertEquals(10, journalEntries.get(1).getAmount());
        assertEquals(transactionDate, journalEntries.get(0).getTransactionDate());
        assertEquals(transactionDate, journalEntries.get(1).getTransactionDate());

        // Make a full repayment to close the Loan
        Float amount = Float.valueOf("1010.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf("0.00"), Double.valueOf("0.00"), true);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

    }

    @Test
    public void testApplyAndWaiveInstallmentFee() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "1", "0");

        // Get loan details
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        // Apply Loan Charge with specific due date
        final String feeAmount = "1.500000";
        String payloadJSON = ChargesHelper.getLoanSpecificInstallmentFeeJSON();
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long chargeId = postChargesResponse.getResourceId();
        assertNotNull(chargeId);

        float amount = Float.parseFloat("5.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);

        payloadJSON = LoanTransactionHelper.getSpecifiedInstallmentChargesForLoanAsJSON(chargeId.toString(), feeAmount);
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);
        final Long loanChargeId = postLoansLoanIdChargesResponse.getResourceId();
        assertNotNull(loanChargeId);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), false);

        // Waive the Loan Charge
        final PostLoansLoanIdChargesChargeIdResponse postWaiveLoanChargesResponse = loanTransactionHelper.applyLoanChargeCommand(loanId,
                loanChargeId, "waive", Utils.emptyJson());
        assertNotNull(postWaiveLoanChargesResponse);

        // evaluate the outstanding
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        Optional<GetLoansLoanIdTransactions> waiveTransaction = getLoansLoanIdResponse.getTransactions().stream()
                .filter(transaction -> transaction.getType().getWaiveCharges() != null && transaction.getType().getWaiveCharges())
                .findFirst();
        assertTrue(waiveTransaction.isPresent());
        assertEquals(transactionDate, waiveTransaction.get().getDate());

        // Make a full repayment to close the Loan
        amount = Float.parseFloat("1000.00");
        loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount, loanId);
        assertNotNull(loanIdTransactionsResponse);
        log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

    }

    @Test
    public void testApplyAndWaiveInstallmentFeeAnotherDueDate() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "1", "0");

        // Get loan details
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        // Apply Loan Charge with specific due date
        final String feeAmount = "1.500000";
        String payloadJSON = ChargesHelper.getLoanSpecificInstallmentFeeJSON();
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long chargeId = postChargesResponse.getResourceId();
        assertNotNull(chargeId);

        float amount = Float.parseFloat("5.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        transactionDate = todaysDate.plusDays(32);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, transactionDate);

        payloadJSON = LoanTransactionHelper.getSpecifiedInstallmentChargesForLoanAsJSON(chargeId.toString(), feeAmount);
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);
        final Long loanChargeId = postLoansLoanIdChargesResponse.getResourceId();
        assertNotNull(loanChargeId);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), false);
        LocalDate repaymentDueDate = getLoansLoanIdResponse.getRepaymentSchedule().getPeriods().get(1).getDueDate();
        // Waive the Loan Charge
        final PostLoansLoanIdChargesChargeIdResponse postWaiveLoanChargesResponse = loanTransactionHelper.applyLoanChargeCommand(loanId,
                loanChargeId, "waive", Utils.emptyJson());
        assertNotNull(postWaiveLoanChargesResponse);

        // evaluate the outstanding
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), false);

        Optional<GetLoansLoanIdTransactions> waiveTransaction = getLoansLoanIdResponse.getTransactions().stream()
                .filter(transaction -> transaction.getType().getWaiveCharges() != null && transaction.getType().getWaiveCharges())
                .findFirst();
        assertTrue(waiveTransaction.isPresent());
        assertEquals(repaymentDueDate, waiveTransaction.get().getDate());

        // Make a full repayment to close the Loan
        amount = Float.parseFloat("1000.00");
        loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount, loanId);
        assertNotNull(loanIdTransactionsResponse);
        log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

    }

    @Test
    public void testApplyAndWaiveLoanSpecificDueDatePenaltyWithDisbursementDate() {

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "12", "0");

        // Get loan details
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        // Apply Loan Charge with specific due date
        final String feeAmount = "10.00";
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount, true);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long chargeId = postChargesResponse.getResourceId();
        assertNotNull(chargeId);

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(chargeId.toString(), operationDate, feeAmount);
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);
        final Long loanChargeId = postLoansLoanIdChargesResponse.getResourceId();
        assertNotNull(loanChargeId);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        // Waive the Loan Charge
        final PostLoansLoanIdChargesChargeIdResponse postWaiveLoanChargesResponse = loanTransactionHelper.applyLoanChargeCommand(loanId,
                loanChargeId, "waive", Utils.emptyJson());
        assertNotNull(postWaiveLoanChargesResponse);

        // evaluate the outstanding
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        // Make a full repayment to close the Loan
        Float amount = Float.valueOf("1000.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

    }

    @Test
    public void testApplyFeeAccrualOnClosedDate() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);

            // Client and Loan account creation
            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithPeriodicAccrual(
                    loanTransactionHelper, null);
            assertNotNull(getLoanProductsProductResponse);

            LocalDate transactionDate = LocalDate.of(Utils.getLocalDateOfTenant().getYear(), 1, 1);
            String operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Disbursement date {}", transactionDate);

            // Create Loan Account
            final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                    getLoanProductsProductResponse.getId().toString(), operationDate, "1", "0");

            // Get loan details
            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

            // Apply Loan Charge with specific due date
            String feeAmount = "10.00";
            String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount, true);
            final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
            assertNotNull(postChargesResponse);
            final Long chargeId = postChargesResponse.getResourceId();
            assertNotNull(chargeId);

            // First Loan Charge
            transactionDate = transactionDate.plusDays(1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, transactionDate);
            operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Operation date {}", transactionDate);
            payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(chargeId.toString(), operationDate, feeAmount);
            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                    responseSpec);
            assertNotNull(postLoansLoanIdChargesResponse);
            final Long loanChargeId01 = postLoansLoanIdChargesResponse.getResourceId();
            assertNotNull(loanChargeId01);

            // Run Accruals
            log.info("Running Periodic Accrual for date {}", transactionDate);
            periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            loanTransactionHelper.evaluateLoanTransactionData(getLoansLoanIdResponse, "loanTransactionType.accrual",
                    Double.valueOf("10.00"));

            // Repay the first charge fully, 10
            Float amount = Float.valueOf("10.00");
            transactionDate = transactionDate.plusDays(40);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, transactionDate);
            operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Operation date {}", transactionDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                    loanId);
            assertNotNull(loanIdTransactionsResponse);
            log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

            // Second Loan Charge
            feeAmount = "15.00";
            transactionDate = transactionDate.plusDays(1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, transactionDate);
            operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Operation date {}", transactionDate);
            payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(chargeId.toString(), operationDate, feeAmount);
            postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON, responseSpec);
            assertNotNull(postLoansLoanIdChargesResponse);
            final Long loanChargeId02 = postLoansLoanIdChargesResponse.getResourceId();
            assertNotNull(loanChargeId02);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("15.00"), true);

            // Run Accruals
            log.info("Running Periodic Accrual for date {}", transactionDate);
            periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            loanTransactionHelper.evaluateLoanTransactionData(getLoansLoanIdResponse, "loanTransactionType.accrual",
                    Double.valueOf("25.00"));

            // Third Loan Charge
            feeAmount = "25.00";
            transactionDate = transactionDate.plusDays(1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, transactionDate);
            operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Operation date {}", transactionDate);
            payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(chargeId.toString(), operationDate, feeAmount);
            postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON, responseSpec);
            assertNotNull(postLoansLoanIdChargesResponse);
            final Long loanChargeId03 = postLoansLoanIdChargesResponse.getResourceId();
            assertNotNull(loanChargeId03);

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("40.00"), true);
            loanTransactionHelper.evaluateLoanTransactionData(getLoansLoanIdResponse, "loanTransactionType.accrual",
                    Double.valueOf("25.00"));

            amount = Float.valueOf("1040.00");
            loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");
            loanTransactionHelper.evaluateLoanTransactionData(getLoansLoanIdResponse, "loanTransactionType.accrual",
                    Double.valueOf("50.00"));

            final Long transactionId = loanTransactionHelper.evaluateLastLoanTransactionData(getLoansLoanIdResponse,
                    "loanTransactionType.accrual", operationDate, Double.valueOf("25.00"));
            assertNotNull(transactionId);
            log.info("transactionId {}", transactionId);

            final GetJournalEntriesTransactionIdResponse journalEntriesResponse = journalEntryHelper
                    .getJournalEntries("L" + transactionId.toString());
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
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);

            // Client and Loan account creation
            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithPeriodicAccrual(
                    loanTransactionHelper, null);
            assertNotNull(getLoanProductsProductResponse);

            LocalDate transactionDate = LocalDate.of(Utils.getLocalDateOfTenant().getYear(), 1, 1);
            String operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Disbursement date {}", transactionDate);

            // Create Loan Account
            final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                    getLoanProductsProductResponse.getId().toString(), operationDate, "1", "0");

            // Get loan details
            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

            // Apply Loan Charge with specific due date
            String feeAmount = "10.00";
            String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount, true);
            final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
            assertNotNull(postChargesResponse);
            final Long chargeId = postChargesResponse.getResourceId();
            assertNotNull(chargeId);

            // First Loan Charge
            transactionDate = transactionDate.plusDays(1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, transactionDate);
            operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Operation date {}", transactionDate);
            payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(chargeId.toString(), operationDate, feeAmount);
            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                    responseSpec);
            assertNotNull(postLoansLoanIdChargesResponse);
            final Long loanChargeId01 = postLoansLoanIdChargesResponse.getResourceId();
            assertNotNull(loanChargeId01);

            Float amount = Float.valueOf("1020.00");
            transactionDate = transactionDate.plusDays(2);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, transactionDate);
            operationDate = Utils.dateFormatter.format(transactionDate);
            log.info("Operation date {}", transactionDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                    loanId);
            assertNotNull(loanIdTransactionsResponse);
            log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

            final Long transactionId = loanTransactionHelper.evaluateLastLoanTransactionData(getLoansLoanIdResponse,
                    "loanTransactionType.accrual", operationDate, Double.valueOf("10.00"));
            assertNotNull(transactionId);
            log.info("transactionId {}", transactionId);

            final GetJournalEntriesTransactionIdResponse journalEntriesResponse = journalEntryHelper.getJournalEntries("L" + transactionId);
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

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithPeriodicAccrual(loanTransactionHelper,
                null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "12", "0");

        // Get loan details
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        // Apply Loan Charge with specific due date

        final String feeAmount = "10.00";
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount, true);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();
        assertNotNull(loanChargeId);

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), operationDate, feeAmount);
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        loanTransactionHelper.disburseLoan((long) loanId, new PostLoansLoanIdRequest().actualDisbursementDate(operationDate)
                .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.parseDouble(principalAmount) * 2, Double.valueOf("10.00"), true);

        operationDate = Utils.dateFormatter.format(transactionDate.plusMonths(1));
        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), operationDate, feeAmount);
        postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON, responseSpec);

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.parseDouble(principalAmount) * 2, Double.valueOf("20.00"), true);
    }

    @Test
    public void testApplyLoanSpecificDueDatePenaltyAccrualWithDisbursementDateWithMultipleDisbursement() {

        final LocalDate todaysDate = Utils.getLocalDateOfTenant();

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithPeriodicAccrual(loanTransactionHelper,
                null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate.minusDays(2);
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate, "12", "0");

        // Get loan details
        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("0.00"), true);

        // Apply Loan Charge with specific due date

        final String feeAmount = "10.00";
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount, true);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();
        assertNotNull(loanChargeId);

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), operationDate, feeAmount);
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        transactionDate = transactionDate.plusDays(1);
        operationDate = Utils.dateFormatter.format(transactionDate);

        loanTransactionHelper.disburseLoan((long) loanId, new PostLoansLoanIdRequest().actualDisbursementDate(operationDate)
                .transactionAmount(new BigDecimal("1000")).locale("en").dateFormat("dd MMMM yyyy"));

        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.parseDouble(principalAmount) * 2, Double.valueOf("10.00"), true);

        periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);

        operationDate = Utils.dateFormatter.format(transactionDate.plusMonths(1));
        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), operationDate, feeAmount);
        postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON, responseSpec);

        transactionDate = transactionDate.plusDays(1);
        operationDate = Utils.dateFormatter.format(transactionDate);
        periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(operationDate);
        // Get loan details expecting to have a delinquency classification
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        validateLoanAccount(getLoansLoanIdResponse, Double.parseDouble(principalAmount) * 2, Double.valueOf("20.00"), true);
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private GetLoanProductsProductIdResponse createLoanProductWithPeriodicAccrual(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().withMultiDisburse()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .withDisallowExpectedDisbursements(true) //
                .build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate, final String repayments, final String interestRate) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount).withLoanTermFrequency(repayments)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(repayments).withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod(interestRate) //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .build(clientId, loanProductId, null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, principalAmount, loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanId, principalAmount);
        return loanId;
    }

    private void validateLoanAccount(GetLoansLoanIdResponse getLoansLoanIdResponse, Double principal, Double fees, boolean isPenalty) {
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, principal);
        if (isPenalty) {
            loanTransactionHelper.validateLoanPenaltiesOustandingBalance(getLoansLoanIdResponse, fees);
        } else {
            loanTransactionHelper.validateLoanFeesOustandingBalance(getLoansLoanIdResponse, fees);
        }
        loanTransactionHelper.validateLoanTotalOustandingBalance(getLoansLoanIdResponse, (principal + fees));
    }

}
