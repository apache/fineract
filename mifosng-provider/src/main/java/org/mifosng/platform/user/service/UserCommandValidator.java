package org.mifosng.platform.user.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.springframework.util.ObjectUtils;

public class UserCommandValidator {

	private final UserCommand command;

	public UserCommandValidator(UserCommand command) {
		this.command = command;
	}

	public void validate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		validateAccountSettings(dataValidationErrors);
		
		if (command.getOfficeId() == null || command.getOfficeId().equals(Long.valueOf(-1))) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.user.office.cannot.be.blank", "The parameter officeId cannot be blank.", "officeId");
			dataValidationErrors.add(error);
		}
		
		if (ObjectUtils.isEmpty(command.getSelectedItems())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.user.roles.cannot.be.blank", "The parameter selectedItems cannot be blank.", "selectedItems");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateAccountSettingDetails() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		validateAccountSettings(dataValidationErrors);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	private void validateAccountSettings(List<ApiParameterError> dataValidationErrors) {
		if (StringUtils.isBlank(command.getUsername())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.user.username.cannot.be.blank", "The parameter username cannot be blank.", "username");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getFirstname())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.user.firstname.cannot.be.blank", "The parameter firstname cannot be blank.", "firstname");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getLastname())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.user.lastname.cannot.be.blank", "The parameter lastname cannot be blank.", "lastname");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getEmail())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.user.email.cannot.be.blank", "The parameter email cannot be blank.", "email");
			dataValidationErrors.add(error);
		}
	}
}