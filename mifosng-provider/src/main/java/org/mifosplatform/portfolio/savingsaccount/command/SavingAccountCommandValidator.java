package org.mifosplatform.portfolio.savingsaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class SavingAccountCommandValidator {

    private final SavingAccountCommand command;

    public SavingAccountCommandValidator(final SavingAccountCommand command) {
        this.command = command;
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account");

        baseDataValidator.reset().parameter("clientId").value(command.getClientId()).notNull();
        baseDataValidator.reset().parameter("productId").value(command.getProductId()).notNull();
        baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).notNull().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).notNull().inMinMaxRange(0, 6);
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(command.getSavingsDepositAmount()).notNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("recurringInterestRate").value(command.getRecurringInterestRate()).notNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("savingInterestRate").value(command.getSavingInterestRate()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("savingInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getSavingInterestRate(), command.getRecurringInterestRate());
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
        baseDataValidator.reset().parameter("commencementDate").value(command.getCommencementDate()).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }

    }

    public void validateForUpdate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("saving.account");

        baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
        baseDataValidator.reset().parameter("clientId").value(command.getClientId()).ignoreIfNull().notNull();
        baseDataValidator.reset().parameter("productId").value(command.getProductId()).ignoreIfNull().notNull();
        baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
        baseDataValidator.reset().parameter("digitsAfterDecimal").value(command.getDigitsAfterDecimal()).ignoreIfNull().inMinMaxRange(0, 6);
        baseDataValidator.reset().parameter("savingsDepositAmountPerPeriod").value(command.getSavingsDepositAmount()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("recurringInterestRate").value(command.getRecurringInterestRate()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("savingInterestRate").value(command.getSavingInterestRate()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("savingInterestRate")
                .comapareMinAndMaxOfTwoBigDecmimalNos(command.getSavingInterestRate(), command.getRecurringInterestRate());
        baseDataValidator.reset().parameter("savingProductType").value(command.getSavingProductType()).ignoreIfNull();
        baseDataValidator.reset().parameter("tenureType").value(command.getTenureType()).ignoreIfNull();
        baseDataValidator.reset().parameter("tenure").value(command.getTenure()).ignoreIfNull();
        baseDataValidator.reset().parameter("frequency").value(command.getFrequency()).ignoreIfNull();
        baseDataValidator.reset().parameter("interestType").value(command.getInterestType()).ignoreIfNull();
        baseDataValidator.reset().parameter("interestCalculationMethod").value(command.getInterestCalculationMethod()).ignoreIfNull();
        baseDataValidator.reset().parameter("minimumBalanceForWithdrawal").value(command.getMinimumBalanceForWithdrawal()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("isPartialDepositAllowed").value(command.isPartialDepositAllowed())
                .trueOrFalseRequired(command.isPartialDepositAllowedChanged()).ignoreIfNull();
        baseDataValidator.reset().parameter("isLockinPeriodAllowed").value(command.isLockinPeriodAllowed())
                .trueOrFalseRequired(command.isLockinPeriodAllowedChanged()).ignoreIfNull();
        baseDataValidator.reset().parameter("lockinPeriod").value(command.getLockinPeriod()).ignoreIfNull();
        baseDataValidator.reset().parameter("lockinPeriodType").value(command.getLockinPeriodType()).ignoreIfNull();
        baseDataValidator.reset().parameter("commencementDate").value(command.getCommencementDate()).ignoreIfNull();
        baseDataValidator.reset().parameter("commencementDate").value(command.getCommencementDate()).ignoreIfNull();

        if (command.isNoFieldChanged()) {
            StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append("deposit.account").append(
                    ".no.parameters.for.update");
            StringBuilder defaultEnglishMessage = new StringBuilder("No parameters passed for update.");
            ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
                    "id");
            dataValidationErrors.add(error);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }

    }

}
