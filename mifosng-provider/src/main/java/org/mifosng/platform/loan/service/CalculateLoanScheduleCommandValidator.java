package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class CalculateLoanScheduleCommandValidator {

	private final CalculateLoanScheduleCommand command;

	public CalculateLoanScheduleCommandValidator(CalculateLoanScheduleCommand command) {
		this.command = command;
	}

	public void validate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
		
		baseDataValidator.reset().parameter("principal").value(command.getPrincipal()).notNull().positiveAmount();
		baseDataValidator.reset().parameter("repaymentFrequencyType").value(command.getRepaymentFrequencyType()).notNull().inMinMaxRange(0, 3);
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEvery()).notNull().greaterThanZero();
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepayments()).notNull().greaterThanZero();
		baseDataValidator.reset().parameter("interestRatePerPeriod").value(command.getInterestRatePerPeriod()).notNull();
		baseDataValidator.reset().parameter("interestRateFrequencyType").value(command.getInterestRateFrequencyType()).notNull().inMinMaxRange(0, 3);
		baseDataValidator.reset().parameter("amortizationType").value(command.getAmortizationType()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestCalculationPeriodType").value(command.getInterestCalculationPeriodType()).notNull().inMinMaxRange(0, 1);
		
		baseDataValidator.reset().parameter("expectedDisbursementDate").value(command.getExpectedDisbursementDate()).notNull();
		
		if (command.getExpectedDisbursementDate() != null) {
			if (command.getRepaymentsStartingFromDate() != null
					&& command.getExpectedDisbursementDate().isAfter(command.getRepaymentsStartingFromDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.expectedDisbursementDate.cannot.be.after.first.repayment.date", 
						"The parameter expectedDisbursementDate has a date which falls after the given first repayment date.", "expectedDisbursementDate", 
						command.getExpectedDisbursementDate(), command.getRepaymentsStartingFromDate());
				dataValidationErrors.add(error);
			}
		}
		
		if (command.getRepaymentsStartingFromDate() != null && command.getInterestChargedFromDate() == null) {
			
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.interestCalculatedFromDate.must.be.entered.when.using.repayments.startfrom.field", 
					"The parameter interestCalculatedFromDate cannot be empty when first repayment date is provided.", "interestCalculatedFromDate", command.getRepaymentsStartingFromDate());
			dataValidationErrors.add(error);
		} else if (command.getRepaymentsStartingFromDate() == null && command.getInterestChargedFromDate() != null) {
			
			// validate interestCalculatedFromDate is after or on repaymentsStartingFromDate
			if (command.getExpectedDisbursementDate().isAfter(command.getInterestChargedFromDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.interestChargedFromDate.cannot.be.before.disbursement.date", 
						"The parameter interestCalculatedFromDate cannot be before the date given for expected disbursement.", "interestChargedFromDate", command.getInterestChargedFromDate(), command.getExpectedDisbursementDate());
				dataValidationErrors.add(error);
			}
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	
	}
}