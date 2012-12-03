package org.mifosplatform.portfolio.savingsdepositaccount.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class WithDrawDepositAccountInterestCommandValidator {

    private final DepositAccountWithdrawInterestCommand command;

    public WithDrawDepositAccountInterestCommandValidator(final DepositAccountWithdrawInterestCommand command) {
        this.command = command;
    }

    public void validate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposit.transaction");

        baseDataValidator.reset().parameter("accountId").value(command.getAccountId()).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("amount").value(command.getWithdrawInterest()).notNull().zeroOrPositiveAmount();
        baseDataValidator.reset().parameter("note").value(command.getNote()).notNull();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
