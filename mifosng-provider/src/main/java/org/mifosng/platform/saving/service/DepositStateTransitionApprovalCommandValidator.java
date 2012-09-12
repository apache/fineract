package org.mifosng.platform.saving.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.DepositStateTransitionApprovalCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class DepositStateTransitionApprovalCommandValidator {
	

	
	private final DepositStateTransitionApprovalCommand command;
	
	public DepositStateTransitionApprovalCommandValidator(final DepositStateTransitionApprovalCommand command) {
		this.command=command;
	}
	
	public void validate(){
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.transition");
		baseDataValidator.reset().parameter("accountId").value(command.getAccountId()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("commencementDate").value(command.getEventDate()).notNull();
		
		baseDataValidator.reset().parameter("depositAmount").value(command.getDepositAmount()).ignoreIfNull().zeroOrPositiveAmount();
		baseDataValidator.reset().parameter("tenureInMonths").value(command.getTenureInMonths()).ignoreIfNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(command.getInterestCompoundedEveryPeriodType()).ignoreIfNull().inMinMaxRange(2, 2);
		
		baseDataValidator.reset().parameter("note").value(command.getNote()).notNull();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}



}
