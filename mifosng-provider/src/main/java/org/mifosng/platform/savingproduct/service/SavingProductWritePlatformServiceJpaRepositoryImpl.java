package org.mifosng.platform.savingproduct.service;

import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.exceptions.ProductNotFoundException;
import org.mifosng.platform.exceptions.SavingProductNotFoundException;
import org.mifosng.platform.savingproduct.domain.SavingFrequencyType;
import org.mifosng.platform.savingproduct.domain.SavingInterestCalculationMethod;
import org.mifosng.platform.savingproduct.domain.SavingProduct;
import org.mifosng.platform.savingproduct.domain.SavingProductRepository;
import org.mifosng.platform.savingproduct.domain.SavingProductType;
import org.mifosng.platform.savingproduct.domain.SavingsInterestType;
import org.mifosng.platform.savingproduct.domain.TenureTypeEnum;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
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
		
		SavingProductType savingProductType = SavingProductType.fromInt(command.getSavingProductType());
		TenureTypeEnum tenureType = TenureTypeEnum.fromInt(command.getTenureType());
		SavingFrequencyType savingFrequencyType=SavingFrequencyType.fromInt(command.getFrequency());
		SavingsInterestType interestType = SavingsInterestType.fromInt(command.getInterestType());
		PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
		SavingInterestCalculationMethod savingInterestCalculationMethod=SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod());
		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		
		SavingProduct product = new SavingProduct(command.getName(), command.getDescription(), currency, command.getInterestRate(), command.getMinInterestRate(), command.getMaxInterestRate(), 
				command.getSavingsDepositAmount(), savingProductType, tenureType, command.getTenure(), savingFrequencyType, interestType, 
				savingInterestCalculationMethod, command.getMinimumBalanceForWithdrawal(), command.isPartialDepositAllowed(), 
				command.isLockinPeriodAllowed(), command.getLockinPeriod(), lockinPeriodType);
		
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