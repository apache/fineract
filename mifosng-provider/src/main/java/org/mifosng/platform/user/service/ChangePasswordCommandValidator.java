package org.mifosng.platform.user.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class ChangePasswordCommandValidator {

	private final ChangePasswordCommand command;

	public ChangePasswordCommandValidator(ChangePasswordCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (StringUtils.isBlank(command.getPassword())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.changepassword.password.cannot.be.blank", "The parameter password cannot be blank.", "password");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getPasswordrepeat())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.changepassword.passwordrepeat.cannot.be.blank", "The parameter passwordrepeat cannot be blank.", "passwordrepeat");
			dataValidationErrors.add(error);
		} else {
			if (!command.getPasswordrepeat().equals(command.getPassword())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.changepassword.passwordrepeat.not.the.same", "The parameter passwordrepeat cannot be blank.", "passwordrepeat");
				dataValidationErrors.add(error);
			}
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}