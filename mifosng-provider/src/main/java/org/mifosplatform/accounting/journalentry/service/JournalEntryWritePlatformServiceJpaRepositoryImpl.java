/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.closure.domain.GLClosure;
import org.mifosplatform.accounting.closure.domain.GLClosureRepository;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepository;
import org.mifosplatform.accounting.glaccount.exception.GLAccountNotFoundException;
import org.mifosplatform.accounting.journalentry.api.JournalEntryJsonInputParams;
import org.mifosplatform.accounting.journalentry.command.JournalEntryCommand;
import org.mifosplatform.accounting.journalentry.command.SingleDebitOrCreditEntryCommand;
import org.mifosplatform.accounting.journalentry.data.LoanDTO;
import org.mifosplatform.accounting.journalentry.domain.JournalEntry;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryRepository;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.accounting.journalentry.exception.JournalEntriesNotFoundException;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException;
import org.mifosplatform.accounting.journalentry.exception.JournalEntryInvalidException.GL_JOURNAL_ENTRY_INVALID_REASON;
import org.mifosplatform.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
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
    private final AccountingProcessorForLoanFactory accountingProcessorForLoanFactory;
    private final AccountingProcessorHelper helper;
    private final JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public JournalEntryWritePlatformServiceJpaRepositoryImpl(final GLClosureRepository glClosureRepository,
            final JournalEntryRepository glJournalEntryRepository, final OfficeRepository officeRepository,
            final GLAccountRepository glAccountRepository, final JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final AccountingProcessorHelper accountingProcessorHelper,
            final AccountingProcessorForLoanFactory accountingProcessorForLoanFactory) {
        this.glClosureRepository = glClosureRepository;
        this.officeRepository = officeRepository;
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.glAccountRepository = glAccountRepository;
        this.accountingProcessorForLoanFactory = accountingProcessorForLoanFactory;
        this.helper = accountingProcessorHelper;
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
        final boolean manualEntry = true;

        for (final JournalEntry journalEntry : journalEntries) {
            JournalEntry reversalJournalEntry;
            final String reversalComment = "Reversal entry for Journal Entry with Entry Id  :" + journalEntry.getId()
                    + " and transaction Id " + command.getTransactionId();
            if (journalEntry.isDebitEntry()) {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(), reversalTransactionId,
                        manualEntry, journalEntry.getTransactionDate(), JournalEntryType.CREDIT, journalEntry.getAmount(), reversalComment,
                        null, null);
            } else {
                reversalJournalEntry = JournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(), reversalTransactionId,
                        manualEntry, journalEntry.getTransactionDate(), JournalEntryType.DEBIT, journalEntry.getAmount(), reversalComment,
                        null, null);
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
    public void createJournalEntriesForLoan(final Map<String, Object> accountingBridgeData) {

        final boolean cashBasedAccountingEnabled = (Boolean) accountingBridgeData.get("cashBasedAccountingEnabled");
        final boolean accrualBasedAccountingEnabled = (Boolean) accountingBridgeData.get("accrualBasedAccountingEnabled");

        if (cashBasedAccountingEnabled || accrualBasedAccountingEnabled) {
            final LoanDTO loanDTO = helper.populateLoanDtoFromMap(accountingBridgeData, cashBasedAccountingEnabled,
                    accrualBasedAccountingEnabled);
            final AccountingProcessorForLoan accountingProcessorForLoan = this.accountingProcessorForLoanFactory
                    .determineProcessor(loanDTO);
            accountingProcessorForLoan.createJournalEntriesForLoan(loanDTO);
        }
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
        final boolean manualEntry = true;
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
            final JournalEntry glJournalEntry = JournalEntry.createNew(office, glAccount, transactionId, manualEntry, transactionDate,
                    type, singleDebitOrCreditEntryCommand.getAmount(), comments, null, null);
            this.glJournalEntryRepository.saveAndFlush(glJournalEntry);
        }
    }

    /**
     * TODO: Need a better implementation with guaranteed uniqueness (but not a
     * long UUID)...maybe something tied to system clock..
     */
    private String generateTransactionId() {
        return RandomStringUtils.random(15, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    }

    private void handleJournalEntryDataIntegrityIssues(final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glJournalEntry.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Journal Entry: " + realCause.getMessage());
    }

}
