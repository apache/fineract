/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccount;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachineImpl;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.FixedTermDepositInterestCalculator;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.DepositAccounDataValidationtException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProduct;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProductRepository;
import org.mifosplatform.portfolio.savingsdepositproduct.exception.DepositProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

/**
 * An assembler for turning {@link DepositAccountCommand} into
 * {@link DepositAccount}'s.
 */
@Service
public class DepositAccountAssembler {

    private final ClientRepository clientRepository;
    private final DepositProductRepository depositProductRepository;
    private final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DepositAccountAssembler(final ClientRepository clientRepository, final DepositProductRepository depositProductRepository,
            final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator, final FromJsonHelper fromApiJsonHelper) {
        this.clientRepository = clientRepository;
        this.depositProductRepository = depositProductRepository;
        this.fixedTermDepositInterestCalculator = fixedTermDepositInterestCalculator;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public DepositAccount assembleFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        final Boolean isInterestWithdrawable = fromApiJsonHelper.extractBooleanNamed("isInterestWithdrawable", element);
        final BigDecimal depositValue = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("deposit", element);
        final Integer tenureInMonthsCommandValue = fromApiJsonHelper.extractIntegerNamed("tenureInMonths", element, Locale.getDefault());
        final BigDecimal maturityInterestRateCommandValue = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityInterestRate",
                element);
        final BigDecimal preClosureInterestRateCommandValue = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("preClosureInterestRate",
                element);
        final Integer interestCompoundedEveryCommandValue = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEvery", element,
                Locale.getDefault());
        final Integer interestCompoundedEveryPeriodTypeCommandValue = fromApiJsonHelper.extractIntegerNamed(
                "interestCompoundedEveryPeriodType", element, Locale.getDefault());
        final Boolean renewalAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("renewalAllowed", element);
        final Boolean preClosureAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("preClosureAllowed", element);
        final Boolean interestCompoundingAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("interestCompoundingAllowed", element);
        final Boolean isLockinPeriodAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isLockinPeriodAllowed", element);
        final Integer lockinPeriodCommandValue = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
        final Integer lockinPeriodTypeCommandValue = fromApiJsonHelper
                .extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
        final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);

        Client client = this.clientRepository.findOne(clientId);
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        DepositProduct product = this.depositProductRepository.findOne(productId);
        if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(productId); }

        // details inherited from product setting (unless allowed to be
        // overridden through account creation api
        Money deposit = Money.of(product.getCurrency(), depositValue);

        Integer tenureInMonths = product.getTenureInMonths();
        if (tenureInMonthsCommandValue != null) {
            tenureInMonths = tenureInMonthsCommandValue;
        }

        BigDecimal maturityInterestRate = product.getMaturityDefaultInterestRate();
        if (maturityInterestRateCommandValue != null) {
            maturityInterestRate = maturityInterestRateCommandValue;
        }

        BigDecimal preClosureInterestRate = product.getPreClosureInterestRate();
        if (preClosureInterestRateCommandValue != null) {
            preClosureInterestRate = preClosureInterestRateCommandValue;
        }

        if (product.getMaturityMinInterestRate().compareTo(preClosureInterestRate) == -1) { throw new DepositAccounDataValidationtException(
                preClosureInterestRate, product.getMaturityMinInterestRate()); }

        Integer compoundingInterestEvery = product.getInterestCompoundedEvery();
        if (interestCompoundedEveryCommandValue != null) {
            compoundingInterestEvery = interestCompoundedEveryCommandValue;
        }

        PeriodFrequencyType compoundingInterestFrequency = product.getInterestCompoundedEveryPeriodType();
        if (interestCompoundedEveryPeriodTypeCommandValue != null) {
            compoundingInterestFrequency = PeriodFrequencyType.fromInt(interestCompoundedEveryPeriodTypeCommandValue);
        }

        boolean renewalAllowed = product.isRenewalAllowed();
        if (isBooleanValueUpdated(renewalAllowedCommandValue)) {
            renewalAllowed = renewalAllowedCommandValue;
        }

        boolean preClosureAllowed = product.isPreClosureAllowed();
        if (isBooleanValueUpdated(preClosureAllowedCommandValue)) {
            preClosureAllowed = preClosureAllowedCommandValue;
        }

        boolean interestCompoundingAllowed = product.isInterestCompoundingAllowed();
        if (isBooleanValueUpdated(interestCompoundingAllowedCommandValue)) {
            interestCompoundingAllowed = interestCompoundingAllowedCommandValue;
        }

        boolean isLockinPeriodAllowed = product.isLockinPeriodAllowed();
        if (isBooleanValueUpdated(isLockinPeriodAllowedCommandValue)) {
            isLockinPeriodAllowed = isLockinPeriodAllowedCommandValue;
        }

        Integer lockinPeriod = product.getLockinPeriod();
        if (lockinPeriodCommandValue != null) {
            lockinPeriod = lockinPeriodCommandValue;
        }

        PeriodFrequencyType lockinPeriodType = product.getLockinPeriodType();
        if (lockinPeriodTypeCommandValue != null) {
            lockinPeriodType = PeriodFrequencyType.fromInt(lockinPeriodTypeCommandValue);
        }
        // end of details allowed to be overriden from product

        DepositAccount account = new DepositAccount().openNew(client, product, externalId, deposit, maturityInterestRate,
                preClosureInterestRate, tenureInMonths, compoundingInterestEvery, compoundingInterestFrequency, commencementDate,
                renewalAllowed, preClosureAllowed, this.fixedTermDepositInterestCalculator, defaultDepositLifecycleStateMachine(),
                isInterestWithdrawable, interestCompoundingAllowed, isLockinPeriodAllowed, lockinPeriod, lockinPeriodType);

        return account;
    }

    private DepositLifecycleStateMachine defaultDepositLifecycleStateMachine() {
        List<DepositAccountStatus> allowedDepositStatuses = Arrays.asList(DepositAccountStatus.values());
        return new DepositLifecycleStateMachineImpl(allowedDepositStatuses);
    }


    public DepositAccount assembleFrom(DepositAccount account, JsonCommand command, Map<String, Object> actualChanges) {
    	
    	final String localeAsInput = command.locale();

        Client client = account.client();

        DepositProduct product = account.product();
        final String productIdParamName = "productId";
        if (command.isChangeInLongParameterNamed(productIdParamName, account.product().getId())) {
            final Long newValue = command.longValueOfParameterNamed(productIdParamName);
            if (newValue != null) {
                product = this.depositProductRepository.findOne(newValue);
                if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(newValue); }
            }
            actualChanges.put(productIdParamName, newValue);
            actualChanges.put("locale", localeAsInput);
        }

        Money deposit = account.getDeposit();
        final String depositParamName = "deposit";
        if (command.isChangeInBigDecimalParameterNamed(depositParamName, deposit.getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(depositParamName);
            actualChanges.put(depositParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            deposit = Money.of(deposit.getCurrency(), newValue);
        }

        Integer tenureInMonths = account.getTenureInMonths();
        final String tenureInMonthsParamName = "tenureInMonths";
        if (command.isChangeInIntegerParameterNamed(tenureInMonthsParamName, tenureInMonths)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureInMonthsParamName);
            actualChanges.put(tenureInMonthsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            tenureInMonths = newValue;
        }

        BigDecimal maturityInterestRate = account.getInterestRate();
        final String maturityInterestRateParamName = "maturityInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(maturityInterestRateParamName, maturityInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maturityInterestRateParamName);
            actualChanges.put(maturityInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            maturityInterestRate = newValue;
        }

        BigDecimal preClosureInterestRate = account.getPreClosureInterestRate();
        final String preClosureInterestRateParamName = "preClosureInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(preClosureInterestRateParamName, preClosureInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(preClosureInterestRateParamName);
            actualChanges.put(preClosureInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            preClosureInterestRate = newValue;
        }

        if (product.getMaturityMinInterestRate().compareTo(preClosureInterestRate) == -1) { throw new DepositAccounDataValidationtException(
                preClosureInterestRate, product.getMaturityMinInterestRate()); }

        Integer interestCompoundedEvery = account.getInterestCompoundedEvery();
        final String interestCompoundedEveryParamName = "interestCompoundedEvery";
        if (command.isChangeInIntegerParameterNamed(interestCompoundedEveryParamName, interestCompoundedEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundedEveryParamName);
            actualChanges.put(interestCompoundedEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestCompoundedEvery = newValue;
        }

        PeriodFrequencyType interestCompoundedEveryPeriodType = account.getInterestCompoundedFrequencyType();
        final String interestCompoundedEveryPeriodTypeParamName = "interestCompoundedEveryPeriodType";
        if (command.isChangeInIntegerParameterNamed(interestCompoundedEveryPeriodTypeParamName,
                interestCompoundedEveryPeriodType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundedEveryPeriodTypeParamName);
            actualChanges.put(interestCompoundedEveryPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestCompoundedEveryPeriodType = PeriodFrequencyType.fromInt(newValue);
        }

        Boolean renewalAllowed = account.isRenewalAllowed();
        final String renewalAllowedParamName = "renewalAllowed";
        if (command.isChangeInBooleanParameterNamed(renewalAllowedParamName, renewalAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(renewalAllowedParamName);
            actualChanges.put(renewalAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            renewalAllowed = newValue;
        }

        Boolean preClosureAllowed = account.isPreClosureAllowed();
        final String preClosureAllowedParamName = "preClosureAllowed";
        if (command.isChangeInBooleanParameterNamed(preClosureAllowedParamName, preClosureAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(preClosureAllowedParamName);
            actualChanges.put(preClosureAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            preClosureAllowed = newValue;
        }

        Boolean isInterestWithdrawable = account.isInterestWithdrawable();
        final String isInterestWithdrawableParamName = "isInterestWithdrawable";
        if (command.isChangeInBooleanParameterNamed(isInterestWithdrawableParamName, isInterestWithdrawable)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isInterestWithdrawableParamName);
            actualChanges.put(isInterestWithdrawableParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            isInterestWithdrawable = newValue;
        }

        Boolean isInterestCompoundingAllowed = account.isInterestCompoundingAllowed();
        final String isInterestCompoundingAllowedParamName = "interestCompoundingAllowed";
        if (command.isChangeInBooleanParameterNamed(isInterestCompoundingAllowedParamName, isInterestCompoundingAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isInterestCompoundingAllowedParamName);
            actualChanges.put(isInterestCompoundingAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            isInterestCompoundingAllowed = newValue;
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

        PeriodFrequencyType lockinPeriodType = account.getLockinPeriodType();
        final String lockinPeriodTypeParamName = "lockinPeriodType";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodTypeParamName, lockinPeriodType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodTypeParamName);
            actualChanges.put(lockinPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            lockinPeriodType = PeriodFrequencyType.fromInt(newValue);
        }

        DepositAccount newAccount = new DepositAccount().openNew(client, product, null, deposit, maturityInterestRate,
                preClosureInterestRate, tenureInMonths, interestCompoundedEvery, interestCompoundedEveryPeriodType, account.maturesOnDate()
                        .plusDays(1), renewalAllowed, preClosureAllowed, this.fixedTermDepositInterestCalculator,
                defaultDepositLifecycleStateMachine(), isInterestWithdrawable, isInterestCompoundingAllowed, isLockinPeriodAllowed,
                lockinPeriod, lockinPeriodType);

        newAccount.updateAccount(account);

        return newAccount;
    }

    public Map<String, Object> assembleUpdatedDepositAccount(final DepositAccount account, final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(20);
        final String localeAsInput = command.locale();

        DepositProduct product = account.product();
        final String productIdParamName = "productId";
        if (command.isChangeInLongParameterNamed(productIdParamName, account.product().getId())) {
            final Long newValue = command.longValueOfParameterNamed(productIdParamName);
            if (newValue != null) {
                product = this.depositProductRepository.findOne(newValue);
                if (product == null || product.isDeleted()) { throw new DepositProductNotFoundException(newValue); }
            }
            actualChanges.put(productIdParamName, newValue);
            actualChanges.put("locale", localeAsInput);
        }

        String externalId = account.getExternalId();
        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            externalId = newValue;
        }

        LocalDate commencementDate = account.getProjectedCommencementDate();
        final String commencementDateParamName = "commencementDate";
        if (command.isChangeInLocalDateParameterNamed("commencementDate", commencementDate)) {
            final LocalDate newValue = command.localDateValueOfParameterNamed(commencementDateParamName);
            actualChanges.put(commencementDateParamName, newValue);
            commencementDate = newValue;
        }

        Money deposit = account.getDeposit();
        final String depositParamName = "deposit";
        if (command.isChangeInBigDecimalParameterNamed(depositParamName, deposit.getAmount())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(depositParamName);
            actualChanges.put(depositParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            deposit = Money.of(deposit.getCurrency(), newValue);
        }

        Integer tenureInMonths = account.getTenureInMonths();
        final String tenureInMonthsParamName = "tenureInMonths";
        if (command.isChangeInIntegerParameterNamed(tenureInMonthsParamName, tenureInMonths)) {
            final Integer newValue = command.integerValueOfParameterNamed(tenureInMonthsParamName);
            actualChanges.put(tenureInMonthsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            tenureInMonths = newValue;
        }

        BigDecimal maturityInterestRate = account.getInterestRate();
        final String maturityInterestRateParamName = "maturityInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(maturityInterestRateParamName, maturityInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maturityInterestRateParamName);
            actualChanges.put(maturityInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            maturityInterestRate = newValue;
        }

        BigDecimal preClosureInterestRate = account.getPreClosureInterestRate();
        final String preClosureInterestRateParamName = "preClosureInterestRate";
        if (command.isChangeInBigDecimalParameterNamed(preClosureInterestRateParamName, preClosureInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(preClosureInterestRateParamName);
            actualChanges.put(preClosureInterestRateParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            preClosureInterestRate = newValue;
        }

        if (product.getMaturityMinInterestRate().compareTo(preClosureInterestRate) == -1) { throw new DepositAccounDataValidationtException(
                preClosureInterestRate, product.getMaturityMinInterestRate()); }

        Integer interestCompoundedEvery = account.getInterestCompoundedEvery();
        final String interestCompoundedEveryParamName = "interestCompoundedEvery";
        if (command.isChangeInIntegerParameterNamed(interestCompoundedEveryParamName, interestCompoundedEvery)) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundedEveryParamName);
            actualChanges.put(interestCompoundedEveryParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestCompoundedEvery = newValue;
        }

        PeriodFrequencyType interestCompoundedEveryPeriodType = account.getInterestCompoundedFrequencyType();
        final String interestCompoundedEveryPeriodTypeParamName = "interestCompoundedEveryPeriodType";
        if (command.isChangeInIntegerParameterNamed(interestCompoundedEveryPeriodTypeParamName,
                interestCompoundedEveryPeriodType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(interestCompoundedEveryPeriodTypeParamName);
            actualChanges.put(interestCompoundedEveryPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            interestCompoundedEveryPeriodType = PeriodFrequencyType.fromInt(newValue);
        }

        Boolean renewalAllowed = account.isRenewalAllowed();
        final String renewalAllowedParamName = "renewalAllowed";
        if (command.isChangeInBooleanParameterNamed(renewalAllowedParamName, renewalAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(renewalAllowedParamName);
            actualChanges.put(renewalAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            renewalAllowed = newValue;
        }

        Boolean preClosureAllowed = account.isPreClosureAllowed();
        final String preClosureAllowedParamName = "preClosureAllowed";
        if (command.isChangeInBooleanParameterNamed(preClosureAllowedParamName, preClosureAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(preClosureAllowedParamName);
            actualChanges.put(preClosureAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            preClosureAllowed = newValue;
        }

        Boolean isInterestWithdrawable = account.isInterestWithdrawable();
        final String isInterestWithdrawableParamName = "isInterestWithdrawable";
        if (command.isChangeInBooleanParameterNamed(isInterestWithdrawableParamName, isInterestWithdrawable)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isInterestWithdrawableParamName);
            actualChanges.put(isInterestWithdrawableParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            isInterestWithdrawable = newValue;
        }

        Boolean isInterestCompoundingAllowed = account.isInterestCompoundingAllowed();
        final String isInterestCompoundingAllowedParamName = "interestCompoundingAllowed";
        if (command.isChangeInBooleanParameterNamed(isInterestCompoundingAllowedParamName, isInterestCompoundingAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isInterestCompoundingAllowedParamName);
            actualChanges.put(isInterestCompoundingAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            isInterestCompoundingAllowed = newValue;
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

        PeriodFrequencyType lockinPeriodType = account.getLockinPeriodType();
        final String lockinPeriodTypeParamName = "lockinPeriodType";
        if (command.isChangeInIntegerParameterNamed(lockinPeriodTypeParamName, lockinPeriodType.getValue())) {
            final Integer newValue = command.integerValueOfParameterNamed(lockinPeriodTypeParamName);
            actualChanges.put(lockinPeriodTypeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            lockinPeriodType = PeriodFrequencyType.fromInt(newValue);
        }

        account.update(product, externalId, commencementDate, deposit, tenureInMonths, maturityInterestRate, preClosureInterestRate,
                interestCompoundedEvery, interestCompoundedEveryPeriodType, renewalAllowed, preClosureAllowed, isInterestWithdrawable,
                isInterestCompoundingAllowed, this.fixedTermDepositInterestCalculator, isLockinPeriodAllowed, lockinPeriod,
                lockinPeriodType);

        return actualChanges;
    }

    public Map<String, Object> updateApprovedDepositAccount(final DepositAccount account, final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(20);
        final String localeAsInput = command.locale();

        Boolean renewalAllowed = account.isRenewalAllowed();
        final String renewalAllowedParamName = "renewalAllowed";
        if (command.isChangeInBooleanParameterNamed(renewalAllowedParamName, renewalAllowed)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(renewalAllowedParamName);
            actualChanges.put(renewalAllowedParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            renewalAllowed = newValue;
        }

        Boolean isInterestWithdrawable = account.isInterestWithdrawable();
        final String isInterestWithdrawableParamName = "isInterestWithdrawable";
        if (command.isChangeInBooleanParameterNamed(isInterestWithdrawableParamName, isInterestWithdrawable)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(isInterestWithdrawableParamName);
            actualChanges.put(isInterestWithdrawableParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            isInterestWithdrawable = newValue;
        }

        account.update(renewalAllowed, isInterestWithdrawable);

        return actualChanges;

    }

    public void postInterest(final DepositAccount account) {
        account.postInterestForDepositAccount(this.fixedTermDepositInterestCalculator);
    }

    private Boolean isBooleanValueUpdated(final Boolean actualValue) {
        Boolean isUpdated = false;
        if (actualValue != null) {
            isUpdated = true;
        }
        return isUpdated;
    }

}