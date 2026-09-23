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

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper.errorCodesOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.PostWorkingCapitalLoansChargeData;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Charges due at disbursement of a Working Capital loan, modeled on Term Loan: put on the loan by the application or
 * the account endpoint (never inherited from the product catalogue), settled out of the disbursed money in one
 * repayment-at-disbursement transaction, recognized as income immediately, netted from the cash handed to the client
 * while the principal owed stays the full amount, and undone together with the disbursement.
 */
public class FeignWorkingCapitalLoanDisbursementChargesTest extends FeignIntegrationTest {

    private static final String REPAYMENT_AT_DISBURSEMENT_CODE = "loanTransactionType.repaymentAtDisbursement";
    private static final String CHARGES_EXCEED_DISBURSED_AMOUNT = "error.msg.wc.loan.disbursement.charges.exceed.disbursed.amount";
    private static final String DISBURSEMENT_CHARGE_ALREADY_DISBURSED = "error.msg.wc.loan.disbursement.charge.loan.already.disbursed";
    private static final int CALCULATION_PERCENT_OF_AMOUNT = 2;

    private static final String BUSINESS_DATE = "2026-01-01";
    private static final String LOAN_DATE = "01 January 2026";
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);
    private static final BigDecimal PERIOD_PAYMENT_RATE = BigDecimal.valueOf(18);

    private FeignChargesHelper chargesHelper;
    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignJournalEntryHelper journalHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private Account fundSourceAccount;
    private Account loanPortfolioAccount;
    private Account transfersSuspenseAccount;
    private Account incomeFromDiscountFeeAccount;
    private Account feesReceivableAccount;
    private Account penaltiesReceivableAccount;
    private Account incomeFromFeeAccount;
    private Account incomeFromPenaltyAccount;
    private Account incomeFromRecoveryAccount;
    private Account writeOffAccount;
    private Account overpaymentAccount;
    private Account deferredIncomeAccount;
    private Account chargeSpecificIncomeAccount;

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        chargesHelper = new FeignChargesHelper(feignClient);
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        journalHelper = new FeignJournalEntryHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();

        final FeignAccountHelper accountHelper = new FeignAccountHelper(feignClient);
        fundSourceAccount = accountHelper.createLiabilityAccount("wcDisbChgFund");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcDisbChgPort");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcDisbChgSusp");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcDisbChgIncDisc");
        feesReceivableAccount = accountHelper.createAssetAccount("wcDisbChgFeeRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcDisbChgPenRcv");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcDisbChgIncFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcDisbChgIncPen");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcDisbChgIncRec");
        writeOffAccount = accountHelper.createExpenseAccount("wcDisbChgWrtOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcDisbChgOverpay");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcDisbChgDefInc");
        chargeSpecificIncomeAccount = accountHelper.createIncomeAccount("wcDisbChgIncSpec");
    }

    @Test
    void chargesOnTheLoanAreSettledOutOfTheDisbursedMoney() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long flatFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long percentFeeId = createCharge(
                    WorkingCapitalLoanRequestBuilders.disbursementCharge(CALCULATION_PERCENT_OF_AMOUNT, false, 5.0));
            final Long productId = createAccrualProduct(List.of(flatFeeId, percentFeeId), null);
            final Long loanId = submitAndApprove(productId, List.of(flatFeeId, percentFeeId));

            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            final List<WorkingCapitalLoanChargeData> charges = wcLoanHelper.getCharges(loanId);
            assertEquals(2, charges.size(), "exactly the charges the application carried");
            assertChargeSettled(charges, flatFeeId, "100");
            assertChargeSettled(charges, percentFeeId, "450");

            final GetWorkingCapitalLoanTransactionIdResponse settlement = singleSettlement(loanId);
            assertEquals(0, new BigDecimal("550").compareTo(settlement.getTransactionAmount()),
                    "one settlement transaction carries the total of every disbursement charge");

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(0, new BigDecimal("8450").compareTo(loan.getNetDisbursalAmount()), "cash handed to the client = 9000 - 550");
            assertEquals(0, PRINCIPAL.compareTo(loan.getBalance().getPrincipal()), "the principal owed stays the full amount");
            assertEquals(0, BigDecimal.ZERO.compareTo(loan.getBalance().getFeeOutstanding()),
                    "a charge settled at disbursement never becomes an outstanding fee");
            assertEquals(0, new BigDecimal("550").compareTo(loan.getSummary().getFee()), "the summary reports the fees as charged");
            assertEquals(0, new BigDecimal("550").compareTo(loan.getSummary().getFeePaid()), "... and as paid");
            assertEquals(0, BigDecimal.ZERO.compareTo(loan.getSummary().getFeeOutstanding()));

            final List<JournalEntryTransactionItem> entries = journalEntriesOf(settlement.getId());
            assertEquals(3, entries.size(), "one fund-source debit for the total plus one income credit per charge");
            assertJournalEntry(entries, "DEBIT", fundSourceAccount, new BigDecimal("550"));
            assertJournalEntry(entries, "CREDIT", incomeFromFeeAccount, new BigDecimal("100"));
            assertJournalEntry(entries, "CREDIT", incomeFromFeeAccount, new BigDecimal("450"));
        });
    }

    @Test
    void aChargeWithItsOwnIncomeMappingRoutesToThatAccount() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long mappedFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long unmappedFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 30.0));
            final Long productId = createAccrualProduct(List.of(mappedFeeId, unmappedFeeId),
                    Map.of(mappedFeeId, chargeSpecificIncomeAccount.getAccountID().longValue()));
            final Long loanId = submitAndApprove(productId, List.of(mappedFeeId, unmappedFeeId));

            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            final List<JournalEntryTransactionItem> entries = journalEntriesOf(singleSettlement(loanId).getId());
            assertJournalEntry(entries, "DEBIT", fundSourceAccount, new BigDecimal("130"));
            assertJournalEntry(entries, "CREDIT", chargeSpecificIncomeAccount, new BigDecimal("100"));
            assertJournalEntry(entries, "CREDIT", incomeFromFeeAccount, new BigDecimal("30"));
        });
    }

    @Test
    void chargesThatConsumeTheWholeDisbursementAreRejected() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 9000.0));
            final Long productId = createAccrualProduct(List.of(feeId), null);
            final Long loanId = submitAndApprove(productId, List.of(feeId));

            final CallFailedRuntimeException failure = wcLoanHelper.disburseExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            assertEquals(403, failure.getStatus(), "a domain-rule rejection is reported as 403");
            assertEquals(List.of(CHARGES_EXCEED_DISBURSED_AMOUNT), errorCodesOf(failure));
            assertEquals("loanStatusType.approved", wcLoanHelper.getLoanDetails(loanId).getStatus().getCode(),
                    "the rejected disbursement must not have moved the loan");
        });
    }

    @Test
    void aChargeAddedByHandBeforeDisbursementIsSettledWithTheOthers() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long applicationFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long manualFeeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 200.0));
            final Long productId = createAccrualProduct(List.of(applicationFeeId, manualFeeId), null);
            final Long clientId = clientHelper.createClient(LOAN_DATE);
            final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                    .submitApplication(clientId, productId, PRINCIPAL, PERIOD_PAYMENT_RATE, LOAN_DATE, LOAN_DATE)
                    .charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(applicationFeeId))));

            // Pending approval: the charge may be added without a due date and with no amount, taking the definition's.
            wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addChargeWithoutDueDate(manualFeeId, 200.0));
            final List<WorkingCapitalLoanChargeData> before = wcLoanHelper.getCharges(loanId);
            assertEquals(2, before.size());
            before.forEach(charge -> assertEquals(Boolean.FALSE, charge.getPaid(), "before disbursement the charge is not settled yet"));

            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, PRINCIPAL, LOAN_DATE));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            final List<WorkingCapitalLoanChargeData> charges = wcLoanHelper.getCharges(loanId);
            assertEquals(2, charges.size(), "the application charge plus the manual one, nothing else");
            assertChargeSettled(charges, applicationFeeId, "100");
            assertChargeSettled(charges, manualFeeId, "200");
            assertEquals(0, new BigDecimal("300").compareTo(singleSettlement(loanId).getTransactionAmount()));
            assertEquals(0, new BigDecimal("8700").compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()));
        });
    }

    @Test
    void aCataloguedChargeNotOnTheLoanIsNotSettled() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createAccrualProduct(List.of(feeId), null);
            final Long loanId = submitAndApprove(productId, List.of());

            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            // Term-loan model: the catalogue is what the product offers, not what every loan pays.
            assertTrue(wcLoanHelper.getCharges(loanId).isEmpty(), "the catalogue is never inherited at disbursement");
            assertTrue(settlements(loanId).isEmpty());
            assertEquals(0, PRINCIPAL.compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()));
        });
    }

    @Test
    void aDisbursementChargeCannotBeAddedOnceDisbursed() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createAccrualProduct(List.of(), null);
            final Long loanId = submitAndApprove(productId, List.of());
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            final CallFailedRuntimeException failure = wcLoanHelper.addChargeExpectingFailure(loanId,
                    WorkingCapitalLoanRequestBuilders.addChargeWithoutDueDate(feeId, 100.0));

            assertEquals(403, failure.getStatus());
            assertEquals(List.of(DISBURSEMENT_CHARGE_ALREADY_DISBURSED), errorCodesOf(failure));
        });
    }

    @Test
    void aProductWithoutDisbursementChargesDisbursesExactlyAsBefore() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long productId = createAccrualProduct(List.of(), null);
            final Long loanId = submitAndApprove(productId, List.of());

            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            assertTrue(settlements(loanId).isEmpty(), "no settlement transaction when there is nothing to settle");
            assertEquals(0, PRINCIPAL.compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()),
                    "the whole disbursed amount reaches the client");
        });
    }

    @Test
    void theDiscountIsNotSubtractedFromTheNetCash() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createAccrualProduct(List.of(feeId), null);
            final BigDecimal discount = BigDecimal.valueOf(1000);
            final Long clientId = clientHelper.createClient(LOAN_DATE);
            final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders
                    .submitApplicationWithDiscount(clientId, productId, PRINCIPAL, PERIOD_PAYMENT_RATE, LOAN_DATE, LOAN_DATE, discount)
                    .charges(List.of(new PostWorkingCapitalLoansChargeData().chargeId(feeId))));
            wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approveWithDiscount(LOAN_DATE, PRINCIPAL, LOAN_DATE, discount));

            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburseWithDiscount(LOAN_DATE, PRINCIPAL, discount));

            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(0, new BigDecimal("8900").compareTo(loan.getNetDisbursalAmount()),
                    "the discount is financed on top of the principal, so only the charges reduce the cash handed over");
            assertEquals(0, new BigDecimal("10000").compareTo(loan.getBalance().getPrincipal()), "principal = disbursed + discount");
        });
    }

    @Test
    void undoDisbursalBacksOutTheSettlementAndRedisbursingSettlesAgainWithoutDuplicates() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
            final Long productId = createAccrualProduct(List.of(feeId), null);
            final Long loanId = submitAndApprove(productId, List.of(feeId));
            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));
            final Long firstSettlementId = singleSettlement(loanId).getId();

            wcLoanHelper.undoDisbursal(loanId, WorkingCapitalLoanRequestBuilders.undoDisbursal());

            final List<WorkingCapitalLoanChargeData> afterUndo = wcLoanHelper.getCharges(loanId);
            assertEquals(1, afterUndo.size(), "the charge stays on the loan, ready to be settled again");
            assertEquals(Boolean.FALSE, afterUndo.get(0).getPaid());
            assertEquals(0, BigDecimal.ZERO.compareTo(afterUndo.get(0).getAmountPaid()));
            assertEquals(0, PRINCIPAL.compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()),
                    "nothing was handed over any more: back to the schedule's expected net, which carries no charges");
            assertTrue(settlements(loanId).isEmpty(), "the settlement is reversed");
            assertEquals(0, BigDecimal.ZERO.compareTo(wcLoanHelper.getLoanDetails(loanId).getSummary().getFeePaid()),
                    "the summary no longer reports the fee as paid");
            final List<JournalEntryTransactionItem> reversal = journalEntriesOf(firstSettlementId);
            assertEquals(4, reversal.size(), "the original pair plus its mirror");

            wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));

            final List<WorkingCapitalLoanChargeData> afterRedisburse = wcLoanHelper.getCharges(loanId);
            assertEquals(1, afterRedisburse.size(), "re-disbursing settles the same charge again, it does not add one");
            assertChargeSettled(afterRedisburse, feeId, "100");
            final GetWorkingCapitalLoanTransactionIdResponse second = singleSettlement(loanId);
            assertFalse(second.getId().equals(firstSettlementId), "a fresh settlement, the reversed one stays reversed");
            assertEquals(0, new BigDecimal("8900").compareTo(wcLoanHelper.getLoanDetails(loanId).getNetDisbursalAmount()));
        });
    }

    @Test
    void aBackdatedRepaymentReprocessingKeepsTheDisbursementChargeSettled() {
        final Long feeId = createCharge(WorkingCapitalLoanRequestBuilders.disbursementCharge(false, 100.0));
        final Long productId = createAccrualProduct(List.of(feeId), null);
        final Long[] loanIdHolder = new Long[1];
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            loanIdHolder[0] = submitAndApprove(productId, List.of(feeId));
            wcLoanHelper.disburse(loanIdHolder[0], WorkingCapitalLoanRequestBuilders.disburse(LOAN_DATE, PRINCIPAL));
        });
        final Long loanId = loanIdHolder[0];
        businessDateHelper.runAt("2026-01-10", () -> {
            // Dated before the business date: the loan is reprocessed, which resets and replays every repayment.
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(1000), "05 January 2026"));

            final List<WorkingCapitalLoanChargeData> charges = wcLoanHelper.getCharges(loanId);
            assertChargeSettled(charges, feeId, "100");
            final GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
            assertEquals(0, new BigDecimal("100").compareTo(loan.getSummary().getFeePaid()),
                    "reprocessing must put the settlement back, not leave the charge outstanding");
            assertEquals(0, BigDecimal.ZERO.compareTo(loan.getSummary().getFeeOutstanding()));
            assertEquals(0, new BigDecimal("1000").compareTo(loan.getSummary().getPrincipalPaid()),
                    "the repayment went to principal, not to the already-settled charge");
            assertEquals(1, settlements(loanId).size(), "the settlement transaction itself is untouched");
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    private Long createCharge(final org.apache.fineract.client.models.ChargeRequest request) {
        return chargesHelper.createCharge(request).getResourceId();
    }

    private Long createAccrualProduct(final List<Long> chargeIds, final Map<Long, Long> feeToIncomeAccountIdsByChargeId) {
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder()
                .withName("WCL DisbChg " + Utils.uniqueRandomStringGenerator("", 8)).withShortName(Utils.uniqueRandomStringGenerator("", 4))
                .withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)).withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)
                .withFundSourceAccountId(fundSourceAccount.getAccountID().longValue())
                .withLoanPortfolioAccountId(loanPortfolioAccount.getAccountID().longValue())
                .withTransfersInSuspenseAccountId(transfersSuspenseAccount.getAccountID().longValue())
                .withIncomeFromDiscountFeeAccountId(incomeFromDiscountFeeAccount.getAccountID().longValue())
                .withReceivableFeeAccountId(feesReceivableAccount.getAccountID().longValue())
                .withReceivablePenaltyAccountId(penaltiesReceivableAccount.getAccountID().longValue())
                .withIncomeFromFeeAccountId(incomeFromFeeAccount.getAccountID().longValue())
                .withIncomeFromPenaltyAccountId(incomeFromPenaltyAccount.getAccountID().longValue())
                .withIncomeFromRecoveryAccountId(incomeFromRecoveryAccount.getAccountID().longValue())
                .withWriteOffAccountId(writeOffAccount.getAccountID().longValue())
                .withOverpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue()).withChargeIds(chargeIds)
                .withFeeToIncomeAccountIds(feeToIncomeAccountIdsByChargeId).build()).getResourceId();
    }

    /**
     * Submits with the given charges on the application - the only way a charge reaches the loan besides the account
     * endpoint.
     */
    private Long submitAndApprove(final Long productId, final List<Long> chargeIds) {
        final Long clientId = clientHelper.createClient(LOAN_DATE);
        final PostWorkingCapitalLoansRequest request = WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, PRINCIPAL,
                PERIOD_PAYMENT_RATE, LOAN_DATE, LOAN_DATE);
        if (!chargeIds.isEmpty()) {
            request.charges(chargeIds.stream().map(id -> new PostWorkingCapitalLoansChargeData().chargeId(id)).toList());
        }
        final Long loanId = wcLoanHelper.submitApplication(request);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(LOAN_DATE, PRINCIPAL, LOAN_DATE));
        return loanId;
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> settlements(final Long loanId) {
        return wcLoanHelper
                .getTransactions(loanId).stream().filter(txn -> txn.getType() != null
                        && REPAYMENT_AT_DISBURSEMENT_CODE.equals(txn.getType().getCode()) && !Boolean.TRUE.equals(txn.getReversed()))
                .toList();
    }

    private GetWorkingCapitalLoanTransactionIdResponse singleSettlement(final Long loanId) {
        final List<GetWorkingCapitalLoanTransactionIdResponse> settlements = settlements(loanId);
        assertEquals(1, settlements.size(), "exactly one live repayment-at-disbursement transaction");
        return settlements.get(0);
    }

    private static void assertChargeSettled(final List<WorkingCapitalLoanChargeData> charges, final Long chargeId, final String amount) {
        final WorkingCapitalLoanChargeData charge = charges.stream().filter(c -> chargeId.equals(c.getChargeId())).findFirst()
                .orElseThrow(() -> new AssertionError("charge " + chargeId + " not on the account"));
        assertEquals(0, new BigDecimal(amount).compareTo(charge.getAmount()), "amount of charge " + chargeId);
        assertEquals(0, new BigDecimal(amount).compareTo(charge.getAmountPaid()), "paid amount of charge " + chargeId);
        assertEquals(Boolean.TRUE, charge.getPaid(), "charge " + chargeId + " is settled");
        assertNotNull(charge.getDueDate(), "a settled disbursement charge carries the disbursement date");
    }

    private List<JournalEntryTransactionItem> journalEntriesOf(final Long wcTransactionId) {
        final GetJournalEntriesTransactionIdResponse response = journalHelper.getJournalEntriesByTransactionId("WC" + wcTransactionId);
        return response == null || response.getPageItems() == null ? List.of() : new ArrayList<>(response.getPageItems());
    }

    private static void assertJournalEntry(final List<JournalEntryTransactionItem> entries, final String expectedType,
            final Account expectedAccount, final BigDecimal expectedAmount) {
        final boolean found = entries.stream()
                .anyMatch(entry -> expectedType.equals(entry.getEntryType().getValue())
                        && expectedAccount.getAccountID().longValue() == entry.getGlAccountId()
                        && expectedAmount.compareTo(BigDecimal.valueOf(entry.getAmount())) == 0);
        assertTrue(found,
                "Expected journal entry " + expectedType + " account=" + expectedAccount.getAccountID() + " amount=" + expectedAmount
                        + " not found in " + entries.stream()
                                .map(e -> e.getEntryType().getValue() + " acct=" + e.getGlAccountId() + " amt=" + e.getAmount()).toList());
    }
}
