package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
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
		
		if (command.getSubmittedOnDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.submitted.on.date.cannot.be.blank", 
					"The parameter submittedOnDateFormatted cannot be empty.", "submittedOnDateFormatted");
			dataValidationErrors.add(error);
		} else {
			if (command.getSubmittedOnDate().isAfter(command.getExpectedDisbursementDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.submitted.on.date.cannot.be.after.expectedDisbursementDate", 
						"The date of parameter submittedOnDateFormatted cannot fall after the date given for expectedDisbursementDateFormatted.", "submittedOnDateFormatted", 
						command.getSubmittedOnDateFormatted(), command.getExpectedDisbursementDateFormatted());
				dataValidationErrors.add(error);
			}
		}
		
		try {
			// reuse calculate loan schedule validator for now
			CalculateLoanScheduleCommand calculateLoanScheduleCommand = new CalculateLoanScheduleCommand(
					command.getCurrencyCode(), command.getDigitsAfterDecimal(),
					command.getPrincipal(), command.getInterestRatePerPeriod(),
					command.getInterestRateFrequencyMethod(),
					command.getInterestMethod(), command.getInterestCalculationPeriodMethod(),
					command.getRepaymentEvery(),
					command.getRepaymentFrequency(),
					command.getNumberOfRepayments(),
					command.getAmortizationMethod(),
					command.getExpectedDisbursementDate(),
					command.getRepaymentsStartingFromDate(),
					command.getInterestCalculatedFromDate());

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