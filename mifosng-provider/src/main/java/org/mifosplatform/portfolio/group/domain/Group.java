/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.group.api.GroupingTypesApiConstants;
import org.mifosplatform.portfolio.group.exception.ClientExistInGroupException;
import org.mifosplatform.portfolio.group.exception.ClientNotInGroupException;
import org.mifosplatform.portfolio.group.exception.InvalidGroupStateTransitionException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.common.collect.Sets;

@Entity
@Table(name = "m_group")
public final class Group extends AbstractPersistable<Long> {

    @Column(name = "external_id", length = 100, unique = true)
    private String externalId;

    /**
     * A value from {@link GroupingTypeStatus}.
     */
    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "activation_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date activationDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "activatedon_userid", nullable = true)
    private AppUser activatedBy;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Group parent;

    @ManyToOne
    @JoinColumn(name = "level_id", nullable = false)
    private GroupLevel groupLevel;

    @Column(name = "display_name", length = 100, unique = true)
    private String name;

    @Column(name = "hierarchy", length = 100)
    private String hierarchy;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private final List<Group> groupMembers = new LinkedList<Group>();

    @ManyToMany
    @JoinTable(name = "m_group_client", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "client_id"))
    private Set<Client> clientMembers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closure_reason_cv_id", nullable = true)
    private CodeValue closureReason;

    @Column(name = "closedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date closureDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "closedon_userid", nullable = true)
    private AppUser closedBy;

    @Column(name = "submittedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date submittedOnDate;

    @ManyToOne(optional = true)
    @JoinColumn(name = "submittedon_userid", nullable = true)
    private AppUser submittedBy;

    public Group() {
        this.name = null;
        this.externalId = null;
        this.clientMembers = new HashSet<Client>();
    }

    public static Group newGroup(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel,
            final String name, final String externalId, final boolean active, final LocalDate activationDate,
            final Set<Client> clientMembers, final Set<Group> groupMembers, final LocalDate submittedOnDate, final AppUser currentUser) {

        GroupingTypeStatus status = GroupingTypeStatus.PENDING;
        LocalDate groupActivationDate = null;
        if (active) {
            status = GroupingTypeStatus.ACTIVE;
            groupActivationDate = activationDate;
        }

        return new Group(office, staff, parent, groupLevel, name, externalId, status, groupActivationDate, clientMembers, groupMembers,
                submittedOnDate, currentUser);
    }

    private Group(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel, final String name,
            final String externalId, final GroupingTypeStatus status, final LocalDate activationDate, final Set<Client> clientMembers,
            final Set<Group> groupMembers, final LocalDate submittedOnDate, final AppUser currentUser) {
        this.office = office;
        this.staff = staff;
        this.groupLevel = groupLevel;
        this.parent = parent;

        if (parent != null) {
            this.parent.addChild(this);
        }

        this.status = status.getValue();
        if (activationDate != null) {
            this.activationDate = activationDate.toDate();
            this.activatedBy = currentUser;
        }

        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        if (clientMembers != null) {
            this.clientMembers = clientMembers;
        }
        if (groupMembers != null) {
            this.groupMembers.addAll(groupMembers);
        }

        this.submittedOnDate = submittedOnDate.toDate();
        this.submittedBy = currentUser;

        validate();
    }

    private void validate() {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        validateActivationDate(dataValidationErrors);
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    private void validateActivationDate(final List<ApiParameterError> dataValidationErrors) {

        if (getSubmittedOnDate() != null && isDateInTheFuture(getSubmittedOnDate())) {

            final String defaultUserMessage = "Submitted on date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.group.submittedOnDate.in.the.future",
                    defaultUserMessage, ClientApiConstants.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }

        if (getActivationLocalDate() != null && getSubmittedOnDate() != null && getSubmittedOnDate().isAfter(getActivationLocalDate())) {

            final String defaultUserMessage = "Submitted on date cannot be after the activation date";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.group.submittedOnDate.after.activation.date",
                    defaultUserMessage, GroupingTypesApiConstants.submittedOnDateParamName, this.submittedOnDate);

            dataValidationErrors.add(error);
        }

        if (getActivationLocalDate() != null && isDateInTheFuture(getActivationLocalDate())) {

            final String defaultUserMessage = "Activation date cannot be in the future.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.group.activationDate.in.the.future",
                    defaultUserMessage, GroupingTypesApiConstants.activationDateParamName, getActivationLocalDate());

            dataValidationErrors.add(error);
        }

        if (getActivationLocalDate() != null) {
            if (this.office.isOpeningDateAfter(getActivationLocalDate())) {
                final String defaultUserMessage = "Activation date cannot be a date before the office opening date.";
                final ApiParameterError error = ApiParameterError.parameterError(
                        "error.msg.group.activationDate.cannot.be.before.office.activation.date", defaultUserMessage,
                        GroupingTypesApiConstants.activationDateParamName, getActivationLocalDate());
                dataValidationErrors.add(error);
            }
        }
    }

    public void activate(final AppUser currentUser, final DateTimeFormatter formatter, final LocalDate activationLocalDate) {
        if (isActive()) {
            final String defaultUserMessage = "Cannot activate group. Group is already active.";
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.group.already.active", defaultUserMessage,
                    "activationDate", activationLocalDate.toString(formatter));

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        this.activationDate = activationLocalDate.toDate();
        this.status = GroupingTypeStatus.ACTIVE.getValue();
        this.activatedBy = currentUser;

        validate();
    }

    public boolean isActivatedAfter(final LocalDate submittedOn) {
        return getActivationLocalDate().isAfter(submittedOn);
    }

    public boolean isNotActive() {
        return !isActive();
    }

    public boolean isActive() {
        return GroupingTypeStatus.fromInt(this.status).isActive();
    }

    private boolean isDateInTheFuture(final LocalDate localDate) {
        return localDate.isAfter(DateUtils.getLocalDateOfTenant());
    }

    public boolean isNotPending() {
        return !isPending();
    }

    public boolean isPending() {
        return GroupingTypeStatus.fromInt(this.status).isPending();
    }

    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(9);

        if (command.isChangeInIntegerParameterNamed(GroupingTypesApiConstants.statusParamName, this.status)) {
            final Integer newValue = command.integerValueOfParameterNamed(GroupingTypesApiConstants.statusParamName);
            actualChanges.put(GroupingTypesApiConstants.statusParamName, GroupingTypeEnumerations.status(newValue));
            this.status = GroupingTypeStatus.fromInt(newValue).getValue();
        }

        if (command.isChangeInStringParameterNamed(GroupingTypesApiConstants.externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(GroupingTypesApiConstants.externalIdParamName);
            actualChanges.put(GroupingTypesApiConstants.externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInLongParameterNamed(GroupingTypesApiConstants.officeIdParamName, this.office.getId())) {
            final Long newValue = command.longValueOfParameterNamed(GroupingTypesApiConstants.officeIdParamName);
            actualChanges.put(GroupingTypesApiConstants.officeIdParamName, newValue);
        }

        if (command.isChangeInLongParameterNamed(GroupingTypesApiConstants.staffIdParamName, staffId())) {
            final Long newValue = command.longValueOfParameterNamed(GroupingTypesApiConstants.staffIdParamName);
            actualChanges.put(GroupingTypesApiConstants.staffIdParamName, newValue);
        }

        if (command.isChangeInStringParameterNamed(GroupingTypesApiConstants.nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(GroupingTypesApiConstants.nameParamName);
            actualChanges.put(GroupingTypesApiConstants.nameParamName, newValue);
            this.name = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        if (command.isChangeInLocalDateParameterNamed(GroupingTypesApiConstants.activationDateParamName, getActivationLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(GroupingTypesApiConstants.activationDateParamName);
            actualChanges.put(GroupingTypesApiConstants.activationDateParamName, valueAsInput);
            actualChanges.put(GroupingTypesApiConstants.dateFormatParamName, dateFormatAsInput);
            actualChanges.put(GroupingTypesApiConstants.localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(GroupingTypesApiConstants.activationDateParamName);
            this.activationDate = newValue.toDate();
        }

        return actualChanges;
    }

    public LocalDate getSubmittedOnDate() {
        return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(this.submittedOnDate), null);
    }

    public LocalDate getActivationLocalDate() {
        LocalDate activationLocalDate = null;
        if (this.activationDate != null) {
            activationLocalDate = new LocalDate(this.activationDate);
        }
        return activationLocalDate;
    }

    public void addClientMember(final Client member) {
        this.clientMembers.add(member);
    }

    public boolean hasClientAsMember(final Client client) {
        return this.clientMembers.contains(client);
    }

    public void generateHierarchy() {
        if (this.parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = "." + getId() + ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public boolean isOfficeIdentifiedBy(final Long officeId) {
        return this.office.identifiedBy(officeId);
    }

    public Long officeId() {
        return this.office.getId();
    }

    private Long staffId() {
        Long staffId = null;
        if (this.staff != null) {
            staffId = this.staff.getId();
        }
        return staffId;
    }

    private void addChild(final Group group) {
        this.groupMembers.add(group);
    }

    public void updateStaff(final Staff staff) {
        this.staff = staff;
    }

    public void unassignStaff() {
        this.staff = null;
    }

    public List<String> updateClientMembersIfDifferent(final Set<Client> clientMembersSet) {
        List<String> differences = new ArrayList<String>();
        if (!clientMembersSet.equals(this.clientMembers)) {
            final Set<Client> diffClients = Sets.symmetricDifference(clientMembersSet, this.clientMembers);
            final String[] diffClientsIds = getClientIds(diffClients);
            if (diffClientsIds != null) {
                differences = Arrays.asList(diffClientsIds);
            }
            this.clientMembers = clientMembersSet;
        }

        return differences;
    }

    public List<String> associateClients(final Set<Client> clientMembersSet) {
        final List<String> differences = new ArrayList<String>();
        for (final Client client : clientMembersSet) {
            if (hasClientAsMember(client)) { throw new ClientExistInGroupException(client.getId(), getId()); }
            this.clientMembers.add(client);
            differences.add(client.getId().toString());
        }

        return differences;
    }

    public List<String> disassociateClients(final Set<Client> clientMembersSet) {
        final List<String> differences = new ArrayList<String>();
        for (final Client client : clientMembersSet) {
            if (hasClientAsMember(client)) {
                this.clientMembers.remove(client);
                differences.add(client.getId().toString());
            } else {
                throw new ClientNotInGroupException(client.getId(), getId());
            }
        }

        return differences;
    }

    private String[] getClientIds(final Set<Client> clients) {
        final String[] clientIds = new String[clients.size()];
        final Iterator<Client> it = clients.iterator();
        for (int i = 0; it.hasNext(); i++) {
            clientIds[i] = it.next().getId().toString();
        }
        return clientIds;
    }

    public GroupLevel getGroupLevel() {
        return this.groupLevel;
    }

    public Staff getStaff() {
        return this.staff;
    }

    public void setStaff(final Staff staff) {
        this.staff = staff;
    }

    public Group getParent() {
        return this.parent;
    }

    public void setParent(final Group parent) {
        this.parent = parent;
    }

    public Office getOffice() {
        return this.office;
    }

    public boolean isCenter() {
        return this.groupLevel.isCenter();
    }

    public boolean isTransferInProgress() {
        return GroupingTypeStatus.fromInt(this.status).isTransferInProgress();
    }

    public boolean isTransferOnHold() {
        return GroupingTypeStatus.fromInt(this.status).isTransferOnHold();
    }

    public boolean isTransferInProgressOrOnHold() {
        return isTransferInProgress() || isTransferOnHold();
    }

    public boolean isChildClient(final Long clientId) {
        if (clientId != null && this.clientMembers != null && !this.clientMembers.isEmpty()) {
            for (final Client client : this.clientMembers) {
                if (client.getId().equals(clientId)) { return true; }
            }
        }
        return false;
    }

    public boolean isChildGroup() {
        return this.parent == null ? false : true;

    }

    public boolean isClosed() {
        return GroupingTypeStatus.fromInt(this.status).isClosed();
    }

    public void close(final AppUser currentUser, final CodeValue closureReason, final LocalDate closureDate) {

        if (isClosed()) {
            final String errorMessage = "Group with identifier " + getId() + " is alread closed.";
            throw new InvalidGroupStateTransitionException(this.groupLevel.getLevelName(), "close", "already.closed", errorMessage, getId());
        }

        if (isNotPending() && getActivationLocalDate().isAfter(closureDate)) {
            final String errorMessage = "The Group closure Date " + closureDate + " cannot be before the group Activation Date "
                    + getActivationLocalDate() + ".";
            throw new InvalidGroupStateTransitionException(this.groupLevel.getLevelName(), "close",
                    "date.cannot.before.group.actvation.date", errorMessage, closureDate, getActivationLocalDate());
        }

        this.closureReason = closureReason;
        this.closureDate = closureDate.toDate();
        this.status = GroupingTypeStatus.CLOSED.getValue();
        this.closedBy = currentUser;
    }

    public boolean hasActiveClients() {
        for (final Client client : this.clientMembers) {
            if (!client.isClosed()) { return true; }
        }
        return false;
    }

    public boolean hasActiveGroups() {
        for (final Group group : this.groupMembers) {
            if (!group.isClosed()) { return true; }
        }
        return false;
    }
}