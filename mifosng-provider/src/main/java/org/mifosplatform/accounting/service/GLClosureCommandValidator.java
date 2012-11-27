package org.mifosplatform.accounting.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosplatform.accounting.api.commands.GLClosureCommand;

public class GLClosureCommandValidator {

    private final GLClosureCommand command;

    public GLClosureCommandValidator(GLClosureCommand command) {
        this.command = command;
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLClosure");

        baseDataValidator.reset().parameter("closingDate").value(command.getClosingDate()).notBlank();

        baseDataValidator.reset().parameter("officeId").value(command.getOfficeId()).notNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("comments").value(command.getComments()).ignoreIfNull().notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLClosure");

        baseDataValidator.reset().parameter("comments").value(command.getComments()).ignoreIfNull().notExceedingLengthOf(500);

        baseDataValidator.reset().anyOfNotNull(command.getComments());

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}