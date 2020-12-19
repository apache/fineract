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
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrencyIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrencyIntegrationTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    private static final String NO_ACCOUNTING = "1";

    static final int MYTHREADS = 30;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void verifyConcurrentLoanRepayments() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        final Integer loanProductID = createLoanProduct(false, NO_ACCOUNTING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "12,000.00");
        this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        this.loanTransactionHelper.disburseLoan("20 September 2011", loanID, "12,000.00",
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());

        ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
        Calendar date = Calendar.getInstance();
        date.set(2011, 9, 20);
        Float repaymentAmount = 100.0f;
        for (int i = 0; i < 10; i++) {
            LOG.info("Starting concurrent transaction number {}", i);
            date.add(Calendar.DAY_OF_MONTH, 1);
            repaymentAmount = repaymentAmount + 100;
            Runnable worker = new LoanRepaymentExecutor(loanTransactionHelper, loanID, repaymentAmount, date);
            executor.execute(worker);
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        LOG.info("\nFinished all threads");

    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);

        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String principal) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    public static class LoanRepaymentExecutor implements Runnable {

        private final Integer loanId;
        private final Float repaymentAmount;
        private final String repaymentDate;
        private final LoanTransactionHelper loanTransactionHelper;

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        LoanRepaymentExecutor(LoanTransactionHelper loanTransactionHelper, Integer loanId, Float repaymentAmount, Calendar repaymentDate) {
            this.loanId = loanId;
            this.repaymentAmount = repaymentAmount;
            this.repaymentDate = dateFormat.format(repaymentDate.getTime());
            this.loanTransactionHelper = loanTransactionHelper;
        }

        @Override
        public void run() {
            try {
                this.loanTransactionHelper.makeRepayment(repaymentDate, repaymentAmount, loanId);
            } catch (Exception e) {
                LOG.info("Found an exception {}", e.getMessage());
                LOG.info("Details of failed concurrent transaction (date, amount, loanId) are {},{},{}", repaymentDate, repaymentAmount,
                        loanId);
                throw (e);
            }
            LOG.info("Details of passed concurrent transaction, details (date, amount, loanId) are {},{},{}", repaymentDate,
                    repaymentAmount, loanId);
        }
    }

}
