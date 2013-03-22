/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestRateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestRatePeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
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
    private final AprCalculator aprCalculator;

    @Autowired
    public SavingsAccountAssembler(final SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            final ClientRepository clientRepository, final GroupRepository groupRepository,
            final SavingsProductRepository savingProductRepository, final AprCalculator aprCalculator,
            final FromJsonHelper fromApiJsonHelper) {
        this.savingsAccountTransactionSummaryWrapper = savingsAccountTransactionSummaryWrapper;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.savingProductRepository = savingProductRepository;
        this.aprCalculator = aprCalculator;
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
        if (command.parameterExists(interestRateParamName)) {
            interestRate = command.bigDecimalValueOfParameterNamed(interestRateParamName);
        } else {
            interestRate = product.interestRate();
        }

        PeriodFrequencyType interestRatePeriodFrequencyType = null;
        Integer interestRatePeriodFrequencyTypeValue = null;
        if (command.parameterExists(interestRatePeriodFrequencyTypeParamName)) {
            interestRatePeriodFrequencyTypeValue = command.integerValueOfParameterNamed(interestRatePeriodFrequencyTypeParamName);
            if (interestRatePeriodFrequencyTypeValue != null) {
                interestRatePeriodFrequencyType = PeriodFrequencyType.fromInt(interestRatePeriodFrequencyTypeValue);
            }
        } else {
            interestRatePeriodFrequencyType = product.interestRatePeriodFrequencyType();
        }

        final BigDecimal annualInterestRate = this.aprCalculator.calculateFrom(interestRatePeriodFrequencyType, interestRate);

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

        PeriodFrequencyType lockinPeriodFrequencyType = null;
        Integer lockinPeriodFrequencyTypeValue = null;
        if (command.parameterExists(lockinPeriodFrequencyTypeParamName)) {
            lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = PeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
            }
        } else {
            lockinPeriodFrequencyType = product.lockinPeriodFrequencyType();
        }

        final SavingsAccount account = SavingsAccount.createNewAccount(client, group, product, accountNo, externalId, interestRate,
                interestRatePeriodFrequencyType, annualInterestRate, minRequiredOpeningBalance, lockinPeriodFrequency,
                lockinPeriodFrequencyType);
        account.setHelpers(this.savingsAccountTransactionSummaryWrapper);

        return account;
    }
}