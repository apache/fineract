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
import java.util.ArrayList;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class BlockTransactionsOnClosedOverpaidLoansTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanTransactionHelper loanTransactionHelperForError;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private AccountHelper accountHelper;
    private ResponseSpecification responseSpecForError;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForError = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelperForError = new LoanTransactionHelper(this.requestSpec, this.responseSpecForError);
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
    }

    @AfterEach
    public void tearDown() {
        this.globalConfigurationHelper.manageConfigurations("block-transactions-on-closed-overpaid-loans", false);
    }

    @Test
    public void testTransactionsOnOverpaidLoan() {
        this.globalConfigurationHelper.manageConfigurations("block-transactions-on-closed-overpaid-loans", true);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer loanProductID = createLoanProduct();
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "01 January 2024");

        this.loanTransactionHelper.approveLoan("01 January 2024", loanID);
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("01 January 2024", loanID, "1000");

        this.loanTransactionHelper.makeRepayment("01 February 2024", 2000.0f, loanID);

        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID,
                "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        ArrayList<HashMap> repaymentErrors = (ArrayList<HashMap>) this.loanTransactionHelperForError.makeRepaymentTypePayment("repayment",
                "02 February 2024", 10.0f, loanID, CommonConstants.RESPONSE_ERROR);
        Assertions.assertEquals("error.msg.loan.transaction.not.allowed.on.closed.or.overpaid",
                repaymentErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        assertBlockedForClosedOrOverpaid("goodwillCredit", loanID, "02 February 2024", 10.0f);
        assertBlockedForClosedOrOverpaid("merchantIssuedRefund", loanID, "02 February 2024", 10.0f);
        assertBlockedForClosedOrOverpaid("payoutRefund", loanID, "02 February 2024", 10.0f);
        assertBlockedForClosedOrOverpaid("waiveinterest", loanID, "02 February 2024", 10.0f);

        Float totalOverpaid = (Float) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID,
                "totalOverpaid");
        assertNotNull(totalOverpaid);
        Assertions.assertTrue(totalOverpaid > 0);

        this.loanTransactionHelper.creditBalanceRefund("03 February 2024", totalOverpaid, null, loanID, "");

        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
    }

    @Test
    public void testTransactionsOnClosedLoan() {
        this.globalConfigurationHelper.manageConfigurations("block-transactions-on-closed-overpaid-loans", true);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer loanProductID = createLoanProduct();
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "01 January 2024");

        this.loanTransactionHelper.approveLoan("01 January 2024", loanID);
        this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("01 January 2024", loanID, "1000");

        HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        Float totalOutstanding = (Float) loanSummary.get("totalOutstanding");
        HashMap repaymentTransaction = this.loanTransactionHelper.makeRepayment("01 February 2024", totalOutstanding, loanID);
        Integer repaymentTransactionId = ((Number) repaymentTransaction.get("resourceId")).intValue();

        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID,
                "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        ArrayList<HashMap> repaymentErrors = (ArrayList<HashMap>) this.loanTransactionHelperForError.makeRepaymentTypePayment("repayment",
                "02 February 2024", 10.0f, loanID, CommonConstants.RESPONSE_ERROR);
        Assertions.assertEquals("error.msg.loan.transaction.not.allowed.on.closed.or.overpaid",
                repaymentErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        this.loanTransactionHelper.reverseRepayment(loanID, repaymentTransactionId, "03 February 2024");

        this.globalConfigurationHelper.manageConfigurations("block-transactions-on-closed-overpaid-loans", false);
        this.loanTransactionHelper.makeRepayment("04 February 2024", 10.0f, loanID);
    }

    private void assertBlockedForClosedOrOverpaid(final String command, final Integer loanId, final String date, final Float amount) {
        ArrayList<HashMap> errors = (ArrayList<HashMap>) this.loanTransactionHelperForError.makeRepaymentTypePayment(command, date, amount,
                loanId, CommonConstants.RESPONSE_ERROR);
        Assertions.assertEquals("error.msg.loan.transaction.not.allowed.on.closed.or.overpaid",
                errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
    }

    private Integer createLoanProduct() {
        final String principal = "1000.00";
        LoanProductTestBuilder loanProductTestBuilder = new LoanProductTestBuilder() //
                .withPrincipal(principal) //
                .withShortName(Utils.uniqueRandomStringGenerator("", 4)) //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance();

        final String loanProductJSON = loanProductTestBuilder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String principal, String submitDate) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("1") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(submitDate) //
                .withSubmittedOnDate(submitDate) //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
