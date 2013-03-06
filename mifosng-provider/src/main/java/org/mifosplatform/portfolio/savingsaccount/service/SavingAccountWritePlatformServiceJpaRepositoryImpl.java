/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.service;

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
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.note.domain.Note;
import org.mifosplatform.portfolio.note.domain.NoteRepository;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountForLookup;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccount;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccountRepository;
import org.mifosplatform.portfolio.savingsaccount.exception.SavingAccountNotFoundException;
import org.mifosplatform.portfolio.savingsaccount.serialization.SavingAccountCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.savingsaccount.serialization.SavingAccountStateTransitionCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachineImpl;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.InvalidSavingStateTransitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingAccountWritePlatformServiceJpaRepositoryImpl implements SavingAccountWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(SavingAccountWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final SavingAccountRepository savingAccountRepository;
    private final SavingAccountAssembler savingAccountAssembler;
    private final NoteRepository noteRepository;
    private final SavingAccountCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final SavingAccountStateTransitionCommandFromApiJsonDeserializer savingAccountStateTransitionCommandFromApiJsonDeserializer;

    @Autowired
    public SavingAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingAccountRepository savingAccountRepository, final SavingAccountAssembler savingAccountAssembler,
            final NoteRepository noteRepository,
            final SavingAccountCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final SavingAccountStateTransitionCommandFromApiJsonDeserializer savingAccountStateTransitionCommandFromApiJsonDeserializer) {
        this.context = context;
        this.savingAccountRepository = savingAccountRepository;
        this.savingAccountAssembler = savingAccountAssembler;
        this.noteRepository = noteRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.savingAccountStateTransitionCommandFromApiJsonDeserializer = savingAccountStateTransitionCommandFromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult createSavingAccount(final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            SavingAccount account = this.savingAccountAssembler.assembleFrom(command);
            this.savingAccountRepository.save(account);

            return new CommandProcessingResultBuilder() //
            .withEntityId(account.getId()) //
            .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }

    }

    @Override
    public CommandProcessingResult updateSavingAccount(final Long accountId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            
            Map<String, Object> changes =  new LinkedHashMap<String, Object>(20);;

            SavingAccount account = this.savingAccountRepository.findOne(accountId);
            if (account == null || account.isDeleted()) { throw new SavingAccountNotFoundException(accountId); }
            if (account.isPendingApproval()) {
               changes = this.savingAccountAssembler.assembleFrom(command, account);
                this.savingAccountRepository.save(account);
            }
            return new CommandProcessingResultBuilder() //
            .withEntityId(account.getId()) //
            .with(changes)
            .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("saving_acc_external_id")) { 
        	final String externalId = command.stringValueOfParameterNamed("externalId");
        	throw new PlatformDataIntegrityException(
                "error.msg.saving.account.duplicate.externalId", "Saving account with externalId " + externalId
                        + " already exists", "externalId", externalId); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.saving.account.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

	@Override
	public CommandProcessingResult rejectSavingApplication(JsonCommand command) {
		
		this.context.authenticatedUser();
		this.savingAccountStateTransitionCommandFromApiJsonDeserializer.validateForReject(command.json());
		
		SavingAccount account = this.savingAccountRepository.findOne(command.entityId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.entityId());
		}
		LocalDate eventDate = command.localDateValueOfParameterNamed("eventDate");
		if (this.isBeforeToday(eventDate)) {
        	final String errorMessage = "User has no authority to reject saving with a date in the past. ";
			
			throw new InvalidSavingStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, account.projectedCommencementDate(), eventDate);
        }
		account.reject(eventDate, defaultDepositLifecycleStateMachine());
		this.savingAccountRepository.save(account);
		
		String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
	}
	
	private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }

	@Override
	public CommandProcessingResult withdrawSavingApplication(JsonCommand command) {
		this.context.authenticatedUser();
		this.savingAccountStateTransitionCommandFromApiJsonDeserializer.validateForWithdrawApplication(command.json());
		
		SavingAccount account = this.savingAccountRepository.findOne(command.entityId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.entityId());
		}
		LocalDate eventDate = command.localDateValueOfParameterNamed("eventDate");
		if (this.isBeforeToday(eventDate)) {
			final String errorMessage = "User has no authority to withdraw saving with a date in the past. ";
			
			throw new InvalidSavingStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, account.projectedCommencementDate(), eventDate);
        }
		
		account.withdrawnByApplicant(eventDate, defaultDepositLifecycleStateMachine());
		this.savingAccountRepository.save(account);
		
		String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
	}

	@Override
	public CommandProcessingResult undoSavingAccountApproval(JsonCommand command) {
		
		this.context.authenticatedUser();
		SavingAccount account = this.savingAccountRepository.findOne(command.entityId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.getLoanId());
		}
		account.undoSavingAccountApproval(defaultDepositLifecycleStateMachine());
		this.savingAccountRepository.save(account);
		
		String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
	}

	@Override
	public CommandProcessingResult approveSavingAccount(JsonCommand command) {
		
		this.context.authenticatedUser();
		this.savingAccountStateTransitionCommandFromApiJsonDeserializer.validateForApprove(command.json());
		
		SavingAccount account = this.savingAccountRepository.findOne(command.entityId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.entityId());
		}
		LocalDate approvalDate = command.localDateValueOfParameterNamed("commencementDate");
		if (approvalDate.isBefore(account.projectedCommencementDate())) {
			final String errorMessage = "The date on which a saving is approved cannot be before its submittal date: "
                    + approvalDate.toString();
			
			throw new InvalidSavingStateTransitionException("approval", "cannot.be.before.submittal.date", errorMessage,
					account.projectedCommencementDate(), approvalDate);
		}
		Map<String, Object> changes = this.savingAccountAssembler.approveSavingAccount(command,account);
		this.savingAccountRepository.save(account);
		
		String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .with(changes)
        .build();
	}

	@Override
	public CommandProcessingResult depositMoney(JsonCommand command) {
		
		this.context.authenticatedUser();
		this.savingAccountStateTransitionCommandFromApiJsonDeserializer.validateForDepositAmount(command.json());
		
		SavingAccount account = this.savingAccountRepository.findOne(command.entityId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.entityId());
		}
		
		if (account.isActive())
		account.depositMoney(command);
		this.savingAccountRepository.save(account);
		String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
	}

	@Override
	public CommandProcessingResult withdrawSavingAmount(JsonCommand command) {
		
		this.context.authenticatedUser();
		SavingAccount account = this.savingAccountRepository.findOne(command.entityId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.entityId());
		}
		if (account.isActive())
			this.savingAccountAssembler.withdrawSavingAccountMoney(command,account);
		this.savingAccountRepository.save(account);
		String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
        return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
	}

	@Override
	public CommandProcessingResult deleteSavingAccount(Long accountId) {
		this.context.authenticatedUser();
		SavingAccount account = this.savingAccountRepository.findOne(accountId);
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(accountId);
		}
		account.delete();
		this.savingAccountRepository.save(account);
		return new CommandProcessingResultBuilder() //
        .withEntityId(account.getId()) //
        .build();
	}

	@Override
	public CommandProcessingResult postInterest(Collection<SavingAccountForLookup> savingAccounts) {
		this.context.authenticatedUser();
		try {
		for(SavingAccountForLookup accountForLookup : savingAccounts ){
			SavingAccount account = this.savingAccountRepository.findOne(accountForLookup.getId());
			if (account == null || account.isDeleted()) { throw new SavingAccountNotFoundException(accountForLookup.getId()); }
			this.savingAccountAssembler.postInterest(account);
			this.savingAccountRepository.save(account);
		}
		return new CommandProcessingResultBuilder() //
        .withEntityId(Long.valueOf(savingAccounts.size())) //
        .build();
		} catch (Exception e) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	private boolean isBeforeToday(final LocalDate date) {
    	return date.isBefore(new LocalDate());
    }
}
