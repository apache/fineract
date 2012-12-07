package org.mifosplatform.useradministration.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a role.
 */
public class RoleCommand {

    private final String name;
    private final String description;

    public RoleCommand(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("role");

        baseDataValidator.reset().parameter("name").value(this.name).notBlank();
        baseDataValidator.reset().parameter("description").value(this.description).notBlank().notExceedingLengthOf(500);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("role");

        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("description").value(this.description).ignoreIfNull().notBlank().notExceedingLengthOf(500);

        baseDataValidator.reset().anyOfNotNull(this.name, this.description);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}