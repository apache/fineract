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
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanCapitalizedIncomeTest extends BaseLoanIntegrationTest {

    @BeforeAll
    public void setup() {
        new BusinessStepHelper().updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "CHECK_DUE_INSTALLMENTS", "UPDATE_LOAN_ARREARS_AGING",
                "ADD_PERIODIC_ACCRUAL_ENTRIES", "ACCRUAL_ACTIVITY_POSTING", "CAPITALIZED_INCOME_AMORTIZATION",
                "LOAN_INTEREST_RECALCULATION", "EXTERNAL_ASSET_OWNER_TRANSFER");
    }

    @Test
    public void testLoanCapitalizedIncomeAmortization() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper
                .createLoanProduct(create4IProgressive().enableIncomeCapitalization(true)
                        .capitalizedIncomeCalculationType(PostLoanProductsRequest.CapitalizedIncomeCalculationTypeEnum.FLAT)
                        .capitalizedIncomeStrategy(PostLoanProductsRequest.CapitalizedIncomeStrategyEnum.EQUAL_AMORTIZATION)
                        .deferredIncomeLiabilityAccountId(deferredIncomeLiabilityAccount.getAccountID().longValue())
                        .incomeFromCapitalizationAccountId(feeIncomeAccount.getAccountID().longValue())
                        .capitalizedIncomeType(PostLoanProductsRequest.CapitalizedIncomeTypeEnum.FEE));

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "1 January 2024",
                    500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            loanTransactionHelper.addCapitalizedIncome(loanId, "1 January 2024", 50.0);
        });
        runAt("2 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income", "01 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "01 January 2024") //
            );
        });
        runAt("3 January 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(50.0, "Capitalized Income", "01 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "01 January 2024"), //
                    transaction(0.02, "Accrual", "02 January 2024"), //
                    transaction(0.55, "Capitalized Income Amortization", "02 January 2024") //
            );

            verifyJournalEntries(loanId, //
                    journalEntry(100, loansReceivableAccount, "DEBIT"), //
                    journalEntry(100, fundSource, "CREDIT"), //
                    journalEntry(50, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50, deferredIncomeLiabilityAccount, "CREDIT"), //
                    journalEntry(0.55, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(0.55, feeIncomeAccount, "CREDIT"), //
                    journalEntry(0.02, interestReceivableAccount, "DEBIT"), //
                    journalEntry(0.02, interestIncomeAccount, "CREDIT"), //
                    journalEntry(0.55, deferredIncomeLiabilityAccount, "DEBIT"), //
                    journalEntry(0.55, feeIncomeAccount, "CREDIT") //
            );
        });
    }
}
