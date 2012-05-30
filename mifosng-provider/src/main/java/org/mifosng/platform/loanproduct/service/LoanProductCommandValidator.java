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
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimalValue()).ignoreIfNull().notNull().inMinMaxRange(0, 6);
		
		baseDataValidator.reset().parameter("principal").value(command.getPrincipalValue()).ignoreIfNull().notNull().positiveAmount();
		baseDataValidator.reset().parameter("inArrearsTolerance").value(command.getInArrearsToleranceValue()).ignoreIfNull().notNull().zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("repaymentFrequencyType").value(command.getRepaymentFrequencyType()).ignoreIfNull().notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEveryValue()).ignoreIfNull().notNull().greaterThanZero();
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepaymentsValue()).ignoreIfNull().notNull().greaterThanZero();
		
		baseDataValidator.reset().parameter("interestRatePerPeriod").value(command.getInterestRatePerPeriodValue()).ignoreIfNull().notBlank();
		baseDataValidator.reset().parameter("interestRateFrequencyType").value(command.getInterestRateFrequencyType()).ignoreIfNull().notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("amortizationType").value(command.getAmortizationType()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestCalculationPeriodType").value(command.getInterestCalculationPeriodType()).ignoreIfNull().notNull().inMinMaxRange(0, 1);
		
		baseDataValidator.reset().anyOfNotNull(command.getName(), command.getDescription(), command.getExternalId(), command.getCurrencyCode(), command.getDigitsAfterDecimal(),
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
		baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).notExceedingLengthOf(100);
		baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
		baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimalValue()).notNull().inMinMaxRange(0, 6);
		
		baseDataValidator.reset().parameter("principal").value(command.getPrincipalValue()).notNull().positiveAmount();
		baseDataValidator.reset().parameter("inArrearsTolerance").value(command.getInArrearsToleranceValue()).notNull().zeroOrPositiveAmount();
		
		baseDataValidator.reset().parameter("repaymentFrequencyType").value(command.getRepaymentFrequencyType()).notNull().inMinMaxRange(0, 3);
		baseDataValidator.reset().parameter("repaymentEvery").value(command.getRepaymentEveryValue()).notNull().greaterThanZero();
		baseDataValidator.reset().parameter("numberOfRepayments").value(command.getNumberOfRepaymentsValue()).notNull().greaterThanZero();
		
		baseDataValidator.reset().parameter("interestRatePerPeriod").value(command.getInterestRatePerPeriodValue()).notNull();
		baseDataValidator.reset().parameter("interestRateFrequencyType").value(command.getInterestRateFrequencyType()).notNull().inMinMaxRange(0, 3);
		
		baseDataValidator.reset().parameter("amortizationType").value(command.getAmortizationType()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).notNull().inMinMaxRange(0, 1);
		baseDataValidator.reset().parameter("interestCalculationPeriodType").value(command.getInterestCalculationPeriodType()).notNull().inMinMaxRange(0, 1);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
	
}