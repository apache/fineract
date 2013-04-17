package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.Date;

import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.journalentry.data.LoanDTO;
import org.mifosplatform.accounting.journalentry.data.LoanTransactionDTO;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccrualBasedAccountingProcessorForLoan implements AccountingProcessorForLoan {

    private final AccountingProcessorHelper helper;

    @Autowired
    public AccrualBasedAccountingProcessorForLoan(final AccountingProcessorHelper accountingProcessorHelper) {
        this.helper = accountingProcessorHelper;
    }

    @Override
    public void createJournalEntriesForLoan(final LoanDTO loanDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(loanDTO.getOfficeId());
        final Office office = this.helper.getOfficeById(loanDTO.getOfficeId());
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final Date transactionDate = loanTransactionDTO.getTransactionDate();
            helper.checkForBranchClosures(latestGLClosure, transactionDate);

            /** Handle Disbursements **/
            if (loanTransactionDTO.getTransactionType().isDisbursement()) {
                createJournalEntriesForDisbursements(loanDTO, loanTransactionDTO, office);
            }

            /*** Handle Interest Postings ***/
            if (loanTransactionDTO.getTransactionType().isApplyInterest()) {
                createJournalEntriesForInterestPostings(loanDTO, loanTransactionDTO, office);
            }

            /*** Handle Apply charges ***/
            else if (loanTransactionDTO.getTransactionType().isApplyCharges()) {
                createJournalEntriesForApplyCharges(loanDTO, loanTransactionDTO, office);
            }

            /***
             * Handle repayments, repayments at disbursement and reversal of
             * Repayments and Repayments at disbursement
             ***/
            else if (loanTransactionDTO.getTransactionType().isRepayment()
                    || loanTransactionDTO.getTransactionType().isRepaymentAtDisbursement()) {
                createJournalEntriesForRepaymentsAndWriteOffs(loanDTO, loanTransactionDTO, office, false, loanTransactionDTO
                        .getTransactionType().isRepaymentAtDisbursement());
            }

            /** Handle Write Offs, waivers and their reversals **/
            else if ((loanTransactionDTO.getTransactionType().isWriteOff() || loanTransactionDTO.getTransactionType().isWaiveInterest() || loanTransactionDTO
                    .getTransactionType().isWaiveCharges())) {
                createJournalEntriesForRepaymentsAndWriteOffs(loanDTO, loanTransactionDTO, office, true, false);
            }
        }
    }

    /**
     * Debit loan Portfolio and credit Fund source for Disbursement.
     * 
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForDisbursements(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {

        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal disbursalAmount = loanTransactionDTO.getAmount();
        final boolean isReversed = loanTransactionDTO.isReversed();

        // create journal entries for the disbursement (or disbursement
        // reversal)
        helper.createAccrualBasedJournalEntriesAndReversalsForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO,
                ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE, loanProductId, loanId, transactionId, transactionDate, disbursalAmount, isReversed);

    }

    /**
     * Recognise the receivable interest <br/>
     * Debit "Interest Receivable" and Credit "Income from Interest"
     * 
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForInterestPostings(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office) {

        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal amount = loanTransactionDTO.getAmount();
        final boolean isReversed = loanTransactionDTO.isReversed();

        // create journal entries for recognising interest (or reversal)
        if (amount != null && !(amount.compareTo(BigDecimal.ZERO) == 0)) {
            helper.createAccrualBasedJournalEntriesAndReversalsForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS, loanProductId, loanId, transactionId, transactionDate, amount, isReversed);
        }
    }

    /**
     * 
     * Handles repayments using the following posting rules <br/>
     * <br/>
     * <br/>
     * 
     * <b>Principal Repayment</b>: Debits "Fund Source" and Credits
     * "Loan Portfolio"<br/>
     * 
     * <b>Interest Repayment</b>:Debits "Fund Source" and and Credits
     * "Receivable Interest" <br/>
     * 
     * <b>Fee Repayment</b>:Debits "Fund Source" (or "Interest on Loans" in case
     * of repayment at disbursement) and and Credits "Receivable Fees" <br/>
     * 
     * <b>Penalty Repayment</b>: Debits "Fund Source" and and Credits
     * "Receivable Penalties" <br/>
     * <br/>
     * Handles write offs using the following posting rules <br/>
     * <br/>
     * <b>Principal Write off</b>: Debits "Losses Written Off" and Credits
     * "Loan Portfolio"<br/>
     * 
     * <b>Interest Write off</b>:Debits "Losses Written off" and and Credits
     * "Receivable Interest" <br/>
     * 
     * <b>Fee Write off</b>:Debits "Losses Written off" and and Credits
     * "Receivable Fees" <br/>
     * 
     * <b>Penalty Write off</b>: Debits "Losses Written off" and and Credits
     * "Receivable Penalties" <br/>
     * <br/>
     * <br/>
     * In case the loan transaction has been reversed, all debits are turned
     * into credits and vice versa
     * 
     * @param loanTransactionDTO
     * @param loanDTO
     * @param office
     * 
     */
    private void createJournalEntriesForRepaymentsAndWriteOffs(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO,
            final Office office, final boolean writeOff, final boolean repaymentAtDisbursement) {
        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final boolean isReversal = loanTransactionDTO.isReversed();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        // handle principal payment or writeOff (and reversals)
        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);
            helper.createCreditJournalEntryOrReversalForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO, loanProductId, loanId,
                    transactionId, transactionDate, principalAmount, isReversal);
        }

        // handle interest payment of writeOff (and reversals)
        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);
            helper.createCreditJournalEntryOrReversalForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE, loanProductId, loanId,
                    transactionId, transactionDate, interestAmount, isReversal);
        }

        // handle fees payment of writeOff (and reversals)
        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            ACCRUAL_ACCOUNTS_FOR_LOAN feeAccountToCredit = ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE;

            if (repaymentAtDisbursement) {
                feeAccountToCredit = ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES;
            }

            helper.createCreditJournalEntryOrReversalForLoan(office, feeAccountToCredit, loanProductId, loanId, transactionId,
                    transactionDate, feesAmount, isReversal);
        }

        // handle penalties payment of writeOff (and reversals)
        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);
            helper.createCreditJournalEntryOrReversalForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE, loanProductId, loanId,
                    transactionId, transactionDate, penaltiesAmount, isReversal);
        }

        /**
         * Single DEBIT transaction for write-offs or Repayments (and their
         * reversals)
         ***/
        if (writeOff) {
            helper.createDebitJournalEntryOrReversalForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF, loanProductId, loanId,
                    transactionId, transactionDate, totalDebitAmount, isReversal);
        } else {
            helper.createDebitJournalEntryOrReversalForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE, loanProductId, loanId,
                    transactionId, transactionDate, totalDebitAmount, isReversal);
        }
    }

    /**
     * Create Journal Entries for application of charges (fees and payments)
     * using the following rules <br/>
     * 
     * <b>Fees:</b> Debit <i>Fees Receivable</i> and credit <i>Income from
     * Fees</i> <br/>
     * 
     * <b>Penalties:</b> Debit <i>Penalties Receivable</i> and credit <i>Income
     * from Penalties</i>
     * 
     * Also handles reversals for both fees and payment applications
     * 
     * @param loanDTO
     * @param loanTransactionDTO
     * @param office
     */
    private void createJournalEntriesForApplyCharges(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO, final Office office) {

        // loan properties
        final Long loanProductId = loanDTO.getLoanProductId();
        final Long loanId = loanDTO.getLoanId();

        // transaction properties
        final String transactionId = loanTransactionDTO.getTransactionId();
        final Date transactionDate = loanTransactionDTO.getTransactionDate();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final boolean isReversed = loanTransactionDTO.isReversed();

        // create journal entries for the fees application (or reversal)
        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            helper.createAccrualBasedJournalEntriesAndReversalsForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES, loanProductId, loanId, transactionId, transactionDate, feesAmount,
                    isReversed);
        }
        // create journal entries for the penalties application (or reversal)
        else if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            helper.createAccrualBasedJournalEntriesAndReversalsForLoan(office, ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES, loanProductId, loanId, transactionId, transactionDate,
                    penaltiesAmount, isReversed);
        }
    }

}
