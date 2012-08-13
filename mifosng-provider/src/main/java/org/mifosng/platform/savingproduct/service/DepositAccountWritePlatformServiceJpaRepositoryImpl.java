package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.exceptions.DepositAccountNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.exceptions.ProductNotFoundException;
import org.mifosng.platform.saving.domain.DepositAccount;
import org.mifosng.platform.saving.domain.DepositAccountRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
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
	
	@Autowired
	public DepositAccountWritePlatformServiceJpaRepositoryImpl(
			final PlatformSecurityContext context, 
			final DepositAccountRepository depositAccountRepository, 
			final DepositAccountAssembler depositAccountAssembler) {
		
		this.context=context;
		this.depositAccountRepository = depositAccountRepository;
		this.depositAccountAssembler = depositAccountAssembler;
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleDataIntegrityIssues(final DepositAccountCommand command, DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("deposit_acc_external_id")) {
			throw new PlatformDataIntegrityException("error.msg.desposit.account.duplicate.externalId", "Deposit account with externalId " + command.getExternalId() + " already exists", "externalId", command.getExternalId());
		}
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.deposit.account.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}
	
	@Transactional
	@Override
	public EntityIdentifier createDepositAccount(final DepositAccountCommand command) {
		
		try {
			this.context.authenticatedUser();
			
			DepositAccountCommandValidator validator = new DepositAccountCommandValidator(command);
			validator.validateForCreate();
			
			final DepositAccount account = this.depositAccountAssembler.assembleFrom(command);
			this.depositAccountRepository.save(account);
			
			return new EntityIdentifier(account.getId());
		} catch (DataIntegrityViolationException dve) {
			 handleDataIntegrityIssues(command, dve);
			 return new EntityIdentifier(Long.valueOf(-1));
		}
	}
	
	@Transactional
	@Override
	public EntityIdentifier updateDepositAccount(final DepositAccountCommand command) {
		
		try {
			
			this.context.authenticatedUser();
			
			DepositAccountCommandValidator validator = new DepositAccountCommandValidator(command);
			validator.validateForUpdate();
			
			// FIXME - update scenarios to be completely done
			final DepositAccount account = this.depositAccountAssembler.assembleFrom(command);
			if (account == null || account.isDeleted()) {
				throw new DepositAccountNotFoundException(command.getId());
			}
	
			account.update(command);
			this.depositAccountRepository.save(account);
			
			return new EntityIdentifier(account.getId());
		} catch (DataIntegrityViolationException dve) {
			 handleDataIntegrityIssues(command, dve);
			 return new EntityIdentifier(Long.valueOf(-1));
		}
	}

	@Transactional
	@Override
	public EntityIdentifier deleteDepositAccount(final Long accountId) {
		
		this.context.authenticatedUser();
		
		DepositAccount account = this.depositAccountRepository.findOne(accountId);
		if (account==null || account.isDeleted()) {
			throw new ProductNotFoundException(accountId);
		}
		
		account.delete();
		this.depositAccountRepository.save(account);
		
		return new EntityIdentifier(accountId);
	}
}