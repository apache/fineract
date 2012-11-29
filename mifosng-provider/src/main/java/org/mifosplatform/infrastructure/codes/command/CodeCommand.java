package org.mifosplatform.infrastructure.codes.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a code.
 */
public class CodeCommand {

    private final String name;

    private final transient Set<String> parametersPassedInRequest;
    private final transient boolean makerCheckerApproval;
    private final transient Long id;

    public CodeCommand(final Set<String> modifiedParameters, final boolean makerCheckerApproval, final Long id, final String name) {
        this.parametersPassedInRequest = modifiedParameters;
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Long getId() {
        return this.id;
    }

    public boolean isNameChanged() {
        return this.parametersPassedInRequest.contains("name");
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("code");
        baseDataValidator.reset().parameter("name").value(this.name).notBlank();
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("code");
        
        baseDataValidator.reset().parameter("id").value(this.id).notNull();
        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank();
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}