package org.mifosng.platform.saving.service;

import org.mifosng.platform.api.commands.SavingAccountCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.exceptions.SavingAccountNotFoundException;
import org.mifosng.platform.saving.domain.SavingAccount;
import org.mifosng.platform.saving.domain.SavingAccountRepository;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
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
	@SuppressWarnings("unused")
	private final NoteRepository noteRepository;
	
	@Autowired
	public SavingAccountWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,final SavingAccountRepository savingAccountRepository,
			SavingAccountAssembler savingAccountAssembler,NoteRepository noteRepository) {
		this.context=context;
		this.savingAccountRepository=savingAccountRepository;
		this.savingAccountAssembler=savingAccountAssembler;
		this.noteRepository=noteRepository;
	}
	
	
	@Transactional
	@Override
	public EntityIdentifier createSavingAccount(SavingAccountCommand command) {
		try {
			this.context.authenticatedUser();
			SavingAccountCommandValidator validator=new SavingAccountCommandValidator(command);
			validator.validateForCreate();
			
			SavingAccount account=this.savingAccountAssembler.assembleFrom(command);
			this.savingAccountRepository.save(account);
			
			return new EntityIdentifier(account.getId());
		} catch (DataIntegrityViolationException dve) {
			 handleDataIntegrityIssues(command, dve);
			 return new EntityIdentifier(Long.valueOf(-1));
		}
		
	}

	@Override
	public EntityIdentifier updateSavingAccount(SavingAccountCommand command) {
		try {
			this.context.authenticatedUser();
			SavingAccountCommandValidator validator=new SavingAccountCommandValidator(command);
			validator.validateForUpdate();
			
			SavingAccount account=this.savingAccountRepository.findOne(command.getId());
			if (account == null || account.isDeleted()) {
				throw new SavingAccountNotFoundException(command.getId());
			}
			if(account.isPendingApproval()){
				this.savingAccountAssembler.assembleFrom(command,account);
				this.savingAccountRepository.save(account);
			}
			return new EntityIdentifier(account.getId());
		} catch (DataIntegrityViolationException dve) {
			 handleDataIntegrityIssues(command, dve);
			 return new EntityIdentifier(Long.valueOf(-1));
		}		
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleDataIntegrityIssues(final SavingAccountCommand command, DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("saving_acc_external_id")) {
			throw new PlatformDataIntegrityException("error.msg.saving.account.duplicate.externalId", "Saving account with externalId " + command.getExternalId() + " already exists", "externalId", command.getExternalId());
		}
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.saving.account.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}
}
