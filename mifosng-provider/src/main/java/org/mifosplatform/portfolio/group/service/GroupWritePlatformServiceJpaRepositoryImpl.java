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
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.group.command.GroupCommand;
import org.mifosplatform.portfolio.group.command.GroupCommandValidator;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
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
    public CommandProcessingResult createGroup(GroupCommand command) {
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

            return new CommandProcessingResult(newGroup.getId());
        } catch (DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateGroup(GroupCommand command) {

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

            return new CommandProcessingResult(groupForUpdate.getId());
        } catch (DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteGroup(Long groupId) {

        context.authenticatedUser();

        Group groupForDelete = this.groupRepository.findOne(groupId);
        if (groupForDelete == null || groupForDelete.isDeleted()) {
            throw new GroupNotFoundException(groupId);
        }
        groupForDelete.delete();
        this.groupRepository.save(groupForDelete);

        return new CommandProcessingResult(groupId);
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
