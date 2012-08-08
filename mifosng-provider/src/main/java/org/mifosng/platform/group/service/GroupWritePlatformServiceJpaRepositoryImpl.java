package org.mifosng.platform.group.service;

import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.GroupNotFoundException;
import org.mifosng.platform.group.domain.Group;
import org.mifosng.platform.group.domain.GroupRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class GroupWritePlatformServiceJpaRepositoryImpl implements GroupWritePlatformService {

    private final PlatformSecurityContext context;

    private final GroupRepository groupRepository;

    private final ClientRepository clientRepository;

    @Autowired
    public GroupWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context, GroupRepository groupRepository,
            ClientRepository clientRepository) {
        this.context = context;
        this.groupRepository = groupRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    @Override
    public EntityIdentifier createGroup(GroupCommand command) {

        this.context.authenticatedUser();

        GroupCommandValidator validator = new GroupCommandValidator(command);
        validator.validateForCreate();

        final Set<Client> clientMembers = assembleSetOfClients(command);

        Group newGroup = Group.newGroup(command.getName(), command.getExternalId(), clientMembers);

        this.groupRepository.save(newGroup);

        return new EntityIdentifier(newGroup.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier updateGroup(GroupCommand command) {

        context.authenticatedUser();

        GroupCommandValidator validator = new GroupCommandValidator(command);
        validator.validateForUpdate();

        Group groupForUpdate = this.groupRepository.findOne(command.getId());
        if (groupForUpdate == null || groupForUpdate.isDeleted()) {
            throw new GroupNotFoundException(command.getId());
        }
        groupForUpdate.update(command);

        groupRepository.save(groupForUpdate);

        return new EntityIdentifier(groupForUpdate.getId());
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
                Long id = Long.valueOf(clientId);
                Client client = this.clientRepository.findOne(id);
                if (client == null) {
                    throw new ClientNotFoundException(id);
                }
                clientMembers.add(client);
            }
        }

        return clientMembers;
    }

}
