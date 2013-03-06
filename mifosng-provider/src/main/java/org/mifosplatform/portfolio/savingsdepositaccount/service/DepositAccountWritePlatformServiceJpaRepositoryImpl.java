/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
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
import org.mifosplatform.portfolio.savingsdepositaccount.serialization.DepositAccountCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.savingsdepositaccount.serialization.DepositAccountStateTransitionCommandFromApiJsonDeserializer;
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
    private final DepositAccountCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final DepositAccountStateTransitionCommandFromApiJsonDeserializer depositAccountStateTransitionCommandFromApiJsonDeserializer;

    @Autowired
    public DepositAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final DepositAccountRepository depositAccountRepository, final DepositAccountAssembler depositAccountAssembler,
            final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator, final NoteRepository noteRepository,
            final DepositAccountCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final DepositAccountStateTransitionCommandFromApiJsonDeserializer depositAccountStateTransitionCommandFromApiJsonDeserializer) {
        this.context = context;
        this.depositAccountRepository = depositAccountRepository;
        this.depositAccountAssembler = depositAccountAssembler;
        this.fixedTermDepositInterestCalculator = fixedTermDepositInterestCalculator;
        this.noteRepository = noteRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.depositAccountStateTransitionCommandFromApiJsonDeserializer = depositAccountStateTransitionCommandFromApiJsonDeserializer;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("deposit_acc_external_id")) { 
        	final String externalId = command.stringValueOfParameterNamed("externalId");
        	throw new PlatformDataIntegrityException(
                "error.msg.desposit.account.duplicate.externalId", "Deposit account with externalId " + externalId
                        + " already exists", "externalId", externalId); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.deposit.account.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createDepositAccount(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final DepositAccount account = this.depositAccountAssembler.assembleFrom(command);
            this.depositAccountRepository.save(account);

            return new CommandProcessingResultBuilder() //
            .withEntityId(account.getId()) //
            .build();
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
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(accountId); }

        account.delete();
        this.depositAccountRepository.save(account);

        return new CommandProcessingResultBuilder() //
        .withEntityId(accountId) //
        .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult approveDepositApplication(final JsonCommand command) {

        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForApprove(command.json());

        DepositAccount account = this.depositAccountRepository.findOne(command.entityId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.entityId()); }

        final LocalDate commencementDate = command.localDateValueOfParameterNamed("commencementDate");

        Map<String, Object> actualChanges = account.approve(commencementDate, defaultDepositLifecycleStateMachine(), command, this.fixedTermDepositInterestCalculator);

        this.depositAccountRepository.save(account);
        
        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .with(actualChanges)
        .build();

    }

    private boolean isBeforeToday(final LocalDate date) {
    	return date.isBefore(new LocalDate());
    }
    private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectDepositApplication(JsonCommand command) {

        this.context.authenticatedUser();
        this.depositAccountStateTransitionCommandFromApiJsonDeserializer.validateForReject(command.json());

        DepositAccount account = this.depositAccountRepository.findOne(command.entityId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.entityId()); }

        LocalDate eventDate = command.localDateValueOfParameterNamed("eventDate");
        if (this.isBeforeToday(eventDate)) {
        	throw new NoAuthorizationException("User has no authority to reject deposit with a date in the past.");
        }

        account.reject(eventDate, defaultDepositLifecycleStateMachine());
        this.depositAccountRepository.save(account);

        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawDepositApplication(JsonCommand command) {

        this.context.authenticatedUser();
        this.depositAccountStateTransitionCommandFromApiJsonDeserializer.validateForWithdrawDepositApplication(command.json());

        DepositAccount account = this.depositAccountRepository.findOne(command.entityId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.entityId()); }

        LocalDate eventDate = command.localDateValueOfParameterNamed("eventDate");
        if (this.isBeforeToday(eventDate)) {
        	throw new NoAuthorizationException("User has no authority to withdraw deposit application with a date in the past.");
        }

        account.withdrawnByApplicant(eventDate, defaultDepositLifecycleStateMachine());
        this.depositAccountRepository.save(account);

        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult undoDepositApproval(JsonCommand command) {

        context.authenticatedUser();

        DepositAccount account = this.depositAccountRepository.findOne(command.entityId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.entityId()); }

        account.undoDepositApproval(defaultDepositLifecycleStateMachine());
        this.depositAccountRepository.save(account);

        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawDepositAccountMoney(final JsonCommand command) {

        this.context.authenticatedUser();
        this.depositAccountStateTransitionCommandFromApiJsonDeserializer.validateForWithdrawDepositAmount(command.json());

        DepositAccount account = this.depositAccountRepository.findOne(command.entityId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.entityId()); }

        LocalDate eventDate = command.localDateValueOfParameterNamed("maturesOnDate");

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
        String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.depositNote(account, noteText);
            this.noteRepository.save(note);
        }

        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult withdrawDepositAccountInterestMoney(JsonCommand command) {

        context.authenticatedUser();
        this.depositAccountStateTransitionCommandFromApiJsonDeserializer.validateForWithdrawInterestAmount(command.json());

        BigDecimal interestAmount = command.bigDecimalValueOfParameterNamed("amount");
        DepositAccount account = this.depositAccountRepository.findOne(command.entityId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.entityId()); }
        if (account.isInterestWithdrawable() && !account.isInterestCompoundingAllowed()) {

           // BigDecimal totalAvailableInterestForWithdrawal = getTotalWithdrawableInterestAvailable(account);
           // BigDecimal interestPaid = account.getInterstPaid();
            BigDecimal remainInterestForWithdrawal = account.getAvailableInterest(); //totalAvailableInterestForWithdrawal.subtract(interestPaid);

            if (remainInterestForWithdrawal.doubleValue() > 0) {
                if (remainInterestForWithdrawal.doubleValue() >= interestAmount.doubleValue()
                        && interestAmount.doubleValue() > 0) {
                    account.withdrawInterest(Money.of(account.getDeposit().getCurrency(), interestAmount));
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
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult renewDepositAccount(JsonCommand command) {

        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForRenew(command.json());
        
        DepositAccount account = this.depositAccountRepository.findOne(command.entityId());
        if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(command.entityId()); }

        // FIXME - KW - extract whats in this if into a method that naturally
        // describes what you are checking.
        if (account.isRenewalAllowed()
                && (new LocalDate().isAfter(account.maturesOnDate()) || new LocalDate().isEqual(account.maturesOnDate()))) {

            if (account.isActive()) {
            	final Map<String, Object> changes = new LinkedHashMap<String, Object>(20);
                final DepositAccount renewedAccount = this.depositAccountAssembler.assembleFrom(account, command,changes);
                this.depositAccountRepository.save(renewedAccount);
                account.closeDepositAccount(defaultDepositLifecycleStateMachine());
                this.depositAccountRepository.save(account);
                return new CommandProcessingResultBuilder() //
                .withEntityId(account.getId()) //
                .with(changes)
                .build();
            }

            throw new DepositAccountReopenException(account.getMaturityDate());
        }

        throw new DepositAccountReopenException(account.getMaturityDate());
    }

    @Transactional
    @Override
    public CommandProcessingResult updateDepositAccount(Long accountId, JsonCommand command) {

        try {
	        	
	            this.context.authenticatedUser();
	            this.fromApiJsonDeserializer.validateForUpdate(command.json());
	            
	            Map<String, Object> changes =  new LinkedHashMap<String, Object>(20);
	            
	            final DepositAccount account = this.depositAccountRepository.findOne(accountId);
	            if (account == null || account.isDeleted()) { throw new DepositAccountNotFoundException(accountId); }
	
	            if (account.isSubmittedAndPendingApproval()) {
	            	changes = this.depositAccountAssembler.assembleUpdatedDepositAccount(account, command);
	            } else if (account.isActive()) {
	            	changes = this.depositAccountAssembler.updateApprovedDepositAccount(account, command);
	            }
	
	            this.depositAccountRepository.save(account);
	
	            return new CommandProcessingResultBuilder() //
	            .withEntityId(account.getId()) //
	            .with(changes)
	            .build();
	            
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
            return new CommandProcessingResultBuilder() //
            .withEntityId(Long.valueOf(accounts.size())) //
            .build();
        } catch (Exception e) {
            return new CommandProcessingResult(Long.valueOf(-1));
        }

    }
}