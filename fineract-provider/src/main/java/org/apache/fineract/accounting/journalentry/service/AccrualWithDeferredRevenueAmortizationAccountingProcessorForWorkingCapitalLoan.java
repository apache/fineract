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
package org.apache.fineract.accounting.journalentry.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.PortfolioProductType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.apache.fineract.portfolio.workingcapitalloan.accounting.WorkingCapitalLoanAccountingProcessor;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransactionAllocation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccrualWithDeferredRevenueAmortizationAccountingProcessorForWorkingCapitalLoan
        implements WorkingCapitalLoanAccountingProcessor {

    private static final int WORKING_CAPITAL_LOAN_ENTITY_TYPE = PortfolioProductType.WORKING_CAPITAL_LOAN.getValue();

    private final AccountingProcessorHelper helper;
    private final JournalEntryRepository journalEntryRepository;

    @Override
    public void postJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final WorkingCapitalLoanTransactionAllocation allocation, final boolean isChargedOff) {
        final Office office = loan.getClient().getOffice();
        helper.checkForBranchClosures(helper.getLatestClosureByBranch(office.getId()), txn.getTransactionDate());

        final JournalEntryPostingHelper accountPostHelper = new JournalEntryPostingHelper(loan, txn);
        plannedPostings(loan, txn, allocation, isChargedOff).forEach(accountPostHelper::post);
    }

    /**
     * One entry a transaction's split books: which account, which side, how much.
     *
     * @param withPaymentDetail
     *            whether the entry carries the transaction's payment detail. Repayment credit legs are booked without
     *            it and every other leg with it, mirroring what the per-type posting methods did before they were
     *            folded into {@link #plannedPostings}.
     * @param resolvedAccount
     *            an already-resolved GL account for legs whose destination is not a product account-type mapping (the
     *            write-off reason mapped expense). When present it wins over {@code account}, both when posting and in
     *            the restatement guard, so the guard keeps describing the ledger the poster produced.
     */
    private record LedgerPosting(CashAccountsForLoan account, GLAccount resolvedAccount, boolean debit, BigDecimal amount,
            boolean withPaymentDetail) {

        static LedgerPosting debit(final CashAccountsForLoan account, final BigDecimal amount) {
            return new LedgerPosting(account, null, true, MathUtil.nullToZero(amount), true);
        }

        static LedgerPosting debit(final GLAccount account, final BigDecimal amount) {
            return new LedgerPosting(null, account, true, MathUtil.nullToZero(amount), true);
        }

        static LedgerPosting credit(final CashAccountsForLoan account, final BigDecimal amount) {
            return new LedgerPosting(account, null, false, MathUtil.nullToZero(amount), true);
        }

        static LedgerPosting creditWithoutPaymentDetail(final CashAccountsForLoan account, final BigDecimal amount) {
            return new LedgerPosting(account, null, false, MathUtil.nullToZero(amount), false);
        }
    }

    /** One ledger position: what a single GL account holds on a single side. */
    private record PostingKey(Long glAccountId, boolean debit) {
    }

    /**
     * Every entry a transaction's split books, for its type and charged-off state.
     * <p>
     * This is the single source of truth for that matrix: {@link #postJournalEntries} books the plan and
     * {@link #splitDiffersFromLedger} compares it against the ledger. Deriving both from here is what keeps the
     * restatement guard exact for every transaction type - a guard that restated the account mapping itself would stop
     * describing the ledger, silently, the moment a posting rule changed.
     * </p>
     */
    private List<LedgerPosting> plannedPostings(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final WorkingCapitalLoanTransactionAllocation allocation, final boolean isChargedOff) {
        final BigDecimal principalPortion = MathUtil.nullToZero(allocation == null ? null : allocation.getPrincipalPortion());
        final BigDecimal feesPortion = MathUtil.nullToZero(allocation == null ? null : allocation.getFeeChargesPortion());
        final BigDecimal penaltiesPortion = MathUtil.nullToZero(allocation == null ? null : allocation.getPenaltyChargesPortion());
        final BigDecimal overpaymentPortion = MathUtil.nullToZero(allocation == null ? null : allocation.getOverpaymentPortion());

        return switch (txn.getTypeOf()) {
            case LoanTransactionType.REPAYMENT ->
                repaymentPostings(txn, principalPortion, feesPortion, penaltiesPortion, overpaymentPortion, isChargedOff);
            case LoanTransactionType.GOODWILL_CREDIT ->
                goodwillCreditPostings(principalPortion, feesPortion, penaltiesPortion, overpaymentPortion, isChargedOff);
            case LoanTransactionType.PAYOUT_REFUND ->
                payoutRefundPostings(loan, txn, principalPortion, feesPortion, penaltiesPortion, overpaymentPortion, isChargedOff);
            case LoanTransactionType.CREDIT_BALANCE_REFUND -> creditBalanceRefundPostings(txn, principalPortion, overpaymentPortion);
            case LoanTransactionType.CHARGE_ADJUSTMENT ->
                chargeAdjustmentPostings(txn, principalPortion, feesPortion, penaltiesPortion, overpaymentPortion, isChargedOff);
            case LoanTransactionType.ACCRUAL -> chargeAccrualPostings(feesPortion, penaltiesPortion);
            case LoanTransactionType.CHARGE_OFF -> chargeOffPostings(loan, principalPortion, feesPortion, penaltiesPortion);
            case LoanTransactionType.WRITEOFF -> writeOffPostings(loan, principalPortion, feesPortion, penaltiesPortion, isChargedOff);
            default -> throw new NotImplementedException(
                    "Post Journal Entries is not implemented yet for " + txn.getTypeOf().getCode() + " for Working Capital Loan");
        };
    }

    /**
     * The account a charged-off principal loss is expensed to: the dedicated fraud expense account when the loan is
     * flagged as fraudulent, the regular charge-off expense otherwise.
     */
    private CashAccountsForLoan chargeOffExpenseAccount(final WorkingCapitalLoan loan) {
        return loan.isFraud() ? CashAccountsForLoan.CHARGE_OFF_FRAUD_EXPENSE : CashAccountsForLoan.CHARGE_OFF_EXPENSE;
    }

    private List<LedgerPosting> repaymentPostings(final WorkingCapitalLoanTransaction txn, final BigDecimal principalPortion,
            final BigDecimal feesPortion, final BigDecimal penaltiesPortion, final BigDecimal overpaymentPortion,
            final boolean isChargedOff) {
        // After charge-off the portfolio and receivables are already written off, so the credits recognize recovery
        // income instead of reducing them.
        final CashAccountsForLoan principalAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.LOAN_PORTFOLIO;
        final CashAccountsForLoan feesAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.FEES_RECEIVABLE;
        final CashAccountsForLoan penaltiesAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.PENALTIES_RECEIVABLE;

        return List.of(LedgerPosting.creditWithoutPaymentDetail(principalAccount, principalPortion),
                LedgerPosting.creditWithoutPaymentDetail(feesAccount, feesPortion),
                LedgerPosting.creditWithoutPaymentDetail(penaltiesAccount, penaltiesPortion),
                LedgerPosting.creditWithoutPaymentDetail(CashAccountsForLoan.OVERPAYMENT, overpaymentPortion),
                LedgerPosting.debit(CashAccountsForLoan.FUND_SOURCE, txn.getTransactionAmount()));
    }

    private List<LedgerPosting> chargeAdjustmentPostings(final WorkingCapitalLoanTransaction txn, final BigDecimal principalPortion,
            final BigDecimal feesPortion, final BigDecimal penaltiesPortion, final BigDecimal overpaymentPortion,
            final boolean isChargedOff) {
        final boolean penaltyCharge = isAdjustedChargeAPenalty(txn);

        // Debit always against the adjusted charge's own income account (fee or penalty), including after charge-off.
        final CashAccountsForLoan debitAccount = penaltyCharge ? CashAccountsForLoan.INCOME_FROM_PENALTIES
                : CashAccountsForLoan.INCOME_FROM_FEES;
        // Receivables were already written off at charge-off; credits go to charge-off fee/penalty income.
        final CashAccountsForLoan chargedOffCreditAccount = penaltyCharge ? CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY
                : CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES;
        final CashAccountsForLoan principalAccount = isChargedOff ? chargedOffCreditAccount : CashAccountsForLoan.LOAN_PORTFOLIO;
        final CashAccountsForLoan feesAccount = isChargedOff ? chargedOffCreditAccount : CashAccountsForLoan.FEES_RECEIVABLE;
        final CashAccountsForLoan penaltiesAccount = isChargedOff ? chargedOffCreditAccount : CashAccountsForLoan.PENALTIES_RECEIVABLE;

        return List.of(LedgerPosting.debit(debitAccount, txn.getTransactionAmount()),
                LedgerPosting.credit(principalAccount, principalPortion), LedgerPosting.credit(feesAccount, feesPortion),
                LedgerPosting.credit(penaltiesAccount, penaltiesPortion),
                LedgerPosting.credit(CashAccountsForLoan.OVERPAYMENT, overpaymentPortion));
    }

    /**
     * Charge-off is a pure accounting tag with no portfolio impact. It writes off the outstanding receivables against
     * the charge-off expense (principal) and reverses the accrued fee/penalty income. There is no interest leg -- WC
     * has no interest concept. Principal expense is routed to the fraud expense account when the loan is marked fraud.
     */
    private List<LedgerPosting> chargeOffPostings(final WorkingCapitalLoan loan, final BigDecimal principalPortion,
            final BigDecimal feesPortion, final BigDecimal penaltiesPortion) {
        return List.of(
                // debit: recognize the loss on principal, reverse accrued fee/penalty income
                LedgerPosting.debit(chargeOffExpenseAccount(loan), principalPortion),
                LedgerPosting.debit(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES, feesPortion),
                LedgerPosting.debit(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY, penaltiesPortion),
                // credit: write off the outstanding receivables
                LedgerPosting.credit(CashAccountsForLoan.LOAN_PORTFOLIO, principalPortion),
                LedgerPosting.credit(CashAccountsForLoan.FEES_RECEIVABLE, feesPortion),
                LedgerPosting.credit(CashAccountsForLoan.PENALTIES_RECEIVABLE, penaltiesPortion));
    }

    /**
     * Terminal write-off: debit the write-off loss account for the whole outstanding, credit the portfolio and
     * receivable accounts per portion (mirrors the accrual term-loan treatment, without the interest leg WC does not
     * have). The loss debits the expense account the product maps to the loan's write-off reason when such a mapping
     * exists, the generic Losses Written-off account otherwise -- see {@link #writeOffLossDebit}.
     * <p>
     * A loan that was already charged off reclassifies instead: the receivables are already off the books, so the
     * credits go against the accounts {@link #chargeOffPostings} debited -- the charge-off expense (or fraud expense)
     * for principal and the charged-off fee/penalty income accounts -- moving the loss to Losses Written-off. The net
     * P&amp;L effect is zero; only its presentation changes. Crediting the asset accounts again here would drive them
     * negative and recognize the loss twice.
     * </p>
     */
    private List<LedgerPosting> writeOffPostings(final WorkingCapitalLoan loan, final BigDecimal principalPortion,
            final BigDecimal feesPortion, final BigDecimal penaltiesPortion, final boolean isChargedOff) {
        if (isChargedOff) {
            return List.of(
                    LedgerPosting.debit(CashAccountsForLoan.LOSSES_WRITTEN_OFF,
                            MathUtil.add(principalPortion, feesPortion, penaltiesPortion)),
                    LedgerPosting.credit(chargeOffExpenseAccount(loan), principalPortion),
                    LedgerPosting.credit(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES, feesPortion),
                    LedgerPosting.credit(CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY, penaltiesPortion));
        }

        return List.of(writeOffLossDebit(loan, MathUtil.add(principalPortion, feesPortion, penaltiesPortion)),
                LedgerPosting.credit(CashAccountsForLoan.LOAN_PORTFOLIO, principalPortion),
                LedgerPosting.credit(CashAccountsForLoan.FEES_RECEIVABLE, feesPortion),
                LedgerPosting.credit(CashAccountsForLoan.PENALTIES_RECEIVABLE, penaltiesPortion));
    }

    /**
     * The debit leg a write-off expenses the loss to: the expense account the product maps to the loan's write-off
     * reason, falling back to the generic Losses Written-off account when the write-off carries no reason or the
     * product does not map it.
     */
    private LedgerPosting writeOffLossDebit(final WorkingCapitalLoan loan, final BigDecimal amount) {
        final CodeValue writeOffReason = loan.getWriteOffReason();
        if (writeOffReason != null) {
            final ProductToGLAccountMapping mapping = helper.getWriteOffMappingByCodeValue(loan.getLoanProduct().getId(),
                    PortfolioProductType.WORKING_CAPITAL_LOAN, writeOffReason.getId());
            if (mapping != null) {
                return LedgerPosting.debit(mapping.getGlAccount(), amount);
            }
        }
        return LedgerPosting.debit(CashAccountsForLoan.LOSSES_WRITTEN_OFF, amount);
    }

    private boolean isAdjustedChargeAPenalty(final WorkingCapitalLoanTransaction txn) {
        return txn.getLoanTransactionRelations().stream()
                .filter(relation -> relation.getToCharge() != null
                        && relation.getRelationType() == LoanTransactionRelationTypeEnum.CHARGE_ADJUSTMENT)
                .findFirst().map(relation -> relation.getToCharge().isPenaltyCharge()).orElseThrow(() -> new IllegalStateException(
                        "Charge adjustment transaction " + txn.getId() + " is missing its link to the adjusted charge"));
    }

    /**
     * On a charged-off loan a payout refund's credits reverse what the charge-off recognized - the charge-off expense
     * for principal and the charged-off fee/penalty income - instead of reducing the portfolio and receivables, which
     * are already written off.
     */
    private List<LedgerPosting> payoutRefundPostings(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final BigDecimal principalPortion, final BigDecimal feesPortion, final BigDecimal penaltiesPortion,
            final BigDecimal overpaymentPortion, final boolean isChargedOff) {
        final CashAccountsForLoan principalAccount = isChargedOff ? chargeOffExpenseAccount(loan) : CashAccountsForLoan.LOAN_PORTFOLIO;
        final CashAccountsForLoan feesAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_FEES
                : CashAccountsForLoan.FEES_RECEIVABLE;
        final CashAccountsForLoan penaltiesAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_CHARGE_OFF_PENALTY
                : CashAccountsForLoan.PENALTIES_RECEIVABLE;

        return List.of(LedgerPosting.debit(CashAccountsForLoan.FUND_SOURCE, txn.getTransactionAmount()),
                LedgerPosting.credit(principalAccount, principalPortion), LedgerPosting.credit(feesAccount, feesPortion),
                LedgerPosting.credit(penaltiesAccount, penaltiesPortion),
                LedgerPosting.credit(CashAccountsForLoan.OVERPAYMENT, overpaymentPortion));
    }

    private List<LedgerPosting> chargeAccrualPostings(final BigDecimal feesPortion, final BigDecimal penaltiesPortion) {
        return List.of(
                // Debit receivable when the charge becomes accrued.
                LedgerPosting.debit(CashAccountsForLoan.FEES_RECEIVABLE, feesPortion),
                LedgerPosting.debit(CashAccountsForLoan.PENALTIES_RECEIVABLE, penaltiesPortion),
                // Credit the corresponding income account.
                LedgerPosting.credit(CashAccountsForLoan.INCOME_FROM_FEES, feesPortion),
                LedgerPosting.credit(CashAccountsForLoan.INCOME_FROM_PENALTIES, penaltiesPortion));
    }

    /**
     * On a charged-off loan a goodwill credit's portfolio/receivable credits become recovery income; the debit side -
     * the goodwill expense and income accounts - is the same either way.
     */
    private List<LedgerPosting> goodwillCreditPostings(final BigDecimal principalPortion, final BigDecimal feesPortion,
            final BigDecimal penaltiesPortion, final BigDecimal overpaymentPortion, final boolean isChargedOff) {
        final CashAccountsForLoan principalAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.LOAN_PORTFOLIO;
        final CashAccountsForLoan feesAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.FEES_RECEIVABLE;
        final CashAccountsForLoan penaltiesAccount = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.PENALTIES_RECEIVABLE;

        // plain add, not MathUtil.add: the portions are already non-null here, and MathUtil consults the tenant's
        // rounding mode, which this leg has no reason to depend on
        return List.of(LedgerPosting.debit(CashAccountsForLoan.GOODWILL_CREDIT, principalPortion.add(overpaymentPortion)),
                LedgerPosting.debit(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES, feesPortion),
                LedgerPosting.debit(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY, penaltiesPortion),
                LedgerPosting.credit(principalAccount, principalPortion), LedgerPosting.credit(feesAccount, feesPortion),
                LedgerPosting.credit(penaltiesAccount, penaltiesPortion),
                LedgerPosting.credit(CashAccountsForLoan.OVERPAYMENT, overpaymentPortion));
    }

    /**
     * Books a credit balance refund from its allocation's split: the overpayment portion gave back overpayment, and any
     * principal portion (the over-refund excess a reprocess derived) lent out fresh principal. At booking a refund is
     * always fully funded by the overpayment, so this reduces to the plain overpayment-debit / fund-source-credit pair.
     */
    private List<LedgerPosting> creditBalanceRefundPostings(final WorkingCapitalLoanTransaction txn, final BigDecimal principalPortion,
            final BigDecimal overpaymentPortion) {
        return List.of(LedgerPosting.debit(CashAccountsForLoan.OVERPAYMENT, overpaymentPortion),
                LedgerPosting.debit(CashAccountsForLoan.LOAN_PORTFOLIO, principalPortion),
                LedgerPosting.credit(CashAccountsForLoan.FUND_SOURCE, txn.getTransactionAmount()));
    }

    @Override
    public void restateJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final WorkingCapitalLoanTransactionAllocation allocation, final boolean isChargedOff) {
        final List<JournalEntry> effectiveEntries = effectiveJournalEntries(txn);
        if (!splitDiffersFromLedger(loan, txn, allocation, effectiveEntries, isChargedOff)) {
            // The ledger already reflects the recomputed split; re-posting would only add cancelling noise.
            return;
        }
        reverseExistingEntries(loan, txn, true);
        postJournalEntries(loan, txn, allocation, isChargedOff);
    }

    /**
     * The entries that currently make up a transaction's posting: {@code findJournalEntries} returns only the ones not
     * flagged reversed. A restatement supersedes its predecessors (see {@link #reverseExistingEntries}), so what
     * remains is exactly the live split.
     */
    private List<JournalEntry> effectiveJournalEntries(final WorkingCapitalLoanTransaction txn) {
        final String transactionId = AccountingProcessorHelper.WORKING_CAPITAL_LOAN_TRANSACTION_IDENTIFIER + txn.getId();
        return journalEntryRepository.findJournalEntries(transactionId, WORKING_CAPITAL_LOAN_ENTITY_TYPE);
    }

    private JournalEntry createMirrorEntry(final JournalEntry journalEntry, final String transactionId, final LocalDate transactionDate) {
        final JournalEntryType reversalType = journalEntry.isDebitEntry() ? JournalEntryType.CREDIT : JournalEntryType.DEBIT;
        return JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(), journalEntry.getGlAccount(),
                journalEntry.getCurrencyCode(), transactionId, Boolean.FALSE, transactionDate, reversalType, journalEntry.getAmount(),
                journalEntry.getDescription(), journalEntry.getEntityType(), journalEntry.getEntityId(), journalEntry.getReferenceNumber(),
                journalEntry.getLoanTransactionId(), journalEntry.getSavingsTransactionId(), journalEntry.getClientTransactionId(),
                journalEntry.getShareTransactionId());
    }

    /**
     * Whether re-booking the transaction now would land differently on the ledger than what is currently live: the
     * planned postings are aggregated per (GL account, side) and compared against the live entries aggregated the same
     * way. Because both sides come from {@link #plannedPostings}, this holds for every transaction type and for both
     * charged-off states - including a pure routing change, where the amounts are unchanged but move to different
     * accounts, which shows up as a key mismatch.
     */
    private boolean splitDiffersFromLedger(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final WorkingCapitalLoanTransactionAllocation allocation, final List<JournalEntry> effectiveEntries,
            final boolean isChargedOff) {
        if (effectiveEntries.isEmpty()) {
            return true;
        }
        final Map<PostingKey, BigDecimal> planned = plannedAmountsByPosition(loan, txn, allocation, isChargedOff);
        final Map<PostingKey, BigDecimal> posted = postedAmountsByPosition(effectiveEntries);

        if (!planned.keySet().equals(posted.keySet())) {
            return true;
        }
        // compareTo, not equals: BigDecimal equality is scale-sensitive, and 100 vs 100.00 is the same posting.
        return planned.entrySet().stream().anyMatch(entry -> entry.getValue().compareTo(posted.get(entry.getKey())) != 0);
    }

    private Map<PostingKey, BigDecimal> plannedAmountsByPosition(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final WorkingCapitalLoanTransactionAllocation allocation, final boolean isChargedOff) {
        final Long productId = loan.getLoanProduct().getId();
        // Resolving every account with the payment type matches the poster: the mapping is payment-type specific only
        // for FUND_SOURCE, and every FUND_SOURCE leg the poster books passes the payment type.
        final Long paymentTypeId = extractPaymentTypeId(txn);

        final Map<PostingKey, BigDecimal> amounts = new HashMap<>();
        for (final LedgerPosting posting : plannedPostings(loan, txn, allocation, isChargedOff)) {
            if (!MathUtil.isGreaterThanZero(posting.amount())) {
                continue; // a zero leg is never booked, so it must not be expected either
            }
            final GLAccount account = posting.resolvedAccount() != null ? posting.resolvedAccount()
                    : helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, posting.account().getValue(), paymentTypeId);
            amounts.merge(new PostingKey(account.getId(), posting.debit()), posting.amount(), BigDecimal::add);
        }
        return amounts;
    }

    private Map<PostingKey, BigDecimal> postedAmountsByPosition(final List<JournalEntry> entries) {
        final Map<PostingKey, BigDecimal> amounts = new HashMap<>();
        for (final JournalEntry entry : entries) {
            if (entry.getGlAccount() == null) {
                continue;
            }
            amounts.merge(new PostingKey(entry.getGlAccount().getId(), entry.isDebitEntry()), entry.getAmount(), BigDecimal::add);
        }
        return amounts;
    }

    @Override
    public void postReversalJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn) {
        reverseExistingEntries(loan, txn, false);
    }

    /**
     * Cancels a transaction's live entries by posting an offsetting mirror for each, keeping the ledger append-only
     * (nothing is deleted). The originals are always flagged reversed.
     *
     * @param supersede
     *            whether the mirrors are flagged reversed as well. A restatement must pass {@code true}: the cancelled
     *            pair then drops out of the live set, so the next reversal cannot mirror these mirrors again - which
     *            would compound and leave the original booking amounts standing on the ledger. An undo passes
     *            {@code false}, keeping its mirrors live as the visible reversal.
     */
    private void reverseExistingEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn, final boolean supersede) {
        final Office office = loan.getClient().getOffice();
        final LocalDate transactionDate = txn.getReversedOnDate() != null ? txn.getReversedOnDate() : DateUtils.getBusinessLocalDate();

        helper.checkForBranchClosures(helper.getLatestClosureByBranch(office.getId()), transactionDate);

        final String transactionId = AccountingProcessorHelper.WORKING_CAPITAL_LOAN_TRANSACTION_IDENTIFIER + txn.getId();
        final List<JournalEntry> existingEntries = journalEntryRepository.findJournalEntries(transactionId,
                WORKING_CAPITAL_LOAN_ENTITY_TYPE);

        for (final JournalEntry journalEntry : existingEntries) {
            final JournalEntry reversalEntry = createMirrorEntry(journalEntry, transactionId, transactionDate);
            if (supersede) {
                reversalEntry.setReversed(true);
            }
            helper.persistJournalEntry(reversalEntry);

            journalEntry.setReversed(true);
            journalEntry.setReversalJournalEntry(reversalEntry);
            helper.persistJournalEntry(journalEntry);
        }
    }

    @Override
    public void postJournalEntriesForDiscountFeeAmortization(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final boolean isChargedOff) {
        final Office office = loan.getClient().getOffice();
        final Long productId = loan.getLoanProduct().getId();
        final String currencyCode = loan.getLoanProductRelatedDetails().getCurrency().getCode();
        final LocalDate transactionDate = txn.getTransactionDate();
        final Long loanId = loan.getId();
        final Long txnId = txn.getId();
        final BigDecimal amount = txn.getTransactionAmount();

        helper.checkForBranchClosures(helper.getLatestClosureByBranch(office.getId()), transactionDate);

        if (MathUtil.isGreaterThanZero(amount)) {
            final GLAccount deferredIncomeAccount = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId,
                    CashAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), null);
            helper.createDebitJournalEntryForWorkingCapitalLoan(office, currencyCode, deferredIncomeAccount, loanId, txnId, transactionDate,
                    amount, null);

            final CashAccountsForLoan creditAccountType = resolveChargeOffExpenseAccount(loan, isChargedOff);
            final GLAccount creditAccount = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, creditAccountType.getValue(),
                    null);
            helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, creditAccount, loanId, txnId, transactionDate,
                    amount, null);
        }
    }

    @Override
    public void postJournalEntriesForDiscountFeeAmortizationAdjustment(final WorkingCapitalLoan loan,
            final WorkingCapitalLoanTransaction txn, final boolean isChargedOff) {
        final Office office = loan.getClient().getOffice();
        final Long productId = loan.getLoanProduct().getId();
        final String currencyCode = loan.getLoanProductRelatedDetails().getCurrency().getCode();
        final LocalDate transactionDate = txn.getTransactionDate();
        final Long loanId = loan.getId();
        final Long txnId = txn.getId();
        final BigDecimal amount = txn.getTransactionAmount();

        helper.checkForBranchClosures(helper.getLatestClosureByBranch(office.getId()), transactionDate);

        if (MathUtil.isGreaterThanZero(amount)) {
            final GLAccount deferredIncomeAccount = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId,
                    CashAccountsForLoan.DEFERRED_INCOME_LIABILITY.getValue(), null);
            helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, deferredIncomeAccount, loanId, txnId,
                    transactionDate, amount, null);

            final CashAccountsForLoan debitAccountType = resolveChargeOffExpenseAccount(loan, isChargedOff);
            final GLAccount debitAccount = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, debitAccountType.getValue(),
                    null);
            helper.createDebitJournalEntryForWorkingCapitalLoan(office, currencyCode, debitAccount, loanId, txnId, transactionDate, amount,
                    null);
        }
    }

    @Override
    public void postJournalEntriesForDiscountFee(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn) {
        postDiscountFeeDeferralEntries(loan, txn, CashAccountsForLoan.LOAN_PORTFOLIO, CashAccountsForLoan.DEFERRED_INCOME_LIABILITY);
    }

    @Override
    public void postJournalEntriesForDiscountFeeAdjustment(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn) {
        postDiscountFeeDeferralEntries(loan, txn, CashAccountsForLoan.DEFERRED_INCOME_LIABILITY, CashAccountsForLoan.LOAN_PORTFOLIO);
    }

    private void postDiscountFeeDeferralEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final CashAccountsForLoan debitAccountType, final CashAccountsForLoan creditAccountType) {
        final Office office = loan.getClient().getOffice();
        final Long productId = loan.getLoanProduct().getId();
        final String currencyCode = loan.getLoanProductRelatedDetails().getCurrency().getCode();
        final LocalDate transactionDate = txn.getTransactionDate();
        final Long loanId = loan.getId();
        final Long txnId = txn.getId();
        final BigDecimal amount = txn.getTransactionAmount();

        helper.checkForBranchClosures(helper.getLatestClosureByBranch(office.getId()), transactionDate);

        if (MathUtil.isGreaterThanZero(amount)) {
            final GLAccount debitAccount = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, debitAccountType.getValue(),
                    null);
            helper.createDebitJournalEntryForWorkingCapitalLoan(office, currencyCode, debitAccount, loanId, txnId, transactionDate, amount,
                    null);

            final GLAccount creditAccount = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, creditAccountType.getValue(),
                    null);
            helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, creditAccount, loanId, txnId, transactionDate,
                    amount, null);
        }
    }

    private Long extractPaymentTypeId(final WorkingCapitalLoanTransaction txn) {
        if (txn.getPaymentDetail() != null && txn.getPaymentDetail().getPaymentType() != null) {
            return txn.getPaymentDetail().getPaymentType().getId();
        }
        return null;
    }

    private CashAccountsForLoan resolveChargeOffExpenseAccount(final WorkingCapitalLoan loan, final boolean isChargedOff) {
        if (!isChargedOff) {
            return CashAccountsForLoan.INCOME_FROM_DISCOUNT_FEE;
        }
        return chargeOffExpenseAccount(loan);
    }

    private class JournalEntryPostingHelper {

        final Office office;
        final Long productId;
        final String currencyCode;
        final LocalDate transactionDate;
        final Long paymentTypeId;
        final Long loanId;
        final Long txnId;
        final PaymentDetail paymentDetail;

        JournalEntryPostingHelper(WorkingCapitalLoan loan, WorkingCapitalLoanTransaction txn) {
            paymentTypeId = extractPaymentTypeId(txn);
            transactionDate = txn.getTransactionDate();
            currencyCode = loan.getLoanProductRelatedDetails().getCurrency().getCode();
            productId = loan.getLoanProduct().getId();
            office = loan.getClient().getOffice();
            loanId = loan.getId();
            txnId = txn.getId();
            paymentDetail = txn.getPaymentDetail();
        }

        /** Books one planned posting. A zero (or absent) amount books nothing. */
        void post(final LedgerPosting posting) {
            if (!MathUtil.isGreaterThanZero(posting.amount())) {
                return;
            }
            final GLAccount account = posting.resolvedAccount() != null ? posting.resolvedAccount()
                    : helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, posting.account().getValue(), paymentTypeId);
            final PaymentDetail entryPaymentDetail = posting.withPaymentDetail() ? paymentDetail : null;
            if (posting.debit()) {
                helper.createDebitJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate,
                        posting.amount(), entryPaymentDetail);
            } else {
                helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate,
                        posting.amount(), entryPaymentDetail);
            }
        }
    }
}
