package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.api.commands.LoanApplicationCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

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
			DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
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