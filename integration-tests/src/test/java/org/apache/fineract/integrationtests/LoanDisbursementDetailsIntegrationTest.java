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

import static java.lang.Double.parseDouble;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDisbursementDetails;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdDisbursementData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;

public class LoanDisbursementDetailsIntegrationTest extends FeignLoanTestBase {

    private static final Pattern EXPECTED_DATE = Pattern.compile("\\[\\s*(\\d+),\\s*(\\d+),\\s*(\\d+)\\s*]");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    private static final String APPROVE_DATE = "01 March 2014";
    private static final String EXPECTED_DISBURSEMENT_DATE = "01 March 2014";
    private static final String PROPOSED_AMOUNT = "5000";
    private static final String APPROVAL_AMOUNT = "5000";
    private static final String OVER_APPLIED_ERROR = "error.msg.loan.disbursal.amount.can't.be.greater.than.maximum.applied.loan.amount.calculation";

    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    private Long loanId;
    private Long disbursementId;

    @Test
    public void createAndValidateMultiDisburseLoansBasedOnEmi() {
        final String installmentAmount = "800";
        final String proposedAmount = "10000";
        final List<PostLoansDisbursementData> createTranches = List.of(LoanRequestBuilders.applyTrancheDetail("01 June 2015", 5000.0),
                LoanRequestBuilders.applyTrancheDetail("01 September 2015", 5000.0));
        final List<PostLoansLoanIdDisbursementData> approveTranches = List.of(
                LoanRequestBuilders.approveTrancheDetail("01 June 2015", 5000.0),
                LoanRequestBuilders.approveTrancheDetail("01 September 2015", 5000.0));

        final Long clientId = createClient("01 January 2014");

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance()
                .withMoratorium("", "").withAmortizationTypeAsEqualInstallments().withTranches(true)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).buildRequest(null));

        final Long loanIdWithEmi = applyForLoanApplicationWithEmiAmount(clientId, loanProductId, proposedAmount, createTranches,
                installmentAmount);

        final List<GetLoansLoanIdRepaymentPeriod> periods = getLoanDetails(loanIdWithEmi).getRepaymentSchedule().getPeriods();
        assertEquals(15, periods.size());
        validateRepaymentScheduleWithEMI(periods);

        verifyLoanStatus(loanIdWithEmi, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanIdWithEmi, LoanRequestBuilders.approveLoanWithTranches(10000.0, "01 June 2015", "01 June 2015", approveTranches));
        verifyLoanStatus(loanIdWithEmi, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanIdWithEmi), status -> Boolean.TRUE.equals(status.getWaitingForDisbursal()));

        final Long loanIdWithoutEmi = applyForLoanApplicationWithEmiAmount(clientId, loanProductId, proposedAmount, createTranches, "");
        assertEquals(15, getLoanDetails(loanIdWithEmi).getRepaymentSchedule().getPeriods().size());

        verifyLoanStatus(loanIdWithoutEmi, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanIdWithoutEmi,
                LoanRequestBuilders.approveLoanWithTranches(10000.0, "01 June 2015", "01 June 2015", approveTranches));
        verifyLoanStatus(loanIdWithoutEmi, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(loanIdWithoutEmi), status -> Boolean.TRUE.equals(status.getWaitingForDisbursal()));
    }

    @Test
    public void validateEqualInstallmentsForMultiTrancheLoan() {
        final String operationDate = "01 January 2014";
        final Long clientId = createClient(operationDate);
        final Long loanProductId = createMultiDisburseProductDisallowingExpectedDisbursements();
        final Long loanId = applyForMultiTrancheLoanApplication(clientId, loanProductId, 1000.0, operationDate);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, operationDate, EXPECTED_DISBURSEMENT_DATE));

        loanHelper.disburseLoan(operationDate, loanId, "900");

        evaluateEqualInstallmentsForRepaymentSchedule(getLoanDetails(loanId).getRepaymentSchedule(), BigDecimal.TWO);
    }

    @Test
    public void disburseLoanWithExceededOverAppliedAmountFails() {
        final String operationDate = "01 January 2014";
        final Long clientId = createClient(operationDate);
        final Long loanProductId = createMultiDisburseProductDisallowingExpectedDisbursements();
        final Long loanId = applyForMultiTrancheLoanApplication(clientId, loanProductId, 1000.0, operationDate);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, operationDate, EXPECTED_DISBURSEMENT_DATE));

        loanHelper.disburseLoan(operationDate, loanId, "900");

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> loanHelper.disburseLoan(operationDate, loanId, "1101"));
        assertEquals(403, exception.getStatus());
        assertErrorGlobalisationCode(exception, OVER_APPLIED_ERROR);
    }

    @Test
    public void disburseLoanWithExceededOverAppliedAmountSucceed() {
        final String operationDate = "01 January 2014";
        final String firstDisbursedPrincipal = "900";
        final String secondDisbursedPrincipal = "1100";

        final Long clientId = createClient(operationDate);
        final Long loanProductId = createMultiDisburseProductDisallowingExpectedDisbursements();
        final Long loanId = applyForMultiTrancheLoanApplication(clientId, loanProductId, 1000.0, operationDate);

        approveLoan(loanId, LoanRequestBuilders.approveLoan(1000.0, operationDate, EXPECTED_DISBURSEMENT_DATE));

        loanHelper.disburseLoan(operationDate, loanId, firstDisbursedPrincipal);
        loanHelper.disburseLoan(operationDate, loanId, secondDisbursedPrincipal);

        final double disbursementPrincipalSum = getLoanDetails(loanId).getDisbursementDetails().stream()
                .mapToDouble(d -> d.getPrincipal().doubleValue()).sum();
        assertEquals(parseDouble(firstDisbursedPrincipal) + parseDouble(secondDisbursedPrincipal), disbursementPrincipalSum);
    }

    @Test
    public void createApproveAndValidateMultiDisburseLoan() {
        final List<PostLoansDisbursementData> createTranches = List.of(LoanRequestBuilders.applyTrancheDetail("01 March 2014", 1000.0));
        final List<PostLoansLoanIdDisbursementData> approveTranches = List
                .of(LoanRequestBuilders.approveTrancheDetail("01 March 2014", 1000.0));

        final Long clientId = createClient("01 January 2014");

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withInterestTypeAsDecliningBalance().withTranches(true)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).buildRequest(null));

        this.loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, PROPOSED_AMOUNT, createTranches);
        verifyLoanStatus(this.loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(this.loanId, LoanRequestBuilders.approveLoanWithTranches(parseDouble(APPROVAL_AMOUNT), APPROVE_DATE,
                EXPECTED_DISBURSEMENT_DATE, approveTranches));
        verifyLoanStatus(this.loanId, LoanStatus.APPROVED);
        verifyLoanStatus(getLoanDetails(this.loanId), status -> Boolean.TRUE.equals(status.getWaitingForDisbursal()));

        this.disbursementId = getLoanDetails(this.loanId).getDisbursementDetails().get(0).getId();
        editLoanDisbursementDetails();
    }

    @Test
    public void allowModifyLoanApplicationAfterUndoDisbursalWithTranches() {
        final String operationDate = APPROVE_DATE;
        String principal = "1000";

        final Long clientId = createClient(operationDate);
        final Long loanProductId = createMultiDisburseProductDisallowingExpectedDisbursements();

        final GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        assertNotNull(loanProduct);
        assertEquals(true, loanProduct.getDisallowExpectedDisbursements());

        final Long loanId = applyForLoanApplicationWithTranches(clientId, loanProductId, PROPOSED_AMOUNT, List.of());

        approveLoan(loanId, LoanRequestBuilders.approveLoan(parseDouble(APPROVAL_AMOUNT), operationDate, operationDate));
        assertEquals(0, getLoanDetails(loanId).getDisbursementDetails().size(), "Disbursement details items");

        loanHelper.disburseLoan(operationDate, loanId, principal);
        assertEquals(1, getLoanDetails(loanId).getDisbursementDetails().size(), "Disbursement details items");

        loanHelper.undoDisbursement(loanId);
        assertEquals(0, getLoanDetails(loanId).getDisbursementDetails().size(), "Disbursement details items");

        undoApproval(loanId);

        principal = "10000";
        assertNotNull(
                modifyLoanApplication(loanId, "modify", modifyLoanApplicationRequest(clientId, loanProductId, principal, operationDate)));

        // ReDo the Approval and Disbursement
        approveLoan(loanId, LoanRequestBuilders.approveLoan(parseDouble(APPROVAL_AMOUNT), operationDate, operationDate));
        loanHelper.disburseLoan(operationDate, loanId, principal);
        assertEquals(1, getLoanDetails(loanId).getDisbursementDetails().size(), "Disbursement details items");
    }

    @Test
    public void testCreateLoanProductWithFullTermTrancheEnabled() {
        final Long loanProductId = createLoanProduct(
                progressiveTrancheProductBuilder().withAllowFullTermForTranche(true).buildRequest(null));

        final GetLoanProductsProductIdResponse loanProduct = retrieveLoanProduct(loanProductId);
        assertNotNull(loanProduct);
        assertEquals(true, loanProduct.getMultiDisburseLoan());
        assertEquals(true, loanProduct.getAllowFullTermForTranche());
    }

    @Test
    public void testCreateLoanProductWithFullTermTrancheOnCumulativeShouldFail() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments()
                        .withInterestTypeAsDecliningBalance().withMoratorium("", "")
                        .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withInterestTypeAsDecliningBalance().withMultiDisburse()
                        .withLoanScheduleType(LoanScheduleType.CUMULATIVE).withAllowFullTermForTranche(true).buildRequest(null)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, "validation.msg.loanproduct.allowFullTermForTranche.requires.progressive.schedule.type");
    }

    @Test
    public void testCreateLoanProductWithFullTermTrancheOnSingleDisburseShouldFail() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> createLoanProduct(
                        new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                                .withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                                .withInterestTypeAsDecliningBalance().withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation("NEXT_INSTALLMENT"))
                                .withAllowFullTermForTranche(true).buildRequest(null)));

        assertEquals(400, exception.getStatus());
        assertErrorGlobalisationCode(exception, "validation.msg.loanproduct.allowFullTermForTranche.requires.multi.disburse.loan");
    }

    @Test
    public void testUpdateLoanProductPreservesAllowFullTermForTranche() {
        final Long loanProductId = createLoanProduct(
                progressiveTrancheProductBuilder().withAllowFullTermForTranche(true).buildRequest(null));

        assertEquals(true, retrieveLoanProduct(loanProductId).getAllowFullTermForTranche());

        updateLoanProduct(loanProductId, new PutLoanProductsProductIdRequest().description("Updated description").locale("en"));

        final GetLoanProductsProductIdResponse updatedProduct = retrieveLoanProduct(loanProductId);
        assertNotNull(updatedProduct);
        assertEquals(true, updatedProduct.getAllowFullTermForTranche());
        assertEquals("Updated description", updatedProduct.getDescription());
    }

    @Test
    public void testLoanInheritsAllowFullTermForTrancheFromProduct() {
        final Long loanProductId = createLoanProduct(
                progressiveTrancheProductBuilder().withAllowFullTermForTranche(true).buildRequest(null));
        final Long clientId = createClient();

        final Long loanId = applyForProgressiveTrancheLoan(clientId, loanProductId, 10000.0, 12, "01 March 2014", "1",
                List.of(LoanRequestBuilders.applyTrancheDetail("01 March 2014", 5000.0),
                        LoanRequestBuilders.applyTrancheDetail("01 April 2014", 5000.0)),
                null);

        final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertEquals(true, loanDetails.getAllowFullTermForTranche());
    }

    @Test
    public void testLoanLevelOverrideOfAllowFullTermForTranche() {
        final Long loanProductId = createLoanProduct(
                progressiveTrancheProductBuilder().withAllowFullTermForTranche(true).buildRequest(null));
        final Long clientId = createClient();

        final Long loanId = applyForProgressiveTrancheLoan(clientId, loanProductId, 10000.0, 12, "01 March 2014", "1",
                List.of(LoanRequestBuilders.applyTrancheDetail("01 March 2014", 5000.0),
                        LoanRequestBuilders.applyTrancheDetail("01 April 2014", 5000.0)),
                false);

        final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertEquals(false, loanDetails.getAllowFullTermForTranche());
    }

    @Test
    public void testFullTermTranche_S1_DisbursementOnInstallmentDate() {
        final Long loanId = fullTermTrancheLoan(true, "01 February 2024");

        disburseFullTermTranches(loanId, "01 January 2024", "01 February 2024");

        verifyFullTermTranchePeriods(loanId, 9, 7, "Total periods should be 9 (2 disbursements + 7 repayment periods)",
                "Should have 7 repayment periods");

        closeFullTermTrancheLoan(loanId, "01 August 2024");
    }

    @Test
    public void testFullTermTranche_S2_MidPeriodDisbursement() {
        final Long loanId = fullTermTrancheLoan(true, "01 February 2024");

        disburseFullTermTranches(loanId, "01 January 2024", "15 February 2024");

        verifyFullTermTranchePeriods(loanId, 9, 7, "Total periods should be 9 (2 disbursements + 7 repayment periods)",
                "Should have 7 repayment periods");

        closeFullTermTrancheLoan(loanId, "01 August 2024");
    }

    @Test
    public void testFullTermTranche_S3_BothBeforeFirstRepayment() {
        final Long loanId = fullTermTrancheLoan(true, "15 January 2024");

        disburseFullTermTranches(loanId, "01 January 2024", "15 January 2024");

        verifyFullTermTranchePeriods(loanId, 8, 6, "Total periods should be 8 (2 disbursements + 6 repayment periods - NO EXTENSION)",
                "Should have 6 repayment periods (no term extension)");

        closeFullTermTrancheLoan(loanId, "01 July 2024");
    }

    @Test
    public void testFullTermTrancheBackwardCompatibility() {
        final Long loanId = fullTermTrancheLoan(false, "01 February 2024");

        disburseFullTermTranches(loanId, "01 January 2024", "01 February 2024");

        final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);
        assertNotNull(loanDetails.getRepaymentSchedule());
        assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
    }

    private LoanProductTestBuilder progressiveTrancheProductBuilder() {
        return new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withInterestTypeAsDecliningBalance()
                .withMultiDisburse().withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation("NEXT_INSTALLMENT"));
    }

    private Long createMultiDisburseProductDisallowingExpectedDisbursements() {
        return createLoanProduct(new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withInterestTypeAsDecliningBalance()
                .withMultiDisburse().withDisallowExpectedDisbursements(true).buildRequest(null));
    }

    private Long fullTermTrancheLoan(final boolean allowFullTermForTranche, final String secondTrancheDate) {
        final AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation("NEXT_INSTALLMENT");

        final Long loanProductId = createLoanProduct(new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments()
                .withInterestTypeAsDecliningBalance().withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withinterestRatePerPeriod("9.4822").withInterestRateFrequencyTypeAsYear().withMultiDisburse()
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).addAdvancedPaymentAllocation(defaultAllocation)
                .withAllowFullTermForTranche(allowFullTermForTranche).withDaysInYear("360").withMinPrincipal("100").buildRequest(null));

        final Long clientId = createClient("01 January 2024");

        final List<PostLoansDisbursementData> createTranches = List.of(LoanRequestBuilders.applyTrancheDetail("01 January 2024", 100.0),
                LoanRequestBuilders.applyTrancheDetail(secondTrancheDate, 100.0));

        final Long loanId = applyForProgressiveTrancheLoan(clientId, loanProductId, 200.0, 6, "01 January 2024", "9.4822", createTranches,
                null);

        approveLoan(loanId,
                LoanRequestBuilders.approveLoanWithTranches(200.0, "01 January 2024", "01 January 2024",
                        List.of(LoanRequestBuilders.approveTrancheDetail("01 January 2024", 100.0),
                                LoanRequestBuilders.approveTrancheDetail(secondTrancheDate, 100.0))));
        return loanId;
    }

    private void disburseFullTermTranches(final Long loanId, final String firstDate, final String secondDate) {
        loanHelper.disburseLoan(firstDate, loanId, "100");
        loanHelper.disburseLoan(secondDate, loanId, "100");
    }

    private void verifyFullTermTranchePeriods(final Long loanId, final int totalPeriods, final int expectedRepaymentPeriods,
            final String totalMessage, final String repaymentMessage) {
        final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails);

        final GetLoansLoanIdRepaymentSchedule schedule = loanDetails.getRepaymentSchedule();
        assertNotNull(schedule);

        final List<GetLoansLoanIdRepaymentPeriod> periods = schedule.getPeriods();
        assertNotNull(periods);
        assertEquals(totalPeriods, periods.size(), totalMessage);

        final long disbursementPeriods = periods.stream().filter(p -> p.getPeriod() == null).count();
        final long repaymentPeriods = periods.stream().filter(p -> p.getPeriod() != null).count();
        assertEquals(2, disbursementPeriods, "Should have 2 disbursement periods");
        assertEquals(expectedRepaymentPeriods, repaymentPeriods, repaymentMessage);
    }

    /** Closes the loan with a full prepayment so the lifecycle cleanup can run. */
    private void closeFullTermTrancheLoan(final Long loanId, final String lastRepaymentDate) {
        final BigDecimal outstandingAmount = getLoanDetails(loanId).getSummary().getTotalOutstanding();
        if (outstandingAmount != null && outstandingAmount.compareTo(BigDecimal.ZERO) > 0) {
            makeLoanRepayment(loanId, LoanRequestBuilders.repayLoan(outstandingAmount.doubleValue(), lastRepaymentDate));
        }
    }

    private void editLoanDisbursementDetails() {
        editDateAndPrincipalOfExistingTranche();
        addNewDisbursementDetails();
        deleteDisbursmentDetails();
    }

    private void editDateAndPrincipalOfExistingTranche() {
        final String updatedExpectedDisbursementDate = "01 March 2014";
        final String updatedPrincipal = "900";

        loanHelper.updateDisbursementDate(this.loanId, this.disbursementId, APPROVAL_AMOUNT, EXPECTED_DISBURSEMENT_DATE,
                updatedExpectedDisbursementDate, updatedPrincipal);

        final GetLoansLoanIdDisbursementDetails detail = getLoanDetails(this.loanId).getDisbursementDetails().get(0);
        assertEquals(0, new BigDecimal(updatedPrincipal).compareTo(detail.getPrincipal()));
        assertEquals(LocalDate.of(2014, 3, 1), detail.getExpectedDisbursementDate());
    }

    private void addNewDisbursementDetails() {
        final GetLoansLoanIdDisbursementDetails existing = getLoanDetails(this.loanId).getDisbursementDetails().get(0);

        final List<DisbursementDetail> addTranches = List.of(
                new DisbursementDetail().id(existing.getId()).expectedDisbursementDate(formatDate(existing.getExpectedDisbursementDate()))
                        .principal(existing.getPrincipal()),
                new DisbursementDetail().expectedDisbursementDate("03 March 2014").principal(BigDecimal.valueOf(2000)),
                new DisbursementDetail().expectedDisbursementDate("04 March 2014").principal(BigDecimal.valueOf(500)));

        addAndDeleteDisbursementDetail(this.loanId, addTranches);
    }

    private void deleteDisbursmentDetails() {
        final List<GetLoansLoanIdDisbursementDetails> disbursementDetails = getLoanDetails(this.loanId).getDisbursementDetails();

        /* Delete the last tranche */
        final List<DisbursementDetail> deleteTranches = new ArrayList<>();
        for (int i = 0; i < disbursementDetails.size() - 1; i++) {
            final GetLoansLoanIdDisbursementDetails detail = disbursementDetails.get(i);
            deleteTranches.add(new DisbursementDetail().id(detail.getId())
                    .expectedDisbursementDate(formatDate(detail.getExpectedDisbursementDate())).principal(detail.getPrincipal()));
        }

        addAndDeleteDisbursementDetail(this.loanId, deleteTranches);
    }

    private String formatDate(final LocalDate date) {
        return DATE_FORMATTER.format(date);
    }

    private void evaluateEqualInstallmentsForRepaymentSchedule(final GetLoansLoanIdRepaymentSchedule schedule, final BigDecimal limit) {
        BigDecimal totalOutstandingForPeriod = BigDecimal.ZERO;
        BigDecimal totalInstallmentAmountForPeriod = BigDecimal.ZERO;
        if (schedule != null) {
            for (GetLoansLoanIdRepaymentPeriod period : schedule.getPeriods()) {
                if (period.getPeriod() != null) {
                    if (period.getPeriod() == 1) {
                        totalOutstandingForPeriod = period.getTotalOutstandingForPeriod();
                        totalInstallmentAmountForPeriod = period.getTotalInstallmentAmountForPeriod();
                    } else {
                        assertTrue(period.getTotalOutstandingForPeriod().subtract(totalOutstandingForPeriod).abs().compareTo(limit) <= 0);
                        assertTrue(period.getTotalInstallmentAmountForPeriod().subtract(totalInstallmentAmountForPeriod).abs()
                                .compareTo(limit) <= 0);
                    }
                }
            }
        }
    }

    private void validateRepaymentScheduleWithEMI(final List<GetLoansLoanIdRepaymentPeriod> periods) {
        LoanDisbursementTestBuilder expectedRepaymentSchedule0 = new LoanDisbursementTestBuilder("[2015, 6, 1]", 0.0f, 0.0f, null, null,
                5000.0f, null, null, null);

        LoanDisbursementTestBuilder expectedRepaymentSchedule1 = new LoanDisbursementTestBuilder("[2015, 7, 1]", 800f, 800.0f, 50.0f,
                750.0f, 4250.0f, 750.0f, 750.0f, "[2015, 6, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule2 = new LoanDisbursementTestBuilder("[2015, 8, 1]", 800.0f, 800.0f, 42.5f,
                757.5f, 3492.5f, 757.5f, 757.5f, "[2015, 7, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule3 = new LoanDisbursementTestBuilder("[2015, 9, 1]", 0.0f, 0.0f, null, null,
                5000.0f, null, null, null);

        LoanDisbursementTestBuilder expectedRepaymentSchedule4 = new LoanDisbursementTestBuilder("[2015, 9, 1]", 800.0f, 800.0f, 34.92f,
                765.08f, 7727.42f, 765.08f, 765.08f, "[2015, 8, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule5 = new LoanDisbursementTestBuilder("[2015, 10, 1]", 800.0f, 800.0f, 77.27f,
                722.73f, 7004.69f, 722.73f, 722.73f, "[2015, 9, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule6 = new LoanDisbursementTestBuilder("[2015, 11, 1]", 800.0f, 800.0f, 70.05f,
                729.95f, 6274.74f, 729.95f, 729.95f, "[2015, 10, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule7 = new LoanDisbursementTestBuilder("[2015, 12, 1]", 800.0f, 800.0f, 62.75f,
                737.25f, 5537.49f, 737.25f, 737.25f, "[2015, 11, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule8 = new LoanDisbursementTestBuilder("[2016, 1, 1]", 800.0f, 800.0f, 55.37f,
                744.63f, 4792.86f, 744.63f, 744.63f, "[2015, 12, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule9 = new LoanDisbursementTestBuilder("[2016, 2, 1]", 800.0f, 800.0f, 47.93f,
                752.07f, 4040.79f, 752.07f, 752.07f, "[2016, 1, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule10 = new LoanDisbursementTestBuilder("[2016, 3, 1]", 800.0f, 800.0f, 40.41f,
                759.59f, 3281.2f, 759.59f, 759.59f, "[2016, 2, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule11 = new LoanDisbursementTestBuilder("[2016, 4, 1]", 800.0f, 800.0f, 32.81f,
                767.19f, 2514.01f, 767.19f, 767.19f, "[2016, 3, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule12 = new LoanDisbursementTestBuilder("[2016, 5, 1]", 800.0f, 800.0f, 25.14f,
                774.86f, 1739.15f, 774.86f, 774.86f, "[2016, 4, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule13 = new LoanDisbursementTestBuilder("[2016, 6, 1]", 800.0f, 800.0f, 17.39f,
                782.61f, 956.54f, 782.61f, 782.61f, "[2016, 5, 1]");

        LoanDisbursementTestBuilder expectedRepaymentSchedule14 = new LoanDisbursementTestBuilder("[2016, 7, 1]", 966.11f, 966.11f, 9.57f,
                956.54f, 0.0f, 956.54f, 956.54f, "[2016, 6, 1]");

        final List<LoanDisbursementTestBuilder> list = new ArrayList<>();
        list.add(expectedRepaymentSchedule0);
        list.add(expectedRepaymentSchedule1);
        list.add(expectedRepaymentSchedule2);
        list.add(expectedRepaymentSchedule3);
        list.add(expectedRepaymentSchedule4);
        list.add(expectedRepaymentSchedule5);
        list.add(expectedRepaymentSchedule6);
        list.add(expectedRepaymentSchedule7);
        list.add(expectedRepaymentSchedule8);
        list.add(expectedRepaymentSchedule9);
        list.add(expectedRepaymentSchedule10);
        list.add(expectedRepaymentSchedule11);
        list.add(expectedRepaymentSchedule12);
        list.add(expectedRepaymentSchedule13);
        list.add(expectedRepaymentSchedule14);

        for (int i = 0; i < list.size(); i++) {
            assertRepaymentScheduleValuesWithEMI(periods.get(i), list.get(i), i);
        }
    }

    private void assertRepaymentScheduleValuesWithEMI(final GetLoansLoanIdRepaymentPeriod period,
            final LoanDisbursementTestBuilder expectedRepaymentSchedule, final int position) {
        assertEquals(parseExpectedDate(expectedRepaymentSchedule.getDueDate()), period.getDueDate());
        assertEquals(expectedRepaymentSchedule.getPrincipalLoanBalanceOutstanding(), toFloat(period.getPrincipalLoanBalanceOutstanding()));
        assertEquals(period.getTotalOriginalDueForPeriod().floatValue(),
                expectedRepaymentSchedule.getTotalOriginalDueForPeriod().floatValue(), 0.0f);
        assertEquals(period.getTotalOutstandingForPeriod().floatValue(), expectedRepaymentSchedule.getTotalOutstandingForPeriod(), 0.0f);

        if (position != 0 && position != 3) {
            assertEquals(period.getInterestOutstanding().floatValue(), expectedRepaymentSchedule.getInterestOutstanding(), 0.0f);
            assertEquals(period.getPrincipalOutstanding().floatValue(), expectedRepaymentSchedule.getPrincipalOutstanding(), 0.0f);
            assertEquals(period.getPrincipalDue().floatValue(), expectedRepaymentSchedule.getPrincipalDue(), 0.0f);
            assertEquals(period.getPrincipalOriginalDue().floatValue(), expectedRepaymentSchedule.getPrincipalOriginalDue(), 0.0f);
            assertEquals(parseExpectedDate(expectedRepaymentSchedule.getFromDate()), period.getFromDate());
        }
    }

    /**
     * The expected schedule carries dates in the array form the untyped API used to return, e.g. {@code [2015, 6, 1]}.
     */
    private LocalDate parseExpectedDate(final String arrayFormattedDate) {
        if (arrayFormattedDate == null) {
            return null;
        }
        final Matcher parts = EXPECTED_DATE.matcher(arrayFormattedDate);
        if (!parts.matches()) {
            throw new IllegalArgumentException("Not an expected date: " + arrayFormattedDate);
        }
        return LocalDate.of(Integer.parseInt(parts.group(1)), Integer.parseInt(parts.group(2)), Integer.parseInt(parts.group(3)));
    }

    private Float toFloat(final BigDecimal value) {
        return value == null ? null : value.floatValue();
    }

    private PutLoansLoanIdRequest modifyLoanApplicationRequest(final Long clientId, final Long loanProductId, final String principal,
            final String operationDate) {
        return new PutLoansLoanIdRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .principal(Long.parseLong(principal))//
                .loanTermFrequency(5)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(5)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .expectedDisbursementDate(operationDate)//
                .submittedOnDate(operationDate)//
                .loanType("individual")//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    private Long applyForMultiTrancheLoanApplication(final Long clientId, final Long loanProductId, final Double principal,
            final String operationDate) {
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, operationDate, principal, 3)//
                .loanTermFrequency(3)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE);
        return applyForLoan(application);
    }

    private Long applyForProgressiveTrancheLoan(final Long clientId, final Long loanProductId, final Double principal,
            final int numberOfRepayments, final String operationDate, final String interestRate,
            final List<PostLoansDisbursementData> tranches, final Boolean allowFullTermForTranche) {
        final PostLoansRequest application = LoanRequestBuilders
                .applyLoan(clientId, loanProductId, operationDate, principal, numberOfRepayments)//
                .interestRatePerPeriod(new BigDecimal(interestRate))//
                .disbursementData(tranches)//
                .fixedEmiAmount(BigDecimal.valueOf(10000))//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .allowFullTermForTranche(allowFullTermForTranche);
        return applyForLoan(application);
    }

    private Long applyForLoanApplicationWithTranches(final Long clientId, final Long loanProductId, final String principal,
            final List<PostLoansDisbursementData> tranches) {
        return applyForLoan(loanApplicationRequest(clientId, loanProductId, principal, tranches).collateral(collateralOf(clientId)));
    }

    private List<PostLoansRequestCollateralData> collateralOf(final Long clientId) {
        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);
        return List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE));
    }

    private PostLoansRequest loanApplicationRequest(final Long clientId, final Long loanProductId, final String principal,
            final List<PostLoansDisbursementData> tranches) {
        final PostLoansRequest application = LoanRequestBuilders
                .applyLoan(clientId, loanProductId, "01 March 2014", parseDouble(principal), 5)//
                .loanTermFrequency(5)//
                .interestRatePerPeriod(BigDecimal.valueOf(2))//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .expectedDisbursementDate("01 March 2014");
        return tranches.isEmpty() ? application : application.disbursementData(tranches).fixedEmiAmount(BigDecimal.valueOf(10000));
    }

    private Long applyForLoanApplicationWithEmiAmount(final Long clientId, final Long loanProductId, final String principal,
            final List<PostLoansDisbursementData> tranches, final String installmentAmount) {
        final PostLoansRequest application = LoanRequestBuilders
                .applyLoan(clientId, loanProductId, "01 June 2015", parseDouble(principal), 12)//
                .loanTermFrequency(12)//
                .interestRatePerPeriod(BigDecimal.ONE)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .expectedDisbursementDate("01 June 2015")//
                .disbursementData(tranches)//
                .collateral(collateralOf(clientId));
        if (!installmentAmount.isEmpty()) {
            application.fixedEmiAmount(new BigDecimal(installmentAmount));
        }
        return applyForLoan(application);
    }
}
