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
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanApplicationUndoLastTrancheTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanApplicationUndoLastTrancheTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanApplicationApprovalTest loanApplicationApprovalTest;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanApplicationApprovalTest = new LoanApplicationApprovalTest();
    }

    @Test
    public void loanApplicationUndoLastTranche() {
        final String proposedAmount = "5000";
        final String approvalAmount = "2000";
        final String approveDate = "01 March 2014";
        final String expectedDisbursementDate = "01 March 2014";
        final String disbursalDate = "01 March 2014";

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper
                .getLoanProductId(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                        .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).build(null));
        LOG.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductID);

        // CREATE TRANCHES
        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("01 March 2014", "1000"));
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("23 June 2014", "4000"));

        // APPROVE TRANCHES
        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("01 March 2014", "1000"));
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("23 June 2014", "1000"));

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, "2", createTranches);
        LOG.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}", loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A LOAN
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(disbursalDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN IS ACTIVE STATUS
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        LOG.info("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("01 April 2014", Float.valueOf("420"), loanID);
        LOG.info("-------------Make repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("01 May 2014", Float.valueOf("412"), loanID);
        LOG.info("-------------Make repayment 3-----------");
        this.loanTransactionHelper.makeRepayment("01 June 2014", Float.valueOf("204"), loanID);
        // DISBURSE A SECOND TRANCHE
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("23 June 2014", loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        // UNDO LAST TRANCHE
        Float disbursedAmount = this.loanTransactionHelper.undoLastDisbursal(loanID);
        validateDisbursedAmount(disbursedAmount);
    }

    @Test
    public void loanApplicationUndoLastTrancheToClose() {
        LocalDate transactionDate = LocalDate.of(2014, 3, 1);
        String operationDate = Utils.dateFormatter.format(transactionDate);
        LOG.info("Operation date {}", transactionDate);

        final String proposedAmount = "1000";

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper
                .getLoanProductId(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                        .withDisallowExpectedDisbursements(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).build(null));
        LOG.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductID);

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, "0", new ArrayList<>());

        LOG.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}", loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        this.loanTransactionHelper.approveLoan(operationDate, proposedAmount, loanID, null);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.approved");

        // DISBURSE A LOAN
        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanID, "500");
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");
        loanTransactionHelper.evaluateLoanDisbursementDetails(getLoansLoanIdResponse, 1, Double.valueOf("500.00"));

        // DISBURSE A LOAN (second)
        transactionDate = transactionDate.plusDays(2);
        operationDate = Utils.dateFormatter.format(transactionDate);
        LOG.info("Operation date {}", transactionDate);
        loanTransactionHelper.disburseLoanWithTransactionAmount(operationDate, loanID, "500");
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");
        loanTransactionHelper.evaluateLoanDisbursementDetails(getLoansLoanIdResponse, 2, Double.valueOf("1000.00"));

        // BACKDATE REPAYMENT
        transactionDate = transactionDate.minusDays(1);
        operationDate = Utils.dateFormatter.format(transactionDate);
        LOG.info("Operation date {}", transactionDate);
        Float amount = Float.valueOf("500.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanID);
        assertNotNull(loanIdTransactionsResponse);
        LOG.info("Loan Transaction Id: {} {}", loanID, loanIdTransactionsResponse.getResourceId());
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");
        loanTransactionHelper.evaluateLoanDisbursementDetails(getLoansLoanIdResponse, 2, Double.valueOf("1000.00"));
        loanTransactionHelper.validateLoanTotalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("500.00"));

        // UNDO LAST TRANCHE
        this.loanTransactionHelper.undoLastDisbursal(loanID);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");
        loanTransactionHelper.validateLoanTotalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("0.00"));
    }

    @Test
    public void loanApplicationUndoLastTrancheWithSameDate() {

        final String proposedAmount = "5000";
        final String approveDate = "01 March 2014";
        final String disbursalDate = "01 March 2014";

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper
                .getLoanProductId(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                        .withDisallowExpectedDisbursements(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).build(null));
        LOG.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductID);

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, "0", new ArrayList<>());

        LOG.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}", loanID);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        this.loanTransactionHelper.approveLoan(approveDate, proposedAmount, loanID, null);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.approved");

        // DISBURSE A LOAN
        loanTransactionHelper.disburseLoanWithTransactionAmount(disbursalDate, loanID, "1000");
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");
        loanTransactionHelper.evaluateLoanDisbursementDetails(getLoansLoanIdResponse, 1, Double.valueOf("1000.00"));

        // DISBURSE A LOAN (second)
        loanTransactionHelper.disburseLoanWithTransactionAmount(disbursalDate, loanID, "2000");
        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");
        loanTransactionHelper.evaluateLoanDisbursementDetails(getLoansLoanIdResponse, 2, Double.valueOf("3000.00"));

        // UNDO LAST TRANCHE
        this.loanTransactionHelper.undoLastDisbursal(loanID);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        assertNotNull(getLoansLoanIdResponse);
        // VALIDATE THE LOAN IS ACTIVE STATUS
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");
        loanTransactionHelper.evaluateLoanDisbursementDetails(getLoansLoanIdResponse, 1, Double.valueOf("1000.00"));
        loanTransactionHelper.validateLoanTotalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("1000.00"));
    }

    private void validateDisbursedAmount(Float disbursedAmount) {
        Assertions.assertEquals(Float.valueOf("1000.0"), disbursedAmount);

    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    public Integer applyForLoanApplicationWithTranches(final Integer clientID, final Integer loanProductID, String principal,
            final String interestRate, List<HashMap> tranches) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                clientID.toString(), collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
        LoanApplicationTestBuilder loanApplication = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("5") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("5") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod(interestRate) //
                .withExpectedDisbursementDate("01 March 2014") //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate("01 March 2014") //
                .withCollaterals(collaterals);

        if (tranches != null && tranches.size() > 0) {
            loanApplication = loanApplication.withTranches(tranches);
        }
        final String loanApplicationJSON = loanApplication.build(clientID.toString(), loanProductID.toString(), null);

        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

}
