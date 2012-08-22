package org.mifosng.platform.charge.service;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class ChargeCommandValidator {

    private final ChargeCommand command;

    public ChargeCommandValidator(ChargeCommand command) {
        this.command = command;
    }

    public void validateForCreate(){
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("name").value(command.getName()).notBlank().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("amount").value(command.getAmount()).notNull().positiveAmount();
        baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
        baseDataValidator.reset().parameter("chargeAppliesTo").value(command.getChargeAppliesTo()).notNull().inMinMaxRange(0, 0);
        baseDataValidator.reset().parameter("chargeTimeType").value(command.getChargeTimeType()).notNull().inMinMaxRange(0, 0);
        baseDataValidator.reset().parameter("chargeCalculationMethod").value(command.getChargeCalculationType()).notNull().inMinMaxRange(0, 3);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

    public void validateForUpdate(){
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
        baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("amount").value(command.getAmount()).ignoreIfNull().positiveAmount();
        baseDataValidator.reset().parameter("currencyCode").value(command.getCurrencyCode()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("chargeAppliesTo").value(command.getChargeAppliesTo()).ignoreIfNull().notNull().inMinMaxRange(0, 0);
        baseDataValidator.reset().parameter("chargeTimeType").value(command.getChargeTimeType()).ignoreIfNull().notNull().inMinMaxRange(0, 0);
        baseDataValidator.reset().parameter("chargeCalculationMethod").value(command.getChargeCalculationType()).ignoreIfNull().notNull().inMinMaxRange(0, 3);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
}
