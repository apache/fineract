package org.mifosplatform.useradministration.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.service.ConfigurationDomainService;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.domain.Role;
import org.mifosplatform.useradministration.domain.RoleRepository;
import org.mifosplatform.useradministration.exception.PermissionNotFoundException;
import org.mifosplatform.useradministration.exception.RoleNotFoundException;
import org.mifosplatform.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleWritePlatformServiceJpaRepositoryImpl implements RoleWritePlatformService {

    private final PlatformSecurityContext context;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public RoleWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final RoleRepository roleRepository,
            final PermissionRepository permissionRepository,
            final ConfigurationDomainService configurationDomainService,
            final PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.configurationDomainService = configurationDomainService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public Long createRole(final RoleCommand command) {

        context.authenticatedUser();
        command.validateForCreate();
        
        final Role entity = new Role(command.getName(), command.getDescription());

        this.roleRepository.save(entity);
        
        if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_ROLE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
        return entity.getId();
    }

    @Transactional
    @Override
    public Long updateRole(final RoleCommand command) {

        context.authenticatedUser();
        command.validateForUpdate();
        
        final Role role = this.roleRepository.findOne(command.getId());
        if (role == null) { throw new RoleNotFoundException(command.getId()); }
        role.update(command);

        this.roleRepository.save(role);
        
        if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_ROLE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
        return role.getId();
    }

    @Transactional
    @Override
    public EntityIdentifier updateRolePermissions(final Long roleId, final JsonCommand command) {
        context.authenticatedUser();

        final Role role = this.roleRepository.findOne(roleId);
        if (role == null) { throw new RoleNotFoundException(roleId); }

        final Collection<Permission> allPermissions = this.permissionRepository.findAll();

        final PermissionsCommand permissionsCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        
        final Map<String, Boolean> commandPermissions = permissionsCommand.getPermissions();
        final Map<String, Object> changes = new HashMap<String, Object>();
        final Map<String, Boolean> changedPermissions = new HashMap<String, Boolean>();
        for (final String permissionCode : commandPermissions.keySet()) {
            final boolean isSelected = commandPermissions.get(permissionCode).booleanValue();

            final Permission permission = findPermissionByCode(allPermissions, permissionCode);
            boolean changed = role.updatePermission(permission, isSelected);
            if (changed) {
                changedPermissions.put(permissionCode, isSelected);
            }
        }

        if (!changedPermissions.isEmpty()) {
            changes.put("permissions", changedPermissions);
            this.roleRepository.save(role);
        }
        
        if (this.configurationDomainService.isMakerCheckerEnabledForTask("PERMISSIONS_ROLE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
        
        return EntityIdentifier.withChanges(role.getId(), changes);
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