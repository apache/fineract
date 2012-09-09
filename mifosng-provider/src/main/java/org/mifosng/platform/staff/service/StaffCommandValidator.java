package org.mifosng.platform.staff.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.StaffCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class StaffCommandValidator {

	private final StaffCommand command;

	public StaffCommandValidator(StaffCommand command) {
		this.command = command;
	}

	public void validateForCreate() {

		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("staff");

		baseDataValidator.reset().parameter("firstname")
				.value(command.getFirstName()).ignoreIfNull()
				.notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("lastname")
				.value(command.getLastName()).notBlank()
				.notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("officeId")
				.value(command.getOfficeId()).notNull()
				.integerGreaterThanZero();
		

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("staff");

		baseDataValidator.reset().parameter("id").value(command.getId())
				.notNull();
		baseDataValidator.reset().parameter("firstname")
				.value(command.getFirstName()).ignoreIfNull()
				.notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("lastname")
				.value(command.getFirstName()).ignoreIfNull().notBlank()
				.notExceedingLengthOf(50);

		baseDataValidator.reset().anyOfNotNull(command.getFirstName(),
				command.getLastName());

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}