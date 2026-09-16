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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class LoanAccountDisbursementToSavingsAccountingTest extends FeignLoanTestBase {

    private static final String DISBURSEMENT_DATE = "01 March 2023";
    private static final double PRINCIPAL = 1000.0;
    private static final double FEE_PERCENTAGE = 1.0;
    private static final double FEE_AMOUNT = 10.0;
    private static final String REPAYMENT_AT_DISBURSEMENT = "Repayment (at time of disbursement)";

    private final FinancialActivityAccountHelper financialActivityAccountHelper = new FinancialActivityAccountHelper(null);
    private Long createdFinancialActivityMappingId;

    @Test
    public void repaymentAtDisbursementFeeUsesLiabilityTransferAccount() {
        runAt(DISBURSEMENT_DATE, () -> {
            Long clientId = createClient();
            Long savingsAccountId = openSavingsAccount(clientId, null, DISBURSEMENT_DATE).longValue();
            Long liabilityTransferAccountId = getOrCreateLiabilityTransferAccountId();
            Long fundSourceAccountId = getAccounts().getFundSource().getAccountID().longValue();
            assertNotEquals(fundSourceAccountId, liabilityTransferAccountId,
                    "The regression fixture requires distinct FUND_SOURCE and LIABILITY_TRANSFER accounts");

            Long chargeId = createDisbursementPercentageCharge(FEE_PERCENTAGE);
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()//
                    .multiDisburseLoan(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedCalculationType(null)//
                    .overAppliedNumber(null);
            Long loanProductId = createLoanProduct(product);

            PostLoansRequest application = LoanRequestBuilders.applyLoanRequest(clientId, loanProductId, DISBURSEMENT_DATE, PRINCIPAL, 1)//
                    .linkAccountId(savingsAccountId)//
                    .charges(List.of(new PostLoansRequestChargeData().chargeId(chargeId).amount(BigDecimal.valueOf(FEE_PERCENTAGE))));
            Long loanId = applyForLoan(application);
            approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, DISBURSEMENT_DATE));

            BigDecimal netDisbursalAmount = getLoanDetails(loanId).getNetDisbursalAmount();
            disburseToSavings(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(DISBURSEMENT_DATE)
                    .netDisbursalAmount(netDisbursalAmount).locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));

            Long feeTransactionId = getTransactionId(loanId, REPAYMENT_AT_DISBURSEMENT, DISBURSEMENT_DATE);
            List<JournalEntryTransactionItem> feeJournalEntries = journalHelper.getJournalEntries("L" + feeTransactionId).getPageItems();
            assertNotNull(feeJournalEntries);
            JournalEntryTransactionItem debitEntry = feeJournalEntries.stream()
                    .filter(entry -> "DEBIT".equals(entry.getEntryType().getValue())).findFirst().orElseThrow();

            assertEquals(FEE_AMOUNT, debitEntry.getAmount(), 0.01);
            assertEquals(liabilityTransferAccountId, debitEntry.getGlAccountId(),
                    "FINERACT-789: a repayment-at-disbursement fee paid from a disbursement to savings must debit "
                            + "LIABILITY_TRANSFER instead of the loan product FUND_SOURCE account " + fundSourceAccountId);
        });
    }

    private Long getOrCreateLiabilityTransferAccountId() {
        Integer financialActivityId = AccountingConstants.FinancialActivity.LIABILITY_TRANSFER.getValue();
        GetFinancialActivityAccountsResponse existingMapping = financialActivityAccountHelper.getAllFinancialActivityAccounts().stream()
                .filter(mapping -> mapping.getFinancialActivityData() != null
                        && financialActivityId.equals(mapping.getFinancialActivityData().getId()))
                .findFirst().orElse(null);
        if (existingMapping != null) {
            return existingMapping.getGlAccountData().getId();
        }

        Long liabilityTransferAccountId = getAccounts().getOverpaymentAccount().getAccountID().longValue();
        createdFinancialActivityMappingId = financialActivityAccountHelper
                .createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                        .financialActivityId(financialActivityId.longValue()).glAccountId(liabilityTransferAccountId))
                .getResourceId();
        assertNotNull(createdFinancialActivityMappingId);
        return liabilityTransferAccountId;
    }

    @AfterEach
    public void deleteCreatedFinancialActivityMapping() {
        if (createdFinancialActivityMappingId != null) {
            financialActivityAccountHelper.deleteFinancialActivityAccount(createdFinancialActivityMappingId);
        }
    }
}
