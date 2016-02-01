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
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.command.PermissionsCommand;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mifosplatform.useradministration.domain.Role;
import org.mifosplatform.useradministration.domain.RoleRepository;
import org.mifosplatform.useradministration.exception.PermissionNotFoundException;
import org.mifosplatform.useradministration.exception.RoleAssociatedException;
import org.mifosplatform.useradministration.exception.RoleNotFoundException;
import org.mifosplatform.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleWritePlatformServiceJpaRepositoryImpl implements RoleWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(RoleWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleDataValidator roleCommandFromApiJsonDeserializer;
    private final PermissionsCommandFromApiJsonDeserializer permissionsFromApiJsonDeserializer;

    @Autowired
    public RoleWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final RoleRepository roleRepository,
            final PermissionRepository permissionRepository, final RoleDataValidator roleCommandFromApiJsonDeserializer,
            final PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.roleCommandFromApiJsonDeserializer = roleCommandFromApiJsonDeserializer;
        this.permissionsFromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult createRole(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.roleCommandFromApiJsonDeserializer.validateForCreate(command.json());

            final Role entity = Role.fromJson(command);
            this.roleRepository.save(entity);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(entity.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("unq_name")) {

            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.role.duplicate.name", "Role with name `" + name + "` already exists",
                    "name", name);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.role.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    @Transactional
    @Override
    public CommandProcessingResult updateRole(final Long roleId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.roleCommandFromApiJsonDeserializer.validateForUpdate(command.json());

            final Role role = this.roleRepository.findOne(roleId);
            if (role == null) { throw new RoleNotFoundException(roleId); }

            final Map<String, Object> changes = role.update(command);
            if (!changes.isEmpty()) {
                this.roleRepository.saveAndFlush(role);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(roleId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    @Transactional
    @Override
    public CommandProcessingResult updateRolePermissions(final Long roleId, final JsonCommand command) {
        this.context.authenticatedUser();

        final Role role = this.roleRepository.findOne(roleId);
        if (role == null) { throw new RoleNotFoundException(roleId); }

        final Collection<Permission> allPermissions = this.permissionRepository.findAll();

        final PermissionsCommand permissionsCommand = this.permissionsFromApiJsonDeserializer.commandFromApiJson(command.json());

        final Map<String, Boolean> commandPermissions = permissionsCommand.getPermissions();
        final Map<String, Object> changes = new HashMap<>();
        final Map<String, Boolean> changedPermissions = new HashMap<>();
        for (final String permissionCode : commandPermissions.keySet()) {
            final boolean isSelected = commandPermissions.get(permissionCode).booleanValue();

            final Permission permission = findPermissionByCode(allPermissions, permissionCode);
            final boolean changed = role.updatePermission(permission, isSelected);
            if (changed) {
                changedPermissions.put(permissionCode, isSelected);
            }
        }

        if (!changedPermissions.isEmpty()) {
            changes.put("permissions", changedPermissions);
            this.roleRepository.save(role);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(roleId) //
                .with(changes) //
                .build();
    }

    private Permission findPermissionByCode(final Collection<Permission> allPermissions, final String permissionCode) {

        if (allPermissions != null) {
            for (final Permission permission : allPermissions) {
                if (permission.hasCode(permissionCode)) { return permission; }
            }
        }
        throw new PermissionNotFoundException(permissionCode);
    }

    /**
     * Method for Delete Role
     */
    @Transactional
    @Override
    public CommandProcessingResult deleteRole(Long roleId) {

        try {
            /**
             * Checking the role present in DB or not using role_id
             */
            final Role role = this.roleRepository.findOne(roleId);
            if (role == null) { throw new RoleNotFoundException(roleId); }
            
            /**
             * Roles associated with users can't be deleted
             */
            final Integer count = this.roleRepository.getCountOfRolesAssociatedWithUsers(roleId);
            if (count > 0) { throw new RoleAssociatedException("error.msg.role.associated.with.users.deleted", roleId); }
            
            this.roleRepository.delete(role);
            return new CommandProcessingResultBuilder().withEntityId(roleId).build();
        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }

    /**
     * Method for disabling the role
     */
    @Transactional
    @Override
    public CommandProcessingResult disableRole(Long roleId) {
        try {
            /**
             * Checking the role present in DB or not using role_id
             */
            final Role role = this.roleRepository.findOne(roleId);
            if (role == null) { throw new RoleNotFoundException(roleId); }
            //if(role.isDisabled()){throw new RoleNotFoundException(roleId);}
            
            /**
             * Roles associated with users can't be disable
             */
            final Integer count = this.roleRepository.getCountOfRolesAssociatedWithUsers(roleId);
            if (count > 0) { throw new RoleAssociatedException("error.msg.role.associated.with.users.disabled", roleId); }
            
            /**
             * Disabling the role
             */
            role.disableRole();
            this.roleRepository.save(role);
            return new CommandProcessingResultBuilder().withEntityId(roleId).build();

        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }

    /**
     * Method for Enabling the role
     */
    @Transactional
    @Override
    public CommandProcessingResult enableRole(Long roleId) {
        try {
            /**
             * Checking the role present in DB or not using role_id
             */
            final Role role = this.roleRepository.findOne(roleId);
            if (role == null) { throw new RoleNotFoundException(roleId); }
            //if(!role.isEnabled()){throw new RoleNotFoundException(roleId);}
            
            role.enableRole();
            this.roleRepository.save(role);
            return new CommandProcessingResultBuilder().withEntityId(roleId).build();

        } catch (final DataIntegrityViolationException e) {
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource: " + e.getMostSpecificCause());
        }
    }
}