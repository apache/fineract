package org.mifosplatform.useradministration.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.PlatformEmailSendException;
import org.mifosplatform.infrastructure.security.service.PlatformPasswordEncoder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.service.ConfigurationDomainService;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.command.UserCommand;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.domain.AppUserRepository;
import org.mifosplatform.useradministration.domain.Role;
import org.mifosplatform.useradministration.domain.RoleRepository;
import org.mifosplatform.useradministration.domain.UserDomainService;
import org.mifosplatform.useradministration.exception.RoleNotFoundException;
import org.mifosplatform.useradministration.exception.UserNotFoundException;
import org.mifosplatform.useradministration.serialization.UserCommandFromApiJsonDeserializer;
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
    private final ConfigurationDomainService configurationDomainService;
    private final UserCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public AppUserWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final AppUserRepository appUserRepository,
            final UserDomainService userDomainService, final OfficeRepository officeRepository, final RoleRepository roleRepository,
            final PlatformPasswordEncoder platformPasswordEncoder, final ConfigurationDomainService configurationDomainService,
            final UserCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.appUserRepository = appUserRepository;
        this.userDomainService = userDomainService;
        this.officeRepository = officeRepository;
        this.roleRepository = roleRepository;
        this.platformPasswordEncoder = platformPasswordEncoder;
        this.configurationDomainService = configurationDomainService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public Long createUser(final JsonCommand command) {

        try {
            context.authenticatedUser();

            final UserCommand userCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            userCommand.validateForCreate();

            final String officeIdParamName = "officeId";
            final Long officeId = command.longValueOfParameterNamed(officeIdParamName);

            final Office userOffice = this.officeRepository.findOne(officeId);
            if (userOffice == null) { throw new OfficeNotFoundException(officeId); }

            final String[] roles = command.arrayValueOfParameterNamed("roles");
            final Set<Role> allRoles = assembleSetOfRoles(roles);

            final AppUser appUser = AppUser.fromJson(userOffice, allRoles, command);
            this.userDomainService.create(appUser, command.isApprovedByChecker());

            return appUser.getId();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        } catch (PlatformEmailSendException e) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

            final String email = command.stringValueOfParameterNamed("email");
            ApiParameterError error = ApiParameterError.parameterError("error.msg.user.email.invalid", "The parameter email is invalid.",
                    "email", email);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    @Transactional
    @Override
    public EntityIdentifier updateUser(final Long userId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            final UserCommand userCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            userCommand.validateForUpdate();

            final AppUser userToUpdate = this.appUserRepository.findOne(userId);
            if (userToUpdate == null) { throw new UserNotFoundException(userId); }

            final Map<String, Object> changes = userToUpdate.update(command, this.platformPasswordEncoder);

            if (changes.containsKey("officeId")) {
                final Long officeId = (Long) changes.get("officeId");
                final Office office = this.officeRepository.findOne(officeId);
                if (office == null) { throw new OfficeNotFoundException(officeId); }

                userToUpdate.changeOffice(office);
            }

            if (changes.containsKey("roles")) {
                final String[] roleIds = (String[]) changes.get("roles");
                final Set<Role> allRoles = assembleSetOfRoles(roleIds);

                userToUpdate.updateRoles(allRoles);
            }

            if (!changes.isEmpty()) {
                this.appUserRepository.save(userToUpdate);
            }

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_USER") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return EntityIdentifier.withChanges(userId, changes);
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return EntityIdentifier.empty();
        }
    }

    /**
     * Different between this and <code>updateUser</code> is that we dont do
     * maker-checker flow or require that user have particular permission to
     * change their own details.
     */
    @Transactional
    @Override
    public EntityIdentifier updateUsersOwnAccountDetails(final Long userId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            final UserCommand userCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            userCommand.validateForUpdate();

            final AppUser userToUpdate = this.appUserRepository.findOne(userId);
            if (userToUpdate == null) { throw new UserNotFoundException(userId); }

            final Map<String, Object> changes = userToUpdate.update(command, this.platformPasswordEncoder);

            if (changes.containsKey("officeId")) {
                final Long officeId = (Long) changes.get("officeId");
                final Office office = this.officeRepository.findOne(officeId);
                if (office == null) { throw new OfficeNotFoundException(officeId); }

                userToUpdate.changeOffice(office);
            }

            if (changes.containsKey("roles")) {
                final String[] roleIds = (String[]) changes.get("roles");
                final Set<Role> allRoles = assembleSetOfRoles(roleIds);

                userToUpdate.updateRoles(allRoles);
            }

            if (!changes.isEmpty()) {
                this.appUserRepository.saveAndFlush(userToUpdate);
            }

            return EntityIdentifier.withChanges(userId, changes);
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return EntityIdentifier.empty();
        }
    }

    private Set<Role> assembleSetOfRoles(final String[] rolesArray) {

        final Set<Role> allRoles = new HashSet<Role>();

        if (!ObjectUtils.isEmpty(rolesArray)) {
            for (String roleId : rolesArray) {
                final Long id = Long.valueOf(roleId);
                final Role role = this.roleRepository.findOne(id);
                if (role == null) { throw new RoleNotFoundException(id); }
                allRoles.add(role);
            }
        }

        return allRoles;
    }

    @Transactional
    @Override
    public void deleteUser(final Long userId, final JsonCommand command) {

        final AppUser user = this.appUserRepository.findOne(userId);
        if (user == null || user.isDeleted()) { throw new UserNotFoundException(userId); }

        user.delete();
        this.appUserRepository.save(user);

        if (this.configurationDomainService.isMakerCheckerEnabledForTask("DELETE_USER") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("username_org")) {
            final String username = command.stringValueOfParameterNamed("username");
            StringBuilder defaultMessageBuilder = new StringBuilder("User with username ").append(username).append(" already exists.");
            throw new PlatformDataIntegrityException("error.msg.user.duplicate.username", defaultMessageBuilder.toString(), "username",
                    username);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }
}