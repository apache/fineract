/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.accounting.api.commands.SingleDebitOrCreditEntryCommand;
import org.mifosplatform.accounting.api.data.LoanDTO;
import org.mifosplatform.accounting.api.data.LoanTransactionDTO;
import org.mifosplatform.accounting.domain.GLAccount;
import org.mifosplatform.accounting.domain.GLAccountRepository;
import org.mifosplatform.accounting.domain.GLClosure;
import org.mifosplatform.accounting.domain.GLClosureRepository;
import org.mifosplatform.accounting.domain.GLJournalEntry;
import org.mifosplatform.accounting.domain.GLJournalEntryRepository;
import org.mifosplatform.accounting.domain.JournalEntryType;
import org.mifosplatform.accounting.domain.PortfolioProductType;
import org.mifosplatform.accounting.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.accounting.exceptions.GLAccountNotFoundException;
import org.mifosplatform.accounting.exceptions.GLJournalEntriesNotFoundException;
import org.mifosplatform.accounting.exceptions.GLJournalEntryInvalidException;
import org.mifosplatform.accounting.exceptions.GLJournalEntryInvalidException.GL_JOURNAL_ENTRY_INVALID_REASON;
import org.mifosplatform.accounting.service.GLJournalEntryCommandValidator;
import org.mifosplatform.accounting.service.GLJournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GLJournalEntryWritePlatformServiceJpaRepositoryImpl implements GLJournalEntryWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GLJournalEntryWritePlatformServiceJpaRepositoryImpl.class);

    private final GLClosureRepository glClosureRepository;
    private final GLAccountRepository glAccountRepository;
    private final GLJournalEntryRepository glJournalEntryRepository;
    private final OfficeRepository officeRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;

    @Autowired
    public GLJournalEntryWritePlatformServiceJpaRepositoryImpl(final GLClosureRepository glClosureRepository,
            final GLAccountRepository glAccountRepository, final GLJournalEntryRepository glJournalEntryRepository,
            final OfficeRepository officeRepository, final ProductToGLAccountMappingRepository accountMappingRepository) {
        this.glClosureRepository = glClosureRepository;
        this.officeRepository = officeRepository;
        this.glAccountRepository = glAccountRepository;
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.accountMappingRepository = accountMappingRepository;
    }

    @Transactional
    @Override
    public String createJournalEntry(final GLJournalEntryCommand command) {
        try {
            GLJournalEntryCommandValidator validator = new GLJournalEntryCommandValidator(command);
            validator.validateForCreate();

            // check office is valid
            final Office office = this.officeRepository.findOne(command.getOfficeId());
            if (office == null) { throw new OfficeNotFoundException(command.getOfficeId()); }

            validateBusinessRulesForJournalEntries(command);

            /** Set a transaction Id and save these Journal entries **/
            Date entryDate = command.getEntryDate().toDateMidnight().toDate();
            String transactionId = generateTransactionId();

            saveAllDebitOrCreditEntries(command, office, entryDate, command.getDebits(), transactionId, JournalEntryType.DEBIT);

            saveAllDebitOrCreditEntries(command, office, entryDate, command.getCredits(), transactionId, JournalEntryType.CREDIT);

            return transactionId;
        } catch (DataIntegrityViolationException dve) {
            handleJournalEntryDataIntegrityIssues(dve);
            return null;
        }
    }

    @Transactional
    @Override
    public String revertJournalEntry(final String transactionId) {

        // is the transaction Id valid
        List<GLJournalEntry> journalEntries = glJournalEntryRepository.findUnReversedManualJournalEntriesByTransactionId(transactionId);

        if (journalEntries.size() <= 1) { throw new GLJournalEntriesNotFoundException(transactionId); }

        String reversalTransactionId = generateTransactionId();

        for (GLJournalEntry journalEntry : journalEntries) {
            GLJournalEntry reversalJournalEntry;
            String reversalComment = "Reversal entry for Journal Entry with Entry Id  :" + journalEntry.getId() + " and transaction Id "
                    + transactionId;
            if (journalEntry.isDebitEntry()) {
                reversalJournalEntry = GLJournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(),
                        reversalTransactionId, false, journalEntry.getEntryDate(), JournalEntryType.CREDIT, journalEntry.getAmount(),
                        reversalComment, null, null);
            } else {
                reversalJournalEntry = GLJournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(),
                        reversalTransactionId, false, journalEntry.getEntryDate(), JournalEntryType.DEBIT, journalEntry.getAmount(),
                        reversalComment, null, null);
            }
            // save the reversal entry
            this.glJournalEntryRepository.saveAndFlush(reversalJournalEntry);
            journalEntry.setReversed(true);
            journalEntry.setReversalJournalEntry(reversalJournalEntry);
            // save the updated journal entry
            this.glJournalEntryRepository.saveAndFlush(journalEntry);
        }
        return reversalTransactionId;
    }

    @Transactional
    @Override
    public void createJournalEntriesForLoan(final LoanDTO loanDTO) {
        // shouldn't be before an accounting closure
        if (loanDTO.isCashBasedAccountingEnabled()) {
            createJournalEntriesUsingCashRules(loanDTO);
        } else if (loanDTO.isAccrualBasedAccountingEnabled()) {
            // TODO Vishwas: add accrual based accounting rules
        }
    }

    @Transactional
    @Override
    public void createJournalEntriesForLoan(final Map<String, Object> accountingBridgeData) {

        final boolean cashBasedAccountingEnabled = (Boolean) accountingBridgeData.get("cashBasedAccountingEnabled");
        final boolean accrualBasedAccountingEnabled = (Boolean) accountingBridgeData.get("accrualBasedAccountingEnabled");

        if (cashBasedAccountingEnabled) {
            final LoanDTO loanDTO = populateDtoFromMap(accountingBridgeData, cashBasedAccountingEnabled, accrualBasedAccountingEnabled);

            createJournalEntriesUsingCashRules(loanDTO);
        } else if (accrualBasedAccountingEnabled) {
            // TODO Vishwas: add accrual based accounting rules
        }
    }

    private LoanDTO populateDtoFromMap(final Map<String, Object> accountingBridgeData, final boolean cashBasedAccountingEnabled,
            final boolean accrualBasedAccountingEnabled) {
        final Long loanId = (Long) accountingBridgeData.get("loanId");
        final Long loanProductId = (Long) accountingBridgeData.get("loanProductId");
        final Long officeId = (Long) accountingBridgeData.get("officeId");

        final List<LoanTransactionDTO> newLoanTransactions = new ArrayList<LoanTransactionDTO>();

        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> newTransactionsMap = (List<Map<String, Object>>) accountingBridgeData.get("newLoanTransactions");

        for (Map<String, Object> map : newTransactionsMap) {
            String transactionId = ((Long) map.get("id")).toString();
            Date transactionDate = ((LocalDate) map.get("date")).toDate();
            BigDecimal amount = (BigDecimal) map.get("amount");
            BigDecimal principal = (BigDecimal) map.get("principalPortion");
            BigDecimal interest = (BigDecimal) map.get("interestPortion");
            BigDecimal fees = (BigDecimal) map.get("feeChargesPortion");
            BigDecimal penalties = (BigDecimal) map.get("penaltyChargesPortion");
            boolean disbursement = (Boolean) map.get("disbursement");
            boolean repayment = (Boolean) map.get("repayment");
            boolean repaymentAtDisbursement = (Boolean) map.get("repaymentAtDisbursement");
            boolean contra = (Boolean) map.get("contra");
            boolean writeOff = (Boolean) map.get("writeOff");

            final LoanTransactionDTO transaction = new LoanTransactionDTO(transactionId, transactionDate, amount, principal, interest,
                    fees, penalties, disbursement, repayment, repaymentAtDisbursement, contra, writeOff);

            newLoanTransactions.add(transaction);

        }

        return new LoanDTO(loanId, loanProductId, officeId, cashBasedAccountingEnabled, accrualBasedAccountingEnabled, newLoanTransactions);
    }

    private void createJournalEntriesUsingCashRules(final LoanDTO loanDTO) {
        GLClosure latestGLClosure = glClosureRepository.getLatestGLClosureByBranch(loanDTO.getOfficeId());
        Office office = officeRepository.findOne(loanDTO.getOfficeId());
        Long loanProductId = loanDTO.getLoanProductId();
        // TODO: Check for accounting type
        for (LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            Date entryDate = loanTransactionDTO.getTransactionDate();
            String transactionId = loanTransactionDTO.getTransactionId();
            Long loanId = loanDTO.getLoanId();
            /**
             * check if an accounting closure has happened for this branch after
             * the transaction Date
             **/
            if (latestGLClosure != null) {
                if (latestGLClosure.getClosingDate().after(entryDate) || latestGLClosure.getClosingDate().equals(entryDate)) { throw new GLJournalEntryInvalidException(
                        GL_JOURNAL_ENTRY_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(), null, null); }
            }

            /*** Debit loan Portfolio and credit Fund source for Disbursal **/
            if (loanTransactionDTO.isDisbursement()) {

                GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
                GLAccount fundSourceAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);

                BigDecimal disbursalAmount = loanTransactionDTO.getAmount();
                createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, entryDate, disbursalAmount);
                createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, entryDate, disbursalAmount);
            } else if (loanTransactionDTO.isRepayment() || loanTransactionDTO.isRepaymentAtDisbursement() || loanTransactionDTO.isContra()) {
                createJournalEntriesForRepaymentOrContraUsingCashRules(office, loanProductId, loanTransactionDTO, entryDate, transactionId,
                        loanId);
            }/***
             * Only principal write off affects cash based accounting (interest
             * and fee write off need not be considered). Debit losses written
             * off and credit Loan Portfolio
             **/
            else if (loanTransactionDTO.isWriteOff()) {
                BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
                GLAccount lossesWrittenOffAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF);
                createDebitJournalEntryForLoanProduct(office, lossesWrittenOffAccount, loanId, transactionId, entryDate, principalAmount);

                GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
                createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, entryDate, principalAmount);
            }
        }
    }

    /**
     * Create a single Debit to fund source and multiple credits if applicable
     * (loan portfolio for principal repayment, Interest on loans for interest
     * repayments, Income from fees for fees payment and Income from penalties
     * for penalty payment)
     * 
     * In case the loan transaction is a contra, all debits are turned into
     * credits and vice versa
     */
    private void createJournalEntriesForRepaymentOrContraUsingCashRules(final Office office, final Long loanProductId,
            final LoanTransactionDTO loanTransactionDTO, final Date entryDate, String transactionId, final Long loanId) {
        BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        BigDecimal interestAmount = loanTransactionDTO.getInterest();
        BigDecimal feesAmount = loanTransactionDTO.getFees();
        BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        boolean isContraTransaction = loanTransactionDTO.isContra();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (isContraTransaction) {
            transactionId = "reversal-" + transactionId;
        }

        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);

            GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, entryDate, principalAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, entryDate, principalAmount);
            }
        }
        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);

            GLAccount interestAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, entryDate, interestAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, entryDate, interestAmount);
            }
        }
        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            GLAccount incomeFromFeesAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, entryDate, feesAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, entryDate, feesAmount);
            }
        }
        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);

            GLAccount incomeFromPenaltiesAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, entryDate, penaltiesAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, entryDate,
                        penaltiesAmount);
            }
        }
        GLAccount fundSourceAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);
        if (isContraTransaction) {
            createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, entryDate, totalDebitAmount);
        } else {
            createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, entryDate, totalDebitAmount);
        }
    }

    private GLAccount getLinkedFinAccountForLoanProduct(final Long loanProductId, final CASH_ACCOUNTS_FOR_LOAN finAccountType) {
        ProductToGLAccountMapping accountMapping = accountMappingRepository.findByProductIdAndProductTypeAndFinancialAccountType(
                loanProductId, PortfolioProductType.LOAN.getValue(), finAccountType.getValue());
        return accountMapping.getGlAccount();
    }

    private void createCreditJournalEntryForLoanProduct(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date entryDate, final BigDecimal amount) {
        GLJournalEntry journalEntry = GLJournalEntry.createNew(office, account, transactionId, true, entryDate, JournalEntryType.CREDIT,
                amount, null, PortfolioProductType.LOAN.toString(), loanId);
        glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void createDebitJournalEntryForLoanProduct(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date entryDate, final BigDecimal amount) {
        GLJournalEntry journalEntry = GLJournalEntry.createNew(office, account, transactionId, true, entryDate, JournalEntryType.DEBIT,
                amount, null, PortfolioProductType.LOAN.toString(), loanId);
        glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void validateBusinessRulesForJournalEntries(final GLJournalEntryCommand command) {

        /** check if date of Journal entry is valid ***/
        final LocalDate entryLocalDate = command.getEntryDate();
        Date entryDate = entryLocalDate.toDateMidnight().toDate();
        // shouldn't be in the future
        Date todaysDate = new Date();
        if (entryDate.after(todaysDate)) { throw new GLJournalEntryInvalidException(GL_JOURNAL_ENTRY_INVALID_REASON.FUTURE_DATE, entryDate,
                null, null); }
        // shouldn't be before an accounting closure
        GLClosure latestGLClosure = glClosureRepository.getLatestGLClosureByBranch(command.getOfficeId());
        if (latestGLClosure != null) {
            if (latestGLClosure.getClosingDate().after(entryDate) || latestGLClosure.getClosingDate().equals(entryDate)) { throw new GLJournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(), null, null); }
        }
        /*** check if credits and debits are valid **/
        SingleDebitOrCreditEntryCommand[] credits = command.getCredits();
        SingleDebitOrCreditEntryCommand[] debits = command.getDebits();

        // atleast one debit or credit must be present
        if ((credits == null || credits.length <= 0) || (debits == null || debits.length <= 0)) { throw new GLJournalEntryInvalidException(
                GL_JOURNAL_ENTRY_INVALID_REASON.NO_DEBITS_OR_CREDITS, null, null, null); }

        // sum of all debits must be = sum of all credits
        BigDecimal creditsSum = BigDecimal.ZERO;
        BigDecimal debitsSum = BigDecimal.ZERO;
        for (SingleDebitOrCreditEntryCommand creditEntryCommand : credits) {
            if (creditEntryCommand.getAmount() == null || creditEntryCommand.getGlAccountId() == null) { throw new GLJournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, null, null, null); }
            creditsSum = creditsSum.add(creditEntryCommand.getAmount());
        }
        for (SingleDebitOrCreditEntryCommand debitEntryCommand : debits) {
            if (debitEntryCommand.getAmount() == null || debitEntryCommand.getGlAccountId() == null) { throw new GLJournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, null, null, null); }
            debitsSum = debitsSum.add(debitEntryCommand.getAmount());
        }
        if (creditsSum.compareTo(debitsSum) != 0) { throw new GLJournalEntryInvalidException(
                GL_JOURNAL_ENTRY_INVALID_REASON.DEBIT_CREDIT_SUM_MISMATCH, null, null, null); }
    }

    private void saveAllDebitOrCreditEntries(final GLJournalEntryCommand command, final Office office, final Date entryDate,
            final SingleDebitOrCreditEntryCommand[] singleDebitOrCreditEntryCommands, final String transactionId,
            final JournalEntryType type) {
        for (SingleDebitOrCreditEntryCommand singleDebitOrCreditEntryCommand : singleDebitOrCreditEntryCommands) {
            GLAccount glAccount = glAccountRepository.findOne(singleDebitOrCreditEntryCommand.getGlAccountId());
            if (glAccount == null) { throw new GLAccountNotFoundException(singleDebitOrCreditEntryCommand.getGlAccountId()); }

            /***
             * validate that the account allows manual adjustments and is not
             * disabled
             **/
            if (glAccount.isDisabled()) {
                throw new GLJournalEntryInvalidException(GL_JOURNAL_ENTRY_INVALID_REASON.GL_ACCOUNT_DISABLED, null, glAccount.getName(),
                        glAccount.getGlCode());
            } else if (!glAccount.isManualEntriesAllowed()) { throw new GLJournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED, null, glAccount.getName(),
                    glAccount.getGlCode()); }

            String comments = command.getComments();
            if (!StringUtils.isBlank(singleDebitOrCreditEntryCommand.getComments())) {
                comments = singleDebitOrCreditEntryCommand.getComments();
            }
            GLJournalEntry glJournalEntry = GLJournalEntry.createNew(office, glAccount, transactionId, false, entryDate, type,
                    singleDebitOrCreditEntryCommand.getAmount(), comments, null, null);
            glJournalEntryRepository.saveAndFlush(glJournalEntry);
        }
    }

    private void handleJournalEntryDataIntegrityIssues(final DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glJournalEntry.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Journal Entry: " + realCause.getMessage());
    }

    /**
     * TODO: Need a better implementation with guaranteed uniqueness (but not a
     * long UUID)...maybe something tied to system clock..
     */
    private String generateTransactionId() {
        return RandomStringUtils.random(15, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    }
}