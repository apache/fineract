/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingSchedulePeriodData;
import org.mifosplatform.portfolio.savingsaccount.domain.ReccuringDepositInterestCalculator;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingAccount;
import org.mifosplatform.portfolio.savingsaccount.domain.SavingScheduleInstallments;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachineImpl;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccountTransactionsException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class SavingAccountAssembler {

    private final ClientRepository clientRepository;
    private final SavingProductRepository savingProductRepository;
    private final ReccuringDepositInterestCalculator reccuringDepositInterestCalculator;
    private final FromJsonHelper fromApiJsonHelper;
    private final SavingScheduleAssembler savingScheduleAssembler;

    @Autowired
    public SavingAccountAssembler( final ClientRepository clientRepository, 
    		final SavingProductRepository savingProductRepository,
            final ReccuringDepositInterestCalculator reccuringDepositInterestCalculator,
            final FromJsonHelper fromApiJsonHelper,
            final SavingScheduleAssembler savingScheduleAssembler) {
        this.clientRepository = clientRepository;
        this.savingProductRepository = savingProductRepository;
        this.reccuringDepositInterestCalculator = reccuringDepositInterestCalculator;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.savingScheduleAssembler = savingScheduleAssembler;
    }

    public SavingAccount assembleFrom(JsonCommand command) {
    	
    	final JsonElement element = command.parsedJson();
    	final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed("digitsAfterDecimal");
        final BigDecimal savingsDepositAmountPerPeriod = command.bigDecimalValueOfParameterNamed("savingsDepositAmountPerPeriod");
        final BigDecimal recurringInterestRate = command.bigDecimalValueOfParameterNamed("recurringInterestRate");
        final BigDecimal savingInterestRateCommandValue = command.bigDecimalValueOfParameterNamed("savingInterestRate");
        final Integer tenureCommandValue = command.integerValueOfParameterNamed("tenure");
        final LocalDate commencementDate = command.localDateValueOfParameterNamed("commencementDate");
        final Integer lockinPeriodTypeCommandValue = command.integerValueOfParameterNamed("lockinPeriodType");
        final boolean isLockinPeriodAllowedCommandValue = command.booleanPrimitiveValueOfParameterNamed("isLockinPeriodAllowed");
        final Integer lockinPeriodCommandValue = command.integerValueOfParameterNamed("lockinPeriod");
        final Integer tenureTypeCommandValue = command.integerValueOfParameterNamed("tenureType");
        final Integer frequencyCommandValue = command.integerValueOfParameterNamed("frequency");
        final Integer interestTypeCommandValue = command.integerValueOfParameterNamed("interestType");
        final Integer interestCalculationMethodCommandValue = command.integerValueOfParameterNamed("interestCalculationMethod");
       // final BigDecimal minimumBalanceForWithdrawal = command.bigDecimalValueOfParameterNamed("minimumBalanceForWithdrawal");
       // final boolean isPartialDepositAllowed = command.booleanPrimitiveValueOfParameterNamed("isPartialDepositAllowed");
        final Integer depositEvery = command.integerValueOfParameterNamed("depositEvery");
        final Integer interestPostEvery = command.integerValueOfParameterNamed("interestPostEvery");
        final Integer interestPostFrequencyCommandValue = command.integerValueOfParameterNamed("interestPostFrequency");
        

        Client client = this.clientRepository.findOne(clientId);
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        SavingProduct product = this.savingProductRepository.findOne(productId);
        if (product == null || product.isDeleted()) { throw new SavingProductNotFoundException(productId); }

        MonetaryCurrency currency = product.getCurrency();
        if (currencyCode != null) currency = new MonetaryCurrency(currencyCode, currency.getDigitsAfterDecimal());
        if (digitsAfterDecimal != null) currency = new MonetaryCurrency(currency.getCode(), digitsAfterDecimal);

        Money savingsDeposit = Money.of(currency, product.getSavingProductRelatedDetail().getSavingsDepositAmount());
        if (savingsDepositAmountPerPeriod != null) {
			savingsDeposit = Money.of(currency, savingsDepositAmountPerPeriod);
		}
        
        Integer tenure = product.getSavingProductRelatedDetail().getTenure();
        if (tenureCommandValue != null) {
            tenure = tenureCommandValue;
        }

        TenureTypeEnum tenureTypeEnum = TenureTypeEnum.fromInt(product.getSavingProductRelatedDetail().getTenureType());
        if (tenureTypeCommandValue != null) {
            tenureTypeEnum = TenureTypeEnum.fromInt(tenureTypeCommandValue);
        }

        SavingProductType savingProductType = SavingProductType.fromInt(product.getSavingProductRelatedDetail().getSavingProductType());

        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(product.getSavingProductRelatedDetail().getFrequency());
        if (frequencyCommandValue != null) {
            savingFrequencyType = SavingFrequencyType.fromInt(frequencyCommandValue);
        }

        SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(product.getSavingProductRelatedDetail()
                .getInterestCalculationMethod());
        if (interestCalculationMethodCommandValue != null) {
            savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(interestCalculationMethodCommandValue);
        }

        SavingsInterestType interestType = SavingsInterestType.fromInt(product.getSavingProductRelatedDetail().getInterestType());
        if (interestTypeCommandValue != null) {
            interestType = SavingsInterestType.fromInt(interestTypeCommandValue);
        }

        BigDecimal savingInterestRate = product.getInterestRate();
        if (savingInterestRateCommandValue != null) savingInterestRate = savingInterestRateCommandValue;

        boolean isLockinPeriodAllowed = product.getSavingProductRelatedDetail().isLockinPeriodAllowed();
        if (isBooleanValueUpdated(isLockinPeriodAllowedCommandValue)) {
            isLockinPeriodAllowed = isLockinPeriodAllowedCommandValue;
        }

        Integer lockinPeriod = product.getSavingProductRelatedDetail().getLockinPeriod();
        if (lockinPeriodCommandValue != null) {
            lockinPeriod = lockinPeriodCommandValue;
        }

        PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(product.getSavingProductRelatedDetail().getLockinPeriodType());
        if (lockinPeriodTypeCommandValue != null) {
            lockinPeriodType = PeriodFrequencyType.fromInt(lockinPeriodTypeCommandValue);
        }
        
        Integer interestPostFrequency = PeriodFrequencyType.fromInt(interestPostFrequencyCommandValue).getValue();

        SavingAccount account = SavingAccount.openNew(client, product, externalId, savingsDeposit, recurringInterestRate,
                savingInterestRate, tenure, commencementDate, tenureTypeEnum, savingProductType, savingFrequencyType,
                interestType, savingInterestCalculationMethod, isLockinPeriodAllowed, lockinPeriod, lockinPeriodType,
                this.reccuringDepositInterestCalculator, defaultDepositLifecycleStateMachine(),depositEvery,interestPostEvery, interestPostFrequency);

        SavingScheduleData savingScheduleData = this.savingScheduleAssembler.fromJson(element);
        for (SavingSchedulePeriodData savingSchedulePeriodData : savingScheduleData.getPeriods()) {

            final SavingScheduleInstallments installment = new SavingScheduleInstallments(account, savingSchedulePeriodData.getDueDate()
                    .toDate(), savingSchedulePeriodData.getPeriod(), savingSchedulePeriodData.getDepositDue(), savingSchedulePeriodData.getInterestAccured());
            account.addSavingScheduleInstallment(installment);
        }

        return account;
    }

    public Map<String, Object> assembleFrom(JsonCommand command, SavingAccount account) {
    	
    	JsonElement element = command.parsedJson();
    	Map<String, Object> actualChanges =  new LinkedHashMap<String, Object>(20);
    	final String localeAsInput = command.locale();

        SavingProduct product = account.getProduct();
        final String productIdParamName = "productId";
        if (command.isChangeInLongParameterNamed(productIdParamName, product.getId())) {
            final Long newValue = command.longValueOfParameterNamed(productIdParamName);
            if (newValue != null) {
                product = this.savingProductRepository.findOne(newValue);
                actualChanges.put("productId", newValue);
                if (product == null || product.isDeleted()) throw new SavingProductNotFoundException(newValue);
            }
        }

        String externalId = account.getExternalId();
        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            externalId = newValue;
        }

        Money savingsDeposit = Money.of(product.getCurrency(), account.getSavingsDepositAmountPerPeriod());
        
        final String savingsDepositAmountPerPeriodParamName = "savingsDepositAmountPerPeriod";
        if (command.isChangeInBigDecimalParameterNamed(savingsDepositAmountPerPeriodParamName, savingsDeposit.getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(savingsDepositAmountPerPeriodParamName);
            actualChanges.put(savingsDepositAmountPerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            savingsDeposit = Money.of(savingsDeposit.getCurrency(), newValue);
        }
        
        Integer tenure = account.getTenure();
        final String tenureParamName = "tenure";
        if (command.isChangeInIntegerParameterNamed(tenureParamName, tenure)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureParamName);
            actualChanges.put(tenureParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            tenure = newValue;
        }
        
        LocalDate commencementDate = new LocalDate(account.getProjectedCommencementDate());
        final String commencementDateParamName = "commencementDate";
        if (command.isChangeInLocalDateParameterNamed("commencementDate", commencementDate)) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(commencementDateParamName);
            actualChanges.put(commencementDateParamName, newValue);
            commencementDate = newValue;
        }
        
        TenureTypeEnum tenureTypeEnum = TenureTypeEnum.fromInt(account.getTenureType());
        final String tenureTypeParamName = "tenureType";
        if (command.isChangeInIntegerParameterNamed(tenureTypeParamName, tenureTypeEnum.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureTypeParamName);
            actualChanges.put(tenureTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            tenureTypeEnum = TenureTypeEnum.fromInt(newValue);
        }
        
        SavingProductType savingProductType = SavingProductType.fromInt(account.getSavingProductType());

        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(account.getFrequency());
        final String frequencyParamName = "frequency";
        if (command.isChangeInIntegerParameterNamed(frequencyParamName, savingFrequencyType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(frequencyParamName);
            actualChanges.put(frequencyParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            savingFrequencyType = SavingFrequencyType.fromInt(newValue);
        }

        SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(account.getInterestCalculationMethod());
        final String interestCalculationMethodParamName = "interestCalculationMethod";
        if (command.isChangeInIntegerParameterNamed(interestCalculationMethodParamName, savingInterestCalculationMethod.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCalculationMethodParamName);
            actualChanges.put(interestCalculationMethodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(newValue);
        }

        BigDecimal reccuringInterestRate = account.getReccuringInterestRate();
        final String reccuringInterestRateParamName = "reccuringInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(reccuringInterestRateParamName, reccuringInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(reccuringInterestRateParamName);
            actualChanges.put(reccuringInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            reccuringInterestRate = newValue;
        }
        
        BigDecimal savingInterestRate = account.getSavingInterestRate();
        final String savingInterestRateParamName = "savingInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(savingInterestRateParamName, savingInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(savingInterestRateParamName);
            actualChanges.put(savingInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            savingInterestRate = newValue;
        }
        
        Boolean isLockinPeriodAllowed = account.isLockinPeriodAllowed();
        final String isLockinPeriodAllowedParamName = "isLockinPeriodAllowed";
        if (command.isChangeInBooleanParameterNamed(isLockinPeriodAllowedParamName, isLockinPeriodAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isLockinPeriodAllowedParamName);
            actualChanges.put(isLockinPeriodAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            isLockinPeriodAllowed = newValue;
        }

        Integer lockinPeriod = account.getLockinPeriod();
        final String lockinPeriodParamName = "lockinPeriod";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodParamName, lockinPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodParamName);
            actualChanges.put(lockinPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            lockinPeriod = newValue;
        }

        PeriodFrequencyType lockinPeriodType = PeriodFrequencyType.fromInt(account.getLockinPeriodType());
        final String lockinPeriodTypeParamName = "lockinPeriodType";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodTypeParamName, lockinPeriodType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodTypeParamName);
            actualChanges.put(lockinPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            lockinPeriodType = PeriodFrequencyType.fromInt(newValue);
        }

        Integer payEvery = account.getPayEvery();
        final String depositEveryParamName = "depositEvery";
        if (command.isChangeInIntegerParameterNamed(depositEveryParamName, payEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(depositEveryParamName);
            actualChanges.put(depositEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            payEvery = newValue;
        }
        
        SavingsInterestType interestType = SavingsInterestType.fromInt(product.getSavingProductRelatedDetail().getInterestType());
        final String interestTypeParamName = "interestType";
        if (command.isChangeInIntegerParameterNamed(interestTypeParamName, interestType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestTypeParamName);
            actualChanges.put(interestTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestType = SavingsInterestType.fromInt(newValue);
        }
        
        Integer interestPostEvery = account.getInterestPostEvery();
        final String interestPostEveryParamName = "interestPostEvery";
        if (command.isChangeInIntegerParameterNamed(interestPostEveryParamName, interestPostEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestPostEveryParamName);
            actualChanges.put(interestPostEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestPostEvery = newValue;
        }

        Integer interestPostFrequency =account.getInterestPostFrequency();
        final String interestPostFrequencyParamName = "interestPostFrequency";
        if (command.isChangeInIntegerParameterNamed(interestPostFrequencyParamName, interestPostFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestPostFrequencyParamName);
            actualChanges.put(interestPostFrequencyParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestPostFrequency = PeriodFrequencyType.fromInt(newValue).getValue();
        }

        account.modifyAccount(product, externalId, savingsDeposit, reccuringInterestRate, savingInterestRate, tenure, commencementDate,
                tenureTypeEnum, savingProductType, savingFrequencyType, savingInterestCalculationMethod, isLockinPeriodAllowed,
                lockinPeriod, lockinPeriodType, this.reccuringDepositInterestCalculator,payEvery, interestType, interestPostEvery, interestPostFrequency);
        
        SavingScheduleData savingScheduleData = this.savingScheduleAssembler.fromJson(element);
        for (SavingSchedulePeriodData savingSchedulePeriodData : savingScheduleData.getPeriods()) {

            final SavingScheduleInstallments installment = new SavingScheduleInstallments(account, savingSchedulePeriodData.getDueDate()
                    .toDate(), savingSchedulePeriodData.getPeriod(), savingSchedulePeriodData.getDepositDue(), savingSchedulePeriodData.getInterestAccured());
            account.addSavingScheduleInstallment(installment);
        }
        
        return actualChanges;

    }

    private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }

	public Map<String, Object> approveSavingAccount(JsonCommand command, SavingAccount account) {
		
		JsonElement element = command.parsedJson();
    	Map<String, Object> actualChanges =  new LinkedHashMap<String, Object>(20);
    	final String localeAsInput = command.locale();

    	LocalDate approvalDate = account.projectedCommencementDate();
		final String commencementDateParamName = "commencementDate";
        if (command.isChangeInLocalDateParameterNamed("commencementDate", approvalDate)) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(commencementDateParamName);
            actualChanges.put(commencementDateParamName, newValue);
            approvalDate = newValue;
        }
		
		BigDecimal savingsDepositAmountPerPeriod=account.getSavingsDepositAmountPerPeriod();
		final String savingsDepositAmountPerPeriodParamName = "savingsDepositAmountPerPeriod";
		if (command.isChangeInBigDecimalParameterNamed(savingsDepositAmountPerPeriodParamName, savingsDepositAmountPerPeriod)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(savingsDepositAmountPerPeriodParamName);
            actualChanges.put(savingsDepositAmountPerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            savingsDepositAmountPerPeriod = newValue;
        }
		
		BigDecimal recurringInterestRate = account.getReccuringInterestRate();
		final String reccuringInterestRateParamName = "reccuringInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(reccuringInterestRateParamName, recurringInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(reccuringInterestRateParamName);
            actualChanges.put(reccuringInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            recurringInterestRate = newValue;
        }
		
		BigDecimal savingInterestRate = account.getSavingInterestRate();
		final String savingInterestRateParamName = "savingInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(savingInterestRateParamName, savingInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(savingInterestRateParamName);
            actualChanges.put(savingInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            savingInterestRate = newValue;
        }
		
		Integer interestType = account.getInterestType();
		final String interestTypeParamName = "interestType";
        if (command.isChangeInIntegerParameterNamed(interestTypeParamName, interestType)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestTypeParamName);
            actualChanges.put(interestTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestType = SavingsInterestType.fromInt(newValue).getValue();
        }
		
		Integer tenure = account.getTenure();
		final String tenureParamName = "tenure";
        if (command.isChangeInIntegerParameterNamed(tenureParamName, tenure)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureParamName);
            actualChanges.put(tenureParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            tenure = newValue;
        }
		
		Integer tenureType = account.getTenureType();
		final String tenureTypeParamName = "tenureType";
        if (command.isChangeInIntegerParameterNamed(tenureTypeParamName, tenureType)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureTypeParamName);
            actualChanges.put(tenureTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            tenureType = TenureTypeEnum.fromInt(newValue).getValue();
        }
		
		Integer frequency = account.getFrequency();
		final String frequencyParamName = "frequency";
        if (command.isChangeInIntegerParameterNamed(frequencyParamName, frequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(frequencyParamName);
            actualChanges.put(frequencyParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            frequency = SavingFrequencyType.fromInt(newValue).getValue();
        }
		
		Integer depositEvery = account.getPayEvery();
		final String depositEveryParamName = "depositEvery";
        if (command.isChangeInIntegerParameterNamed(depositEveryParamName, depositEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(depositEveryParamName);
            actualChanges.put(depositEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            depositEvery = newValue;
        }
		
		Integer interestPostEvery = account.getInterestPostEvery();
		final String interestPostEveryParamName = "interestPostEvery";
        if (command.isChangeInIntegerParameterNamed(interestPostEveryParamName, interestPostEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestPostEveryParamName);
            actualChanges.put(interestPostEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestPostEvery = newValue;
        }
        
		Integer interestPostFrequency =account.getInterestPostFrequency();
		final String interestPostFrequencyParamName = "interestPostFrequency";
        if (command.isChangeInIntegerParameterNamed(interestPostFrequencyParamName, interestPostFrequency)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestPostFrequencyParamName);
            actualChanges.put(interestPostFrequencyParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestPostFrequency = PeriodFrequencyType.fromInt(newValue).getValue();
        }
		
		account.approveSavingAccount(approvalDate, savingsDepositAmountPerPeriod, recurringInterestRate, savingInterestRate, interestType,
				tenure, tenureType, frequency, depositEvery,defaultDepositLifecycleStateMachine(),this.reccuringDepositInterestCalculator, interestPostEvery, interestPostFrequency);
		
		
		/*CalculateSavingScheduleCommand calculateSavingScheduleCommand = new CalculateSavingScheduleCommand(account.getProduct().getId(), savingsDepositAmountPerPeriod, payEvery,
				frequency, recurringInterestRate, approvalDate, tenure,tenureType,interestPostEvery,interestPostFrequency,account.getInterestCalculationMethod());*/
		
		SavingScheduleData savingScheduleData = this.savingScheduleAssembler.calculateSavingSchedule(element,account);
        for (SavingSchedulePeriodData savingSchedulePeriodData : savingScheduleData.getPeriods()) {

            final SavingScheduleInstallments installment = new SavingScheduleInstallments(account, savingSchedulePeriodData.getDueDate()
                    .toDate(), savingSchedulePeriodData.getPeriod(), savingSchedulePeriodData.getDepositDue(),savingSchedulePeriodData.getInterestAccured());
            account.addSavingScheduleInstallment(installment);
        }
        
        return actualChanges;
	}

	public void withdrawSavingAccountMoney(	JsonCommand command, SavingAccount account) {
		
		LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
		BigDecimal withdraAmount = command.bigDecimalValueOfParameterNamed("amount");
		if (withdraAmount.doubleValue() > account.getOutstandingAmount().doubleValue()) {
			throw new DepositAccountTransactionsException("deposit.transaction.interest.withdrawal.exceed", "You can Withdraw "
                    + account.getOutstandingAmount() + " only"); 
		}
		
		account.withdrawAmount(withdraAmount, transactionDate);
		
	}

	public void postInterest(SavingAccount account) {
		account.postInterest();
	}
	
	private Boolean isBooleanValueUpdated(Boolean actualValue) {
		 Boolean isUpdated = false;
		if(actualValue != null){
			isUpdated = true;
		}
		return isUpdated;
	}

}