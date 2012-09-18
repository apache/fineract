package org.mifosng.platform.loan.service;


import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class LoanChargeCommandValidator {

    private final LoanChargeCommand command;

    public LoanChargeCommandValidator(LoanChargeCommand command) {
        this.command = command;
    }

    public void validate(){
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("chargeId").value(command.getChargeId()).notNull().longGreaterThanZero();
        baseDataValidator.reset().parameter("amount").value(command.getAmount()).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("chargeTimeType").value(command.getChargeTimeType()).ignoreIfNull().inMinMaxRange(1, 1);
        baseDataValidator.reset().parameter("chargeCalculationType").value(command.getChargeCalculationType()).ignoreIfNull().inMinMaxRange(1, 4);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
}
