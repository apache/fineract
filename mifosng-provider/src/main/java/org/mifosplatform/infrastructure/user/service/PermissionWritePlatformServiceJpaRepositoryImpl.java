package org.mifosplatform.infrastructure.user.service;

import java.util.Collection;
import java.util.Map;

import org.mifosng.platform.api.commands.PermissionsCommand;
import org.mifosng.platform.exceptions.PermissionNotFoundException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.Permission;
import org.mifosplatform.infrastructure.user.domain.PermissionRepository;
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
    public Long updateMakerCheckerPermissions(final PermissionsCommand command) {
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
        }

        return new Long(0);
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