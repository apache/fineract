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
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

/**
 * Client Loan Integration Test for checking Loan Disbursement with Waive Interest and Write-Off.
 */
public class LoanWithWaiveInterestAndWriteOffIntegrationTest extends FeignLoanTestBase {

    private static final String LP_PRINCIPAL = "12,000.00";
    private static final String LP_REPAYMENTS = "2";
    private static final String LP_REPAYMENT_PERIOD = "6";
    private static final String LP_INTEREST_RATE = "1";
    private static final Double PRINCIPAL = 4500.00;
    private static final Integer LOAN_TERM_FREQUENCY = 18;
    private static final Integer NUMBER_OF_REPAYMENTS = 9;
    private static final Integer REPAYMENT_PERIOD = 2;
    private static final String DISBURSEMENT_DATE = "30 October 2010";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "23 September 2010";
    private static final String EXPECTED_DISBURSAL_DATE = "28 October 2010";
    private static final Double RATE_OF_INTEREST_PER_PERIOD = 2.0;
    private static final String DATE_OF_JOINING = "04 March 2009";
    private static final Double INTEREST_VALUE_AMOUNT = 40.00;

    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void checkClientLoanCreateAndDisburseFlow() {
        final Long clientId = createClient(DATE_OF_JOINING);
        assertEquals(clientId, clientHelper.getClient(clientId).getId(), "ERROR IN CREATING THE CLIENT");

        final Long loanProductId = createLoanProduct();
        final Long loanId = applyForLoanApplication(clientId, loanProductId);

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, "28 September 2010"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);

        undoApproval(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, "01 October 2010"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);

        disburseWithNetDisbursalAmount(loanId);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        verifyRepaymentScheduleEntryFor(1, 4000.0, loanId);
        makeRepayment("01 January 2011", 540.0f, loanId);

        loanHelper.undoDisbursement(loanId);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);

        disburseWithNetDisbursalAmount(loanId);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        final float repaymentWithInterest = 540.0f;
        final float repaymentWithoutInterest = 500.0f;

        verifyRepaymentScheduleEntryFor(1, 4000.0, loanId);
        makeRepayment("01 January 2011", repaymentWithInterest, loanId);
        makeRepayment("01 March 2011", repaymentWithInterest, loanId);
        addInterestWaiver(loanId, waiveInterest(INTEREST_VALUE_AMOUNT, "01 May 2011"));
        makeRepayment("01 May 2011", repaymentWithoutInterest, loanId);
        makeRepayment("01 July 2011", repaymentWithInterest, loanId);
        addInterestWaiver(loanId, waiveInterest(INTEREST_VALUE_AMOUNT, "01 September 2011"));
        makeRepayment("01 September 2011", repaymentWithoutInterest, loanId);
        makeRepayment("01 November 2011", repaymentWithInterest, loanId);
        addInterestWaiver(loanId, waiveInterest(INTEREST_VALUE_AMOUNT, "01 January 2012"));
        makeRepayment("01 January 2012", repaymentWithoutInterest, loanId);
        verifyRepaymentScheduleEntryFor(7, 1000.0, loanId);

        writeOffLoan(loanId, "01 March 2012");
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosed);
    }

    @Test
    public void checkClientLoan_WRITTEN_OFF() {
        final Long clientId = createClient(DATE_OF_JOINING);
        assertEquals(clientId, clientHelper.getClient(clientId).getId(), "ERROR IN CREATING THE CLIENT");

        final Long loanProductId = createLoanProduct();
        final Long loanId = applyForLoanApplication(clientId, loanProductId);

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, "28 September 2010"));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getWaitingForDisbursal);

        disburseWithNetDisbursalAmount(loanId);
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);

        verifyRepaymentScheduleEntryFor(1, 4000.0, loanId);
        makeRepayment("01 January 2011", 680.0f, loanId);

        GetLoansLoanIdSummary summary = getLoanDetails(loanId).getSummary();
        assertEquals(0, BigDecimal.valueOf(500.0).compareTo(summary.getPrincipalPaid()), "Checking for Principal paid ");
        assertEquals(0, BigDecimal.valueOf(180.0).compareTo(summary.getInterestPaid()), "Checking for interestPaid paid ");
        assertEquals(0, BigDecimal.valueOf(680.0).compareTo(summary.getTotalRepayment()), "Checking for total paid ");

        writeOffLoan(loanId, "01 January 2011");
        verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getClosed);

        summary = getLoanDetails(loanId).getSummary();
        assertEquals(0, BigDecimal.valueOf(4000.0).compareTo(summary.getPrincipalWrittenOff()), "Checking for Principal written off ");
        assertEquals(0, BigDecimal.valueOf(1440.0).compareTo(summary.getInterestWrittenOff()), "Checking for interestPaid written off ");
        assertEquals(0, BigDecimal.valueOf(5440.0).compareTo(summary.getTotalWrittenOff()), "Checking for total written off ");
    }

    private void disburseWithNetDisbursalAmount(final Long loanId) {
        disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(DISBURSEMENT_DATE)//
                .netDisbursalAmount(getLoanDetails(loanId).getNetDisbursalAmount())//
                .note("DISBURSE NOTE")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private void verifyRepaymentScheduleEntryFor(final int repaymentNumber, final Double expectedPrincipalOutstanding, final Long loanId) {
        assertEquals(0, BigDecimal.valueOf(expectedPrincipalOutstanding).compareTo(
                getLoanDetails(loanId).getRepaymentSchedule().getPeriods().get(repaymentNumber).getPrincipalLoanBalanceOutstanding()),
                "Mismatch in Principal Loan Balance Outstanding ");
    }

    private Long createLoanProduct() {
        return createLoanProduct(new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().buildRequest(null));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final PostLoansRequest application = LoanRequestBuilders
                .applyLoan(clientId, loanProductId, LOAN_APPLICATION_SUBMISSION_DATE, PRINCIPAL, NUMBER_OF_REPAYMENTS)//
                .loanTermFrequency(LOAN_TERM_FREQUENCY)//
                .repaymentEvery(REPAYMENT_PERIOD)//
                .expectedDisbursementDate(EXPECTED_DISBURSAL_DATE)//
                .interestRatePerPeriod(BigDecimal.valueOf(RATE_OF_INTEREST_PER_PERIOD))//
                .interestType(LoanTestData.InterestType.FLAT)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));

        return loanHelper.applyForLoan(application).getLoanId();
    }
}
