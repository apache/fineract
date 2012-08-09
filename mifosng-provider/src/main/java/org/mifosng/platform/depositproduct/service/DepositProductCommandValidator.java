package org.mifosng.platform.depositproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class DepositProductCommandValidator {
	
	private final DepositProductCommand command;
	
	public DepositProductCommandValidator(final DepositProductCommand command) {
		this.command=command;
	}
	
	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).notNull();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).notNull();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("tenureMonths").value(command.getTenureMonths()).notNull();
		baseDataValidator.reset().parameter("tenureMonths").value(command.getTenureMonths()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).notNull();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).notNull();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).notNull();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("canRenew").value(command.getCanRenew()).notNull();
		baseDataValidator.reset().parameter("canPreClose").value(command.getCanPreClose()).notNull();
		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).notNull();
		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).zeroOrPositiveAmount();
		
		baseDataValidator.reset().comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("savingsproduct");
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).notNull();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).notNull();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("tenureMonths").value(command.getTenureMonths()).notNull();
		baseDataValidator.reset().parameter("tenureMonths").value(command.getTenureMonths()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).notNull();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).notNull();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).notNull();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("canRenew").value(command.getCanRenew()).notNull();
		baseDataValidator.reset().parameter("canPreClose").value(command.getCanPreClose()).notNull();
		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).notNull();
		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).zeroOrPositiveAmount();
		
		baseDataValidator.reset().comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		
		baseDataValidator.reset().anyOfNotNull(command.getName(), command.getDescription(),command.getCurrencyCode(),command.getDigitsAfterDecimal(),command.getMinimumBalance(),command.getMaximumBalance(),command.getTenureMonths(),command.getMaturityDefaultInterestRate(),command.getMaturityMaxInterestRate(),command.getMaturityMinInterestRate(),command.getCanRenew(),command.getCanPreClose(),command.getPreClosureInterestRate());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
