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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LoanSpecificDueDateChargeAfterMaturityTest extends FeignLoanTestBase {

    private static final String DATE_OF_JOINING = "01 January 2011";
    private static final Double LP_PRINCIPAL = 10000.0;
    private static final String EXPECTED_DISBURSAL_DATE = "04 March 2011";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "03 March 2011";
    private static final String STRATEGY = LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY;

    private Long commonLoanProductId;
    private Long clientId;

    @BeforeAll
    public void setupCommon() {
        commonLoanProductId = createLoanProduct(noInterestPeriodicAccrualProduct(500.0, 15, LoanTestData.RepaymentFrequencyType.DAYS_L, 4));
        clientId = createClient();
    }

    @Test
    public void checkPeriodicAccrualAccountingAPIFlow() {
        final Long loanProductID = createLoanProduct(
                noInterestPeriodicAccrualProduct(LP_PRINCIPAL, 1, LoanTestData.RepaymentFrequencyType.MONTHS_L, 1));

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForNoInterestLoan(clientID, loanProductID);

        final double FEE_PORTION = 50.0;
        final double PENALTY_PORTION = 100.0;
        final double NEXT_FEE_PORTION = 55.0;
        final double NEXT_PENALTY_PORTION = 105.0;

        Long flat = chargesHelper.createLoanSpecifiedDueDateCharge(FEE_PORTION).getResourceId();
        Long flatSpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDatePenalty(PENALTY_PORTION).getResourceId();
        Long flatNext = chargesHelper.createLoanSpecifiedDueDateCharge(NEXT_FEE_PORTION).getResourceId();
        Long flatSpecifiedDueDateNext = chargesHelper.createLoanSpecifiedDueDatePenalty(NEXT_PENALTY_PORTION).getResourceId();

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        assertTrue(loanDetails.getStatus().getPendingApproval());

        approveLoan(loanID, LoanRequestBuilders.approveLoan(LP_PRINCIPAL, EXPECTED_DISBURSAL_DATE));
        loanDetails = getLoanDetails(loanID);
        assertTrue(loanDetails.getStatus().getWaitingForDisbursal());

        disburseLoan(loanID, LoanRequestBuilders.disburseLoan(LP_PRINCIPAL, EXPECTED_DISBURSAL_DATE));
        loanDetails = getLoanDetails(loanID);
        assertTrue(loanDetails.getStatus().getActive());

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 1));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 1));
        assertEquals(0.0, penaltyChargesDue(loanSchedule, 1));
        assertEquals(0.0, penaltyChargesOutstanding(loanSchedule, 1));
        assertEquals(10000.0, totalDueForPeriod(loanSchedule, 1));
        assertEquals(10000.0, totalOutstandingForPeriod(loanSchedule, 1));

        final String penaltyCharge1AddedDate = "05 April 2011";
        addChargesForLoan(loanID, LoanRequestBuilders.addLoanCharge(flatSpecifiedDueDate, PENALTY_PORTION, penaltyCharge1AddedDate));

        noAccrualTransactionForRepayment(loanID);

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 2));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(LocalDate.of(2011, 4, 5), loanSchedule.get(2).getDueDate());

        runPeriodicAccrualAccounting(penaltyCharge1AddedDate);
        checkAccrualTransactionForRepayment(LocalDate.of(2011, 4, 5), 0.0, 0.0, PENALTY_PORTION, loanID);

        final String feeCharge1AddedDate = "06 April 2011";
        addChargesForLoan(loanID, LoanRequestBuilders.addLoanCharge(flat, FEE_PORTION, feeCharge1AddedDate));

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(FEE_PORTION, feeChargesDue(loanSchedule, 2));
        assertEquals(FEE_PORTION, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + FEE_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + FEE_PORTION, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(LocalDate.of(2011, 4, 6), loanSchedule.get(2).getDueDate());

        final String penaltyCharge2AddedDate = "07 April 2011";
        addChargesForLoan(loanID,
                LoanRequestBuilders.addLoanCharge(flatSpecifiedDueDateNext, NEXT_PENALTY_PORTION, penaltyCharge2AddedDate));

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(FEE_PORTION, feeChargesDue(loanSchedule, 2));
        assertEquals(FEE_PORTION, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(LocalDate.of(2011, 4, 7), loanSchedule.get(2).getDueDate());

        final String feeCharge2AddedDate = "08 April 2011";
        addChargesForLoan(loanID, LoanRequestBuilders.addLoanCharge(flatNext, NEXT_FEE_PORTION, feeCharge2AddedDate));

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(FEE_PORTION + NEXT_FEE_PORTION, feeChargesDue(loanSchedule, 2));
        assertEquals(FEE_PORTION + NEXT_FEE_PORTION, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION + NEXT_FEE_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION + NEXT_FEE_PORTION, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(LocalDate.of(2011, 4, 8), loanSchedule.get(2).getDueDate());

        runPeriodicAccrualAccounting(penaltyCharge2AddedDate);
        checkAccrualTransactionForRepayment(LocalDate.of(2011, 4, 7), 0.0, FEE_PORTION, NEXT_PENALTY_PORTION, loanID);
    }

    @Test
    public void reopenClosedLoan() {
        final Long loanProductID = createLoanProduct(
                noInterestPeriodicAccrualProduct(LP_PRINCIPAL, 1, LoanTestData.RepaymentFrequencyType.MONTHS_L, 1));

        final Long clientID = createClient(DATE_OF_JOINING);

        final Long loanID = applyForNoInterestLoan(clientID, loanProductID);

        final double PENALTY_PORTION = 100.0;

        Long flatSpecifiedDueDate = chargesHelper.createLoanSpecifiedDueDatePenalty(PENALTY_PORTION).getResourceId();

        assertTrue(getLoanDetails(loanID).getStatus().getPendingApproval());

        approveLoan(loanID, LoanRequestBuilders.approveLoan(LP_PRINCIPAL, EXPECTED_DISBURSAL_DATE));
        assertTrue(getLoanDetails(loanID).getStatus().getWaitingForDisbursal());

        disburseLoan(loanID, LoanRequestBuilders.disburseLoan(LP_PRINCIPAL, EXPECTED_DISBURSAL_DATE));
        assertTrue(getLoanDetails(loanID).getStatus().getActive());

        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 1));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 1));
        assertEquals(0.0, penaltyChargesDue(loanSchedule, 1));
        assertEquals(0.0, penaltyChargesOutstanding(loanSchedule, 1));
        assertEquals(10000.0, totalDueForPeriod(loanSchedule, 1));
        assertEquals(10000.0, totalOutstandingForPeriod(loanSchedule, 1));

        makeRepayment("10 March 2011", 10000.0f, loanID);
        assertTrue(getLoanDetails(loanID).getStatus().getClosedObligationsMet());

        final String penaltyCharge1AddedDate = "13 April 2011";
        Long penalty1LoanChargeId = addChargesForLoan(loanID,
                LoanRequestBuilders.addLoanCharge(flatSpecifiedDueDate, PENALTY_PORTION, penaltyCharge1AddedDate)).getResourceId();

        assertTrue(getLoanDetails(loanID).getStatus().getActive());

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 2));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(LocalDate.of(2011, 4, 13), loanSchedule.get(2).getDueDate());

        runPeriodicAccrualAccounting("14 April 2011");
        // Transaction date will be the due date of the instalment (in case of N+1 scenario)
        checkAccrualTransactionForRepayment(LocalDate.of(2011, 4, 13), 0.0, 0.0, PENALTY_PORTION, loanID);

        waiveLoanCharge(loanID, penalty1LoanChargeId, 2);

        assertTrue(getLoanDetails(loanID).getStatus().getClosedObligationsMet());

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 2));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(0.0, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesWaived(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(0.0, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalWaivedForPeriod(loanSchedule, 2));
        assertEquals(LocalDate.of(2011, 4, 13), loanSchedule.get(2).getDueDate());

        final String penaltyCharge2AddedDate = "14 April 2011";
        Long penalty2LoanChargeId = addChargesForLoan(loanID,
                LoanRequestBuilders.addLoanCharge(flatSpecifiedDueDate, PENALTY_PORTION, penaltyCharge2AddedDate)).getResourceId();

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 2));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesWaived(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalWaivedForPeriod(loanSchedule, 2));
        assertEquals(LocalDate.of(2011, 4, 14), loanSchedule.get(2).getDueDate());

        assertTrue(getLoanDetails(loanID).getStatus().getActive());

        makeRepayment("15 April 2011", (float) PENALTY_PORTION, loanID);

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 2));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(0.0, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesWaived(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesPaid(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(0.0, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalWaivedForPeriod(loanSchedule, 2));
        // Might need to change if refund should update the due date of N+1 instalment
        assertEquals(LocalDate.of(2011, 4, 14), loanSchedule.get(2).getDueDate());

        assertTrue(getLoanDetails(loanID).getStatus().getClosedObligationsMet());

        loanChargeRefund(loanID, penalty2LoanChargeId, PENALTY_PORTION);

        assertTrue(getLoanDetails(loanID).getStatus().getOverpaid());

        loanSchedule = getRepaymentSchedule(loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0.0, feeChargesDue(loanSchedule, 2));
        assertEquals(0.0, feeChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, penaltyChargesDue(loanSchedule, 2));
        assertEquals(0.0, penaltyChargesOutstanding(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesWaived(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, penaltyChargesPaid(loanSchedule, 2));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, totalDueForPeriod(loanSchedule, 2));
        assertEquals(0.0, totalOutstandingForPeriod(loanSchedule, 2));
        assertEquals(PENALTY_PORTION, totalWaivedForPeriod(loanSchedule, 2));
        // Might need to change if refund should update the due date of N+1 instalment
        assertEquals(LocalDate.of(2011, 4, 14), loanSchedule.get(2).getDueDate());
    }

    @Test
    public void addChargeAfterLoanMaturity() {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate("01 September 2023");

            PostChargesResponse penaltyCharge = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0);

            final PostLoansResponse loanResponse = applyForLoanApplication(clientId, commonLoanProductId, 1000L, 30, 30, 1, BigDecimal.ZERO,
                    "01 September 2023", "01 September 2023");

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 September 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 September 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1000.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 0.0, 1000.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 0, 1000.0, 0.0, 0.0, 1000.0);
            assertTrue(loanDetails.getStatus().getActive());

            updateBusinessDate("01 October 2023");

            PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("01 October 2023").locale("en")
                            .transactionAmount(1000.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 1000.0, 0.0, 1000.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            updateBusinessDate("04 October 2023");

            makeMerchantIssuedRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("04 October 2023").locale("en").transactionAmount(1000.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 1000.0, 0.0, 1000.0, 1000.0);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 1000.0, 0.0, 1000.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            reverseLoanTransaction(loanResponse.getLoanId(), repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("04 October 2023")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 1000.0, 0.0, 1000.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 1000.0);
            validateLoanTransaction(loanDetails, 2, 1000.0, 1000.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            addChargesForLoan(loanResponse.getLoanId(), new PostLoansLoanIdChargesRequest().chargeId(penaltyCharge.getResourceId())
                    .dateFormat(DATETIME_PATTERN).locale("en").amount(10.0).dueDate("04 October 2023"));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 10.0, 1000.0, 0.0, 1000.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 1000.0);
            validateRepaymentPeriod(loanDetails, 2, 0.0, 0.0, 0.0, 10.0, 0.0, 10.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private PostLoanProductsRequest noInterestPeriodicAccrualProduct(double principal, int repaymentEvery, long repaymentFrequencyType,
            int numberOfRepayments) {
        return create4Period1MonthLongWithoutInterestProduct(STRATEGY)//
                .minPrincipal(principal)//
                .principal(principal)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(repaymentEvery)//
                .repaymentFrequencyType(repaymentFrequencyType)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL);
    }

    private Long applyForNoInterestLoan(final Long clientId, final Long loanProductID) {
        return applyForLoan(new PostLoansRequest().clientId(clientId).productId(loanProductID).principal(BigDecimal.valueOf(LP_PRINCIPAL))//
                .loanTermFrequency(1).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(1).repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.ZERO).interestType(LoanTestData.InterestType.FLAT)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL).expectedDisbursementDate(EXPECTED_DISBURSAL_DATE)//
                .submittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE).transactionProcessingStrategyCode(STRATEGY).loanType("individual")//
                .dateFormat(DATETIME_PATTERN).locale("en"));
    }

    private PostLoansResponse applyForLoanApplication(final Long clientId, final Long loanProductId, final Long principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        return loanHelper.applyForLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId)
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(DATETIME_PATTERN).transactionProcessingStrategyCode(STRATEGY)
                .locale("en").submittedOnDate(submittedOnDate).amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)
                .interestRatePerPeriod(interestRate)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE).repaymentEvery(repaymentAfterEvery)
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS).numberOfRepayments(numberOfRepayments)
                .loanTermFrequency(loanTermFrequency).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)
                .principal(BigDecimal.valueOf(principal)).loanType("individual"));
    }

    private List<GetLoansLoanIdRepaymentPeriod> getRepaymentSchedule(Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods();
    }

    private void checkAccrualTransactionForRepayment(final LocalDate transactionDate, final double interestPortion, final double feePortion,
            final double penaltyPortion, final Long loanId) {
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : getLoanDetails(loanId).getTransactions()) {
            if (Boolean.TRUE.equals(transaction.getType().getAccrual()) && transactionDate.equals(transaction.getDate())) {
                isTransactionFound = true;
                assertEquals(interestPortion, Utils.getDoubleValue(transaction.getInterestPortion()), "Mismatch in transaction amounts");
                assertEquals(feePortion, Utils.getDoubleValue(transaction.getFeeChargesPortion()), "Mismatch in transaction amounts");
                assertEquals(penaltyPortion, Utils.getDoubleValue(transaction.getPenaltyChargesPortion()),
                        "Mismatch in transaction amounts");
                break;
            }
        }
        assertTrue(isTransactionFound, "No Accrual entries are posted");
    }

    private void noAccrualTransactionForRepayment(final Long loanId) {
        for (GetLoansLoanIdTransactions transaction : getLoanDetails(loanId).getTransactions()) {
            assertFalse(Boolean.TRUE.equals(transaction.getType().getAccrual()), "Accrual entries are posted!");
        }
    }

    private void loanChargeRefund(final Long loanId, final Long loanChargeId, final double amount) {
        ok(() -> fineractClient().loanTransactions().handleCommandsLoanTransaction(loanId,
                new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat(DATETIME_PATTERN).loanChargeId(loanChargeId)
                        .transactionAmount(amount).note("Loancharge Refund Made!!!"),
                "chargeRefund"));
    }

    private static double feeChargesDue(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getFeeChargesDue());
    }

    private static double feeChargesOutstanding(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getFeeChargesOutstanding());
    }

    private static double penaltyChargesDue(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getPenaltyChargesDue());
    }

    private static double penaltyChargesOutstanding(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getPenaltyChargesOutstanding());
    }

    private static double penaltyChargesWaived(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getPenaltyChargesWaived());
    }

    private static double penaltyChargesPaid(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getPenaltyChargesPaid());
    }

    private static double totalDueForPeriod(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getTotalDueForPeriod());
    }

    private static double totalOutstandingForPeriod(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getTotalOutstandingForPeriod());
    }

    private static double totalWaivedForPeriod(List<GetLoansLoanIdRepaymentPeriod> schedule, int index) {
        return Utils.getDoubleValue(schedule.get(index).getTotalWaivedForPeriod());
    }

    private static void validateLoanTransaction(GetLoansLoanIdResponse loanDetails, int index, double transactionAmount,
            double principalPortion, double overPaidPortion, double loanBalance) {
        assertEquals(transactionAmount, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getAmount()));
        assertEquals(principalPortion, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getPrincipalPortion()));
        assertEquals(overPaidPortion, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getOverpaymentPortion()));
        assertEquals(loanBalance, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getOutstandingLoanBalance()));
    }

    private static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, int index, double principalDue, double principalPaid,
            double principalOutstanding, double paidInAdvance, double paidLate) {
        assertEquals(principalDue, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalDue()));
        assertEquals(principalPaid, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalPaid()));
        assertEquals(principalOutstanding,
                Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalOutstanding()));
        assertEquals(paidInAdvance,
                Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidInAdvanceForPeriod()));
        assertEquals(paidLate,
                Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidLateForPeriod()));
    }

    private static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, int index, double principalDue, double principalPaid,
            double principalOutstanding, double penaltyDue, double penaltyPaid, double penaltyOutstanding, double paidInAdvance,
            double paidLate) {
        assertEquals(principalDue, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalDue()));
        assertEquals(principalPaid, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalPaid()));
        assertEquals(principalOutstanding,
                Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalOutstanding()));
        assertEquals(penaltyDue, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPenaltyChargesDue()));
        assertEquals(penaltyPaid, Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPenaltyChargesPaid()));
        assertEquals(penaltyOutstanding,
                Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getPenaltyChargesOutstanding()));
        assertEquals(paidInAdvance,
                Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidInAdvanceForPeriod()));
        assertEquals(paidLate,
                Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidLateForPeriod()));
    }
}
