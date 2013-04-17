/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.group.domain.Group;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_client", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "account_no_UNIQUE") })
public final class Client extends AbstractPersistable<Long> {

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    /**
     * A value from {@link ClientStatus}.
     */
    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "firstname", length = 50)
    private String firstname;

    @Column(name = "middlename", length = 50)
    private String middlename;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "fullname", length = 100)
    private String fullname;

    @SuppressWarnings("unused")
    @Column(name = "display_name", length = 100, nullable = false)
    private String displayName;

    @Column(name = "activation_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date activationDate;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "image_key", length = 500, nullable = true)
    private String imageKey;

    @ManyToMany
    @JoinTable(name = "m_group_client", joinColumns = @JoinColumn(name = "client_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups;

    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;

    public static Client createNew(final Office clientOffice, final Group clientParentGroup, final JsonCommand command,
            final boolean isPendingStateAllowed) {

        final String accountNo = command.stringValueOfParameterNamed(ClientApiConstants.accountNoParamName);
        final String externalId = command.stringValueOfParameterNamed(ClientApiConstants.externalIdParamName);

        final String firstname = command.stringValueOfParameterNamed(ClientApiConstants.firstnameParamName);
        final String middlename = command.stringValueOfParameterNamed(ClientApiConstants.middlenameParamName);
        final String lastname = command.stringValueOfParameterNamed(ClientApiConstants.lastnameParamName);
        final String fullname = command.stringValueOfParameterNamed(ClientApiConstants.fullnameParamName);

        ClientStatus status = ClientStatus.ACTIVE;
        boolean active = true;
        if (isPendingStateAllowed && command.hasParameter("active")) {
            active = command.booleanPrimitiveValueOfParameterNamed(ClientApiConstants.activeParamName);
        }

        LocalDate activationDate = null;
        if (active) {
            activationDate = command.localDateValueOfParameterNamed(ClientApiConstants.activationDateParamName);
        } else {
            status = ClientStatus.PENDING;
        }

        return new Client(status, clientOffice, clientParentGroup, accountNo, firstname, middlename, lastname, fullname, activationDate,
                externalId);
    }

    protected Client() {
        //
    }

    private Client(final ClientStatus status, final Office office, final Group clientParentGroup, final String accountNo,
            final String firstname, final String middlename, final String lastname, final String fullname, final LocalDate activationDate,
            final String externalId) {
        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
        }
        this.status = status.getValue();
        this.office = office;
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        if (activationDate != null) {
            this.activationDate = activationDate.toDateMidnight().toDate();
        }
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

        if (clientParentGroup != null) {
            this.groups = new HashSet<Group>();
            this.groups.add(clientParentGroup);
        }

        deriveDisplayName();
        validateNameParts();
    }

    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }

    public void setAccountNumberRequiresAutoGeneration(final boolean accountNumberRequiresAutoGeneration) {
        this.accountNumberRequiresAutoGeneration = accountNumberRequiresAutoGeneration;
    }

    public boolean identifiedBy(final String identifier) {
        return identifier.equalsIgnoreCase(this.externalId);
    }

    public boolean identifiedBy(final Long clientId) {
        return getId().equals(clientId);
    }

    public void changeOffice(final Office newOffice) {
        this.office = newOffice;
    }

    public void updateAccountNo(final String accountIdentifier) {
        this.accountNumber = accountIdentifier;
        this.accountNumberRequiresAutoGeneration = false;
    }

    public void activate(final DateTimeFormatter formatter, final LocalDate activationLocalDate) {
        if (isActive()) {
            final String defaultUserMessage = "Cannot activate client. Client is already active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.clients.already.active", defaultUserMessage,
                    "activationDate", activationLocalDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        if (isDateInTheFuture(activationLocalDate)) {

            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.clients.activationDate.in.the.future",
                    defaultUserMessage, "activationDate", activationLocalDate);

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        this.activationDate = activationLocalDate.toDate();
        this.status = ClientStatus.ACTIVE.getValue();
    }

    public boolean isNotActive() {
        return !isActive();
    }

    public boolean isActive() {
        return ClientStatus.fromInt(this.status).isActive();
    }

    public boolean isNotPending() {
        return !isPending();
    }

    public boolean isPending() {
        return ClientStatus.fromInt(this.status).isPending();
    }

    private boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(DateUtils.getLocalDateOfTenant());
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(9);

        final String statusEnumParamName = "status";
        if (command.isChangeInIntegerParameterNamed(statusEnumParamName, this.status)) {
            final Integer newValue = command.integerValueOfParameterNamed(statusEnumParamName);
            actualChanges.put(statusEnumParamName, ClientEnumerations.status(newValue));
            this.status = ClientStatus.fromInt(newValue).getValue();
        }

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
            this.activationDate = newValue.toDate();
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
        if (this.activationDate != null) {
            joiningLocalDate = LocalDate.fromDateFields(this.activationDate);
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
        return activationDate;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(final String imageKey) {
        this.imageKey = imageKey;
    }

    public Long officeId() {
        return this.office.getId();
    }
}