package org.mifosng.platform.user.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mifosng.platform.accounting.api.commands.RolePermissionCommand;
import org.mifosng.platform.api.commands.RoleCommand;
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
import org.springframework.util.ObjectUtils;

@Service
public class RoleWritePlatformServiceJpaRepositoryImpl implements
		RoleWritePlatformService {

	private final PlatformSecurityContext context;
	private final RoleRepository roleRepository;

	private final PermissionRepository permissionRepository;

	@Autowired
	public RoleWritePlatformServiceJpaRepositoryImpl(
			final PlatformSecurityContext context,
			final RoleRepository roleRepository,
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

		List<Permission> selectedPermissions = assembleListOfSelectedPermissions(command
				.getPermissions());

		Role entity = new Role(command.getName(), command.getDescription(),
				selectedPermissions);

		this.roleRepository.save(entity);

		return entity.getId();
	}

	@Transactional
	@Override
	public Long updateRole(RoleCommand command) {

		context.authenticatedUser();

		RoleCommandValidator validator = new RoleCommandValidator(command);
		validator.validateForUpdate();

		List<Permission> selectedPermissions = assembleListOfSelectedPermissions(command
				.getPermissions());

		Role role = this.roleRepository.findOne(command.getId());
		if (role == null) {
			throw new RoleNotFoundException(command.getId());
		}
		role.update(command, selectedPermissions);

		this.roleRepository.save(role);

		return role.getId();
	}

	@Transactional
	@Override
	public Long updateRolePermissions(final RolePermissionCommand command) {
		context.authenticatedUser();

		final Role role = this.roleRepository.findOne(command.getRoleId());
		if (role == null) {
			throw new RoleNotFoundException(command.getRoleId());
		}

		Collection<Permission> allPermissions = this.permissionRepository
				.findAll();

		Map<String, Boolean> commandPermissions = command.getPermissions();
		for (String permissionCode : commandPermissions.keySet()) {
			Boolean selected = commandPermissions.get(permissionCode);

			Permission permission = getPermissionByCode(allPermissions,
					permissionCode);

			if (role.getPermissions().contains(permission)) {
				if (!(selected)) {
					role.getPermissions().remove(permission);
				}

			} else {
				if (selected) {
					role.getPermissions().add(permission);
				}

			}
		}

		this.roleRepository.save(role);

		return role.getId();
	}

	private Permission getPermissionByCode(
			Collection<Permission> allPermissions, String permissionCode) {
				
		if (allPermissions != null) {
			for (Permission permission : allPermissions) {
				if (permission.code().equals(permissionCode)) return permission;
				
			}
		}

		throw new PermissionNotFoundException(permissionCode);
	}

	private List<Permission> assembleListOfSelectedPermissions(
			final String[] selectedPermissionsArray) {
		List<Long> selectedPermissionIds = new ArrayList<Long>();
		List<Permission> selectedPermissions = new ArrayList<Permission>();

		if (!ObjectUtils.isEmpty(selectedPermissionsArray)) {
			for (String selectedId : selectedPermissionsArray) {
				selectedPermissionIds.add(Long.valueOf(selectedId));
			}

			Collection<Permission> allPermissions = this.permissionRepository
					.findAll();
			for (Permission permission : allPermissions) {
				if (selectedPermissionIds.contains(permission.getId())) {
					selectedPermissions.add(permission);
				}
			}
		}
		return selectedPermissions;
	}
}