/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.closure.domain.GLClosureRepository;
import org.mifosplatform.accounting.common.AccountingConstants.ACCRUAL_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.journalentry.data.LoanDTO;
import org.mifosplatform.accounting.journalentry.data.LoanTransactionDTO;
import org.mifosplatform.accounting.journalentry.domain.JournalEntry;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryRepository;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException.GL_JOURNAL_ENTRY_INVALID_REASON;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountingProcessorHelper {

    private final JournalEntryRepository glJournalEntryRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final GLClosureRepository closureRepository;
    private final OfficeRepository officeRepository;

    @Autowired
    public AccountingProcessorHelper(final JournalEntryRepository glJournalEntryRepository,
            final ProductToGLAccountMappingRepository accountMappingRepository, final GLClosureRepository closureRepository,
            final OfficeRepository officeRepository) {
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.closureRepository = closureRepository;
        this.officeRepository = officeRepository;
    }

    public LoanDTO populateLoanDtoFromMap(final Map<String, Object> accountingBridgeData, final boolean cashBasedAccountingEnabled,
            final boolean accrualBasedAccountingEnabled) {
        final Long loanId = (Long) accountingBridgeData.get("loanId");
        final Long loanProductId = (Long) accountingBridgeData.get("loanProductId");
        final Long officeId = (Long) accountingBridgeData.get("officeId");
        final List<LoanTransactionDTO> newLoanTransactions = new ArrayList<LoanTransactionDTO>();

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> newTransactionsMap = (List<Map<String, Object>>) accountingBridgeData.get("newLoanTransactions");

        for (final Map<String, Object> map : newTransactionsMap) {
            final String transactionId = ((Long) map.get("id")).toString();
            final Date transactionDate = ((LocalDate) map.get("date")).toDate();
            final LoanTransactionEnumData transactionType = (LoanTransactionEnumData) map.get("type");
            final BigDecimal amount = (BigDecimal) map.get("amount");
            final BigDecimal principal = (BigDecimal) map.get("principalPortion");
            final BigDecimal interest = (BigDecimal) map.get("interestPortion");
            final BigDecimal fees = (BigDecimal) map.get("feeChargesPortion");
            final BigDecimal penalties = (BigDecimal) map.get("penaltyChargesPortion");
            final boolean reversed = (Boolean) map.get("reversed");

            final LoanTransactionDTO transaction = new LoanTransactionDTO(transactionId, transactionDate, transactionType, amount,
                    principal, interest, fees, penalties, reversed);

            newLoanTransactions.add(transaction);

        }

        return new LoanDTO(loanId, loanProductId, officeId, cashBasedAccountingEnabled, accrualBasedAccountingEnabled, newLoanTransactions);
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for
     * Accrual Based accounting.
     * 
     * The target accounts for debits and credits are switched in case of a
     * reversal
     * 
     * @param office
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param loanProductId
     * @param loanId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     */
    public void createAccrualBasedJournalEntriesAndReversalsForLoan(final Office office,
            final ACCRUAL_ACCOUNTS_FOR_LOAN accountTypeToBeDebited, final ACCRUAL_ACCOUNTS_FOR_LOAN accountTypeToBeCredited,
            final Long loanProductId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountTypeToDebitId = accountTypeToBeDebited.getValue();
        int accountTypeToCreditId = accountTypeToBeCredited.getValue();
        // reverse debits and credits for reversals
        if (isReversal) {
            accountTypeToDebitId = accountTypeToBeCredited.getValue();
            accountTypeToCreditId = accountTypeToBeDebited.getValue();
        }
        createJournalEntriesForLoan(office, accountTypeToDebitId, accountTypeToCreditId, loanProductId, loanId, transactionId,
                transactionDate, amount);
    }

    /**
     * Convenience method that creates a pair of related Debits and Credits for
     * Cash Based accounting.
     * 
     * The target accounts for debits and credits are switched in case of a
     * reversal
     * 
     * @param office
     * @param accountTypeToBeDebited
     *            Enum of the placeholder GLAccount to be debited
     * @param accountTypeToBeCredited
     *            Enum of the placeholder of the GLAccount to be credited
     * @param loanProductId
     * @param loanId
     * @param transactionId
     * @param transactionDate
     * @param amount
     * @param isReversal
     */
    public void createCashBasedJournalEntriesAndReversalsForLoan(final Office office, final CASH_ACCOUNTS_FOR_LOAN accountTypeToBeDebited,
            final CASH_ACCOUNTS_FOR_LOAN accountTypeToBeCredited, final Long loanProductId, final Long loanId, final String transactionId,
            final Date transactionDate, final BigDecimal amount, final Boolean isReversal) {
        int accountTypeToDebitId = accountTypeToBeDebited.getValue();
        int accountTypeToCreditId = accountTypeToBeCredited.getValue();
        // reverse debits and credits for reversals
        if (isReversal) {
            accountTypeToDebitId = accountTypeToBeCredited.getValue();
            accountTypeToCreditId = accountTypeToBeDebited.getValue();
        }
        createJournalEntriesForLoan(office, accountTypeToDebitId, accountTypeToCreditId, loanProductId, loanId, transactionId,
                transactionDate, amount);
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final CASH_ACCOUNTS_FOR_LOAN accountMappingType,
            final Long loanProductId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountMappingTypeId = accountMappingType.getValue();
        createCreditJournalEntryOrReversalForLoan(office, accountMappingTypeId, loanProductId, loanId, transactionId, transactionDate,
                amount, isReversal);
    }

    public void createCreditJournalEntryOrReversalForLoan(final Office office, final ACCRUAL_ACCOUNTS_FOR_LOAN accountMappingType,
            final Long loanProductId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountMappingTypeId = accountMappingType.getValue();
        createCreditJournalEntryOrReversalForLoan(office, accountMappingTypeId, loanProductId, loanId, transactionId, transactionDate,
                amount, isReversal);
    }

    public void createDebitJournalEntryOrReversalForLoan(final Office office, final CASH_ACCOUNTS_FOR_LOAN accountMappingType,
            final Long loanProductId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountMappingTypeId = accountMappingType.getValue();
        createDebitJournalEntryOrReversalForLoan(office, accountMappingTypeId, loanProductId, loanId, transactionId, transactionDate,
                amount, isReversal);
    }

    public void createDebitJournalEntryOrReversalForLoan(final Office office, final ACCRUAL_ACCOUNTS_FOR_LOAN accountMappingType,
            final Long loanProductId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount,
            final Boolean isReversal) {
        int accountMappingTypeId = accountMappingType.getValue();
        createDebitJournalEntryOrReversalForLoan(office, accountMappingTypeId, loanProductId, loanId, transactionId, transactionDate,
                amount, isReversal);
    }

    /**
     * @param latestGLClosure
     * @param transactionDate
     */
    public void checkForBranchClosures(final GLClosure latestGLClosure, final Date transactionDate) {
        /**
         * check if an accounting closure has happened for this branch after the
         * transaction Date
         **/
        if (latestGLClosure != null) {
            if (latestGLClosure.getClosingDate().after(transactionDate) || latestGLClosure.getClosingDate().equals(transactionDate)) { throw new JournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(), null, null); }
        }
    }

    public GLClosure getLatestClosureByBranch(long officeId) {
        return this.closureRepository.getLatestGLClosureByBranch(officeId);
    }

    public Office getOfficeById(long officeId) {
        return this.officeRepository.findOne(officeId);
    }

    private void createJournalEntriesForLoan(final Office office, final int accountTypeToDebitId, final int accountTypeToCreditId,
            final Long loanProductId, final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount) {
        GLAccount debitAccount = getLinkedGLAccountForLoanProduct(loanProductId, accountTypeToDebitId);
        GLAccount creditAccount = getLinkedGLAccountForLoanProduct(loanProductId, accountTypeToCreditId);
        createDebitJournalEntryForLoan(office, debitAccount, loanId, transactionId, transactionDate, amount);
        createCreditJournalEntryForLoan(office, creditAccount, loanId, transactionId, transactionDate, amount);
    }

    private void createDebitJournalEntryOrReversalForLoan(final Office office, final int accountMappingTypeId, final Long loanProductId,
            final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount, final Boolean isReversal) {
        GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, accountMappingTypeId);
        if (isReversal) {
            createCreditJournalEntryForLoan(office, account, loanId, transactionId, transactionDate, amount);
        } else {
            createDebitJournalEntryForLoan(office, account, loanId, transactionId, transactionDate, amount);
        }
    }

    private void createCreditJournalEntryOrReversalForLoan(final Office office, final int accountMappingTypeId, final Long loanProductId,
            final Long loanId, final String transactionId, final Date transactionDate, final BigDecimal amount, final Boolean isReversal) {
        GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, accountMappingTypeId);
        if (isReversal) {
            createDebitJournalEntryForLoan(office, account, loanId, transactionId, transactionDate, amount);
        } else {
            createCreditJournalEntryForLoan(office, account, loanId, transactionId, transactionDate, amount);
        }
    }

    private void createCreditJournalEntryForLoan(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        final JournalEntry journalEntry = JournalEntry.createNew(office, account, transactionId, manualEntry, transactionDate,
                JournalEntryType.CREDIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void createDebitJournalEntryForLoan(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        final JournalEntry journalEntry = JournalEntry.createNew(office, account, transactionId, manualEntry, transactionDate,
                JournalEntryType.DEBIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private GLAccount getLinkedGLAccountForLoanProduct(final Long loanProductId, final int accountMappingTypeId) {
        final ProductToGLAccountMapping accountMapping = this.accountMappingRepository
                .findByProductIdAndProductTypeAndFinancialAccountType(loanProductId, PortfolioProductType.LOAN.getValue(),
                        accountMappingTypeId);
        return accountMapping.getGlAccount();
    }

}
