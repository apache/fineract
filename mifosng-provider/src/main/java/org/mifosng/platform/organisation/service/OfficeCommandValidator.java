package org.mifosng.platform.organisation.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.OfficeCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class OfficeCommandValidator {

	private final OfficeCommand command;

	public OfficeCommandValidator(OfficeCommand command) {
		this.command = command;
	}

	public void validate() {
		
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
}