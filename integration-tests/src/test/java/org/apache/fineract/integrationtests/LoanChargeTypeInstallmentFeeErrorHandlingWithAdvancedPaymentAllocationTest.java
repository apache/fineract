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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LoanChargeTypeInstallmentFeeErrorHandlingWithAdvancedPaymentAllocationTest extends FeignLoanTestBase {

    @Disabled
    @Test
    public void addingLoanChargeTypeInstallmentFeeForAdvancedPaymentAllocationGivesErrorTest() {
        runAt("15 March 2023", () -> {
            final Account assetAccount = accountHelper.createAssetAccount("installmentFeeAsset");
            final Account incomeAccount = accountHelper.createIncomeAccount("installmentFeeIncome");
            final Account expenseAccount = accountHelper.createExpenseAccount("installmentFeeExpense");
            final Account overpaymentAccount = accountHelper.createLiabilityAccount("installmentFeeOverpayment");

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = createClient();
            final Long loanProductId = createLoanProduct(assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

            disburseLoan(loanId, "15 February 2023", 1000.0);

            Long installmentFeeCharge = chargesHelper.createCharge(ChargeRequestBuilders.loanInstallmentFee(50)).getResourceId();

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> addLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(installmentFeeCharge).amount(50.0).locale("en")
                            .dateFormat("dd MMMM yyyy")));

            assertEquals(403, exception.getStatus());
            assertTrue(exception.getDeveloperMessage().contains(
                    "Charge with identifier %d cannot be applied: Installment fee charges are not supported for Advanced payment allocation strategy"
                            .formatted(installmentFeeCharge)));
            assertEquals("error.msg.charge.cannot.be.applied.toloan", exception.getUserMessageGlobalisationCode());
        });
    }

    private Long createLoanProduct(final Account... accounts) {
        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);
        PostLoanProductsRequest loanProductCreateRequest = new LoanProductTestBuilder().withPrincipal("15,000.00")
                .withNumberOfRepayments("4").withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .addAdvancedPaymentAllocation(defaultAllocation).withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withMultiDisburse()
                .withDisallowExpectedDisbursements(true).buildRequest();
        return createLoanProduct(loanProductCreateRequest);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {

        PostLoansRequest loanApplication = LoanRequestBuilders
                .legacyDaysBasedApplication(clientId, loanProductId, "1000", 60, 4, 15, "15 February 2023", "15 February 2023")
                .externalId(externalId)//
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy");

        final Long loanId = applyForLoan(loanApplication);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, "15 February 2023"));
        return loanId;
    }
}
