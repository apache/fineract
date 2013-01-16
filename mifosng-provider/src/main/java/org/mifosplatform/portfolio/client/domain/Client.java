package org.mifosplatform.portfolio.client.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.office.domain.Office;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_client", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "account_no_UNIQUE")})
public class Client extends AbstractPersistable<Long> {

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "firstname", length = 50)
    private String firstname;

    @Column(name = "middlename", length = 50)
    private String middlename;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "fullname", length = 100)
    private String fullname;

    @SuppressWarnings("unused")
    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "joined_date")
    @Temporal(TemporalType.DATE)
    private Date joinedDate;

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "image_key", length = 500)
    private String imageKey;

    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;

    public static Client fromJson(final Office clientOffice, final JsonCommand command) {

        final String accountNo = command.stringValueOfParameterNamed("accountNo");
        final String firstname = command.stringValueOfParameterNamed("firstname");
        final String middlename = command.stringValueOfParameterNamed("middlename");
        final String lastname = command.stringValueOfParameterNamed("lastname");
        final String fullname = command.stringValueOfParameterNamed("fullname");
        final LocalDate joiningDate = command.localDateValueOfParameterNamed("joinedDate");
        final String externalId = command.stringValueOfParameterNamed("externalId");

        return new Client(clientOffice, accountNo, firstname, middlename, lastname, fullname, joiningDate, externalId);
    }

    protected Client() {
        //
    }

    private Client(final Office office, final String accountNo, final String firstname, final String middlename, final String lastname,
            final String fullname, final LocalDate openingDate, final String externalId) {
        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }
        this.office = office;
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        this.joinedDate = openingDate.toDateMidnight().toDate();
        if (StringUtils.isNotBlank(firstname)) {
            this.firstname = firstname.trim();
        } else {
            this.firstname = null;
        }

        if (StringUtils.isNotBlank(middlename)) {
            this.middlename = middlename.trim();
        } else {
            this.middlename = null;
        }

        if (StringUtils.isNotBlank(lastname)) {
            this.lastname = lastname.trim();
        } else {
            this.lastname = null;
        }

        if (StringUtils.isNotBlank(fullname)) {
            this.fullname = fullname.trim();
        } else {
            this.fullname = null;
        }

        deriveDisplayName();
        validateNameParts();
    }

    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }

    public void setAccountNumberRequiresAutoGeneration(boolean accountNumberRequiresAutoGeneration) {
        this.accountNumberRequiresAutoGeneration = accountNumberRequiresAutoGeneration;
    }

    public boolean identifiedBy(final String identifier) {
        return identifier.equalsIgnoreCase(this.externalId);
    }

    public boolean identifiedBy(Long clientId) {
        return getId().equals(clientId);
    }

    public void changeOffice(final Office newOffice) {
        this.office = newOffice;
    }

    public void updateAccountNo(final String accountIdentifier) {
        this.accountNumber = accountIdentifier;
        this.accountNumberRequiresAutoGeneration = false;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(9);

        final String accountNoParamName = "accountNo";
        if (command.isChangeInStringParameterNamed(accountNoParamName, this.accountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(accountNoParamName);
            actualChanges.put(accountNoParamName, newValue);
            this.accountNumber = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String officeIdParamName = "officeId";
        if (command.isChangeInLongParameterNamed(officeIdParamName, this.office.getId())) {
            final Long newValue = command.longValueOfParameterNamed(officeIdParamName);
            actualChanges.put(officeIdParamName, newValue);
        }

        final String firstnameParamName = "firstname";
        if (command.isChangeInStringParameterNamed(firstnameParamName, this.firstname)) {
            final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
            actualChanges.put(firstnameParamName, newValue);
            this.firstname = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String middlenameParamName = "middlename";
        if (command.isChangeInStringParameterNamed(middlenameParamName, this.middlename)) {
            final String newValue = command.stringValueOfParameterNamed(middlenameParamName);
            actualChanges.put(middlenameParamName, newValue);
            this.middlename = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String lastnameParamName = "lastname";
        if (command.isChangeInStringParameterNamed(lastnameParamName, this.lastname)) {
            final String newValue = command.stringValueOfParameterNamed(lastnameParamName);
            actualChanges.put(lastnameParamName, newValue);
            this.lastname = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String fullnameParamName = "fullname";
        if (command.isChangeInStringParameterNamed(fullnameParamName, this.fullname)) {
            final String newValue = command.stringValueOfParameterNamed(fullnameParamName);
            actualChanges.put(fullnameParamName, newValue);
            this.fullname = newValue;
        }

        validateNameParts();

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String joiningDateParamName = "joinedDate";
        if (command.isChangeInLocalDateParameterNamed(joiningDateParamName, getJoiningLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(joiningDateParamName);
            actualChanges.put(joiningDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(joiningDateParamName);
            this.joinedDate = newValue.toDate();
        }

        deriveDisplayName();

        return actualChanges;
    }

    private void validateNameParts() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("client");

        if (StringUtils.isNotBlank(this.fullname)) {

            baseDataValidator.reset().parameter("firstname").value(this.firstname)
                    .mustBeBlankWhenParameterProvided("fullname", this.fullname);

            baseDataValidator.reset().parameter("middlename").value(this.middlename)
                    .mustBeBlankWhenParameterProvided("fullname", this.fullname);

            baseDataValidator.reset().parameter("lastname").value(this.lastname)
                    .mustBeBlankWhenParameterProvided("fullname", this.fullname);
        }

        if (StringUtils.isBlank(this.fullname)) {
            baseDataValidator.reset().parameter("firstname").value(this.firstname).notBlank().notExceedingLengthOf(50);
            baseDataValidator.reset().parameter("middlename").value(this.middlename).ignoreIfNull().notExceedingLengthOf(50);
            baseDataValidator.reset().parameter("lastname").value(this.lastname).notBlank().notExceedingLengthOf(50);
        }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    private void deriveDisplayName() {

        StringBuilder nameBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(this.firstname)) {
            nameBuilder.append(this.firstname).append(' ');
        }

        if (StringUtils.isNotBlank(this.middlename)) {
            nameBuilder.append(this.middlename).append(' ');
        }

        if (StringUtils.isNotBlank(this.lastname)) {
            nameBuilder.append(this.lastname);
        }

        if (StringUtils.isNotBlank(this.fullname)) {
            nameBuilder = new StringBuilder(this.fullname);
        }

        this.displayName = nameBuilder.toString();
    }

    private LocalDate getJoiningLocalDate() {
        LocalDate joiningLocalDate = null;
        if (this.joinedDate != null) {
            joiningLocalDate = LocalDate.fromDateFields(this.joinedDate);
        }
        return joiningLocalDate;
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on client so it wont appear
     * in query/report results.
     * 
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.externalId = this.getId() + "_" + this.externalId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isOfficeIdentifiedBy(final Long officeId) {
        return this.office.identifiedBy(officeId);
    }

    public Office getOffice() {
        return office;
    }

    public String getFirstName() {
        return firstname;
    }

    public String getLastName() {
        return lastname;
    }

    public Date getJoiningDate() {
        return joinedDate;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }
}