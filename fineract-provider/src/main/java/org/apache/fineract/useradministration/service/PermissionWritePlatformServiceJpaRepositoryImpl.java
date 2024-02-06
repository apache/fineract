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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.command.PermissionsCommand;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.exception.PermissionNotFoundException;
import org.apache.fineract.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class PermissionWritePlatformServiceJpaRepositoryImpl implements PermissionWritePlatformService {

    private final PlatformSecurityContext context;
    private final PermissionRepository permissionRepository;
    private final PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer;

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
        for (Map.Entry<String, Boolean> entry : commandPermissions.entrySet()) {

            final Permission permission = findPermissionInCollectionByCode(allPermissions, entry.getKey());

            if (permission.getCode().endsWith("_CHECKER") || permission.getCode().startsWith("READ_")
                    || permission.getGrouping().equalsIgnoreCase("special")) {
                throw new PermissionNotFoundException(entry.getKey());
            }

            final boolean isSelected = entry.getValue();
            final boolean changed = permission.enableMakerChecker(isSelected);
            if (changed) {
                changedPermissions.put(entry.getKey(), isSelected);
                this.permissionRepository.saveAndFlush(permission);
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
                if (permission.hasCode(permissionCode)) {
                    return permission;
                }
            }
        }

        throw new PermissionNotFoundException(permissionCode);
    }
}
