package org.mifosplatform.portfolio.client.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a client identifier.
 */
public class ClientIdentifierCommand {

    private final Long documentTypeId;
    private final String documentKey;
    private final String description;

    public ClientIdentifierCommand(final Long documentTypeId, final String documentKey, final String description) {
        this.documentTypeId = documentTypeId;
        this.documentKey = documentKey;
        this.description = description;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public String getDescription() {
        return description;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("clientIdentifier");

        baseDataValidator.reset().parameter("documentTypeId").value(this.documentTypeId).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("documentKey").value(this.documentKey).notBlank();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
    
    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("clientIdentifier");

        baseDataValidator.reset().parameter("documentKey").value(this.documentKey).ignoreIfNull().notBlank();

        // FIXME - kw
//        if (command.isDocumentTypeChanged()) {
//            baseDataValidator.reset().parameter("documentTypeId").value(command.getDocumentTypeId()).notNull().integerGreaterThanZero();
//        }

        baseDataValidator.reset().anyOfNotNull(this.documentTypeId, this.documentKey);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}