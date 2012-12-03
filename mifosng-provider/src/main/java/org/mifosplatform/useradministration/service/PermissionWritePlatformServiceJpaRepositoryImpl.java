package org.mifosplatform.useradministration.service;

import java.util.Collection;
import java.util.Map;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.exception.PermissionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionWritePlatformServiceJpaRepositoryImpl implements PermissionWritePlatformService {

    private final PlatformSecurityContext context;
    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final PermissionRepository permissionRepository) {
        this.context = context;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    @Override
    public void updateMakerCheckerPermissions(final PermissionsCommand command) {
        context.authenticatedUser();

        final Collection<Permission> allPermissions = this.permissionRepository.findAll();

        final Map<String, Boolean> commandPermissions = command.getPermissions();
        for (final String permissionCode : commandPermissions.keySet()) {

            final Permission permission = findPermissionByCode(allPermissions, permissionCode);

            if (permission.getCode().endsWith("_CHECKER") || permission.getCode().startsWith("READ_")
                    || permission.getGrouping().equalsIgnoreCase("special")) { throw new PermissionNotFoundException(permissionCode); }

            final boolean isSelected = commandPermissions.get(permissionCode).booleanValue();
            permission.enableMakerChecker(isSelected);

            this.permissionRepository.save(permission);
            
            // Typical rollback if maker-checker enabled not here - does it make sense to have a maker checker task for enabling maker-checker for other tasks?
        }
    }

    private Permission findPermissionByCode(Collection<Permission> allPermissions, String permissionCode) {

        if (allPermissions != null) {
            for (Permission permission : allPermissions) {
                if (permission.hasCode(permissionCode)) { return permission; }
            }
        }

        throw new PermissionNotFoundException(permissionCode);
    }
}