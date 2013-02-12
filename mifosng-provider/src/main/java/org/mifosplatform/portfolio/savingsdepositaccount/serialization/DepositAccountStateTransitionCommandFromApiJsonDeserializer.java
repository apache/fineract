package org.mifosplatform.portfolio.savingsdepositaccount.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
public class DepositAccountStateTransitionCommandFromApiJsonDeserializer {
	
	/**
     * The parameters supported for this command.
     */
	
	private final Set<String> supportedParams = new HashSet<String>(Arrays.asList("eventDate", "locale", "dateFormat", "note", "maturesOnDate", "amount" ));
	
	private final FromJsonHelper fromApiJsonHelper;
	
	@Autowired
	public DepositAccountStateTransitionCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateForReject(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final LocalDate eventDate = fromApiJsonHelper.extractLocalDateNamed("eventDate", element);
		baseDataValidator.reset().parameter("eventDate").value(eventDate).notNull();
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
	
	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

	public void validateForWithdrawDepositApplication(String json) {
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final LocalDate eventDate = fromApiJsonHelper.extractLocalDateNamed("eventDate", element);
		baseDataValidator.reset().parameter("eventDate").value(eventDate).notNull();
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForWithdrawDepositAmount(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final LocalDate maturesOnDate = fromApiJsonHelper.extractLocalDateNamed("maturesOnDate", element);
		baseDataValidator.reset().parameter("maturesOnDate").value(maturesOnDate).notNull();
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForWithdrawInterestAmount(String json) {
		
		if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParams);
        
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
        baseDataValidator.reset().parameter("deposit").value(amount).notNull().zeroOrPositiveAmount();
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
	
	

}
