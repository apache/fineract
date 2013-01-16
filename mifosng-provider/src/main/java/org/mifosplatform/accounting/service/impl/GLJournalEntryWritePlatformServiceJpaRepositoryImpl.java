package org.mifosplatform.accounting.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.AccountingConstants.JOURNAL_ENTRY_TYPE;
import org.mifosplatform.accounting.AccountingConstants.PORTFOLIO_PRODUCT_TYPE;
import org.mifosplatform.accounting.api.commands.GLJournalEntryCommand;
import org.mifosplatform.accounting.api.commands.SingleDebitOrCreditEntryCommand;
import org.mifosplatform.accounting.domain.GLAccount;
import org.mifosplatform.accounting.domain.GLAccountRepository;
import org.mifosplatform.accounting.domain.GLClosure;
import org.mifosplatform.accounting.domain.GLClosureRepository;
import org.mifosplatform.accounting.domain.GLJournalEntry;
import org.mifosplatform.accounting.domain.GLJournalEntryRepository;
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
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
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
    public String createJournalEntry(GLJournalEntryCommand command) {
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

            saveAllDebitOrCreditEntries(command, office, entryDate, command.getDebits(), transactionId, JOURNAL_ENTRY_TYPE.DEBIT);

            saveAllDebitOrCreditEntries(command, office, entryDate, command.getCredits(), transactionId, JOURNAL_ENTRY_TYPE.CREDIT);

            return transactionId;
        } catch (DataIntegrityViolationException dve) {
            handleJournalEntryDataIntegrityIssues(dve);
            return null;
        }
    }

    @Transactional
    @Override
    public String revertJournalEntry(String transactionId) {

        // is the transaction Id valid
        List<GLJournalEntry> journalEntries = glJournalEntryRepository.findUnReversedManualJournalEntriesByTransactionId(transactionId);

        if (journalEntries.size() <= 1) { throw new GLJournalEntriesNotFoundException(transactionId); }

        String reversalTransactionId = generateTransactionId();

        for (GLJournalEntry journalEntry : journalEntries) {
            GLJournalEntry reversalJournalEntry;
            String reversalComment = "Reversal entry for Journal Entry with Entry Id  :" + journalEntry.getId() + " and transaction Id "
                    + transactionId;
            if (journalEntry.getType().equalsIgnoreCase(JOURNAL_ENTRY_TYPE.DEBIT.toString())) {
                reversalJournalEntry = GLJournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(),
                        reversalTransactionId, false, journalEntry.getEntryDate(), JOURNAL_ENTRY_TYPE.CREDIT.toString(),
                        journalEntry.getAmount(), reversalComment, null, null);
            } else {
                reversalJournalEntry = GLJournalEntry.createNew(journalEntry.getOffice(), journalEntry.getGlAccount(),
                        reversalTransactionId, false, journalEntry.getEntryDate(), JOURNAL_ENTRY_TYPE.DEBIT.toString(),
                        journalEntry.getAmount(), reversalComment, null, null);
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
    public void createJournalEntriesForLoan(Loan loan, List<LoanTransaction> loanTransactions) {
        // shouldn't be before an accounting closure
        GLClosure latestGLClosure = glClosureRepository.getLatestGLClosureByBranch(loan.getOfficeId());
        Office office = loan.getClient().getOffice();
        Long loanProductId = loan.loanProduct().getId();
        // TODO: Check for accounting type
        for (LoanTransaction loanTransaction : loanTransactions) {
            Date entryDate = loanTransaction.getDateOf();
            String transactionId = loanTransaction.getId().toString();
            Long loanId = loan.getId();
            /**
             * check if an accounting closure has happened for this branch after
             * the transaction Date
             **/
            if (latestGLClosure != null) {
                if (latestGLClosure.getClosingDate().after(loanTransaction.getDateOf())) { throw new GLJournalEntryInvalidException(
                        GL_JOURNAL_ENTRY_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate(), null, null); }
            }
            /*** Debit loan Portfolio and credit Fund source for Disbursal **/
            if (loanTransaction.isDisbursement()) {

                GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
                GLAccount fundSourceAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);

                BigDecimal disbursalAmount = loanTransaction.getAmount();
                createDebitJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, entryDate, disbursalAmount);
                createCreditJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, entryDate, disbursalAmount);
            }/***
             * Create a single Debit to fund source and multiple credits if
             * applicable (loan portfolio for principal repayment, Interest on
             * loans for interest repayments, Income from fees for fees payment
             * and Income from penalties for penalty payment)
             **/
            else if (loanTransaction.isRepayment() || loanTransaction.isRepaymentAtDisbursement()) {
                BigDecimal principalAmount = loanTransaction.getPrincipalPortion(loan.getCurrency()).getAmount();
                BigDecimal interestAmount = loanTransaction.getInterestPortion(loan.getCurrency()).getAmount();
                BigDecimal feesAmount = loanTransaction.getFeeChargesPortion(loan.getCurrency()).getAmount();
                BigDecimal penaltiesAmount = loanTransaction.getPenaltyChargesPortion(loan.getCurrency()).getAmount();

                BigDecimal totalDebitAmount = new BigDecimal(0);

                if (!(principalAmount.compareTo(BigDecimal.ZERO) == 0)) {
                    totalDebitAmount = totalDebitAmount.add(principalAmount);

                    GLAccount loanPortfolioAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.LOAN_PORTFOLIO);
                    createCreditJournalEntryForLoanProduct(office, loanPortfolioAccount, loanId, transactionId, entryDate, principalAmount);
                }
                if (!(interestAmount.compareTo(BigDecimal.ZERO) == 0)) {
                    totalDebitAmount = totalDebitAmount.add(interestAmount);

                    GLAccount interestAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.INTEREST_ON_LOANS);
                    createCreditJournalEntryForLoanProduct(office, interestAccount, loanId, transactionId, entryDate, interestAmount);
                }
                if (!(feesAmount.compareTo(BigDecimal.ZERO) == 0)) {
                    totalDebitAmount = totalDebitAmount.add(feesAmount);

                    GLAccount incomeFromFeesAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                            CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_FEES);
                    createCreditJournalEntryForLoanProduct(office, incomeFromFeesAccount, loanId, transactionId, entryDate, feesAmount);
                }
                if (!(penaltiesAmount.compareTo(BigDecimal.ZERO) == 0)) {
                    totalDebitAmount = totalDebitAmount.add(penaltiesAmount);

                    GLAccount incomeFromPenaltiesAccount = getLinkedFinAccountForLoanProduct(loanProductId,
                            CASH_ACCOUNTS_FOR_LOAN.INCOME_FROM_PENALTIES);
                    createCreditJournalEntryForLoanProduct(office, incomeFromPenaltiesAccount, loanId, transactionId, entryDate,
                            penaltiesAmount);
                }
                GLAccount fundSourceAccount = getLinkedFinAccountForLoanProduct(loanProductId, CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE);
                createDebitJournalEntryForLoanProduct(office, fundSourceAccount, loanId, transactionId, entryDate, totalDebitAmount);
            } // TODO Vishwas: Add conditions for write off, contra waive
              // interest
              // and waive charges
        }

    }

    @Transactional
    @Override
    public void createJournalEntriesForLoan(Loan loan, LoanTransaction loanTransaction) {
        List<LoanTransaction> loanTransactions = new ArrayList<LoanTransaction>();
        loanTransactions.add(loanTransaction);
        createJournalEntriesForLoan(loan, loanTransactions);
    }

    private GLAccount getLinkedFinAccountForLoanProduct(Long loanProductId, CASH_ACCOUNTS_FOR_LOAN finAccountType) {
        ProductToGLAccountMapping accountMapping = accountMappingRepository.findByProductIdAndProductTypeAndFinancialAccountType(
                loanProductId, PORTFOLIO_PRODUCT_TYPE.LOAN.getValue(), finAccountType.getValue());
        return accountMapping.getGlAccount();
    }

    private void createCreditJournalEntryForLoanProduct(Office office, GLAccount account, Long loanId, String transactionId,
            Date entryDate, BigDecimal amount) {
        GLJournalEntry journalEntry = GLJournalEntry.createNew(office, account, transactionId, true, entryDate,
                JOURNAL_ENTRY_TYPE.CREDIT.toString(), amount, null, PORTFOLIO_PRODUCT_TYPE.LOAN.toString(), loanId);
        glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private void createDebitJournalEntryForLoanProduct(Office office, GLAccount account, Long loanId, String transactionId, Date entryDate,
            BigDecimal amount) {
        GLJournalEntry journalEntry = GLJournalEntry.createNew(office, account, transactionId, true, entryDate,
                JOURNAL_ENTRY_TYPE.DEBIT.toString(), amount, null, PORTFOLIO_PRODUCT_TYPE.LOAN.toString(), loanId);
        glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    /**
     * @param command
     */
    private void validateBusinessRulesForJournalEntries(GLJournalEntryCommand command) {

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
            if (latestGLClosure.getClosingDate().after(entryDate)) { throw new GLJournalEntryInvalidException(
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

    /**
     * @param command
     * @param office
     * @param entryDate
     * @param singleDebitOrCreditEntryCommands
     * @param transactionId
     */
    private void saveAllDebitOrCreditEntries(GLJournalEntryCommand command, final Office office, Date entryDate,
            SingleDebitOrCreditEntryCommand[] singleDebitOrCreditEntryCommands, String transactionId, JOURNAL_ENTRY_TYPE type) {
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
            GLJournalEntry glJournalEntry = GLJournalEntry.createNew(office, glAccount, transactionId, false, entryDate, type.toString(),
                    singleDebitOrCreditEntryCommand.getAmount(), comments, null, null);
            glJournalEntryRepository.saveAndFlush(glJournalEntry);
        }
    }

    /**
     * @param command
     * @param dve
     */
    private void handleJournalEntryDataIntegrityIssues(DataIntegrityViolationException dve) {
        Throwable realCause = dve.getMostSpecificCause();
        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glJournalEntry.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource Journal Entry: " + realCause.getMessage());
    }

    /**
     * TODO: Need a better implementation with guaranteed uniqueness (but not a
     * long UUID)...maybe something tied to system clock..
     * 
     * @return
     */
    private String generateTransactionId() {
        return RandomStringUtils.random(15, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
    }

}
