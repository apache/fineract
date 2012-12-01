package org.mifosplatform.portfolio.loanaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class LoanStateTransitionCommandValidator {

	private final LoanStateTransitionCommand command;

	public LoanStateTransitionCommandValidator(LoanStateTransitionCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transition");
		
		baseDataValidator.reset().parameter("loanId").value(command.getLoanId()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("eventDate").value(command.getEventDate()).notNull();
		baseDataValidator.reset().parameter("note").value(command.getNote()).notExceedingLengthOf(1000);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
