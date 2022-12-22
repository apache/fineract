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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanChargeSpecificDueDateTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private static final String principalAmount = "1000.00";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testApplyLoanSpecificDueDateFeeWithDisbursementDate() {

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
                getLoanProductsProductResponse.getId().toString(), operationDate);

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
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, null);
        assertNotNull(getLoanProductsProductResponse);

        // Older date to have more than one overdue installment
        LocalDate transactionDate = todaysDate;
        String operationDate = Utils.dateFormatter.format(transactionDate);
        log.info("Operation date {}", transactionDate);

        // Create Loan Account
        final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                getLoanProductsProductResponse.getId().toString(), operationDate);

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
                getLoanProductsProductResponse.getId().toString(), operationDate);

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
        validateLoanAccount(getLoansLoanIdResponse, Double.valueOf(principalAmount), Double.valueOf("10.00"), true);

        // Make a full repayment to close the Loan
        Float amount = Float.valueOf("1010.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        log.info("Loan Transaction Id: {} {}", loanId, loanIdTransactionsResponse.getResourceId());

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId) {
        final HashMap<String, Object> loanProductMap = new LoanProductTestBuilder().build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount).withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths() //
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
