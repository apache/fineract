package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.currencyCodeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.digitsAfterDecimalParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestRateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.interestRatePeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.lockinPeriodFrequencyTypeParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.minRequiredOpeningBalanceParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.nameParamName;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.springframework.stereotype.Component;

@Component
public class SavingsProductAssembler {

    public SavingsProduct assemble(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed(nameParamName);
        final String description = command.stringValueOfParameterNamed(descriptionParamName);

        final String currencyCode = command.stringValueOfParameterNamed(currencyCodeParamName);
        final Integer digitsAfterDecimal = command.integerValueOfParameterNamed(digitsAfterDecimalParamName);
        MonetaryCurrency currency = new MonetaryCurrency(currencyCode, digitsAfterDecimal);

        final BigDecimal interestRate = command.bigDecimalValueOfParameterNamed(interestRateParamName);
        PeriodFrequencyType interestRatePeriodFrequencyType = null;
        final Integer interestRatePeriodFrequencyTypeValue = command.integerValueOfParameterNamed(interestRatePeriodFrequencyTypeParamName);
        if (interestRatePeriodFrequencyTypeValue != null) {
            interestRatePeriodFrequencyType = PeriodFrequencyType.fromInt(interestRatePeriodFrequencyTypeValue);
        }

        final BigDecimal minRequiredOpeningBalance = command.bigDecimalValueOfParameterNamed(minRequiredOpeningBalanceParamName);

        final Integer lockinPeriodFrequency = command.integerValueOfParameterNamed(lockinPeriodFrequencyParamName);
        PeriodFrequencyType lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = command.integerValueOfParameterNamed(lockinPeriodFrequencyTypeParamName);
        if (lockinPeriodFrequencyTypeValue != null) {
            lockinPeriodFrequencyType = PeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
        }

        return SavingsProduct.createNew(name, description, currency, interestRate, interestRatePeriodFrequencyType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType);
    }
}