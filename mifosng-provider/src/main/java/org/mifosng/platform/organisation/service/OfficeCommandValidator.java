package org.mifosng.platform.organisation.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class OfficeCommandValidator {

	private final OfficeCommand command;

	public OfficeCommandValidator(OfficeCommand command) {
		this.command = command;
	}
	
	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("office");
		
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("openingDate").value(command.getOpeningDate()).notBlank();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("parentId").value(command.getParentId()).notNull().greaterThanZero();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("office");
		
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("openingDate").value(command.getOpeningDate()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("parentId").value(command.getParentId()).ignoreIfNull().notNull().greaterThanZero();
		
		baseDataValidator.reset().anyOfNotNull(command.getName(), command.getOpeningDate(), command.getExternalId(), command.getParentId());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}