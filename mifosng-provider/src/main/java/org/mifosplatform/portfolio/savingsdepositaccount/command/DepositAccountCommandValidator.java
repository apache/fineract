package org.mifosplatform.portfolio.savingsdepositaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class DepositAccountCommandValidator {

    private final DepositAccountCommand command;

    public DepositAccountCommandValidator(final DepositAccountCommand command) {
        this.command = command;
    }

    public void validateForUpdate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");

        baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
        baseDataValidator.reset().parameter("clientId").value(command.getClientId()).ignoreIfNull().notNull();
        baseDataValidator.reset().parameter("productId").value(command.getProductId()).ignoreIfNull().notNull();
        baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);

        baseDataValidator.reset().parameter("deposit").value(command.getDepositAmount()).ignoreIfNull().notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("maturityInterestRate").value(command.getMaturityInterestRate()).ignoreIfNull().notNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("tenureInMonths").value(command.getTenureInMonths()).ignoreIfNull().notNull()
                .integerGreaterThanZero();

        baseDataValidator.reset().parameter("interestCompoundedEvery").value(command.getInterestCompoundedEvery()).ignoreIfNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(command.getInterestCompoundedEveryPeriodType())
                .ignoreIfNull().inMinMaxRange(2, 2);
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

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");

        baseDataValidator.reset().parameter("clientId").value(command.getClientId()).notNull();
        baseDataValidator.reset().parameter("productId").value(command.getProductId()).notNull();
        baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);

        baseDataValidator.reset().parameter("deposit").value(command.getDepositAmount()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("maturityInterestRate").value(command.getMaturityInterestRate()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("tenureInMonths").value(command.getTenureInMonths()).ignoreIfNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("interestCompoundedEvery").value(command.getInterestCompoundedEvery()).ignoreIfNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(command.getInterestCompoundedEveryPeriodType())
                .ignoreIfNull().inMinMaxRange(2, 2);
        baseDataValidator.reset().parameter("commencementDate").value(command.getCommencementDate()).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}