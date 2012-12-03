package org.mifosplatform.useradministration.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a role.
 * 
 * <p>Fields that are transient are intended not to be serialized into JSON.</p>
 */
public class RoleCommand {

    private final transient Long id;
    private final String name;
    private final String description;

    private final transient boolean makerCheckerApproval;
    private final transient Set<String> modifiedParameters;

    public RoleCommand(
            final Set<String> modifiedParameters,
            final boolean makerCheckerApproval,
            final Long id, 
            final String name, 
            final String description) {
        this.modifiedParameters = modifiedParameters;
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNameChanged() {
        return this.modifiedParameters.contains("name");
    }

    public boolean isDescriptionChanged() {
        return this.modifiedParameters.contains("description");
    }
    
    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
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

        baseDataValidator.reset().parameter("id").value(this.id).notNull();
        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("description").value(this.description).ignoreIfNull().notBlank()
                .notExceedingLengthOf(500);

        baseDataValidator.reset().anyOfNotNull(this.name, this.description);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }        
    }
}