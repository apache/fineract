package org.mifosng.platform.organisation.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.OfficeCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class OfficeCommandValidator {

	private final OfficeCommand command;

	public OfficeCommandValidator(OfficeCommand command) {
		this.command = command;
	}
	
	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (StringUtils.isBlank(this.command.getName())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.office.name.cannot.be.blank", "The parameter name cannot be blank.", "name");
			dataValidationErrors.add(error);
		}
		
		if (!command.isRootOffice() && (this.command.getParentId() == null || this.command.getParentId().intValue() <= 0)) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.office.parent.cannot.be.blank", "A valid parentId must be provided.", "parentId");
			dataValidationErrors.add(error);
		}
		
		if (this.command.getOpeningDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.office.opening.date.cannot.be.blank", "The parameter openingDate cannot be blank", "openingDate");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(this.command.getExternalId())) {
			if (this.command.getExternalId().trim().length() > 100) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.office.externalId.exceeds.max.length", "The parameter externalId exceeds max length of {0}", "externalId", 100);
				dataValidationErrors.add(error);
			}
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (command.getId() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.office.id.cannot.be.blank", "A valid id must be provided.", "id");
			dataValidationErrors.add(error);
		}
		
		boolean ignoreNulls = true;

		// NOTE: remember for updates that values that are null are ignored from validation and update.
		boolean nameHasData = validateParameterNotEmpty(this.command.getName(), "office", "name", ignoreNulls, dataValidationErrors);
		boolean openingDateHasData = validateParameterNotEmpty(this.command.getOpeningDateFormatted(), "office", "openingDateFormatted", ignoreNulls, dataValidationErrors);
		boolean externalIdHasData = validateParameterNotGreaterThan(100, this.command.getExternalId(), "office", "externalId", ignoreNulls, dataValidationErrors);
		boolean parentidHasData = validateParameterNotNull(this.command.getParentId(), "office", "parentid", ignoreNulls, dataValidationErrors);
		
		if (!anyOfTrue(nameHasData, openingDateHasData, externalIdHasData, parentidHasData)) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.office.no.parameters.for.update", "No parameters passed for update.", "id");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	private boolean anyOfTrue(boolean nameHasData, boolean openingDateHasData, boolean externalIdHasData, boolean parentidHasData) {
		return nameHasData || openingDateHasData || externalIdHasData || parentidHasData;
	}

	private boolean validateParameterNotNull(Long value, String resourceName, String parameterName, boolean ignoreNulls, List<ApiParameterError> dataValidationErrors) {
		if (value == null && ignoreNulls) return false;
		return true;
	}

	private boolean validateParameterNotGreaterThan(Integer maxLength, String value, String resourceName, String parameterName, boolean ignoreNulls, List<ApiParameterError> dataValidationErrors) {
		if (value == null && ignoreNulls) return false;
		
		if (this.command.getExternalId().trim().length() > maxLength) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg." + resourceName + "." + parameterName + ".exceeds.max.length", "The parameter " + parameterName + " exceeds max length of " + maxLength + ".", parameterName, maxLength);
			dataValidationErrors.add(error);
			return true;
		}
		
		return true;
	}

	private boolean validateParameterNotEmpty(String value, String resourceName, String parameterName, boolean ignoreNulls, List<ApiParameterError> dataValidationErrors) {
		
		if (value == null && ignoreNulls) return false;
		
		if (StringUtils.isBlank(value)) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg." + resourceName + "." + parameterName + ".cannot.be.blank", "The parameter " + parameterName + " cannot be blank.", parameterName);
			dataValidationErrors.add(error);
			return true;
		}
		
		return true;
	}
}