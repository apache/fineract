package org.mifosplatform.accounting.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosplatform.accounting.api.commands.GLAccountCommand;

public class GLAccountCommandValidator {

    private final GLAccountCommand command;

    public GLAccountCommandValidator(GLAccountCommand command) {
        this.command = command;
    }

    public void validateForCreate() {

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLAccount");

        baseDataValidator.reset().parameter("name").value(command.getName()).notBlank().notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("glCode").value(command.getGlCode()).notBlank().notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("parentId").value(command.getParentId()).ignoreIfNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("classification").value(command.getClassification()).notBlank().notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLAccount");

        baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank().notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("glCode").ignoreIfNull().value(command.getGlCode()).notBlank().notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("parentId").value(command.getParentId()).ignoreIfNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("classification").value(command.getClassification()).ignoreIfNull().notBlank()
                .notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notBlank()
                .notExceedingLengthOf(500);

        baseDataValidator.reset().anyOfNotNull(command.getName(), command.getGlCode(), command.getParentId(), command.getClassification(),
                command.getDescription());

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}