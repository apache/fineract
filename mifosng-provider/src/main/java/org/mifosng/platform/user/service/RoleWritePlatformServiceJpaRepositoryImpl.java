package org.mifosng.platform.user.service;

import java.util.Collection;
import java.util.Map;

import org.mifosng.platform.accounting.api.commands.RolePermissionCommand;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.exceptions.PermissionNotFoundException;
import org.mifosng.platform.exceptions.RoleNotFoundException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.Permission;
import org.mifosng.platform.user.domain.PermissionRepository;
import org.mifosng.platform.user.domain.Role;
import org.mifosng.platform.user.domain.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleWritePlatformServiceJpaRepositoryImpl implements RoleWritePlatformService {

    private final PlatformSecurityContext context;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    // FIXME - kw - this value is hardcoded for now but will be retrieved from
    // configuration
    private boolean makerCheckerEnabledForTask = false;
    
    @Autowired
    public RoleWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final RoleRepository roleRepository,
            final PermissionRepository permissionRepository) {
        this.context = context;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    @Override
    public Long createRole(final RoleCommand command) {

        context.authenticatedUser();

        RoleCommandValidator validator = new RoleCommandValidator(command);
        validator.validateForCreate();

        Role entity = new Role(command.getName(), command.getDescription());

        this.roleRepository.save(entity);
        
        if (this.makerCheckerEnabledForTask && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        return entity.getId();
    }

    @Transactional
    @Override
    public Long updateRole(RoleCommand command) {

        context.authenticatedUser();

        RoleCommandValidator validator = new RoleCommandValidator(command);
        validator.validateForUpdate();

        Role role = this.roleRepository.findOne(command.getId());
        if (role == null) { throw new RoleNotFoundException(command.getId()); }
        role.update(command);

        this.roleRepository.save(role);
        
        if (this.makerCheckerEnabledForTask && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        return role.getId();
    }

    @Transactional
    @Override
    public Long updateRolePermissions(final RolePermissionCommand command) {
        context.authenticatedUser();

        final Role role = this.roleRepository.findOne(command.getRoleId());
        if (role == null) { throw new RoleNotFoundException(command.getRoleId()); }

        final Collection<Permission> allPermissions = this.permissionRepository.findAll();

        final Map<String, Boolean> commandPermissions = command.getPermissions();
        for (final String permissionCode : commandPermissions.keySet()) {
            final boolean isSelected = commandPermissions.get(permissionCode).booleanValue();

            final Permission permission = findPermissionByCode(allPermissions, permissionCode);
            if (isSelected) {
                role.addPermission(permission);
            } else {
                role.removePermission(permission);
            }
        }

        this.roleRepository.save(role);
        
        if (this.makerCheckerEnabledForTask && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        return role.getId();
    }

    private Permission findPermissionByCode(Collection<Permission> allPermissions, String permissionCode) {

        if (allPermissions != null) {
            for (Permission permission : allPermissions) {
                if (permission.hasCode(permissionCode)) {return permission;}
            }
        }

        throw new PermissionNotFoundException(permissionCode);
    }
}