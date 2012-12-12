package org.mifosplatform.organisation.office.command;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a office.
 */
public class OfficeCommand {

    private final String name;
    private final String externalId;
    private final LocalDate openingDate;
    private final Long parentId;

    public OfficeCommand(final String officeName, final String externalId, final Long parentId, final LocalDate openingDate) {
        this.name = officeName;
        this.externalId = externalId;
        this.parentId = parentId;
        this.openingDate = openingDate;
    }

    public String getName() {
        return name;
    }

    public String getExternalId() {
        return externalId;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public Long getParentId() {
        return parentId;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("office");

        baseDataValidator.reset().parameter("name").value(this.name).notBlank();
        baseDataValidator.reset().parameter("openingDate").value(this.openingDate).notBlank();
        baseDataValidator.reset().parameter("externalId").value(this.externalId).notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("parentId").value(this.parentId).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("office");

        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("openingDate").value(this.openingDate).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("externalId").value(this.externalId).ignoreIfNull().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("parentId").value(this.parentId).ignoreIfNull().notNull().integerGreaterThanZero();

        baseDataValidator.reset().anyOfNotNull(this.name, this.openingDate, this.externalId, this.parentId);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}