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
	
}