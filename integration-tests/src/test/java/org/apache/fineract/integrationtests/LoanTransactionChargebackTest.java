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

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.GetPaymentTypesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanTransactionChargebackTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecError;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private final String amountVal = "1000";
    private LocalDate todaysDate;
    private String operationDate;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecError = new ResponseSpecBuilder().expectStatusCode(503).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        this.todaysDate = Utils.getLocalDateOfTenant();
        this.operationDate = Utils.dateFormatter.format(this.todaysDate);
    }

    @Test
    public void applyLoanTransactionChargeback() {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Integer chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "1000.00", 0, responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("1000.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, amount.doubleValue());

        // Try to reverse a Loan Transaction charge back
        PostLoansLoanIdTransactionsResponse reverseTransactionResponse = loanTransactionHelper.reverseLoanTransaction(loanId,
                chargebackTransactionId, operationDate, responseSpecError);

        // Try to reverse a Loan Transaction repayment with linked transactions
        reverseTransactionResponse = loanTransactionHelper.reverseLoanTransaction(loanId, transactionId, operationDate, responseSpecError);
    }

    @Test
    public void applyLoanTransactionChargebackInLongTermLoan() {
        // Client and Loan account creation
        final Integer daysToSubtract = 1;
        final Integer numberOfRepayments = 3;
        final Integer loanId = createAccounts(daysToSubtract, numberOfRepayments);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        final String baseAmount = "333.33";
        Float amount = Float.valueOf(baseAmount);
        final LocalDate transactionDate = this.todaysDate.minusMonths(numberOfRepayments - 1).plusDays(3);
        String operationDate = Utils.dateFormatter.format(transactionDate);

        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();
        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("666.67"));

        final Integer chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, amount.toString(), 0, responseSpec);
        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("666.67"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("1000.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf(amountVal));

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
        for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
            if (period.getPeriod() != null && period.getPeriod() == 3) {
                log.info("Period number {} for due date {} and totalDueForPeriod {}", period.getPeriod(), period.getDueDate(),
                        period.getTotalDueForPeriod());
                assertEquals(Double.valueOf("666.67"), period.getTotalDueForPeriod());
            }
        }

        evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf(baseAmount));
    }

    @Test
    public void applyLoanTransactionChargebackOverNoRepaymentType() {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        Set<GetLoansLoanIdTransactions> loanTransactions = getLoansLoanIdResponse.getTransactions();
        assertNotNull(loanTransactions);
        log.info("Loan Id {} with {} transactions", loanId, loanTransactions.size());
        assertEquals(2, loanTransactions.size());
        GetLoansLoanIdTransactions loanTransaction = loanTransactions.iterator().next();
        log.info("Try to apply the Charge back over transaction Id {} with type {}", loanTransaction.getId(),
                loanTransaction.getType().getCode());

        applyChargebackTransaction(loanId, loanTransaction.getId().intValue(), amountVal, 0, responseSpecError);
    }

    @Test
    public void applyLoanTransactionChargebackAfterMature() {
        // Client and Loan account creation
        final Integer loanId = createAccounts(45, 1);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
        log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
        assertEquals(2, getLoanRepaymentSchedule.getPeriods().size());

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        Integer chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "500.00", 0, responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("500.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("500.00"));

        // N+1 Scenario
        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
        log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
        assertEquals(3, getLoanRepaymentSchedule.getPeriods().size());
        getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
        for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
            if (period.getPeriod() != null && period.getPeriod() == 2) {
                log.info("Period number {} for due date {} and totalDueForPeriod {}", period.getPeriod(), period.getDueDate(),
                        period.getTotalDueForPeriod());
                assertEquals(Double.valueOf("500.00"), period.getPrincipalDue());
            }
        }

        chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "300.00", 0, responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 2, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("800.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("800.00"));

        // N+1 Scenario -- Remains the same periods number
        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
        log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
        assertEquals(3, getLoanRepaymentSchedule.getPeriods().size());
        getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
        for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
            if (period.getPeriod() != null && period.getPeriod() == 2) {
                log.info("Period number {} for due date {} and totalDueForPeriod {}", period.getPeriod(), period.getDueDate(),
                        period.getTotalDueForPeriod());
                assertEquals(Double.valueOf("800.00"), period.getPrincipalDue());
            }
        }
    }

    @Test
    public void applyLoanTransactionChargebackWithLoanOverpaidToLoanActive() {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Integer chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "200.00", 0, responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("100.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("100.00"));
    }

    @Test
    public void applyLoanTransactionChargebackWithLoanOverpaidToLoanClose() {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Integer chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "100.00", 0, responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("0.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("0.00"));
    }

    @Test
    public void applyLoanTransactionChargebackWithLoanOverpaidToKeepAsLoanOverpaid() {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Integer chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "50.00", 0, responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("0.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("0.00"));
    }

    @Test
    public void applyMultipleLoanTransactionChargeback() {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Integer transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        // First round, empty array
        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        applyChargebackTransaction(loanId, transactionId, "200.00", 0, responseSpec);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("200.00"));

        evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf("200.00"));

        // Second round, array size equal to 1
        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));

        applyChargebackTransaction(loanId, transactionId, "300.00", 1, responseSpec);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("500.00"));

        evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf("500.00"));

        // Third round, array size equal to 2
        reviewLoanTransactionRelations(loanId, transactionId, 2, Double.valueOf("0.00"));

        applyChargebackTransaction(loanId, transactionId, "500.00", 0, responseSpec);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("1000.00"));

        evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf("1000.00"));
    }

    private Integer createAccounts(final Integer daysToSubtract, final Integer numberOfRepayments) {
        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, null);

        // Older date to have more than one overdue installment
        final LocalDate transactionDate = this.todaysDate.minusDays(daysToSubtract + (30 * (numberOfRepayments - 1)));
        String operationDate = Utils.dateFormatter.format(transactionDate);

        return createLoanAccount(loanTransactionHelper, clientId.toString(), getLoanProductsProductResponse.getId().toString(),
                operationDate, amountVal, numberOfRepayments.toString());
    }

    private String createChargebackPayload(final String transactionAmount, final Integer paymentTypeId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("transactionAmount", transactionAmount);
        map.put("paymentTypeId", paymentTypeId);
        map.put("locale", CommonConstants.LOCALE);
        final String chargebackPayload = new Gson().toJson(map);
        log.info("{}", chargebackPayload);
        return chargebackPayload;
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate, final String principalAmount, final String numberOfRepayments) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount)
                .withLoanTermFrequency(numberOfRepayments).withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .build(clientId, loanProductId, null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, principalAmount, loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, principalAmount);
        return loanId;
    }

    private Integer applyChargebackTransaction(final Integer loanId, final Integer transactionId, final String amount,
            final Integer paymentTypeIdx, ResponseSpecification responseSpec) {
        List<GetPaymentTypesResponse> paymentTypeList = PaymentTypeHelper.getSystemPaymentType(this.requestSpec, this.responseSpec);
        assertTrue(!paymentTypeList.isEmpty());

        final String payload = createChargebackPayload(amount, paymentTypeList.get(paymentTypeIdx).getId());
        log.info("Loan Chargeback: {}", payload);
        PostLoansLoanIdTransactionsTransactionIdResponse postLoansTransactionCommandResponse = loanTransactionHelper
                .applyLoanTransactionCommand(loanId, transactionId, "chargeback", payload, responseSpec);
        assertNotNull(postLoansTransactionCommandResponse);

        log.info("Loan Chargeback Id: {}", postLoansTransactionCommandResponse.getResourceId());
        return postLoansTransactionCommandResponse.getResourceId();
    }

    private void reviewLoanTransactionRelations(final Integer loanId, final Integer transactionId, final Integer expectedSize,
            final Double outstandingBalance) {
        log.info("Loan Transaction Id: {} {}", loanId, transactionId);

        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper.getLoanTransaction(loanId,
                transactionId);
        log.info("Loan with {} Chargeback Transactions and balance {}", getLoansTransactionResponse.getTransactionRelations().size(),
                getLoansTransactionResponse.getOutstandingLoanBalance());
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());
        assertEquals(expectedSize, getLoansTransactionResponse.getTransactionRelations().size());
        // Outstanding amount
        assertEquals(outstandingBalance, getLoansTransactionResponse.getOutstandingLoanBalance());
    }

    private void evaluateLoanSummaryAdjustments(GetLoansLoanIdResponse getLoansLoanIdResponse, Double amountExpected) {
        // Evaluate The Loan Summary Principal Adjustments
        GetLoansLoanIdSummary getLoansLoanIdSummary = getLoansLoanIdResponse.getSummary();
        if (getLoansLoanIdSummary != null) {
            log.info("Loan with Principal Adjustments {} expected {}", getLoansLoanIdSummary.getPrincipalAdjustments(), amountExpected);
            assertEquals(amountExpected, getLoansLoanIdSummary.getPrincipalAdjustments());
        }
    }

}
