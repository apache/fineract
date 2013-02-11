package org.mifosplatform.portfolio.savingsaccountproduct.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.savingsaccountproduct.command.SavingProductCommand;

public class SavingProductCommandValidator {

    private final SavingProductCommand command;

    public SavingProductCommandValidator(final SavingProductCommand command) {
        this.command = command;
    }

    public void validateForUpdate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("savingsproduct");
        baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
        baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);
        baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().inMinMaxRange(0, 6);
        baseDataValidator.reset().parameter("interestRate").value(command.getInterestRate()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("minInterestRate").value(command.getMinInterestRate()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("maxInterestRate").value(command.getMaxInterestRate()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("minInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getMinInterestRate(), command.getMaxInterestRate());
        baseDataValidator.reset().parameter("interestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getInterestRate(), command.getMaxInterestRate());
        baseDataValidator.reset().parameter("minInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getMinInterestRate(), command.getInterestRate());
        baseDataValidator.reset().parameter("savingsDepositAmount").value(command.getSavingsDepositAmount()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("savingProductType").value(command.getSavingProductType()).ignoreIfNull();
        baseDataValidator.reset().parameter("tenureType").value(command.getTenureType()).ignoreIfNull();
        baseDataValidator.reset().parameter("tenure").value(command.getTenure()).ignoreIfNull();
        baseDataValidator.reset().parameter("frequency").value(command.getFrequency()).ignoreIfNull();
        baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).ignoreIfNull();
        baseDataValidator.reset().parameter("interestCalculationMethod").value(command.getInterestCalculationMethod()).ignoreIfNull();
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(command.getMinimumBalanceForWithdrawal()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("isPartialDepositAllowed").value(command.isPartialDepositAllowed()).ignoreIfNull();
        baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(command.isLockinPeriodAllowed()).ignoreIfNull();
        baseDataValidator.reset().parameter("lockinPeriod").value(command.getLockinPeriod()).ignoreIfNull();
        baseDataValidator.reset().parameter("lockinPeriodType").value(command.getLockinPeriodType()).ignoreIfNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("product");
        baseDataValidator.reset().parameter("name").value(command.getName()).notBlank();
        baseDataValidator.reset().parameter("description").value(command.getDescription()).notExceedingLengthOf(500);
        baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
        baseDataValidator.reset().parameter("interestRate").value(command.getInterestRate()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("minInterestRate").value(command.getMinInterestRate()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("maxInterestRate").value(command.getMaxInterestRate()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("minInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getMinInterestRate(), command.getMaxInterestRate());
        baseDataValidator.reset().parameter("interestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getInterestRate(), command.getMaxInterestRate());
        baseDataValidator.reset().parameter("minInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getMinInterestRate(), command.getInterestRate());
        baseDataValidator.reset().parameter("savingsDepositAmount").value(command.getSavingsDepositAmount()).notNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("savingProductType").value(command.getSavingProductType()).notNull();
        baseDataValidator.reset().parameter("tenureType").value(command.getTenureType()).notNull();
        baseDataValidator.reset().parameter("tenure").value(command.getTenure()).notNull();
        baseDataValidator.reset().parameter("frequency").value(command.getFrequency()).notNull();
        baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).notNull();
        baseDataValidator.reset().parameter("interestCalculationMethod").value(command.getInterestCalculationMethod()).notNull();
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(command.getMinimumBalanceForWithdrawal()).notNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("isPartialDepositAllowed").value(command.isPartialDepositAllowed())
                .trueOrFalseRequired(command.isPartialDepositAllowedChanged()).notNull();
        baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(command.isLockinPeriodAllowed())
                .trueOrFalseRequired(command.isLockinPeriodAllowedChanged()).notNull();
        baseDataValidator.reset().parameter("lockinPeriod").value(command.getLockinPeriod()).notNull();
        baseDataValidator.reset().parameter("lockinPeriodType").value(command.getLockinPeriodType()).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}