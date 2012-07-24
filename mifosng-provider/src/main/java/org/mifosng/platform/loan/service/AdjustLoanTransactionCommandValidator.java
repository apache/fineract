package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class AdjustLoanTransactionCommandValidator {

	private final AdjustLoanTransactionCommand command;

	public AdjustLoanTransactionCommandValidator(AdjustLoanTransactionCommand command) {
		this.command = command;
	}

	public void validate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
		
		baseDataValidator.reset().parameter("loanId").value(command.getLoanId()).notNull().greaterThanZero();
		baseDataValidator.reset().parameter("transactionId").value(command.getTransactionId()).notNull().greaterThanZero();
		baseDataValidator.reset().parameter("transactionDate").value(command.getTransactionDate()).notNull();
		baseDataValidator.reset().parameter("transactionAmount").value(command.getTransactionAmount()).notNull().positiveAmount();
		baseDataValidator.reset().parameter("note").value(command.getNote()).notExceedingLengthOf(1000);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}