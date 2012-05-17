package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class LoanTransactionCommandValidator {

	private final LoanTransactionCommand command;

	public LoanTransactionCommandValidator(LoanTransactionCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (command.getLoanId() == null || command.getLoanId().doubleValue() <= 0) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.id.is.invalid", "The parameter loanId is invalid.", "loanId", command.getLoanId());
			dataValidationErrors.add(error);
		}
		
		if (command.getTransactionDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.repayment.date.cannot.be.blank", "The parameter transactionDateFormatted cannot be blank.", "transactionDateFormatted");
			dataValidationErrors.add(error);
		}
		
		if (command.getTransactionAmount() == null || command.getTransactionAmount().doubleValue() <= 0) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.repayment.must.be.greater.than.zero", "The parameter transactionAmountFormatted less than zero.", "transactionAmountFormatted", command.getTransactionAmountFormatted());
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(command.getComment()) && command.getComment().length() > 1000) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.note.exceeds.max.length", "The parameter comment exceeds max allowed size of {0}", "comment", 1000);
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
