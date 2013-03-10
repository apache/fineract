/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.InvalidOfficeException;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.organisation.staff.domain.Staff;
import org.mifosplatform.organisation.staff.domain.StaffRepository;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupLevel;
import org.mifosplatform.portfolio.group.domain.GroupLevelRepository;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupHasNoStaffException;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.group.exception.InvalidGroupLevelException;
import org.mifosplatform.portfolio.group.serialization.GroupCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class GroupWritePlatformServiceJpaRepositoryImpl implements GroupWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GroupWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final GroupRepository groupRepository;
    private final ClientRepository clientRepository;
    private final OfficeRepository officeRepository;
    private final StaffRepository staffRepository;
    private final GroupLevelRepository groupLevelRepository;
    private final GroupCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public GroupWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final GroupRepository groupRepository,
            final ClientRepository clientRepository, final OfficeRepository officeRepository, final StaffRepository staffRepository,
            final GroupLevelRepository groupLevelRepository, final GroupCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.groupRepository = groupRepository;
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.staffRepository = staffRepository;
        this.groupLevelRepository = groupLevelRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult createGroup(final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final String name = command.stringValueOfParameterNamed("name");
            final String externalId = command.stringValueOfParameterNamed("externalId");

            final Long parentId = command.longValueOfParameterNamed("parentId");

            Long officeId = null;
            GroupLevel groupLevel = null;
            Group parentGroup = null;

            if (parentId == null) {
                /*
                 * Scenario: Creating parent group [group with highest level]
                 * In-case of Centre/Groups, parent group with highest level is
                 * Centre. In-case of Federation/Cluster/SHG, parent group with
                 * highest level is Federation. In-case of
                 * Village/Centre/Groups, parent group with highest level is
                 * Village.
                 */

                officeId = command.longValueOfParameterNamed("officeId");
                groupLevel = this.groupLevelRepository.findBySuperParent(true);

            } else {

                parentGroup = this.groupRepository.findOne(parentId);
                if (parentGroup == null || parentGroup.isDeleted()) { throw new GroupNotFoundException(parentId); }

                officeId = parentGroup.getOfficeId();
                groupLevel = this.groupLevelRepository.findByParentId(parentGroup.getGroupLevel().getId());

            }

            final Office groupOffice = this.officeRepository.findOne(officeId);
            if (groupOffice == null) { throw new OfficeNotFoundException(officeId); }

            Staff staff = null;
            final Long staffId = command.longValueOfParameterNamed("staffId");

            /**
             * Validate the staff is present in the given office or not
             */
            if (staffId != null) {
                staff = this.staffRepository.findByOffice(staffId, officeId);
                if (staff == null) { throw new StaffNotFoundException(staffId); }
            }

            Set<Client> clientMembers = null;
            if (groupLevel.canHaveClients()) {
                clientMembers = assembleSetOfClients(officeId, command);
            }

            Set<Group> childGroups = null;
            if (groupLevel.isRecursable()) {
                childGroups = assembleSetOfChildGroups(officeId, command);
            }

            final Group newGroup = Group
                    .newGroup(groupOffice, staff, parentGroup, groupLevel, name, externalId, clientMembers, childGroups);

            // pre-save to generate id for use in group hierarchy
            this.groupRepository.save(newGroup);

            newGroup.generateHierarchy();

            this.groupRepository.saveAndFlush(newGroup);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(groupOffice.getId()) //
                    .withGroupId(newGroup.getId()) //
                    .withEntityId(newGroup.getId()) //
                    .build();

        } catch (final DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateGroup(final Long grouptId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(9);

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Group groupForUpdate = this.groupRepository.findOne(grouptId);
            if (groupForUpdate == null || groupForUpdate.isDeleted()) { throw new GroupNotFoundException(grouptId); }

            final Long officeId = groupForUpdate.getOfficeId();

            final String nameParamName = "name";
            if (command.isChangeInStringParameterNamed(nameParamName, groupForUpdate.getName())) {
                final String newValue = command.stringValueOfParameterNamed(nameParamName);
                actualChanges.put(nameParamName, newValue);
                groupForUpdate.setName(StringUtils.defaultIfEmpty(newValue, null));
            }

            final String externalIdParamName = "externalId";
            if (command.isChangeInStringParameterNamed(externalIdParamName, groupForUpdate.getExternalId())) {
                final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
                actualChanges.put(externalIdParamName, newValue);
                groupForUpdate.setExternalId(StringUtils.defaultIfEmpty(newValue, null));
            }

            final Staff presentStaff = groupForUpdate.getStaff();
            Long presentStaffId = null;
            if (presentStaff != null) {
                presentStaffId = presentStaff.getId();
            }

            final String staffIdParamName = "staffId";
            if (command.isChangeInLongParameterNamed(staffIdParamName, presentStaffId)) {
                final Long newValue = command.longValueOfParameterNamed(staffIdParamName);
                actualChanges.put(staffIdParamName, newValue);
                final Staff newStaff = this.staffRepository.findByOffice(newValue, officeId);
                if (newStaff == null) { throw new StaffNotFoundException(newValue); }
                groupForUpdate.setStaff(newStaff);
            }

            final GroupLevel groupLevel = this.groupLevelRepository.findOne(groupForUpdate.getGroupLevel().getId());

            /*
             * Ignoring parentId param, if group for update is super parent.
             * TODO Need to check: Ignoring is correct or need throw unsupported
             * param
             */
            if (!groupLevel.isSuperParent()) {
                final String parentIdParamName = "parentId";
                final Long parentId = groupForUpdate.getParent().getId();
                if (command.isChangeInLongParameterNamed(parentIdParamName, parentId)) {

                    final Long newValue = command.longValueOfParameterNamed(parentIdParamName);
                    actualChanges.put(parentIdParamName, newValue);
                    final Group newParentGroup = this.groupRepository.findOne(newValue);

                    if (newParentGroup == null || newParentGroup.isDeleted()) { throw new StaffNotFoundException(newValue); }

                    if (!newParentGroup.isOfficeIdentifiedBy(officeId)) {
                        final String errorMessage = "Group and parent group must have the same office";
                        throw new InvalidOfficeException("group", "attach.to.parent.group", errorMessage);
                    }

                    /*
                     * If Group is not super parent then validate group level's
                     * parent level is same as group parent's level this check
                     * makes sure new group is added at immediate next level in
                     * hierarchy
                     */

                    if (!groupForUpdate.getGroupLevel().isIdentifiedByParentId(newParentGroup.getGroupLevel().getId())) {
                        final String errorMessage = "Parent group's level is  not equal to child level's parent level ";
                        throw new InvalidGroupLevelException("add", "invalid.level", errorMessage);
                    }

                    groupForUpdate.setParent(newParentGroup);
                }
            }

            final Set<Client> clientMembers = assembleSetOfClients(officeId, command);
            final String clientMembersParamName = "clientMembers";

            if (!clientMembers.equals(groupForUpdate.getClientMembers())) {
                final String[] newValue = command.arrayValueOfParameterNamed(clientMembersParamName);
                // TODO Use Guava's symmetricDifference
                actualChanges.put(clientMembersParamName, newValue);
                groupForUpdate.setClientMembers(clientMembers);
            }

            this.groupRepository.saveAndFlush(groupForUpdate);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(groupForUpdate.getId()) //
                    .withGroupId(groupForUpdate.getOfficeId()) //
                    .withEntityId(groupForUpdate.getId()) //
                    .with(actualChanges) //
                    .build();

        } catch (final DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult unassignStaff(final Long grouptId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(9);

        this.fromApiJsonDeserializer.validateForUnassignStaff(command.json());

        final Group groupForUpdate = this.groupRepository.findOne(grouptId);
        if (groupForUpdate == null || groupForUpdate.isDeleted()) { throw new GroupNotFoundException(grouptId); }

        final Staff presentStaff = groupForUpdate.getStaff();
        Long presentStaffId = null;
        if (presentStaff == null) { throw new GroupHasNoStaffException(grouptId); }
        presentStaffId = presentStaff.getId();
        final String staffIdParamName = "staffId";
        if (!command.isChangeInLongParameterNamed(staffIdParamName, presentStaffId)) {
            groupForUpdate.unassigStaff();
        }
        this.groupRepository.saveAndFlush(groupForUpdate);

        actualChanges.put(staffIdParamName, null);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(groupForUpdate.getId()) //
                .withGroupId(groupForUpdate.getOfficeId()) //
                .withEntityId(groupForUpdate.getId()) //
                .with(actualChanges) //
                .build();

    }

    @Transactional
    @Override
    public CommandProcessingResult deleteGroup(final Long groupId) {

        this.context.authenticatedUser();

        // TODO add logic to check any active group loans are preset

        final Group groupForDelete = this.groupRepository.findOne(groupId);
        if (groupForDelete == null || groupForDelete.isDeleted()) { throw new GroupNotFoundException(groupId); }
        groupForDelete.delete();
        this.groupRepository.save(groupForDelete);

        return new CommandProcessingResultBuilder() //
                .withOfficeId(groupForDelete.getId()) //
                .withGroupId(groupForDelete.getOfficeId()) //
                .withEntityId(groupForDelete.getId()) //
                .build();
    }

    private Set<Client> assembleSetOfClients(final Long officeId, final JsonCommand command) {

        final Set<Client> clientMembers = new HashSet<Client>();
        final String[] clientMembersArray = command.arrayValueOfParameterNamed("clientMembers");

        if (!ObjectUtils.isEmpty(clientMembersArray)) {
            for (final String clientId : clientMembersArray) {
                final Long id = Long.valueOf(clientId);
                final Client client = this.clientRepository.findOne(id);
                if (client == null || client.isDeleted()) { throw new ClientNotFoundException(id); }
                if (!client.isOfficeIdentifiedBy(officeId)) {
                    final String errorMessage = "Group and Client must have the same office.";
                    throw new InvalidOfficeException("client", "attach.to.group", errorMessage);
                }
                clientMembers.add(client);
            }
        }

        return clientMembers;
    }

    private Set<Group> assembleSetOfChildGroups(final Long officeId, final JsonCommand command) {

        final Set<Group> childGroups = new HashSet<Group>();
        final String[] childGroupsArray = command.arrayValueOfParameterNamed("childGroups");

        if (!ObjectUtils.isEmpty(childGroupsArray)) {
            for (final String groupId : childGroupsArray) {
                final Long id = Long.valueOf(groupId);
                final Group group = this.groupRepository.findOne(id);
                if (group == null || group.isDeleted()) { throw new GroupNotFoundException(id); }
                if (!group.isOfficeIdentifiedBy(officeId)) {
                    final String errorMessage = "Group and child groups must have the same office.";
                    throw new InvalidOfficeException("group", "attach.to.parent.group", errorMessage);
                }
                childGroups.add(group);
            }
        }

        return childGroups;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleGroupDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {
            throw new PlatformDataIntegrityException("error.msg.group.duplicate.externalId", "Group with externalId {0} already exists",
                    "externalId", command.stringValueOfParameterNamed("externalId"));
        } else if (realCause.getMessage().contains("name")) { throw new PlatformDataIntegrityException("error.msg.group.duplicate.name",
                "Group with name {0} already exists", "name", command.stringValueOfParameterNamed("name")); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.group.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
