package org.mifosplatform.portfolio.savingsaccountproduct.serialization;

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
public class SavingProductCommandFromApiJsonDeserializer {
	
	Set<String> supportedParams = new HashSet<String>(Arrays.asList("locale", "name", "description", "currencyCode",
            "digitsAfterDecimal", "interestRate", "minInterestRate", "maxInterestRate", "savingsDepositAmount", "savingProductType",
            "tenureType", "tenure", "frequency", "interestType", "interestCalculationMethod", "minimumBalanceForWithdrawal",
            "isPartialDepositAllowed", "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType", "depositEvery"));
	
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public SavingProductCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForCreate(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final String name = fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        
        final String description = fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);
		
        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).notBlank();
        
        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, Locale.getDefault());
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).notNull().inMinMaxRange(0, 6);
        
        final BigDecimal interestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRate", element);
        baseDataValidator.reset().parameter("interestRate").value(interestRate).notNull().zeroOrPositiveAmount();
        
        final BigDecimal minInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minInterestRate", element);
        baseDataValidator.reset().parameter("minInterestRate").value(minInterestRate).notNull().zeroOrPositiveAmount();
        
        final BigDecimal maxInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxInterestRate", element);
        baseDataValidator.reset().parameter("maxInterestRate").value(maxInterestRate).notNull().zeroOrPositiveAmount();
        
        baseDataValidator.reset().parameter("minInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(minInterestRate,maxInterestRate);
        baseDataValidator.reset().parameter("interestRate").comapareMinAndMaxOfTwoBigDecmimalNos(interestRate,maxInterestRate);
        baseDataValidator.reset().parameter("minInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(minInterestRate,interestRate);
        
        final BigDecimal savingsDepositAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("savingsDepositAmount", element);
        baseDataValidator.reset().parameter("savingsDepositAmount").value(savingsDepositAmount).notNull().zeroOrPositiveAmount();
        
        final Integer savingProductType = fromApiJsonHelper.extractIntegerNamed("savingProductType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("savingProductType").value(savingProductType).notNull();
        
        final Integer tenureType = fromApiJsonHelper.extractIntegerNamed("tenureType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureType").value(tenureType).notNull();
        
        final Integer tenure = fromApiJsonHelper.extractIntegerNamed("tenure", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenure").value(tenure).notNull();
        
        final Integer frequency = fromApiJsonHelper.extractIntegerNamed("frequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("frequency").value(frequency).notNull();
        
        final Integer interestType = fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestType").value(interestType).notNull();
        
        final Integer interestCalculationMethod = fromApiJsonHelper.extractIntegerNamed("interestCalculationMethod", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestCalculationMethod").value(interestCalculationMethod).notNull();
        
        final BigDecimal minimumBalanceForWithdrawal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minimumBalanceForWithdrawal", element);
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(minimumBalanceForWithdrawal).notNull().zeroOrPositiveAmount();
        
        final Boolean isPartialDepositAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isPartialDepositAllowed", element);
        baseDataValidator.reset().parameter("isPartialDepositAllowed").value(isPartialDepositAllowedCommandValue)
        .trueOrFalseRequired(isBooleanValueUpdated(isPartialDepositAllowedCommandValue)).notNull();
        
        final Boolean isLockinPeriodAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isLockinPeriodAllowed", element);
		baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(isLockinPeriodAllowedCommandValue).trueOrFalseRequired(isBooleanValueUpdated(isLockinPeriodAllowedCommandValue));
		
		final Integer lockinPeriod = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
		baseDataValidator.reset().parameter("lockinPeriod").value(lockinPeriod).notNull().zeroOrPositiveAmount();
		
		final Integer lockinPeriodType = fromApiJsonHelper.extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
		baseDataValidator.reset().parameter("lockinPeriodType").value(lockinPeriodType).notNull().inMinMaxRange(1, 3);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
		
	}

	public void validateForUpdate(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final String name = fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).ignoreIfNull().notBlank().notExceedingLengthOf(100);
        
        final String description = fromApiJsonHelper.extractStringNamed("description", element);
        baseDataValidator.reset().parameter("description").value(description).notExceedingLengthOf(500);
		
        final String currencyCode = fromApiJsonHelper.extractStringNamed("currencyCode", element);
        baseDataValidator.reset().parameter("currencyCode").value(currencyCode).ignoreIfNull().notBlank();
        
        final Integer digitsAfterDecimal = fromApiJsonHelper.extractIntegerNamed("digitsAfterDecimal", element, Locale.getDefault());
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(digitsAfterDecimal).ignoreIfNull().inMinMaxRange(0, 6);
        
        final BigDecimal interestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("interestRate", element);
        baseDataValidator.reset().parameter("interestRate").value(interestRate).ignoreIfNull().zeroOrPositiveAmount();
        
        final BigDecimal minInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minInterestRate", element);
        baseDataValidator.reset().parameter("minInterestRate").value(minInterestRate).ignoreIfNull().zeroOrPositiveAmount();
        
        final BigDecimal maxInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maxInterestRate", element);
        baseDataValidator.reset().parameter("maxInterestRate").value(maxInterestRate).ignoreIfNull().zeroOrPositiveAmount();
        
        baseDataValidator.reset().parameter("minInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(minInterestRate,maxInterestRate);
        baseDataValidator.reset().parameter("interestRate").comapareMinAndMaxOfTwoBigDecmimalNos(interestRate,maxInterestRate);
        baseDataValidator.reset().parameter("minInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(minInterestRate,interestRate);
        
        final BigDecimal savingsDepositAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("savingsDepositAmount", element);
        baseDataValidator.reset().parameter("savingsDepositAmount").value(savingsDepositAmount).ignoreIfNull().zeroOrPositiveAmount();
        
        final Integer savingProductType = fromApiJsonHelper.extractIntegerNamed("savingProductType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("savingProductType").value(savingProductType).ignoreIfNull();
        
        final Integer tenureType = fromApiJsonHelper.extractIntegerNamed("tenureType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureType").value(tenureType).ignoreIfNull();
        
        final Integer tenure = fromApiJsonHelper.extractIntegerNamed("tenure", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenure").value(tenure).ignoreIfNull().integerGreaterThanZero();
        
        final Integer frequency = fromApiJsonHelper.extractIntegerNamed("frequency", element, Locale.getDefault());
        baseDataValidator.reset().parameter("frequency").value(frequency).ignoreIfNull();
        
        final Integer interestType = fromApiJsonHelper.extractIntegerNamed("interestType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestType").value(interestType).ignoreIfNull();
        
        final Integer interestCalculationMethod = fromApiJsonHelper.extractIntegerNamed("interestCalculationMethod", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestCalculationMethod").value(interestCalculationMethod).ignoreIfNull();
        
        final BigDecimal minimumBalanceForWithdrawal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minimumBalanceForWithdrawal", element);
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(minimumBalanceForWithdrawal).ignoreIfNull().zeroOrPositiveAmount();
        
        final Boolean isPartialDepositAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isPartialDepositAllowed", element);
        baseDataValidator.reset().parameter("isPartialDepositAllowed").value(isPartialDepositAllowedCommandValue)
        .trueOrFalseRequired(isBooleanValueUpdated(isPartialDepositAllowedCommandValue)).ignoreIfNull();
        
        final Boolean isLockinPeriodAllowedCommandValue = fromApiJsonHelper.extractBooleanNamed("isLockinPeriodAllowed", element);
		baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(isLockinPeriodAllowedCommandValue)
		.trueOrFalseRequired(isBooleanValueUpdated(isLockinPeriodAllowedCommandValue)).ignoreIfNull();
		
		final Integer lockinPeriod = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
		baseDataValidator.reset().parameter("lockinPeriod").value(lockinPeriod).ignoreIfNull().zeroOrPositiveAmount();
		
		final Integer lockinPeriodType = fromApiJsonHelper.extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
		baseDataValidator.reset().parameter("lockinPeriodType").value(lockinPeriodType).ignoreIfNull().inMinMaxRange(1, 3);
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
		
	}
	
	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
	
	private Boolean isBooleanValueUpdated(Boolean actualValue) {
		 Boolean isUpdated = false;
		if(actualValue != null){
			isUpdated = true;
		}
		return isUpdated;
	}

}
