package org.mifosplatform.portfolio.savingsaccount.service;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.crypto.RuntimeCryptoException;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.loanaccount.command.UndoStateTransitionCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountApprovalCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountApprovalCommandValidator;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommandValidator;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountDepositCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountDepositCommandValidator;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountStateTransitionCommandValidator;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountWithdrawalCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingStateTransitionsCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountForLookup;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccount;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccountRepository;
import org.mifosplatform.portfolio.savingsaccount.exception.SavingAccountNotFoundException;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachineImpl;
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

    @Autowired
    public SavingAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingAccountRepository savingAccountRepository, final SavingAccountAssembler savingAccountAssembler,
            final NoteRepository noteRepository) {
        this.context = context;
        this.savingAccountRepository = savingAccountRepository;
        this.savingAccountAssembler = savingAccountAssembler;
        this.noteRepository = noteRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createSavingAccount(final SavingAccountCommand command) {
        try {
            this.context.authenticatedUser();
            SavingAccountCommandValidator validator = new SavingAccountCommandValidator(command);
            validator.validateForCreate();

            SavingAccount account = this.savingAccountAssembler.assembleFrom(command);
            this.savingAccountRepository.save(account);

            return new CommandProcessingResult(account.getId());
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }

    }

    @Override
    public CommandProcessingResult updateSavingAccount(final SavingAccountCommand command) {
        try {
            this.context.authenticatedUser();
            SavingAccountCommandValidator validator = new SavingAccountCommandValidator(command);
            validator.validateForUpdate();

            SavingAccount account = this.savingAccountRepository.findOne(command.getId());
            if (account == null || account.isDeleted()) { throw new SavingAccountNotFoundException(command.getId()); }
            if (account.isPendingApproval()) {
                this.savingAccountAssembler.assembleFrom(command, account);
                this.savingAccountRepository.save(account);
            }
            return new CommandProcessingResult(account.getId());
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final SavingAccountCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("saving_acc_external_id")) { throw new PlatformDataIntegrityException(
                "error.msg.saving.account.duplicate.externalId", "Saving account with externalId " + command.getExternalId()
                        + " already exists", "externalId", command.getExternalId()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.saving.account.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

	@Override
	public CommandProcessingResult rejectSavingApplication(SavingStateTransitionsCommand command) {
		
		this.context.authenticatedUser();
		
		SavingAccountStateTransitionCommandValidator validator = new SavingAccountStateTransitionCommandValidator(command);
		validator.validate();
		
		SavingAccount account = this.savingAccountRepository.findOne(command.getAccountId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.getAccountId());
		}
		LocalDate eventDate = command.getEventDate();
		account.reject(eventDate, defaultDepositLifecycleStateMachine());
		this.savingAccountRepository.save(account);
		
		String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
		return new CommandProcessingResult(account.getId());
	}
	
	private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }

	@Override
	public CommandProcessingResult withdrawSavingApplication(SavingStateTransitionsCommand command) {
		this.context.authenticatedUser();
		
		SavingAccountStateTransitionCommandValidator validator = new SavingAccountStateTransitionCommandValidator(command);
		validator.validate();
		
		SavingAccount account = this.savingAccountRepository.findOne(command.getAccountId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.getAccountId());
		}
		LocalDate eventDate = command.getEventDate();
		account.withdrawnByApplicant(eventDate, defaultDepositLifecycleStateMachine());
		this.savingAccountRepository.save(account);
		
		String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
		return new CommandProcessingResult(account.getId());
	}

	@Override
	public CommandProcessingResult undoSavingAccountApproval(UndoStateTransitionCommand command) {
		
		this.context.authenticatedUser();
		SavingAccount account = this.savingAccountRepository.findOne(command.getLoanId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.getLoanId());
		}
		account.undoSavingAccountApproval(defaultDepositLifecycleStateMachine());
		this.savingAccountRepository.save(account);
		
		String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
		return new CommandProcessingResult(account.getId());
	}

	@Override
	public CommandProcessingResult approveSavingAccount(SavingAccountApprovalCommand command) {
		
		this.context.authenticatedUser();
		SavingAccountApprovalCommandValidator validator = new SavingAccountApprovalCommandValidator(command);
		validator.validate();
		SavingAccount account = this.savingAccountRepository.findOne(command.getAccountId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.getAccountId());
		}
		LocalDate approvalDate = command.getApprovalDate();
		if (approvalDate.isBefore(account.projectedCommencementDate())) {
			throw new RuntimeCryptoException("Date of approval cannot before application submission date");
		}
		this.savingAccountAssembler.approveSavingAccount(command,account);
		this.savingAccountRepository.save(account);
		
		String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		
		return new CommandProcessingResult(account.getId());
	}

	@Override
	public CommandProcessingResult depositMoney(SavingAccountDepositCommand command) {
		
		this.context.authenticatedUser();
		SavingAccountDepositCommandValidator validator = new SavingAccountDepositCommandValidator(command);
		validator.validate();
		
		SavingAccount account = this.savingAccountRepository.findOne(command.getAccountId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.getAccountId());
		}
		
		if (account.isActive())
		account.depositMoney(command);
		this.savingAccountRepository.save(account);
		String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		return new CommandProcessingResult(account.getId());
	}

	@Override
	public CommandProcessingResult withdrawSavingAmount(SavingAccountWithdrawalCommand command) {
		
		this.context.authenticatedUser();
		SavingAccount account = this.savingAccountRepository.findOne(command.getAccountId());
		if (account == null || account.isDeleted()) {
			throw new SavingAccountNotFoundException(command.getAccountId());
		}
		if (account.isActive())
			this.savingAccountAssembler.withdrawSavingAccountMoney(command,account);
		this.savingAccountRepository.save(account);
		String noteText = command.getNote();
        if (StringUtils.isNotBlank(noteText)) {
            Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
		return new CommandProcessingResult(account.getId());
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
		return new CommandProcessingResult(accountId);
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
		return new CommandProcessingResult(new Long(savingAccounts.size()));
		} catch (Exception e) {
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
}
