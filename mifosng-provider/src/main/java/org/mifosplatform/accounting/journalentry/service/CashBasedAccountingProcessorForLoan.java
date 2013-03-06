package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.Date;

import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.journalentry.data.LoanDTO;
import org.mifosplatform.accounting.journalentry.data.LoanTransactionDTO;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CashBasedAccountingProcessorForLoan implements AccountingProcessorForLoan {

    private final AccountingProcessorHelper helper;

    @Autowired
    public CashBasedAccountingProcessorForLoan(final AccountingProcessorHelper accountingProcessorHelper) {
        this.helper = accountingProcessorHelper;
    }

    @Override
    public void createJournalEntriesForLoan(LoanDTO loanDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(loanDTO.getOfficeId());
        final Office office = this.helper.getOfficeById(loanDTO.getOfficeId());
        final Long loanProductId = loanDTO.getLoanProductId();
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final Date transactionDate = loanTransactionDTO.getTransactionDate();
            final String transactionId = loanTransactionDTO.getTransactionId();
            final Long loanId = loanDTO.getLoanId();

            helper.checkForBranchClosures(latestGLClosure, transactionDate);

            /** Handle Disbursements and reversals of disbursements **/
            if (loanTransactionDTO.getTransactionType().isDisbursement() && !loanTransactionDTO.isReversed()) {
                createJournalEntriesForDisbursements(loanDTO, loanTransactionDTO, office);
            }
            /***
             * Logic for repayments, repayments at disbursement and reversal of
             * Repayments and Repayments at disbursement
             ***/
            else if (loanTransactionDTO.getTransactionType().isRepayment()
                    || loanTransactionDTO.getTransactionType().isRepaymentAtDisbursement()) {
                createJournalEntriesForRepayments(loanDTO, loanTransactionDTO, office);
            }
            /***
             * Only principal write off affects cash based accounting (interest
             * and fee write off need not be considered). Debit losses written
             * off and credit Loan Portfolio
             **/
            else if (loanTransactionDTO.getTransactionType().isWriteOff() && !loanTransactionDTO.isReversed()) {
                final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
                final GLAccount lossesWrittenOffAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF);
                helper.createDebitJournalEntryForLoanProduct(office, lossesWrittenOffAccount, loanId, transactionId, transactionDate,
                        principalAmount);

                final GLAccount loanPortfolioAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
                helper.createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            }
        }
    }

    /**
     * Debit loan Portfolio and credit Fund source for a Disbursement <br/>
     * 
     * All debits are turned into credits and vice versa in case of disbursement
     * reversals
     * 
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

        final GLAccount loanPortfolioAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId,
                CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
        final GLAccount fundSourceAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);
        if (isReversed) {
            helper.createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate, disbursalAmount);
            helper.createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                    disbursalAmount);

        } else {
            helper.createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                    disbursalAmount);
            helper.createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    disbursalAmount);
        }
    }

    /**
     * Create a single Debit to fund source and multiple credits if applicable
     * (loan portfolio for principal repayments, Interest on loans for interest
     * repayments, Income from fees for fees payment and Income from penalties
     * for penalty payment)
     * 
     * In case the loan transaction is a reversal, all debits are turned into
     * credits and vice versa
     */
    private void createJournalEntriesForRepayments(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO, final Office office) {
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

        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);

            final GLAccount loanPortfolioAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
            if (isReversal) {
                helper.createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            }
        }
        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);

            final GLAccount interestAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS);
            if (isReversal) {
                helper.createDebitJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate,
                        interestAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate,
                        interestAmount);
            }
        }
        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            final GLAccount incomeFromFeesAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES);
            if (isReversal) {
                helper.createDebitJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate,
                        feesAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate,
                        feesAmount);
            }
        }
        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);

            final GLAccount incomeFromPenaltiesAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES);
            if (isReversal) {
                helper.createDebitJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            }
        }
        final GLAccount fundSourceAccount = helper.getLinkedCashAccountsForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);
        if (isReversal) {
            helper.createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    totalDebitAmount);
        } else {
            helper.createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    totalDebitAmount);
        }
    }
}
