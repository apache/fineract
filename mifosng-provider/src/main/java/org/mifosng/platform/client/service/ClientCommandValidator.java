package org.mifosng.platform.client.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.ClientCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class ClientCommandValidator {

	private final ClientCommand command;

	public ClientCommandValidator(ClientCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (command.getOfficeId() == null || command.getOfficeId().equals(Long.valueOf(-1))) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.office.cannot.be.blank", "The parameter officeId cannot be blank.", "officeId");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(command.getFullname()) && (StringUtils.isNotBlank(command.getFirstname()) || StringUtils.isNotBlank(command.getLastname()))) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.fullname.cannot.entered.when.firstname.or.lastname.entered", "The parameter fullname cannot be blank.", "fullname");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isBlank(command.getFirstname()) && StringUtils.isBlank(command.getLastname()) ) {
			
			if (StringUtils.isBlank(command.getFullname())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.fullname.must.be.provide.or.firstname.lastname", "The parameter fullname cannot be blank or firstName and lastName must be both provided.", "fullname");
				dataValidationErrors.add(error);
			}
		} else {
			if (StringUtils.isBlank(command.getFirstname())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.firstname.cannot.be.blank", "The parameter firstname cannot be blank.", "firstname");
				dataValidationErrors.add(error);
			}
			
			if (StringUtils.isBlank(command.getLastname())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.lastname.cannot.be.blank", "The parameter lastname cannot be blank.", "lastname");
				dataValidationErrors.add(error);
			}
		}
		
		if (command.getJoiningDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.joining.date.cannot.be.blank", "The parameter joiningDate cannot be blank.", "joiningDate");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
