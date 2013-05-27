/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activationDateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeOnMonthDayParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeTypeParamName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.savings.exception.SavingsProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service
public class SavingsAccountAssembler {

    private final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final SavingsProductRepository savingProductRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountAssembler(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final ClientRepositoryWrapper clientRepository, final GroupRepository groupRepository,
            final SavingsProductRepository savingProductRepository, final FromJsonHelper fromApiJsonHelper) {
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.savingProductRepository = savingProductRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    /**
     * Assembles a new {@link SavingsAccount} from JSON details passed in
     * request inheriting details where relevant from chosen
     * {@link SavingsProduct}.
     * 
     * @param existingReversedTransactionIds
     * @param existingTransactionIds
     */
    public SavingsAccount assembleFrom(final JsonCommand command, List<Long> existingTransactionIds,
            List<Long> existingReversedTransactionIds) {

        final JsonElement element = command.parsedJson();

        final String accountNo = fromApiJsonHelper.extractStringNamed("accountNo", element);
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);

        final SavingsProduct product = this.savingProductRepository.findOne(productId);
        if (product == null) { throw new SavingsProductNotFoundException(productId); }

        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        final Long groupId = fromApiJsonHelper.extractLongNamed("groupId", element);

        Client client = null;
        Group group = null;
        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        }

        if (groupId != null) {
            group = this.groupRepository.findOne(groupId);
            if (group == null) { throw new GroupNotFoundException(groupId); }
        }

        BigDecimal interestRate = null;
        if (command.parameterExists(nominalAnnualInterestRateParamName)) {
            interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);
        } else {
            interestRate = product.nominalAnnualInterestRate();
        }

        final boolean active = fromApiJsonHelper.extractBooleanNamed(activeParamName, element);
        final LocalDate activationDate = fromApiJsonHelper.extractLocalDateNamed(activationDateParamName, element);

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        } else {
            interestCompoundingPeriodType = product.interestCompoundingPeriodType();
        }

        SavingsInterestPostingPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsInterestPostingPeriodType.fromInt(interestPostingPeriodTypeValue);
        } else {
            interestPostingPeriodType = product.interestPostingPeriodType();
        }

        SavingsInterestCalculationType interestCalculationType = null;
        final Integer interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        if (interestCalculationTypeValue != null) {
            interestCalculationType = SavingsInterestCalculationType.fromInt(interestCalculationTypeValue);
        } else {
            interestCalculationType = product.interestCalculationType();
        }

        SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType = null;
        final Integer interestCalculationDaysInYearTypeValue = command
                .integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        if (interestCalculationDaysInYearTypeValue != null) {
            interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(interestCalculationDaysInYearTypeValue);
        } else {
            interestCalculationDaysInYearType = product.interestCalculationDaysInYearType();
        }

        BigDecimal minRequiredOpeningBalance = null;
        if (command.parameterExists(minRequiredOpeningBalanceParamName)) {
            minRequiredOpeningBalance = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);
        } else {
            minRequiredOpeningBalance = product.minRequiredOpeningBalance();
        }

        Integer lockinPeriodFrequency = null;
        if (command.parameterExists(lockinPeriodFrequencyParamName)) {
            lockinPeriodFrequency = command.integerValueOfParameterNamed(lockinPeriodFrequencyParamName);
        } else {
            lockinPeriodFrequency = product.lockinPeriodFrequency();
        }

        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        Integer lockinPeriodFrequencyTypeValue = null;
        if (command.parameterExists(lockinPeriodFrequencyTypeParamName)) {
            lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
            }
        } else {
            lockinPeriodFrequencyType = product.lockinPeriodFrequencyType();
        }

        BigDecimal withdrawalFeeAmount = null;
        if (command.parameterExists(withdrawalFeeAmountParamName)) {
            withdrawalFeeAmount = command.bigDecimalValueOfParameterNamed(withdrawalFeeAmountParamName);
        } else {
            withdrawalFeeAmount = product.withdrawalFeeAmount();
        }

        SavingsWithdrawalFeesType withdrawalFeeType = null;
        if (command.parameterExists(withdrawalFeeAmountParamName)) {
            final Integer withdrawalFeeTypeValue = command.integerValueOfParameterNamed(withdrawalFeeTypeParamName);
            if (withdrawalFeeTypeValue != null) {
                withdrawalFeeType = SavingsWithdrawalFeesType.fromInt(withdrawalFeeTypeValue);
            }
        } else {
            withdrawalFeeType = product.withdrawalFeeType();
        }

        BigDecimal annualFeeAmount = null;
        if (command.parameterExists(annualFeeAmountParamName)) {
            annualFeeAmount = command.bigDecimalValueOfParameterNamed(annualFeeAmountParamName);
        } else {
            annualFeeAmount = product.annualFeeAmount();
        }

        MonthDay monthDayOfAnnualFee = null;
        if (command.parameterExists(annualFeeOnMonthDayParamName)) {
            monthDayOfAnnualFee = command.extractMonthDayNamed(annualFeeOnMonthDayParamName);
        } else {
            monthDayOfAnnualFee = product.monthDayOfAnnualFee();
        }

        final SavingsAccount account = SavingsAccount.createNewAccount(client, group, product, accountNo, externalId, interestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType,
                annualFeeAmount, monthDayOfAnnualFee);
        account.setHelpers(this.savingsAccountTransactionSummaryWrapper);

        if (active) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            account.activate(formatter, activationDate, existingTransactionIds, existingReversedTransactionIds);
        }

        return account;
    }
}