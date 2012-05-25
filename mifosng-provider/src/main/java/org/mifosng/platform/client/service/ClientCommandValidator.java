package org.mifosng.platform.client.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.ClientCommand;
import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class ClientCommandValidator {

	private final ClientCommand command;

	public ClientCommandValidator(ClientCommand command) {
		this.command = command;
	}
	
	public void validateForUpdate() {
		
	}

	public void validateForCreate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");
		
		if (StringUtils.isNotBlank(command.getFullname())) {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).mustBeBlankWhenParameterProvided("fullname", command.getFullname());
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).mustBeBlankWhenParameterProvided("fullname", command.getFullname());
		} else {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).notBlank();//.andNotBlank("lastname", command.getLastname());
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).notBlank();//.andNotBlank("firstname", command.getFirstname());
		}
		
		baseDataValidator.reset().parameter("openingDateFormatted").value(command.getJoiningDateFormatted()).notBlank();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().greaterThanZero();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
		
//		if (StringUtils.isNotBlank(command.getFullname()) && (StringUtils.isNotBlank(command.getFirstname()) || StringUtils.isNotBlank(command.getLastname()))) {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.fullname.cannot.entered.when.firstname.or.lastname.entered", "The parameter fullname cannot be blank.", "fullname");
//			dataValidationErrors.add(error);
//		}
//		if (StringUtils.isBlank(command.getFirstname()) && StringUtils.isBlank(command.getLastname()) ) {
//			
//			if (StringUtils.isBlank(command.getFullname())) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.fullname.must.be.provide.or.firstname.lastname", "The parameter fullname cannot be blank or firstName and lastName must be both provided.", "fullname");
//				dataValidationErrors.add(error);
//			}
//		} else {
//			if (StringUtils.isBlank(command.getFirstname())) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.firstname.cannot.be.blank", "The parameter firstname cannot be blank.", "firstname");
//				dataValidationErrors.add(error);
//			}
//			
//			if (StringUtils.isBlank(command.getLastname())) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.lastname.cannot.be.blank", "The parameter lastname cannot be blank.", "lastname");
//				dataValidationErrors.add(error);
//			}
//		}
//		
//		if (command.getJoiningDate() == null) {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.joining.date.cannot.be.blank", "The parameter joiningDate cannot be blank.", "joiningDate");
//			dataValidationErrors.add(error);
//		}
//		
//		if (!dataValidationErrors.isEmpty()) {
//			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
//		}
	}
}
