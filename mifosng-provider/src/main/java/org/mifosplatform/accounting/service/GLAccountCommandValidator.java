package org.mifosplatform.accounting.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.accounting.api.commands.GLAccountCommand;
import org.mifosplatform.accounting.domain.GLAccountType;
import org.mifosplatform.accounting.domain.GLAccountUsage;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

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

        baseDataValidator.reset().parameter("classification").value(command.getClassification()).notNull()
                .inMinMaxRange(GLAccountType.getMinValue(), GLAccountType.getMaxValue());

        baseDataValidator.reset().parameter("usage").value(command.getUsage())
                .inMinMaxRange(GLAccountUsage.getMinValue(), GLAccountUsage.getMaxValue());

        baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notExceedingLengthOf(500);

        baseDataValidator.reset().parameter("manualEntriesAllowed").value(command.getManualEntriesAllowed()).notBlank();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("GLAccount");

        baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank().notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("glCode").ignoreIfNull().value(command.getGlCode()).notBlank().notExceedingLengthOf(45);

        baseDataValidator.reset().parameter("parentId").value(command.getParentId()).ignoreIfNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("classification").value(command.getClassification()).ignoreIfNull()
                .inMinMaxRange(GLAccountType.getMinValue(), GLAccountType.getMaxValue());
        baseDataValidator.reset().parameter("usage").value(command.getUsage()).ignoreIfNull()
                .inMinMaxRange(GLAccountUsage.getMinValue(), GLAccountUsage.getMaxValue());

        baseDataValidator.reset().parameter("description").value(command.getDescription()).ignoreIfNull().notBlank()
                .notExceedingLengthOf(500);

        baseDataValidator.reset().parameter("disabled").value(command.getDisabled()).ignoreIfNull();

        baseDataValidator.reset().anyOfNotNull(command.getName(), command.getGlCode(), command.getParentId(), command.getClassification(),
                command.getDescription(), command.getDisabled());

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}