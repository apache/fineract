package org.mifosplatform.portfolio.loanaccount.loanschedule.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class CalculateLoanScheduleCommandValidator {

	private final CalculateLoanScheduleCommand command;

	public CalculateLoanScheduleCommandValidator(CalculateLoanScheduleCommand command) {
		this.command = command;
	}

	public void validate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
		
		baseDataValidator.reset().parameter("principal").value(command.getPrincipal()).notNull().positiveAmount();
		
		baseDataValidator.reset().parameter("loanTermFrequency").value(command.getLoanTermFrequency()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("loanTermFrequencyType").value(command.getLoanTermFrequencyType()).notNull().inMinMaxRange(0, 4);
		
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepayments()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEvery()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("repaymentFrequencyType").value(command.getRepaymentFrequencyType()).notNull().inMinMaxRange(0, 3);
		
		// FIXME - this constraint doesnt really need to be here. should be possible to express loan term as say 12 months whilst also saying
		//       - that the repayment structure is 6 repayments every bi-monthly.
		if (command.getLoanTermFrequencyType() != null && !command.getLoanTermFrequencyType().equals(command.getRepaymentFrequencyType())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.loanTermFrequencyType.not.the.same.as.repaymentFrequencyType", 
					"The parameters loanTermFrequencyType and repaymentFrequencyType must be the same.", "loanTermFrequencyType", 
					command.getLoanTermFrequencyType(), command.getRepaymentFrequencyType());
			dataValidationErrors.add(error);
		} else {
			if (command.getLoanTermFrequency() != null && command.getRepaymentEvery() != null && command.getNumberOfRepayments() != null) { 
				int suggestsedLoanTerm = command.getRepaymentEvery() * command.getNumberOfRepayments();
				if (command.getLoanTermFrequency().intValue() < suggestsedLoanTerm) {
					ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.loanTermFrequency.less.than.repayment.structure.suggests", 
							"The parameter loanTermFrequency is less than the suggest loan term as indicated by numberOfRepayments and repaymentEvery.", "loanTermFrequency", 
							command.getLoanTermFrequency(), command.getNumberOfRepayments(), command.getRepaymentEvery());
					dataValidationErrors.add(error);
				}
			}
		}
		
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
					"The parameter interestCalculatedFromDate cannot be empty when first repayment date is provided.", "interestChargedFromDate", command.getRepaymentsStartingFromDate());
			dataValidationErrors.add(error);
		} else if (command.getRepaymentsStartingFromDate() == null && command.getInterestChargedFromDate() != null) {
			
			// validate interestCalculatedFromDate is after or on repaymentsStartingFromDate
			if (command.getExpectedDisbursementDate().isAfter(command.getInterestChargedFromDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.interestChargedFromDate.cannot.be.before.disbursement.date", 
						"The parameter interestCalculatedFromDate cannot be before the date given for expected disbursement.", "interestChargedFromDate", command.getInterestChargedFromDate(), command.getExpectedDisbursementDate());
				dataValidationErrors.add(error);
			}
		}
		
//		if (command.getCharges() != null) {
//			for (LoanChargeCommand loanCharge : command.getCharges()) {
//				try {
//					LoanChargeCommandValidator validator = new LoanChargeCommandValidator(loanCharge);
//					validator.validateForCreate();
//				} catch (PlatformApiDataValidationException e) {
//					dataValidationErrors.addAll(e.getErrors());
//				}
//			}
//		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	
	}
}