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
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");
		
		if (command.getFullname() != null && StringUtils.isNotBlank(command.getFullname())) {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).mustBeBlankWhenParameterProvided("fullname", command.getFullname());
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).mustBeBlankWhenParameterProvided("fullname", command.getFullname());
		} else if (StringUtils.isBlank(command.getFullname())) {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).ignoreIfNull().notBlank();
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).ignoreIfNull().notBlank();
		}
		
		baseDataValidator.reset().parameter("joiningDateFormatted").value(command.getJoiningDateFormatted()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).ignoreIfNull().notNull().greaterThanZero();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForCreate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");
		
		if (StringUtils.isNotBlank(command.getFullname())) {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).mustBeBlankWhenParameterProvided("fullname", command.getFullname());
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).mustBeBlankWhenParameterProvided("fullname", command.getFullname());
		} else {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).notBlank();
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).notBlank();
		}
		
		baseDataValidator.reset().parameter("joiningDateFormatted").value(command.getJoiningDateFormatted()).notBlank();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().greaterThanZero();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}
