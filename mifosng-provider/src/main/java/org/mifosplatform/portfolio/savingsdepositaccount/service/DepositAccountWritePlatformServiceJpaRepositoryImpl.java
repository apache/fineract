package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.loanaccount.command.UndoStateTransitionCommand;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingsProductNotFoundException;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommandValidator;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawInterestCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountWithdrawalCommandValidator;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionApprovalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionApprovalCommandValidator;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.command.WithDrawDepositAccountInterestCommandValidator;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountsForLookup;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccount;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountRepository;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachineImpl;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.FixedTermDepositInterestCalculator;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccountNotFoundException;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccountReopenException;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccountTransactionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositAccountWritePlatformServiceJpaRepositoryImpl implements DepositAccountWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(DepositAccountWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final DepositAccountRepository depositAccountRepository;
    private final DepositAccountAssembler depositAccountAssembler;
    private final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator;
    private final NoteRepository noteRepository;

    @Autowired
    public DepositAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final DepositAccountRepository depositAccountRepository, final DepositAccountAssembler depositAccountAssembler,
            final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator, final NoteRepository noteRepository) {
        this.context = context;
        this.depositAccountRepository = depositAccountRepository;
        this.depositAccountAssembler = depositAccountAssembler;
        this.fixedTermDepositInterestCalculator = fixedTermDepositInterestCalculator;
        this.noteRepository = noteRepository;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final DepositAccountCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("deposit_acc_external_id")) { throw new PlatformDataIntegrityException(
                "error.msg.desposit.account.duplicate.externalId", "Deposit account with externalId " + command.getExternalId()
                        + " already exists", "externalId", command.getExternalId()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.deposit.account.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createDepositAccount(final DepositAccountCommand command) {

        try {
            this.context.authenticatedUser();

            DepositAccountCommandValidator validator = new DepositAccountCommandValidator(command);
            validator.validateForCreate();

            final DepositAccount account = this.depositAccountAssembler.assembleFrom(command);
            this.depositAccountRepository.save(account);

            return new CommandProcessingResult(account.getId());
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDepositAccount(final Long accountId) {

        this.context.authenticatedUser();

        DepositAccount account = this.depositAccountRepository.findOne(accountId);
        if (account == null || account.isDeleted()) { throw new SavingsProductNotFoundException(accountId); }

        account.delete();
        this.depositAccountRepository.save(account);

        return new CommandProcessingResult(accountId);
    }

    @Transactional
    @Override
    public CommandProcessingResult approveDepositApplication(final DepositStateTransitionApprovalCommand command) {

        // AppUser currentUser = context.authenticatedUser();

        DepositStateTransitionApprovalCommandValidator validator = new DepositStateTransitionApprovalCommandValidator(command);
        validator.validate();

        DepositAccount account = this.depositAccountRepository.findOne(command.getAccountId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getAccountId()); }

        // FIXME - madhukar - you are checking for loan permission here instead
        // of some specific deposit account permission.
        // removing check for now until rules dealing with creating and
        // maintaining deposit accounts in the past is clarified
        LocalDate eventDate = command.getEventDate();
        // if (this.isBeforeToday(eventDate) &&
        // currentUser.canNotApproveLoanInPast()) {
        // throw new
        // NoAuthorizationException("User has no authority to approve deposit with a date in the past.");
        // }

        account.approve(eventDate, defaultDepositLifecycleStateMachine(), command, this.fixedTermDepositInterestCalculator);

        this.depositAccountRepository.save(account);

        String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResult(account.getId());

    }

    // private boolean isBeforeToday(final LocalDate date) {
    // return date.isBefore(new LocalDate());
    // }
    private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectDepositApplication(DepositStateTransitionCommand command) {

        // AppUser currentUser = context.authenticatedUser();

        DepositStateTransitionCommandValidator validator = new DepositStateTransitionCommandValidator(command);
        validator.validate();

        DepositAccount account = this.depositAccountRepository.findOne(command.getAccountId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getAccountId()); }

        // removing check for now until rules dealing with creating and
        // maintaining deposit accounts in the past is clarified
        LocalDate eventDate = command.getEventDate();
        // if (this.isBeforeToday(eventDate) &&
        // currentUser.canNotApproveLoanInPast()) {
        // throw new
        // NoAuthorizationException("User has no authority to approve deposit with a date in the past.");
        // }

        account.reject(eventDate, defaultDepositLifecycleStateMachine());
        this.depositAccountRepository.save(account);

        String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResult(account.getId());
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawDepositApplication(DepositStateTransitionCommand command) {

        // AppUser currentUser = context.authenticatedUser();

        DepositStateTransitionCommandValidator validator = new DepositStateTransitionCommandValidator(command);
        validator.validate();

        DepositAccount account = this.depositAccountRepository.findOne(command.getAccountId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getAccountId()); }

        // removing check for now until rules dealing with creating and
        // maintaining deposit accounts in the past is clarified
        LocalDate eventDate = command.getEventDate();
        // if (this.isBeforeToday(eventDate) &&
        // currentUser.canNotApproveLoanInPast()) {
        // throw new
        // NoAuthorizationException("User has no authority to approve deposit with a date in the past.");
        // }

        account.withdrawnByApplicant(eventDate, defaultDepositLifecycleStateMachine());
        this.depositAccountRepository.save(account);

        String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResult(account.getId());
    }

    @Transactional
    @Override
    public CommandProcessingResult undoDepositApproval(UndoStateTransitionCommand command) {

        context.authenticatedUser();

        DepositAccount account = this.depositAccountRepository.findOne(command.getLoanId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getLoanId()); }

        account.undoDepositApproval(defaultDepositLifecycleStateMachine());
        this.depositAccountRepository.save(account);

        String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }
        return new CommandProcessingResult(account.getId());
    }

    /*
     * @Transactional
     * 
     * @Override public EntityIdentifier
     * matureDepositApplication(DepositStateTransitionCommand command) {
     * 
     * AppUser currentUser = context.authenticatedUser();
     * 
     * DepositStateTransitionCommandValidator validator = new
     * DepositStateTransitionCommandValidator(command); validator.validate();
     * 
     * DepositAccount account =
     * this.depositAccountRepository.findOne(command.getAccountId()); if
     * (account == null || account.isDeleted()) { throw new
     * DepositAccountNotFoundException(command.getAccountId()); }
     * 
     * LocalDate eventDate = command.getEventDate(); if
     * (this.isBeforeToday(eventDate) && currentUser.canNotApproveLoanInPast())
     * { throw new NoAuthorizationException(
     * "User has no authority to mature deposit with a date in the past."); }
     * 
     * account.matureDepositApplication(eventDate,
     * defaultDepositLifecycleStateMachine());
     * this.depositAccountRepository.save(account);
     * 
     * if(account.isRenewalAllowed()){ final DepositAccount renewedAccount =
     * this.depositAccountAssembler.assembleFrom(account);
     * this.depositAccountRepository.save(renewedAccount); return new
     * EntityIdentifier(renewedAccount.getId()); //returns the new deposit
     * application id }
     * 
     * String noteText = command.getNote(); if
     * (StringUtils.isNotBlank(noteText)) { Note note =
     * Note.depositNote(account, noteText); this.noteRepository.save(note); }
     * 
     * return new EntityIdentifier(account.getId()); }
     */

    @Transactional
    @Override
    public CommandProcessingResult withdrawDepositAccountMoney(final DepositAccountWithdrawalCommand command) {

        context.authenticatedUser();

        DepositAccountWithdrawalCommandValidator validator = new DepositAccountWithdrawalCommandValidator(command);
        validator.validate();

        DepositAccount account = this.depositAccountRepository.findOne(command.getAccountId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getAccountId()); }

        LocalDate eventDate = command.getMaturesOnDate();

        Integer lockinPeriod = account.getLockinPeriod();
        LocalDate lockinPeriodExpDate = account.getActualCommencementDate().plusMonths(Integer.valueOf(lockinPeriod));
        if (account.isLockinPeriodAllowed()) {
            if (eventDate.isBefore(lockinPeriodExpDate)) { throw new DepositAccountTransactionsException(
                    "deposit.transaction.canot.withdraw.before.lockinperiod.reached",
                    "You can not withdraw your application before maturity date reached"); }
        }
        // if (eventDate.isBefore(account.maturesOnDate())) {
        // this.depositAccountAssembler.adjustTotalAmountForPreclosureInterest(account,
        // eventDate);
        // }
        account.withdrawDepositAccountMoney(defaultDepositLifecycleStateMachine(), fixedTermDepositInterestCalculator, eventDate);
        this.depositAccountRepository.save(account);
        String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResult(account.getId());
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawDepositAccountInterestMoney(DepositAccountWithdrawInterestCommand command) {

        context.authenticatedUser();

        WithDrawDepositAccountInterestCommandValidator validator = new WithDrawDepositAccountInterestCommandValidator(command);
        validator.validate();

        DepositAccount account = this.depositAccountRepository.findOne(command.getAccountId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getAccountId()); }
        if (account.isInterestWithdrawable() && !account.isInterestCompoundingAllowed()) {

           // BigDecimal totalAvailableInterestForWithdrawal = getTotalWithdrawableInterestAvailable(account);
           // BigDecimal interestPaid = account.getInterstPaid();
            BigDecimal remainInterestForWithdrawal = account.getAvailableInterest(); //totalAvailableInterestForWithdrawal.subtract(interestPaid);

            if (remainInterestForWithdrawal.doubleValue() > 0) {
                if (remainInterestForWithdrawal.doubleValue() >= command.getWithdrawInterest().doubleValue()
                        && command.getWithdrawInterest().doubleValue() > 0) {
                    account.withdrawInterest(Money.of(account.getDeposit().getCurrency(), command.getWithdrawInterest()));
                    this.depositAccountRepository.save(account);
                } else {
                    throw new DepositAccountTransactionsException("deposit.transaction.interest.withdrawal.exceed", "You can Withdraw "
                            + remainInterestForWithdrawal + " only");
                }
            } else {
                throw new DepositAccountTransactionsException("deposit.transaction.interest.withdrawal.insufficient.amount",
                        "You don't have enough money for withdrawal");
            }
        } else {
            throw new DepositAccountTransactionsException("deposit.transaction.interest.withdrawal.cannot.withdraw",
                    "You can not withdraw interst for this account");
        }
        return new CommandProcessingResult(account.getId());
    }

/*    private BigDecimal getTotalWithdrawableInterestAvailable(DepositAccount account) {
        BigDecimal interstGettingForPeriod = BigDecimal.valueOf(account.getAccuredInterest().getAmount().doubleValue()
                / new Double(account.getTenureInMonths()));
        Integer noOfMonthsforInterestCal = Months.monthsBetween(account.getActualCommencementDate(), new LocalDate()).getMonths();
        Integer noOfPeriods = noOfMonthsforInterestCal / account.getInterestCompoundedEvery();
        return interstGettingForPeriod.multiply(new BigDecimal(noOfPeriods));
    }*/

    @Transactional
    @Override
    public CommandProcessingResult renewDepositAccount(DepositAccountCommand command) {

        this.context.authenticatedUser();

        RenewDepositAccountCommandValidator validator = new RenewDepositAccountCommandValidator(command);
        validator.validateForCreate();

        DepositAccount account = this.depositAccountRepository.findOne(command.getId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getId()); }

        // FIXME - KW - extract whats in this if into a method that naturally
        // describes what you are checking.
        if (account.isRenewalAllowed()
                && (new LocalDate().isAfter(account.maturesOnDate()) || new LocalDate().isEqual(account.maturesOnDate()))) {

            if (account.isActive()) {
                final DepositAccount renewedAccount = this.depositAccountAssembler.assembleFrom(account, command);
                this.depositAccountRepository.save(renewedAccount);
                account.closeDepositAccount(defaultDepositLifecycleStateMachine());
                this.depositAccountRepository.save(account);
                return new CommandProcessingResult(renewedAccount.getId());
            }

            throw new DepositAccountReopenException(account.getMaturityDate());
        }

        throw new DepositAccountReopenException(account.getMaturityDate());
    }

    @Override
    public CommandProcessingResult updateDepositAccount(DepositAccountCommand command) {

        try {
            this.context.authenticatedUser();

            DepositAccountCommandValidator validator = new DepositAccountCommandValidator(command);
            validator.validateForUpdate();

            final DepositAccount account = this.depositAccountRepository.findOne(command.getId());
            if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.getId()); }

            if (account.isSubmittedAndPendingApproval()) {
                this.depositAccountAssembler.assembleUpdatedDepositAccount(account, command);
            } else if (account.isActive()) {
                this.depositAccountAssembler.updateApprovedDepositAccount(account, command);
            }

            this.depositAccountRepository.save(account);

            return new CommandProcessingResult(account.getId());
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult postInterestToDepositAccount(Collection<DepositAccountsForLookup> accounts) {

        try {

            for (DepositAccountsForLookup accountData : accounts) {
                DepositAccount account = this.depositAccountRepository.findOne(accountData.getId());
                if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(accountData.getId()); }
                this.depositAccountAssembler.postInterest(account);
                this.depositAccountRepository.save(account);
            }
            return new CommandProcessingResult(new Long(accounts.size()));
        } catch (Exception e) {
            return new CommandProcessingResult(Long.valueOf(-1));
        }

    }
}