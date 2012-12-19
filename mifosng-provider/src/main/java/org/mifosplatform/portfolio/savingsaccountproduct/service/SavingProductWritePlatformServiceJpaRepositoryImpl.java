package org.mifosplatform.portfolio.savingsaccountproduct.service;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.command.SavingProductCommand;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingsProductNotFoundException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavingProductWritePlatformServiceJpaRepositoryImpl implements SavingProductWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingProductRepository savingProductRepository;

    @Autowired
    public SavingProductWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingProductRepository savingProductRepository) {
        this.context = context;
        this.savingProductRepository = savingProductRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createSavingProduct(final SavingProductCommand command) {

        this.context.authenticatedUser();
        SavingProductCommandValidator validator = new SavingProductCommandValidator(command);
        validator.validateForCreate();

        SavingProductType savingProductType = SavingProductType.fromInt(command.getSavingProductType());
        TenureTypeEnum tenureType = TenureTypeEnum.fromInt(command.getTenureType());
        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(command.getFrequency());
        SavingsInterestType interestType = SavingsInterestType.fromInt(command.getInterestType());
        PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
        SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod());
        MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
        
        if ( savingProductType.equals(SavingProductType.INVALID) ||
        	 tenureType.equals(TenureTypeEnum.INVALID) ||
        	 savingFrequencyType.equals(SavingFrequencyType.INVALID) ||
        	 interestType.equals(SavingsInterestType.INVALID) ||
        	 lockinPeriodType.equals(PeriodFrequencyType.INVALID)||
        	 savingInterestCalculationMethod.equals(SavingInterestCalculationMethod.INVALID)
        		) {
			throw new RuntimeException("Please select a valid types"); 
		}

        SavingProduct product = new SavingProduct(command.getName(), command.getDescription(), currency, command.getInterestRate(),
                command.getMinInterestRate(), command.getMaxInterestRate(), command.getSavingsDepositAmount(),command.getDepositEvery(), savingProductType,
                tenureType, command.getTenure(), savingFrequencyType, interestType, savingInterestCalculationMethod,
                command.getMinimumBalanceForWithdrawal(), command.isPartialDepositAllowed(), command.isLockinPeriodAllowed(),
                command.getLockinPeriod(), lockinPeriodType);

        this.savingProductRepository.save(product);
        return new CommandProcessingResult(product.getId());
    }

    @Transactional
    @Override
    public CommandProcessingResult updateSavingProduct(final SavingProductCommand command) {

        this.context.authenticatedUser();
        SavingProductCommandValidator validator = new SavingProductCommandValidator(command);
        validator.validateForUpdate();

        SavingProduct product = this.savingProductRepository.findOne(command.getId());
        if (product == null) { throw new SavingProductNotFoundException(command.getId()); }
        product.update(command);
        this.savingProductRepository.save(product);
        return new CommandProcessingResult(Long.valueOf(product.getId()));
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteSavingProduct(Long productId) {

        this.context.authenticatedUser();
        SavingProduct product = this.savingProductRepository.findOne(productId);
        if (product == null || product.isDeleted()) { throw new SavingsProductNotFoundException(productId); }
        product.delete();
        this.savingProductRepository.save(product);
        return new CommandProcessingResult(Long.valueOf(product.getId()));
    }

}