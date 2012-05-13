package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;

public class UserCommandValidator {

	private final UserCommand command;

	public UserCommandValidator(UserCommand command) {
		this.command = command;
	}

	public void validate() {
		
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		validateAccountSettings(dataValidationErrors);
		
		if (command.getOfficeId() == null || command.getOfficeId().equals(Long.valueOf(-1))) {
			ErrorResponse error = new ErrorResponse("validation.msg.user.office.cannot.be.blank", "officeId");
			dataValidationErrors.add(error);
		}
		
		if (command.getSelectedItems() == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.user.roles.cannot.be.blank", "selectedItems");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}

	public void validateAccountSettingDetails() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		validateAccountSettings(dataValidationErrors);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}

	private void validateAccountSettings(List<ErrorResponse> dataValidationErrors) {
		if (StringUtils.isBlank(command.getUsername())) {
			ErrorResponse error = new ErrorResponse("validation.msg.user.username.cannot.be.blank", "username");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getFirstname())) {
			ErrorResponse error = new ErrorResponse("validation.msg.user.firstname.cannot.be.blank", "firstname");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getLastname())) {
			ErrorResponse error = new ErrorResponse("validation.msg.user.lastname.cannot.be.blank", "lastname");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getEmail())) {
			ErrorResponse error = new ErrorResponse("validation.msg.user.email.cannot.be.blank", "email");
			dataValidationErrors.add(error);
		}
	}
}