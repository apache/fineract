package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.EnrollClientCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;

public class EnrollClientCommandValidator {

	private final EnrollClientCommand command;

	public EnrollClientCommandValidator(EnrollClientCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		if (command.getOfficeId() == null || command.getOfficeId().equals(Long.valueOf(-1))) {
			ErrorResponse error = new ErrorResponse("validation.msg.client.office.cannot.be.blank", "officeId");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(command.getFullname()) && (StringUtils.isNotBlank(command.getFirstname()) || StringUtils.isNotBlank(command.getLastname()))) {
			ErrorResponse error = new ErrorResponse("validation.msg.client.fullname.cannot.entered.when.firstname.or.lastname.entered", "fullname");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getFirstname()) && StringUtils.isBlank(command.getLastname()) ) {
			
			if (StringUtils.isBlank(command.getFullname())) {
				ErrorResponse error = new ErrorResponse("validation.msg.client.fullname.must.be.provide.or.firstname.lastname", "fullname");
				dataValidationErrors.add(error);
			}
		} else {
			if (StringUtils.isBlank(command.getFirstname())) {
				ErrorResponse error = new ErrorResponse("validation.msg.client.firstname.cannot.be.blank", "firstname");
				dataValidationErrors.add(error);
			}
			
			if (StringUtils.isBlank(command.getLastname())) {
				ErrorResponse error = new ErrorResponse("validation.msg.client.lastname.cannot.be.blank", "lastname");
				dataValidationErrors.add(error);
			}
		}
		
		if (command.getJoiningDate() == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.client.joining.date.cannot.be.blank", "joiningDate");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation erros exist.");
		}
	}

}
