package org.mifosplatform.infrastructure.office.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private final transient Set<String> modifiedParameters;
    private final transient boolean makerCheckerApproval;
    private final transient Long id;

    public OfficeCommand(final Set<String> modifiedParameters, final boolean makerCheckerApproval, final Long id, final String officeName,
            final String externalId, final Long parentId, final LocalDate openingDate) {
        this.modifiedParameters = modifiedParameters;
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.name = officeName;
        this.externalId = externalId;
        this.parentId = parentId;
        this.openingDate = openingDate;
    }

    public Long getId() {
        return id;
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

    public boolean isNameChanged() {
        return this.modifiedParameters.contains("name");
    }

    public boolean isExternalIdChanged() {
        return this.modifiedParameters.contains("externalId");
    }

    public boolean isOpeningDateChanged() {
        return this.modifiedParameters.contains("openingDate");
    }

    public boolean isParentChanged() {
        return this.modifiedParameters.contains("parentId");
    }
    
    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
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

        baseDataValidator.reset().parameter("id").value(this.id).notNull();
        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("openingDate").value(this.openingDate).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("externalId").value(this.externalId).ignoreIfNull().notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("parentId").value(this.parentId).ignoreIfNull().notNull().integerGreaterThanZero();

        baseDataValidator.reset().anyOfNotNull(this.name, this.openingDate, this.externalId, this.parentId);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }        
    }
}