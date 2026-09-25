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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanApplicationRejectionForLoanProductWithPeriodicAccrualAccountingTest extends FeignLoanTestBase {

    @Test
    public void loanApplicationRejectionForPeriodicAccrualAccountingLoanProductTest() {

        Account assetAccount = accountHelper.createAssetAccount();
        Account incomeAccount = accountHelper.createIncomeAccount();
        Account expenseAccount = accountHelper.createExpenseAccount();
        Account overpaymentAccount = accountHelper.createLiabilityAccount();

        // Create Loan Product with Periodic Accrual accounting
        final Long loanProductId = createLoanProductWithPeriodicAccrualAccounting(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Client and Loan account creation
        final Long clientId = createClient();

        final Long loanId = createLoanAccount(clientId, loanProductId, loanExternalIdStr);

        // verify Loan status as submitted and pending approval
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getStatus().getPendingApproval());

        // Reject Loan application
        PostLoansLoanIdResponse result = loanHelper.rejectLoanByExternalId(loanExternalIdStr,
                LoanRequestBuilders.rejectLoan("3 September 2022"));

        // Verify Loan application status is Rejected
        assertEquals("Rejected", result.getChanges().getStatus().getValue());

    }

    private Long createLoanProductWithPeriodicAccrualAccounting(final Account... accounts) {
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1").withNumberOfRepayments("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").buildRequest(null));
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String externalId) {
        return applyForLoan(LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 September 2022", 1000.0, 1)//
                .expectedDisbursementDate("03 September 2022")//
                .interestType(LoanTestData.InterestType.FLAT)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .externalId(externalId));
    }

}
