package org.mifosng.platform.user.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.RoleCommand;
import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class RoleCommandValidator {

	private final RoleCommand command;

	public RoleCommandValidator(RoleCommand command) {
		this.command = command;
	}

	public void validateForCreate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("role");
		
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).notBlank().notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("permissions").value(command.getPermissions()).arrayNotEmpty();
		
		baseDataValidator.reset().anyOfNotNull(command.getName(), command.getDescription(), command.getPermissions());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("role");
		
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notBlank().notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("permissions").value(command.getPermissions()).ignoreIfNull().arrayNotEmpty();
		
		baseDataValidator.reset().anyOfNotNull(command.getName(), command.getDescription(), command.getPermissions());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}