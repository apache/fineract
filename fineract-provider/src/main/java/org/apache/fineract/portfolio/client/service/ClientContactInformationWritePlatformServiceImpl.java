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
package org.apache.fineract.portfolio.client.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientContactInformation;
import org.apache.fineract.portfolio.client.domain.ClientContactInformationRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientContactInformationNotFoundException;
import org.apache.fineract.portfolio.client.serialization.ClientContactInformationCommandFromApiJsonDeserializer;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientContactInformationWritePlatformServiceImpl implements ClientContactInformationWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientContactInformationWritePlatformServiceImpl.class);

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientContactInformationRepository clientContactInformationRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final ClientContactInformationCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public ClientContactInformationWritePlatformServiceImpl(final PlatformSecurityContext context,
            final ClientRepositoryWrapper clientRepository, final CodeValueRepositoryWrapper codeValueRepository,
            final ClientContactInformationRepository clientContactInformationRepository,
            final ClientContactInformationCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.clientContactInformationRepository = clientContactInformationRepository;
        this.codeValueRepository = codeValueRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult addClientContactInformation(final Long clientId, final JsonCommand command) {
        final AppUser appUser = this.context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForCreate(command.json());

        final String contactKey = command.stringValueOfParameterNamed(ClientApiConstants.contactKeyParamName);
        final Long contactTypeId = command.longValueOfParameterNamed(ClientApiConstants.contactTypeIdParamName);
        String contactTypeLabel = null;
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            final CodeValue contactType = this.codeValueRepository.findOneWithNotFoundDetection(contactTypeId);
            contactTypeLabel = contactType.label();
            ClientContactInformation clientContactInformation = ClientContactInformation.fromJson(client, contactType, command, appUser);

            this.clientContactInformationRepository.saveAndFlush(clientContactInformation);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientContactInformation.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleClientContactInformationDataIntegrityViolation(contactTypeLabel, contactTypeId, contactKey, dve.getMostSpecificCause(),
                    dve);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleClientContactInformationDataIntegrityViolation(contactTypeLabel, contactTypeId, contactKey, throwable, dve);
        }
        return CommandProcessingResult.empty();
    }

    @Transactional
    @Override
    public CommandProcessingResult addClientContactInformation(AppUser appUser, Client client, JsonCommand command) {
        final JsonArray contactsArray = command.arrayOfParameterNamed(ClientApiConstants.CONTACTS);

        if (contactsArray != null) {
            for (int i = 0; i < contactsArray.size(); i++) {
                final JsonObject jsonObject = contactsArray.get(i).getAsJsonObject();

                CodeValue contactType = null;
                if (jsonObject.get(ClientApiConstants.contactTypeIdParamName) != null) {
                    final Long contactTypeId = jsonObject.get(ClientApiConstants.contactTypeIdParamName).getAsLong();
                    contactType = this.codeValueRepository.findOneWithNotFoundDetection(contactTypeId);
                }

                if (contactType != null) {
                    ClientContactInformation clientContactInformation = ClientContactInformation.fromJson(client, contactType, jsonObject,
                            appUser);

                    this.clientContactInformationRepository.save(clientContactInformation);

                    return new CommandProcessingResultBuilder() //
                            .withCommandId(command.commandId()) //
                            .withClientId(client.getId()) //
                            .withEntityId(clientContactInformation.getId()) //
                            .build();
                }
            }
        }
        return CommandProcessingResult.empty();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClientContactInformation(final Long clientId, final Long identifierId, final JsonCommand command) {
        final AppUser appUser = this.context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForUpdate(command.json());

        final String contactKey = command.stringValueOfParameterNamed(ClientApiConstants.contactKeyParamName);
        final Long contactTypeId = command.longValueOfParameterNamed(ClientApiConstants.contactTypeIdParamName);
        String contactTypeLabel = null;

        try {
            ClientContactInformation clientContactInformation = this.clientContactInformationRepository
                    .findByIdAndClient(command.entityId(), clientId);

            if (clientContactInformation != null) {
                final CodeValue contactType = this.codeValueRepository.findOneWithNotFoundDetection(contactTypeId);
                final Map<String, Object> actualChanges = clientContactInformation.update(contactType, command, appUser);

                if (!actualChanges.isEmpty()) {
                    this.clientContactInformationRepository.saveAndFlush(clientContactInformation);
                }

                return new CommandProcessingResultBuilder() //
                        .withCommandId(command.commandId()) //
                        .withClientId(clientId) //
                        .withEntityId(clientContactInformation.getId()) //
                        .build();
            } else {
                throw new ClientContactInformationNotFoundException(command.entityId());
            }
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleClientContactInformationDataIntegrityViolation(contactTypeLabel, contactTypeId, contactKey, dve.getMostSpecificCause(),
                    dve);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleClientContactInformationDataIntegrityViolation(contactTypeLabel, contactTypeId, contactKey, throwable, dve);
        }
        return CommandProcessingResult.empty();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientContactInformation(final Long clientId, final Long identifierId, final Long commandId) {

        // final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        ClientContactInformation clientContactInformation = this.clientContactInformationRepository.findByIdAndClient(identifierId,
                clientId);

        if (clientContactInformation != null) {
            this.clientContactInformationRepository.delete(clientContactInformation);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(commandId) //
                .withClientId(clientId) //
                .withEntityId(identifierId) //
                .build();
    }

    private void handleClientContactInformationDataIntegrityViolation(final String documentTypeLabel, final Long documentTypeId,
            final String documentKey, final Throwable cause, final Exception dve) {

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.clientInformation.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        LOG.error("Error occured.", dve);
    }
}
