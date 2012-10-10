package org.mifosng.platform.saving.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.SavingAccountCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.ProductNotFoundException;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.saving.domain.DepositAccountStatus;
import org.mifosng.platform.saving.domain.DepositLifecycleStateMachine;
import org.mifosng.platform.saving.domain.DepositLifecycleStateMachineImpl;
import org.mifosng.platform.saving.domain.ReccuringDepositInterestCalculator;
import org.mifosng.platform.saving.domain.SavingAccount;
import org.mifosng.platform.savingproduct.domain.SavingFrequencyType;
import org.mifosng.platform.savingproduct.domain.SavingInterestCalculationMethod;
import org.mifosng.platform.savingproduct.domain.SavingProduct;
import org.mifosng.platform.savingproduct.domain.SavingProductRepository;
import org.mifosng.platform.savingproduct.domain.SavingProductType;
import org.mifosng.platform.savingproduct.domain.SavingsInterestType;
import org.mifosng.platform.savingproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingAccountAssembler {
	
	private final ClientRepository clientRepository;
	private final SavingProductRepository savingProductRepository;
	private final ReccuringDepositInterestCalculator reccuringDepositInterestCalculator;
	
	@Autowired
	public SavingAccountAssembler(final ClientRepository clientRepository,final SavingProductRepository savingProductRepository,
			final ReccuringDepositInterestCalculator reccuringDepositInterestCalculator) {
		this.clientRepository=clientRepository;
		this.savingProductRepository=savingProductRepository;
		this.reccuringDepositInterestCalculator=reccuringDepositInterestCalculator;
	}
	//minimumBalanceForWithdrawal,isPartialDepositAllowed
	public SavingAccount assembleFrom(SavingAccountCommand command){
		
		Client client = this.clientRepository.findOne(command.getClientId());
		if (client == null || client.isDeleted()) {
			throw new ClientNotFoundException(command.getClientId());
		}
		
		SavingProduct product=this.savingProductRepository.findOne(command.getProductId());
		if(product == null || product.isDeleted()){
			throw new ProductNotFoundException(command.getProductId());
		}
		
		MonetaryCurrency currency = product.getCurrency();
		if(command.getCurrencyCode() != null && command.getDigitsAfterDecimal() != null){
			currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		}
		
		Money savingsDeposit = Money.of(currency, command.getSavingsDepositAmount());
		
		Integer tenure = product.getSavingProductRelatedDetail().getTenure();
		if(command.getTenure() != null){
			tenure=command.getTenure();
		}
		
		TenureTypeEnum tenureTypeEnum=product.getSavingProductRelatedDetail().getTenureType();
		if (command.getTenureType() != null) {
			tenureTypeEnum = TenureTypeEnum.fromInt(command.getTenureType());
		}
		
		SavingProductType savingProductType= product.getSavingProductRelatedDetail().getSavingProductType();
		if(command.getSavingProductType() != null){
			savingProductType = SavingProductType.fromInt(command.getSavingProductType()); 
		}
				
		SavingFrequencyType savingFrequencyType = product.getSavingProductRelatedDetail().getFrequency();
		if (command.getFrequency() != null) {
			savingFrequencyType = SavingFrequencyType.fromInt(command.getFrequency());
		}
		
		SavingInterestCalculationMethod savingInterestCalculationMethod = product.getSavingProductRelatedDetail().getInterestCalculationMethod();
		if (command.getInterestCalculationMethod() != null) {
			savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod());
		}
		
		SavingsInterestType interestType = product.getSavingProductRelatedDetail().getInterestType();
		if (command.getInterestType() != null) {
			interestType = SavingsInterestType.fromInt(command.getInterestType());
		}
		
		BigDecimal reccuringInterestRate = command.getRecurringInterestRate();
		BigDecimal savingInterestRate = product.getInterestRate();
		if(command.getSavingInterestRate() != null)
			savingInterestRate = command.getSavingInterestRate();
		
		boolean isLockinPeriodAllowed = product.getSavingProductRelatedDetail().isLockinPeriodAllowed();
		if(command.isLockinPeriodChanged()){
			isLockinPeriodAllowed = command.isLockinPeriodAllowed();
		}
		
		Integer lockinPeriod = product.getSavingProductRelatedDetail().getLockinPeriod();
		if(command.getLockinPeriod() != null){
			lockinPeriod = command.getLockinPeriod();
		}
		
		PeriodFrequencyType lockinPeriodType = product.getSavingProductRelatedDetail().getLockinPeriodType();
		if(command.getLockinPeriodType() != null){
			lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
		}
		
		SavingAccount account = SavingAccount.openNew(client, product, command.getExternalId(),savingsDeposit,
				reccuringInterestRate,savingInterestRate,tenure,command.getCommencementDate(),tenureTypeEnum, savingProductType,
				savingFrequencyType, interestType, savingInterestCalculationMethod, isLockinPeriodAllowed,lockinPeriod, lockinPeriodType, 
				this.reccuringDepositInterestCalculator, defaultDepositLifecycleStateMachine());
		
		return account;
	}

	public void assembleFrom(SavingAccountCommand command, SavingAccount account) {
		
		SavingProduct product=account.getProduct();
		if(command.isProductIdChanged()){
			if(product.getId() != command.getProductId()){
				product = this.savingProductRepository.findOne(command.getProductId());
				if(product == null || product.isDeleted())
					throw new ProductNotFoundException(command.getProductId());
			}
		}
		
		String externalId = account.getExternalId();
		if(command.isExternalIdChanged()){
			externalId = command.getExternalId();
		}
		
		Money savingsDeposit = Money.of(product.getCurrency(), account.getSavingsDepositAmountPerPeriod());
		if(command.isDepositAmountChanged()){
			savingsDeposit = Money.of(product.getCurrency(), command.getSavingsDepositAmount());
		}
		Integer tenure = account.getTenure();
		if(command.isTenureInMonthsChanged()){
			tenure = command.getTenure();
		}
		
		LocalDate commencementDate = new LocalDate(account.getProjectedCommencementDate());
		if (command.isCommencementDateChanged()) {
			commencementDate = command.getCommencementDate();
		}
		
		TenureTypeEnum tenureTypeEnum=account.getTenureType();
		if(command.isTenureTypeEnumChanged()){
			tenureTypeEnum = TenureTypeEnum.fromInt(command.getTenureType());
		}
		
		SavingProductType savingProductType= account.getSavingProductType();
		if (command.isSavingProductTypeChanged()) {
			savingProductType = SavingProductType.fromInt(command.getSavingProductType());
		}
				
		SavingFrequencyType savingFrequencyType = account.getFrequency();
		if (command.isSavingFrequencyTypeChanged()) {
			savingFrequencyType = SavingFrequencyType.fromInt(command.getFrequency());
		}
		
		SavingInterestCalculationMethod savingInterestCalculationMethod =account.getInterestCalculationMethod();
		if (command.isSavingInterestCalculationMethodChanged()) {
			savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(command.getInterestCalculationMethod());
		}
		
		BigDecimal reccuringInterestRate = account.getReccuringInterestRate();
		if (command.isReccuringInterestRateChanged()) {
			reccuringInterestRate = command.getRecurringInterestRate();
		}
		
		BigDecimal savingInterestRate = account.getSavingInterestRate();
		if (command.isSavingInterestRateChanged()) {
			savingInterestRate = command.getSavingInterestRate(); 
		}
		
		boolean isLockinPeriodAllowed = account.isLockinPeriodAllowed();
		if (command.isLockinPeriodAllowedChanged()) {
			isLockinPeriodAllowed = command.isLockinPeriodAllowed();
		}
		
		Integer lockinPeriod =account.getLockinPeriod();
		if (command.isLockinPeriodChanged()) {
			lockinPeriod = command.getLockinPeriod();
		}
		
		PeriodFrequencyType lockinPeriodType = account.getLockinPeriodType();
		if (command.isLockinPeriodTypeChanged()) {
			lockinPeriodType = PeriodFrequencyType.fromInt(command.getLockinPeriodType());
		}
		account.modifyAccount(product, externalId, savingsDeposit, reccuringInterestRate, savingInterestRate, tenure,
				commencementDate, tenureTypeEnum, savingProductType, savingFrequencyType, savingInterestCalculationMethod,
				isLockinPeriodAllowed, lockinPeriod, lockinPeriodType, this.reccuringDepositInterestCalculator);
		
	}
	
	private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
		List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
		return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
	}


}
