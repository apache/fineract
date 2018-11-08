/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.group.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
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
import javax.persistence.Transient;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.exception.ClientExistInGroupException;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.group.exception.GroupExistsInCenterException;
import org.apache.fineract.portfolio.group.exception.GroupNotExistsInCenterException;
import org.apache.fineract.portfolio.group.exception.InvalidGroupStateTransitionException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_group")
public final class Group extends AbstractPersistableCustom<Long> {

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
    private List<Group> groupMembers = new LinkedList<>();

    @ManyToMany
    @JoinTable(name = "m_group_client", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "client_id"))
    private Set<Client> clientMembers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closure_reason_cv_id", nullable = true)
    private CodeValue closureReason;

    @Column(name = "closedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date closureDate;

    @ManyToOne(optional = true, fetch=FetchType.LAZY)
    @JoinColumn(name = "closedon_userid", nullable = true)
    private AppUser closedBy;

    @Column(name = "submittedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date submittedOnDate;

    @ManyToOne(optional = true, fetch=FetchType.LAZY)
    @JoinColumn(name = "submittedon_userid", nullable = true)
    private AppUser submittedBy;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "center", orphanRemoval = true)
    private Set<StaffAssignmentHistory> staffHistory;
    
    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;
    
    @Transient
    private boolean accountNumberRequiresAutoGeneration = false;

    @OneToMany(mappedBy="group",cascade = CascadeType.REMOVE)
    private Set<GroupRole> groupRole;


    // JPA default constructor for entity
    protected Group() {
        this.name = null;
        this.externalId = null;
        this.clientMembers = new HashSet<>();
    }

    public static Group newGroup(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel,
            final String name, final String externalId, final boolean active, final LocalDate activationDate,
            final Set<Client> clientMembers, final Set<Group> groupMembers, final LocalDate submittedOnDate, final AppUser currentUser,
            final String accountNo) {

        // By default new group is created in PENDING status, unless explicitly
        // status is set to active
        GroupingTypeStatus status = GroupingTypeStatus.PENDING;
        LocalDate groupActivationDate = null;
        if (active) {
            status = GroupingTypeStatus.ACTIVE;
            groupActivationDate = activationDate;
        }
        
        return new Group(office, staff, parent, groupLevel, name, externalId, status, groupActivationDate, clientMembers, groupMembers,
                submittedOnDate, currentUser, accountNo);
    }

    private Group(final Office office, final Staff staff, final Group parent, final GroupLevel groupLevel, final String name,
            final String externalId, final GroupingTypeStatus status, final LocalDate activationDate, final Set<Client> clientMembers,
            final Set<Group> groupMembers, final LocalDate submittedOnDate, final AppUser currentUser, final String accountNo) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        this.office = office;
        this.staff = staff;
        this.groupLevel = groupLevel;
        this.parent = parent;

        if (parent != null) {
            this.parent.addChild(this);
        }
        
        if (StringUtils.isBlank(accountNo)) {
            this.accountNumber = new RandomPasswordGenerator(19).generate();
            this.accountNumberRequiresAutoGeneration = true;
        } else {
            this.accountNumber = accountNo;
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

        if (groupMembers != null) {
            this.groupMembers.addAll(groupMembers);
        }

        this.submittedOnDate = submittedOnDate.toDate();
        this.submittedBy = currentUser;
        this.staffHistory = null;

        associateClients(clientMembers);

        /*
         * Always keep status change at the bottom, as status change rule
         * depends on the attribute's value
         */

        setStatus(activationDate, currentUser, status, dataValidationErrors);

        throwExceptionIfErrors(dataValidationErrors);
    }

    private void setStatus(final LocalDate activationDate, final AppUser loginUser, final GroupingTypeStatus status,
            final List<ApiParameterError> dataValidationErrors) {

        if (status.isActive()) {
            activate(loginUser, activationDate, dataValidationErrors);
        } else {
            this.status = status.getValue();
        }

    }

    private void activate(final AppUser currentUser, final LocalDate activationLocalDate, final List<ApiParameterError> dataValidationErrors) {

        validateStatusNotEqualToActiveAndLogError(dataValidationErrors);
        if (dataValidationErrors.isEmpty()) {
            this.status = GroupingTypeStatus.ACTIVE.getValue();
            setActivationDate(activationLocalDate.toDate(), currentUser, dataValidationErrors);
        }

    }

    public void activate(final AppUser currentUser, final LocalDate activationLocalDate) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        activate(currentUser, activationLocalDate, dataValidationErrors);
        if (this.isCenter() && this.hasStaff()) {
            Staff staff = this.getStaff();
            this.reassignStaff(staff, activationLocalDate);
        }
        throwExceptionIfErrors(dataValidationErrors);

    }

    private void setActivationDate(final Date activationDate, final AppUser loginUser, final List<ApiParameterError> dataValidationErrors) {

        if (activationDate != null) {
            this.activationDate = activationDate;
            this.activatedBy = loginUser;
        }

        validateActivationDate(dataValidationErrors);

    }

    public boolean isActivatedAfter(final LocalDate submittedOn) {
        return getActivationLocalDate().isAfter(submittedOn);
    }

    public boolean isNotActive() {
        return !isActive();
    }

    public boolean isActive() {
        return this.status != null ? GroupingTypeStatus.fromInt(this.status).isActive() : false;
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
        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

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
			if (newValue != null) {
				this.activationDate = newValue.toDate();
			}
        }
        
        if (command.isChangeInStringParameterNamed(GroupingTypesApiConstants.accountNoParamName, this.accountNumber)) {
            final String newValue = command.stringValueOfParameterNamed(GroupingTypesApiConstants.accountNoParamName);
            actualChanges.put(GroupingTypesApiConstants.accountNoParamName, newValue);
            this.accountNumber = StringUtils.defaultIfEmpty(newValue, null);
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

    public List<String> associateClients(final Set<Client> clientMembersSet) {
        final List<String> differences = new ArrayList<>();
        for (final Client client : clientMembersSet) {
            if (hasClientAsMember(client)) { throw new ClientExistInGroupException(client.getId(), getId()); }
            this.clientMembers.add(client);
            differences.add(client.getId().toString());
        }

        return differences;
    }

    public List<String> disassociateClients(final Set<Client> clientMembersSet) {
        final List<String> differences = new ArrayList<>();
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

    public boolean hasClientAsMember(final Client client) {
        return this.clientMembers.contains(client);
    }

	public void generateHierarchy() {
		if (this.parent != null) {
			this.hierarchy = this.parent.hierarchyOf(getId());
		} else {
			this.hierarchy = "." + getId() + ".";
			for (Group group : this.groupMembers) {
				group.setParent(this);
				group.generateHierarchy();
			}
		}
	}
    
	public void resetHierarchy() {
			this.hierarchy = "." + this.getId();
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
        if (this.isCenter() && this.isActive()) {
            LocalDate updatedDate = DateUtils.getLocalDateOfTenant();
            reassignStaff(staff, updatedDate);
        }
        this.staff = staff;
    }

    public void unassignStaff() {
        if (this.isCenter() && this.isActive()) {
            LocalDate dateOfStaffUnassigned = DateUtils.getLocalDateOfTenant();
            removeStaff(dateOfStaffUnassigned);
        }
        this.staff = null;
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

    public boolean isGroup() {
        return this.groupLevel.isGroup();
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

    public boolean hasGroupAsMember(final Group group) {
        return this.groupMembers.contains(group);
    }

    public boolean hasStaff() {
        if (this.staff != null) { return true; }
        return false;
    }

    public List<String> associateGroups(final Set<Group> groupMembersSet) {

        final List<String> differences = new ArrayList<>();
        for (final Group group : groupMembersSet) {

            if (group.isCenter()) {
                final String defaultUserMessage = "Center can not assigned as a child";
                throw new GeneralPlatformDomainRuleException("error.msg.center.cannot.be.assigned.as.child", defaultUserMessage,
                        group.getId());
            }

            if (hasGroupAsMember(group)) { throw new GroupExistsInCenterException(getId(), group.getId()); }

            if (group.isChildGroup()) {
                final String defaultUserMessage = "Group is already associated with a center";
                throw new GeneralPlatformDomainRuleException("error.msg.group.already.associated.with.center", defaultUserMessage, group
                        .getParent().getId(), group.getId());
            }

            this.groupMembers.add(group);
            differences.add(group.getId().toString());
            group.setParent(this);
    		group.generateHierarchy();
        }

        return differences;
    }

    public List<String> disassociateGroups(Set<Group> groupMembersSet) {

        final List<String> differences = new ArrayList<>();
        for (final Group group : groupMembersSet) {
            if (hasGroupAsMember(group)) {
                this.groupMembers.remove(group);
                differences.add(group.getId().toString());
    			group.resetHierarchy();
            } else {
                throw new GroupNotExistsInCenterException(group.getId(), getId());
            }
        }

        return differences;
    }

    public Boolean isGroupsClientCountWithinMinMaxRange(Integer minClients, Integer maxClients) {

        if (maxClients == null && minClients == null) { return true; }

        // set minClients or maxClients to 0 if null

        if (minClients == null) {
            minClients = 0;
        }

        if (maxClients == null) {
            maxClients = Integer.MAX_VALUE;
        }

        Set<Client> activeClientMembers = getActiveClientMembers();

        if (activeClientMembers.size() >= minClients && activeClientMembers.size() <= maxClients) { return true; }
        return false;
    }

    public Boolean isGroupsClientCountWithinMaxRange(Integer maxClients) {
        Set<Client> activeClientMembers = getActiveClientMembers();
        if (maxClients == null) {
            return true;
        } else if (activeClientMembers.size() <= maxClients) {
            return true;
        } else {
            return false;
        }
    }

    public Set<Client> getActiveClientMembers() {
        Set<Client> activeClientMembers = new HashSet<>();
        for (Client client : this.clientMembers) {
            if (client.isActive()) {
                activeClientMembers.add(client);
            }
        }
        return activeClientMembers;
    }

    private void validateActivationDate(final List<ApiParameterError> dataValidationErrors) {

        if (getSubmittedOnDate() != null && isDateInTheFuture(getSubmittedOnDate())) {

            final String defaultUserMessage = "Submitted on date cannot be in the future.";
            final String globalisationMessageCode = "error.msg.group.submittedOnDate.in.the.future";
            final ApiParameterError error = ApiParameterError.parameterError(globalisationMessageCode, defaultUserMessage,
                    GroupingTypesApiConstants.submittedOnDateParamName, this.submittedOnDate);

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

    private void validateStatusNotEqualToActiveAndLogError(final List<ApiParameterError> dataValidationErrors) {

        if (isActive()) {
            final String defaultUserMessage = "Cannot activate group. Group is already active.";
            final String globalisationMessageCode = "error.msg.group.already.active";
            final ApiParameterError error = ApiParameterError.parameterError(globalisationMessageCode, defaultUserMessage,
                    GroupingTypesApiConstants.activeParamName, true);
            dataValidationErrors.add(error);
        }
    }

    private void throwExceptionIfErrors(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }

    public Set<Client> getClientMembers() {
        return this.clientMembers;
    }

    // StaffAssignmentHistory[during center creation]
    public void captureStaffHistoryDuringCenterCreation(final Staff newStaff, final LocalDate assignmentDate) {
        if (this.isCenter() && this.isActive() && staff != null) {
            this.staff = newStaff;
            final StaffAssignmentHistory staffAssignmentHistory = StaffAssignmentHistory.createNew(this, this.staff, assignmentDate);
            if (staffAssignmentHistory != null) {
                staffHistory = new HashSet<>();
                this.staffHistory.add(staffAssignmentHistory);
            }
        }
    }

    // StaffAssignmentHistory[assign staff]
    public void reassignStaff(final Staff newStaff, final LocalDate assignmentDate) {
        this.staff = newStaff;
        final StaffAssignmentHistory staffAssignmentHistory = StaffAssignmentHistory.createNew(this, this.staff, assignmentDate);
        this.staffHistory.add(staffAssignmentHistory);
    }

    // StaffAssignmentHistory[unassign staff]
    public void removeStaff(final LocalDate unassignDate) {
        final StaffAssignmentHistory latestHistoryRecord = findLatestIncompleteHistoryRecord();
        if (latestHistoryRecord != null) {
            latestHistoryRecord.updateEndDate(unassignDate);
        }
    }

    private StaffAssignmentHistory findLatestIncompleteHistoryRecord() {

        StaffAssignmentHistory latestRecordWithNoEndDate = null;
        for (final StaffAssignmentHistory historyRecord : this.staffHistory) {
            if (historyRecord.isCurrentRecord()) {
                latestRecordWithNoEndDate = historyRecord;
                break;
            }
        }
        return latestRecordWithNoEndDate;
    }
    
    public boolean isAccountNumberRequiresAutoGeneration() {
        return this.accountNumberRequiresAutoGeneration;
    }

    public void setAccountNumberRequiresAutoGeneration(final boolean accountNumberRequiresAutoGeneration) {
        this.accountNumberRequiresAutoGeneration = accountNumberRequiresAutoGeneration;
    }
    
    public void updateAccountNo(final String accountIdentifier) {
        this.accountNumber = accountIdentifier;
        this.accountNumberRequiresAutoGeneration = false;
    }

	public void setGroupMembers(List<Group> groupMembers) {
		this.groupMembers = groupMembers;
	}
    
    
}