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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.fineract.accounting.common.AccountingConstants.CashAccountsForLoan;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.PortfolioProductType;
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
        final Long productId = loan.getLoanProduct().getId();
        final String currencyCode = loan.getLoanProductRelatedDetails().getCurrency().getCode();
        final LocalDate transactionDate = txn.getTransactionDate();
        final Long paymentTypeId = extractPaymentTypeId(txn);

        helper.checkForBranchClosures(helper.getLatestClosureByBranch(office.getId()), transactionDate);

        final BigDecimal principalPortion = MathUtil.nullToZero(allocation.getPrincipalPortion());
        final BigDecimal feesPortion = MathUtil.nullToZero(allocation.getFeeChargesPortion());
        final BigDecimal penaltiesPortion = MathUtil.nullToZero(allocation.getPenaltyChargesPortion());
        final BigDecimal overpaymentPortion = txn.getTransactionAmount().subtract(principalPortion).subtract(feesPortion)
                .subtract(penaltiesPortion).max(BigDecimal.ZERO);

        switch (txn.getTypeOf()) {
            case LoanTransactionType.REPAYMENT -> {
                if (isChargedOff) {
                    postChargedOffRepaymentEntries(office, productId, currencyCode, transactionDate, paymentTypeId, txn, principalPortion,
                            feesPortion, penaltiesPortion, overpaymentPortion);
                } else {
                    postRegularRepaymentEntries(office, productId, currencyCode, transactionDate, paymentTypeId, txn, principalPortion,
                            feesPortion, penaltiesPortion, overpaymentPortion);
                }
            }
            case LoanTransactionType.GOODWILL_CREDIT -> {
                if (!isChargedOff) {
                    postGoodwillCreditJournalEntries(loan, txn, principalPortion, feesPortion, penaltiesPortion, overpaymentPortion);
                } else {
                    throw new NotImplementedException("Charge off is not implemented yet for Goodwill Credit for Working Capital Loan");
                }
            }
            case LoanTransactionType.CREDIT_BALANCE_REFUND -> postCreditBalanceRefundJournalEntries(loan, txn);
            case LoanTransactionType.CHARGE_ADJUSTMENT ->
                postChargeAdjustmentJournalEntries(loan, txn, feesPortion, penaltiesPortion, isChargedOff);
            default -> {
                throw new NotImplementedException(
                        "Post Journal Entries is not implemented yet for " + txn.getTypeOf().getCode() + " for Working Capital Loan");
            }
        }
    }

    private void postChargeAdjustmentJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn,
            final BigDecimal feesPortion, final BigDecimal penaltiesPortion, final boolean isChargedOff) {
        final JournalEntryPostingHelper accountPostHelper = new JournalEntryPostingHelper(loan, txn);
        final CashAccountsForLoan feeIncomeAccountType = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.INCOME_FROM_FEES;
        final CashAccountsForLoan penaltyIncomeAccountType = isChargedOff ? CashAccountsForLoan.INCOME_FROM_RECOVERY
                : CashAccountsForLoan.INCOME_FROM_PENALTIES;

        // Debit income (reverse income recognized at accrual time)
        accountPostHelper.postDebitJournalEntry(feeIncomeAccountType, feesPortion);
        accountPostHelper.postDebitJournalEntry(penaltyIncomeAccountType, penaltiesPortion);

        // Credit receivable (reduce the outstanding charge)
        accountPostHelper.postCreditJournalEntry(CashAccountsForLoan.FEES_RECEIVABLE, feesPortion);
        accountPostHelper.postCreditJournalEntry(CashAccountsForLoan.PENALTIES_RECEIVABLE, penaltiesPortion);
    }

    private void postGoodwillCreditJournalEntries(WorkingCapitalLoan loan, WorkingCapitalLoanTransaction txn, BigDecimal principalPortion,
            BigDecimal feesPortion, BigDecimal penaltiesPortion, BigDecimal overpaymentPortion) {
        BigDecimal overpaymentPlusPrincipal = principalPortion.add(overpaymentPortion);
        JournalEntryPostingHelper accountPostHelper = new JournalEntryPostingHelper(loan, txn);
        // debit
        accountPostHelper.postDebitJournalEntry(CashAccountsForLoan.GOODWILL_CREDIT, overpaymentPlusPrincipal);
        accountPostHelper.postDebitJournalEntry(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_FEES, feesPortion);
        accountPostHelper.postDebitJournalEntry(CashAccountsForLoan.INCOME_FROM_GOODWILL_CREDIT_PENALTY, penaltiesPortion);
        // credit
        accountPostHelper.postCreditJournalEntry(CashAccountsForLoan.LOAN_PORTFOLIO, principalPortion);
        accountPostHelper.postCreditJournalEntry(CashAccountsForLoan.FEES_RECEIVABLE, feesPortion);
        accountPostHelper.postCreditJournalEntry(CashAccountsForLoan.PENALTIES_RECEIVABLE, penaltiesPortion);
        accountPostHelper.postCreditJournalEntry(CashAccountsForLoan.OVERPAYMENT, overpaymentPortion);
    }

    private void postCreditBalanceRefundJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn) {
        final BigDecimal amount = txn.getTransactionAmount();
        final JournalEntryPostingHelper accountPostHelper = new JournalEntryPostingHelper(loan, txn);
        // debit
        accountPostHelper.postDebitJournalEntry(CashAccountsForLoan.OVERPAYMENT, amount);
        // credit
        accountPostHelper.postCreditJournalEntry(CashAccountsForLoan.FUND_SOURCE, amount);
    }

    @Override
    public void postReversalJournalEntries(final WorkingCapitalLoan loan, final WorkingCapitalLoanTransaction txn) {
        final Office office = loan.getClient().getOffice();
        final LocalDate transactionDate = txn.getReversedOnDate() != null ? txn.getReversedOnDate() : DateUtils.getBusinessLocalDate();

        helper.checkForBranchClosures(helper.getLatestClosureByBranch(office.getId()), transactionDate);

        final String transactionId = AccountingProcessorHelper.WORKING_CAPITAL_LOAN_TRANSACTION_IDENTIFIER + txn.getId();
        final List<JournalEntry> existingEntries = journalEntryRepository.findJournalEntries(transactionId,
                WORKING_CAPITAL_LOAN_ENTITY_TYPE);

        for (final JournalEntry journalEntry : existingEntries) {
            final JournalEntryType reversalType = journalEntry.isDebitEntry() ? JournalEntryType.CREDIT : JournalEntryType.DEBIT;
            final JournalEntry reversalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getPaymentDetail(),
                    journalEntry.getGlAccount(), journalEntry.getCurrencyCode(), transactionId, Boolean.FALSE, transactionDate,
                    reversalType, journalEntry.getAmount(), journalEntry.getDescription(), journalEntry.getEntityType(),
                    journalEntry.getEntityId(), journalEntry.getReferenceNumber(), journalEntry.getLoanTransactionId(),
                    journalEntry.getSavingsTransactionId(), journalEntry.getClientTransactionId(), journalEntry.getShareTransactionId());
            helper.persistJournalEntry(reversalEntry);

            journalEntry.setReversed(true);
            journalEntry.setReversalJournalEntry(reversalEntry);
            helper.persistJournalEntry(journalEntry);
        }
    }

    private void postRegularRepaymentEntries(final Office office, final Long productId, final String currencyCode,
            final LocalDate transactionDate, final Long paymentTypeId, final WorkingCapitalLoanTransaction txn,
            final BigDecimal principalPortion, final BigDecimal feesPortion, final BigDecimal penaltiesPortion,
            final BigDecimal overpaymentPortion) {
        postRepaymentCreditEntries(office, productId, currencyCode, transactionDate, txn, principalPortion,
                CashAccountsForLoan.LOAN_PORTFOLIO, feesPortion, CashAccountsForLoan.FEES_RECEIVABLE, penaltiesPortion,
                CashAccountsForLoan.PENALTIES_RECEIVABLE, overpaymentPortion);
        postFundSourceDebit(office, productId, currencyCode, transactionDate, paymentTypeId, txn);
    }

    private void postChargedOffRepaymentEntries(final Office office, final Long productId, final String currencyCode,
            final LocalDate transactionDate, final Long paymentTypeId, final WorkingCapitalLoanTransaction txn,
            final BigDecimal principalPortion, final BigDecimal feesPortion, final BigDecimal penaltiesPortion,
            final BigDecimal overpaymentPortion) {
        postRepaymentCreditEntries(office, productId, currencyCode, transactionDate, txn, principalPortion,
                CashAccountsForLoan.INCOME_FROM_RECOVERY, feesPortion, CashAccountsForLoan.INCOME_FROM_RECOVERY, penaltiesPortion,
                CashAccountsForLoan.INCOME_FROM_RECOVERY, overpaymentPortion);
        postFundSourceDebit(office, productId, currencyCode, transactionDate, paymentTypeId, txn);
    }

    private void postRepaymentCreditEntries(final Office office, final Long productId, final String currencyCode,
            final LocalDate transactionDate, final WorkingCapitalLoanTransaction txn, final BigDecimal principalPortion,
            final CashAccountsForLoan principalAccountType, final BigDecimal feesPortion, final CashAccountsForLoan feesAccountType,
            final BigDecimal penaltiesPortion, final CashAccountsForLoan penaltiesAccountType, final BigDecimal overpaymentPortion) {
        final Long loanId = txn.getWcLoan().getId();
        final Long txnId = txn.getId();

        if (MathUtil.isGreaterThanZero(principalPortion)) {
            final GLAccount account = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, principalAccountType.getValue(),
                    null);
            helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate,
                    principalPortion, null);
        }

        if (MathUtil.isGreaterThanZero(feesPortion)) {
            final GLAccount account = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, feesAccountType.getValue(), null);
            helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate, feesPortion,
                    null);
        }

        if (MathUtil.isGreaterThanZero(penaltiesPortion)) {
            final GLAccount account = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, penaltiesAccountType.getValue(),
                    null);
            helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate,
                    penaltiesPortion, null);
        }

        if (MathUtil.isGreaterThanZero(overpaymentPortion)) {
            final GLAccount account = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId,
                    CashAccountsForLoan.OVERPAYMENT.getValue(), null);
            helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate,
                    overpaymentPortion, null);
        }
    }

    private void postFundSourceDebit(final Office office, final Long productId, final String currencyCode, final LocalDate transactionDate,
            final Long paymentTypeId, final WorkingCapitalLoanTransaction txn) {
        final BigDecimal totalAmount = txn.getTransactionAmount();
        if (MathUtil.isGreaterThanZero(totalAmount)) {
            final GLAccount account = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId,
                    CashAccountsForLoan.FUND_SOURCE.getValue(), paymentTypeId);
            helper.createDebitJournalEntryForWorkingCapitalLoan(office, currencyCode, account, txn.getWcLoan().getId(), txn.getId(),
                    transactionDate, totalAmount, txn.getPaymentDetail());
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

            final CashAccountsForLoan creditAccountType = isChargedOff ? CashAccountsForLoan.CHARGE_OFF_EXPENSE
                    : CashAccountsForLoan.INCOME_FROM_DISCOUNT_FEE;
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

            final CashAccountsForLoan debitAccountType = isChargedOff ? CashAccountsForLoan.CHARGE_OFF_EXPENSE
                    : CashAccountsForLoan.INCOME_FROM_DISCOUNT_FEE;
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

        void postCreditJournalEntry(CashAccountsForLoan accountType, BigDecimal amount) {
            if (MathUtil.isGreaterThanZero(amount)) {
                final GLAccount account = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, accountType.getValue(),
                        paymentTypeId);
                helper.createCreditJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate, amount,
                        paymentDetail);
            }
        }

        void postDebitJournalEntry(CashAccountsForLoan accountType, BigDecimal amount) {
            if (MathUtil.isGreaterThanZero(amount)) {
                final GLAccount account = helper.getLinkedGLAccountForWorkingCapitalLoanProduct(productId, accountType.getValue(),
                        paymentTypeId);
                helper.createDebitJournalEntryForWorkingCapitalLoan(office, currencyCode, account, loanId, txnId, transactionDate, amount,
                        paymentDetail);
            }
        }
    }
}
