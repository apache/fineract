package org.mifosplatform.infrastructure.staff.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a staff member.
 */
public class StaffCommand {

    private final String firstname;
    private final String lastname;
    private final Long officeId;
    private final Boolean isLoanOfficer;

    private final transient Set<String> parametersPassedInRequest;
    private final transient boolean makerCheckerApproval;
    private final transient Long id;

    public StaffCommand(final Set<String> parametersPassedInRequest, final boolean makerCheckerApproval, final Long id,
            final Long officeId, final String firstName, final String lastName, final Boolean isLoanOfficer) {
        this.parametersPassedInRequest = parametersPassedInRequest;
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.firstname = firstName;
        this.lastname = lastName;
        this.officeId = officeId;
        this.isLoanOfficer = isLoanOfficer;
    }

    public Long getId() {
        return id;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public Boolean getIsLoanOfficer() {
        return this.isLoanOfficer;
    }

    public boolean isFirstnameChanged() {
        return this.parametersPassedInRequest.contains("firstname");
    }

    public boolean isLastnameChanged() {
        return this.parametersPassedInRequest.contains("lastname");
    }

    public boolean isLoanOfficerFlagChanged() {
        return this.parametersPassedInRequest.contains("isLoanOfficer");
    }

    public boolean isOfficeChanged() {
        return this.parametersPassedInRequest.contains("officeId");
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff");

        baseDataValidator.reset().parameter("firstname").value(this.firstname).notBlank().notExceedingLengthOf(50);
        baseDataValidator.reset().parameter("lastname").value(this.lastname).notBlank().notExceedingLengthOf(50);
        baseDataValidator.reset().parameter("officeId").value(this.officeId).notNull().integerGreaterThanZero();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("staff");

        baseDataValidator.reset().parameter("id").value(this.id).notNull();
        baseDataValidator.reset().parameter("officeId").value(this.officeId).ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("firstname").value(this.firstname).ignoreIfNull().notBlank().notExceedingLengthOf(50);
        baseDataValidator.reset().parameter("lastname").value(this.lastname).ignoreIfNull().notBlank().notExceedingLengthOf(50);

        baseDataValidator.reset().parameter("loanOfficerFlag").value(this.isLoanOfficer).ignoreIfNull();

        baseDataValidator.reset().anyOfNotNull(this.firstname, this.lastname, this.officeId, this.isLoanOfficer);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }        
    }
}