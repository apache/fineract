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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
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
import org.apache.fineract.portfolio.group.data.CenterActivateRequest;
import org.apache.fineract.portfolio.group.data.CenterAssociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterCloseRequest;
import org.apache.fineract.portfolio.group.data.CenterCommandResponse;
import org.apache.fineract.portfolio.group.data.CenterCreateRequest;
import org.apache.fineract.portfolio.group.data.CenterCreateResponse;
import org.apache.fineract.portfolio.group.data.CenterDeleteRequest;
import org.apache.fineract.portfolio.group.data.CenterDeleteResponse;
import org.apache.fineract.portfolio.group.data.CenterDisassociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterUpdateRequest;
import org.apache.fineract.portfolio.group.data.CenterUpdateResponse;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupAssignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupAssociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupCloseRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
import org.apache.fineract.portfolio.group.data.GroupDeleteRequest;
import org.apache.fineract.portfolio.group.data.GroupDeleteResponse;
import org.apache.fineract.portfolio.group.data.GroupDisassociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffRequest;
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
import org.apache.fineract.portfolio.group.mapping.GroupDateMapper;
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
public class GroupingTypesWriteServiceImpl implements GroupingTypesWriteService {

    private final PlatformSecurityContext context;
    private final GroupRepositoryWrapper groupRepository;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final StaffRepositoryWrapper staffRepository;
    private final NoteRepository noteRepository;
    private final GroupLevelRepository groupLevelRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanOfficerService loanOfficerService;
    private final ExternalIdFactory externalIdFactory;
    private final GroupDateMapper dateMapper;

    private record Updated(Group group, Map<String, Object> changes) {
    }

    @Transactional
    @Override
    public GroupCreateResponse createGroup(final GroupCreateRequest request) {
        final Group group = createGroupingType(GroupTypes.GROUP, request.getCenterId(), request.getName(), request.getExternalId(),
                request.getOfficeId(), request.getStaffId(), request.getActive(), request.getActivationDate(), request.getSubmittedOnDate(),
                request.getClientMembers(), null, request.getDatatables(), request.getDateFormat(), request.getLocale());
        final GroupCreateResponse response = GroupCreateResponse.builder().officeId(group.officeId()).groupId(group.getId())
                .resourceId(group.getId()).build();
        businessEventNotifierService.notifyPostBusinessEvent(new GroupsCreateBusinessEvent(toLegacyResult(group)));
        return response;
    }

    @Transactional
    @Override
    public CenterCreateResponse createCenter(final CenterCreateRequest request) {
        final Group center = createGroupingType(GroupTypes.CENTER, null, request.getName(), request.getExternalId(), request.getOfficeId(),
                request.getStaffId(), request.getActive(), request.getActivationDate(), request.getSubmittedOnDate(), null,
                request.getGroupMembers(), request.getDatatables(), request.getDateFormat(), request.getLocale());
        final CenterCreateResponse response = CenterCreateResponse.builder().officeId(center.officeId()).groupId(center.getId())
                .resourceId(center.getId()).build();
        businessEventNotifierService.notifyPostBusinessEvent(new CentersCreateBusinessEvent(toLegacyResult(center)));
        return response;
    }

    @Transactional
    @Override
    public GroupUpdateResponse updateGroup(final GroupUpdateRequest request) {
        final Updated updated = updateGroupingType(request.getId(), request.getName(), request.getExternalId(), request.getOfficeId(),
                request.getStaffId(), request.getCenterId(), request.getActivationDate(), request.getSubmittedOnDate(),
                request.getDateFormat(), request.getLocale(), GroupTypes.GROUP);
        return GroupUpdateResponse.builder().officeId(updated.group().officeId()).groupId(updated.group().getId())
                .resourceId(updated.group().getId()).changes(updated.changes()).build();
    }

    @Transactional
    @Override
    public CenterUpdateResponse updateCenter(final CenterUpdateRequest request) {
        final Updated updated = updateGroupingType(request.getId(), request.getName(), request.getExternalId(), request.getOfficeId(),
                request.getStaffId(), null, request.getActivationDate(), request.getSubmittedOnDate(), request.getDateFormat(),
                request.getLocale(), GroupTypes.CENTER);
        return CenterUpdateResponse.builder().officeId(updated.group().officeId()).groupId(updated.group().getId())
                .resourceId(updated.group().getId()).changes(updated.changes()).build();
    }

    @Transactional
    @Override
    public GroupDeleteResponse deleteGroup(final GroupDeleteRequest request) {
        final Group deleted = deleteGroupingType(request.getId());
        return GroupDeleteResponse.builder().officeId(deleted.officeId()).groupId(deleted.getId()).resourceId(deleted.getId()).build();
    }

    @Transactional
    @Override
    public CenterDeleteResponse deleteCenter(final CenterDeleteRequest request) {
        final Group deleted = deleteGroupingType(request.getId());
        return CenterDeleteResponse.builder().officeId(deleted.officeId()).groupId(deleted.getId()).resourceId(deleted.getId()).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse activateGroup(final GroupActivateRequest request) {
        final Group group = activateGroupingType(request.getId(), request.getActivationDate(), request.getDateFormat(),
                request.getLocale());
        return GroupCommandResponse.builder().officeId(group.officeId()).groupId(group.getId()).resourceId(group.getId()).build();
    }

    @Transactional
    @Override
    public CenterCommandResponse activateCenter(final CenterActivateRequest request) {
        final Group center = activateGroupingType(request.getId(), request.getActivationDate(), request.getDateFormat(),
                request.getLocale());
        return CenterCommandResponse.builder().officeId(center.officeId()).groupId(center.getId()).resourceId(center.getId()).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse closeGroup(final GroupCloseRequest request) {
        final Group group = groupRepository.findOneWithNotFoundDetection(request.getId());
        final LocalDate closureDate = dateMapper.toLocalDate(request.getClosureDate(), GroupingTypesApiConstants.closureDateParamName,
                request.getDateFormat(), request.getLocale());
        final AppUser currentUser = context.authenticatedUser();
        final CodeValue closureReason = codeValueRepository
                .findOneByCodeNameAndIdWithNotFoundDetection(GroupingTypesApiConstants.GROUP_CLOSURE_REASON, request.getClosureReasonId());
        if (group.hasActiveClients()) {
            throw new InvalidGroupStateTransitionException(group.getGroupLevel().getLevelName(), "close", "active.clients.exist",
                    group.getGroupLevel().getLevelName() + " cannot be closed because of active clients associated with it.");
        }
        validateLoansAndSavingsForGroupOrCenterClose(group, closureDate);
        entityDatatableChecksWritePlatformService.runTheCheck(group.getId(), EntityTables.GROUP.getName(), StatusEnum.CLOSE.getValue(),
                EntityTables.GROUP.getForeignKeyColumnNameOnDatatable(), null);
        group.close(currentUser, closureReason, closureDate);
        groupRepository.saveAndFlush(group);
        return GroupCommandResponse.builder().groupId(group.getId()).resourceId(group.getId()).build();
    }

    @Transactional
    @Override
    public CenterCommandResponse closeCenter(final CenterCloseRequest request) {
        final Group center = groupRepository.findOneWithNotFoundDetection(request.getId());
        final LocalDate closureDate = dateMapper.toLocalDate(request.getClosureDate(), GroupingTypesApiConstants.closureDateParamName,
                request.getDateFormat(), request.getLocale());
        final CodeValue closureReason = codeValueRepository
                .findOneByCodeNameAndIdWithNotFoundDetection(GroupingTypesApiConstants.CENTER_CLOSURE_REASON, request.getClosureReasonId());
        final AppUser currentUser = context.authenticatedUser();
        if (center.hasActiveGroups()) {
            throw new InvalidGroupStateTransitionException(center.getGroupLevel().getLevelName(), "close", "active.groups.exist",
                    center.getGroupLevel().getLevelName() + " cannot be closed because of active groups associated with it.");
        }
        validateLoansAndSavingsForGroupOrCenterClose(center, closureDate);
        // legacy runs the centre close check against the ACTIVATE status; kept as-is
        entityDatatableChecksWritePlatformService.runTheCheck(center.getId(), EntityTables.GROUP.getName(), StatusEnum.ACTIVATE.getValue(),
                EntityTables.GROUP.getForeignKeyColumnNameOnDatatable(), null);
        center.close(currentUser, closureReason, closureDate);
        groupRepository.saveAndFlush(center);
        return CenterCommandResponse.builder().resourceId(center.getId()).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse associateClients(final GroupAssociateClientsRequest request) {
        final Group group = groupRepository.findOneWithNotFoundDetection(request.getId());
        final Set<Client> clientMembers = assembleSetOfClients(group.officeId(), request.getClientMembers());
        final Map<String, Object> actualChanges = new HashMap<>();
        final List<String> changes = group.associateClients(clientMembers);
        if (group.isGroup()) {
            validateGroupRulesBeforeClientAssociation(group);
        }
        if (!changes.isEmpty()) {
            actualChanges.put(GroupingTypesApiConstants.clientMembersParamName, changes);
        }
        groupRepository.saveAndFlush(group);
        return GroupCommandResponse.builder().officeId(group.officeId()).groupId(group.getId()).resourceId(group.getId())
                .changes(actualChanges).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse disassociateClients(final GroupDisassociateClientsRequest request) {
        final Group group = groupRepository.findOneWithNotFoundDetection(request.getId());
        final Set<Client> clientMembers = assembleSetOfClients(group.officeId(), request.getClientMembers());
        // check if any client has got group loans
        checkForActiveJLGLoans(group.getId(), clientMembers);
        validateForJLGSavings(group.getId(), clientMembers);
        final Map<String, Object> actualChanges = new HashMap<>();
        final List<String> changes = group.disassociateClients(clientMembers);
        if (!changes.isEmpty()) {
            actualChanges.put(GroupingTypesApiConstants.clientMembersParamName, changes);
        }
        groupRepository.saveAndFlush(group);
        return GroupCommandResponse.builder().officeId(group.officeId()).groupId(group.getId()).resourceId(group.getId())
                .changes(actualChanges).build();
    }

    @Transactional
    @Override
    public CenterCommandResponse associateGroups(final CenterAssociateGroupsRequest request) {
        final Group center = groupRepository.findOneWithNotFoundDetection(request.getId());
        final Set<Group> groupMembers = assembleSetOfChildGroups(center.officeId(), request.getGroupMembers());
        checkGroupMembersMeetingSyncWithCenterMeeting(center.getId(), groupMembers);
        final Map<String, Object> actualChanges = new HashMap<>();
        final List<String> changes = center.associateGroups(groupMembers);
        if (!changes.isEmpty()) {
            actualChanges.put(GroupingTypesApiConstants.groupMembersParamName, changes);
        }
        groupRepository.saveAndFlush(center);
        return CenterCommandResponse.builder().officeId(center.officeId()).groupId(center.getId()).resourceId(center.getId())
                .changes(actualChanges).build();
    }

    @Transactional
    @Override
    public CenterCommandResponse disassociateGroups(final CenterDisassociateGroupsRequest request) {
        final Group center = groupRepository.findOneWithNotFoundDetection(request.getId());
        final Set<Group> groupMembers = assembleSetOfChildGroups(center.officeId(), request.getGroupMembers());
        final Map<String, Object> actualChanges = new HashMap<>();
        final List<String> changes = center.disassociateGroups(groupMembers);
        if (!changes.isEmpty()) {
            // legacy records centre group removals under the clientMembers key; kept for compatibility
            actualChanges.put(GroupingTypesApiConstants.clientMembersParamName, changes);
        }
        groupRepository.saveAndFlush(center);
        return CenterCommandResponse.builder().officeId(center.officeId()).groupId(center.getId()).resourceId(center.getId())
                .changes(actualChanges).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse assignStaff(final GroupAssignStaffRequest request) {
        context.authenticatedUser();
        final Group group = groupRepository.findOneWithNotFoundDetection(request.getId());
        final Staff staff = staffRepository.findByOfficeHierarchyWithNotFoundDetection(request.getStaffId(),
                group.getOffice().getHierarchy());
        group.updateStaff(staff);
        if (Boolean.TRUE.equals(request.getInheritStaffForClientAccounts())) {
            final LocalDate loanOfficerReassignmentDate = DateUtils.getBusinessLocalDate();
            /*
             * update loan officer for client and update loan officer for clients loans and savings
             */
            final Set<Client> clients = group.getClientMembers();
            if (clients != null) {
                for (final Client client : clients) {
                    client.updateStaff(staff);
                    if (loanRepositoryWrapper.doNonClosedLoanAccountsExistForClient(client.getId())) {
                        for (final Loan loan : loanRepositoryWrapper.findLoanByClientId(client.getId())) {
                            if (loan.isDisbursed() && !loan.isClosed()) {
                                loanOfficerService.reassignLoanOfficer(loan, staff, loanOfficerReassignmentDate);
                            }
                        }
                    }
                    if (savingsAccountRepositoryWrapper.doNonClosedSavingAccountsExistForClient(client.getId())) {
                        for (final SavingsAccount savingsAccount : savingsAccountRepositoryWrapper
                                .findSavingAccountByClientId(client.getId())) {
                            if (!savingsAccount.isClosed()) {
                                savingsAccount.reassignSavingsOfficer(staff, loanOfficerReassignmentDate);
                            }
                        }
                    }
                }
            }
        }
        groupRepository.saveAndFlush(group);
        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);
        actualChanges.put(GroupingTypesApiConstants.staffIdParamName, request.getStaffId());
        return GroupCommandResponse.builder().officeId(group.officeId()).groupId(group.getId()).resourceId(group.getId())
                .changes(actualChanges).build();
    }

    @Transactional
    @Override
    public GroupCommandResponse unassignStaff(final GroupUnassignStaffRequest request) {
        context.authenticatedUser();
        final Group group = groupRepository.findOneWithNotFoundDetection(request.getId());
        final Staff presentStaff = group.getStaff();
        if (presentStaff == null) {
            throw new GroupHasNoStaffException(request.getId());
        }
        // legacy: unassign only when the supplied staffId is not a change from the current staff
        if (Objects.equals(request.getStaffId(), presentStaff.getId())) {
            group.unassignStaff();
        }
        groupRepository.saveAndFlush(group);
        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);
        actualChanges.put(GroupingTypesApiConstants.staffIdParamName, null);
        return GroupCommandResponse.builder().officeId(group.officeId()).groupId(group.getId()).resourceId(group.getId())
                .changes(actualChanges).build();
    }

    private Group activateGroupingType(final Long groupId, final String activationDateValue, final String dateFormat, final String locale) {
        try {
            final AppUser currentUser = context.authenticatedUser();
            final Group group = groupRepository.findOneWithNotFoundDetection(groupId);
            if (group.isGroup()) {
                validateGroupRulesBeforeActivation(group);
            }
            final LocalDate activationDate = dateMapper.toLocalDate(activationDateValue, GroupingTypesApiConstants.activationDateParamName,
                    dateFormat, locale);
            validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(group.getOffice(), group.getGroupLevel(), activationDate);
            group.activate(currentUser, activationDate);
            groupRepository.saveAndFlush(group);
            return group;
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleGroupDataIntegrityIssues(null, null, dve.getMostSpecificCause(), dve, GroupTypes.GROUP);
        } catch (final PersistenceException dve) {
            throw handleGroupDataIntegrityIssues(null, null, ExceptionUtils.getRootCause(dve.getCause()), dve, GroupTypes.GROUP);
        }
    }

    private CommandProcessingResult toLegacyResult(final Group group) {
        return new CommandProcessingResultBuilder().withOfficeId(group.officeId()).withGroupId(group.getId()).withEntityId(group.getId())
                .build();
    }

    private Group createGroupingType(final GroupTypes groupingType, final Long centerId, final String name, final String externalIdValue,
            final Long requestedOfficeId, final Long staffId, final Boolean activeFlag, final String activationDateValue,
            final String submittedOnDateValue, final List<Long> clientMemberIds, final List<Long> groupMemberIds,
            final List<Map<String, Object>> datatables, final String dateFormat, final String locale) {
        try {
            final String externalId = externalIdFactory.create(externalIdValue).getValue();
            final AppUser currentUser = context.authenticatedUser();
            Long officeId = requestedOfficeId;
            Group parentGroup = null;
            if (centerId != null) {
                parentGroup = groupRepository.findOneWithNotFoundDetection(centerId);
                officeId = parentGroup.officeId();
            }
            final Office groupOffice = officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final LocalDate activationDate = dateMapper.toLocalDate(activationDateValue, GroupingTypesApiConstants.activationDateParamName,
                    dateFormat, locale);
            final GroupLevel groupLevel = groupLevelRepository.findById(groupingType.getId())
                    .orElseThrow(() -> new GroupLevelNotFoundException(groupingType.getId()));
            validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(groupOffice, groupLevel, activationDate);

            Staff staff = null;
            if (staffId != null) {
                staff = staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, groupOffice.getHierarchy());
            }
            final Set<Client> clientMembers = assembleSetOfClients(officeId, clientMemberIds);
            final Set<Group> groupMembers = assembleSetOfChildGroups(officeId, groupMemberIds);

            final boolean active = Boolean.TRUE.equals(activeFlag);
            LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
            if (active && DateUtils.isAfter(submittedOnDate, activationDate)) {
                submittedOnDate = activationDate;
            }
            if (StringUtils.isNotBlank(submittedOnDateValue)) {
                submittedOnDate = dateMapper.toLocalDate(submittedOnDateValue, GroupingTypesApiConstants.submittedOnDateParamName,
                        dateFormat, locale);
            }

            final Group newGroup = Group.newGroup(groupOffice, staff, parentGroup, groupLevel, name, externalId, active, activationDate,
                    clientMembers, groupMembers, submittedOnDate, currentUser, null);

            if (newGroup.isActive()) {
                groupRepository.saveAndFlush(newGroup);
                // validate Group creation rules for Group
                if (newGroup.isGroup()) {
                    validateGroupRulesBeforeActivation(newGroup);
                }
            }

            groupRepository.save(newGroup);

            generateAccountNumber(newGroup);

            /*
             * Generate hierarchy for a new center/group and all the child groups if they exist
             */
            newGroup.generateHierarchy();

            groupRepository.saveAndFlush(newGroup);
            newGroup.captureStaffHistoryDuringCenterCreation(staff, activationDate);

            if (newGroup.isGroup()) {
                if (datatables != null) {
                    final JsonArray datatableArray = new GsonBuilder().serializeNulls().create().toJsonTree(datatables).getAsJsonArray();
                    entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getValue(), EntityTables.GROUP.getName(),
                            newGroup.getId(), null, datatableArray);
                }
                entityDatatableChecksWritePlatformService.runTheCheck(newGroup.getId(), EntityTables.GROUP.getName(),
                        StatusEnum.CREATE.getValue(), EntityTables.GROUP.getForeignKeyColumnNameOnDatatable(), null);
            }
            return newGroup;
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleGroupDataIntegrityIssues(externalIdValue, name, dve.getMostSpecificCause(), dve, groupingType);
        } catch (final PersistenceException dve) {
            throw handleGroupDataIntegrityIssues(externalIdValue, name, ExceptionUtils.getRootCause(dve.getCause()), dve, groupingType);
        }
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

    private Set<Client> assembleSetOfClients(final Long groupOfficeId, final List<Long> clientIds) {
        final Set<Client> clientMembers = new HashSet<>();
        if (clientIds != null) {
            for (final Long id : clientIds) {
                final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(id);
                if (!client.isOfficeIdentifiedBy(groupOfficeId)) {
                    throw new InvalidOfficeException("client", "attach.to.group",
                            "Client with identifier " + id + " must have the same office as group.", id.toString(), groupOfficeId);
                }
                clientMembers.add(client);
            }
        }
        return clientMembers;
    }

    private Set<Group> assembleSetOfChildGroups(final Long officeId, final List<Long> groupIds) {
        final Set<Group> childGroups = new HashSet<>();
        if (groupIds != null) {
            for (final Long id : groupIds) {
                final Group group = groupRepository.findOneWithNotFoundDetection(id);
                if (!group.isOfficeIdentifiedBy(officeId)) {
                    throw new InvalidOfficeException("group", "attach.to.parent.group",
                            "Group and child groups must have the same office.");
                }
                childGroups.add(group);
            }
        }
        return childGroups;
    }

    private Updated updateGroupingType(final Long groupId, final String name, final String externalId, final Long officeId,
            final Long staffId, final Long centerId, final String activationDateValue, final String submittedOnDateValue,
            final String dateFormat, final String locale, final GroupTypes groupingType) {
        try {
            context.authenticatedUser();
            final Group groupForUpdate = groupRepository.findOneWithNotFoundDetection(groupId);
            final Office groupOffice = groupForUpdate.getOffice();
            final String groupHierarchy = groupOffice.getHierarchy();
            context.validateAccessRights(groupHierarchy);

            final LocalDate activationDate = dateMapper.toLocalDate(activationDateValue, GroupingTypesApiConstants.activationDateParamName,
                    dateFormat, locale);
            final LocalDate submittedOnDate = dateMapper.toLocalDate(submittedOnDateValue,
                    GroupingTypesApiConstants.submittedOnDateParamName, dateFormat, locale);
            validateActivationDateNotBeforeSubmittedOnDate(groupingType, activationDate, submittedOnDate);
            validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(groupOffice, groupForUpdate.getGroupLevel(), activationDate);

            final Map<String, Object> actualChanges = new LinkedHashMap<>(9);
            if (externalId != null && !externalId.equals(groupForUpdate.getExternalId())) {
                actualChanges.put(GroupingTypesApiConstants.externalIdParamName, externalId);
                groupForUpdate.setExternalId(StringUtils.defaultIfEmpty(externalId, null));
            }
            if (officeId != null && !officeId.equals(groupForUpdate.officeId())) {
                actualChanges.put(GroupingTypesApiConstants.officeIdParamName, officeId); // recorded, not applied
            }
            final Long currentStaffId = groupForUpdate.getStaff() == null ? null : groupForUpdate.getStaff().getId();
            if (staffId != null && !staffId.equals(currentStaffId)) {
                actualChanges.put(GroupingTypesApiConstants.staffIdParamName, staffId);
                groupForUpdate.updateStaff(staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, groupHierarchy));
            }
            if (name != null && !name.equals(groupForUpdate.getName())) {
                actualChanges.put(GroupingTypesApiConstants.nameParamName, name);
                groupForUpdate.setName(StringUtils.defaultIfEmpty(name, null));
            }
            if (activationDate != null && !activationDate.equals(groupForUpdate.getActivationDate())) {
                actualChanges.put(GroupingTypesApiConstants.activationDateParamName, activationDateValue);
                actualChanges.put(GroupingTypesApiConstants.dateFormatParamName, dateFormat);
                actualChanges.put(GroupingTypesApiConstants.localeParamName, locale);
                groupForUpdate.setActivationDate(activationDate);
            }
            if (submittedOnDate != null && !submittedOnDate.equals(groupForUpdate.getSubmittedOnDate())) {
                actualChanges.put(GroupingTypesApiConstants.submittedOnDateParamName, submittedOnDateValue);
                actualChanges.put(GroupingTypesApiConstants.dateFormatParamName, dateFormat);
                actualChanges.put(GroupingTypesApiConstants.localeParamName, locale);
                groupForUpdate.setSubmittedOnDate(submittedOnDate);
            }

            final GroupLevel groupLevel = groupLevelRepository.findById(groupForUpdate.getGroupLevel().getId())
                    .orElseThrow(() -> new GroupLevelNotFoundException(groupForUpdate.getGroupLevel().getId()));

            /*
             * Ignoring parentId param, if group for update is super parent. TODO Need to check: Ignoring is correct or
             * need throw unsupported param
             */
            if (!groupLevel.isSuperParent() && centerId != null) {
                final Long presentParentId = groupForUpdate.getParent() == null ? null : groupForUpdate.getParent().getId();
                if (!centerId.equals(presentParentId)) {
                    actualChanges.put(GroupingTypesApiConstants.centerIdParamName, centerId);
                    final Group newParentGroup = groupRepository.findOneWithNotFoundDetection(centerId);
                    if (!newParentGroup.isOfficeIdentifiedBy(groupForUpdate.officeId())) {
                        throw new InvalidOfficeException("group", "attach.to.parent.group",
                                "Group and parent group must have the same office");
                    }
                    if (!groupForUpdate.getGroupLevel().isIdentifiedByParentId(newParentGroup.getGroupLevel().getId())) {
                        throw new InvalidGroupLevelException("add", "invalid.level",
                                "Parent group's level is  not equal to child level's parent level ");
                    }
                    groupForUpdate.setParent(newParentGroup);
                    groupForUpdate.generateHierarchy();
                }
            }

            groupRepository.saveAndFlush(groupForUpdate);
            return new Updated(groupForUpdate, actualChanges);
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleGroupDataIntegrityIssues(externalId, name, dve.getMostSpecificCause(), dve, groupingType);
        } catch (final PersistenceException dve) {
            throw handleGroupDataIntegrityIssues(externalId, name, ExceptionUtils.getRootCause(dve.getCause()), dve, groupingType);
        }
    }

    private Group deleteGroupingType(final Long groupId) {
        try {
            final Group groupForDelete = groupRepository.findOneWithNotFoundDetection(groupId);
            if (groupForDelete.isNotPending()) {
                throw new GroupMustBePendingToBeDeletedException(groupId);
            }
            final List<Note> relatedNotes = noteRepository.findByGroup(groupForDelete);
            noteRepository.deleteAllInBatch(relatedNotes);
            groupRepository.delete(groupForDelete);
            groupRepository.flush();
            return groupForDelete;
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            log.error("Error occurred.", ExceptionUtils.getRootCause(dve.getCause()));
            throw ErrorHandler.getMappable(dve, "error.msg.group.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    private RuntimeException handleGroupDataIntegrityIssues(final String externalId, final String name, final Throwable realCause,
            final Exception dve, final GroupTypes groupLevel) {
        final String levelName = switch (groupLevel) {
            case CENTER -> "Center";
            case GROUP -> "Group";
            default -> "Invalid";
        };
        final String message = realCause == null || realCause.getMessage() == null ? "" : realCause.getMessage();
        if (message.contains("'external_id'")) {
            return new PlatformDataIntegrityException("error.msg." + levelName.toLowerCase() + ".duplicate.externalId",
                    levelName + " with externalId `" + externalId + "` already exists.", GroupingTypesApiConstants.externalIdParamName,
                    externalId);
        } else if (message.contains("'name'")) {
            return new PlatformDataIntegrityException("error.msg." + levelName.toLowerCase() + ".duplicate.name",
                    levelName + " with name `" + name + "` already exists.", GroupingTypesApiConstants.nameParamName, name);
        }
        log.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.group.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
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

    private void validateGroupRulesBeforeClientAssociation(final Group group) {
        Integer minClients = configurationDomainService.retrieveMinAllowedClientsInGroup();
        Integer maxClients = configurationDomainService.retrieveMaxAllowedClientsInGroup();
        boolean isGroupClientCountValid = group.isGroupsClientCountWithinMaxRange(maxClients);
        if (!isGroupClientCountValid) {
            throw new GroupMemberCountNotInPermissibleRangeException(group.getId(), minClients, maxClients);
        }
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

    // legacy update validation: when both dates are supplied, activation may not precede submission
    private static void validateActivationDateNotBeforeSubmittedOnDate(final GroupTypes groupingType, final LocalDate activationDate,
            final LocalDate submittedOnDate) {
        final List<ApiParameterError> errors = new ArrayList<>();
        new DataValidatorBuilder(errors)
                .resource(groupingType == GroupTypes.CENTER ? GroupingTypesApiConstants.CENTER_RESOURCE_NAME
                        : GroupingTypesApiConstants.GROUP_RESOURCE_NAME)
                .parameter(GroupingTypesApiConstants.activationDateParamName).value(activationDate).validateDateAfter(submittedOnDate);
        if (!errors.isEmpty()) {
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private void validateOfficeOpeningDateisAfterGroupOrCenterOpeningDate(final Office groupOffice, final GroupLevel groupLevel,
            final LocalDate activationDate) {
        if (activationDate != null && DateUtils.isAfter(groupOffice.getOpeningLocalDate(), activationDate)) {
            final String levelName = groupLevel.getLevelName();
            final String errorMessage = levelName + " activation date should be greater than or equal to the parent Office's creation date "
                    + activationDate.toString();
            throw new InvalidGroupStateTransitionException(levelName.toLowerCase(), "activate.date",
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
}
