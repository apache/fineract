package org.mifosng.platform.savingproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class SavingProductCommandValidator {
	
	private final SavingProductCommand command;
	
	public SavingProductCommandValidator(final SavingProductCommand command) {
		this.command=command;
	}
	
	public void validateForUpdate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("savingsproduct");
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("interestRate").value(command.getInterestRate()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).notNull();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).notNull();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		baseDataValidator.reset().anyOfNotNull(command.getName(), command.getDescription(),command.getCurrencyCode(),command.getDigitsAfterDecimal(),command.getInterestRate(),command.getMinimumBalance(),command.getMaximumBalance());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("interestRate").value(command.getInterestRate()).notNull();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).notNull();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).notNull();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}