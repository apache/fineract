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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Loan Integration Test for checking Loan Application Repayments Schedule, loan charges, penalties, loan
 * repayments and verifying accounting transactions
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ClientLoanMultipleDisbursementsIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanMultipleDisbursementsIntegrationTest.class);

    private Long clientId;

    @BeforeEach
    public void setup() {
        clientId = createClient();
    }

    private Long createLoanProduct(final boolean multiDisburseLoan) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
            builder = builder.withMaxTrancheCount("30");
        }
        final PostLoanProductsRequest loanProductRequest = builder.buildRequest(null);
        return createLoanProduct(loanProductRequest);
    }

    private Long applyForLoanApplicationWithTranches(final Long clientId, final Long loanProductID, String principal,
            List<PostLoansDisbursementData> tranches, String submitDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoan(LoanRequestBuilders
                .legacyIndividualApplication(clientId, loanProductID, principal, 4, BigDecimal.ZERO, submitDate).disbursementData(tranches)//
                .fixedEmiAmount(new BigDecimal("10000")));
    }

    private PostLoansDisbursementData createTrancheDetail(final String date, final String amount) {
        return new PostLoansDisbursementData().expectedDisbursementDate(date).principal(new BigDecimal(amount));
    }

    /**
     * Sums the per-disbursement {@code principalDisbursed} across the schedule's disbursement rows (those without a
     * {@code period} number). The generated Feign {@code GetLoansLoanIdRepaymentPeriod} model does not expose
     * {@code principalDisbursed}, so read it from the raw loan JSON.
     */
    private BigDecimal sumDisbursedPrincipal(final Long loanId) {
        String loanJson = FeignRawHttpHelper.get("/loans/" + loanId + "?associations=all&exclude=guarantors,futureSchedule");
        JsonArray periods = JsonParser.parseString(loanJson).getAsJsonObject().getAsJsonObject("repaymentSchedule")
                .getAsJsonArray("periods");
        BigDecimal total = BigDecimal.ZERO;
        for (JsonElement element : periods) {
            JsonObject period = element.getAsJsonObject();
            boolean isDisbursement = !period.has("period") || period.get("period").isJsonNull();
            if (isDisbursement && period.has("principalDisbursed")) {
                total = total.add(period.get("principalDisbursed").getAsBigDecimal());
            }
        }
        return total;
    }

    /***
     * Test case to verify repayment schedule shows all disbursals for tranche loans
     */
    @Test
    public void checkThatAllMultiDisbursalsAppearOnLoanScheduleAndOutStandingBalanceIsZeroTest() {
        /***
         * Create loan product with allowing multiple disbursals
         */
        boolean allowMultipleDisbursals = true;
        final Long loanProductID = createLoanProduct(allowMultipleDisbursals);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String principal = "12,000.00";

        LOG.info("-----------------------------------10 Tranches--------------------------------------");
        List<PostLoansDisbursementData> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 January 2021", "1"));
        tranches.add(createTrancheDetail("02 January 2021", "2"));
        tranches.add(createTrancheDetail("03 January 2021", "4"));
        tranches.add(createTrancheDetail("04 January 2021", "8"));
        tranches.add(createTrancheDetail("05 January 2021", "16"));
        tranches.add(createTrancheDetail("06 January 2021", "32"));
        tranches.add(createTrancheDetail("07 January 2021", "64"));
        tranches.add(createTrancheDetail("08 January 2021", "128"));
        tranches.add(createTrancheDetail("09 January 2021", "256"));
        tranches.add(createTrancheDetail("10 January 2021", "512"));
        String submitDate = "01 January 2021";

        final Long loanID = applyForLoanApplicationWithTranches(clientId, loanProductID, principal, tranches, submitDate);
        Assertions.assertNotNull(loanID);
        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        approveLoan(loanID, approveLoanRequest(12000.0, "01 January 2021"));
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        LOG.info("-------------------------------DISBURSE 8 LOANS -------------------------------------------");
        disburseLoanWithNetDisbursalAmount(loanID, "12 January 2021", "1");
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);
        disburseLoanWithNetDisbursalAmount(loanID, "12 January 2021", "2");
        disburseLoanWithNetDisbursalAmount(loanID, "12 January 2021", "4");
        disburseLoanWithNetDisbursalAmount(loanID, "13 January 2021", "8");
        disburseLoanWithNetDisbursalAmount(loanID, "14 January 2021", "16");
        disburseLoanWithNetDisbursalAmount(loanID, "14 January 2021", "32");
        disburseLoanWithNetDisbursalAmount(loanID, "15 January 2021", "64");
        disburseLoanWithNetDisbursalAmount(loanID, "15 January 2021", "128");

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = loanDetails.getRepaymentSchedule().getPeriods();
        final int loanScheduleLineCount = loanSchedule.size();
        final int expectedLoanScheduleLineCount = 9;
        final int expectedDisbursals = 8;
        final BigDecimal val255 = BigDecimal.valueOf(255.0);
        final BigDecimal expectedTotalPrincipalDisbursed = val255;
        final BigDecimal expectedPrincipalDue = val255;
        final BigDecimal expectedPrincipalLoanBalanceOutstanding = BigDecimal.valueOf(0.0);

        assertEquals(expectedLoanScheduleLineCount, loanScheduleLineCount, "Checking nine lines in schedule");

        int disbursalCount = 0;
        // First 8 lines should be disbursals
        for (int i = 0; i < loanScheduleLineCount - 1; i++) {
            if (loanSchedule.get(i).getPeriod() == null) {
                disbursalCount += 1;
            }
        }
        assertEquals(expectedDisbursals, disbursalCount, "Checking for eight disbursals");
        final BigDecimal totalPrincipalDisbursed = sumDisbursedPrincipal(loanID);
        assertTrue(expectedTotalPrincipalDisbursed.compareTo(totalPrincipalDisbursed) == 0, "Checking Principal Disburse is 255");

        GetLoansLoanIdRepaymentPeriod lastPeriod = loanSchedule.get(8);
        final BigDecimal principalDue = lastPeriod.getPrincipalDue();
        assertTrue(expectedPrincipalDue.compareTo(principalDue) == 0, "Checking Principal Due is 255");

        final BigDecimal principalLoanBalanceOutstanding = lastPeriod.getPrincipalLoanBalanceOutstanding();
        assertTrue(expectedPrincipalLoanBalanceOutstanding.compareTo(principalLoanBalanceOutstanding) == 0,
                "Checking Principal Loan Balance Outstanding is zero");
    }

    @Test
    public void checkThatAllMultiDisbursalsAppearOnLoanScheduleAndOutStandingBalanceIsZeroButLoanGotReopenedTest() {
        /***
         * Create loan product with allowing multiple disbursals
         */
        boolean allowMultipleDisbursals = true;
        final Long loanProductID = createLoanProduct(allowMultipleDisbursals);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String principal = "12,000.00";

        LOG.info("-----------------------------------2 Tranches--------------------------------------");
        List<PostLoansDisbursementData> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 January 2021", "1"));
        tranches.add(createTrancheDetail("02 January 2021", "2"));
        String submitDate = "01 January 2021";

        final Long loanID = applyForLoanApplicationWithTranches(clientId, loanProductID, principal, tranches, submitDate);
        Assertions.assertNotNull(loanID);
        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        approveLoan(loanID, approveLoanRequest(12000.0, "01 January 2021"));
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        LOG.info(
                "-------------------------------DISBURSE 1, repay fully, disburse again LOANS -------------------------------------------");
        disburseLoanWithAmount(loanID, "12 January 2021", 1);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);
        makeLoanRepayment(loanID, "Repayment", "13 January 2021", 1.0);
        verifyLoanStatus(loanID, LoanStatus.CLOSED_OBLIGATIONS_MET);
        disburseLoanWithAmount(loanID, "14 January 2021", 2);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = loanDetails.getRepaymentSchedule().getPeriods();
        final int loanScheduleLineCount = loanSchedule.size();
        final int expectedLoanScheduleLineCount = 3;
        final int expectedDisbursals = 2;
        final BigDecimal expectedTotalPrincipalDisbursed = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalDue = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalPaid = BigDecimal.valueOf(1.0);
        final BigDecimal expectedPrincipalOutstanding = BigDecimal.valueOf(2.0);
        final BigDecimal expectedPrincipalLoanBalanceOutstanding = BigDecimal.valueOf(0.0);

        assertEquals(expectedLoanScheduleLineCount, loanScheduleLineCount, "Checking 3 lines in schedule");

        int disbursalCount = 0;
        for (int i = 0; i < loanScheduleLineCount - 1; i++) {
            if (loanSchedule.get(i).getPeriod() == null) {
                disbursalCount += 1;
            }
        }
        assertEquals(expectedDisbursals, disbursalCount, "Checking for 2 disbursals");
        final BigDecimal totalPrincipalDisbursed = sumDisbursedPrincipal(loanID);
        assertTrue(expectedTotalPrincipalDisbursed.compareTo(totalPrincipalDisbursed) == 0, "Checking Principal Disburse is 3");

        GetLoansLoanIdRepaymentPeriod lastPeriod = loanSchedule.get(2);
        final BigDecimal principalDue = lastPeriod.getPrincipalDue();
        assertTrue(expectedPrincipalDue.compareTo(principalDue) == 0, "Checking Principal Due is 3");
        final BigDecimal principalPaid = lastPeriod.getPrincipalPaid();
        assertTrue(expectedPrincipalPaid.compareTo(principalPaid) == 0, "Checking Principal Paid is 1");
        final BigDecimal principalOutstanding = lastPeriod.getPrincipalOutstanding();
        assertTrue(expectedPrincipalOutstanding.compareTo(principalOutstanding) == 0, "Checking Principal Due is 2");

        final BigDecimal principalLoanBalanceOutstanding = lastPeriod.getPrincipalLoanBalanceOutstanding();
        assertTrue(expectedPrincipalLoanBalanceOutstanding.compareTo(principalLoanBalanceOutstanding) == 0,
                "Checking Principal Loan Balance Outstanding is zero");
    }

    @Test
    public void checkThatAllMultiDisbursalsAppearOnLoanScheduleAndOutStandingBalanceIsZeroButLoanGotReopenedFromOverPaidTest() {
        /***
         * Create loan product with allowing multiple disbursals
         */
        boolean allowMultipleDisbursals = true;
        final Long loanProductID = createLoanProduct(allowMultipleDisbursals);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String principal = "12,000.00";

        LOG.info("-----------------------------------2 Tranches--------------------------------------");
        List<PostLoansDisbursementData> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 January 2021", "1"));
        tranches.add(createTrancheDetail("02 January 2021", "2"));
        String submitDate = "01 January 2021";

        final Long loanID = applyForLoanApplicationWithTranches(clientId, loanProductID, principal, tranches, submitDate);
        Assertions.assertNotNull(loanID);
        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        approveLoan(loanID, approveLoanRequest(12000.0, "01 January 2021"));
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        LOG.info(
                "-------------------------------DISBURSE 1, repay fully, disburse again LOANS -------------------------------------------");
        disburseLoanWithAmount(loanID, "12 January 2021", 1);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);
        makeLoanRepayment(loanID, "Repayment", "13 January 2021", 2.0);
        verifyLoanStatus(loanID, LoanStatus.OVERPAID);
        disburseLoanWithAmount(loanID, "14 January 2021", 2);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        List<GetLoansLoanIdRepaymentPeriod> loanSchedule = loanDetails.getRepaymentSchedule().getPeriods();
        final int loanScheduleLineCount = loanSchedule.size();
        final int expectedLoanScheduleLineCount = 3;
        final int expectedDisbursals = 2;
        final BigDecimal expectedTotalPrincipalDisbursed = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalDue = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalPaid = BigDecimal.valueOf(2.0);
        final BigDecimal expectedPrincipalOutstanding = BigDecimal.valueOf(1.0);
        final BigDecimal expectedPrincipalLoanBalanceOutstanding = BigDecimal.valueOf(0.0);

        assertEquals(expectedLoanScheduleLineCount, loanScheduleLineCount, "Checking nine lines in schedule");

        int disbursalCount = 0;
        for (int i = 0; i < loanScheduleLineCount - 1; i++) {
            if (loanSchedule.get(i).getPeriod() == null) {
                disbursalCount += 1;
            }
        }
        assertEquals(expectedDisbursals, disbursalCount, "Checking for 2 disbursals");
        final BigDecimal totalPrincipalDisbursed = sumDisbursedPrincipal(loanID);
        assertTrue(expectedTotalPrincipalDisbursed.compareTo(totalPrincipalDisbursed) == 0, "Checking Principal Disburse is 3");

        GetLoansLoanIdRepaymentPeriod lastPeriod = loanSchedule.get(2);
        final BigDecimal principalDue = lastPeriod.getPrincipalDue();
        assertTrue(expectedPrincipalDue.compareTo(principalDue) == 0, "Checking Principal Due is 3");
        final BigDecimal principalPaid = lastPeriod.getPrincipalPaid();
        assertTrue(expectedPrincipalPaid.compareTo(principalPaid) == 0, "Checking Principal Paid is 1");
        final BigDecimal principalOutstanding = lastPeriod.getPrincipalOutstanding();
        assertTrue(expectedPrincipalOutstanding.compareTo(principalOutstanding) == 0, "Checking Principal Due is 2");

        final BigDecimal principalLoanBalanceOutstanding = lastPeriod.getPrincipalLoanBalanceOutstanding();
        assertTrue(expectedPrincipalLoanBalanceOutstanding.compareTo(principalLoanBalanceOutstanding) == 0,
                "Checking Principal Loan Balance Outstanding is zero");
    }
}
