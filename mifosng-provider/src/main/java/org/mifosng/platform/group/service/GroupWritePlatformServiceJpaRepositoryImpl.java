package org.mifosng.platform.group.service;

import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.exceptions.GroupNotFoundException;
import org.mifosng.platform.group.domain.Group;
import org.mifosng.platform.group.domain.GroupRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupWritePlatformServiceJpaRepositoryImpl implements GroupWritePlatformService {

    private final PlatformSecurityContext context;
    
    private final GroupRepository groupRepository;
    
    @Autowired
    public GroupWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context, GroupRepository groupRepository) {
        this.context = context;
        this.groupRepository = groupRepository;
    }

    @Transactional
    @Override
    public EntityIdentifier createGroup(GroupCommand command) {
       
        this.context.authenticatedUser();
        
        GroupCommandValidator validator = new GroupCommandValidator(command);
        validator.validateForCreate();
        
        Group newGroup = Group.newGroup(command.getName(), command.getExternalId());
        
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
        if (groupForUpdate == null || groupForUpdate.isDeleted()){
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
        if (groupForDelete == null || groupForDelete.isDeleted()){
            throw new GroupNotFoundException(groupId);
        }
        groupForDelete.delete();
        this.groupRepository.save(groupForDelete);
        
        return new EntityIdentifier(groupId);
    }

}
