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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrencyIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ConcurrencyIntegrationTest.class);

    private static final String NO_ACCOUNTING = "1";

    static final int MYTHREADS = 30;

    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void verifyConcurrentLoanRepayments() {
        final Long clientId = createClient();
        Assertions.assertNotNull(clientHelper.getClient(clientId));
        final Long loanProductId = createLoanProduct(false, NO_ACCOUNTING);
        final Long loanId = applyForLoanApplicationWithCollateral(clientId, loanProductId, "12,000.00");
        // the loanHelper.approveLoan(date, loanId) shorthand pins the approved amount at 1000; this loan is for 12000
        approveLoan(loanId, LoanRequestBuilders.approveLoan(12000.0, "20 September 2011"));
        BigDecimal netDisbursalAmount = getLoanDetails(loanId).getNetDisbursalAmount();
        disburseLoan(loanId, LoanRequestBuilders.disburseLoan(12000.0, "20 September 2011").netDisbursalAmount(netDisbursalAmount));
        ExecutorService executor = Executors.newFixedThreadPool(MYTHREADS);
        Calendar date = Calendar.getInstance();
        date.set(2011, 9, 20);
        Double repaymentAmount = 100.0;
        for (int i = 0; i < 10; i++) {
            LOG.info("Starting concurrent transaction number {}", i);
            date.add(Calendar.DAY_OF_MONTH, 1);
            repaymentAmount = repaymentAmount + 100;
            Runnable worker = new LoanRepaymentExecutor(transactionHelper, loanId, repaymentAmount, date);
            executor.execute(worker);
        }

        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {

        }
        LOG.info("\nFinished all threads");

    }

    private Long createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
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
        return createLoanProduct(builder.buildRequest(null));
    }

    private Long applyForLoanApplicationWithCollateral(final Long clientId, final Long loanProductId, String principal) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        final PostLoansRequest application = LoanRequestBuilders
                .legacyIndividualApplication(clientId, loanProductId, principal, 4, BigDecimal.valueOf(2), "20 September 2011")//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));
        return applyForLoan(application);
    }

    public static class LoanRepaymentExecutor implements Runnable {

        private final Long loanId;
        private final Double repaymentAmount;
        private final String repaymentDate;
        private final FeignTransactionHelper transactionHelper;

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        LoanRepaymentExecutor(FeignTransactionHelper transactionHelper, Long loanId, Double repaymentAmount, Calendar repaymentDate) {
            this.loanId = loanId;
            this.repaymentAmount = repaymentAmount;
            this.repaymentDate = dateFormat.format(repaymentDate.getTime());
            this.transactionHelper = transactionHelper;
        }

        @Override
        public void run() {
            try {
                this.transactionHelper.makeLoanRepayment(loanId, LoanRequestBuilders.repayLoan(repaymentAmount, repaymentDate));
            } catch (Exception e) {
                LOG.info("Found an exception {}", e.getMessage());
                LOG.info("Details of failed concurrent transaction (date, amount, loanId) are {},{},{}", repaymentDate, repaymentAmount,
                        loanId);
                throw e;
            }
            LOG.info("Details of passed concurrent transaction, details (date, amount, loanId) are {},{},{}", repaymentDate,
                    repaymentAmount, loanId);
        }
    }

}
