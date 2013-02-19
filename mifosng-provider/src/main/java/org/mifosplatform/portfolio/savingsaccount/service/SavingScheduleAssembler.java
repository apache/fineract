package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingSchedulePeriodData;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccount;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class SavingScheduleAssembler {
	
	private final FromJsonHelper fromApiJsonHelper;
	private final SavingProductRepository savingProductRepository;
    private final SavingScheduleGenerator savingScheduleGenerator;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    
    @Autowired
    public SavingScheduleAssembler( final FromJsonHelper fromApiJsonHelper,
    		final SavingProductRepository savingProductRepository,
    		final ApplicationCurrencyRepository applicationCurrencyRepository) {
    	this.fromApiJsonHelper = fromApiJsonHelper;
    	this.savingProductRepository = savingProductRepository;
    	this.savingScheduleGenerator = new SavingScheduleGenerator();
    	this.applicationCurrencyRepository = applicationCurrencyRepository;
	}

	public SavingScheduleData fromJson(JsonElement element) {
		
		SavingScheduleData savingScheduleData = null;
		
		final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
		final Integer depositEvery = fromApiJsonHelper.extractIntegerWithLocaleNamed("depositEvery", element);
        final LocalDate scheduleStartDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
        final BigDecimal interestRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("recurringInterestRate", element);
        final BigDecimal depositAmountPerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("savingsDepositAmountPerPeriod", element);
        final Integer tenure = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("tenure", element);
        
        final Integer tenureTypeCommandValue = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("tenureType", element);
        TenureTypeEnum tenureType = TenureTypeEnum.fromInt(tenureTypeCommandValue);
        final Integer frequencyCommandValue = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("frequency", element);
        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(frequencyCommandValue);
        //final Integer interestTypeCommandValue = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestType", element);
        //SavingsInterestType interestType = SavingsInterestType.fromInt(interestTypeCommandValue);
        final Integer interestCalculationMethodCommandValue = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("interestCalculationMethod", element);
        SavingInterestCalculationMethod interestCalculationMethod = SavingInterestCalculationMethod.fromInt(interestCalculationMethodCommandValue);
        
        final SavingProduct savingProduct = this.savingProductRepository.findOne(productId);
        if (savingProduct == null) throw new SavingProductNotFoundException(productId);
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(savingProduct.getCurrency().getCode());
        
        SavingProductType savingProductType = SavingProductType.fromInt(savingProduct.getSavingProductRelatedDetail().getSavingProductType());
        
		if (savingProductType.isReccuring() && tenureType.isFixedPeriod()) {
			savingScheduleData = savingScheduleGenerator.generate(scheduleStartDate, depositAmountPerPeriod,
					depositEvery, savingFrequencyType, savingProduct, tenure, applicationCurrency,interestRate,interestCalculationMethod);
		} else {
			final Collection<SavingSchedulePeriodData> periods = new ArrayList<SavingSchedulePeriodData>();
			 CurrencyData currencyData = new CurrencyData(applicationCurrency.getCode(), applicationCurrency.getName(),
		                savingProduct.getCurrency().getDigitsAfterDecimal(), applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());
			SavingSchedulePeriodData installment = SavingSchedulePeriodData.addScheduleInformation(1, new LocalDate(scheduleStartDate).plusMonths(depositEvery), depositAmountPerPeriod, BigDecimal.ZERO);
			periods.add(installment);
			savingScheduleData = new SavingScheduleData(currencyData, depositAmountPerPeriod, BigDecimal.ZERO, BigDecimal.ZERO, periods);	
		}
		
		return savingScheduleData;
	}

	public SavingScheduleData calculateSavingSchedule(JsonElement element, SavingAccount account) {
		
		SavingScheduleData savingScheduleData = null;
		
		SavingProduct savingProduct = account.getProduct();
		SavingProductType savingProductType = SavingProductType.fromInt(account.getSavingProductType());
		SavingInterestCalculationMethod interestCalculationMethod =  SavingInterestCalculationMethod.fromInt(account.getInterestCalculationMethod());
		
		TenureTypeEnum tenureType = TenureTypeEnum.fromInt(account.getTenureType());
		final Integer tenureTypeCommandValue = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("tenureType", element);
		if (tenureTypeCommandValue != null) tenureType = TenureTypeEnum.fromInt(tenureTypeCommandValue);
		
		LocalDate scheduleStartDate = new LocalDate(account.getProjectedCommencementDate());
		final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
		if (commencementDate != null) scheduleStartDate = commencementDate ; 
		
		BigDecimal depositAmountPerPeriod = account.getSavingsDepositAmountPerPeriod();
		final BigDecimal savingsDepositAmountPerPeriod = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("savingsDepositAmountPerPeriod", element);
		if (savingsDepositAmountPerPeriod != null ) depositAmountPerPeriod = savingsDepositAmountPerPeriod;
		
		Integer depositEvery = account.getPayEvery();
		final Integer depositEveryCommandValue = fromApiJsonHelper.extractIntegerWithLocaleNamed("depositEvery", element);
		if (depositEveryCommandValue != null ) depositEvery = depositEveryCommandValue;
		
		SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(account.getFrequency());
		final Integer frequencyCommandValue = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("frequency", element);
		if (frequencyCommandValue != null ) savingFrequencyType = SavingFrequencyType.fromInt(frequencyCommandValue);
		
		Integer tenure = account.getTenure();
		final Integer tenureCommandValue = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("tenure", element);
		if (tenureCommandValue != null ) tenure = tenureCommandValue;
		
		BigDecimal interestRate = account.getReccuringInterestRate();
		final BigDecimal recurringInterestRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("recurringInterestRate", element);
		if (recurringInterestRate != null ) interestRate = recurringInterestRate;
		
		final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(savingProduct.getCurrency().getCode());
		
		if (savingProductType.isReccuring() && tenureType.isFixedPeriod()) {
			savingScheduleData = savingScheduleGenerator.generate(scheduleStartDate, depositAmountPerPeriod,
					depositEvery, savingFrequencyType, savingProduct, tenure, applicationCurrency,interestRate,interestCalculationMethod);
		} else {
			final Collection<SavingSchedulePeriodData> periods = new ArrayList<SavingSchedulePeriodData>();
			 CurrencyData currencyData = new CurrencyData(applicationCurrency.getCode(), applicationCurrency.getName(),
		                savingProduct.getCurrency().getDigitsAfterDecimal(), applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());
			SavingSchedulePeriodData installment = SavingSchedulePeriodData.addScheduleInformation(1, new LocalDate(scheduleStartDate).plusMonths(depositEvery), depositAmountPerPeriod, BigDecimal.ZERO);
			periods.add(installment);
			savingScheduleData = new SavingScheduleData(currencyData, depositAmountPerPeriod, BigDecimal.ZERO, BigDecimal.ZERO, periods);	
		}
		
		return savingScheduleData;
	}

}
