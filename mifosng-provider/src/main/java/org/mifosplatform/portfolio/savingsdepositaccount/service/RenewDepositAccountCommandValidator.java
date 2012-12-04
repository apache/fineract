package org.mifosplatform.portfolio.savingsdepositaccount.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositAccountCommand;

public class RenewDepositAccountCommandValidator {

    private final DepositAccountCommand command;

    public RenewDepositAccountCommandValidator(final DepositAccountCommand command) {
        this.command = command;
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.account");

        baseDataValidator.reset().parameter("clientId").value(command.getClientId()).ignoreIfNull();
        baseDataValidator.reset().parameter("productId").value(command.getProductId()).ignoreIfNull();
        baseDataValidator.reset().parameter("externalId").value(command.getExternalId()).ignoreIfNull().notExceedingLengthOf(100);

        baseDataValidator.reset().parameter("deposit").value(command.getDepositAmount()).ignoreIfNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("maturityInterestRate").value(command.getMaturityInterestRate()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("preClosureInterestRate").value(command.getPreClosureInterestRate()).ignoreIfNull()
                .zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("tenureInMonths").value(command.getTenureInMonths()).ignoreIfNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("interestCompoundedEvery").value(command.getInterestCompoundedEvery()).ignoreIfNull()
                .integerGreaterThanZero();
        baseDataValidator.reset().parameter("interestCompoundedEveryPeriodType").value(command.getInterestCompoundedEveryPeriodType())
                .ignoreIfNull().inMinMaxRange(2, 2);
        baseDataValidator.reset().parameter("commencementDate").value(command.getCommencementDate()).ignoreIfNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
