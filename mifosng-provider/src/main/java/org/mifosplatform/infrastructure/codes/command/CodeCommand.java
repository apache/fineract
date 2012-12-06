package org.mifosplatform.infrastructure.codes.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * <p>Immutable command for creating or updating details of a code.</p>
 */
public class CodeCommand {

    private final String name;

    public CodeCommand(final String name) {
        this.name = name;
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

        baseDataValidator.reset().parameter("name").value(this.name).notBlank();

        baseDataValidator.reset().anyOfNotNull(this.name);
        
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}