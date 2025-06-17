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

import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.codes.exception.CodeValueNotFoundException;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateNewRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierUpdateResponse;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierStatus;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.apache.fineract.portfolio.client.exception.DuplicateClientIdentifierException;
import org.apache.fineract.portfolio.client.serialization.ClientIdentifierCommandFromApiJsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientIdentifierWritePlatformServiceJpaRepositoryImpl implements ClientIdentifierWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientIdentifierWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final ClientIdentifierCommandFromApiJsonDeserializer clientIdentifierCommandFromApiJsonDeserializer;

    @Autowired
    public ClientIdentifierWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ClientRepositoryWrapper clientRepository, final ClientIdentifierRepository clientIdentifierRepository,
            final CodeValueRepositoryWrapper codeValueRepository,
            final ClientIdentifierCommandFromApiJsonDeserializer clientIdentifierCommandFromApiJsonDeserializer) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.clientIdentifierRepository = clientIdentifierRepository;
        this.codeValueRepository = codeValueRepository;
        this.clientIdentifierCommandFromApiJsonDeserializer = clientIdentifierCommandFromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public ClientIdentifierCreateResponse addClientIdentifier(final Long clientId, final ClientIdentifierCreateNewRequest request) {

        final String documentKey = request.getDocumentKey();
        String documentTypeLabel = null;
        Long documentTypeId = null;
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            final CodeValue documentType = this.codeValueRepository.findOneWithNotFoundDetection(request.getDocumentTypeId());
            documentTypeId = documentType.getId();
            documentTypeLabel = documentType.getLabel();

            final ClientIdentifier clientIdentifier = new ClientIdentifier(client, documentType, documentKey, request.getStatus(),
                    request.getDescription());

            this.clientIdentifierRepository.saveAndFlush(clientIdentifier);

            return new ClientIdentifierCreateResponse().builder() //
                    .officeId(client.officeId()) //
                    .clientId(clientId) //
                    .resourceId(clientIdentifier.getId()) // entityId
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, dve.getMostSpecificCause(), dve);
            return new ClientIdentifierCreateResponse();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, throwable, dve);
            return new ClientIdentifierCreateResponse();
        }
    }

    @Transactional
    @Override
    public ClientIdentifierUpdateResponse updateClientIdentifier(final Long clientId, final Long identifierId,
            final ClientIdentifierUpdateRequest request) {

        Long documentTypeId = request.getDocumentTypeId();
        validateForUpdate(documentTypeId, request.getDocumentKey());

        String documentTypeLabel = null;
        String documentKey = null;
        try {
            CodeValue documentType = null;

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final ClientIdentifier clientIdentifierForUpdate = this.clientIdentifierRepository.findById(identifierId)
                    .orElseThrow(() -> new ClientIdentifierNotFoundException(identifierId));

            final Map<String, Object> changes = update(clientIdentifierForUpdate, request);

            if (changes.containsKey("documentTypeId")) {
                documentType = this.codeValueRepository.findOneWithNotFoundDetection(documentTypeId);
                if (documentType == null) {
                    throw new CodeValueNotFoundException(documentTypeId);
                }

                documentTypeId = documentType.getId();
                documentTypeLabel = documentType.getLabel();
                clientIdentifierForUpdate.update(documentType);
            }

            if (changes.containsKey("documentTypeId") && changes.containsKey("documentKey")) {
                documentTypeId = request.getDocumentTypeId();
                documentKey = request.getDocumentKey();
            } else if (changes.containsKey("documentTypeId") && !changes.containsKey("documentKey")) {
                documentTypeId = request.getDocumentTypeId();
                documentKey = clientIdentifierForUpdate.getDocumentKey();
            } else if (!changes.containsKey("documentTypeId") && changes.containsKey("documentKey")) {
                documentTypeId = clientIdentifierForUpdate.getDocumentType().getId();
                documentKey = clientIdentifierForUpdate.getDocumentKey();
            }

            if (!changes.isEmpty()) {
                this.clientIdentifierRepository.saveAndFlush(clientIdentifierForUpdate);
            }

            return ClientIdentifierUpdateResponse.builder().clientId(client.officeId()).clientId(clientId).resourceId(identifierId) // entityId
                    .changes(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, dve.getMostSpecificCause(), dve);
            return ClientIdentifierUpdateResponse.builder().resourceId(-1L).build();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, throwable, dve);
            return new ClientIdentifierUpdateResponse();
        }
    }

    @Transactional
    @Override
    public ClientIdentifierDeleteResponse deleteClientIdentifier(final Long clientId, final Long identifierId) {

        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

        final ClientIdentifier clientIdentifier = this.clientIdentifierRepository.findById(identifierId)
                .orElseThrow(() -> new ClientIdentifierNotFoundException(identifierId));
        this.clientIdentifierRepository.delete(clientIdentifier);

        return ClientIdentifierDeleteResponse.builder().officeId(client.officeId()).clientId(clientId).resourceId(identifierId).build();
    }

    private void handleClientIdentifierDataIntegrityViolation(final String documentTypeLabel, final Long documentTypeId,
            final String documentKey, final Throwable cause, final Exception dve) {
        if (cause.getMessage().contains("unique_active_client_identifier")) {
            throw new DuplicateClientIdentifierException(documentTypeLabel);
        } else if (cause.getMessage().contains("unique_identifier_key")) {
            throw new DuplicateClientIdentifierException(documentTypeId, documentTypeLabel, documentKey);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw ErrorHandler.getMappable(dve, "error.msg.clientIdentifier.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        LOG.error("Error occured.", dve);
    }

    // TODO refactor
    public void validateForUpdate(final Long documentTypeId, final String documentKey) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("clientIdentifier");

        baseDataValidator.reset().parameter("documentKey").value(documentKey).ignoreIfNull().notBlank();

        // FIXME - KW - add in validation
        // if (command.isDocumentTypeChanged()) {
        // baseDataValidator.reset().parameter("documentTypeId").value(command.getDocumentTypeId()).notNull().integerGreaterThanZero();
        // }

        baseDataValidator.reset().anyOfNotNull(documentTypeId, documentKey);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public Map<String, Object> update(ClientIdentifier clientIdentifierForUpdate, final ClientIdentifierUpdateRequest request) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String documentTypeIdParamName = "documentTypeId";
        Long documentTypeId = request.getDocumentTypeId();
        if (documentTypeId.compareTo(clientIdentifierForUpdate.getDocumentType().getId()) != 0) {
            actualChanges.put(documentTypeIdParamName, documentTypeId);
        }

        final String documentKeyParamName = "documentKey";
        String documentKey = request.getDocumentKey();
        if (documentKey.compareTo(clientIdentifierForUpdate.getDocumentKey()) != 0) {
            actualChanges.put(documentKeyParamName, documentKey);
            clientIdentifierForUpdate.setDocumentKey(documentKey);
        }

        final String descriptionParamName = "description";
        String description = request.getDescription();
        if (description.compareTo(clientIdentifierForUpdate.getDescription()) != 0) {
            actualChanges.put(descriptionParamName, description);
            clientIdentifierForUpdate.setDescription(description);
        }

        final String statusParamName = "status";
        String status = request.getStatus();
        if (status.compareTo(ClientIdentifierStatus.fromInt(clientIdentifierForUpdate.getStatus()).getCode()) != 0) {
            actualChanges.put(statusParamName, ClientIdentifierStatus.valueOf(status));
            clientIdentifierForUpdate.setStatus(ClientIdentifierStatus.valueOf(status).getValue());
        }

        return actualChanges;
    }

}
