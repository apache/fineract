package org.mifosplatform.infrastructure.user.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

/**
 * Immutable command for creating or updating details of a User.
 */
public class UserCommand {

    private final String username;
    private final String firstname;
    private final String lastname;
    private final String password;
    private final String repeatPassword;
    private final String email;
    private final Long officeId;

    private final String[] notSelectedRoles;
    private final String[] roles;

    private transient final boolean makerCheckerApproval;
    private transient final Long id;
    private transient final Set<String> modifiedParameters;

    public UserCommand(final Set<String> modifiedParameters, final boolean makerCheckerApproval, final Long id, final String username,
            final String firstname, final String lastname, final String password, final String repeatPassword, final String email,
            final Long officeId, final String[] notSelectedRoles, final String[] roles) {
        this.modifiedParameters = modifiedParameters;
        this.makerCheckerApproval = makerCheckerApproval;
        this.id = id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.password = password;
        this.repeatPassword = repeatPassword;
        this.email = email;
        this.officeId = officeId;
        this.notSelectedRoles = notSelectedRoles;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPassword() {
        return password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public String getEmail() {
        return email;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public String[] getNotSelectedRoles() {
        return notSelectedRoles;
    }

    public String[] getRoles() {
        return roles;
    }

    public boolean isUsernameChanged() {
        return this.modifiedParameters.contains("username");
    }

    public boolean isFirstnameChanged() {
        return this.modifiedParameters.contains("firstname");
    }

    public boolean isLastnameChanged() {
        return this.modifiedParameters.contains("lastname");
    }

    public boolean isEmailChanged() {
        return this.modifiedParameters.contains("email");
    }

    public boolean isPasswordChanged() {
        return this.modifiedParameters.contains("password") && this.modifiedParameters.contains("repeatPassword");
    }

    public boolean isOfficeChanged() {
        return this.modifiedParameters.contains("officeId");
    }

    public boolean isRolesChanged() {
        return this.modifiedParameters.contains("roles");
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }

    public void validateForCreate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");

        baseDataValidator.reset().parameter("username").value(this.username).notBlank();
        baseDataValidator.reset().parameter("firstname").value(this.firstname).notBlank();
        baseDataValidator.reset().parameter("lastname").value(this.lastname).notBlank();
        baseDataValidator.reset().parameter("email").value(this.email).notBlank();
        baseDataValidator.reset().parameter("officeId").value(this.officeId).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("roles").value(this.roles).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }        
    }

    public void validateForUpdate() {
        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");

        baseDataValidator.reset().parameter("id").value(this.id).notNull();
        baseDataValidator.reset().parameter("username").value(this.username).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("firstname").value(this.firstname).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("lastname").value(this.lastname).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("email").value(this.email).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("officeId").value(this.officeId).ignoreIfNull().notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("roles").value(this.roles).ignoreIfNull().arrayNotEmpty();

        baseDataValidator.reset().parameter("password").value(this.password).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("passwordRepeat").value(this.repeatPassword).ignoreIfNull().notBlank();
        baseDataValidator.reset().parameter("password").value(this.password).ignoreIfNull()
                .equalToParameter("repeatPassword", this.repeatPassword);

        baseDataValidator.reset().anyOfNotNull(this.username, this.firstname, this.lastname, this.email, this.officeId,
                this.roles, this.password, this.repeatPassword);

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}