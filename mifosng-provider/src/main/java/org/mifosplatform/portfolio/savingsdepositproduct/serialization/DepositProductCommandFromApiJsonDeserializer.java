package org.mifosplatform.portfolio.savingsdepositproduct.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class DepositProductCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParams = new HashSet<String>(Arrays.asList("locale", "name", "externalId", "description",
            "currencyCode", "digitsAfterDecimal", "minimumBalance", "maximumBalance", "tenureInMonths", "maturityDefaultInterestRate",
            "maturityMinInterestRate", "maturityMaxInterestRate", "interestCompoundedEvery", "interestCompoundedEveryPeriodType",
            "renewalAllowed", "preClosureAllowed", "preClosureInterestRate", "interestCompoundingAllowed", "isLockinPeriodAllowed",
            "lockinPeriod", "lockinPeriodType"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DepositProductCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.product");
        final JsonElement element = fromApiJsonHelper.parse(json);

        final String name = fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        final String description = fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);

        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank();

        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, Locale.getDefault());
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);

        final BigDecimal minimumBalance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minimumBalance", element);
        baseDataValidator.reset().parameter("minimumBalance").value(minimumBalance).notNull().zeroOrPositiveAmount();

        final BigDecimal maximumBalance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maximumBalance", element);
        baseDataValidator.reset().parameter("maximumBalance").value(maximumBalance).zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("minimumBalance").comapareMinimumAndMaximumAmounts(minimumBalance, maximumBalance);

        final Integer tenureInMonths = fromApiJsonHelper.extractIntegerNamed("tenureInMonths", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureInMonths").value(tenureInMonths).notNull().zeroOrPositiveAmount();

        final BigDecimal maturityDefaultInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityDefaultInterestRate",
                element);
        baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(maturityDefaultInterestRate).notNull()
                .zeroOrPositiveAmount();

        final BigDecimal maturityMinInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityMinInterestRate", element);
        baseDataValidator.reset().parameter("maturityMinInterestRate").value(maturityMinInterestRate).notNull().zeroOrPositiveAmount();

        final BigDecimal maturityMaxInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityMaxInterestRate", element);
        baseDataValidator.reset().parameter("maturityMaxInterestRate").value(maturityMaxInterestRate).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("maturityMinInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(maturityMinInterestRate, maturityMaxInterestRate);
        baseDataValidator.reset().parameter("maturityDefaultInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(maturityDefaultInterestRate, maturityMaxInterestRate);
        baseDataValidator.reset().parameter("maturityMinInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(maturityMinInterestRate, maturityDefaultInterestRate);

        final Integer interestCompoundedEvery = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEvery", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("interestCompoundedEvery").value(interestCompoundedEvery).notNull().zeroOrPositiveAmount();

        final Integer interestCompoundedEveryPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEveryPeriodType",
                element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(interestCompoundedEveryPeriodType).notNull()
                .inMinMaxRange(1, 3);

        final Boolean renewalAllowed = fromApiJsonHelper.extractBooleanNamed("renewalAllowed", element);
        baseDataValidator.reset().parameter("renewalAllowed").value(renewalAllowed)
                .trueOrFalseRequired(isBooleanValueUpdated(renewalAllowed));

        final Boolean preClosureAllowed = fromApiJsonHelper.extractBooleanNamed("preClosureAllowed", element);
        baseDataValidator.reset().parameter("preClosureAllowed").value(preClosureAllowed)
                .trueOrFalseRequired(isBooleanValueUpdated(preClosureAllowed));

        final Boolean interestCompoundingAllowed = fromApiJsonHelper.extractBooleanNamed("interestCompoundingAllowed", element);
        baseDataValidator.reset().parameter("interestCompoundingAllowed").value(interestCompoundingAllowed)
                .trueOrFalseRequired(isBooleanValueUpdated(interestCompoundingAllowed));

        final BigDecimal preClosureInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("preClosureInterestRate", element);
        baseDataValidator.reset().parameter("preClosureInterestRate").value(preClosureInterestRate).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("preClosureInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(preClosureInterestRate, maturityMinInterestRate);

        final Boolean isLockinPeriodAllowed = fromApiJsonHelper.extractBooleanNamed("isLockinPeriodAllowed", element);
        baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(isLockinPeriodAllowed)
                .trueOrFalseRequired(isBooleanValueUpdated(isLockinPeriodAllowed));

        final Integer lockinPeriod = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriod").value(lockinPeriod).notNull().zeroOrPositiveAmount();

        final Integer lockinPeriodType = fromApiJsonHelper.extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriodType").value(lockinPeriodType).notNull().inMinMaxRange(1, 3);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.product");
        final JsonElement element = fromApiJsonHelper.parse(json);

        final String name = fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).ignoreIfNull().notBlank().notExceedingLengthOf(100);

        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        final String description = fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).ignoreIfNull().notExceedingLengthOf(500);

        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).ignoreIfNull().notBlank();

        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, Locale.getDefault());
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).ignoreIfNull().inMinMaxRange(0, 6);

        final BigDecimal minimumBalance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minimumBalance", element);
        baseDataValidator.reset().parameter("minimumBalance").value(minimumBalance).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal maximumBalance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maximumBalance", element);
        baseDataValidator.reset().parameter("maximumBalance").value(maximumBalance).ignoreIfNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("minimumBalance").comapareMinimumAndMaximumAmounts(minimumBalance, maximumBalance);

        final Integer tenureInMonths = fromApiJsonHelper.extractIntegerNamed("tenureInMonths", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureInMonths").value(tenureInMonths).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal maturityDefaultInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityDefaultInterestRate",
                element);
        baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(maturityDefaultInterestRate).ignoreIfNull()
                .zeroOrPositiveAmount();

        final BigDecimal maturityMinInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityMinInterestRate", element);
        baseDataValidator.reset().parameter("maturityMinInterestRate").value(maturityMinInterestRate).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal maturityMaxInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityMaxInterestRate", element);
        baseDataValidator.reset().parameter("maturityMaxInterestRate").value(maturityMaxInterestRate).ignoreIfNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("maturityMinInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(maturityMinInterestRate, maturityMaxInterestRate);
        baseDataValidator.reset().parameter("maturityDefaultInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(maturityDefaultInterestRate, maturityMaxInterestRate);
        baseDataValidator.reset().parameter("maturityMinInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(maturityMinInterestRate, maturityDefaultInterestRate);

        final Integer interestCompoundedEvery = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEvery", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("interestCompoundedEvery").value(interestCompoundedEvery).ignoreIfNull().zeroOrPositiveAmount();

        final Integer interestCompoundedEveryPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEveryPeriodType",
                element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(interestCompoundedEveryPeriodType).ignoreIfNull()
                .inMinMaxRange(1, 3);

        final Boolean renewalAllowed = fromApiJsonHelper.extractBooleanNamed("renewalAllowed", element);
        baseDataValidator.reset().parameter("renewalAllowed").value(renewalAllowed).ignoreIfNull()
                .trueOrFalseRequired(isBooleanValueUpdated(renewalAllowed));

        final Boolean preClosureAllowed = fromApiJsonHelper.extractBooleanNamed("preClosureAllowed", element);
        baseDataValidator.reset().parameter("preClosureAllowed").value(preClosureAllowed).ignoreIfNull()
                .trueOrFalseRequired(isBooleanValueUpdated(preClosureAllowed));

        final Boolean interestCompoundingAllowed = fromApiJsonHelper.extractBooleanNamed("interestCompoundingAllowed", element);
        baseDataValidator.reset().parameter("interestCompoundingAllowed").value(interestCompoundingAllowed).ignoreIfNull()
                .trueOrFalseRequired(isBooleanValueUpdated(interestCompoundingAllowed));

        final BigDecimal preClosureInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("preClosureInterestRate", element);
        baseDataValidator.reset().parameter("preClosureInterestRate").value(preClosureInterestRate).ignoreIfNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("preClosureInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(preClosureInterestRate, maturityMinInterestRate);

        final Boolean lockinPeriodAllowed = fromApiJsonHelper.extractBooleanNamed("isLockinPeriodAllowed", element);
        baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(lockinPeriodAllowed).ignoreIfNull()
                .trueOrFalseRequired(isBooleanValueUpdated(lockinPeriodAllowed));

        final Integer lockinPeriod = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriod").value(lockinPeriod).ignoreIfNull().zeroOrPositiveAmount();

        final Integer lockinPeriodType = fromApiJsonHelper.extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriodType").value(lockinPeriodType).ignoreIfNull().inMinMaxRange(1, 3);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private Boolean isBooleanValueUpdated(final Boolean actualValue) {
        Boolean isUpdated = false;
        if (actualValue != null) {
            isUpdated = true;
        }
        return isUpdated;
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
