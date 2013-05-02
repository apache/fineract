package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.annualFeeOnMonthDayParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestPostingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nominalAnnualInterestRateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.withdrawalFeeTypeParamName;

import java.math.BigDecimal;

import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.springframework.stereotype.Component;

@Component
public class SavingsProductAssembler {

    public SavingsProduct assemble(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed(nameParamName);
        final String description = command.stringValueOfParameterNamed(descriptionParamName);

        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
        MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);

        final BigDecimal interestRate = command.bigDecimalValueOfParameterNamed(nominalAnnualInterestRateParamName);

        SavingsCompoundingInterestPeriodType interestCompoundingPeriodType = null;
        final Integer interestPeriodTypeValue = command.integerValueOfParameterNamed(interestCompoundingPeriodTypeParamName);
        if (interestPeriodTypeValue != null) {
            interestCompoundingPeriodType = SavingsCompoundingInterestPeriodType.fromInt(interestPeriodTypeValue);
        }

        SavingsInterestPostingPeriodType interestPostingPeriodType = null;
        final Integer interestPostingPeriodTypeValue = command.integerValueOfParameterNamed(interestPostingPeriodTypeParamName);
        if (interestPostingPeriodTypeValue != null) {
            interestPostingPeriodType = SavingsInterestPostingPeriodType.fromInt(interestPostingPeriodTypeValue);
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

        final BigDecimal minRequiredOpeningBalance = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamed(lockinPeriodFrequencyParamName);
        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        final BigDecimal withdrawalFeeAmount = command.bigDecimalValueOfParameterNamed(withdrawalFeeAmountParamName);
        SavingsWithdrawalFeesType withdrawalFeeType = null;
        final Integer withdrawalFeeTypeValue = command.integerValueOfParameterNamed(withdrawalFeeTypeParamName);
        if (withdrawalFeeTypeValue != null) {
            withdrawalFeeType = SavingsWithdrawalFeesType.fromInt(withdrawalFeeTypeValue);
        }

        final BigDecimal annualFeeAmount = command.bigDecimalValueOfParameterNamed(annualFeeAmountParamName);
        final MonthDay monthDayOfAnnualFee = command.extractMonthDayNamed(annualFeeOnMonthDayParamName);

        return SavingsProduct.createNew(name, description, currency, interestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType, annualFeeAmount,
                monthDayOfAnnualFee);
    }
}