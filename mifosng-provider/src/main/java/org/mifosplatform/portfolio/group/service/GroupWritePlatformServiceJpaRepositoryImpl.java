/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.service;

import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
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
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.mifosplatform.portfolio.group.command.GroupCommandValidator;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupLevel;
import org.mifosplatform.portfolio.group.domain.GroupLevelRepository;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupLevelNotFoundException;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.group.exception.InvalidGroupLevelException;
import org.mifosplatform.portfolio.group.exception.LoanOfficerNotFoundException;
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

    @Autowired
    public GroupWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final GroupRepository groupRepository,
            final ClientRepository clientRepository, final OfficeRepository officeRepository, final StaffRepository staffRepository,
            final GroupLevelRepository groupLevelRepository) {
        this.context = context;
        this.groupRepository = groupRepository;
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.staffRepository = staffRepository;
        this.groupLevelRepository = groupLevelRepository; 
    }

    @Transactional
    @Override
    public CommandProcessingResult createGroup(final GroupCommand command) {
        try {
            this.context.authenticatedUser();

            final GroupCommandValidator validator = new GroupCommandValidator(command);
            validator.validateForCreate();

            final Office groupOffice = this.officeRepository.findOne(command.getOfficeId());

            if (groupOffice == null) { throw new OfficeNotFoundException(command.getOfficeId()); }

            Staff staff = null;
            final Long staffId = command.getStaffId();

            /**
             * Validate the staff is present in the given office or not
             */
            if (staffId != null) {
                staff = this.staffRepository.findByOffice(staffId, command.getOfficeId());
                if (staff == null) { throw new StaffNotFoundException(staffId); }
            }

            final Long parentId = command.getParentId();
            Group parent = null;
            
            /**
             * Validate the parent group to check it is present.
             */
            Long parentGroupLevelId = null;
            if (parentId != null) {
                parent = this.groupRepository.findOne(parentId);
                if (parent == null) { throw new GroupNotFoundException(parentId); }
                parentGroupLevelId = parent.getLevelId();
            }
            
            final Long levelId = command.getLevelId();
            
            final GroupLevel groupLevel = groupLevelRepository.findOne(levelId);
            
            if(groupLevel == null){throw new GroupLevelNotFoundException(levelId);}
            
            final Long parentLevelId = groupLevel.getParentId();
                
            /*
             * If Group is not super parent then validate group level's parent level is same as group parent's level
             * this check makes sure new group is added at immediate next level in hierarchy 
             */
            if(!groupLevel.isSuperParent()){
                if (parentGroupLevelId != parentLevelId) {
                    final String errorMessage = "Parent group's level[" + parentLevelId + "] is  not equal to child level's parent level ["
                            + parentGroupLevelId + "]";
                    throw new InvalidGroupLevelException("add", "invalid.level", errorMessage, parentLevelId , parentGroupLevelId);
                }
            }
            
            final Set<Client> clientMembers = assembleSetOfClients(command);

            final Group newGroup = Group.newGroup(groupOffice, staff, parent , levelId , command.getName(), command.getExternalId(), clientMembers);

         // pre save to generate id for use in group hierarchy
            this.groupRepository.save(newGroup);
            
            newGroup.generateHierarchy();
            
            this.groupRepository.saveAndFlush(newGroup);
            

            return new CommandProcessingResult(newGroup.getId());
        } catch (final DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateGroup(final GroupCommand command) {

        try {
            this.context.authenticatedUser();

            final GroupCommandValidator validator = new GroupCommandValidator(command);
            validator.validateForUpdate();

            final Group groupForUpdate = this.groupRepository.findOne(command.getId());
            if (groupForUpdate == null || groupForUpdate.isDeleted()) { throw new GroupNotFoundException(command.getId()); }

            Office groupOffice = null;
            final Long officeId = command.getOfficeId();
            Staff staff = null;
            final Long staffId = command.getStaffId();

            if (command.isOfficeIdChanged() && command.isStaffChanged()) {
                /**
                 * Scenario: Both Office and Staff are changed, check new
                 * staff is present in new office
                 */
                if (officeId != null) {
                    groupOffice = this.officeRepository.findOne(officeId);
                    if (groupOffice == null) { throw new OfficeNotFoundException(command.getOfficeId()); }
                }
                if (staffId != null) {
                    staff = this.staffRepository.findByOffice(staffId, officeId);
                    if (staff == null) { throw new LoanOfficerNotFoundException(staffId); }
                }

            } else if (command.isOfficeIdChanged()) {
                /**
                 * Scenario: Only Office is changed, check new office has
                 * present staff, This situation is not practical as 
                 * staff can't be present in two offices
                 */
                // TODO When office is changed need to make sure all the staff
                // at loan level are also updated or verified
                
                if (officeId != null) {
                    groupOffice = this.officeRepository.findOne(officeId);
                    if (groupOffice == null) { throw new OfficeNotFoundException(command.getOfficeId()); }
                }
                if (staffId != null) {
                    staff = this.staffRepository.findByOffice(staffId, groupForUpdate.getOfficeId());
                    if (staff == null) { throw new LoanOfficerNotFoundException(staffId); }
                }

            } else if (command.isStaffChanged()) {
                /**
                 * Scenario: Only staff is changed, check new new loan
                 * officer is in present assigned office
                 */
                if (staffId != null) {
                    staff = this.staffRepository.findByOffice(staffId, groupForUpdate.getOfficeId());
                    if (staff == null) { throw new LoanOfficerNotFoundException(staffId); }
                }
            }

            final Set<Client> clientMembers = assembleSetOfClients(command);

            groupForUpdate.update(command, groupOffice, staff, clientMembers);

            this.groupRepository.saveAndFlush(groupForUpdate);

            return new CommandProcessingResult(groupForUpdate.getId());
        } catch (final DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override

    public CommandProcessingResult assignStaff(final GroupCommand command) {

        this.context.authenticatedUser();

        final GroupCommandValidator validator = new GroupCommandValidator(command);
        validator.validateForStaffUpdate();

        final Group groupForUpdate = this.groupRepository.findOne(command.getId());
        if (groupForUpdate == null || groupForUpdate.isDeleted()) { throw new GroupNotFoundException(command.getId()); }

        Staff staff = null;
        final Long staffId = command.getStaffId();
        if (command.isStaffChanged() && staffId != null) {
            staff = this.staffRepository.findByOffice(staffId, groupForUpdate.getOfficeId());
            if (staff == null) { throw new LoanOfficerNotFoundException(staffId); }
        }

        groupForUpdate.assigStaff(command, staff);

        this.groupRepository.saveAndFlush(groupForUpdate);

        return new CommandProcessingResult(groupForUpdate.getId());
    }

    @Transactional
    @Override
    public CommandProcessingResult unassignStaff(final GroupCommand command) {

        this.context.authenticatedUser();

        final GroupCommandValidator validator = new GroupCommandValidator(command);
        validator.validateForStaffUpdate();

        final Group groupForUpdate = this.groupRepository.findOne(command.getId());
        if (groupForUpdate == null || groupForUpdate.isDeleted()) { throw new GroupNotFoundException(command.getId()); }

        final Long staffId = command.getStaffId();
        final Long presentStaffId = groupForUpdate.getStaffId();

        if (command.isStaffChanged() && staffId != null) {
            if (!staffId.equals(presentStaffId)) { throw new LoanOfficerNotFoundException(staffId); }
        }

        groupForUpdate.unassigStaff(command);

        this.groupRepository.saveAndFlush(groupForUpdate);

        return new CommandProcessingResult(groupForUpdate.getId());
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteGroup(final Long groupId) {

        this.context.authenticatedUser();

        final Group groupForDelete = this.groupRepository.findOne(groupId);
        if (groupForDelete == null || groupForDelete.isDeleted()) { throw new GroupNotFoundException(groupId); }
        groupForDelete.delete();
        this.groupRepository.save(groupForDelete);

        return new CommandProcessingResult(groupId);
    }

    private Set<Client> assembleSetOfClients(final GroupCommand command) {

        final Set<Client> clientMembers = new HashSet<Client>();
        final String[] clientMembersArray = command.getClientMembers();

        if (command.isClientMembersChanged() && !ObjectUtils.isEmpty(clientMembersArray)) {
            for (final String clientId : clientMembersArray) {
                final Long id = Long.valueOf(clientId);
                final Client client = this.clientRepository.findOne(id);
                if (client == null || client.isDeleted()) { throw new ClientNotFoundException(id); }
                if (!client.isOfficeIdentifiedBy(command.getOfficeId())) {
                    final String errorMessage = "Group and Client must have the same office.";
                    throw new InvalidOfficeException("client", "attach.to.group", errorMessage);
                }
                clientMembers.add(client);
            }
        }

        return clientMembers;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleGroupDataIntegrityIssues(final GroupCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {
            throw new PlatformDataIntegrityException("error.msg.group.duplicate.externalId", "Group with externalId {0} already exists",
                    "externalId", command.getExternalId());
        } else if (realCause.getMessage().contains("name")) { throw new PlatformDataIntegrityException("error.msg.group.duplicate.name",
                "Group with name {0} already exists", "name", command.getName()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.group.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
