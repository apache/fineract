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
package org.apache.fineract.integrationtests.loan.repayment;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.BaseLoanIntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Test;

public class LoanRepaymentTest extends BaseLoanIntegrationTest {

    @Test
    public void test_LoanRepaymentWorks_WhenDisbursementChargeIsAvailable_AndAccrualAccounting_AndDailyRecalculateInterest_AndDailyInterestCalculationPeriod() {

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create charges
            double charge1Amount = 1.0;
            double charge2Amount = 1.5;
            Long charge1Id = createDisbursementPercentageCharge(charge1Amount);
            Long charge2Id = createDisbursementPercentageCharge(charge2Amount);

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyInterval(1)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                    .allowPartialPeriodInterestCalcualtion(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedNumber(null)//
                    .overAppliedCalculationType(null)//
                    .multiDisburseLoan(null)//
                    .charges(List.of(new ChargeData().id(charge1Id), new ChargeData().id(charge2Id)));//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .charges(List.of(//
                            new PostLoansRequestChargeData().chargeId(charge1Id).amount(BigDecimal.valueOf(charge1Amount)), //
                            new PostLoansRequestChargeData().chargeId(charge2Id).amount(BigDecimal.valueOf(charge2Amount))//
            ));//

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(25.0, "Repayment (at time of disbursement)", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(25.0, feeIncomeAccount, "CREDIT"), //
                    journalEntry(25.0, fundSource, "DEBIT") //
            );

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023"), //
                    transaction(25.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(25.0, feeIncomeAccount, "CREDIT"), //
                    journalEntry(25.0, fundSource, "DEBIT"), //
                    journalEntry(500.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(500.0, fundSource, "DEBIT") //
            );
        });
    }
}
