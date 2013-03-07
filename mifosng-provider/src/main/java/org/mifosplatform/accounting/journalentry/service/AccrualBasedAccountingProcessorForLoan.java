package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.Date;

import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
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
    public void createJournalEntriesForLoan(LoanDTO loanDTO) {
        final GLClosure latestGLClosure = this.helper.getLatestClosureByBranch(loanDTO.getOfficeId());
        final Office office = this.helper.getOfficeById(loanDTO.getOfficeId());
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final Date transactionDate = loanTransactionDTO.getTransactionDate();
            helper.checkForBranchClosures(latestGLClosure, transactionDate);

            /** Handle Disbursements **/
            if (loanTransactionDTO.getTransactionType().isDisbursement()) {
                createJournalEntriesForDisbursements(loanDTO, loanTransactionDTO, office);
            }

            /*** Handle Apply charges ***/
            // TODO

            /***
             * Handle repayments, repayments at disbursement and reversal of
             * Repayments and Repayments at disbursement
             ***/
            else if (loanTransactionDTO.getTransactionType().isRepayment()
                    || loanTransactionDTO.getTransactionType().isRepaymentAtDisbursement()) {
                createJournalEntriesForRepayments(loanDTO, loanTransactionDTO, office);
            }

            /** Handle Write Offs and waivers **/
            else if ((loanTransactionDTO.getTransactionType().isWriteOff() || loanTransactionDTO.getTransactionType().isWaiveInterest() || loanTransactionDTO
                    .getTransactionType().isWaiveCharges())) {
                createJournalEntriesForWriteOff(loanDTO, loanTransactionDTO, office);
            }
        }
    }

    /**
     * Debit loan Portfolio and credit Fund source for Disbursement. Also
     * recognise the receivable interest (Debit "Interest Receivable" and credit
     * "Income from Interest")
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
        final GLAccount loanPortfolioAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
        final GLAccount fundSourceAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE);
        if (isReversed) {
            helper.createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                    disbursalAmount);
            helper.createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate, disbursalAmount);
        } else {
            helper.createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                    disbursalAmount);
            helper.createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    disbursalAmount);
        }

        // create journal entries for recognising interest (or reversal)
        BigDecimal interestApplied = loanDTO.getCalculatedInterest();
        if (interestApplied != null && !(interestApplied.compareTo(BigDecimal.ZERO) == 0)) {

            final GLAccount interestReceivableAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE);
            final GLAccount interestOnLoansAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, interestOnLoansAccount, loanId, transactionId, transactionDate,
                        interestApplied);
                helper.createCreditJournalEntryForLoanProduct(office, interestReceivableAccount, loanId, transactionId, transactionDate,
                        interestApplied);
            } else {
                helper.createDebitJournalEntryForLoanProduct(office, interestReceivableAccount, loanId, transactionId, transactionDate,
                        interestApplied);
                helper.createCreditJournalEntryForLoanProduct(office, interestOnLoansAccount, loanId, transactionId, transactionDate,
                        interestApplied);
            }
        }
    }

    /**
     * Create a single Debit for fund source and multiple credits if applicable
     * (loan portfolio for principal repayments, Interest receivable for
     * interest repayments, Fee receivable for fees payment and Penalty
     * Receivable from penalties for penalty payment)
     * 
     * <br/>
     * 
     * In case the loan transaction has been reversed, all debits are turned
     * into credits and vice versa
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
        final boolean isReversed = loanTransactionDTO.isReversed();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);

            final GLAccount loanPortfolioAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            }
        }
        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);

            final GLAccount interestAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate,
                        interestAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate,
                        interestAmount);
            }
        }
        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            final GLAccount incomeFromFeesAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate,
                        feesAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate,
                        feesAmount);
            }
        }
        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);

            final GLAccount incomeFromPenaltiesAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            }
        }
        final GLAccount fundSourceAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                ACCRUAL_ACCOUNTS_FOR_LOAN.FUND_SOURCE);
        if (isReversed) {
            helper.createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    totalDebitAmount);
        } else {
            helper.createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    totalDebitAmount);
        }
    }

    /**
     * Handles write offs using the following posting rules <br/>
     * 
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
     * 
     * @param loanTransactionDTO
     * @param loanDTO
     * @param office
     */
    private void createJournalEntriesForWriteOff(final LoanDTO loanDTO, final LoanTransactionDTO loanTransactionDTO, final Office office) {
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
        final boolean isReversed = loanTransactionDTO.isReversed();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);

            final GLAccount loanPortfolioAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            }

        }
        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);

            final GLAccount interestAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.INTEREST_RECEIVABLE);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate,
                        interestAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate,
                        interestAmount);
            }
        }
        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            final GLAccount incomeFromFeesAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.FEES_RECEIVABLE);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate,
                        feesAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate,
                        feesAmount);
            }
        }
        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);

            final GLAccount incomeFromPenaltiesAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                    ACCRUAL_ACCOUNTS_FOR_LOAN.PENALTIES_RECEIVABLE);
            if (isReversed) {
                helper.createDebitJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            } else {
                helper.createCreditJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            }
        }
        final GLAccount fundSourceAccount = helper.getLinkedAccrualAccountsForLoanProduct(loanProductId,
                ACCRUAL_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF);
        if (isReversed) {
            helper.createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    totalDebitAmount);
        } else {
            helper.createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate,
                    totalDebitAmount);
        }
    }

}
