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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.assertEqualBigDecimal;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetDisbursementDetail;
import org.apache.fineract.client.models.GetWorkingCapitalLoanSummary;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Principal / disbursed-total lifecycle for Working Capital loans.
 *
 * <p>
 * The reported worked example is reproduced verbatim so this class doubles as the regression test: submitted 500
 * (discount 100) → approved 450 (discount 45) → disbursed 250 (discount 25) → repayment 100.
 *
 * <p>
 * Field semantics under test (taken from the specification, not from the implementation):
 * <ul>
 * <li>{@code principal} = <em>active contractual principal</em>, moving proposed → approved → actually disbursed.
 * Confluence "Working Capital APIs - Design page (WIP)":474 ("Active principal from loan product related details") and
 * :604; {@code WorkingCapitalLoanApiResourceSwagger.java:210}; classic-loan parity {@code Loan.java:549-557},
 * {@code LoanScheduleAssembler.java:1527}, {@code LoanApplicationWritePlatformServiceJpaRepositoryImpl.java:883},
 * {@code LoanDisbursementService.java:207}, {@code LoanWritePlatformServiceJpaRepositoryImpl.java:3163}.</li>
 * <li>{@code balance.totalDisbursement} / {@code summary.totalDisbursement} = "Total disbursed amount". Confluence APIs
 * design page:520.</li>
 * <li>{@code proposedPrincipal} / {@code approvedPrincipal} values, and the {@code netDisbursalAmount} of the disbursed
 * loan (250), are quoted directly from the reported GET output.</li>
 * <li>Characterization guards (current behaviour, not spec — the report never states them, so they are asserted only to
 * keep the behaviour stable): the {@code netDisbursalAmount} values after approval, after undo-disbursal and on
 * approve-without-amount.</li>
 * <li>An undone disbursal leaves no repayable balance, so it is indistinguishable from a loan that was only ever
 * approved.</li>
 * <li>The expected disbursement amount ({@code disbursementDetails[0].principal}) tracks the active principal, so a
 * pending application edited 500 → 400 offers 400.</li>
 * </ul>
 *
 * <p>
 * Deliberately NOT asserted: {@code approvedPrincipal} semantics before approval. That is an unresolved semantics
 * question and out of scope here, so the current {@code ZERO} before approval is asserted as-is.
 */
public class FeignWorkingCapitalLoanPrincipalLifecycleTest extends FeignIntegrationTest {

    private static final String DISBURSAL_EVENT = "WorkingCapitalLoanDisbursalBusinessEvent";

    /** Reported scenario: "principalAmount": $500 */
    private static final BigDecimal PROPOSED_PRINCIPAL = BigDecimal.valueOf(500);
    /** Reported scenario: "discount": "$100" */
    private static final BigDecimal PROPOSED_DISCOUNT = BigDecimal.valueOf(100);
    /** Reported scenario: "approvedLoanAmount": $450 */
    private static final BigDecimal APPROVED_PRINCIPAL = BigDecimal.valueOf(450);
    /** Reported scenario: approval "discountAmount": $45 */
    private static final BigDecimal APPROVED_DISCOUNT = BigDecimal.valueOf(45);
    /** Reported scenario: "transactionAmount": $250 */
    private static final BigDecimal DISBURSED_PRINCIPAL = BigDecimal.valueOf(250);
    /** Reported scenario: disbursal "discountAmount": $25 */
    private static final BigDecimal DISBURSED_DISCOUNT = BigDecimal.valueOf(25);

    private static final BigDecimal MODIFIED_PRINCIPAL = BigDecimal.valueOf(400);
    private static final BigDecimal REPAYMENT = BigDecimal.valueOf(100);

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    /** Product default; percent, not a fraction — see {@code WorkingCapitalLoanProductTestBuilder:47}. */
    private static final BigDecimal PERIOD_PAYMENT_RATE = BigDecimal.valueOf(18);

    private static final String ISO_DATE = "2026-01-01";
    private static final String DATE = "01 January 2026";

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignExternalEventHelper externalEventHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        externalEventHelper = new FeignExternalEventHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    @Test
    @Order(1)
    void submittedLoanPublishesProposedAsActivePrincipal() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            final GetBalance balance = balanceOf(loan);
            final GetWorkingCapitalLoanSummary summary = summaryOf(loan);

            assertAll("submitted loan principal fields", //
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getProposedPrincipal(), "proposedPrincipal on a submitted loan"),
                    () -> assertEqualBigDecimal(ZERO, loan.getApprovedPrincipal(),
                            "approvedPrincipal before approval (Working Capital keeps ZERO)"),
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getPrincipal(),
                            "active principal on a submitted loan equals the proposed principal"),
                    () -> assertNull(loan.getNetDisbursalAmount(),
                            "netDisbursalAmount is null before the schedule exists (generated at approval)"),
                    () -> assertEqualBigDecimal(ZERO, balance.getPrincipal(), "balance.principal before disbursement"),
                    () -> assertEqualBigDecimal(ZERO, balance.getTotalDisbursement(), "balance.totalDisbursement before disbursement"),
                    () -> assertEqualBigDecimal(ZERO, summary.getPrincipalOutstanding(),
                            "summary.principalOutstanding before disbursement"));
        });
    }

    @Test
    @Order(2)
    void modifyBeforeApprovalMovesProposedAndActivePrincipal() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();

            wcLoanHelper.modifyApplication(loanId, WorkingCapitalLoanRequestBuilders.modifyPrincipal(MODIFIED_PRINCIPAL));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            assertAll("modified application principal fields", //
                    () -> assertEqualBigDecimal(MODIFIED_PRINCIPAL, loan.getProposedPrincipal(), "proposedPrincipal after modify"),
                    () -> assertEqualBigDecimal(MODIFIED_PRINCIPAL, loan.getPrincipal(),
                            "active principal must follow the modified proposed principal — otherwise a loan modified 500 → 400 still publishes 500"),
                    () -> assertEqualBigDecimal(ZERO, loan.getApprovedPrincipal(), "approvedPrincipal stays ZERO after modify"),
                    () -> assertEqualBigDecimal(MODIFIED_PRINCIPAL, expectedDisbursementAmountOf(loan),
                            "the expected disbursement amount must follow the modified principal — otherwise the loan reads 400 everywhere but the disbursement section still offers 500"));
        });
    }

    @Test
    @Order(3)
    void approveAtReducedAmountUpdatesActivePrincipal() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();
            approveReduced(loanId);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            final GetBalance balance = balanceOf(loan);

            assertAll("approved loan principal fields", //
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getProposedPrincipal(), "proposedPrincipal after approval"),
                    () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, loan.getApprovedPrincipal(), "approvedPrincipal after approval"),
                    () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, loan.getPrincipal(),
                            "active principal must become the approved principal (classic parity: LoanScheduleAssembler.java:1527)"),
                    () -> assertEqualBigDecimal(APPROVED_DISCOUNT, loan.getApprovedDiscountFee(), "approved discount fee after approval"),
                    () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, loan.getNetDisbursalAmount(), "netDisbursalAmount after approval"),
                    () -> assertEqualBigDecimal(ZERO, balance.getPrincipal(), "balance.principal is still zero before disbursement"),
                    () -> assertEqualBigDecimal(ZERO, balance.getTotalDisbursement(),
                            "balance.totalDisbursement is still zero before disbursement"));
        });
    }

    @Test
    @Order(4)
    void disburseBelowApprovedUpdatesActivePrincipalAndDisbursedTotal() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();
            approveReduced(loanId);
            disburseReduced(loanId);

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            final GetBalance balance = balanceOf(loan);
            final GetWorkingCapitalLoanSummary summary = summaryOf(loan);

            assertAll("disbursed loan principal fields", //
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getProposedPrincipal(), "proposedPrincipal after disbursement"),
                    () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, loan.getApprovedPrincipal(), "approvedPrincipal after disbursement"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, loan.getPrincipal(),
                            "active principal must become the actually disbursed amount (classic parity: LoanDisbursementService.java:207)"),
                    () -> assertEqualBigDecimal(DISBURSED_DISCOUNT, loan.getDiscountFee(), "effective discount fee after disbursement"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, loan.getNetDisbursalAmount(), "netDisbursalAmount after disbursement"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL.add(DISBURSED_DISCOUNT), balance.getPrincipal(),
                            "balance.principal is total repayable = disbursed + discount"),
                    () -> assertEqualBigDecimal(DISBURSED_DISCOUNT, balance.getTotalDiscountFee(),
                            "balance.totalDiscountFee after disbursement"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, balance.getTotalDisbursement(),
                            "balance.totalDisbursement must carry the disbursed amount, not 0"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, summary.getTotalDisbursement(),
                            "summary.totalDisbursement must carry the disbursed amount, not 0"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL.add(DISBURSED_DISCOUNT), summary.getPrincipalOutstanding(),
                            "summary.principalOutstanding right after disbursement"));
        });
    }

    @Test
    @Order(5)
    void repaymentLeavesActivePrincipalAndDisbursedTotalUntouched() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();
            approveReduced(loanId);
            disburseReduced(loanId);

            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, DATE));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            final GetBalance balance = balanceOf(loan);
            final GetWorkingCapitalLoanSummary summary = summaryOf(loan);

            assertAll("post-repayment principal fields", //
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, loan.getPrincipal(),
                            "active principal is contractual, so a repayment must not move it"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL.add(DISBURSED_DISCOUNT), balance.getPrincipal(),
                            "balance.principal is unchanged by a repayment"),
                    () -> assertEqualBigDecimal(REPAYMENT, balance.getPrincipalPaid(), "balance.principalPaid after a 100 repayment"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, balance.getTotalDisbursement(),
                            "balance.totalDisbursement is unchanged by a repayment"),
                    () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL.add(DISBURSED_DISCOUNT).subtract(REPAYMENT),
                            summary.getPrincipalOutstanding(), "summary.principalOutstanding after a 100 repayment"));
        });
    }

    @Test
    @Order(6)
    void undoDisburseRestoresApprovedPrincipalAndClearsRepayableBalance() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();
            approveReduced(loanId);
            disburseReduced(loanId);

            wcLoanHelper.undoDisbursal(loanId, WorkingCapitalLoanRequestBuilders.undoDisbursal());

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            final GetBalance balance = balanceOf(loan);
            final GetWorkingCapitalLoanSummary summary = summaryOf(loan);

            assertAll("post-undo-disbursal principal fields", //
                    () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, loan.getPrincipal(),
                            "active principal must fall back to the approved principal (classic parity: LoanWritePlatformServiceJpaRepositoryImpl.java:3163)"),
                    () -> assertNull(loan.getDiscountFee(), "effective discount fee is cleared on undo-disbursal"),
                    () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, loan.getApprovedPrincipal(),
                            "approvedPrincipal survives undo-disbursal"),
                    () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, loan.getNetDisbursalAmount(),
                            "netDisbursalAmount returns to the approved principal"),
                    () -> assertEqualBigDecimal(ZERO, balance.getTotalDisbursement(),
                            "balance.totalDisbursement must be backed out on undo-disbursal"),
                    () -> assertEqualBigDecimal(ZERO, balance.getPrincipalPaid(), "balance.principalPaid is reset on undo-disbursal"),
                    () -> assertEqualBigDecimal(ZERO, balance.getPrincipal(),
                            "nothing is paid out after an undo-disbursal, so there is nothing repayable"),
                    () -> assertEqualBigDecimal(ZERO, balance.getPrincipalOutstanding(),
                            "balance.principalOutstanding is cleared with the repayable principal"),
                    () -> assertEqualBigDecimal(ZERO, balance.getTotalOutstanding(), "balance.totalOutstanding after undo-disbursal"),
                    () -> assertEqualBigDecimal(ZERO, balance.getTotalDiscountFee(),
                            "the discount fee is charged at disbursement, so it is backed out with it"),
                    () -> assertEqualBigDecimal(ZERO, balance.getUnrealizedIncomeFromDiscountFee(),
                            "no discount fee left means no unrealized income from it"),
                    () -> assertEqualBigDecimal(ZERO, summary.getPrincipal(), "summary.principal after undo-disbursal"),
                    () -> assertEqualBigDecimal(ZERO, summary.getPrincipalOutstanding(),
                            "summary.principalOutstanding after undo-disbursal"),
                    () -> assertEqualBigDecimal(ZERO, summary.getTotalDisbursement(), "summary.totalDisbursement after undo-disbursal"));
        });
    }

    @Test
    @Order(7)
    void undoApproveRestoresProposedPrincipal() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();
            approveReduced(loanId);

            wcLoanHelper.undoApproval(loanId, WorkingCapitalLoanRequestBuilders.emptyCommand());

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            assertAll("post-undo-approval principal fields", //
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getPrincipal(),
                            "active principal must fall back to the proposed principal (classic parity: LoanApplicationWritePlatformServiceJpaRepositoryImpl.java:883)"),
                    () -> assertEqualBigDecimal(ZERO, loan.getApprovedPrincipal(), "approvedPrincipal is reset to ZERO on undo-approval"),
                    () -> assertNull(loan.getApprovedDiscountFee(), "approved discount fee is cleared on undo-approval"),
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getProposedPrincipal(),
                            "proposedPrincipal survives undo-approval"),
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, expectedDisbursementAmountOf(loan),
                            "the expected disbursement amount follows the active principal back to the proposed one"));
        });
    }

    @Test
    @Order(8)
    void approveWithoutAmountLeavesActivePrincipalAtProposed() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long loanId = submitLoan();

            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approveWithDiscount(DATE, null, DATE, PROPOSED_DISCOUNT));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);

            assertAll("approve-without-amount principal fields", //
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getApprovedPrincipal(),
                            "approvedPrincipal defaults to the proposed principal when no amount is sent"),
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getPrincipal(), "active principal equals the proposed principal"),
                    () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, loan.getNetDisbursalAmount(),
                            "netDisbursalAmount equals the approved principal"));
        });
    }

    @Test
    @Order(9)
    void disbursalBusinessEventCarriesActivePrincipalAndDisbursedTotal() {
        externalEventHelper.enableBusinessEvent(DISBURSAL_EVENT);
        try {
            businessDateHelper.runAt(ISO_DATE, () -> {
                final Long loanId = submitLoan();
                approveReduced(loanId);
                externalEventHelper.deleteAllExternalEvents();
                disburseReduced(loanId);

                final List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(DISBURSAL_EVENT);
                final ExternalEventResponse event = events.stream().filter(e -> loanId.equals(toLong(e.getPayLoad().get("id")))).findFirst()
                        .orElse(null);
                assertNotNull(event, "A " + DISBURSAL_EVENT + " must be recorded for loan " + loanId);

                final Map<String, Object> payload = event.getPayLoad();
                @SuppressWarnings("unchecked")
                final Map<String, Object> summary = (Map<String, Object>) payload.get("summary");
                assertNotNull(summary, "event payload must carry a summary");

                assertAll("disbursal event principal fields", //
                        () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, toBigDecimal(payload.get("principal")),
                                "event principal must be the actually disbursed amount"),
                        () -> assertEqualBigDecimal(APPROVED_PRINCIPAL, toBigDecimal(payload.get("approvedPrincipal")),
                                "event approvedPrincipal"),
                        () -> assertEqualBigDecimal(PROPOSED_PRINCIPAL, toBigDecimal(payload.get("proposedPrincipal")),
                                "event proposedPrincipal"),
                        () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL, toBigDecimal(summary.get("principalDisbursed")),
                                "event summary.principalDisbursed is sourced from balance.totalDisbursement"),
                        () -> assertEqualBigDecimal(DISBURSED_PRINCIPAL.add(DISBURSED_DISCOUNT),
                                toBigDecimal(summary.get("totalPrincipal")),
                                "event summary.totalPrincipal is sourced from balance.principal"));
            });
        } finally {
            externalEventHelper.disableBusinessEvent(DISBURSAL_EVENT);
        }
    }

    /**
     * Two loans in the same state - approved for 450, nothing paid out - must publish the same balances whether that
     * state was reached by approval alone or by disbursing and then undoing it.
     */
    @Test
    @Order(10)
    void undoneDisbursalMatchesNeverDisbursedLoan() {
        businessDateHelper.runAt(ISO_DATE, () -> {
            final Long neverDisbursedId = submitLoan();
            approveReduced(neverDisbursedId);

            final Long undoneId = submitLoan();
            approveReduced(undoneId);
            disburseReduced(undoneId);
            wcLoanHelper.undoDisbursal(undoneId, WorkingCapitalLoanRequestBuilders.undoDisbursal());

            final GetWorkingCapitalLoansLoanIdResponse neverDisbursed = wcLoanHelper.getLoanDetails(neverDisbursedId);
            final GetWorkingCapitalLoansLoanIdResponse undone = wcLoanHelper.getLoanDetails(undoneId);

            final GetBalance neverDisbursedBalance = balanceOf(neverDisbursed);
            final GetBalance undoneBalance = balanceOf(undone);

            assertAll("the two routes into 'approved, nothing paid out' must agree", //
                    () -> assertEqualBigDecimal(neverDisbursed.getPrincipal(), undone.getPrincipal(), "active principal"),
                    () -> assertEqualBigDecimal(neverDisbursedBalance.getPrincipal(), undoneBalance.getPrincipal(), "balance.principal"),
                    () -> assertEqualBigDecimal(neverDisbursedBalance.getPrincipalOutstanding(), undoneBalance.getPrincipalOutstanding(),
                            "balance.principalOutstanding"),
                    () -> assertEqualBigDecimal(neverDisbursedBalance.getTotalOutstanding(), undoneBalance.getTotalOutstanding(),
                            "balance.totalOutstanding"),
                    () -> assertEqualBigDecimal(neverDisbursedBalance.getTotalExpectedRepayment(),
                            undoneBalance.getTotalExpectedRepayment(), "balance.totalExpectedRepayment"),
                    () -> assertEqualBigDecimal(neverDisbursedBalance.getTotalDisbursement(), undoneBalance.getTotalDisbursement(),
                            "balance.totalDisbursement"),
                    () -> assertEqualBigDecimal(neverDisbursedBalance.getTotalDiscountFee(), undoneBalance.getTotalDiscountFee(),
                            "balance.totalDiscountFee"),
                    () -> assertEqualBigDecimal(neverDisbursedBalance.getUnrealizedIncomeFromDiscountFee(),
                            undoneBalance.getUnrealizedIncomeFromDiscountFee(), "balance.unrealizedIncomeFromDiscountFee"),
                    () -> assertEqualBigDecimal(summaryOf(neverDisbursed).getPrincipalOutstanding(),
                            summaryOf(undone).getPrincipalOutstanding(), "summary.principalOutstanding"));
        });
    }

    // -----------------------------------------------------------------------
    // Setup helpers
    // -----------------------------------------------------------------------

    /**
     * A product whose {@code discountDefault} is overridable, so the loan-level discount ladder (100 → 45 → 25) is
     * accepted at submit, approve and disburse.
     */
    private Long createProduct() {
        final String uniqueName = "WCL Principal " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
        return productHelper
                .createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                        .withShortName(uniqueShortName).withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).build())
                .getResourceId();
    }

    private Long submitLoan() {
        final Long clientId = clientHelper.createClient(DATE);
        final Long productId = createProduct();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplicationWithDiscount(clientId,
                productId, PROPOSED_PRINCIPAL, PERIOD_PAYMENT_RATE, DATE, DATE, PROPOSED_DISCOUNT));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private void approveReduced(final Long loanId) {
        wcLoanHelper.approve(loanId,
                WorkingCapitalLoanRequestBuilders.approveWithDiscount(DATE, APPROVED_PRINCIPAL, DATE, APPROVED_DISCOUNT));
    }

    private void disburseReduced(final Long loanId) {
        wcLoanHelper.disburse(loanId,
                WorkingCapitalLoanRequestBuilders.disburseWithDiscount(DATE, DISBURSED_PRINCIPAL, DISBURSED_DISCOUNT));
    }

    private static GetBalance balanceOf(final GetWorkingCapitalLoansLoanIdResponse loan) {
        final GetBalance balance = loan.getBalance();
        assertNotNull(balance, "loan.balance must be present");
        return balance;
    }

    /** {@code disbursementDetails[0].principal} is the expected disbursement amount of the single supported tranche. */
    private static BigDecimal expectedDisbursementAmountOf(final GetWorkingCapitalLoansLoanIdResponse loan) {
        final List<GetDisbursementDetail> details = loan.getDisbursementDetails();
        assertNotNull(details, "loan.disbursementDetails must be present");
        assertFalse(details.isEmpty(), "loan.disbursementDetails must carry the expected tranche");
        return details.getFirst().getPrincipal();
    }

    private static GetWorkingCapitalLoanSummary summaryOf(final GetWorkingCapitalLoansLoanIdResponse loan) {
        final GetWorkingCapitalLoanSummary summary = loan.getSummary();
        assertNotNull(summary, "loan.summary must be present");
        return summary;
    }

    private static Long toLong(final Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    /** Avro {@code bigdecimal} fields surface as either a JSON number or a string, depending on the codec. */
    private static BigDecimal toBigDecimal(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return new BigDecimal(value.toString());
    }
}
