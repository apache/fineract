package org.mifosng.platform;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;

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
		
		if (StringUtils.isBlank(value.toString())) {
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
}