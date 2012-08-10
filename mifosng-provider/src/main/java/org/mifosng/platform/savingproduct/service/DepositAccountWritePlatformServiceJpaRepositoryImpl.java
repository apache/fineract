package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.exceptions.DepositAccountNotFoundException;
import org.mifosng.platform.exceptions.ProductNotFoundException;
import org.mifosng.platform.saving.domain.DepositAccount;
import org.mifosng.platform.saving.domain.DepositAccountRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositAccountWritePlatformServiceJpaRepositoryImpl implements DepositAccountWritePlatformService {

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
	
	@Transactional
	@Override
	public EntityIdentifier createDepositAccount(final DepositAccountCommand command) {
		
		this.context.authenticatedUser();
		
		DepositAccountCommandValidator validator = new DepositAccountCommandValidator(command);
		validator.validateForCreate();
		
		final DepositAccount account = this.depositAccountAssembler.assembleFrom(command);
		this.depositAccountRepository.save(account);
		
		return new EntityIdentifier(account.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier updateDepositAccount(final DepositAccountCommand command) {
		
		this.context.authenticatedUser();
		
		DepositAccountCommandValidator validator = new DepositAccountCommandValidator(command);
		validator.validateForUpdate();
		
		final DepositAccount account = this.depositAccountAssembler.assembleFrom(command);
		if (account == null || account.isDeleted()) {
			throw new DepositAccountNotFoundException(command.getId());
		}

		account.update(command);
		this.depositAccountRepository.save(account);
		
		return new EntityIdentifier(Long.valueOf(1));
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