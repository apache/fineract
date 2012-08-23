package org.mifosng.platform.saving.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.DepositStateTransitionCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class DepositStateTransitionCommandValidator {
	
	private final DepositStateTransitionCommand command;
	
	public DepositStateTransitionCommandValidator(final DepositStateTransitionCommand command) {
		this.command=command;
	}
	
	public void validate(){
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.transition");
		baseDataValidator.reset().parameter("accountId").value(command.getAccountId()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("eventDate").value(command.getEventDate()).notNull();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
