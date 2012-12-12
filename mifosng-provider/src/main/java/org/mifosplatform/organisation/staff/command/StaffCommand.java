package org.mifosplatform.organisation.staff.command;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a staff member.
 */
public class StaffCommand {

    private final String firstname;
    private final String lastname;
    private final Long officeId;
    private final Boolean isLoanOfficer;

    public StaffCommand(final Long officeId, final String firstName, final String lastName, final Boolean isLoanOfficer) {
        this.firstname = firstName;
        this.lastname = lastName;
        this.officeId = officeId;
        this.isLoanOfficer = isLoanOfficer;
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

        baseDataValidator.reset().parameter("officeId").value(this.officeId).ignoreIfNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("firstname").value(this.firstname).ignoreIfNull().notBlank().notExceedingLengthOf(50);
        baseDataValidator.reset().parameter("lastname").value(this.lastname).ignoreIfNull().notBlank().notExceedingLengthOf(50);

        baseDataValidator.reset().parameter("loanOfficerFlag").value(this.isLoanOfficer).ignoreIfNull();

        baseDataValidator.reset().anyOfNotNull(this.firstname, this.lastname, this.officeId, this.isLoanOfficer);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}