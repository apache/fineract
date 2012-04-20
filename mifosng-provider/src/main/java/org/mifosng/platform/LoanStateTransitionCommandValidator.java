package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;

public class LoanStateTransitionCommandValidator {

	private final LoanStateTransitionCommand command;

	public LoanStateTransitionCommandValidator(LoanStateTransitionCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		if (command.getLoanId() == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.loan.id.is.invalid", "loanId");
			dataValidationErrors.add(error);
		}
		
		if (command.getEventDate() == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.loan.state.transition.date.cannot.be.blank", "eventDate");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(command.getComment()) && command.getComment().length() > 1000) {
			ErrorResponse error = new ErrorResponse("validation.msg.note.exceeds.max.length", "comment");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}

}
