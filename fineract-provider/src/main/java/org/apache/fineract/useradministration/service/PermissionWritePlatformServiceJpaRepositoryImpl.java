/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.useradministration.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.command.PermissionsCommand;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.exception.PermissionNotFoundException;
import org.apache.fineract.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
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