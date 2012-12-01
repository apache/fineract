package org.mifosplatform.infrastructure.documentmanagement.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class DocumentCommandValidator {

    private final DocumentCommand command;

    public DocumentCommandValidator(DocumentCommand command) {
        this.command = command;
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("document");

        baseDataValidator.reset().parameter("name").value(command.getName()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("size").value(command.getSize()).ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("fileName").value(command.getFileName()).ignoreIfNull().notBlank().notExceedingLengthOf(250);
        baseDataValidator.reset().parameter("location").value(command.getLocation()).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("description").value(command.getName()).ignoreIfNull().notExceedingLengthOf(250);

        baseDataValidator.reset().anyOfNotNull(command.getName(), command.getFileName(), command.getDescription(), command.getLocation(),
                command.getSize());

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("document");
        baseDataValidator.reset().parameter("parentEntityType").value(command.getParentEntityType()).notBlank().notExceedingLengthOf(50);
        baseDataValidator.reset().parameter("parentEntityId").value(command.getParentEntityId()).integerGreaterThanZero();
        baseDataValidator.reset().parameter("name").value(command.getName()).notBlank().notExceedingLengthOf(250);
        baseDataValidator.reset().parameter("size").value(command.getSize()).integerGreaterThanZero();
        baseDataValidator.reset().parameter("fileName").value(command.getFileName()).notBlank().notExceedingLengthOf(250);
        baseDataValidator.reset().parameter("description").value(command.getName()).notExceedingLengthOf(250);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
