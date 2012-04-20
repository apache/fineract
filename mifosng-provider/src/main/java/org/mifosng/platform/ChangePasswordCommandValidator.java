package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;

public class ChangePasswordCommandValidator {

	private final ChangePasswordCommand command;

	public ChangePasswordCommandValidator(ChangePasswordCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		if (StringUtils.isBlank(command.getPassword())) {
			ErrorResponse error = new ErrorResponse("validation.msg.changepassword.password.cannot.be.blank", "password");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getPasswordrepeat())) {
			ErrorResponse error = new ErrorResponse("validation.msg.changepassword.passwordrepeat.cannot.be.blank", "passwordrepeat");
			dataValidationErrors.add(error);
		} else {
			if (!command.getPasswordrepeat().equals(command.getPassword())) {
				ErrorResponse error = new ErrorResponse("validation.msg.changepassword.passwordrepeat.not.the.same", "passwordrepeat");
				dataValidationErrors.add(error);
			}
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}
}