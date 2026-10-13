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

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.StandingInstructionsHistoryApi.RetrieveAllStandingInstructionHistoryQueryParams;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetPageItemsStandingInstructionSwagger;
import org.apache.fineract.client.models.GetStandingInstructionHistoryPageItemsResponse;
import org.apache.fineract.client.models.GetStandingInstructionsResponse;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StandingInstructionLoanAccountTest extends FeignLoanTestBase {

    private static final Integer LOAN_ACCOUNT_TYPE = PortfolioAccountType.LOAN.getValue();
    private static final Integer LOAN_REPAYMENT = AccountTransferType.LOAN_REPAYMENT.getValue();

    private static FineractFeignClient client;
    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FeignSavingsTransactionHelper savingsTransactionHelper;
    private static FinancialActivityAccountHelper financialActivityAccountHelper;

    @BeforeAll
    public static void setupSavingsHelpers() {
        client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        savingsTransactionHelper = new FeignSavingsTransactionHelper(client);
        financialActivityAccountHelper = new FinancialActivityAccountHelper(null);
    }

    @AfterEach
    public void deleteFinancialActivityMappings() {
        for (GetFinancialActivityAccountsResponse mapping : financialActivityAccountHelper.getAllFinancialActivityAccounts()) {
            financialActivityAccountHelper.deleteFinancialActivityAccount(mapping.getId());
        }
    }

    @Test
    public void standingInstructionCreatedAtDisbursementIsListedByLoanAccount() {
        runAt("01 March 2023", () -> {
            Long clientId = createClient();
            Long loanId = createDisbursedLoanWithStandingInstruction(clientId);

            List<GetPageItemsStandingInstructionSwagger> withoutTransferType = listStandingInstructions(null, null, loanId);
            assertEquals(1, withoutTransferType.size());
            assertEquals(loanId, withoutTransferType.get(0).getToAccount().getId());
            assertEquals(LOAN_REPAYMENT, withoutTransferType.get(0).getTransferType().getId());

            List<GetPageItemsStandingInstructionSwagger> withTransferType = listStandingInstructions(LOAN_REPAYMENT, null, loanId);
            assertEquals(1, withTransferType.size());
            assertEquals(loanId, withTransferType.get(0).getToAccount().getId());

            List<GetPageItemsStandingInstructionSwagger> withClient = listStandingInstructions(null, clientId, loanId);
            assertEquals(1, withClient.size());
        });
    }

    @Test
    public void standingInstructionJobPaysLoanDuesAndHistoryIsListedByLoanAccount() {
        Long[] ids = new Long[2];
        runAt("01 March 2023", () -> {
            ids[0] = createClient();
            ids[1] = createDisbursedLoanWithStandingInstruction(ids[0]);
        });
        Long loanId = ids[1];

        runAt("31 March 2023", () -> {
            SchedulerJobHelper.executeAndAwaitJob("Execute Standing Instruction");

            GetLoansLoanIdResponse loan = getLoanDetails(loanId);
            assertTrue(loan.getStatus().getClosedObligationsMet(), "Loan should be repaid by the standing instruction");

            RetrieveAllStandingInstructionHistoryQueryParams queryParams = new RetrieveAllStandingInstructionHistoryQueryParams()
                    .fromAccountId(loanId).fromAccountType(LOAN_ACCOUNT_TYPE);
            List<GetStandingInstructionHistoryPageItemsResponse> history = List.copyOf(
                    ok(() -> client.standingInstructionsHistory().retrieveAllStandingInstructionHistory(queryParams)).getPageItems());
            assertEquals(1, history.size());
            assertEquals(0, BigDecimal.valueOf(1000).compareTo(BigDecimal.valueOf(history.get(0).getAmount())));
        });
    }

    private Long createDisbursedLoanWithStandingInstruction(Long clientId) {
        Long savingsProductId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct()).getResourceId();
        Long savingsId = savingsHelper.createApproveActivateSavings(clientId, savingsProductId, "01 March 2023");
        savingsTransactionHelper.deposit(savingsId, "5000", "01 March 2023");

        Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
        mapLiabilityTransferFinancialActivity(loanProductId);
        Long loanId = applyForLoan(applyLoanRequest(clientId, loanProductId, "01 March 2023", 1000.0, 1)
                .createStandingInstructionAtDisbursement(true).linkAccountId(savingsId));
        approveLoan(loanId, approveLoanRequest(1000.0, "01 March 2023"));
        disburseLoan(loanId, BigDecimal.valueOf(1000), "01 March 2023");
        return loanId;
    }

    private void mapLiabilityTransferFinancialActivity(Long loanProductId) {
        if (!financialActivityAccountHelper.getAllFinancialActivityAccounts().isEmpty()) {
            return;
        }
        Long fundSourceAccountId = retrieveLoanProduct(loanProductId).getAccountingMappings().getFundSourceAccount().getId();
        financialActivityAccountHelper.createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                .financialActivityId((long) AccountingConstants.FinancialActivity.LIABILITY_TRANSFER.getValue())
                .glAccountId(fundSourceAccountId));
    }

    private List<GetPageItemsStandingInstructionSwagger> listStandingInstructions(Integer transferType, Long clientId, Long loanId) {
        GetStandingInstructionsResponse response = ok(() -> client.standingInstructions().retrieveAllStandingInstructions(null, null, null,
                null, null, transferType, null, clientId, loanId, LOAN_ACCOUNT_TYPE));
        return List.copyOf(response.getPageItems());
    }
}
