package org.mifosng.platform.group.service;

import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.GroupNotFoundException;
import org.mifosng.platform.exceptions.InvalidOfficeException;
import org.mifosng.platform.exceptions.OfficeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.group.domain.Group;
import org.mifosng.platform.group.domain.GroupRepository;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
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

    @Autowired
    public GroupWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context, GroupRepository groupRepository,
            ClientRepository clientRepository, OfficeRepository officeRepository) {
        this.context = context;
        this.groupRepository = groupRepository;
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
    }

    @Transactional
    @Override
    public EntityIdentifier createGroup(GroupCommand command) {
        try {
            this.context.authenticatedUser();

            GroupCommandValidator validator = new GroupCommandValidator(command);
            validator.validateForCreate();

            Office groupOffice = this.officeRepository.findOne(command.getOfficeId());
            if (groupOffice == null) {
                throw new OfficeNotFoundException(command.getOfficeId());
            }

            final Set<Client> clientMembers = assembleSetOfClients(command);

            Group newGroup = Group.newGroup(groupOffice, command.getName(), command.getExternalId(), clientMembers);

            this.groupRepository.saveAndFlush(newGroup);

            return new EntityIdentifier(newGroup.getId());
        } catch (DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new EntityIdentifier(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public EntityIdentifier updateGroup(GroupCommand command) {

        try {
            context.authenticatedUser();

            GroupCommandValidator validator = new GroupCommandValidator(command);
            validator.validateForUpdate();

            Group groupForUpdate = this.groupRepository.findOne(command.getId());
            if (groupForUpdate == null || groupForUpdate.isDeleted()) {
                throw new GroupNotFoundException(command.getId());
            }

            Office groupOffice = null;
            Long officeId = command.getOfficeId();
            if (command.isOfficeIdChanged() && officeId != null) {
                groupOffice = this.officeRepository.findOne(officeId);
                if (groupOffice == null) {
                    throw new OfficeNotFoundException(command.getOfficeId());
                }
            }

            final Set<Client> clientMembers = assembleSetOfClients(command);

            groupForUpdate.update(command, groupOffice, clientMembers);

            groupRepository.saveAndFlush(groupForUpdate);

            return new EntityIdentifier(groupForUpdate.getId());
        } catch (DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new EntityIdentifier(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public EntityIdentifier deleteGroup(Long groupId) {

        context.authenticatedUser();

        Group groupForDelete = this.groupRepository.findOne(groupId);
        if (groupForDelete == null || groupForDelete.isDeleted()) {
            throw new GroupNotFoundException(groupId);
        }
        groupForDelete.delete();
        this.groupRepository.save(groupForDelete);

        return new EntityIdentifier(groupId);
    }

    private Set<Client> assembleSetOfClients(final GroupCommand command) {

        Set<Client> clientMembers = new HashSet<Client>();
        String[] clientMembersArray = command.getClientMembers();

        if (command.isClientMembersChanged() && !ObjectUtils.isEmpty(clientMembersArray)) {
            for (String clientId : clientMembersArray) {
                final Long id = Long.valueOf(clientId);
                Client client = this.clientRepository.findOne(id);
                if (client == null || client.isDeleted()) {
                    throw new ClientNotFoundException(id);
                }
                if (!client.isOfficeIdentifiedBy(command.getOfficeId())){
                    String errorMessage = "Group and Client must have the same office.";
                    throw new InvalidOfficeException("client", "attach.to.group", errorMessage);
                }
                clientMembers.add(client);
            }
        }

        return clientMembers;
    }

    /*
      * Guaranteed to throw an exception no matter what the data integrity issue is.
      */
    private void handleGroupDataIntegrityIssues(final GroupCommand command, DataIntegrityViolationException dve)  {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {
            throw new PlatformDataIntegrityException("error.msg.group.duplicate.externalId", "Group with externalId {0} already exists", "externalId", command.getExternalId());
        } else if (realCause.getMessage().contains("name")) {
            throw new PlatformDataIntegrityException("error.msg.group.duplicate.name", "Group with name {0} already exists", "name", command.getName());
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.group.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }
}
