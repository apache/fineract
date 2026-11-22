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
package org.apache.fineract.portfolio.group.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.group.CentersCreateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.group.GroupsCreateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.office.exception.InvalidOfficeException;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.service.LoanStatusMapper;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.data.CenterAssociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterCreateRequest;
import org.apache.fineract.portfolio.group.data.CenterDisassociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterUpdateRequest;
import org.apache.fineract.portfolio.group.data.DatatableEntry;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupActivateResponse;
import org.apache.fineract.portfolio.group.data.GroupAssignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupAssignStaffResponse;
import org.apache.fineract.portfolio.group.data.GroupAssociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupCloseRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
import org.apache.fineract.portfolio.group.data.GroupDeleteRequest;
import org.apache.fineract.portfolio.group.data.GroupDeleteResponse;
import org.apache.fineract.portfolio.group.data.GroupDisassociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffResponse;
import org.apache.fineract.portfolio.group.data.GroupUpdateRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateResponse;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupLevel;
import org.apache.fineract.portfolio.group.domain.GroupLevelRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.apache.fineract.portfolio.group.exception.GroupAccountExistsException;
import org.apache.fineract.portfolio.group.exception.GroupHasNoStaffException;
import org.apache.fineract.portfolio.group.exception.GroupLevelNotFoundException;
import org.apache.fineract.portfolio.group.exception.GroupMemberCountNotInPermissibleRangeException;
import org.apache.fineract.portfolio.group.exception.GroupMustBePendingToBeDeletedException;
import org.apache.fineract.portfolio.group.exception.InvalidGroupLevelException;
import org.apache.fineract.portfolio.group.exception.InvalidGroupStateTransitionException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanOfficerService;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
public class GroupingTypesWritePlatformServiceJpaRepositoryImpl implements GroupingTypesWritePlatformService {

    private final PlatformSecurityContext context;
    private final GroupRepositoryWrapper groupRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final StaffRepositoryWrapper staffRepository;
    private final NoteRepository noteRepository;
    private final GroupLevelRepository groupLevelRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CommandProcessingService commandProcessingService;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanOfficerService loanOfficerService;
    private final ExternalIdFactory externalIdFactory;

    @Transactional
    @Override
    public GroupActivateResponse activateGroup(final GroupActivateRequest request) {
        final AppUser currentUser = this.context.authenticatedUser();
        final Group group = this.groupRepository.findOneWithNotFoundDetection(request.getGroupId());

        if (group.isGroup()) {
            validateGroupRulesBeforeActivation(group);
        }

        final Locale locale = request.getLocale() == null ? null : JsonParserHelper.localeFromString(request.getLocale());
        final LocalDate activationDate = DateUtils.parseLocalDate(request.getActivationDate(), request.getDateFormat(), locale);
        validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(group.getOffice(), group.getGroupLevel(), activationDate);

        group.activate(currentUser, activationDate);

        return GroupActivateResponse.builder().resourceId(group.getId()).officeId(group.officeId()).groupId(group.getId()).build();
    }

    @Transactional
    @Override
    public GroupAssignStaffResponse assignGroupStaff(final GroupAssignStaffRequest request) {
        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        final Group groupForUpdate = this.groupRepository.findOneWithNotFoundDetection(request.getGroupId());

        final Staff staff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(request.getStaffId(),
                groupForUpdate.getOffice().getHierarchy());
        groupForUpdate.updateStaff(staff);

        if (Boolean.TRUE.equals(request.getInheritStaffForClientAccounts())) {
            LocalDate loanOfficerReassignmentDate = DateUtils.getBusinessLocalDate();
            Set<Client> clients = groupForUpdate.getClientMembers();
            if (clients != null) {
                for (Client client : clients) {
                    client.updateStaff(staff);
                    if (this.loanRepositoryWrapper.doNonClosedLoanAccountsExistForClient(client.getId())) {
                        for (final Loan loan : this.loanRepositoryWrapper.findLoanByClientId(client.getId())) {
                            if (loan.isDisbursed() && !loan.isClosed()) {
                                loanOfficerService.reassignLoanOfficer(loan, staff, loanOfficerReassignmentDate);
                            }
                        }
                    }
                    if (this.savingsAccountRepositoryWrapper.doNonClosedSavingAccountsExistForClient(client.getId())) {
                        for (final SavingsAccount savingsAccount : this.savingsAccountRepositoryWrapper
                                .findSavingAccountByClientId(client.getId())) {
                            if (!savingsAccount.isClosed()) {
                                savingsAccount.reassignSavingsOfficer(staff, loanOfficerReassignmentDate);
                            }
                        }
                    }
                }
            }
        }
        this.groupRepository.saveAndFlush(groupForUpdate);

        actualChanges.put(GroupingTypesApiConstants.staffIdParamName, request.getStaffId());

        return GroupAssignStaffResponse.builder().resourceId(groupForUpdate.getId()).officeId(groupForUpdate.officeId())
                .groupId(request.getGroupId()).changes(actualChanges).build();
    }

    @Transactional
    @Override
    public GroupUnassignStaffResponse unassignGroupStaff(final GroupUnassignStaffRequest request) {
        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(9);

        final Group groupForUpdate = this.groupRepository.findOneWithNotFoundDetection(request.getGroupId());
        final Staff presentStaff = groupForUpdate.getStaff();
        if (presentStaff == null) {
            throw new GroupHasNoStaffException(request.getGroupId());
        }

        final Long presentStaffId = presentStaff.getId();
        if (request.getStaffId() != null && request.getStaffId().equals(presentStaffId)) {
            groupForUpdate.unassignStaff();
            this.groupRepository.saveAndFlush(groupForUpdate);
            actualChanges.put(GroupingTypesApiConstants.staffIdParamName, null);
        }

        return GroupUnassignStaffResponse.builder().resourceId(groupForUpdate.getId()).officeId(groupForUpdate.officeId())
                .groupId(request.getGroupId()).changes(actualChanges).build();
    }

    private void generateAccountNumber(Group newGroup) {
        EntityAccountType entityAccountType = null;
        AccountNumberFormat accountNumberFormat = null;
        if (newGroup.isCenter()) {
            entityAccountType = EntityAccountType.CENTER;
            accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(entityAccountType);
            newGroup.updateAccountNo(this.accountNumberGenerator.generateCenterAccountNumber(newGroup, accountNumberFormat));
        } else {
            entityAccountType = EntityAccountType.GROUP;
            accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(entityAccountType);
            newGroup.updateAccountNo(this.accountNumberGenerator.generateGroupAccountNumber(newGroup, accountNumberFormat));
        }
    }

    /**
     * Shared by {@link #createGroup(GroupCreateRequest)} and {@link #createCenter(CenterCreateRequest)}: builds the
     * {@link Group} entity, applies the activation/association permission checks, generates the account number and
     * hierarchy, and persists it. Returns the persisted group together with whether maker-checker is enabled for those
     * tasks; the callers reject the request in that case, see createGroup.
     */
    private Map.Entry<Group, Boolean> createAndPersistGroupingType(final Office groupOffice, final Staff staff, final Group parentGroup,
            final GroupLevel groupLevel, final String name, final String externalId, final boolean active, final LocalDate activationDate,
            final Set<Client> clientMembers, final Set<Group> groupMembers, final LocalDate submittedOnDate, final AppUser currentUser,
            final String accountNo) {

        final Group newGroup = Group.newGroup(groupOffice, staff, parentGroup, groupLevel, name, externalId, active, activationDate,
                clientMembers, groupMembers, submittedOnDate, currentUser, accountNo);

        boolean rollbackTransaction = false;
        if (newGroup.isActive()) {
            this.groupRepository.saveAndFlush(newGroup);
            // validate Group creation rules for Group
            if (newGroup.isGroup()) {
                validateGroupRulesBeforeActivation(newGroup);
            }

            if (newGroup.isCenter()) {
                final CommandWrapper commandWrapper = new CommandWrapperBuilder().activateCenter(null).build();
                rollbackTransaction = this.commandProcessingService.validateRollbackCommand(commandWrapper, currentUser);
            } else {
                final CommandWrapper commandWrapper = new CommandWrapperBuilder().activateGroup(null).build();
                rollbackTransaction = this.commandProcessingService.validateRollbackCommand(commandWrapper, currentUser);
            }
        }

        if (!newGroup.isCenter() && newGroup.hasActiveClients()) {
            final CommandWrapper commandWrapper = new CommandWrapperBuilder().associateClientsToGroup(newGroup.getId()).build();
            rollbackTransaction = this.commandProcessingService.validateRollbackCommand(commandWrapper, currentUser);
        }

        this.groupRepository.save(newGroup);

        // Account Number generation
        if (StringUtils.isBlank(accountNo)) {
            generateAccountNumber(newGroup);
        }

        /*
         * Generate hierarchy for a new center/group and all the child groups if they exist
         */
        newGroup.generateHierarchy();

        this.groupRepository.saveAndFlush(newGroup);
        newGroup.captureStaffHistoryDuringCenterCreation(staff, activationDate);

        return Map.entry(newGroup, rollbackTransaction);
    }

    @Transactional
    @Override
    public GroupCommandResponse associateClientsToGroup(final GroupAssociateClientsRequest request) {
        final Group groupForUpdate = this.groupRepository.findOneWithNotFoundDetection(request.getGroupId());
        final Set<Client> clientMembers = assembleSetOfClients(groupForUpdate.officeId(), request.getClientMembers());
        final Map<String, Object> actualChanges = new HashMap<>();

        final List<String> changes = groupForUpdate.associateClients(clientMembers);

        if (groupForUpdate.isGroup()) {
            validateGroupRulesBeforeClientAssociation(groupForUpdate);
        }
        if (!changes.isEmpty()) {
            actualChanges.put(GroupingTypesApiConstants.clientMembersParamName, changes);
        }

        this.groupRepository.saveAndFlush(groupForUpdate);

        return GroupCommandResponse.builder().resourceId(groupForUpdate.getId()).officeId(groupForUpdate.officeId())
                .groupId(groupForUpdate.getId()).changes(actualChanges).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse disassociateClientsFromGroup(final GroupDisassociateClientsRequest request) {
        final Group groupForUpdate = this.groupRepository.findOneWithNotFoundDetection(request.getGroupId());
        final Set<Client> clientMembers = assembleSetOfClients(groupForUpdate.officeId(), request.getClientMembers());

        // check if any client has got group loans
        checkForActiveJLGLoans(groupForUpdate.getId(), clientMembers);
        validateForJLGSavings(groupForUpdate.getId(), clientMembers);
        final Map<String, Object> actualChanges = new HashMap<>();

        final List<String> changes = groupForUpdate.disassociateClients(clientMembers);
        if (!changes.isEmpty()) {
            actualChanges.put(GroupingTypesApiConstants.clientMembersParamName, changes);
        }

        this.groupRepository.saveAndFlush(groupForUpdate);

        return GroupCommandResponse.builder().resourceId(groupForUpdate.getId()).officeId(groupForUpdate.officeId())
                .groupId(groupForUpdate.getId()).changes(actualChanges).build();
    }

    @Transactional
    @Override
    public GroupCreateResponse createCenter(final CenterCreateRequest request) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final Long officeId = request.getOfficeId();
            final Office centerOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final Locale locale = StringUtils.isBlank(request.getLocale()) ? null : JsonParserHelper.localeFromString(request.getLocale());
            final LocalDate activationDate = DateUtils.parseLocalDate(request.getActivationDate(), request.getDateFormat(), locale);
            final GroupLevel groupLevel = this.groupLevelRepository.findById(GroupTypes.CENTER.getId())
                    .orElseThrow(() -> new GroupLevelNotFoundException(GroupTypes.CENTER.getId()));

            validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(centerOffice, groupLevel, activationDate);

            Staff staff = null;
            if (request.getStaffId() != null) {
                staff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(request.getStaffId(), centerOffice.getHierarchy());
            }

            final Set<Group> groupMembers = assembleSetOfChildGroups(officeId, request.getGroupMembers());

            final boolean active = Boolean.TRUE.equals(request.getActive());
            LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
            if (active && activationDate != null && DateUtils.isAfter(submittedOnDate, activationDate)) {
                submittedOnDate = activationDate;
            }
            if (StringUtils.isNotBlank(request.getSubmittedOnDate())) {
                submittedOnDate = DateUtils.parseLocalDate(request.getSubmittedOnDate(), request.getDateFormat(), locale);
            }

            final String externalId = this.externalIdFactory.create(request.getExternalId()).getValue();

            // See createGroup: the typed pipeline cannot park a request for checker approval, so it is rejected.
            final Map.Entry<Group, Boolean> outcome = createAndPersistGroupingType(centerOffice, staff, null, groupLevel, request.getName(),
                    externalId, active, activationDate, Collections.emptySet(), groupMembers, submittedOnDate, currentUser, null);
            if (outcome.getValue()) {
                throw new GeneralPlatformDomainRuleException("error.msg.center.maker.checker.not.supported",
                        "Maker-checker is enabled for center activation, which this endpoint does not yet support.");
            }
            final Group newCenter = outcome.getKey();

            final CommandProcessingResult commandProcessingResult = new CommandProcessingResultBuilder() //
                    .withOfficeId(centerOffice.getId()) //
                    .withGroupId(newCenter.getId()) //
                    .withEntityId(newCenter.getId()) //
                    .build();
            businessEventNotifierService.notifyPostBusinessEvent(new CentersCreateBusinessEvent(commandProcessingResult));

            return GroupCreateResponse.builder().resourceId(newCenter.getId()).officeId(centerOffice.getId()).groupId(newCenter.getId())
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssuesTyped(request.getName(), request.getExternalId(), dve.getMostSpecificCause(), dve,
                    GroupTypes.CENTER);
            return GroupCreateResponse.builder().build();
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleGroupDataIntegrityIssuesTyped(request.getName(), request.getExternalId(), throwable, dve, GroupTypes.CENTER);
            return GroupCreateResponse.builder().build();
        }
    }

    private void validateGroupRulesBeforeActivation(final Group group) {
        Integer minClients = configurationDomainService.retrieveMinAllowedClientsInGroup();
        Integer maxClients = configurationDomainService.retrieveMaxAllowedClientsInGroup();
        boolean isGroupClientCountValid = group.isGroupsClientCountWithinMinMaxRange(minClients, maxClients);
        if (!isGroupClientCountValid) {
            throw new GroupMemberCountNotInPermissibleRangeException(group.getId(), minClients, maxClients);
        }
        entityDatatableChecksWritePlatformService.runTheCheck(group.getId(), EntityTables.GROUP.getName(), StatusEnum.ACTIVATE.getValue(),
                EntityTables.GROUP.getForeignKeyColumnNameOnDatatable(), null);
    }

    public void validateGroupRulesBeforeClientAssociation(final Group group) {
        Integer minClients = configurationDomainService.retrieveMinAllowedClientsInGroup();
        Integer maxClients = configurationDomainService.retrieveMaxAllowedClientsInGroup();
        boolean isGroupClientCountValid = group.isGroupsClientCountWithinMaxRange(maxClients);
        if (!isGroupClientCountValid) {
            throw new GroupMemberCountNotInPermissibleRangeException(group.getId(), minClients, maxClients);
        }
    }

    @Transactional
    @Override
    public GroupUpdateResponse updateCenter(final CenterUpdateRequest request) {
        return updateGroupingType(request.getCenterId(), request.getName(), request.getExternalId(), request.getStaffId(), null,
                request.getSubmittedOnDate(), request.getActivationDate(), request.getLocale(), request.getDateFormat(), GroupTypes.CENTER);
    }

    @Override
    @Transactional
    public GroupUpdateResponse updateGroup(final GroupUpdateRequest request) {
        return updateGroupingType(request.getGroupId(), request.getName(), request.getExternalId(), request.getStaffId(),
                request.getCenterId(), request.getSubmittedOnDate(), request.getActivationDate(), request.getLocale(),
                request.getDateFormat(), GroupTypes.GROUP);
    }

    private GroupUpdateResponse updateGroupingType(final Long groupId, final String name, final String externalId, final Long staffId,
            final Long centerId, final String submittedOnDateValue, final String activationDateValue, final String localeValue,
            final String dateFormat, final GroupTypes groupingType) {
        try {
            final Group group = this.groupRepository.findOneWithNotFoundDetection(groupId);

            final String groupHierarchy = group.getOffice().getHierarchy();

            this.context.validateAccessRights(groupHierarchy);

            final Map<String, Object> changes = new HashMap<>();

            if (StringUtils.isNotBlank(name) && !Objects.equals(name, group.getName())) {

                group.setName(name);
                changes.put(GroupUpdateRequest.Fields.name, name);
            }

            // The DTO can't distinguish "not sent" from "sent as null", so null leaves externalId untouched; an
            // empty string is distinguishable from both and clears it, matching legacy's defaultIfEmpty(value, null).
            if (externalId != null) {
                final String newExternalId = externalId.isEmpty() ? null : externalId;
                if (!Objects.equals(newExternalId, group.getExternalId())) {
                    group.setExternalId(newExternalId);
                    changes.put(GroupUpdateRequest.Fields.externalId, newExternalId);
                }
            }

            if (staffId != null) {
                final Staff currentStaff = group.getStaff();
                if (currentStaff == null || !Objects.equals(staffId, currentStaff.getId())) {
                    final Staff newStaff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, groupHierarchy);

                    group.updateStaff(newStaff);

                    changes.put(GroupUpdateRequest.Fields.staffId, staffId);
                }
            }

            // A super parent (center) has no parent level, so centerId is ignored for it, as legacy did.
            if (centerId != null && !group.getGroupLevel().isSuperParent()) {
                final Group currentParent = group.getParent();
                final Long currentParentId = currentParent == null ? null : currentParent.getId();

                if (!Objects.equals(centerId, currentParentId)) {
                    final Group newParentGroup = this.groupRepository.findOneWithNotFoundDetection(centerId);

                    if (!newParentGroup.isOfficeIdentifiedBy(group.officeId())) {
                        final String errorMessage = "Group and parent group must have the same office";
                        throw new InvalidOfficeException("group", "attach.to.parent.group", errorMessage);
                    }

                    if (!group.getGroupLevel().isIdentifiedByParentId(newParentGroup.getGroupLevel().getId())) {
                        final String errorMessage = "Parent group's level is not equal to child level's parent level";
                        throw new InvalidGroupLevelException("add", "invalid.level", errorMessage);
                    }

                    group.setParent(newParentGroup);
                    group.generateHierarchy();

                    changes.put(GroupUpdateRequest.Fields.centerId, centerId);
                }
            }

            final Locale locale = StringUtils.isBlank(localeValue) ? null : JsonParserHelper.localeFromString(localeValue);

            LocalDate submittedOnDate = group.getSubmittedOnDate();
            if (StringUtils.isNotEmpty(submittedOnDateValue)) {
                submittedOnDate = DateUtils.parseLocalDate(submittedOnDateValue, dateFormat, locale);
                if (!Objects.equals(submittedOnDate, group.getSubmittedOnDate())) {
                    group.setSubmittedOnDate(submittedOnDate);
                    changes.put(GroupUpdateRequest.Fields.submittedOnDate, submittedOnDateValue);
                }
            }

            if (StringUtils.isNotEmpty(activationDateValue)) {
                final LocalDate activationDate = DateUtils.parseLocalDate(activationDateValue, dateFormat, locale);
                validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(group.getOffice(), group.getGroupLevel(), activationDate);
                if (DateUtils.isAfter(submittedOnDate, activationDate)) {
                    final String levelName = groupingType == GroupTypes.CENTER ? "center" : "group";
                    throw new GeneralPlatformDomainRuleException("error.msg." + levelName + ".activation.date.before.submitted.date",
                            "The parameter activationDate cannot be before the submittedOnDate.", activationDate, submittedOnDate);
                }
                if (!Objects.equals(activationDate, group.getActivationDate())) {
                    group.setActivationDate(activationDate);
                    changes.put(GroupUpdateRequest.Fields.activationDate, activationDateValue);
                }
            }

            this.groupRepository.saveAndFlush(group);

            return GroupUpdateResponse.builder().resourceId(group.getId()).groupId(group.getId()).officeId(group.officeId())
                    .changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssuesTyped(name, externalId, dve.getMostSpecificCause(), dve, groupingType);
            return GroupUpdateResponse.builder().build();
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleGroupDataIntegrityIssuesTyped(name, externalId, throwable, dve, groupingType);
            return GroupUpdateResponse.builder().build();
        }
    }

    @Transactional
    @Override
    public GroupDeleteResponse deleteGroup(final GroupDeleteRequest request) {
        try {
            final Long groupId = request.getGroupId();
            final Group groupForDelete = this.groupRepository.findOneWithNotFoundDetection(groupId);

            if (groupForDelete.isNotPending()) {
                throw new GroupMustBePendingToBeDeletedException(groupId);
            }

            final List<Note> relatedNotes = this.noteRepository.findByGroup(groupForDelete);
            this.noteRepository.deleteAllInBatch(relatedNotes);

            final Long officeId = groupForDelete.officeId();
            final Long deletedGroupId = groupForDelete.getId();

            this.groupRepository.delete(groupForDelete);
            this.groupRepository.flush();

            return GroupDeleteResponse.builder() //
                    .resourceId(deletedGroupId) //
                    .officeId(officeId) //
                    .groupId(deletedGroupId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            log.error("Error occurred.", throwable);
            throw ErrorHandler.getMappable(dve, "error.msg.group.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    @Transactional
    @Override
    public GroupCommandResponse close(final GroupCloseRequest request) {
        final Group groupOrCenter = this.groupRepository.findOneWithNotFoundDetection(request.getGroupId());
        final Locale locale = StringUtils.isBlank(request.getLocale()) ? null : JsonParserHelper.localeFromString(request.getLocale());
        final LocalDate closureDate = DateUtils.parseLocalDate(request.getClosureDate(), request.getDateFormat(), locale);
        final AppUser currentUser = this.context.authenticatedUser();

        if (request.getGroupingType() == GroupTypes.CENTER) {
            closeCenter(groupOrCenter, closureDate, request.getClosureReasonId(), currentUser);
            return GroupCommandResponse.builder().resourceId(groupOrCenter.getId()).build();
        }
        closeGroup(groupOrCenter, closureDate, request.getClosureReasonId(), currentUser);
        return GroupCommandResponse.builder().resourceId(groupOrCenter.getId()).groupId(groupOrCenter.getId()).build();
    }

    private void closeGroup(final Group group, final LocalDate closureDate, final Long closureReasonId, final AppUser currentUser) {
        final CodeValue closureReason = this.codeValueRepository
                .findOneByCodeNameAndIdWithNotFoundDetection(GroupingTypesApiConstants.GROUP_CLOSURE_REASON, closureReasonId);

        if (group.hasActiveClients()) {
            final String errorMessage = group.getGroupLevel().getLevelName()
                    + " cannot be closed because of active clients associated with it.";
            throw new InvalidGroupStateTransitionException(group.getGroupLevel().getLevelName(), "close", "active.clients.exist",
                    errorMessage);
        }

        validateLoansAndSavingsForGroupOrCenterClose(group, closureDate);

        entityDatatableChecksWritePlatformService.runTheCheck(group.getId(), EntityTables.GROUP.getName(), StatusEnum.CLOSE.getValue(),
                EntityTables.GROUP.getForeignKeyColumnNameOnDatatable(), null);

        group.close(currentUser, closureReason, closureDate);

        this.groupRepository.saveAndFlush(group);
    }

    private void validateLoansAndSavingsForGroupOrCenterClose(final Group groupOrCenter, final LocalDate closureDate) {
        final Collection<Loan> groupLoans = this.loanRepositoryWrapper.findByGroupId(groupOrCenter.getId());
        for (final Loan loan : groupLoans) {
            final LoanStatusMapper loanStatus = new LoanStatusMapper(loan.getStatus().getValue());
            if (loanStatus.isOpen()) {
                final String errorMessage = groupOrCenter.getGroupLevel().getLevelName() + " cannot be closed because of non-closed loans.";
                throw new InvalidGroupStateTransitionException(groupOrCenter.getGroupLevel().getLevelName(), "close", "loan.not.closed",
                        errorMessage);
            } else if (loanStatus.isClosed() && DateUtils.isAfter(loan.getClosedOnDate(), closureDate)) {
                final String errorMessage = groupOrCenter.getGroupLevel().getLevelName()
                        + "closureDate cannot be before the loan closedOnDate.";
                throw new InvalidGroupStateTransitionException(groupOrCenter.getGroupLevel().getLevelName(), "close",
                        "date.cannot.before.loan.closed.date", errorMessage, closureDate, loan.getClosedOnDate());
            } else if (loanStatus.isPendingApproval()) {
                final String errorMessage = groupOrCenter.getGroupLevel().getLevelName() + " cannot be closed because of non-closed loans.";
                throw new InvalidGroupStateTransitionException(groupOrCenter.getGroupLevel().getLevelName(), "close", "loan.not.closed",
                        errorMessage);
            } else if (loanStatus.isAwaitingDisbursal()) {
                final String errorMessage = "Group cannot be closed because of non-closed loans.";
                throw new InvalidGroupStateTransitionException(groupOrCenter.getGroupLevel().getLevelName(), "close", "loan.not.closed",
                        errorMessage);
            }
        }

        final List<SavingsAccount> groupSavingAccounts = this.savingsAccountRepositoryWrapper.findByGroupId(groupOrCenter.getId());

        for (final SavingsAccount saving : groupSavingAccounts) {
            if (saving.isActive() || saving.isSubmittedAndPendingApproval() || saving.isApproved()) {
                final String errorMessage = groupOrCenter.getGroupLevel().getLevelName()
                        + " cannot be closed with active savings accounts associated.";
                throw new InvalidGroupStateTransitionException(groupOrCenter.getGroupLevel().getLevelName(), "close",
                        "savings.account.not.closed", errorMessage);
            } else if (saving.isClosed() && DateUtils.isAfter(saving.getClosedOnDate(), closureDate)) {
                final String errorMessage = groupOrCenter.getGroupLevel().getLevelName()
                        + " closureDate cannot be before the loan closedOnDate.";
                throw new InvalidGroupStateTransitionException(groupOrCenter.getGroupLevel().getLevelName(), "close",
                        "date.cannot.before.loan.closed.date", errorMessage, closureDate, saving.getClosedOnDate());
            }
        }
    }

    private void closeCenter(final Group center, final LocalDate closureDate, final Long closureReasonId, final AppUser currentUser) {
        final CodeValue closureReason = this.codeValueRepository
                .findOneByCodeNameAndIdWithNotFoundDetection(GroupingTypesApiConstants.CENTER_CLOSURE_REASON, closureReasonId);

        if (center.hasActiveGroups()) {
            final String errorMessage = center.getGroupLevel().getLevelName()
                    + " cannot be closed because of active groups associated with it.";
            throw new InvalidGroupStateTransitionException(center.getGroupLevel().getLevelName(), "close", "active.groups.exist",
                    errorMessage);
        }

        validateLoansAndSavingsForGroupOrCenterClose(center, closureDate);

        entityDatatableChecksWritePlatformService.runTheCheck(center.getId(), EntityTables.GROUP.getName(), StatusEnum.ACTIVATE.getValue(),
                EntityTables.GROUP.getForeignKeyColumnNameOnDatatable(), null);

        center.close(currentUser, closureReason, closureDate);

        this.groupRepository.saveAndFlush(center);
    }

    private Set<Group> assembleSetOfChildGroups(final Long officeId, final Set<Long> childGroupIds) {
        final Set<Group> childGroups = new HashSet<>();
        if (childGroupIds == null) {
            return childGroups;
        }
        for (final Long id : childGroupIds) {
            final Group group = this.groupRepository.findOneWithNotFoundDetection(id);

            if (!group.isOfficeIdentifiedBy(officeId)) {
                final String errorMessage = "Group and child groups must have the same office.";
                throw new InvalidOfficeException("group", "attach.to.parent.group", errorMessage);
            }

            childGroups.add(group);
        }
        return childGroups;
    }

    @Transactional
    @Override
    public GroupCommandResponse associateGroupsToCenter(final CenterAssociateGroupsRequest request) {
        final Long centerId = request.getCenterId();
        final Group centerForUpdate = this.groupRepository.findOneWithNotFoundDetection(centerId);
        final Set<Group> groupMembers = assembleSetOfChildGroups(centerForUpdate.officeId(), request.getGroupMembers());
        checkGroupMembersMeetingSyncWithCenterMeeting(centerId, groupMembers);

        final Map<String, Object> actualChanges = new HashMap<>();

        final List<String> changes = centerForUpdate.associateGroups(groupMembers);
        if (!changes.isEmpty()) {
            actualChanges.put(GroupingTypesApiConstants.groupMembersParamName, changes);
        }

        this.groupRepository.saveAndFlush(centerForUpdate);

        return GroupCommandResponse.builder().resourceId(centerForUpdate.getId()).officeId(centerForUpdate.officeId())
                .groupId(centerForUpdate.getId()).changes(actualChanges).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse disassociateGroupsFromCenter(final CenterDisassociateGroupsRequest request) {
        final Group centerForUpdate = this.groupRepository.findOneWithNotFoundDetection(request.getCenterId());
        final Set<Group> groupMembers = assembleSetOfChildGroups(centerForUpdate.officeId(), request.getGroupMembers());

        final Map<String, Object> actualChanges = new HashMap<>();

        final List<String> changes = centerForUpdate.disassociateGroups(groupMembers);
        if (!changes.isEmpty()) {
            actualChanges.put(GroupingTypesApiConstants.clientMembersParamName, changes);
        }

        this.groupRepository.saveAndFlush(centerForUpdate);

        return GroupCommandResponse.builder().resourceId(centerForUpdate.getId()).officeId(centerForUpdate.officeId())
                .groupId(centerForUpdate.getId()).changes(actualChanges).build();
    }

    @Transactional
    private void checkForActiveJLGLoans(final Long groupId, final Set<Client> clientMembers) {
        for (final Client client : clientMembers) {
            final Collection<Loan> loans = this.loanRepositoryWrapper.findActiveLoansByLoanIdAndGroupId(client.getId(), groupId);
            if (!CollectionUtils.isEmpty(loans)) {
                final String defaultUserMessage = "Client with identifier " + client.getId()
                        + " cannot be disassociated it has group loans.";
                throw new GroupAccountExistsException("disassociate", "client.has.group.loan", defaultUserMessage, client.getId(), groupId);
            }
        }
    }

    @Transactional
    private void validateForJLGSavings(final Long groupId, final Set<Client> clientMembers) {
        for (final Client client : clientMembers) {
            final Collection<SavingsAccount> savings = this.savingsAccountRepositoryWrapper.findByClientIdAndGroupId(client.getId(),
                    groupId);
            if (!CollectionUtils.isEmpty(savings)) {
                final String defaultUserMessage = "Client with identifier " + client.getId()
                        + " cannot be disassociated it has group savings.";
                throw new GroupAccountExistsException("disassociate", "client.has.group.saving", defaultUserMessage, client.getId(),
                        groupId);
            }
        }
    }

    public void validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(final Office groupOffice, final GroupLevel groupLevel,
            final LocalDate activationDate) {
        if (activationDate != null && DateUtils.isAfter(groupOffice.getOpeningLocalDate(), activationDate)) {
            final String levelName = groupLevel.getLevelName();
            final String errorMessage = levelName + " activation date should be greater than or equal to the parent Office's creation date "
                    + activationDate.toString();
            throw new InvalidGroupStateTransitionException(levelName.toLowerCase(Locale.ROOT), "activate.date",
                    "cannot.be.before.office.activation.date", errorMessage, activationDate, groupOffice.getOpeningLocalDate());
        }
    }

    private void checkGroupMembersMeetingSyncWithCenterMeeting(final Long centerId, final Set<Group> groupMembers) {
        // Get parent(center) calendar
        Calendar ceneterCalendar = null;
        final CalendarInstance parentCalendarInstance = this.calendarInstanceRepository.findByEntityIdAndEntityTypeIdAndCalendarTypeId(
                centerId, CalendarEntityType.CENTERS.getValue(), CalendarType.COLLECTION.getValue());
        if (parentCalendarInstance != null) {
            ceneterCalendar = parentCalendarInstance.getCalendar();
        }

        for (final Group group : groupMembers) {
            // Get child(group) calendar
            Calendar groupCalendar = null;
            final CalendarInstance groupCalendarInstance = this.calendarInstanceRepository.findByEntityIdAndEntityTypeIdAndCalendarTypeId(
                    group.getId(), CalendarEntityType.GROUPS.getValue(), CalendarType.COLLECTION.getValue());
            if (groupCalendarInstance != null) {
                groupCalendar = groupCalendarInstance.getCalendar();
            }

            // Group shouldn't have a meeting when no meeting attached for center
            if (ceneterCalendar == null && groupCalendar != null) {
                throw new GeneralPlatformDomainRuleException(
                        "error.msg.center.associating.group.not.allowed.with.meeting.attached.to.group",
                        "Group with id " + group.getId() + " is already associated with meeting", group.getId());
            }
            // Group meeting recurrence should match with center meeting recurrence
            else if (ceneterCalendar != null && groupCalendar != null) {
                if (!ceneterCalendar.getRecurrence().equalsIgnoreCase(groupCalendar.getRecurrence())) {
                    throw new GeneralPlatformDomainRuleException("error.msg.center.associating.group.not.allowed.with.different.meeting",
                            "Group with id " + group.getId() + " meeting recurrence doesnot matched with center meeting recurrence",
                            group.getId());
                }
            }
        }
    }

    @Transactional
    @Override
    public GroupCreateResponse createGroup(final GroupCreateRequest request) {
        try {
            final Long centerId = request.getCenterId();
            final AppUser currentUser = this.context.authenticatedUser();

            Long officeId;
            Group parentGroup = null;
            if (centerId == null) {
                officeId = request.getOfficeId();
            } else {
                parentGroup = this.groupRepository.findOneWithNotFoundDetection(centerId);
                officeId = parentGroup.officeId();
            }

            final Office groupOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final Locale locale = StringUtils.isBlank(request.getLocale()) ? null : JsonParserHelper.localeFromString(request.getLocale());
            final LocalDate activationDate = DateUtils.parseLocalDate(request.getActivationDate(), request.getDateFormat(), locale);
            final GroupLevel groupLevel = this.groupLevelRepository.findById(GroupTypes.GROUP.getId())
                    .orElseThrow(() -> new GroupLevelNotFoundException(GroupTypes.GROUP.getId()));

            validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(groupOffice, groupLevel, activationDate);

            Staff staff = null;
            final Long staffId = request.getStaffId();
            if (staffId != null) {
                staff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, groupOffice.getHierarchy());
            }

            final Set<Client> clientMembers = assembleSetOfClients(officeId, request.getClientMembers());
            final Set<Group> groupMembers = Collections.emptySet();

            final boolean active = Boolean.TRUE.equals(request.getActive());

            LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
            if (active && activationDate != null && DateUtils.isAfter(submittedOnDate, activationDate)) {
                submittedOnDate = activationDate;
            }
            if (request.getSubmittedOnDate() != null && !request.getSubmittedOnDate().isBlank()) {
                submittedOnDate = DateUtils.parseLocalDate(request.getSubmittedOnDate(), request.getDateFormat(), locale);
            }

            final String accountNo = request.getAccountNo();
            final String externalId = this.externalIdFactory.create(request.getExternalId()).getValue();

            // createAndPersistGroupingType() also calls commandProcessingService.validateRollbackCommand(...) for the
            // activate/associate-clients permission checks, which additionally signals whether maker-checker is
            // enabled for those tasks. Legacy parks the command for checker approval in that case; the new command
            // pipeline has no persistence for an "awaiting approval" command yet (maker-checker is a separate,
            // not-yet-started proposal, see fineract-command/README.md), so until that exists we reject the request
            // outright rather than silently activating/associating without the configured approval.
            final Map.Entry<Group, Boolean> outcome = createAndPersistGroupingType(groupOffice, staff, parentGroup, groupLevel,
                    request.getName(), externalId, active, activationDate, clientMembers, groupMembers, submittedOnDate, currentUser,
                    accountNo);
            if (outcome.getValue()) {
                throw new GeneralPlatformDomainRuleException("error.msg.group.maker.checker.not.supported",
                        "Maker-checker is enabled for group activation or client association, which this endpoint does not yet support.");
            }
            final Group newGroup = outcome.getKey();

            if (request.getDatatables() != null && !request.getDatatables().isEmpty()) {
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getValue(), EntityTables.GROUP.getName(),
                        newGroup.getId(), null, toJsonArray(request.getDatatables()));
            }

            this.entityDatatableChecksWritePlatformService.runTheCheck(newGroup.getId(), EntityTables.GROUP.getName(),
                    StatusEnum.CREATE.getValue(), EntityTables.GROUP.getForeignKeyColumnNameOnDatatable(), null);

            final CommandProcessingResult commandProcessingResult = new CommandProcessingResultBuilder() //
                    .withOfficeId(groupOffice.getId()) //
                    .withGroupId(newGroup.getId()) //
                    .withEntityId(newGroup.getId()) //
                    .build();

            businessEventNotifierService.notifyPostBusinessEvent(new GroupsCreateBusinessEvent(commandProcessingResult));

            return GroupCreateResponse.builder().resourceId(newGroup.getId()).officeId(groupOffice.getId()).groupId(newGroup.getId())
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssuesTyped(request.getName(), request.getExternalId(), dve.getMostSpecificCause(), dve,
                    GroupTypes.GROUP);
            return GroupCreateResponse.builder().build();
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleGroupDataIntegrityIssuesTyped(request.getName(), request.getExternalId(), throwable, dve, GroupTypes.GROUP);
            return GroupCreateResponse.builder().build();
        }
    }

    private JsonArray toJsonArray(final List<DatatableEntry> datatableEntries) {
        final Gson gson = new Gson();
        final JsonArray array = new JsonArray();
        for (final DatatableEntry entry : datatableEntries) {
            final JsonObject jsonEntry = new JsonObject();
            jsonEntry.addProperty("registeredTableName", entry.getRegisteredTableName());
            jsonEntry.add("data", gson.toJsonTree(entry.getData()));
            array.add(jsonEntry);
        }
        return array;
    }

    private Set<Client> assembleSetOfClients(final Long groupOfficeId, final Set<Long> clientMemberIds) {
        final Set<Client> clientMembers = new HashSet<>();
        if (clientMemberIds == null || clientMemberIds.isEmpty()) {
            return clientMembers;
        }
        for (final Long clientId : clientMemberIds) {
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            if (!client.isOfficeIdentifiedBy(groupOfficeId)) {
                final String errorMessage = "Client with identifier " + clientId + " must have the same office as group.";
                throw new InvalidOfficeException("client", "attach.to.group", errorMessage, clientId.toString(), groupOfficeId);
            }
            clientMembers.add(client);
        }
        return clientMembers;
    }

    private void handleGroupDataIntegrityIssuesTyped(final String name, final String externalId, final Throwable realCause,
            final Exception dve, final GroupTypes groupLevel) {
        String levelName = "Invalid";
        switch (groupLevel) {
            case CENTER:
                levelName = "Center";
            break;
            case GROUP:
                levelName = "Group";
            break;
            case INVALID:
            break;
        }

        if (realCause.getMessage().contains("'external_id'")) {
            final String errorMessageForUser = levelName + " with externalId `" + externalId + "` already exists.";
            final String errorMessageForMachine = "error.msg." + levelName.toLowerCase(Locale.ROOT) + ".duplicate.externalId";
            throw new PlatformDataIntegrityException(errorMessageForMachine, errorMessageForUser,
                    GroupingTypesApiConstants.externalIdParamName, externalId);
        } else if (realCause.getMessage().contains("'name'")) {
            final String errorMessageForUser = levelName + " with name `" + name + "` already exists.";
            final String errorMessageForMachine = "error.msg." + levelName.toLowerCase(Locale.ROOT) + ".duplicate.name";
            throw new PlatformDataIntegrityException(errorMessageForMachine, errorMessageForUser, GroupingTypesApiConstants.nameParamName,
                    name);
        }

        log.error("Error occurred.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.group.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }

}
