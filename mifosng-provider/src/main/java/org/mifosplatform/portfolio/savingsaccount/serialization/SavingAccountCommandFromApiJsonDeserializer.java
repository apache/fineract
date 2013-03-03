package org.mifosplatform.portfolio.savingsaccount.serialization;

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
import org.joda.time.LocalDate;
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
public class SavingAccountCommandFromApiJsonDeserializer {

    final Set<String> supportedParams = new HashSet<String>(Arrays.asList("clientId", "productId", "externalId", "currencyCode",
            "digitsAfterDecimal", "savingsDepositAmountPerPeriod", "recurringInterestRate", "savingInterestRate", "tenure",
            "commencementDate", "locale", "dateFormat", "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType", "tenureType",
            "frequency", "interestType", "interestCalculationMethod", "minimumBalanceForWithdrawal", "isPartialDepositAllowed",
            "depositEvery", "interestPostEvery", "interestPostFrequency"));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingAccountCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
        baseDataValidator.reset().parameter("clientId").value(clientId).notNull();

        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).notNull();

        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        final BigDecimal savingsDepositAmountPerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                "savingsDepositAmountPerPeriod", element);
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(savingsDepositAmountPerPeriod).notNull()
                .zeroOrPositiveAmount();

        final BigDecimal recurringInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("recurringInterestRate", element);
        baseDataValidator.reset().parameter("recurringInterestRate").value(recurringInterestRate).notNull().zeroOrPositiveAmount();

        final BigDecimal savingInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("savingInterestRate", element);
        baseDataValidator.reset().parameter("savingInterestRate").value(savingInterestRate).notNull().zeroOrPositiveAmount();

        final Integer tenure = fromApiJsonHelper.extractIntegerNamed("tenure", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenure").value(tenure).notNull();

        final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
        baseDataValidator.reset().parameter("commencementDate").value(commencementDate).notNull();

        final Boolean isLockinPeriodAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isLockinPeriodAllowed", element);
        baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(isLockinPeriodAllowedCommandValue)
                .trueOrFalseRequired(isBooleanValueUpdated(isLockinPeriodAllowedCommandValue));

        final Integer lockinPeriod = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriod").value(lockinPeriod).notNull().zeroOrPositiveAmount();

        final Integer lockinPeriodType = fromApiJsonHelper.extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriodType").value(lockinPeriodType).notNull().inMinMaxRange(1, 3);

        final Integer tenureType = fromApiJsonHelper.extractIntegerNamed("tenureType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureType").value(tenureType).ignoreIfNull();

        final Integer frequency = fromApiJsonHelper.extractIntegerNamed("frequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("frequency").value(frequency).ignoreIfNull();

        final Integer interestType = fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestType").value(interestType).ignoreIfNull();

        final Integer interestCalculationMethod = fromApiJsonHelper.extractIntegerNamed("interestCalculationMethod", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("interestCalculationMethod").value(interestCalculationMethod).ignoreIfNull();

        final BigDecimal minimumBalanceForWithdrawal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minimumBalanceForWithdrawal",
                element);
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(minimumBalanceForWithdrawal).ignoreIfNull()
                .zeroOrPositiveAmount();

        final Boolean isPartialDepositAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isPartialDepositAllowed", element);
        baseDataValidator.reset().parameter("isPartialDepositAllowed").value(isPartialDepositAllowedCommandValue)
                .trueOrFalseRequired(isBooleanValueUpdated(isPartialDepositAllowedCommandValue)).ignoreIfNull();

        final Integer depositEvery = fromApiJsonHelper.extractIntegerNamed("depositEvery", element, Locale.getDefault());
        baseDataValidator.reset().parameter("depositEvery").value(depositEvery).notNull().zeroOrPositiveAmount();

        final Integer interestPostEvery = fromApiJsonHelper.extractIntegerNamed("interestPostEvery", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestPostEvery").value(interestPostEvery).notNull().zeroOrPositiveAmount();

        final Integer interestPostFrequency = fromApiJsonHelper.extractIntegerNamed("interestPostFrequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestPostFrequency").value(interestPostFrequency).notNull().zeroOrPositiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).ignoreIfNull();

        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        final BigDecimal savingsDepositAmountPerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                "savingsDepositAmountPerPeriod", element);
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(savingsDepositAmountPerPeriod).ignoreIfNull()
                .zeroOrPositiveAmount();

        final BigDecimal recurringInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("recurringInterestRate", element);
        baseDataValidator.reset().parameter("recurringInterestRate").value(recurringInterestRate).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal savingInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("savingInterestRate", element);
        baseDataValidator.reset().parameter("savingInterestRate").value(savingInterestRate).ignoreIfNull().zeroOrPositiveAmount();

        final Integer tenure = fromApiJsonHelper.extractIntegerNamed("tenure", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenure").value(tenure).ignoreIfNull();

        final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
        baseDataValidator.reset().parameter("commencementDate").value(commencementDate).ignoreIfNull();

        final Boolean isLockinPeriodAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isLockinPeriodAllowed", element);
        baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(isLockinPeriodAllowedCommandValue)
                .trueOrFalseRequired(isBooleanValueUpdated(isLockinPeriodAllowedCommandValue));

        final Integer lockinPeriod = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriod").value(lockinPeriod).ignoreIfNull().zeroOrPositiveAmount();

        final Integer lockinPeriodType = fromApiJsonHelper.extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("lockinPeriodType").value(lockinPeriodType).ignoreIfNull().inMinMaxRange(1, 3);

        final Integer tenureType = fromApiJsonHelper.extractIntegerNamed("tenureType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureType").value(tenureType).ignoreIfNull();

        final Integer frequency = fromApiJsonHelper.extractIntegerNamed("frequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("frequency").value(frequency).ignoreIfNull();

        final Integer interestType = fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestType").value(interestType).ignoreIfNull();

        final Integer interestCalculationMethod = fromApiJsonHelper.extractIntegerNamed("interestCalculationMethod", element,
                Locale.getDefault());
        baseDataValidator.reset().parameter("interestCalculationMethod").value(interestCalculationMethod).ignoreIfNull();

        final BigDecimal minimumBalanceForWithdrawal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minimumBalanceForWithdrawal",
                element);
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(minimumBalanceForWithdrawal).ignoreIfNull()
                .zeroOrPositiveAmount();

        final Boolean isPartialDepositAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isPartialDepositAllowed", element);
        baseDataValidator.reset().parameter("isPartialDepositAllowed").value(isPartialDepositAllowedCommandValue)
                .trueOrFalseRequired(isBooleanValueUpdated(isPartialDepositAllowedCommandValue)).ignoreIfNull();

        final Integer depositEvery = fromApiJsonHelper.extractIntegerNamed("depositEvery", element, Locale.getDefault());
        baseDataValidator.reset().parameter("depositEvery").value(depositEvery).ignoreIfNull().zeroOrPositiveAmount();

        final Integer interestPostEvery = fromApiJsonHelper.extractIntegerNamed("interestPostEvery", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestPostEvery").value(interestPostEvery).ignoreIfNull().zeroOrPositiveAmount();

        final Integer interestPostFrequency = fromApiJsonHelper.extractIntegerNamed("interestPostFrequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestPostFrequency").value(interestPostFrequency).ignoreIfNull().zeroOrPositiveAmount();

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