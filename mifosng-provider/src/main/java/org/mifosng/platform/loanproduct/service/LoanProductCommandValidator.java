package org.mifosng.platform.loanproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.LoanProductCommand;
import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

public class LoanProductCommandValidator {

	private final LoanProductCommand command;

	public LoanProductCommandValidator(LoanProductCommand command) {
		this.command = command;
	}
	
	public void validateForUpdate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
		
		baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
		baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		
		baseDataValidator.reset().parameter("principalFormatted").value(command.getPrincipalFormatted()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("inArrearsToleranceAmountFormatted").value(command.getInArrearsToleranceAmountFormatted()).ignoreIfNull().notBlank();
		
		baseDataValidator.reset().parameter("repaymentFrequency").value(command.getRepaymentFrequency()).ignoreIfNull().notNull().inMinMaxRange(0, 3);
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEvery()).ignoreIfNull().notNull().greaterThanZero();
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepayments()).ignoreIfNull().notNull().greaterThanZero();
		
		baseDataValidator.reset().parameter("interestRatePerPeriodFormatted").value(command.getInterestRatePerPeriodFormatted()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("interestRateFrequencyMethod").value(command.getInterestRateFrequencyMethod()).ignoreIfNull().notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("amortizationMethod").value(command.getAmortizationMethod()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestMethod").value(command.getInterestMethod()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestCalculationPeriodMethod").value(command.getInterestCalculationPeriodMethod()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		
		baseDataValidator.reset().anyOfNotNull(command.getName(), command.getDescription(), command.getExternalId(), command.getCurrencyCode(), command.getDigitsAfterDecimal(),
				command.getPrincipalFormatted(), command.getInArrearsToleranceAmountFormatted(), command.getRepaymentFrequency(), command.getRepaymentEvery(), command.getNumberOfRepayments(),
				command.getInterestRatePerPeriodFormatted(), command.getInterestRateFrequencyMethod(), command.getAmortizationMethod(), command.getInterestMethod(), command.getInterestCalculationPeriodMethod());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
		
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
		
		baseDataValidator.reset().parameter("principalFormatted").value(command.getPrincipalFormatted()).notBlank();
		baseDataValidator.reset().parameter("inArrearsToleranceAmountFormatted").value(command.getInArrearsToleranceAmountFormatted()).notBlank();
		
		baseDataValidator.reset().parameter("repaymentFrequency").value(command.getRepaymentFrequency()).notNull().inMinMaxRange(0, 3);
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEvery()).notNull().greaterThanZero();
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepayments()).notNull().greaterThanZero();
		
		baseDataValidator.reset().parameter("interestRatePerPeriodFormatted").value(command.getInterestRatePerPeriodFormatted()).notBlank();
		baseDataValidator.reset().parameter("interestRateFrequencyMethod").value(command.getInterestRateFrequencyMethod()).notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("amortizationMethod").value(command.getAmortizationMethod()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestMethod").value(command.getInterestMethod()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestCalculationPeriodMethod").value(command.getInterestCalculationPeriodMethod()).notNull().inMinMaxRange(0, 1);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

// FIXME - commented out old validation for now. test that new validation is appropriate 
//	private void validateRemainingAttributes(LoanProductCommand command, List<ApiParameterError> dataValidationErrors) {
//		
//		if (StringUtils.isBlank(command.getName())) {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.name.cannot.be.blank", "The parameter name cannot be blank.", "name");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getDescription() != null) {
//			if (command.getDescription().length() > 500) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.description.exceeds.max.length", "The parameter description cannot exceed max length of {}.", "description", 500);
//				dataValidationErrors.add(error);
//			}
//		}
//		
//		if (StringUtils.isBlank(command.getCurrencyCode())) {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.currency.cannot.be.empty", "The parameter currencyCode cannot be blank.", "currencyCode");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getDigitsAfterDecimal() != null) {
//			
//			if (command.getDigitsAfterDecimal() < 0 || command.getDigitsAfterDecimal() > 6) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.currency.digitsAfterDecimal.must.be.between.zero.and.six.inclusive", "The parameter digitsAfterDecimal must be between {0} and {1} inclusive.", "selectedDigitsAfterDecimal", 0, 6);
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.currency.digitsAfterDecimal.cannot.be.empty", "The parameter digitsAfterDecimal cannot be blank.", "selectedDigitsAfterDecimal");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getPrincipal() != null) {
//			if (command.getPrincipal().doubleValue() <= Double.valueOf("0.0")) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.principal.amount.must.be.greater.than.zero", "The parameter amount must be greater than zero.", "amount");
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.principal.amount.cannot.be.empty", "The parameter amount cannot be blank.", "amount");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getRepaymentFrequency() != null) {
//			PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequency());
//			ApiParameterError error = null;
//			switch (frequencyType) {
//			case DAYS:
//				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 365);
//				break;
//			case WEEKS:
//				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 52);
//				break;
//			case MONTHS:
//				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 12);
//				break;
//			case YEARS:
//				error = validateNumberExistsAndInRange("validation.msg.product.repayment.repaidEvery", "repaidEvery", command.getRepaymentEvery(), 1, 2);
//				break;
//			default:
//				error = ApiParameterError.parameterError("validation.msg.product.repayment.frequency.invalid", "The parameter selectedRepaymentFrequencyOption is invalid.", "selectedRepaymentFrequencyOption");
//				break;
//			}
//			if (error != null) {
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.repayment.frequency.cannot.be.empty", 
//					"The parameter selectedRepaymentFrequencyOption cannot be blank.", "selectedRepaymentFrequencyOption");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getNumberOfRepayments() != null) {
//			if (command.getNumberOfRepayments() < 1 || command.getNumberOfRepayments() > 100) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.number.of.repayments.is.out.of.range", 
//						"The parameter numberOfRepayments must be between {} and {} inclusive.", "numberOfRepayments", 1, 100);
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.number.of.repayments.cannot.be.empty", 
//					"The parameter numberOfRepayments cannot be empty.", "numberOfRepayments");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getAmortizationMethod() != null) {
//			AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());
//			ApiParameterError error = null;
//			switch (amortizationMethod) {
//			case EQUAL_PRINCIPAL:
//				break;
//			case EQUAL_INSTALLMENTS:
//				break;
//			default:
//				error = ApiParameterError.parameterError("validation.msg.product.amortization.method.invalid", 
//						"The parameter selectedAmortizationMethodOption cannot be empty.", "selectedAmortizationMethodOption");
//				break;
//			}
//			if (error != null) {
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.amortization.method.cannot.be.empty", 
//					"The parameter selectedAmortizationMethodOption cannot be empty.", "selectedAmortizationMethodOption");
//			dataValidationErrors.add(error);
//		}
//		
//		// refactor in validation of monetary value.
//		if (command.getInArrearsToleranceAmount() != null) {
//			if (command.getInArrearsToleranceAmount().doubleValue() < Double.valueOf("0.0")) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.arrears.tolerance.amount.cannot.be.negative", 
//						"The parameter inArrearsTolerance cannot be less than zero.", "inArrearsTolerance");
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.arrears.tolerance.amount.cannot.be.empty", 
//					"The parameter inArrearsTolerance cannot be empty.", "inArrearsTolerance");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getInterestRatePerPeriod()!= null) {
//			if (command.getInterestRatePerPeriod().doubleValue() < Double.valueOf("0.0")) {
//				ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.rate.per.period.amount.cannot.be.negative", 
//						"The parameter nominalInterestRate cannot be less than zero.", "nominalInterestRate");
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.rate.per.period.amount.cannot.be.empty", 
//					"The parameter nominalInterestRate cannot be empty.", "nominalInterestRate");
//			dataValidationErrors.add(error);
//		}
//		
//		// interest rate period type
//		if (command.getInterestRateFrequencyMethod() != null) {
//			PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
//			ApiParameterError error = null;
//			switch (frequencyType) {
//			case DAYS:
//				break;
//			case WEEKS:
//				break;
//			case MONTHS:
//				break;
//			case YEARS:
//				break;
//			default:
//				error = ApiParameterError.parameterError("validation.msg.product.interest.frequency.invalid", 
//						"The parameter selectedInterestFrequencyOption is invalid.", "selectedInterestFrequencyOption");
//				break;
//			}
//			if (error != null) {
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.frequency.cannot.be.empty", 
//					"The parameter selectedInterestFrequencyOption cannot be empty.", "selectedInterestFrequencyOption");
//			dataValidationErrors.add(error);
//		}
//		
//		if (command.getInterestMethod() != null) {
//			InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
//			ApiParameterError error = null;
//			switch (interestMethod) {
//			case DECLINING_BALANCE:
//				break;
//			case FLAT:
//				break;
//			default:
//				error = ApiParameterError.parameterError("validation.msg.product.interest.method.invalid", 
//						"The parameter selectedInterestMethodOption is invalid.", "selectedInterestMethodOption");
//				break;
//			}
//			if (error != null) {
//				dataValidationErrors.add(error);
//			}
//		} else {
//			ApiParameterError error = ApiParameterError.parameterError("validation.msg.product.interest.method.cannot.be.empty", 
//					"The parameter selectedInterestMethodOption cannot be empty.", "selectedInterestMethodOption");
//			dataValidationErrors.add(error);
//		}
//		
//		if (!dataValidationErrors.isEmpty()) {
//			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
//		}
//	}
//
//	private ApiParameterError validateNumberExistsAndInRange(String errorCodePreFix, String fieldName, Integer value, int min, int max) {
//		ApiParameterError error = null;
//		if (value == null) {
//			error = ApiParameterError.parameterError(errorCodePreFix + ".cannot.be.empty", "The parameter " + fieldName + " cannot be blank.", fieldName);
//		} else {
//			if (value < min || value > max) {
//				error = ApiParameterError.parameterError(errorCodePreFix + ".outside.allowed.range", "The parameter " + fieldName + " must be between {0} and {1} inclusive.", fieldName, min, max);
//			}
//		}
//		return error;
//	}
}