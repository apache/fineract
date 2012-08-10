package org.mifosng.platform.savingproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class DepositAccountCommandValidator {
	
	private final DepositAccountCommand command;
	
	public DepositAccountCommandValidator(final DepositAccountCommand command) {
		this.command=command;
	}
	
	public void validateForUpdate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("clientId").value(command.getClientId()).ignoreIfNull().notNull();
		baseDataValidator.reset().parameter("productId").value(command.getProductId()).ignoreIfNull().notNull();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("depositAmount").value(command.getDepositAmount()).ignoreIfNull().notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("interestRate").value(command.getInterestRate()).ignoreIfNull().notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("termInMonths").value(command.getTermInMonths()).ignoreIfNull().notNull().integerGreaterThanZero();
		
		baseDataValidator.reset().anyOfNotNull(
				command.getClientId(), command.getProductId(), command.getExternalId(),
				command.getCurrencyCode(),command.getDigitsAfterDecimal(),
				command.getDepositAmount(), command.getInterestRate(),command.getTermInMonths());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");
		
		baseDataValidator.reset().parameter("clientId").value(command.getClientId()).notNull();
		baseDataValidator.reset().parameter("productId").value(command.getProductId()).notNull();
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);

		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
		baseDataValidator.reset().parameter("depositAmount").value(command.getDepositAmount()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("interestRate").value(command.getInterestRate()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("termInMonths").value(command.getTermInMonths()).notNull().integerGreaterThanZero();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}