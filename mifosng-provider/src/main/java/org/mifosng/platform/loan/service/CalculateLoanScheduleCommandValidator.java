package org.mifosng.platform.loan.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public class CalculateLoanScheduleCommandValidator {

	private final CalculateLoanScheduleCommand command;

	public CalculateLoanScheduleCommandValidator(CalculateLoanScheduleCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		if (StringUtils.isBlank(command.getCurrencyCode())) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.currency.cannot.be.empty", "The parameter currencyCode cannot be blank.", "currencyCode");
			dataValidationErrors.add(error);
		}
		
		if (command.getDigitsAfterDecimal() != null) {
			
			if (command.getDigitsAfterDecimal() < 0 || command.getDigitsAfterDecimal() > 6) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.currency.digitsAfterDecimal.must.be.between.zero.and.six.inclusive", "The parameter digitsAfterDecimal must be a value between 1 and 6.", "digitsAfterDecimal", command.getDigitsAfterDecimal(), 1, 6);
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.currency.digitsAfterDecimal.cannot.be.empty", "The parameter digitsAfterDecimal cannot be blank.", "digitsAfterDecimal");
			dataValidationErrors.add(error);
		}
		
		// FIXME - validate the number of digits before and after decimal provided (before is limited by database field, after is limited by number selected for product (and ultimately by the database field))
		if (command.getPrincipal() != null) {
			if (command.getPrincipal().doubleValue() <= Double.valueOf("0.0")) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.principal.amount.must.be.greater.than.zero", "The parameter principalMoney must be greater than zero.", "principalMoney");
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.principal.amount.cannot.be.empty", "The parameter principalMoney cannot be blank.", "principalMoney");
			dataValidationErrors.add(error);
		}
		
		if (command.getRepaymentFrequency() != null) {
			PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequency());
			ApiParameterError error = null; 
			switch (frequencyType) {
			case DAYS:
				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 365);
				break;
			case WEEKS:
				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 52);
				break;
			case MONTHS:
				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 12);
				break;
			case YEARS:
				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 2);
				break;
			default:
				error = ApiParameterError.parameterError("validation.msg.product.repayment.frequency.invalid", "The parameter repaymentFrequency is invalid.", "repaymentFrequency", command.getRepaymentFrequency());
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.repayment.frequency.cannot.be.empty", "The parameter repaymentFrequency cannot be empty.", "repaymentFrequency");
			dataValidationErrors.add(error);
		}
		
		if (command.getNumberOfRepayments() != null) {
			if (command.getNumberOfRepayments() < 1 || command.getNumberOfRepayments() > 100) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.number.of.repayments.is.out.of.range", "The parameter numberOfRepayments must between {1} and {2}.", "numberOfRepayments", command.getNumberOfRepayments(), 1, 100);
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.number.of.repayments.cannot.be.empty", "The parameter numberOfRepayments cannot be empty.", "numberOfRepayments");
			dataValidationErrors.add(error);
		}
		
		if (command.getAmortizationMethod() != null) {
			AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());
			ApiParameterError error = null;
			switch (amortizationMethod) {
			case EQUAL_PRINCIPAL:
				break;
			case EQUAL_INSTALLMENTS:
				break;
			default:
				error = ApiParameterError.parameterError("validation.msg.product.amortization.method.invalid", "The parameter amortizationMethod is invalid.", "amortizationMethod", command.getAmortizationMethod());
				dataValidationErrors.add(error);
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.amortization.method.cannot.be.empty", "The parameter amortizationMethod cannot be empty.", "amortizationMethod");
			dataValidationErrors.add(error);
		}
		
		if (command.getExpectedDisbursementDate() == null) {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.expectedDisbursementDate.method.cannot.be.blank", "The parameter expectedDisbursementDate cannot be empty.", "expectedDisbursementDate");
			dataValidationErrors.add(error);
		} else {
			if (command.getRepaymentsStartingFromDate() != null
					&& command.getExpectedDisbursementDate().isAfter(command.getRepaymentsStartingFromDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.expectedDisbursementDate.cannot.be.after.first.repayment.date", 
						"The parameter expectedDisbursementDate has a date which falls after the given first repayment date.", "expectedDisbursementDate", 
						command.getExpectedDisbursementDate(), command.getRepaymentsStartingFromDate());
				dataValidationErrors.add(error);
			}
		}
		
		if (command.getRepaymentsStartingFromDate() != null && command.getInterestCalculatedFromDate() == null) {
			
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.interestCalculatedFromDate.must.be.entered.when.using.repayments.startfrom.field", 
					"The parameter interestCalculatedFromDate cannot be empty when first repayment date is provided.", "interestCalculatedFromDate", command.getRepaymentsStartingFromDate());
			dataValidationErrors.add(error);
		} else if (command.getRepaymentsStartingFromDate() == null && command.getInterestCalculatedFromDate() != null) {
			
			// validate interestCalculatedFromDate is after or on repaymentsStartingFromDate
			if (command.getExpectedDisbursementDate().isAfter(command.getInterestCalculatedFromDate())) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.loan.interestCalculatedFromDate.cannot.be.before.disbursement.date", 
						"The parameter interestCalculatedFromDate cannot be before the date given for expected disbursement.", "interestCalculatedFromDate", command.getInterestCalculatedFromDate(), command.getExpectedDisbursementDate());
				dataValidationErrors.add(error);
			}
		}
		
		if (command.getInterestRatePerPeriod()!= null) {
			if (command.getInterestRatePerPeriod().doubleValue() < Double.valueOf("0.0")) {
				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.rate.per.period.amount.cannot.be.negative", 
						"The parameter interestRatePerPeriodFormatted cannot be less than zero.", "interestRatePerPeriodFormatted", 
						command.getInterestRatePerPeriod());
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.rate.per.period.amount.cannot.be.empty", 
					"The parameter interestRatePerPeriodFormatted cannot be empty.", "interestRatePerPeriodFormatted", 
					command.getInterestRatePerPeriod());
			dataValidationErrors.add(error);
		}
		
		// interest rate period type
		if (command.getInterestRateFrequencyMethod() != null) {
			PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
			ApiParameterError error = null;
			switch (frequencyType) {
			case DAYS:
				break;
			case WEEKS:
				break;
			case MONTHS:
				break;
			case YEARS:
				break;
			default:
				error = ApiParameterError.parameterError("validation.msg.product.interest.frequency.invalid", 
						"The parameter interestRateFrequencyMethod cannot be empty.", "interestRateFrequencyMethod", command.getInterestRateFrequencyMethod());
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.frequency.cannot.be.empty", 
					"The parameter interestRateFrequencyMethod cannot be empty.", "interestRateFrequencyMethod");
			dataValidationErrors.add(error);
		}
		
		if (command.getInterestMethod() != null) {
			InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
			ApiParameterError error = null;
			switch (interestMethod) {
			case DECLINING_BALANCE:
				break;
			case FLAT:
				break;
			default:
				error = ApiParameterError.parameterError("validation.msg.product.interest.method.invalid", 
						"The parameter interestMethod is invalid.", "interestMethod", command.getInterestMethod());
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.method.cannot.be.empty", 
					"The parameter interestMethod cannot be empty.", "interestMethod");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
	private ApiParameterError validateNumberExistsAndInRange(String errorCodePreFix, String fieldName, Integer value, int min, int max) {
		ApiParameterError error = null;
		if (value == null) {
			error = ApiParameterError.parameterError(errorCodePreFix + ".cannot.be.empty", "The parameter " + fieldName + " cannot be blank.", fieldName);
		} else {
			if (value < min || value > max) {
				error = ApiParameterError.parameterError(errorCodePreFix + ".outside.allowed.range", "The parameter " + fieldName + " must be between {0} and {1} inclusive.", fieldName, min, max);
			}
		}
		return error;
	}
}