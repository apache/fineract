package org.mifosng.platform.documentmanagement.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.DocumentCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class DocumentCommandValidator {

	private final DocumentCommand command;

	public DocumentCommandValidator(DocumentCommand command) {
		this.command = command;
	}

	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("document");

		baseDataValidator.reset().parameter("name").value(command.getName())
				.ignoreIfNull().notBlank();
		/*** TODO:Validate for max length of File to be stored **/
		baseDataValidator.reset().parameter("size").value(command.getSize())
				.ignoreIfNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("fileName")
				.value(command.getFileName()).ignoreIfNull().notBlank()
				.integerGreaterThanZero();
		baseDataValidator.reset().parameter("location")
				.value(command.getLocation()).ignoreIfNull().notBlank()
				.integerGreaterThanZero();
		/*** TODO: Validate for permissable types ***/
		baseDataValidator.reset().parameter("type").value(command.getName())
				.ignoreIfNull().notBlank().notExceedingLengthOf(250);
		baseDataValidator.reset().parameter("description")
				.value(command.getName()).ignoreIfNull()
				.notExceedingLengthOf(250);

		baseDataValidator.reset().anyOfNotNull(command.getName(),
				command.getFileName(), command.getDescription(),
				command.getLocation(), command.getSize());

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForCreate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("document");
		baseDataValidator.reset().parameter("parentEntityType")
				.value(command.getParentEntityType()).notBlank()
				.notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("parentEntityId")
				.value(command.getParentEntityId()).integerGreaterThanZero();
		baseDataValidator.reset().parameter("name").value(command.getName())
				.notBlank().notExceedingLengthOf(250);
		/*** TODO:Validate for max length of File to be stored **/
		baseDataValidator.reset().parameter("size").value(command.getSize())
				.integerGreaterThanZero();
		baseDataValidator.reset().parameter("fileName")
				.value(command.getFileName()).notBlank()
				.notExceedingLengthOf(250);
		/*** TODO: Validate for permissable types ***/
		baseDataValidator.reset().parameter("type").value(command.getName())
				.notBlank().notExceedingLengthOf(250);
		baseDataValidator.reset().parameter("description")
				.value(command.getName()).notExceedingLengthOf(250);
 
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}
