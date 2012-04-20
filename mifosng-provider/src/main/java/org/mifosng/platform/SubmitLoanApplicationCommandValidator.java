package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;

public class SubmitLoanApplicationCommandValidator {

	private final SubmitLoanApplicationCommand command;

	public SubmitLoanApplicationCommandValidator(SubmitLoanApplicationCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		if (command.getLoanSchedule() == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.submit.loan.loan.schedule.cannot.be.blank", "loanSchedule");
			dataValidationErrors.add(error);
		}
		
		if (command.getSubmittedOnDate() == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.loan.submitted.on.date.cannot.be.blank", "submittedOnDate");
			dataValidationErrors.add(error);
		} else {
			if (command.getSubmittedOnDate().isAfter(command.getExpectedDisbursementDate())) {
				ErrorResponse error = new ErrorResponse("validation.msg.loan.submitted.on.date.cannot.be.after.expectedDisbursementDate", "submittedOnDate");
				dataValidationErrors.add(error);
			}
		}
		
		try {
			// reuse calculate loan schedule validator for now
			CalculateLoanScheduleCommand calculateLoanScheduleCommand = new CalculateLoanScheduleCommand(
					command.getCurrencyCode(), command.getDigitsAfterDecimal(),
					command.getPrincipal(), command.getInterestRatePerPeriod(),
					command.getInterestRateFrequencyMethod(),
					command.getInterestMethod(), command.getRepaymentEvery(),
					command.getRepaymentFrequency(),
					command.getNumberOfRepayments(),
					command.getAmortizationMethod(),
					command.isFlexibleRepaymentSchedule(),
					command.isInterestRebateAllowed(),
					command.getExpectedDisbursementDate(),
					command.getRepaymentsStartingFromDate(),
					command.getInterestCalculatedFromDate());

			CalculateLoanScheduleCommandValidator validator = new CalculateLoanScheduleCommandValidator(
					calculateLoanScheduleCommand);
			validator.validate();
		} catch (NewDataValidationException e) {
			dataValidationErrors.addAll(e.getValidationErrors());
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}
}