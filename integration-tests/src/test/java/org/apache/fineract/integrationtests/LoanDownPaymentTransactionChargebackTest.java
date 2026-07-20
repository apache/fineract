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

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;

public class LoanDownPaymentTransactionChargebackTest extends FeignLoanTestBase {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void loanDownPaymentTransactionChargebackTest() {
        runAt("03 March 2023", () -> {
            Account fundSource = getAccounts().getFundSource();
            Account loansReceivableAccount = getAccounts().getLoansReceivableAccount();

            Long clientId = createClient();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(false);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
            });

            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("01 March 2023")
                            .locale(LoanTestData.LOCALE).transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            final Long chargebackTransactionId = applyChargebackTransaction(loanId, downPaymentTransaction_1.getResourceId(), 50.0,
                    getPaymentTypeId(0));

            reviewLoanTransactionRelations(loanId, downPaymentTransaction_1.getResourceId(), 1, Double.valueOf("750.00"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("800.00"));

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(300.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            verifyTRJournalEntries(chargebackTransactionId, //
                    credit(fundSource, 50.0), //
                    debit(loansReceivableAccount, 50.0) //
            );
        });
    }

    @Test
    public void loanDownPaymentTransactionChargebackForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            Account fundSource = getAccounts().getFundSource();
            Account loansReceivableAccount = getAccounts().getLoansReceivableAccount();

            Long clientId = createClient();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(true);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("01 March 2023")
                            .locale(LoanTestData.LOCALE).transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            final Long chargebackTransactionId = applyChargebackTransaction(loanId, downPaymentTransaction_1.getResourceId(), 50.0,
                    getPaymentTypeId(0));

            reviewLoanTransactionRelations(loanId, downPaymentTransaction_1.getResourceId(), 1, Double.valueOf("750.00"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("800.00"));

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(300.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            verifyTRJournalEntries(chargebackTransactionId, //
                    credit(fundSource, 50.0), //
                    debit(loansReceivableAccount, 50.0) //
            );
        });
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(boolean isAdvancedPaymentStrategy) {
        PostLoanProductsRequest product = isAdvancedPaymentStrategy
                ? createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                : createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(true);
        product.setNumberOfRepayments(3);
        product.setRepaymentEvery(15);
        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(false);
        return createLoanProduct(product);
    }
}
