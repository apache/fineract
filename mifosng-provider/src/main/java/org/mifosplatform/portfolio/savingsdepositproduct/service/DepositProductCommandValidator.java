package org.mifosplatform.portfolio.savingsdepositproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;

public class DepositProductCommandValidator {
	
	private final DepositProductCommand command;
	
	public DepositProductCommandValidator(final DepositProductCommand command) {
		this.command=command;
	}
	
	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.product");
		
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("minimumBalance").comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		
		baseDataValidator.reset().parameter("tenureInMonths").value(command.getTenureInMonths()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMinInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityDefaultInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().parameter("maturityMinInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityDefaultInterestRate());
		
		baseDataValidator.reset().parameter("interestCompoundedEvery").value(command.getInterestCompoundedEvery()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(command.getInterestCompoundedEveryPeriodType()).notNull().inMinMaxRange(1, 3);
		
		baseDataValidator.reset().parameter("renewalAllowed").trueOrFalseRequired(command.isRenewalAllowedChanged());
		baseDataValidator.reset().parameter("preClosureAllowed").trueOrFalseRequired(command.isPreClosureAllowedChanged());
		baseDataValidator.reset().parameter("interestCompoundingAllowed").trueOrFalseRequired(command.interestCompoundingAllowedChanged());

		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("preClosureInterestRate").comapareMinAndMaxOfTwoBigDecmimalNos(command.getPreClosureInterestRate(), command.getMaturityMinInterestRate());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForUpdate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.product");
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		
		baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("minimumBalance").value(command.getMinimumBalance()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maximumBalance").value(command.getMaximumBalance()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().comapareMinimumAndMaximumAmounts(command.getMinimumBalance(), command.getMaximumBalance());
		
		baseDataValidator.reset().parameter("tenureMonths").value(command.getTenureInMonths()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityDefaultInterestRate").value(command.getMaturityDefaultInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMinInterestRate").value(command.getMaturityMinInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("maturityMaxInterestRate").value(command.getMaturityMaxInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityDefaultInterestRate(), command.getMaturityMaxInterestRate());
		baseDataValidator.reset().comapareMinAndMaxOfTwoBigDecmimalNos(command.getMaturityMinInterestRate(), command.getMaturityDefaultInterestRate());
		
		baseDataValidator.reset().parameter("interestCompoundedEvery").value(command.getInterestCompoundedEvery()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(command.getInterestCompoundedEveryPeriodType()).ignoreIfNull().inMinMaxRange(1, 3);
		baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).ignoreIfNull().zeroOrPositiveAmount();
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