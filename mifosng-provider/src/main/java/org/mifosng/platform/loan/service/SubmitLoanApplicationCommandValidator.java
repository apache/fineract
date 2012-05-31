package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class SubmitLoanApplicationCommandValidator {

	private final SubmitLoanApplicationCommand command;

	public SubmitLoanApplicationCommandValidator(SubmitLoanApplicationCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (command.getLoanSchedule() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.submit.loan.loan.schedule.cannot.be.blank", 
					"The parameter loanSchedule cannot be empty.", "loanSchedule");
			dataValidationErrors.add(error);
		}
		
		if (command.getSubmittedOnLocalDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.submitted.on.date.cannot.be.blank", 
					"The parameter submittedOnDate cannot be empty.", "submittedOnDate");
			dataValidationErrors.add(error);
		} else {
			if (command.getSubmittedOnLocalDate().isAfter(command.getExpectedDisbursementLocalDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.submitted.on.date.cannot.be.after.expectedDisbursementDate", 
						"The date of parameter submittedOnDate cannot fall after the date given for expectedDisbursementDate.", "submittedOnDate", 
						command.getSubmittedOnDate(), command.getExpectedDisbursementDate());
				dataValidationErrors.add(error);
			}
		}
		
		try {
			// reuse calculate loan schedule validator for now
			CalculateLoanScheduleCommand calculateLoanScheduleCommand = new CalculateLoanScheduleCommand(
					command.getCurrencyCode(), command.getDigitsAfterDecimalValue(),
					command.getPrincipalValue(), command.getInterestRatePerPeriodValue(),
					command.getInterestRateFrequencyType(),
					command.getInterestType(), command.getInterestCalculationPeriodType(),
					command.getRepaymentEveryValue(),
					command.getRepaymentFrequencyType(),
					command.getNumberOfRepaymentsValue(),
					command.getAmortizationType(),
					command.getExpectedDisbursementLocalDate(),
					command.getRepaymentsStartingFromLocalDate(),
					command.getInterestChargedFromLocalDate());

			CalculateLoanScheduleCommandValidator validator = new CalculateLoanScheduleCommandValidator(
					calculateLoanScheduleCommand);
			validator.validate();
		} catch (PlatformApiDataValidationException e) {
			dataValidationErrors.addAll(e.getErrors());
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}