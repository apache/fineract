package org.mifosplatform.infrastructure.user.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class UserCommandValidator {

	private final UserCommand command;

	public UserCommandValidator(UserCommand command) {
		this.command = command;
	}
	
	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");
		
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("username").value(command.getUsername()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("lastname").value(command.getLastname()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("email").value(command.getEmail()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).ignoreIfNull().notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("roles").value(command.getRoles()).ignoreIfNull().arrayNotEmpty();

		baseDataValidator.reset().parameter("password").value(command.getPassword()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("passwordRepeat").value(command.getPassword()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("password").value(command.getPassword()).ignoreIfNull().equalToParameter("repeatPassword", command.getRepeatPassword());
		
		baseDataValidator.reset().anyOfNotNull(command.getUsername(), command.getFirstname(), command.getLastname(), command.getEmail(), command.getOfficeId(), 
				command.getRoles(), command.getPassword(), command.getRepeatPassword());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");
		
		baseDataValidator.reset().parameter("username").value(command.getUsername()).notBlank();
		baseDataValidator.reset().parameter("firstname").value(command.getFirstname()).notBlank();
		baseDataValidator.reset().parameter("lastname").value(command.getLastname()).notBlank();
		baseDataValidator.reset().parameter("email").value(command.getEmail()).notBlank();
		baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("roles").value(command.getRoles()).arrayNotEmpty();

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}