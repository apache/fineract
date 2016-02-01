/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.exception.PermissionNotFoundException;
import org.mifosplatform.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PermissionWritePlatformServiceJpaRepositoryImpl implements PermissionWritePlatformService {

    private final PlatformSecurityContext context;
    private final PermissionRepository permissionRepository;
    private final PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public PermissionWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final PermissionRepository permissionRepository, final PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.permissionRepository = permissionRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    @Transactional
    @Override
    public CommandProcessingResult updateMakerCheckerPermissions(final JsonCommand command) {
        this.context.authenticatedUser();

        final Collection<Permission> allPermissions = this.permissionRepository.findAll();

        final PermissionsCommand permissionsCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());

        final Map<String, Boolean> commandPermissions = permissionsCommand.getPermissions();
        final Map<String, Object> changes = new HashMap<>();
        final Map<String, Boolean> changedPermissions = new HashMap<>();
        for (final String permissionCode : commandPermissions.keySet()) {

            final Permission permission = findPermissionInCollectionByCode(allPermissions, permissionCode);

            if (permission.getCode().endsWith("_CHECKER") || permission.getCode().startsWith("READ_")
                    || permission.getGrouping().equalsIgnoreCase("special")) { throw new PermissionNotFoundException(permissionCode); }

            final boolean isSelected = commandPermissions.get(permissionCode).booleanValue();
            final boolean changed = permission.enableMakerChecker(isSelected);
            if (changed) {
                changedPermissions.put(permissionCode, isSelected);
                this.permissionRepository.save(permission);
            }
        }

        if (!changedPermissions.isEmpty()) {
            changes.put("permissions", changedPermissions);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).build();
    }

    private Permission findPermissionInCollectionByCode(final Collection<Permission> allPermissions, final String permissionCode) {

        if (allPermissions != null) {
            for (final Permission permission : allPermissions) {
                if (permission.hasCode(permissionCode)) { return permission; }
            }
        }

        throw new PermissionNotFoundException(permissionCode);
    }
}