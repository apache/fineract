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
package org.apache.fineract.notification.service;

import java.util.Map;
import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.domain.Topic;
import org.apache.fineract.notification.domain.TopicRepositoryWrapper;
import org.apache.fineract.notification.serialization.TopicCommandFromApiJsonDeserializer;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicWritePlatformServiceJpaRepositoryImpl implements TopicWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(TopicWritePlatformServiceJpaRepositoryImpl.class);
	
	private final PlatformSecurityContext context;
    private final TopicCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final TopicRepositoryWrapper topicRepositoryWrapper;
    private final PermissionRepository	permissionRepository;

	public TopicWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context,
			TopicCommandFromApiJsonDeserializer fromApiJsonDeserializer, TopicRepositoryWrapper topicRepositoryWrapper,
			PermissionRepository permissionRepository) {
		this.context = context;
		this.fromApiJsonDeserializer = fromApiJsonDeserializer;
		this.topicRepositoryWrapper = topicRepositoryWrapper;
		this.permissionRepository = permissionRepository;
	}

	@Transactional
    @Override
	public CommandProcessingResult createTopic(JsonCommand command) {
		
		try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            Long permissionId = null;
            Topic topic = null;
            final String permissionIdParamName = "permissionId";
            if (command.parameterExists(permissionIdParamName)) {
            	permissionId = command.longValueOfParameterNamed(permissionIdParamName);
            }
            Permission permission = permissionRepository.findOne(permissionId);
            if (permission != null) {
            	topic = Topic.fromJson(permission, command);
            	this.topicRepositoryWrapper.save(topic);
            } else {
            	topic = Topic.fromJson(null, command);
            	this.topicRepositoryWrapper.save(topic);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(topic.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
		
	}

	private void handleDataIntegrityIssues(final JsonCommand command,final Throwable realCause,
			final Exception dve) {
		if (realCause.getMessage().contains("topic_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.topic.duplicate.name", "Topic with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.topic.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
		
	}

	@Override
	public CommandProcessingResult updateTopic(JsonCommand command) {
		try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Topic topic = this.topicRepositoryWrapper.findOneWithNotFoundDetection(command.entityId());
            Map<String, Object> changes = topic.update(command);
            if (changes.containsKey("permissionIdParamName")) {
            	final String permissionIdParamName = "permissionId";
            	topic.setPermission(permissionRepository.getOne(command.longValueOfParameterNamed(permissionIdParamName)));
            }
            
            this.topicRepositoryWrapper.save(topic);
            return new CommandProcessingResultBuilder().withEntityId(topic.getId()).with(changes).build();
		} catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
	}

	@Override
	public CommandProcessingResult activateTopic(final Long topicId) {
		this.context.authenticatedUser();
        final Topic topic = this.topicRepositoryWrapper.findOneWithNotFoundDetection(topicId);

        topic.setActive(true);
        this.topicRepositoryWrapper.save(topic);
        return new CommandProcessingResultBuilder().withEntityId(topic.getId()).build();
	}

	@Override
	public CommandProcessingResult deactivateTopic(final Long topicId) {
		this.context.authenticatedUser();
        final Topic topic = this.topicRepositoryWrapper.findOneWithNotFoundDetection(topicId);

        topic.setActive(false);
        this.topicRepositoryWrapper.save(topic);
        return new CommandProcessingResultBuilder().withEntityId(topic.getId()).build();
	}

}
