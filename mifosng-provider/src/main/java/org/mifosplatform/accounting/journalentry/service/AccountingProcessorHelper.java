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
        final BigDecimal calculatedInterest = (BigDecimal) accountingBridgeData.get("calculatedInterest");

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

        return new LoanDTO(loanId, loanProductId, officeId, calculatedInterest, cashBasedAccountingEnabled, accrualBasedAccountingEnabled,
                newLoanTransactions);
    }

    public GLAccount getLinkedCashAccountsForLoanProduct(final Long loanProductId, final CASH_ACCOUNTS_FOR_LOAN finAccountType) {
        final ProductToGLAccountMapping accountMapping = this.accountMappingRepository
                .findByProductIdAndProductTypeAndFinancialAccountType(loanProductId, PortfolioProductType.LOAN.getValue(),
                        finAccountType.getValue());
        return accountMapping.getGlAccount();
    }

    public GLAccount getLinkedAccrualAccountsForLoanProduct(final Long loanProductId, final ACCRUAL_ACCOUNTS_FOR_LOAN finAccountType) {
        final ProductToGLAccountMapping accountMapping = this.accountMappingRepository
                .findByProductIdAndProductTypeAndFinancialAccountType(loanProductId, PortfolioProductType.LOAN.getValue(),
                        finAccountType.getValue());
        return accountMapping.getGlAccount();
    }

    public void createCreditJournalEntryForLoanProduct(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        final JournalEntry journalEntry = JournalEntry.createNew(office, account, transactionId, manualEntry, transactionDate,
                JournalEntryType.CREDIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    public void createDebitJournalEntryForLoanProduct(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        final JournalEntry journalEntry = JournalEntry.createNew(office, account, transactionId, manualEntry, transactionDate,
                JournalEntryType.DEBIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
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

}
