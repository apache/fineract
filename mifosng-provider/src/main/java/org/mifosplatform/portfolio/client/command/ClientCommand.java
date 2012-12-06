package org.mifosplatform.portfolio.client.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 *
 */
public class ClientCommand {

    private final String externalId;
    private final String firstname;
    private final String lastname;
    private final String clientOrBusinessName;
    private final Long officeId;
    private final LocalDate joinedDate;

    public ClientCommand(final String externalId, final String firstname, final String lastname, final String clientOrBusinessName,
            final Long officeId, final LocalDate joinedDate) {
        this.externalId = externalId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.clientOrBusinessName = clientOrBusinessName;
        this.officeId = officeId;
        this.joinedDate = joinedDate;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");

        if (StringUtils.isNotBlank(this.clientOrBusinessName)) {
            baseDataValidator.reset().parameter("firstname").value(this.firstname)
                    .mustBeBlankWhenParameterProvided("clientOrBusinessName", this.clientOrBusinessName);
            baseDataValidator.reset().parameter("lastname").value(this.lastname)
                    .mustBeBlankWhenParameterProvided("clientOrBusinessName", this.clientOrBusinessName);
        } else {
            baseDataValidator.reset().parameter("firstname").value(this.firstname).notBlank();
            baseDataValidator.reset().parameter("lastname").value(this.lastname).notBlank();
        }

        baseDataValidator.reset().parameter("joinedDate").value(this.joinedDate).notBlank();
        baseDataValidator.reset().parameter("externalId").value(this.externalId).notExceedingLengthOf(100);
        baseDataValidator.reset().parameter("officeId").value(this.officeId).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");

        if (this.clientOrBusinessName != null && StringUtils.isNotBlank(this.clientOrBusinessName)) {
            baseDataValidator.reset().parameter("firstname").value(this.firstname)
                    .mustBeBlankWhenParameterProvided("clientOrBusinessName", this.clientOrBusinessName);
            baseDataValidator.reset().parameter("lastname").value(this.lastname)
                    .mustBeBlankWhenParameterProvided("clientOrBusinessName", this.clientOrBusinessName);
        } else if (StringUtils.isBlank(this.clientOrBusinessName)) {
            baseDataValidator.reset().parameter("firstname").value(this.firstname).ignoreIfNull().notBlank();
            baseDataValidator.reset().parameter("lastname").value(this.lastname).ignoreIfNull().notBlank();
        }

        baseDataValidator.reset().parameter("joinedDate").value(this.joinedDate).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("externalId").value(this.externalId).ignoreIfNull().notExceedingLengthOf(100);

        baseDataValidator.reset().anyOfNotNull(this.firstname, this.lastname, this.clientOrBusinessName, this.joinedDate, this.externalId,
                this.officeId);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getClientOrBusinessName() {
        return this.clientOrBusinessName;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public LocalDate getJoiningDate() {
        return this.joinedDate;
    }
}