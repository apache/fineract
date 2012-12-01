package org.mifosplatform.infrastructure.user.service;

import java.util.Collection;
import java.util.Map;

import org.mifosplatform.infrastructure.configuration.service.ConfigurationDomainService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RoleCommandValidator;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;
import org.mifosplatform.infrastructure.user.domain.Permission;
import org.mifosplatform.infrastructure.user.domain.PermissionRepository;
import org.mifosplatform.infrastructure.user.domain.Role;
import org.mifosplatform.infrastructure.user.domain.RoleRepository;
import org.mifosplatform.infrastructure.user.exception.PermissionNotFoundException;
import org.mifosplatform.infrastructure.user.exception.RoleNotFoundException;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleWritePlatformServiceJpaRepositoryImpl implements RoleWritePlatformService {

    private final PlatformSecurityContext context;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public RoleWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final RoleRepository roleRepository,
            final PermissionRepository permissionRepository,
            final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public Long createRole(final RoleCommand command) {

        context.authenticatedUser();

        final RoleCommandValidator validator = new RoleCommandValidator(command);
        validator.validateForCreate();
        
        final Role entity = new Role(command.getName(), command.getDescription());

        this.roleRepository.save(entity);
        
        if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_ROLE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
        return entity.getId();
    }

    @Transactional
    @Override
    public Long updateRole(RoleCommand command) {

        context.authenticatedUser();

        final RoleCommandValidator validator = new RoleCommandValidator(command);
        validator.validateForUpdate();
        
        final Role role = this.roleRepository.findOne(command.getId());
        if (role == null) { throw new RoleNotFoundException(command.getId()); }
        role.update(command);

        this.roleRepository.save(role);
        
        if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_ROLE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
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
        
        if (this.configurationDomainService.isMakerCheckerEnabledForTask("PERMISSIONS_ROLE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
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