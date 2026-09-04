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
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateRequest;
import org.apache.fineract.portfolio.client.data.ClientIdentifierCreateResponse;
import org.apache.fineract.portfolio.client.data.ClientIdentifierDeleteRequest;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class ClientIdentifierWriteServiceImpl implements ClientIdentifierWriteService {

    private static final String CLIENT_IDENTIFIER_CODE_NAME = "Customer Identifier";

    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;

    @Transactional
    @Override
    public ClientIdentifierCreateResponse createClientIdentifier(final ClientIdentifierCreateRequest request) {
        String documentKey = null;
        String documentTypeLabel = null;
        Long documentTypeId = null;
        try {
            final Client client = clientRepositoryWrapper.findOneWithNotFoundDetection(request.getClientId());
            final CodeValue documentType = codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(CLIENT_IDENTIFIER_CODE_NAME,
                    request.getDocumentTypeId());
            documentKey = request.getDocumentKey();
            documentTypeId = documentType.getId();
            documentTypeLabel = documentType.getLabel();

            final ClientIdentifier entity = ClientIdentifier.builder().client(client).documentType(documentType)
                    .documentKey(StringUtils.defaultIfEmpty(request.getDocumentKey(), null))
                    .description(StringUtils.defaultIfEmpty(request.getDescription(), null)).build();

            final ClientIdentifierStatus statusEnum = ClientIdentifierStatus
                    .valueOf(StringUtils.defaultIfEmpty(request.getStatus(), ClientIdentifierStatus.ACTIVE.name()).toUpperCase());
            entity.setStatus(statusEnum.getValue());
            if (statusEnum.isActive()) {
                entity.setActive(statusEnum.getValue());
            }

            clientIdentifierRepository.saveAndFlush(entity);

            return ClientIdentifierCreateResponse.builder().resourceId(entity.getId()).clientId(request.getClientId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, throwable, dve);
        }
    }

    @Transactional
    @Override
    public ClientIdentifierUpdateResponse updateClientIdentifier(final ClientIdentifierUpdateRequest request) {
        String documentKey = null;
        String documentTypeLabel = null;
        Long documentTypeId = null;
        try {
            final ClientIdentifier entity = clientIdentifierRepository.findById(request.getId())
                    .orElseThrow(() -> new ClientIdentifierNotFoundException(request.getId()));
            if (entity.getClient() == null || !entity.getClient().getId().equals(request.getClientId())) {
                throw new ClientIdentifierNotFoundException(request.getId());
            }

            final HashMap<String, Object> changes = new HashMap<>();

            if (request.getDocumentTypeId() != null && !request.getDocumentTypeId().equals(entity.getDocumentType().getId())) {
                final CodeValue documentType = codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(CLIENT_IDENTIFIER_CODE_NAME,
                        request.getDocumentTypeId());
                entity.setDocumentType(documentType);
                changes.put(ClientIdentifierUpdateRequest.Fields.documentTypeId, request.getDocumentTypeId());
                documentTypeId = documentType.getId();
                documentTypeLabel = documentType.getLabel();
            }
            if (StringUtils.isNotBlank(request.getDocumentKey())) {
                entity.setDocumentKey(request.getDocumentKey());
                changes.put(ClientIdentifierUpdateRequest.Fields.documentKey, request.getDocumentKey());
                documentKey = request.getDocumentKey();
            }
            if (StringUtils.isNotBlank(request.getDescription())) {
                entity.setDescription(request.getDescription());
                changes.put(ClientIdentifierUpdateRequest.Fields.description, request.getDescription());
            }
            if (StringUtils.isNotBlank(request.getStatus())) {
                final ClientIdentifierStatus statusEnum = ClientIdentifierStatus.valueOf(request.getStatus().toUpperCase());
                entity.setStatus(statusEnum.getValue());
                entity.setActive(statusEnum.isActive() ? statusEnum.getValue() : null);
                changes.put(ClientIdentifierUpdateRequest.Fields.status, request.getStatus());
            }

            final var response = ClientIdentifierUpdateResponse.builder().resourceId(entity.getId()).clientId(request.getClientId());
            if (!changes.isEmpty()) {
                response.changes(changes);
                clientIdentifierRepository.saveAndFlush(entity);
            }
            return response.build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            final Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(documentTypeId, documentTypeLabel, documentKey, throwable, dve);
        }
    }

    @Transactional
    @Override
    public ClientIdentifierDeleteResponse deleteClientIdentifier(final ClientIdentifierDeleteRequest request) {
        final ClientIdentifier entity = clientIdentifierRepository.findById(request.getId())
                .orElseThrow(() -> new ClientIdentifierNotFoundException(request.getId()));
        if (entity.getClient() == null || !entity.getClient().getId().equals(request.getClientId())) {
            throw new ClientIdentifierNotFoundException(request.getId());
        }
        clientIdentifierRepository.delete(entity);
        return ClientIdentifierDeleteResponse.builder().resourceId(request.getId()).clientId(request.getClientId()).build();
    }

    private RuntimeException handleDataIntegrityIssues(final Long documentTypeId, final String documentTypeLabel, final String documentKey,
            final Throwable realCause, final Exception dve) {
        if (realCause.getMessage() != null && realCause.getMessage().contains("unique_active_client_identifier")) {
            return new DuplicateClientIdentifierException(documentTypeLabel);
        } else if (realCause.getMessage() != null && realCause.getMessage().contains("unique_identifier_key")) {
            return new DuplicateClientIdentifierException(documentTypeId, documentTypeLabel, documentKey);
        }
        log.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.clientIdentifier.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
