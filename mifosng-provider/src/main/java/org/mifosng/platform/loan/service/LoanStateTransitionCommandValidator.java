package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class LoanStateTransitionCommandValidator {

	private final LoanStateTransitionCommand command;

	public LoanStateTransitionCommandValidator(LoanStateTransitionCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (command.getLoanId() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.id.is.invalid", "The parameter loanId is invalid.", "loanId", command.getLoanId());
			dataValidationErrors.add(error);
		}
		
		if (command.getEventDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.state.transition.date.cannot.be.blank", "The parameter eventDate cannot be blank.", "eventDate");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(command.getComment()) && command.getComment().length() > 1000) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.note.exceeds.max.length", "The parameter comment exceeds mas allowed length of {0}.", "comment");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
