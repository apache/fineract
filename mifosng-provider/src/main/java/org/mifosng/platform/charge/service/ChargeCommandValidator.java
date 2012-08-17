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

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }

    public void validateForUpdate(){
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("charge");

        baseDataValidator.reset().parameter("id").value(command.getId()).notNull();
        baseDataValidator.reset().parameter("name").value(command.getName()).notBlank().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("amount").value(command.getAmount()).notNull().positiveAmount();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
        }
    }
}
