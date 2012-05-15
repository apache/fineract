package org.mifosng.platform.user.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.util.ObjectUtils;

public class RoleCommandValidator {

	private final RoleCommand command;

	public RoleCommandValidator(RoleCommand command) {
		this.command = command;
	}

	public void validateForCreate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		validate(dataValidationErrors);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (command.getId() == null || command.getId() < 1) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.role.id.not.provided", "The parameter id cannot be blank.", "id");
			dataValidationErrors.add(error);
		}
		
		validate(dataValidationErrors);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	private void validate(List<ApiParameterError> dataValidationErrors) {
		if (StringUtils.isBlank(command.getName())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.role.name.cannot.be.blank", "The parameter name cannot be blank.", "name");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getDescription())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.role.description.cannot.be.blank", "The parameter description cannot be blank.", "description");
			dataValidationErrors.add(error);
		} else {
			if (command.getDescription().trim().length() > 500) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.role.description.exceeds.max.length", "The parameter description has data that exceeds its max allowed length of {0}.", "description", 500);
				dataValidationErrors.add(error);
			}
		}
		
		if (ObjectUtils.isEmpty(command.getSelectedItems())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.role.permissions.cannot.be.empty", "The parameter selectedItems cannot be blank. You must select at least one permission.", "selectedItems");
			dataValidationErrors.add(error);
		}
	}
}
