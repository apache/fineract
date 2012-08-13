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
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.product");
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("tenureMonths").value(command.getTenureMonths()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).notNull().zeroOrPositiveAmount();
//		baseDataValidator.reset().parameter("canRenew").value(command.getCanRenew()).ignoreIfNull();
//		baseDataValidator.reset().parameter("canPreClose").value(command.getCanPreClose()).ignoreIfNull();
		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).notNull().zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("minimumBalance").comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		baseDataValidator.reset().parameter("maturityMinInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityDefaultInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().parameter("maturityMinInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityDefaultInterestRate());
		baseDataValidator.reset().parameter("preClosureInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getPreClosureInterestRate(), command.getMaturityMinInterestRate());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.product");
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		
		baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).ignoreIfNull().zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("tenureMonths").value(command.getTenureMonths()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
		
		baseDataValidator.reset().comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityDefaultInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityDefaultInterestRate());
		baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(command.getPreClosureInterestRate(), command.getMaturityMinInterestRate());
		
		if (command.isNoFieldChanged()) {
			StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append("deposit.product").append(".no.parameters.for.update");
			StringBuilder defaultEnglishMessage = new StringBuilder("No parameters passed for update.");
			ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(), "id");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}