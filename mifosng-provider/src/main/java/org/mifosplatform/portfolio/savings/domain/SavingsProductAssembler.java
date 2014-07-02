/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.SavingsApiConstants.enforceMinRequiredBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.allowOverdraftParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.chargesParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.idParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.inMultiplesOfParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minBalanceForInterestCalculationParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.overdraftLimitParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.shortNameParamName;
import static org.mifosplatform.portfolio.savings.SavingsApiConstants.withdrawalFeeForTransfersParamName;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.ChargeCannotBeAppliedToException;
import org.mifosplatform.portfolio.loanproduct.exception.InvalidCurrencyException;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Component
public class SavingsProductAssembler {

    private final ChargeRepositoryWrapper chargeRepository;

    @Autowired
    public SavingsProductAssembler(final ChargeRepositoryWrapper chargeRepository) {
        this.chargeRepository = chargeRepository;
    }

    public SavingsProduct assemble(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed(nameParamName);
        final String shortName = command.stringValueOfParameterNamed(shortNameParamName);
        final String description = command.stringValueOfParameterNamed(descriptionParamName);

        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
        final Integer inMultiplesOf = command.integerValueOfParameterNamed(inMultiplesOfParamName);
        final MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal, inMultiplesOf);

        final BigDecimal interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        }

        SavingsPostingInterestPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsPostingInterestPeriodType.fromInt(interestPostingPeriodTypeValue);
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

        final BigDecimal minRequiredOpeningBalance = command
                .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minRequiredOpeningBalanceParamName);

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamedDefaultToNullIfZero(lockinPeriodFrequencyParamName);
        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        boolean iswithdrawalFeeApplicableForTransfer = false;
        if (command.parameterExists(withdrawalFeeForTransfersParamName)) {
            iswithdrawalFeeApplicableForTransfer = command.booleanPrimitiveValueOfParameterNamed(withdrawalFeeForTransfersParamName);
        }

        final AccountingRuleType accountingRuleType = AccountingRuleType.fromInt(command.integerValueOfParameterNamed("accountingRule"));

        // Savings product charges
        final Set<Charge> charges = assembleListOfSavingsProductCharges(command, currencyCode);

        boolean allowOverdraft = false;
        if (command.parameterExists(allowOverdraftParamName)) {
            allowOverdraft = command.booleanPrimitiveValueOfParameterNamed(allowOverdraftParamName);
        }

        final BigDecimal overdraftLimit = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(overdraftLimitParamName);

        boolean enforceMinRequiredBalance = false;
        if (command.parameterExists(enforceMinRequiredBalanceParamName)) {
            enforceMinRequiredBalance = command.booleanPrimitiveValueOfParameterNamed(enforceMinRequiredBalanceParamName);
        }

        final BigDecimal minRequiredBalance = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero(minRequiredBalanceParamName);
        final BigDecimal minBalanceForInterestCalculation = command
                .bigDecimalValueOfParameterNamedDefaultToNullIfZero(minBalanceForInterestCalculationParamName);

        return SavingsProduct.createNew(name, shortName, description, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, iswithdrawalFeeApplicableForTransfer, accountingRuleType, charges,
                allowOverdraft, overdraftLimit, enforceMinRequiredBalance, minRequiredBalance, minBalanceForInterestCalculation);
    }

    public Set<Charge> assembleListOfSavingsProductCharges(final JsonCommand command, final String savingsProductCurrencyCode) {

        final Set<Charge> charges = new HashSet<>();

        if (command.parameterExists(chargesParamName)) {
            final JsonArray chargesArray = command.arrayOfParameterNamed(chargesParamName);
            if (chargesArray != null) {
                for (int i = 0; i < chargesArray.size(); i++) {

                    final JsonObject jsonObject = chargesArray.get(i).getAsJsonObject();
                    if (jsonObject.has(idParamName)) {
                        final Long id = jsonObject.get(idParamName).getAsLong();

                        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(id);

                        if (!charge.isSavingsCharge()) {
                            final String errorMessage = "Charge with identifier " + charge.getId()
                                    + " cannot be applied to Savings product.";
                            throw new ChargeCannotBeAppliedToException("savings.product", errorMessage, charge.getId());
                        }

                        if (!savingsProductCurrencyCode.equals(charge.getCurrencyCode())) {
                            final String errorMessage = "Charge and Savings Product must have the same currency.";
                            throw new InvalidCurrencyException("charge", "attach.to.savings.product", errorMessage);
                        }
                        charges.add(charge);
                    }
                }
            }
        }

        return charges;
    }
}