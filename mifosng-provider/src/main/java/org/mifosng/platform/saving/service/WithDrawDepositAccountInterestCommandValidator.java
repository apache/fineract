package org.mifosng.platform.saving.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.DepositAccountWithdrawInterestCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class WithDrawDepositAccountInterestCommandValidator {
	
	private final DepositAccountWithdrawInterestCommand command;
	
	public WithDrawDepositAccountInterestCommandValidator(final DepositAccountWithdrawInterestCommand command) {
		this.command=command;
	}
	
	public void validate(){
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.transaction");

		baseDataValidator.reset().parameter("accountId").value(command.getAccountId()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("amount").value(command.getWithdrawInterest()).notNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("note").value(command.getNote()).notNull();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

}
