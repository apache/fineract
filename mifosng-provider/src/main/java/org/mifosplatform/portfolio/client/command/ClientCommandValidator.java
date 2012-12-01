package org.mifosplatform.portfolio.client.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class ClientCommandValidator {

	private final ClientCommand command;

	public ClientCommandValidator(ClientCommand command) {
		this.command = command;
	}
	
	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");
		
		if (command.getClientOrBusinessName() != null && StringUtils.isNotBlank(command.getClientOrBusinessName())) {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).mustBeBlankWhenParameterProvided("clientOrBusinessName", command.getClientOrBusinessName());
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).mustBeBlankWhenParameterProvided("clientOrBusinessName", command.getClientOrBusinessName());
		} else if (StringUtils.isBlank(command.getClientOrBusinessName())) {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).ignoreIfNull().notBlank();
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).ignoreIfNull().notBlank();
		}
		
		baseDataValidator.reset().parameter("joiningDate").value(command.getJoiningDate()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		
		if (command.isOfficeChanged()) {
			baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().integerGreaterThanZero();
		}
		
		baseDataValidator.reset().anyOfNotNull(command.getFirstname(), command.getLastname(), command.getClientOrBusinessName(), 
				command.getJoiningDate(), command.getExternalId(), command.getOfficeId());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForCreate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");
		
		if (StringUtils.isNotBlank(command.getClientOrBusinessName())) {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).mustBeBlankWhenParameterProvided("clientOrBusinessName", command.getClientOrBusinessName());
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).mustBeBlankWhenParameterProvided("clientOrBusinessName", command.getClientOrBusinessName());
		} else {
			baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).notBlank();
			baseDataValidator.reset().parameter("lastname").value(command.getLastname()).notBlank();
		}
		
		baseDataValidator.reset().parameter("joiningDate").value(command.getJoiningDate()).notBlank();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().integerGreaterThanZero();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}
