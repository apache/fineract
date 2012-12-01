package org.mifosng.platform.guarantor;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.api.commands.GuarantorCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class GuarantorCommandValidator {

	private final GuarantorCommand command;

	public GuarantorCommandValidator(GuarantorCommand command) {
		this.command = command;
	}

	public void validateForCreate() {

		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("Guarantor");

		// validate for existing Client
		if (command.isExternalGuarantor() == null
				|| command.isExternalGuarantor() == false) {
			baseDataValidator.reset().parameter("existingClientId")
					.value(command.getExistingClientId()).notNull()
					.integerGreaterThanZero();
		} else {
			// validate for an external guarantor
			baseDataValidator.reset().parameter("firstname")
					.value(command.getFirstname()).notBlank()
					.notExceedingLengthOf(50);
			baseDataValidator.reset().parameter("lastname")
					.value(command.getLastname()).notBlank()
					.notExceedingLengthOf(50);

			validateNonMandatoryFieldsForMaxLength(baseDataValidator);
		}

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForUpdate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("Guarantor");

		// validate for existing Client
		if (command.isExternalGuarantor() == null
				|| command.isExternalGuarantor() == false) {
			baseDataValidator.reset().parameter("existingClientId")
					.value(command.getExistingClientId()).notNull()
					.integerGreaterThanZero();
		} else {
			// validate for an external guarantor
			baseDataValidator.reset().parameter("firstname")
					.value(command.getFirstname()).notBlank()
					.notExceedingLengthOf(50);
			baseDataValidator.reset().parameter("lastname")
					.value(command.getLastname()).notBlank()
					.notExceedingLengthOf(50);

			validateNonMandatoryFieldsForMaxLength(baseDataValidator);
		}
		baseDataValidator.reset().anyOfNotNull(command.getExistingClientId(),
				command.getAddressLine1(), command.getAddressLine2(),
				command.getCity(), command.getComment(), command.getCountry(),
				command.getFirstname(), command.getHousePhoneNumber(),
				command.getLastname(), command.getMobileNumber(),
				command.getState(), command.getZip());

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	/**
	 * @param baseDataValidator
	 */
	private void validateNonMandatoryFieldsForMaxLength(
			DataValidatorBuilder baseDataValidator) {
		// validate non mandatory fields for length
		baseDataValidator.reset().parameter("addressLine1")
				.value(command.getAddressLine1()).ignoreIfNull()
				.notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("addressLine2")
				.value(command.getAddressLine2()).ignoreIfNull()
				.notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("city").value(command.getCity())
				.ignoreIfNull().notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("state").value(command.getState())
				.ignoreIfNull().notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("zip").value(command.getZip())
				.ignoreIfNull().notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("country")
				.value(command.getCountry()).ignoreIfNull()
				.notExceedingLengthOf(50);
		baseDataValidator.reset().parameter("mobileNumber")
				.value(command.getMobileNumber()).ignoreIfNull()
				.notExceedingLengthOf(20);
		baseDataValidator.reset().parameter("housePhoneNumber")
				.value(command.getHousePhoneNumber()).ignoreIfNull()
				.notExceedingLengthOf(20);
		baseDataValidator.reset().parameter("comment")
				.value(command.getComment()).ignoreIfNull()
				.notExceedingLengthOf(500);
	}
}