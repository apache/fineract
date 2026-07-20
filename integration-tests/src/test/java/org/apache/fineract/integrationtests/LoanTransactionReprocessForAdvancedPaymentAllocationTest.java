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

import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;

public class LoanTransactionReprocessForAdvancedPaymentAllocationTest extends FeignLoanTestBase {

    @Test
    public void loanTransactionReprocessForAddChargeTest() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            runAt("15 March 2023", () -> {
                Account assetAccount = accountHelper.createAssetAccount("reprocessAsset");
                Account incomeAccount = accountHelper.createIncomeAccount("reprocessIncome");
                Account expenseAccount = accountHelper.createExpenseAccount("reprocessExpense");
                Account overpaymentAccount = accountHelper.createLiabilityAccount("reprocessOverpayment");

                String loanExternalIdStr = UUID.randomUUID().toString();
                Long clientId = createClient("15 February 2023");
                Long loanProductId = createLoanProduct(assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
                Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

                disburseLoan(loanId, "15 February 2023", 1000.0);

                addCharge(loanId, false, 50, "22 February 2023");

                String loanTransactionExternalIdStr = UUID.randomUUID().toString();
                addRepayment(loanId, LoanRequestBuilders.repayLoan(50.0, "20 February 2023").externalId(loanTransactionExternalIdStr));

                verifyRepaymentTransaction(loanId, "20 February 2023", 50.0, 50.0, 0.0, 0.0, 0.0);

                addCharge(loanId, true, 10, "22 February 2023");

                GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = getLoanTransactionDetails(loanId,
                        loanTransactionExternalIdStr);
                assertNotNull(getLoansTransactionResponse);
                assertEquals(0, getLoansTransactionResponse.getTransactionRelations().size());

                addCharge(loanId, true, 10, "18 February 2023");

                getLoansTransactionResponse = getLoanTransactionDetails(loanId, loanTransactionExternalIdStr);
                assertNotNull(getLoansTransactionResponse);
                assertEquals(1, getLoansTransactionResponse.getTransactionRelations().size());

                verifyRepaymentTransaction(loanId, "20 February 2023", 50.0, 40.0, 0.0, 0.0, 10.0);
            });
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Long createLoanProduct(Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation("NEXT_INSTALLMENT");
        String loanProductCreateJSON = new LoanProductTestBuilder().withPrincipal("15,000.00").withNumberOfRepayments("4")
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .addAdvancedPaymentAllocation(defaultAllocation).withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withMultiDisburse()
                .withDisallowExpectedDisbursements(true).build();
        return createLoanProductFromJson(loanProductCreateJSON);
    }

    private Long createLoanAccount(Long clientId, Long loanProductId, String externalId) {
        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("60")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("4").withRepaymentEveryAfter("15").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("15 February 2023")
                .withSubmittedOnDate("15 February 2023").withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).build(clientId.toString(), loanProductId.toString(), null);

        Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, "15 February 2023"));
        return loanId;
    }

    private void verifyRepaymentTransaction(Long loanId, String date, double amount, double principalPortion, double interestPortion,
            double feePortion, double penaltyPortion) {
        GetLoansLoanIdTransactions repayment = getLoanDetails(loanId).getTransactions().stream()
                .filter(tx -> Boolean.TRUE.equals(tx.getType().getRepayment()) && date.equals(tx.getDate().format(dateTimeFormatter)))
                .findFirst().orElseThrow();
        assertEquals(amount, Utils.getDoubleValue(repayment.getAmount()));
        assertEquals(principalPortion, Utils.getDoubleValue(repayment.getPrincipalPortion()));
        assertEquals(interestPortion, Utils.getDoubleValue(repayment.getInterestPortion()));
        assertEquals(feePortion, Utils.getDoubleValue(repayment.getFeeChargesPortion()));
        assertEquals(penaltyPortion, Utils.getDoubleValue(repayment.getPenaltyChargesPortion()));
    }
}
