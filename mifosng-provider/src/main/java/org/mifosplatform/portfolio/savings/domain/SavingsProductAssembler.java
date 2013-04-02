package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationDaysInYearTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCalculationTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestCompoundingPeriodTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nameParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nominalAnnualInterestRateParamName;

import java.math.BigDecimal;

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

        final BigDecimal minRequiredOpeningBalance = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamed(lockinPeriodFrequencyParamName);
        SavingsPeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        return SavingsProduct.createNew(name, description, currency, interestRate, interestPeriodType, interestCalculationType,
                interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType);
    }
}