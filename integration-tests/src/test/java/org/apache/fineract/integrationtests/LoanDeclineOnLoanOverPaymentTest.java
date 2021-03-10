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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanDeclineOnLoanOverPaymentTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanDeclineOnLoanOverPaymentTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanApplicationApprovalTest loanApplicationApprovalTest;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private ResponseSpecification httpStatusForidden;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanApplicationApprovalTest = new LoanApplicationApprovalTest();
        this.httpStatusForidden = new ResponseSpecBuilder().expectStatusCode(400).build();
    }

    @Test
    public void loanApplicationOverPayment() {

        final String proposedAmount = "10000";
        final String approvalAmount = "10000";
        final String disburseAmount = "10000";
        final String amountToBePaid = "12000.00";
        Float RepaymentAmount = Float.valueOf(amountToBePaid);

        final String approveDate = "01 March 2015";
        final String expectedDisbursementDate = "01 March 2015";
        final String writeOffDate = "01 March 2015";
        final String disbursementDate = "01 March 2015";
        final String adjustRepaymentDate = "16 March 2015";
        List<HashMap> approveTranches = null;
        Integer configId = 0;

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        Integer loanID = applyForLoanApplication(clientID, loanProductID, proposedAmount);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------PENDING LOAN-----------------------------------------------------------");

        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);
        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(disbursementDate, loanID, disburseAmount);
        // loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LOG.info("-----------------------------------DISBURSE LOAN-----------------------------------------------------------");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // Retrieving All Global Configuration details
        final ArrayList<HashMap> globalConfig = GlobalConfigurationHelper.getAllGlobalConfigurations(requestSpec, responseSpec);
        Assertions.assertNotNull(globalConfig);

        // Updating Value for reschedule-repayments-on-holidays Global
        // Configuration
        String configName = (String) globalConfig.get(30).get("name");

        if ("block-loan-overpayment".equals(configName)) {
            configId = (Integer) globalConfig.get(30).get("id");
        }

        Assertions.assertNotNull(configId);

        HashMap configData = GlobalConfigurationHelper.getGlobalConfigurationById(requestSpec, responseSpec, configId.toString());
        Assertions.assertNotNull(configData);

        Boolean enabled = (Boolean) globalConfig.get(30).get("enabled");

        // block-loan-overpayment is disabled
        if (enabled == true) {
            enabled = false;
            configId = GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, configId.toString(),
                    enabled);
        }

        loanStatusHashMap = this.loanTransactionHelper.makeRepayment(writeOffDate, RepaymentAmount, loanID);

        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LOG.info("-----------------------------------OVERPAID LOAN-----------------------------------------------------------");

        // when the block-loan-overpayment flag is disabled then loan will be overpaid.
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        // Changing the block-loan-overpayment flag disabled to enabled
        enabled = true;
        configId = GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(requestSpec, responseSpec, configId.toString(),
                enabled);
        Assertions.assertNotNull(configId);

        loanID = applyForLoanApplication(clientID, loanProductID, proposedAmount);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------PENDING LOAN-----------------------------------------------------------");

        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);
        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(disbursementDate, loanID, disburseAmount);

        LOG.info("-----------------------------------DISBURSE LOAN-----------------------------------------------------------");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList error = (ArrayList) loanTransactionHelper.makeRepaymentObject(loanID, this.httpStatusForidden, adjustRepaymentDate,
                RepaymentAmount);

        HashMap hash = (HashMap) error.get(0);

        Assertions.assertEquals("validation.msg.loan.transaction.null.repayment.exceeding.outstanding.balance",
                hash.get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE), "repayment.exceeding.outstanding.balance.");

    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String proposedAmount) {
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal(proposedAmount).withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withSubmittedOnDate("02 April 2012").build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }

}
