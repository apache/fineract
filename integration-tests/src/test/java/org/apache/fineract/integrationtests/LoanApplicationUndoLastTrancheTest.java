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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdDisbursementDetails;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanApplicationUndoLastTrancheTest extends FeignLoanTestBase {

    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void loanApplicationUndoLastTranche() {
        final Double proposedAmount = 5000.0;
        final Double approvalAmount = 2000.0;
        final String approveDate = "01 March 2014";
        final String expectedDisbursementDate = "01 March 2014";
        final String disbursalDate = "01 March 2014";

        final Long clientId = createClient("01 January 2014");

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).buildRequest(null));

        final List<PostLoansDisbursementData> createTranches = List.of(LoanRequestBuilders.applyTrancheDetail("01 March 2014", 1000.0),
                LoanRequestBuilders.applyTrancheDetail("23 June 2014", 4000.0));

        final Long loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, proposedAmount, 2.0, createTranches);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId,
                LoanRequestBuilders.approveLoanWithTranches(approvalAmount, approveDate, expectedDisbursementDate,
                        List.of(LoanRequestBuilders.approveTrancheDetail("01 March 2014", 1000.0),
                                LoanRequestBuilders.approveTrancheDetail("23 June 2014", 1000.0)))
                        .note("Approval NOTE"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);

        disburseWithNetDisbursalAmount(loanId, disbursalDate);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        makeRepayment("01 April 2014", 420.0f, loanId);
        makeRepayment("01 May 2014", 412.0f, loanId);
        makeRepayment("01 June 2014", 204.0f, loanId);

        disburseWithNetDisbursalAmount(loanId, "23 June 2014");

        assertEquals(0, BigDecimal.valueOf(1000.0).compareTo(undoLastDisbursal(loanId)));
    }

    @Test
    public void loanApplicationUndoLastTrancheToClose() {
        LocalDate transactionDate = LocalDate.of(2014, 3, 1);
        String operationDate = Utils.dateFormatter.format(transactionDate);

        final Double proposedAmount = 1000.0;

        final Long clientId = createClient("01 January 2014");

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                .withDisallowExpectedDisbursements(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).buildRequest(null));

        final Long loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, proposedAmount, 0.0, List.of());

        approveLoan(loanId, LoanRequestBuilders.approveLoan(proposedAmount, operationDate));
        validateLoanStatus(getLoanDetails(loanId), "loanStatusType.approved");

        loanHelper.disburseLoan(operationDate, loanId, "500");
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.active");
        evaluateLoanDisbursementDetails(loanDetails, 1, 500.00);

        transactionDate = transactionDate.plusDays(2);
        operationDate = Utils.dateFormatter.format(transactionDate);
        loanHelper.disburseLoan(operationDate, loanId, "500");
        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.active");
        evaluateLoanDisbursementDetails(loanDetails, 2, 1000.00);

        // backdated repayment
        transactionDate = transactionDate.minusDays(1);
        operationDate = Utils.dateFormatter.format(transactionDate);
        assertNotNull(makeLoanRepayment(loanId, LoanRequestBuilders.repayLoan(500.00, operationDate)));

        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.active");
        evaluateLoanDisbursementDetails(loanDetails, 2, 1000.00);
        validateLoanTotalOutstandingBalance(loanDetails, 500.00);

        undoLastDisbursal(loanId);

        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.closed.obligations.met");
        validateLoanTotalOutstandingBalance(loanDetails, 0.00);
    }

    @Test
    public void loanApplicationUndoLastTrancheWithSameDate() {
        final Double proposedAmount = 5000.0;
        final String approveDate = "01 March 2014";
        final String disbursalDate = "01 March 2014";

        final Long clientId = createClient("01 January 2014");

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                .withDisallowExpectedDisbursements(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).buildRequest(null));

        final Long loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, proposedAmount, 0.0, List.of());

        approveLoan(loanId, LoanRequestBuilders.approveLoan(proposedAmount, approveDate));
        validateLoanStatus(getLoanDetails(loanId), "loanStatusType.approved");

        loanHelper.disburseLoan(disbursalDate, loanId, "1000");
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.active");
        evaluateLoanDisbursementDetails(loanDetails, 1, 1000.00);

        loanHelper.disburseLoan(disbursalDate, loanId, "2000");
        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.active");
        evaluateLoanDisbursementDetails(loanDetails, 2, 3000.00);

        undoLastDisbursal(loanId);

        loanDetails = getLoanDetails(loanId);
        validateLoanStatus(loanDetails, "loanStatusType.active");
        evaluateLoanDisbursementDetails(loanDetails, 1, 1000.00);
        validateLoanTotalOutstandingBalance(loanDetails, 1000.00);
    }

    private BigDecimal undoLastDisbursal(final Long loanId) {
        return loanHelper.undoLastDisbursement(loanId, new PostLoansLoanIdRequest().note("UNDO LAST DISBURSAL")).getChanges()
                .getDisbursedAmount();
    }

    private void disburseWithNetDisbursalAmount(final Long loanId, final String disbursalDate) {
        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disbursalDate)//
                .netDisbursalAmount(getLoanDetails(loanId).getNetDisbursalAmount())//
                .note("DISBURSE NOTE")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private void evaluateLoanDisbursementDetails(final GetLoansLoanIdResponse loanDetails, final int numItems,
            final Double amountExpected) {
        assertEquals(numItems, loanDetails.getDisbursementDetails().size());
        BigDecimal total = BigDecimal.ZERO;
        for (GetLoansLoanIdDisbursementDetails disbursementDetails : loanDetails.getDisbursementDetails()) {
            total = total.add(disbursementDetails.getPrincipal());
        }
        assertEquals(0, BigDecimal.valueOf(amountExpected).compareTo(total));
    }

    private void validateLoanTotalOutstandingBalance(final GetLoansLoanIdResponse loanDetails, final Double amountExpected) {
        assertEquals(0, BigDecimal.valueOf(amountExpected).compareTo(loanDetails.getSummary().getTotalOutstanding()));
    }

    private Long applyForLoanApplicationWithTranches(final Long clientId, final Long loanProductId, final Double principal,
            final Double interestRate, final List<PostLoansDisbursementData> tranches) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 March 2014", principal, 5)//
                .interestRatePerPeriod(BigDecimal.valueOf(interestRate))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .maxOutstandingLoanBalance(BigDecimal.valueOf(36000))//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        if (!tranches.isEmpty()) {
            application.disbursementData(tranches).fixedEmiAmount(BigDecimal.valueOf(10000));
        }

        return loanHelper.applyForLoan(application).getLoanId();
    }
}
