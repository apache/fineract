package org.mifosng.platform.user.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.exceptions.OfficeNotFoundException;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.exceptions.RoleNotFoundException;
import org.mifosng.platform.exceptions.UserNotFoundException;
import org.mifosng.platform.infrastructure.BasicPasswordEncodablePlatformUser;
import org.mifosng.platform.infrastructure.PlatformEmailSendException;
import org.mifosng.platform.infrastructure.PlatformPasswordEncoder;
import org.mifosng.platform.infrastructure.PlatformUser;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.mifosng.platform.user.domain.Role;
import org.mifosng.platform.user.domain.RoleRepository;
import org.mifosng.platform.user.domain.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class AppUserWritePlatformServiceJpaRepositoryImpl implements AppUserWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(AppUserWritePlatformServiceJpaRepositoryImpl.class);
	
	private final PlatformSecurityContext context;
	private final UserDomainService userDomainService;
	private final PlatformPasswordEncoder platformPasswordEncoder;
	private final AppUserRepository appUserRepository;
	private final OfficeRepository officeRepository;
	private final RoleRepository roleRepository;
	
	@Autowired
	public AppUserWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final AppUserRepository appUserRepository, final UserDomainService userDomainService,
			final OfficeRepository officeRepository, final RoleRepository roleRepository, final PlatformPasswordEncoder platformPasswordEncoder) {
		this.context = context;
		this.appUserRepository = appUserRepository;
		this.userDomainService = userDomainService;
		this.officeRepository = officeRepository;
		this.roleRepository = roleRepository;
		this.platformPasswordEncoder = platformPasswordEncoder;
	}
	
	@Transactional
	@Override
	public Long createUser(final UserCommand command) {
		
		try {
			context.authenticatedUser();
			
			UserCommandValidator validator = new UserCommandValidator(command);
			validator.validateForCreate();
			
			final Set<Role> allRoles = assembleSetOfRoles(command);

			Office office = this.officeRepository.findOne(command.getOfficeId());
			if (office == null) {
				throw new OfficeNotFoundException(command.getOfficeId());
			}

			String password = command.getPassword();
			if (StringUtils.isBlank(password)) {
				password = "autogenerate";
			}
			
	        AppUser appUser = AppUser.createNew(office, 
	        		allRoles, command.getUsername(), command.getEmail(), 
	        		command.getFirstname(), command.getLastname(), 
	        		password);
			
			this.userDomainService.create(appUser);
			
			return appUser.getId();
		} catch (DataIntegrityViolationException dve) {
			handleAppUserDataIntegrityIssues(command, dve);
			return Long.valueOf(-1);
		} catch (PlatformEmailSendException e) {
			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError("error.msg.user.email.invalid", "The parameter email is invalid.", "email", command.getEmail());
			dataValidationErrors.add(error);
			
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);			
		}
	}

	@Transactional
	@Override
	public Long updateUser(final UserCommand command) {
		
		try {
			context.authenticatedUser();
			
			UserCommandValidator validator = new UserCommandValidator(command);
			validator.validateForUpdate();
			
			final Set<Role> allRoles = assembleSetOfRoles(command);

			Office office = null;
			if (command.isOfficeChanged()) {
				office = this.officeRepository.findOne(command.getOfficeId());
				if (office == null) {
					throw new OfficeNotFoundException(command.getOfficeId());
				}
			}

			AppUser userToUpdate = this.appUserRepository.findOne(command.getId());
			if (userToUpdate == null) {
				throw new UserNotFoundException(command.getId());
			}
			
			userToUpdate.update(allRoles, office, command);
			this.appUserRepository.saveAndFlush(userToUpdate);
			
			if (command.isPasswordChanged()) {
				PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser(
						userToUpdate.getId(),
						userToUpdate.getUsername(), command.getPassword());

				String newPasswordEncoded = this.platformPasswordEncoder.encode(dummyPlatformUser);
				
				userToUpdate.updatePasswordOnFirstTimeLogin(newPasswordEncoded);
				this.appUserRepository.saveAndFlush(userToUpdate);
			}
			
			return userToUpdate.getId();
		} catch (DataIntegrityViolationException dve) {
			handleAppUserDataIntegrityIssues(command, dve);
			return Long.valueOf(-1);
		}
	}
	
	@Transactional
	@Override
	public void updateUsersOwnAccountDetails(final UserCommand command) {
		updateUser(command);
	}
	
	private Set<Role> assembleSetOfRoles(final UserCommand command) {
		
		final Set<Role> allRoles = new HashSet<Role>();
		
		String[] rolesArray = command.getRoles();
		if (command.isRolesChanged() && !ObjectUtils.isEmpty(rolesArray)) {
			for (String roleId : rolesArray) {
				Long id = Long.valueOf(roleId);
				Role role = this.roleRepository.findOne(id);
				if (role == null) {
					throw new RoleNotFoundException(id);
				}
				allRoles.add(role);
			}
		}
		
		return allRoles;
	}
	
	// FIXME - KW - move this to a soft delete of user marking a is_deleted field as true so doesnt appear in results
	// more than making a user disabled, locked?
	@Transactional
	@Override
	public void deleteUser(final Long userId) {
		try {
			this.appUserRepository.delete(userId);
		} catch (InvalidDataAccessApiUsageException exception) {
			// spring data jpa throws this when deleting user that doesnt exist
			throw new UserNotFoundException(userId);
		}
	}
	
	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleAppUserDataIntegrityIssues(final UserCommand command, final DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("username_org")) {
			StringBuilder defaultMessageBuilder = new StringBuilder("User with username ").append(command.getUsername()).append(" already exists.");
			throw new PlatformDataIntegrityException("error.msg.user.duplicate.username", defaultMessageBuilder.toString(), "username", command.getUsername());
		}
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}
}