package org.mifosplatform.portfolio.loanaccount.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.loanaccount.command.AdjustLoanTransactionCommand;

public class AdjustLoanTransactionCommandValidator {

	private final AdjustLoanTransactionCommand command;

	public AdjustLoanTransactionCommandValidator(AdjustLoanTransactionCommand command) {
		this.command = command;
	}

	public void validate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
		
		baseDataValidator.reset().parameter("loanId").value(command.getLoanId()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("transactionId").value(command.getTransactionId()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("transactionDate").value(command.getTransactionDate()).notNull();
		baseDataValidator.reset().parameter("transactionAmount").value(command.getTransactionAmount()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("note").value(command.getNote()).notExceedingLengthOf(1000);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}