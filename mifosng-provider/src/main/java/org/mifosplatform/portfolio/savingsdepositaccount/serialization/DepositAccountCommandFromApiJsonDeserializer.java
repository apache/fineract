package org.mifosplatform.portfolio.savingsdepositaccount.serialization;

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
public class DepositAccountCommandFromApiJsonDeserializer {
	
	/**
     * The parameters supported for this command.
     */
	
	private final Set<String> supportedParams = new HashSet<String>(Arrays.asList("clientId", "productId", "externalId", "deposit",
            "maturityInterestRate", "preClosureInterestRate", "tenureInMonths", "interestCompoundedEvery",
            "interestCompoundedEveryPeriodType", "commencementDate", "renewalAllowed", "preClosureAllowed",
            "interestCompoundingAllowed", "locale", "dateFormat", "isInterestWithdrawable", "isLockinPeriodAllowed", "lockinPeriod",
            "lockinPeriodType","note"));
	
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public DepositAccountCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}
	
	public void validateForCreate(final String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notNull();
		
		final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
		baseDataValidator.reset().parameter("productId").value(productId).notNull();
        
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        
        final BigDecimal deposit = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("deposit", element);
        baseDataValidator.reset().parameter("deposit").value(deposit).notNull().zeroOrPositiveAmount();
        
		final Integer tenureInMonths = fromApiJsonHelper.extractIntegerNamed("tenureInMonths", element, Locale.getDefault());
		baseDataValidator.reset().parameter("tenureInMonths").value(tenureInMonths).notNull().zeroOrPositiveAmount();
		
		final BigDecimal maturityInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityInterestRate", element);
		baseDataValidator.reset().parameter("maturityInterestRate").value(maturityInterestRate).notNull().zeroOrPositiveAmount();
		
		final Integer interestCompoundedEvery = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEvery", element, Locale.getDefault());
		baseDataValidator.reset().parameter("interestCompoundedEvery").value(interestCompoundedEvery).notNull().zeroOrPositiveAmount();
		
		final Integer interestCompoundedEveryPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEveryPeriodType", element, Locale.getDefault());
		baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(interestCompoundedEveryPeriodType).notNull().inMinMaxRange(1, 3);
		
		final BigDecimal preClosureInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("preClosureInterestRate", element);
		baseDataValidator.reset().parameter("preClosureInterestRate").value(preClosureInterestRate).notNull().zeroOrPositiveAmount();
		
		final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
		baseDataValidator.reset().parameter("commencementDate").value(commencementDate).notNull();
		
		final Boolean isInterestWithdrawable = fromApiJsonHelper.extractBooleanNamed("isInterestWithdrawable", element);
		final Boolean isInterestWithdrawableChanged = isBooleanValueUpdated(isInterestWithdrawable);
		baseDataValidator.reset().parameter("isInterestWithdrawable").trueOrFalseRequired(isInterestWithdrawableChanged);
		
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

	public void validateForApprove(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
		baseDataValidator.reset().parameter("commencementDate").value(commencementDate).notNull();
		
		final BigDecimal deposit = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("deposit", element);
		baseDataValidator.reset().parameter("deposit").value(deposit).ignoreIfNull().zeroOrPositiveAmount();
		
		final BigDecimal maturityInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityInterestRate", element);
        baseDataValidator.reset().parameter("maturityInterestRate").value(maturityInterestRate).ignoreIfNull().zeroOrPositiveAmount();
        
        final Integer tenureInMonths = fromApiJsonHelper.extractIntegerNamed("tenureInMonths", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureInMonths").value(tenureInMonths).ignoreIfNull().integerGreaterThanZero();
        
        final Integer interestCompoundedEveryPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEveryPeriodType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(interestCompoundedEveryPeriodType).ignoreIfNull().inMinMaxRange(2, 2);
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForRenew(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).ignoreIfNull();
		
		final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
        baseDataValidator.reset().parameter("productId").value(productId).ignoreIfNull();
        
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);

        final BigDecimal deposit = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("deposit", element);
        baseDataValidator.reset().parameter("deposit").value(deposit).ignoreIfNull().zeroOrPositiveAmount();
        
        final BigDecimal maturityInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityInterestRate", element);
        baseDataValidator.reset().parameter("maturityInterestRate").value(maturityInterestRate).ignoreIfNull()
                .zeroOrPositiveAmount();
        
        final BigDecimal preClosureInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("preClosureInterestRate", element);
        baseDataValidator.reset().parameter("preClosureInterestRate").value(preClosureInterestRate).ignoreIfNull()
                .zeroOrPositiveAmount();
        
        final Integer tenureInMonths = fromApiJsonHelper.extractIntegerNamed("tenureInMonths", element, Locale.getDefault());
        baseDataValidator.reset().parameter("tenureInMonths").value(tenureInMonths).ignoreIfNull().integerGreaterThanZero();

        final Integer interestCompoundedEvery = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEvery", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestCompoundedEvery").value(interestCompoundedEvery).ignoreIfNull()
                .integerGreaterThanZero();
        
        final Integer interestCompoundedEveryPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEveryPeriodType", element, Locale.getDefault());
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(interestCompoundedEveryPeriodType)
                .ignoreIfNull().inMinMaxRange(2, 2);
        
        final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
        baseDataValidator.reset().parameter("commencementDate").value(commencementDate).ignoreIfNull();
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
		
	}

	public void validateForUpdate(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).ignoreIfNull();
		
		final Long productId = fromApiJsonHelper.extractLongNamed("productId", element);
		baseDataValidator.reset().parameter("productId").value(productId).ignoreIfNull();
        
        final String externalId = fromApiJsonHelper.extractStringNamed("externalId", element);
        baseDataValidator.reset().parameter("externalId").value(externalId).ignoreIfNull().notExceedingLengthOf(100);
        
        final BigDecimal deposit = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("deposit", element);
        baseDataValidator.reset().parameter("deposit").value(deposit).ignoreIfNull().zeroOrPositiveAmount();
        
		final Integer tenureInMonths = fromApiJsonHelper.extractIntegerNamed("tenureInMonths", element, Locale.getDefault());
		baseDataValidator.reset().parameter("tenureInMonths").value(tenureInMonths).ignoreIfNull().zeroOrPositiveAmount();
		
		final BigDecimal maturityInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("maturityInterestRate", element);
		baseDataValidator.reset().parameter("maturityInterestRate").value(maturityInterestRate).ignoreIfNull().zeroOrPositiveAmount();
		
		final Integer interestCompoundedEvery = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEvery", element, Locale.getDefault());
		baseDataValidator.reset().parameter("interestCompoundedEvery").value(interestCompoundedEvery).ignoreIfNull().zeroOrPositiveAmount();
		
		final Integer interestCompoundedEveryPeriodType = fromApiJsonHelper.extractIntegerNamed("interestCompoundedEveryPeriodType", element, Locale.getDefault());
		baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(interestCompoundedEveryPeriodType).ignoreIfNull().inMinMaxRange(1, 3);
		
		final BigDecimal preClosureInterestRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("preClosureInterestRate", element);
		baseDataValidator.reset().parameter("preClosureInterestRate").value(preClosureInterestRate).ignoreIfNull().zeroOrPositiveAmount();
		
		final LocalDate commencementDate = fromApiJsonHelper.extractLocalDateNamed("commencementDate", element);
		baseDataValidator.reset().parameter("commencementDate").value(commencementDate).ignoreIfNull();
		
		final Boolean isInterestWithdrawable = fromApiJsonHelper.extractBooleanNamed("isInterestWithdrawable", element);
		final Boolean isInterestWithdrawableChanged = isBooleanValueUpdated(isInterestWithdrawable);
		baseDataValidator.reset().parameter("isInterestWithdrawable").trueOrFalseRequired(isInterestWithdrawableChanged);
		
		final Integer lockinPeriod = fromApiJsonHelper.extractIntegerNamed("lockinPeriod", element, Locale.getDefault());
		baseDataValidator.reset().parameter("lockinPeriod").value(lockinPeriod).ignoreIfNull().zeroOrPositiveAmount();
		
		final Integer lockinPeriodType = fromApiJsonHelper.extractIntegerNamed("lockinPeriodType", element, Locale.getDefault());
		baseDataValidator.reset().parameter("lockinPeriodType").value(lockinPeriodType).ignoreIfNull().inMinMaxRange(1, 3);
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
		
	}

}
