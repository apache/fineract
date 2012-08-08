package org.mifosng.platform;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.ApiParameterError;
import org.springframework.util.ObjectUtils;

public class DataValidatorBuilder {

	private final List<ApiParameterError> dataValidationErrors;
	private String resource;
	private String parameter;
	private Object value;
	private boolean ignoreNullValue = false;

	public DataValidatorBuilder(List<ApiParameterError> dataValidationErrors) {
		this.dataValidationErrors = dataValidationErrors;
	}
	
	public DataValidatorBuilder reset() {
		return new DataValidatorBuilder(dataValidationErrors).resource(resource);
	}

	public DataValidatorBuilder resource(final String resource) {
		this.resource = resource;
		return this;
	}

	public DataValidatorBuilder parameter(final String parameter) {
		this.parameter = parameter;
		return this;
	}

	public DataValidatorBuilder value(Object value) {
		this.value = value;
		return this;
	}
	
	public DataValidatorBuilder ignoreIfNull() {
		this.ignoreNullValue = true;
		return this;
	}
	

	public DataValidatorBuilder andNotBlank(String linkedParameterName, String linkedValue) {
		if (value == null && linkedValue == null && ignoreNullValue) {
			return this;
		}
		
		if (StringUtils.isBlank(linkedValue)) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(linkedParameterName).append(".cannot.be.empty.when.").append(parameter).append(".is.populated");
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(linkedParameterName).append(" cannot be empty when ").append(parameter).append(" is populated.");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), linkedParameterName, linkedValue, value);
			dataValidationErrors.add(error);
		}
		return this;
	}

	public DataValidatorBuilder equalToParameter(String linkedParameterName, Object linkedValue) {
		if (value == null && linkedValue == null && ignoreNullValue) {
			return this;
		}
		
		if (value != null && !value.equals(linkedValue)) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(linkedParameterName).append(".not.equal.to.").append(parameter);
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(linkedParameterName).append(" is not equal to ").append(parameter).append(".");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), linkedParameterName, linkedValue, value);
			dataValidationErrors.add(error);
		}
		return this;
	}
	
	public DataValidatorBuilder notSameAsParameter(String linkedParameterName, Object linkedValue) {
		if (value == null && linkedValue == null && ignoreNullValue) {
			return this;
		}
		
		if (value != null && value.equals(linkedValue)) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(linkedParameterName).append(".same.as.").append(parameter);
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(linkedParameterName).append(" is same as ").append(parameter).append(".");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), linkedParameterName, linkedValue, value);
			dataValidationErrors.add(error);
		}
		return this;
	}
	
	public DataValidatorBuilder notNull() {
		if (value == null && !ignoreNullValue) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".cannot.be.blank");
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" cannot be blank.");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter);
			dataValidationErrors.add(error);
		}
		return this;
	}

	public DataValidatorBuilder notBlank() {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		if (value == null || StringUtils.isBlank(value.toString())) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".cannot.be.blank");
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" cannot be blank.");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter);
			dataValidationErrors.add(error);
		}
		return this;
	}

	public DataValidatorBuilder notExceedingLengthOf(Integer maxLength) {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		if (value != null && value.toString().trim().length() > maxLength) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".exceeds.max.length");
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" exceeds max length of ").append(maxLength).append(".");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, maxLength, value.toString());
			dataValidationErrors.add(error);
		}
		return this;
	}

	public DataValidatorBuilder inMinMaxRange(Integer min, Integer max) {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		if (value != null) {
			Integer number = Integer.valueOf(value.toString());
			if (number < min || number > max) {
				StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".is.not.within.expected.range");
				StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" must be between ").append(min).append(" and ").append(max).append(".");
				ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, number, min, max);
				dataValidationErrors.add(error);
			}
		}
		return this;
	}
	
	public DataValidatorBuilder positiveAmount() {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		if (value != null) {
			BigDecimal number = BigDecimal.valueOf(Double.valueOf(value.toString()));
			if (number.compareTo(BigDecimal.ZERO) <= 0) {
				StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".not.greater.than.zero");
				StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" must be greater than 0.");
				ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, number, 0);
				dataValidationErrors.add(error);
			}
		}
		return this;
	}
	
	public DataValidatorBuilder zeroOrPositiveAmount() {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		if (value != null) {
			BigDecimal number = BigDecimal.valueOf(Double.valueOf(value.toString()));
			if (number.compareTo(BigDecimal.ZERO) < 0) {
				StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".not.zero.or.greater");
				StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" must be greater than or equal to 0.");
				ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, number, 0);
				dataValidationErrors.add(error);
			}
		}
		return this;
	}
	
	public DataValidatorBuilder greaterThanZero() {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		if (value != null) {
			Integer number = Integer.valueOf(value.toString());
			if (number < 1) {
				StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".not.greater.than.zero");
				StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" must be greater than 0.");
				ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, number, 0);
				dataValidationErrors.add(error);
			}
		}
		return this;
	}
	
	public DataValidatorBuilder arrayNotEmpty() {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		Object[] array = (Object[]) value;
		if (ObjectUtils.isEmpty(array)) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".cannot.be.empty");
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" cannot be empty. You must select at least one.");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter);
			dataValidationErrors.add(error);
		}
		return this;
	}

	public DataValidatorBuilder anyOfNotNull(Object... object) {
		boolean hasData = false;
		for (Object obj : object) {
			if (obj != null) {
				hasData = true;
				break;
			}
		}
		
		if (!hasData) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".no.parameters.for.update");
			StringBuilder defaultEnglishMessage = new StringBuilder("No parameters passed for update.");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), "id");
			dataValidationErrors.add(error);
		}
		return this;
	}

	public DataValidatorBuilder inValidValue(String parameterValueCode, Object invalidValue) {
		StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".invalid.").append(parameterValueCode);
		StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" has an invalid value.");
		ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, invalidValue);
		dataValidationErrors.add(error);
		return this;
	}

	public DataValidatorBuilder mustBeBlankWhenParameterProvided(String parameterName, Object parameterValue) {
		if (value == null && ignoreNullValue) {
			return this;
		}
		
		if (value == null && parameterValue != null) {
			return this;
		}
		
		if (value != null && StringUtils.isBlank(value.toString()) && parameterValue != null && StringUtils.isNotBlank(parameterValue.toString())) {
			return this;
		}
		
		StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".").append(parameter).append(".cannot.also.be.provided.when.").append(parameterName).append(".is.populated");
		StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(parameter).append(" cannot also be provided when ").append(parameterName).append(" is populated.");
		ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, value, parameterName, parameterValue);
		dataValidationErrors.add(error);
		return this;
	}
	
	public DataValidatorBuilder comapareMinimumAndMaximumAmounts(BigDecimal minimumBalance,BigDecimal maximumBalance){
		if(minimumBalance!=null&&maximumBalance!=null)
		if(maximumBalance.compareTo(minimumBalance)==-1){
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(resource).append(".minimum.amount.should.lessthan.maximun.amount");
			StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append(" minimum amount ").append(minimumBalance).append(" should less than maximum amount ").append(maximumBalance).append(".");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), parameter, minimumBalance, maximumBalance);
			dataValidationErrors.add(error);
			return this;
		}
		return this;
	}
}