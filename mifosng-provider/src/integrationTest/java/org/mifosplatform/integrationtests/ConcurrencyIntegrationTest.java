/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.accounting.Account;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ConcurrencyIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    private static final String NO_ACCOUNTING = "1";

    final int MYTHREADS = 30;

    @Before
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
        this.loanTransactionHelper.disburseLoan("20 September 2011", loanID, "12,000.00");

        ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
        Calendar date = Calendar.getInstance();
        date.set(2011, 9, 20);
        Float repaymentAmount = 100.0f;
        for (int i = 0; i < 10; i++) {
            System.out.println("Starting concurrent transaction number " + i);
            date.add(Calendar.DAY_OF_MONTH, 1);
            repaymentAmount = repaymentAmount + 100;
            Runnable worker = new LoanRepaymentExecutor(loanTransactionHelper, loanID, repaymentAmount, date);
            executor.execute(worker);
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        System.out.println("\nFinished all threads");

    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts).build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String principal) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
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
                System.out.println("Found an exception" + e.getMessage());
                System.out.println("Details of failed concurrent transaction (date, amount, loanId) are " + repaymentDate + ","
                        + repaymentAmount + "," + loanId);
                throw (e);
            }
            System.out.println("Details of passed concurrent transaction, details (date, amount, loanId) are " + repaymentDate + ","
                    + repaymentAmount + "," + loanId);
        }
    }

}
