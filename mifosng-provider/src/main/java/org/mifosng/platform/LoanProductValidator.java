package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.CreateLoanProductCommand;
import org.mifosng.data.command.LoanProductCommandData;
import org.mifosng.data.command.UpdateLoanProductCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public class LoanProductValidator {

	public void validateForUpdate(UpdateLoanProductCommand command) {
		
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		Long productId = command.getId();
		if (productId == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.product.id.cannot.be.empty", "id");
			dataValidationErrors.add(error);
		}
		
		validateRemainingAttributes(command, dataValidationErrors);
	}
	
	public void validateForCreate(CreateLoanProductCommand command) {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		validateRemainingAttributes(command, dataValidationErrors);
	}
	
	private void validateRemainingAttributes(LoanProductCommandData command, List<ErrorResponse> dataValidationErrors) {
		
		if (StringUtils.isBlank(command.getName())) {
			ErrorResponse error = new ErrorResponse("validation.msg.product.name.cannot.be.blank", "name");
			dataValidationErrors.add(error);
		}
		
		if (command.getDescription() != null) {
			if (command.getDescription().length() > 500) {
				ErrorResponse error = new ErrorResponse("validation.msg.product.description.exceeds.max.length", "description");
				dataValidationErrors.add(error);
			}
		}
		
		if (StringUtils.isBlank(command.getCurrencyCode())) {
			ErrorResponse error = new ErrorResponse("validation.msg.product.currency.cannot.be.empty", "currencyCode");
			dataValidationErrors.add(error);
		}
		
		if (command.getDigitsAfterDecimal() != null) {
			
			if (command.getDigitsAfterDecimal() < 0 || command.getDigitsAfterDecimal() > 6) {
				ErrorResponse error = new ErrorResponse("validation.msg.product.currency.digitsAfterDecimal.must.be.between.zero.and.six.inclusive", "selectedDigitsAfterDecimal", command.getDigitsAfterDecimal());
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.currency.digitsAfterDecimal.cannot.be.empty", "selectedDigitsAfterDecimal");
			dataValidationErrors.add(error);
		}
		
		if (command.getPrincipal() != null) {
			if (command.getPrincipal().doubleValue() <= Double.valueOf("0.0")) {
				ErrorResponse error = new ErrorResponse("validation.msg.product.principal.amount.must.be.greater.than.zero", "amount");
				dataValidationErrors.add(error);
			}
			// TODO - check number of digits before decimal and after decimal
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.principal.amount.cannot.be.empty", "amount");
			dataValidationErrors.add(error);
		}
		
		if (command.getRepaymentFrequency() != null) {
			PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequency());
			ErrorResponse error = null;
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
				error = new ErrorResponse("validation.msg.product.repayment.frequency.invalid", "selectedRepaymentFrequencyOption");
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.repayment.frequency.cannot.be.empty", "selectedRepaymentFrequencyOption");
			dataValidationErrors.add(error);
		}
		
		if (command.getNumberOfRepayments() != null) {
			if (command.getNumberOfRepayments() < 1 || command.getNumberOfRepayments() > 100) {
				ErrorResponse error = new ErrorResponse("validation.msg.product.number.of.repayments.is.out.of.range", "numberOfRepayments");
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.number.of.repayments.cannot.be.empty", "numberOfRepayments");
			dataValidationErrors.add(error);
		}
		
		if (command.getAmortizationMethod() != null) {
			AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());
			ErrorResponse error = null;
			switch (amortizationMethod) {
			case EQUAL_PRINCIPAL:
				break;
			case EQUAL_INSTALLMENTS:
				break;
			default:
				error = new ErrorResponse("validation.msg.product.amortization.method.invalid", "selectedAmortizationMethodOption");
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.amortization.method.cannot.be.empty", "selectedAmortizationMethodOption");
			dataValidationErrors.add(error);
		}
		
		// refactor in validation of monetary value.
		if (command.getInArrearsToleranceAmount() != null) {
			if (command.getInArrearsToleranceAmount().doubleValue() < Double.valueOf("0.0")) {
				ErrorResponse error = new ErrorResponse("validation.msg.product.arrears.tolerance.amount.cannot.be.negative", "inArrearsTolerance");
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.arrears.tolerance.amount.cannot.be.empty", "inArrearsTolerance");
			dataValidationErrors.add(error);
		}
		
		if (command.getInterestRatePerPeriod()!= null) {
			if (command.getInterestRatePerPeriod().doubleValue() < Double.valueOf("0.0")) {
				ErrorResponse error = new ErrorResponse("validation.msg.product.interest.rate.per.period.amount.cannot.be.negative", "nominalInterestRate");
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.interest.rate.per.period.amount.cannot.be.empty", "nominalInterestRate");
			dataValidationErrors.add(error);
		}
		
		// interest rate period type
		if (command.getInterestRateFrequencyMethod() != null) {
			PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
			ErrorResponse error = null;
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
				error = new ErrorResponse("validation.msg.product.interest.frequency.invalid", "selectedInterestFrequencyOption");
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.interest.frequency.cannot.be.empty", "selectedInterestFrequencyOption");
			dataValidationErrors.add(error);
		}
		
		if (command.getInterestMethod() != null) {
			InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
			ErrorResponse error = null;
			switch (interestMethod) {
			case DECLINING_BALANCE:
				break;
			case FLAT:
				break;
			default:
				error = new ErrorResponse("validation.msg.product.interest.method.invalid", "selectedInterestMethodOption");
				break;
			}
			if (error != null) {
				dataValidationErrors.add(error);
			}
		} else {
			ErrorResponse error = new ErrorResponse("validation.msg.product.interest.method.cannot.be.empty", "selectedInterestMethodOption");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Validation errors exist.");
		}
		
	}

	private ErrorResponse validateNumberExistsAndInRange(String errorCodePreFix, String fieldName, Integer value, int min, int max) {
		ErrorResponse error = null;
		if (value == null) {
			error = new ErrorResponse(errorCodePreFix + ".cannot.be.empty", fieldName);
		} else {
			if (value < min || value > max) {
				error = new ErrorResponse(errorCodePreFix + ".outside.allowed.range", fieldName);
			}
		}
		return error;
	}
}