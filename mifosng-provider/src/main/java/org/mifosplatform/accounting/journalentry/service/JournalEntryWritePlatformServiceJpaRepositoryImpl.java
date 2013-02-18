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

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.closure.domain.GLClosureRepository;
import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.glaccount.data.LoanDTO;
import org.mifosplatform.accounting.glaccount.data.LoanTransactionDTO;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepository;
import org.mifosplatform.accounting.glaccount.exception.GLAccountNotFoundException;
import org.mifosplatform.accounting.journalentry.api.JournalEntryJsonInputParams;
import org.mifosplatform.accounting.journalentry.command.JournalEntryCommand;
import org.mifosplatform.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.mifosplatform.accounting.journalentry.domain.JournalEntry;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryRepository;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.accounting.journalentry.exception.JournalEntriesNotFoundException;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException.GL_JOURNAL_ENTRY_INVALID_REASON;
import org.mifosplatform.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
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
public class JournalEntryWritePlatformServiceJpaRepositoryImpl implements JournalEntryWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(JournalEntryWritePlatformServiceJpaRepositoryImpl.class);

    private final GLClosureRepository glClosureRepository;
    private final GLAccountRepository glAccountRepository;
    private final JournalEntryRepository glJournalEntryRepository;
    private final OfficeRepository officeRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public JournalEntryWritePlatformServiceJpaRepositoryImpl(final GLClosureRepository glClosureRepository,
            final GLAccountRepository glAccountRepository, final JournalEntryRepository glJournalEntryRepository,
            final OfficeRepository officeRepository, final ProductToGLAccountMappingRepository accountMappingRepository,
            final JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.glClosureRepository = glClosureRepository;
        this.officeRepository = officeRepository;
        this.glAccountRepository = glAccountRepository;
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult createJournalEntry(final JsonCommand command) {
        try {
            final JournalEntryCommand journalEntryCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            journalEntryCommand.validateForCreate();

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue());
            final Office office = this.officeRepository.findOne(officeId);
            if (office == null) { throw new OfficeNotFoundException(officeId); }

            validateBusinessRulesForJournalEntries(journalEntryCommand);

            /** Set a transaction Id and save these Journal entries **/
            final Date transactionDate = command.DateValueOfParameterNamed(JournalEntryJsonInputParams.TRANSACTION_DATE.getValue());
            final String transactionId = generateTransactionId();

            saveAllDebitOrCreditEntries(journalEntryCommand, office, transactionDate, journalEntryCommand.getDebits(), transactionId,
                    JournalEntryType.DEBIT);

            saveAllDebitOrCreditEntries(journalEntryCommand, office, transactionDate, journalEntryCommand.getCredits(), transactionId,
                    JournalEntryType.CREDIT);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withTransactionId(transactionId).build();
        } catch (final DataIntegrityViolationException dve) {
            handleJournalEntryDataIntegrityIssues(dve);
            return null;
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult revertJournalEntry(final JsonCommand command) {
        // is the transaction Id valid
        final List<JournalEntry> journalEntries = this.glJournalEntryRepository.findUnReversedManualJournalEntriesByTransactionId(command
                .getTransactionId());

        if (journalEntries.size() <= 1) { throw new JournalEntriesNotFoundException(command.getTransactionId()); }

        final String reversalTransactionId = generateTransactionId();

        for (final JournalEntry journalEntry : journalEntries) {
            JournalEntry reversalJournalEntry;
            final String reversalComment = "Reversal entry for Journal Entry with Entry Id  :" + journalEntry.getId()
                    + " and transaction Id " + command.getTransactionId();
            if (journalEntry.isDebitEntry()) {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(), reversalTransactionId,
                        false, journalEntry.getTransactionDate(), JournalEntryType.CREDIT, journalEntry.getAmount(), reversalComment, null,
                        null);
            } else {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(), reversalTransactionId,
                        false, journalEntry.getTransactionDate(), JournalEntryType.DEBIT, journalEntry.getAmount(), reversalComment, null,
                        null);
            }
            // save the reversal entry
            this.glJournalEntryRepository.saveAndFlush(reversalJournalEntry);
            journalEntry.setReversed(true);
            journalEntry.setReversalJournalEntry(reversalJournalEntry);
            // save the updated journal entry
            this.glJournalEntryRepository.saveAndFlush(journalEntry);
        }
        return new CommandProcessingResultBuilder().withTransactionId(reversalTransactionId).build();
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

        for (final Map<String, Object> map : newTransactionsMap) {
            final String transactionId = ((Long) map.get("id")).toString();
            final Date transactionDate = ((LocalDate) map.get("date")).toDate();
            final BigDecimal amount = (BigDecimal) map.get("amount");
            final BigDecimal principal = (BigDecimal) map.get("principalPortion");
            final BigDecimal interest = (BigDecimal) map.get("interestPortion");
            final BigDecimal fees = (BigDecimal) map.get("feeChargesPortion");
            final BigDecimal penalties = (BigDecimal) map.get("penaltyChargesPortion");
            final boolean disbursement = (Boolean) map.get("disbursement");
            final boolean repayment = (Boolean) map.get("repayment");
            final boolean repaymentAtDisbursement = (Boolean) map.get("repaymentAtDisbursement");
            final boolean contra = (Boolean) map.get("contra");
            final boolean writeOff = (Boolean) map.get("writeOff");

            final LoanTransactionDTO transaction = new LoanTransactionDTO(transactionId, transactionDate, amount, principal, interest,
                    fees, penalties, disbursement, repayment, repaymentAtDisbursement, contra, writeOff);

            newLoanTransactions.add(transaction);

        }

        return new LoanDTO(loanId, loanProductId, officeId, cashBasedAccountingEnabled, accrualBasedAccountingEnabled, newLoanTransactions);
    }

    private void createJournalEntriesUsingCashRules(final LoanDTO loanDTO) {
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(loanDTO.getOfficeId());
        final Office office = this.officeRepository.findOne(loanDTO.getOfficeId());
        final Long loanProductId = loanDTO.getLoanProductId();
        // TODO: Check for accounting type
        for (final LoanTransactionDTO loanTransactionDTO : loanDTO.getNewLoanTransactions()) {
            final Date transactionDate = loanTransactionDTO.getTransactionDate();
            final String transactionId = loanTransactionDTO.getTransactionId();
            final Long loanId = loanDTO.getLoanId();
            /**
             * check if an accounting closure has happened for this branch after
             * the transaction Date
             **/
            if (latestGLClosure != null) {
                if (latestGLClosure.getClosingDate().after(transactionDate) || latestGLClosure.getClosingDate().equals(transactionDate)) { throw new JournalEntryInvalidException(
                        GL_JOURNAL_ENTRY_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(), null, null); }
            }

            /*** Debit loan Portfolio and credit Fund source for Disbursal **/
            if (loanTransactionDTO.isDisbursement() && !loanTransactionDTO.isContra()) {

                final GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
                final GLAccount fundSourceAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);

                final BigDecimal disbursalAmount = loanTransactionDTO.getAmount();
                createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate, disbursalAmount);
                createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate, disbursalAmount);
            } else if (loanTransactionDTO.isRepayment() || loanTransactionDTO.isRepaymentAtDisbursement() || loanTransactionDTO.isContra()) {
                createJournalEntriesForRepaymentOrContraUsingCashRules(office, loanProductId, loanTransactionDTO, transactionDate,
                        transactionId, loanId);
            }/***
             * Only principal write off affects cash based accounting (interest
             * and fee write off need not be considered). Debit losses written
             * off and credit Loan Portfolio
             **/
            else if (loanTransactionDTO.isWriteOff()) {
                final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
                final GLAccount lossesWrittenOffAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOSSES_WRITTEN_OFF);
                createDebitJournalEntryForLoanProduct(office, lossesWrittenOffAccount, loanId, transactionId, transactionDate,
                        principalAmount);

                final GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                        CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
                createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
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
            final LoanTransactionDTO loanTransactionDTO, final Date transactionDate, String transactionId, final Long loanId) {
        final BigDecimal principalAmount = loanTransactionDTO.getPrincipal();
        final BigDecimal interestAmount = loanTransactionDTO.getInterest();
        final BigDecimal feesAmount = loanTransactionDTO.getFees();
        final BigDecimal penaltiesAmount = loanTransactionDTO.getPenalties();
        final boolean isContraTransaction = loanTransactionDTO.isContra();

        BigDecimal totalDebitAmount = new BigDecimal(0);

        if (isContraTransaction) {
            transactionId = "reversal-" + transactionId;
        }

        if (principalAmount != null && !(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(principalAmount);

            final GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate, principalAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, transactionDate,
                        principalAmount);
            }
        }
        if (interestAmount != null && !(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(interestAmount);

            final GLAccount interestAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate, interestAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, transactionDate, interestAmount);
            }
        }
        if (feesAmount != null && !(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(feesAmount);

            final GLAccount incomeFromFeesAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate, feesAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, transactionDate, feesAmount);
            }
        }
        if (penaltiesAmount != null && !(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
            totalDebitAmount = totalDebitAmount.add(penaltiesAmount);

            final GLAccount incomeFromPenaltiesAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                    CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES);
            if (isContraTransaction) {
                createDebitJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            } else {
                createCreditJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, transactionDate,
                        penaltiesAmount);
            }
        }
        final GLAccount fundSourceAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);
        if (isContraTransaction) {
            createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate, totalDebitAmount);
        } else {
            createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, transactionDate, totalDebitAmount);
        }
    }

    private GLAccount getLinkedFinAccountForLoanProduct(final Long loanProductId, final CASH_ACCOUNTS_FOR_LOAN finAccountType) {
        final ProductToGLAccountMapping accountMapping = this.accountMappingRepository
                .findByProductIdAndProductTypeAndFinancialAccountType(loanProductId, PortfolioProductType.LOAN.getValue(),
                        finAccountType.getValue());
        return accountMapping.getGlAccount();
    }

    private void createCreditJournalEntryForLoanProduct(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final JournalEntry journalEntry = JournalEntry.createNew(office, account, transactionId, true, transactionDate,
                JournalEntryType.CREDIT, amount, null, PortfolioProductType.LOAN.toString(), loanId);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void createDebitJournalEntryForLoanProduct(final Office office, final GLAccount account, final Long loanId,
            final String transactionId, final Date transactionDate, final BigDecimal amount) {
        final JournalEntry journalEntry = JournalEntry.createNew(office, account, transactionId, true, transactionDate,
                JournalEntryType.DEBIT, amount, null, PortfolioProductType.LOAN.toString(), loanId);
        this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void validateBusinessRulesForJournalEntries(final JournalEntryCommand command) {
        /** check if date of Journal entry is valid ***/
        final LocalDate entryLocalDate = command.getTransactionDate();
        final Date transactionDate = entryLocalDate.toDateMidnight().toDate();
        // shouldn't be in the future
        final Date todaysDate = new Date();
        if (transactionDate.after(todaysDate)) { throw new JournalEntryInvalidException(GL_JOURNAL_ENTRY_INVALID_REASON.FUTURE_DATE,
                transactionDate, null, null); }
        // shouldn't be before an accounting closure
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(command.getOfficeId());
        if (latestGLClosure != null) {
            if (latestGLClosure.getClosingDate().after(transactionDate) || latestGLClosure.getClosingDate().equals(transactionDate)) { throw new JournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(), null, null); }
        }
        /*** check if credits and debits are valid **/
        final SingleDebitOrCreditEntryCommand[] credits = command.getCredits();
        final SingleDebitOrCreditEntryCommand[] debits = command.getDebits();

        // atleast one debit or credit must be present
        if ((credits == null || credits.length <= 0) || (debits == null || debits.length <= 0)) { throw new JournalEntryInvalidException(
                GL_JOURNAL_ENTRY_INVALID_REASON.NO_DEBITS_OR_CREDITS, null, null, null); }

        // sum of all debits must be = sum of all credits
        BigDecimal creditsSum = BigDecimal.ZERO;
        BigDecimal debitsSum = BigDecimal.ZERO;
        for (final SingleDebitOrCreditEntryCommand creditEntryCommand : credits) {
            if (creditEntryCommand.getAmount() == null || creditEntryCommand.getGlAccountId() == null) { throw new JournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, null, null, null); }
            creditsSum = creditsSum.add(creditEntryCommand.getAmount());
        }
        for (final SingleDebitOrCreditEntryCommand debitEntryCommand : debits) {
            if (debitEntryCommand.getAmount() == null || debitEntryCommand.getGlAccountId() == null) { throw new JournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.DEBIT_CREDIT_ACCOUNT_OR_AMOUNT_EMPTY, null, null, null); }
            debitsSum = debitsSum.add(debitEntryCommand.getAmount());
        }
        if (creditsSum.compareTo(debitsSum) != 0) { throw new JournalEntryInvalidException(
                GL_JOURNAL_ENTRY_INVALID_REASON.DEBIT_CREDIT_SUM_MISMATCH, null, null, null); }
    }

    private void saveAllDebitOrCreditEntries(final JournalEntryCommand command, final Office office, final Date transactionDate,
            final SingleDebitOrCreditEntryCommand[] singleDebitOrCreditEntryCommands, final String transactionId,
            final JournalEntryType type) {
        for (final SingleDebitOrCreditEntryCommand singleDebitOrCreditEntryCommand : singleDebitOrCreditEntryCommands) {
            final GLAccount glAccount = this.glAccountRepository.findOne(singleDebitOrCreditEntryCommand.getGlAccountId());
            if (glAccount == null) { throw new GLAccountNotFoundException(singleDebitOrCreditEntryCommand.getGlAccountId()); }

            /***
             * validate that the account allows manual adjustments and is not
             * disabled
             **/
            if (glAccount.isDisabled()) {
                throw new JournalEntryInvalidException(GL_JOURNAL_ENTRY_INVALID_REASON.GL_ACCOUNT_DISABLED, null, glAccount.getName(),
                        glAccount.getGlCode());
            } else if (!glAccount.isManualEntriesAllowed()) { throw new JournalEntryInvalidException(
                    GL_JOURNAL_ENTRY_INVALID_REASON.GL_ACCOUNT_MANUAL_ENTRIES_NOT_PERMITTED, null, glAccount.getName(),
                    glAccount.getGlCode()); }

            String comments = command.getComments();
            if (!StringUtils.isBlank(singleDebitOrCreditEntryCommand.getComments())) {
                comments = singleDebitOrCreditEntryCommand.getComments();
            }
            final JournalEntry glJournalEntry = JournalEntry.createNew(office, glAccount, transactionId, false, transactionDate, type,
                    singleDebitOrCreditEntryCommand.getAmount(), comments, null, null);
            this.glJournalEntryRepository.saveAndFlush(glJournalEntry);
        }
    }

    private void handleJournalEntryDataIntegrityIssues(final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
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
