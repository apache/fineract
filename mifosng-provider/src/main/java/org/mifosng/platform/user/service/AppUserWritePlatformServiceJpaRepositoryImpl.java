package org.mifosng.platform.user.service;

import static org.mifosng.platform.Specifications.usersThatMatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ApiParameterError;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
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
			AppUser currentUser = context.authenticatedUser();
			
			UserCommandValidator validator = new UserCommandValidator(command);
			validator.validateForCreate();
			
			Set<Role> allRoles = assembleSetOfRoles(command.getRoles());

			Office office = this.officeRepository.findOne(command.getOfficeId());

			String password = command.getPassword();
			if (StringUtils.isBlank(password)) {
				password = "autogenerate";
			}
			
	        AppUser appUser = AppUser.createNew(currentUser.getOrganisation(), office, 
	        		allRoles, command.getUsername(), command.getEmail(), 
	        		command.getFirstname(), command.getLastname(), 
	        		password);
			
			this.userDomainService.create(appUser);
			
			return appUser.getId();
		} catch (DataIntegrityViolationException dve) {
			handleOfficeDataIntegrityIssues(command, dve);
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
	public Long updateUser(UserCommand command) {
		
		try {
			AppUser currentUser = context.authenticatedUser();
			
			UserCommandValidator validator = new UserCommandValidator(command);
			validator.validateForUpdate();
			
			Set<Role> allRoles = assembleSetOfRoles(command.getRoles());

			Office office = null;
			if (command.getOfficeId() != null) {
				office = this.officeRepository.findOne(command.getOfficeId());
				if (office == null) {
					throw new PlatformResourceNotFoundException("error.msg.office.id.invalid", "Office with identifier {0} does not exist.", command.getOfficeId());
				}
			}

			AppUser userToUpdate = this.appUserRepository.findOne(usersThatMatch(currentUser.getOrganisation(), command.getId()));
			if (userToUpdate == null) {
				throw new PlatformResourceNotFoundException("error.msg.user.id.invalid", "User with identifier {0} does not exist.", command.getId());
			}
			
			userToUpdate.update(allRoles, office, command.getUsername(), command.getFirstname(), command.getLastname(), command.getEmail());
			this.appUserRepository.saveAndFlush(userToUpdate);
			
			if (command.getPassword() != null || command.getRepeatPassword() != null) {
				PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser(
						userToUpdate.getId(),
						userToUpdate.getUsername(), command.getPassword());

				String newPasswordEncoded = this.platformPasswordEncoder.encode(dummyPlatformUser);
				
				userToUpdate.updatePasswordOnFirstTimeLogin(newPasswordEncoded);
				this.appUserRepository.saveAndFlush(userToUpdate);
			}
			
			return userToUpdate.getId();
		} catch (DataIntegrityViolationException dve) {
			handleOfficeDataIntegrityIssues(command, dve);
			return Long.valueOf(-1);
		}
	}
	
	private Set<Role> assembleSetOfRoles(final String[] rolesArray) {
		Set<Role> allRoles = new HashSet<Role>();
		if (!ObjectUtils.isEmpty(rolesArray)) {
			for (String roleId : rolesArray) {
				Role role = this.roleRepository.findOne(Long.valueOf(roleId));
				allRoles.add(role);
			}
		}
		return allRoles;
	}
	
	@Transactional
	@Override
	public void deleteUser(Long userId) {
		this.appUserRepository.delete(userId);
	}
	
	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleOfficeDataIntegrityIssues(final UserCommand command, DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("username_org")) {
			throw new PlatformDataIntegrityException("error.msg.user.duplicate.username", "User with username {0} already exists", "username", command.getUsername());
		}
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}
}