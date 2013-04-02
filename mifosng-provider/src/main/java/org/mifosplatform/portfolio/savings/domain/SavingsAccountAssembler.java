/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activationDateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nominalAnnualInterestRateParamName;

import java.math.BigDecimal;
import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
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
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final SavingsProductRepository savingProductRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountAssembler(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final ClientRepository clientRepository, final GroupRepository groupRepository,
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
     */
    public SavingsAccount assembleFrom(final JsonCommand command) {

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
            client = this.clientRepository.findOne(clientId);
            if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }
        }

        if (groupId != null) {
            group = this.groupRepository.findOne(groupId);
            if (group == null || group.isDeleted()) { throw new GroupNotFoundException(groupId); }
        }

        BigDecimal interestRate = null;
        if (command.parameterExists(nominalAnnualInterestRateParamName)) {
            interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);
        } else {
            interestRate = product.interestRate();
        }

        final boolean active = fromApiJsonHelper.extractBooleanNamed(activeParamName, element);
        final LocalDate activationDate = fromApiJsonHelper.extractLocalDateNamed(activationDateParamName, element);

        SavingsCompoundingInterestPeriodType interestPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        }

        SavingsInterestCalculationType interestCalculationType = null;
        final Integer interestCalculationTypeValue = command.integerValueOfParameterNamed(interestCalculationTypeParamName);
        if (interestCalculationTypeValue != null) {
            interestCalculationType = SavingsInterestCalculationType.fromInt(interestCalculationTypeValue);
        }

        SavingsInterestCalculationDaysInYearType interestCalculationDaysInYearType = null;
        final Integer interestCalculationDaysInYearTypeValue = command
                .integerValueOfParameterNamed(interestCalculationDaysInYearTypeParamName);
        if (interestCalculationDaysInYearTypeValue != null) {
            interestCalculationDaysInYearType = SavingsInterestCalculationDaysInYearType.fromInt(interestCalculationDaysInYearTypeValue);
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

        final SavingsAccount account = SavingsAccount.createNewAccount(client, group, product, accountNo, externalId, interestRate,
                interestPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType);
        account.setHelpers(this.savingsAccountTransactionSummaryWrapper);

        if (active) {
            final Locale locale = command.extractLocale();
            final DateTimeFormatter formatter = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            account.activate(formatter, activationDate);
        }

        return account;
    }
}