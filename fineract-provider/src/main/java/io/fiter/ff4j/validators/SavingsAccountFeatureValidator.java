package io.fiter.ff4j.validators;

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodFrequencyIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.depositPeriodParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.minDepositTermParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.minDepositTermTypeIdParamName;

import com.google.gson.JsonElement;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SavingsAccountFeatureValidator {

    private final FF4j ff4j;
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountFeatureValidator(FF4j ff4j, FromJsonHelper fromApiJsonHelper) {
        this.ff4j = ff4j;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateDepositDetailsForUpdate(final JsonElement element, final DataValidatorBuilder baseDataValidator,
            DepositAccountType depositAccountType) {

        if (ff4j.check(FeatureList.MINIMUM_RD_PERIOD) && depositAccountType.equals(DepositAccountType.RECURRING_DEPOSIT)) {

            Integer minDepositTermType = null;

            if (fromApiJsonHelper.parameterExists(minDepositTermTypeIdParamName, element)) {
                minDepositTermType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(minDepositTermTypeIdParamName, element);
            }

            Integer minTerm = null;
            if (fromApiJsonHelper.parameterExists(minDepositTermParamName, element)) {
                minTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(minDepositTermParamName, element);
            }

            if (fromApiJsonHelper.parameterExists(depositPeriodParamName, element)) {
                minTerm = fromApiJsonHelper.extractIntegerSansLocaleNamed(depositPeriodParamName, element);

            }

            if (fromApiJsonHelper.parameterExists(depositPeriodFrequencyIdParamName, element)) {
                minDepositTermType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(depositPeriodFrequencyIdParamName, element);
            }

            if (minDepositTermType != null && minTerm != null) {

                Integer minDurationAllowed = 0;
                if (minDepositTermType.equals(SavingsPeriodFrequencyType.DAYS.getValue())) {
                    minDurationAllowed = ff4j.getFeature(FeatureList.MINIMUM_RD_PERIOD).getCustomProperties().get("daily").asInt();
                } else {
                    minDurationAllowed = ff4j.getFeature(FeatureList.MINIMUM_RD_PERIOD).getCustomProperties().get("other").asInt();
                }

                if (minDurationAllowed > minTerm) {
                    baseDataValidator.reset().parameter(minDepositTermTypeIdParamName).failWithCodeNoParameterAddedToErrorCode(
                            "period.not.allowed", "Period less than minimum allowed for term type");
                }
            }

        }

    }
}
