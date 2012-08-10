package org.mifosng.platform.loanproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.ApiParameterError;
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
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		
		baseDataValidator.reset().parameter("principal").value(command.getPrincipal()).ignoreIfNull().notNull().positiveAmount();
		baseDataValidator.reset().parameter("inArrearsTolerance").value(command.getInArrearsTolerance()).ignoreIfNull().notNull().zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("repaymentFrequencyType").value(command.getRepaymentFrequencyType()).ignoreIfNull().notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEvery()).ignoreIfNull().notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepayments()).ignoreIfNull().notNull().integerGreaterThanZero();
		
		baseDataValidator.reset().parameter("interestRatePerPeriod").value(command.getInterestRatePerPeriod()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("interestRateFrequencyType").value(command.getInterestRateFrequencyType()).ignoreIfNull().notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("amortizationType").value(command.getAmortizationType()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestCalculationPeriodType").value(command.getInterestCalculationPeriodType()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		
		baseDataValidator.reset().anyOfNotNull(command.getFundId(), command.getTransactionProcessingStrategyId(), command.getName(), command.getDescription(), command.getCurrencyCode(), command.getDigitsAfterDecimal(),
				command.getPrincipal(), command.getInArrearsTolerance(), command.getRepaymentFrequencyType(), command.getRepaymentEvery(), command.getNumberOfRepayments(),
				command.getInterestRatePerPeriod(), command.getInterestRateFrequencyType(), command.getAmortizationType(), command.getInterestType(), command.getInterestCalculationPeriodType());
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForCreate() {
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
		
		baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
		baseDataValidator.reset().parameter("description").value(command.getDescription()).notExceedingLengthOf(500);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
		
		baseDataValidator.reset().parameter("principal").value(command.getPrincipal()).notNull().positiveAmount();
		baseDataValidator.reset().parameter("inArrearsTolerance").value(command.getInArrearsTolerance()).notNull().zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("repaymentFrequencyType").value(command.getRepaymentFrequencyType()).notNull().inMinMaxRange(0, 3);
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEvery()).notNull().integerGreaterThanZero();
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepayments()).notNull().integerGreaterThanZero();
		
		baseDataValidator.reset().parameter("interestRatePerPeriod").value(command.getInterestRatePerPeriod()).notNull();
		baseDataValidator.reset().parameter("interestRateFrequencyType").value(command.getInterestRateFrequencyType()).notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("amortizationType").value(command.getAmortizationType()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestCalculationPeriodType").value(command.getInterestCalculationPeriodType()).notNull().inMinMaxRange(0, 1);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
}