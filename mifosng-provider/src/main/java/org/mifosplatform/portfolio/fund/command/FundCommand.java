package org.mifosplatform.portfolio.fund.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a fund.
 */
public class FundCommand {

    private final String name;
    private final String externalId;

    private final transient Set<String> parametersPassedInRequest;
    private final transient boolean makerCheckerApproval;
    private final transient Long id;

    public FundCommand(final Set<String> parametersPassedInRequest, final boolean makerCheckerApproval, final Long id,
            final String fundName, final String externalId) {
        this.parametersPassedInRequest = parametersPassedInRequest;
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.name = fundName;
        this.externalId = externalId;
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

    public boolean isNameChanged() {
        return this.parametersPassedInRequest.contains("name");
    }

    public boolean isExternalIdChanged() {
        return this.parametersPassedInRequest.contains("externalId");
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("fund");

        baseDataValidator.reset().parameter("name").value(this.name).notBlank();
        baseDataValidator.reset().parameter("externalId").value(this.externalId).notExceedingLengthOf(100);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("fund");

        baseDataValidator.reset().parameter("id").value(this.id).notNull();
        baseDataValidator.reset().parameter("name").value(this.name).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("externalId").value(this.externalId).ignoreIfNull().notExceedingLengthOf(100);

        baseDataValidator.reset().anyOfNotNull(this.name, this.externalId);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}