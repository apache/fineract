package org.mifosplatform.portfolio.loanaccount.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.loanaccount.command.LoanApplicationCommand;
import org.mifosplatform.portfolio.loanaccount.command.LoanChargeCommand;
import org.mifosplatform.portfolio.loanaccount.loanschedule.command.CalculateLoanScheduleCommand;

public class LoanApplicationCommandValidator {

	private final LoanApplicationCommand command;

	public LoanApplicationCommandValidator(LoanApplicationCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
//		if (command.getLoanSchedule() == null) {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.submit.loan.loan.schedule.cannot.be.blank", 
//					"The parameter loanSchedule cannot be empty.", "loanSchedule");
//			dataValidationErrors.add(error);
//		}
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

		if (command.getGroupId() != null){
			baseDataValidator.reset().parameter("clientId").value(command.getClientId()).mustBeBlankWhenParameterProvided("groupId", command.getGroupId()).longGreaterThanZero();
		}
		if (command.getClientId() != null){
			baseDataValidator.reset().parameter("groupId").value(command.getGroupId()).mustBeBlankWhenParameterProvided("clientId", command.getClientId()).longGreaterThanZero();
		}

		if (command.getSubmittedOnDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.submitted.on.date.cannot.be.blank", 
					"The parameter submittedOnDate cannot be empty.", "submittedOnDate");
			dataValidationErrors.add(error);
		} else {
			if (command.getSubmittedOnDate().isAfter(command.getExpectedDisbursementDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.submitted.on.date.cannot.be.after.expectedDisbursementDate", 
						"The date of parameter submittedOnDate cannot fall after the date given for expectedDisbursementDate.", "submittedOnDate", 
						command.getSubmittedOnDate(), command.getExpectedDisbursementDate());
				dataValidationErrors.add(error);
			}
		}
		
		if (command.getTransactionProcessingStrategyId() == null) {
			baseDataValidator.reset().parameter("transactionProcessingStrategyId").value(command.getTransactionProcessingStrategyId()).notNull().inMinMaxRange(2, 2);
		}
		
		try {
			// reuse calculate loan schedule validator for now
			CalculateLoanScheduleCommand calculateLoanScheduleCommand = this.command.toCalculateLoanScheduleCommand();
			CalculateLoanScheduleCommandValidator validator = new CalculateLoanScheduleCommandValidator(calculateLoanScheduleCommand);
			validator.validate();
		} catch (PlatformApiDataValidationException e) {
			dataValidationErrors.addAll(e.getErrors());
		}

        if (this.command.getCharges() != null){
            for (LoanChargeCommand loanChargeCommand : this.command.getCharges()){
                try {
                    LoanChargeCommandValidator validator = new LoanChargeCommandValidator(loanChargeCommand);
                    validator.validateForCreate();
                } catch (PlatformApiDataValidationException e) {
                    dataValidationErrors.addAll(e.getErrors());
                }
            }
        }

		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}