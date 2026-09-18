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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdDisbursementData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanApplicationApprovalTest extends FeignLoanTestBase {

    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    /*
     * Positive test case: Approved amount non zero is less than proposed amount
     */
    @Test
    public void loanApplicationApprovedAmountLessThanProposedAmount() {

        final String proposedAmount = "8000";
        final Double approvalAmount = 5000.0;
        final String approveDate = "20 September 2012";

        final Long clientId = createClient("01 January 2012");
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null));
        final Long loanId = applyForLoanApplicationWithCollateral(clientId, loanProductId, proposedAmount);

        verifyLoanStatus(getLoanDetails(loanId), status -> status.getPendingApproval());

        approveLoan(loanId, LoanRequestBuilders.approveLoan(approvalAmount, approveDate));
        verifyLoanStatus(getLoanDetails(loanId), status -> status.getWaitingForDisbursal());

    }

    /*
     * Negative test case: Approved amount non zero is greater than proposed amount
     */
    @Test
    public void loanApplicationApprovedAmountGreaterThanProposedAmount() {

        final String proposedAmount = "5000";
        final Double approvalAmount = 9000.0;
        final String approveDate = "2 April 2012";

        final Long clientId = createClient("01 January 2012");
        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().buildRequest(null));
        final Long loanId = applyForLoanApplicationWithCollateral(clientId, loanProductId, proposedAmount);

        verifyLoanStatus(getLoanDetails(loanId), status -> status.getPendingApproval());

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> approveLoan(loanId, LoanRequestBuilders.approveLoan(approvalAmount, approveDate)));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, "error.msg.loan.approval.amount.can't.be.greater.than.loan.amount.demanded");

    }

    public PostLoansDisbursementData createTrancheDetail(final String date, final double amount) {
        return LoanRequestBuilders.applyTrancheDetail(date, amount);
    }

    public PostLoansLoanIdDisbursementData approveTrancheDetail(final String date, final double amount) {
        return LoanRequestBuilders.approveTrancheDetail(date, amount);
    }

    @Test
    public void loanApplicationApprovalAndValidationForMultiDisburseLoans() {
        List<PostLoansDisbursementData> createTranches = List.of(//
                createTrancheDetail("01 March 2014", 1000), //
                createTrancheDetail("23 March 2014", 4000));

        final Long clientId = createClient("01 January 2014");
        log.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientId);

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(true) //
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true) //
                .buildRequest(null));
        log.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductId);

        trancheLoansApprovedAmountLesserThanProposedAmount(clientId, loanProductId, createTranches);
        trancheLoansApprovalValidation(clientId, loanProductId, createTranches);
    }

    @Test
    public void loanApplicationShouldFailIfTransactionProcessingStrategyIsAdvancedPaymentAllocationButItIsNotConfiguredOnProduct() {
        final Long clientId = createClient("01 January 2014");
        log.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientId);

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(false)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).buildRequest(null));
        log.info("----------------------------------LOAN PRODUCT CREATED WITH ID------------------------------------------- {}",
                loanProductId);

        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> applyForLoan(LoanRequestBuilders.applyLoan(clientId, loanProductId, "01 March 2022", 1000.0, 1)//
                        .interestType(LoanTestData.InterestType.FLAT)//
                        .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                        .transactionProcessingStrategyCode(
                                AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)));

        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, "strategy.cannot.be.advanced.payment.allocation.if.not.configured");

    }

    private void trancheLoansApprovedAmountLesserThanProposedAmount(Long clientId, Long loanProductId,
            List<PostLoansDisbursementData> createTranches) {
        final String proposedAmount = "5000";
        final Double approvalAmount = 2000.0;
        final String approveDate = "01 March 2014";
        final String expectedDisbursementDate = "01 March 2014";

        List<PostLoansLoanIdDisbursementData> approveTranches = List.of(//
                approveTrancheDetail("01 March 2014", 1000), //
                approveTrancheDetail("23 March 2014", 1000));

        final Long loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, proposedAmount, createTranches);
        log.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}", loanId);

        verifyLoanStatus(getLoanDetails(loanId), status -> status.getPendingApproval());

        log.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        approveLoan(loanId,
                LoanRequestBuilders.approveLoanWithTranches(approvalAmount, approveDate, expectedDisbursementDate, approveTranches));
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertFalse(loanDetails.getStatus().getPendingApproval());
        verifyLoanStatus(loanDetails, status -> status.getWaitingForDisbursal());
        log.info("-----------------------------------MULTI DISBURSAL LOAN APPROVED SUCCESSFULLY---------------------------------------");

    }

    private void trancheLoansApprovalValidation(Long clientId, Long loanProductId, List<PostLoansDisbursementData> createTranches) {
        final String proposedAmount = "5000";
        final Double approvalAmount1 = 10000.0;
        final Double approvalAmount3 = 400.0;
        final Double approvalAmount4 = 200.0;

        final String approveDate = "01 March 2014";
        final String expectedDisbursementDate = "01 March 2014";

        List<PostLoansLoanIdDisbursementData> approveTranche1 = List.of(//
                approveTrancheDetail("01 March 2014", 5000), //
                approveTrancheDetail("23 March 2014", 5000));

        List<PostLoansLoanIdDisbursementData> approveTranche3 = List.of(//
                approveTrancheDetail("01 March 2014", 100), //
                approveTrancheDetail("23 March 2014", 100), //
                approveTrancheDetail("24 March 2014", 100), //
                approveTrancheDetail("25 March 2014", 100));

        List<PostLoansLoanIdDisbursementData> approveTranche4 = List.of(//
                approveTrancheDetail("01 March 2014", 100), //
                approveTrancheDetail("23 March 2014", 100), //
                approveTrancheDetail("24 March 2014", 100));

        final Long loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, proposedAmount, createTranches);
        log.info("-----------------------------------LOAN CREATED WITH LOANID------------------------------------------------- {}", loanId);

        verifyLoanStatus(getLoanDetails(loanId), status -> status.getPendingApproval());

        log.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");

        /* Sum of tranches is greater than approved amount */
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> approveLoan(loanId,
                LoanRequestBuilders.approveLoanWithTranches(approvalAmount4, approveDate, expectedDisbursementDate, approveTranche4)));
        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception,
                "validation.msg.loan.principal.sum.of.multi.disburse.amounts.must.be.equal.to.or.lesser.than.approved.principal");

        /* Sum of tranches exceeds the proposed amount */
        exception = assertThrows(CallFailedRuntimeException.class, () -> approveLoan(loanId,
                LoanRequestBuilders.approveLoanWithTranches(approvalAmount1, approveDate, expectedDisbursementDate, approveTranche1)));
        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, "error.msg.loan.approval.amount.can't.be.greater.than.loan.amount.demanded");

        /* No. of tranches exceeds the max tranche count at product level */
        exception = assertThrows(CallFailedRuntimeException.class, () -> approveLoan(loanId,
                LoanRequestBuilders.approveLoanWithTranches(approvalAmount3, approveDate, expectedDisbursementDate, approveTranche3)));
        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, "error.msg.disbursementData.exceeding.max.tranche.count");
    }

    private Long applyForLoanApplicationWithCollateral(final Long clientId, final Long loanProductId, final String proposedAmount) {
        final PostLoansRequest application = LoanRequestBuilders
                .legacyIndividualApplication(clientId, loanProductId, proposedAmount, 5, BigDecimal.valueOf(2), "20 September 2012")//
                .submittedOnDate("02 April 2012")//
                .collateral(collateralFor(clientId));
        return applyForLoan(application);
    }

    public Long applyForLoanApplicationWithTranches(final Long clientId, final Long loanProductId, String principal,
            List<PostLoansDisbursementData> tranches) {
        log.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest application = LoanRequestBuilders
                .legacyIndividualApplication(clientId, loanProductId, principal, 5, BigDecimal.valueOf(2), "01 March 2014")//
                .collateral(collateralFor(clientId))//
                .disbursementData(tranches);
        return applyForLoan(application);
    }

    private List<PostLoansRequestCollateralData> collateralFor(final Long clientId) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        Assertions.assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        Assertions.assertNotNull(clientCollateralId);
        return List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE));
    }
}
