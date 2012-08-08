package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.ProductNotFoundException;
import org.mifosng.platform.exceptions.SavingProductNotFoundException;
import org.mifosng.platform.saving.domain.SavingProduct;
import org.mifosng.platform.saving.domain.SavingProductRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingProductWritePlatformServiceJpaRepositoryImpl implements SavingProductWritePlatformService {

	private final PlatformSecurityContext context;
	private final SavingProductRepository savingProductRepository;
	
	@Autowired
	public SavingProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final SavingProductRepository savingProductRepository) {
		this.context=context;
		this.savingProductRepository=savingProductRepository;
	}
	
	@Transactional
	@Override
	public EntityIdentifier createSavingProduct(final SavingProductCommand command) {
		
		this.context.authenticatedUser();
		SavingProductCommandValidator validator=new SavingProductCommandValidator(command);
		validator.validateForCreate();
		
		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		
		SavingProduct product = new SavingProduct(command.getName(),command.getDescription(),currency,command.getInterestRate(),command.getMinimumBalance(),command.getMaximumBalance());  
		this.savingProductRepository.save(product);
		return new EntityIdentifier(product.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier updateSavingProduct(final SavingProductCommand command) {
		
		this.context.authenticatedUser();
		SavingProductCommandValidator validator=new SavingProductCommandValidator(command);
		validator.validateForUpdate();
		
		SavingProduct product=this.savingProductRepository.findOne(command.getId());
		if (product == null) {
			throw new SavingProductNotFoundException(command.getId());
		}
		product.update(command);
		this.savingProductRepository.save(product);
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}

	@Transactional
	@Override
	public EntityIdentifier deleteSavingProduct(Long productId) {
		
		this.context.authenticatedUser();
		SavingProduct product=this.savingProductRepository.findOne(productId);
		if (product==null || product.isDeleted()) {
			throw new ProductNotFoundException(productId);
		}
		product.delete();
		this.savingProductRepository.save(product);
		return new EntityIdentifier(Long.valueOf(product.getId()));
	}
	
	
}