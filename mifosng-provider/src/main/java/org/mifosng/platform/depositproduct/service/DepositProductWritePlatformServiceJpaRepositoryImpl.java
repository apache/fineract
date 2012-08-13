package org.mifosng.platform.depositproduct.service;

import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.deposit.domain.DepositProduct;
import org.mifosng.platform.deposit.domain.DepositProductRepository;
import org.mifosng.platform.exceptions.DepositProductNotFoundException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositProductWritePlatformServiceJpaRepositoryImpl implements
		DepositProductWritePlatformService {
	
	private final PlatformSecurityContext context;
	private final DepositProductRepository depositProductRepository;
	
	
	@Autowired
	public DepositProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final DepositProductRepository depositProductRepository){
		this.context=context;
		this.depositProductRepository=depositProductRepository;
	}

	@Transactional
	@Override
	public EntityIdentifier createDepositProduct(DepositProductCommand command) {
		
		this.context.authenticatedUser();
		DepositProductCommandValidator validator=new DepositProductCommandValidator(command);
		validator.validateForCreate();
		
		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		DepositProduct product = new DepositProduct(command.getName(),command.getDescription(),currency,command.getMinimumBalance(),command.getMaximumBalance(),
				command.getTenureMonths(),command.getMaturityDefaultInterestRate(),command.getMaturityMinInterestRate(),command.getMaturityMaxInterestRate(),
				command.isRenewalAllowed(), command.isPreClosureAllowed(),
				command.getPreClosureInterestRate());
		this.depositProductRepository.save(product);
		
		return new EntityIdentifier(product.getId());
		
	}

	@Transactional
	@Override
	public EntityIdentifier updateDepositProduct(DepositProductCommand command) {
		
		this.context.authenticatedUser();
		DepositProductCommandValidator validator=new DepositProductCommandValidator(command);
		validator.validateForUpdate();
		
		DepositProduct product=this.depositProductRepository.findOne(command.getId());
		if(product==null){
			throw new DepositProductNotFoundException(command.getId());
		}
		
		product.update(command);
		this.depositProductRepository.save(product);
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}

	@Transactional
	@Override
	public EntityIdentifier deleteDepositProduct(Long productId) {
		
		this.context.authenticatedUser();
		DepositProduct product=this.depositProductRepository.findOne(productId);
		if(product==null){
			throw new DepositProductNotFoundException(productId);
		}
		product.delete();
		this.depositProductRepository.save(product);
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}
}