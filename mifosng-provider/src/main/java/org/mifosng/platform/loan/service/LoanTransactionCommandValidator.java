package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class LoanTransactionCommandValidator {

	private final LoanTransactionCommand command;

	public LoanTransactionCommandValidator(LoanTransactionCommand command) {
		this.command = command;
	}

	public void validate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("role");
		
		baseDataValidator.reset().parameter("loanid").value(command.getLoanId()).notNull().greaterThanZero();
		baseDataValidator.reset().parameter("transactionDate").value(command.getTransactionLocalDate()).notNull();
		baseDataValidator.reset().parameter("transactionAmount").value(command.getTransactionAmount()).notNull().positiveAmount();
		baseDataValidator.reset().parameter("note").value(command.getNote()).notExceedingLengthOf(1000);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}