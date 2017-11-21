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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.PlatformEmailSendException;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.domain.Topic;
import org.apache.fineract.notification.domain.TopicRepository;
import org.apache.fineract.notification.domain.TopicSubscriber;
import org.apache.fineract.notification.domain.TopicSubscriberRepository;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.api.AppUserApiConstant;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserPreviousPassword;
import org.apache.fineract.useradministration.domain.AppUserPreviousPasswordRepository;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.apache.fineract.useradministration.exception.PasswordPreviouslyUsedException;
import org.apache.fineract.useradministration.exception.RoleNotFoundException;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@Service
public class AppUserWritePlatformServiceJpaRepositoryImpl implements AppUserWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AppUserWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final UserDomainService userDomainService;
    private final PlatformPasswordEncoder platformPasswordEncoder;
    private final AppUserRepository appUserRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final RoleRepository roleRepository;
    private final UserDataValidator fromApiJsonDeserializer;
    private final AppUserPreviousPasswordRepository appUserPreviewPasswordRepository;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final TopicRepository topicRepository;
    private final TopicSubscriberRepository topicSubscriberRepository;

    @Autowired
    public AppUserWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final AppUserRepository appUserRepository,
            final UserDomainService userDomainService, final OfficeRepositoryWrapper officeRepositoryWrapper, final RoleRepository roleRepository,
            final PlatformPasswordEncoder platformPasswordEncoder, final UserDataValidator fromApiJsonDeserializer,
            final AppUserPreviousPasswordRepository appUserPreviewPasswordRepository, final StaffRepositoryWrapper staffRepositoryWrapper,
            final ClientRepositoryWrapper clientRepositoryWrapper, final TopicRepository topicRepository,
            final TopicSubscriberRepository topicSubscriberRepository) {
        this.context = context;
        this.appUserRepository = appUserRepository;
        this.userDomainService = userDomainService;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.roleRepository = roleRepository;
        this.platformPasswordEncoder = platformPasswordEncoder;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.appUserPreviewPasswordRepository = appUserPreviewPasswordRepository;
        this.staffRepositoryWrapper = staffRepositoryWrapper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.topicRepository = topicRepository;
        this.topicSubscriberRepository = topicSubscriberRepository;
    }

    @Transactional
    @Override
    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    public CommandProcessingResult createUser(final JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final String officeIdParamName = "officeId";
            final Long officeId = command.longValueOfParameterNamed(officeIdParamName);

            final Office userOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);

            final String[] roles = command.arrayValueOfParameterNamed("roles");
            final Set<Role> allRoles = assembleSetOfRoles(roles);

            AppUser appUser;

            final String staffIdParamName = "staffId";
            final Long staffId = command.longValueOfParameterNamed(staffIdParamName);

            Staff linkedStaff = null;
            if (staffId != null) {
                linkedStaff = this.staffRepositoryWrapper.findByOfficeWithNotFoundDetection(staffId, userOffice.getId());
            }
            
            Collection<Client> clients = null;
            if(command.hasParameter(AppUserConstants.IS_SELF_SERVICE_USER)
            		&& command.booleanPrimitiveValueOfParameterNamed(AppUserConstants.IS_SELF_SERVICE_USER)
            		&& command.hasParameter(AppUserConstants.CLIENTS)){
            	JsonArray clientsArray = command.arrayOfParameterNamed(AppUserConstants.CLIENTS);
            	Collection<Long> clientIds = new HashSet<>();
            	for(JsonElement clientElement : clientsArray){
            		clientIds.add(clientElement.getAsLong());
            	}
            	clients = this.clientRepositoryWrapper.findAll(clientIds);
            }

            appUser = AppUser.fromJson(userOffice, linkedStaff, allRoles, clients, command);

            final Boolean sendPasswordToEmail = command.booleanObjectValueOfParameterNamed("sendPasswordToEmail");
            this.userDomainService.create(appUser, sendPasswordToEmail);
            List<Topic> possibleTopics = topicRepository.findByEntityId(appUser.getOffice().getId());
            
            if (!possibleTopics.isEmpty()) {
            	Set<Role> userRoles = appUser.getRoles();
            	for (Role curRole : userRoles) {
            		for (Topic curTopic : possibleTopics) {
            			if(curRole.getName().compareToIgnoreCase(curTopic.getMemberType()) == 0) {
            				TopicSubscriber topicSubscriber = new TopicSubscriber(curTopic, appUser, new Date());
            				topicSubscriberRepository.save(topicSubscriber);
            			}
            		}
            	}
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(appUser.getId()) //
                    .withOfficeId(userOffice.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException | AuthenticationServiceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }catch (final PlatformEmailSendException e) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

            final String email = command.stringValueOfParameterNamed("email");
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.user.email.invalid",
                    "The parameter email is invalid.", "email", email);
            dataValidationErrors.add(error);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    @Transactional
    @Override
    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    public CommandProcessingResult updateUser(final Long userId, final JsonCommand command) {

        try {

            this.context.authenticatedUser(new CommandWrapperBuilder().updateUser(null).build());

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final AppUser userToUpdate = this.appUserRepository.findOne(userId);

            if (userToUpdate == null) { throw new UserNotFoundException(userId); }

            final AppUserPreviousPassword currentPasswordToSaveAsPreview = getCurrentPasswordToSaveAsPreview(userToUpdate, command);
            
            Collection<Client> clients = null;
            boolean isSelfServiceUser = userToUpdate.isSelfServiceUser();
            if(command.hasParameter(AppUserConstants.IS_SELF_SERVICE_USER)){
            	isSelfServiceUser = command.booleanPrimitiveValueOfParameterNamed(AppUserConstants.IS_SELF_SERVICE_USER); 
            }
            
            if(isSelfServiceUser && command.hasParameter(AppUserConstants.CLIENTS)){
            	JsonArray clientsArray = command.arrayOfParameterNamed(AppUserConstants.CLIENTS);
            	Collection<Long> clientIds = new HashSet<>();
            	for(JsonElement clientElement : clientsArray){
            		clientIds.add(clientElement.getAsLong());
            	}
            	clients = this.clientRepositoryWrapper.findAll(clientIds);
            }

            final Map<String, Object> changes = userToUpdate.update(command, this.platformPasswordEncoder, clients);

            if (changes.containsKey("officeId")) {
                final Long oldOfficeId = userToUpdate.getOffice().getId();
                final Long newOfficeId = (Long) changes.get("officeId");
                final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(newOfficeId);
                userToUpdate.changeOffice(office);

                List<Topic> oldTopics = topicRepository.findByEntityId(oldOfficeId);
                List<Topic> newTopics = topicRepository.findByEntityId(newOfficeId);
                
                List<TopicSubscriber> oldSubscriptions = topicSubscriberRepository.findBySubscriber(userToUpdate);
                for (TopicSubscriber subscriber : oldSubscriptions) {
                	for (Topic topic : oldTopics) {
                		if (subscriber.getTopic().getId() == topic.getId()) {
                			topicSubscriberRepository.delete(subscriber);
                		}
                	}
                }
                
                Set<Role> userRoles = userToUpdate.getRoles();
            	for (Role curRole : userRoles) {
            		for (Topic curTopic : newTopics) {
            			if (curRole.getName().compareToIgnoreCase(curTopic.getMemberType()) == 0) {
            				TopicSubscriber newSubscription = new TopicSubscriber(curTopic, userToUpdate, new Date());
            				topicSubscriberRepository.save(newSubscription);
            			}
            		}
            	}
            }

            if (changes.containsKey("staffId")) {
                final Long staffId = (Long) changes.get("staffId");
                Staff linkedStaff = null;
                if (staffId != null) {
                    linkedStaff = this.staffRepositoryWrapper.findByOfficeWithNotFoundDetection(staffId, userToUpdate.getOffice().getId());
                }
                userToUpdate.changeStaff(linkedStaff);
            }

            if (changes.containsKey("roles")) {
                final String[] roleIds = (String[]) changes.get("roles");
                final Set<Role> oldRoles = userToUpdate.getRoles() ;
                final Set<Role> tempOldRoles = new HashSet<>(oldRoles);
                final Set<Role> updatedRoles = assembleSetOfRoles(roleIds);
                final Set<Role> tempUpdatedRoles = new HashSet<>(updatedRoles);
                
                tempOldRoles.removeAll(updatedRoles);
                List<TopicSubscriber> oldSubscriptions = topicSubscriberRepository.findBySubscriber(userToUpdate);
                for (TopicSubscriber subscriber : oldSubscriptions) {
                	Topic topic = subscriber.getTopic();
                	for (Role role : tempOldRoles) {
                		if (role.getName().compareToIgnoreCase(topic.getMemberType()) == 0) {
                			topicSubscriberRepository.delete(subscriber);
                		}
                	}
                }
                
                tempUpdatedRoles.removeAll(oldRoles);
                List<Topic> newTopics = topicRepository.findByEntityId(userToUpdate.getOffice().getId());
                for (Topic topic : newTopics) {
                	for (Role role : tempUpdatedRoles) {
                		if (role.getName().compareToIgnoreCase(topic.getMemberType()) == 0) {
                			TopicSubscriber topicSubscriber = new TopicSubscriber(topic, userToUpdate, new Date());
            				topicSubscriberRepository.save(topicSubscriber);
                		}
                	}
                }
                
                userToUpdate.updateRoles(updatedRoles);
            }

            if (!changes.isEmpty()) {
                this.appUserRepository.saveAndFlush(userToUpdate);

                if (currentPasswordToSaveAsPreview != null) {
                    this.appUserPreviewPasswordRepository.save(currentPasswordToSaveAsPreview);
                }

            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(userId) //
                    .withOfficeId(userToUpdate.getOffice().getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException | AuthenticationServiceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .build();
        }
    }

    /**
     * encode the new submitted password retrieve the last n used password check
     * if the current submitted password, match with one of them
     * 
     * @param user
     * @param command
     * @return
     */
    private AppUserPreviousPassword getCurrentPasswordToSaveAsPreview(final AppUser user, final JsonCommand command) {

        final String passWordEncodedValue = user.getEncodedPassword(command, this.platformPasswordEncoder);

        AppUserPreviousPassword currentPasswordToSaveAsPreview = null;

        if (passWordEncodedValue != null) {

            PageRequest pageRequest = new PageRequest(0, AppUserApiConstant.numberOfPreviousPasswords, Sort.Direction.DESC, "removalDate");

            final List<AppUserPreviousPassword> nLastUsedPasswords = this.appUserPreviewPasswordRepository.findByUserId(user.getId(),
                    pageRequest);

            for (AppUserPreviousPassword aPreviewPassword : nLastUsedPasswords) {

                if (aPreviewPassword.getPassword().equals(passWordEncodedValue)) {

                throw new PasswordPreviouslyUsedException();

                }
            }

            currentPasswordToSaveAsPreview = new AppUserPreviousPassword(user);

        }

        return currentPasswordToSaveAsPreview;

    }

    private Set<Role> assembleSetOfRoles(final String[] rolesArray) {

        final Set<Role> allRoles = new HashSet<>();

        if (!ObjectUtils.isEmpty(rolesArray)) {
            for (final String roleId : rolesArray) {
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
    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    public CommandProcessingResult deleteUser(final Long userId) {

        final AppUser user = this.appUserRepository.findOne(userId);
        if (user == null || user.isDeleted()) { throw new UserNotFoundException(userId); }

        user.delete();
        List<TopicSubscriber> subscriptions = topicSubscriberRepository.findBySubscriber(user);
        for (TopicSubscriber subscription : subscriptions) {
        	topicSubscriberRepository.delete(subscription);
        }
        this.appUserRepository.save(user);

        return new CommandProcessingResultBuilder().withEntityId(userId).withOfficeId(user.getOffice().getId()).build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("'username_org'")) {
            final String username = command.stringValueOfParameterNamed("username");
            final StringBuilder defaultMessageBuilder = new StringBuilder("User with username ").append(username)
                    .append(" already exists.");
            throw new PlatformDataIntegrityException("error.msg.user.duplicate.username", defaultMessageBuilder.toString(), "username",
                    username);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }
}