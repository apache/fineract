package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
		
		if (command.getTransactionId() == null || command.getTransactionId().longValue() <= 0) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.id.is.invalid", "The parameter repaymentId is invalid.", "transactionId", command.getTransactionId());
			dataValidationErrors.add(error);
		}
		
		if (command.getLoanId() == null || command.getLoanId().doubleValue() <= 0) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.id.is.invalid", "The parameter loanId is invalid.", "loanId", command.getLoanId());
			dataValidationErrors.add(error);
		}
		
		if (command.getTransactionDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.repayment.date.cannot.be.blank", "The parameter transactionDate cannot be blank.", "transactionDate");
			dataValidationErrors.add(error);
		}
		
		if (command.getTransactionAmount() == null || command.getTransactionAmount().doubleValue() <= 0) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.repayment.must.be.greater.than.zero", "The parameter transactionAmount less than zero.", "transactionAmount", command.getTransactionAmount());
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(command.getNote()) && command.getNote().length() > 1000) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.note.exceeds.max.length", "The parameter note exceeds max allowed size of {0}", "note", 1000);
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
