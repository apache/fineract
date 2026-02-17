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
package org.apache.fineract.infrastructure.documentmanagement.service;

import static java.util.Objects.requireNonNull;

import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentDeleteRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentDeleteResponse;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentUpdateRequest;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentUpdateResponse;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentRepository;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.mapping.DocumentMapper;
import org.apache.fineract.infrastructure.event.business.domain.document.DocumentCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.document.DocumentDeletedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentWritePlatformServiceImpl implements DocumentWritePlatformService {

    private static final String STORE_PREFIX = "documents";

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final ContentStoreService storeService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final FineractProperties properties;

    @Transactional
    @Override
    public DocumentCreateResponse createDocument(@Valid final DocumentCreateRequest request) {
        try {
            requireNonNull(request, "Request parameter required");
            requireNonNull(request.getFileName(), "File name parameter required");
            requireNonNull(request.getStream(), "Document content required");

            // TODO: make "prefix" configurable
            var path = getPath(STORE_PREFIX, request.getEntityType(), request.getEntityId(), request.getFileName(),
                    storeService.getDelimiter());

            path = storeService.upload(path, request.getStream(), request.getType());

            final var doc = new Document().setParentEntityType(request.getEntityType()).setParentEntityId(request.getEntityId())
                    .setName(Optional.ofNullable(request.getName()).orElse(request.getFileName())).setFileName(request.getFileName())
                    .setSize(request.getSize()).setType(request.getType()).setDescription(request.getDescription()).setLocation(path)
                    .setStorageType(storeService.getType().getValue());

            documentRepository.save(doc);

            businessEventNotifierService.notifyPostBusinessEvent(new DocumentCreatedBusinessEvent(documentMapper.map(doc)));

            return DocumentCreateResponse.builder().resourceId(doc.getId()).resourceIdentifier(request.getEntityType()).build();
        } catch (final Exception e) {
            throw ErrorHandler.getMappable(e, "error.msg.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    @Transactional
    @Override
    public DocumentUpdateResponse updateDocument(@Valid final DocumentUpdateRequest request) {
        try {
            requireNonNull(request, "Request parameter required");

            String path = null;

            if (StringUtils.isNotEmpty(request.getFileName())) {
                // TODO: make "prefix" configurable
                path = getPath(STORE_PREFIX, request.getEntityType(), request.getEntityId(), request.getFileName(),
                        storeService.getDelimiter());
            }

            final var doc = this.documentRepository.findById(request.getId())
                    .orElseThrow(() -> new DocumentNotFoundException(request.getEntityType(), request.getEntityId(), request.getId()));

            doc.setStorageType(storeService.getType().getValue());

            if (StringUtils.isNotEmpty(request.getFileName())) {
                // these two only make sense if we have an actual file

                if (request.getStream() != null && StringUtils.isNotEmpty(path) && !Strings.CI.equals(doc.getLocation(), path)) {
                    storeService.delete(doc.getLocation());
                    storeService.upload(path, request.getStream(), request.getType());
                    doc.setLocation(path);
                    doc.setFileName(FilenameUtils.getName(path));
                }
            } else if (request.getStream() != null) {
                storeService.upload(doc.getLocation(), request.getStream(), request.getType());
            }
            if (StringUtils.isNotEmpty(request.getDescription()) && !Strings.CI.equals(doc.getDescription(), request.getDescription())) {
                doc.setDescription(request.getDescription());
            }
            if (StringUtils.isNotEmpty(request.getName()) && !Strings.CI.equals(doc.getName(), request.getName())) {
                doc.setName(request.getName());
            }
            if (Objects.requireNonNullElse(request.getSize(), 0L) > 1L && !Objects.equals(doc.getSize(), request.getSize())) {
                doc.setSize(request.getSize());
            }

            documentRepository.save(doc);

            // TODO: shouldn't we send a business event?
            // businessEventNotifierService.notifyPostBusinessEvent(new DocumentUpdateBusinessEvent(doc));

            return DocumentUpdateResponse.builder().resourceId(doc.getId()).resourceIdentifier(request.getEntityType()).build();
        } catch (final Exception e) {
            throw ErrorHandler.getMappable(e, "error.msg.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    @Transactional
    @Override
    public DocumentDeleteResponse deleteDocument(@Valid final DocumentDeleteRequest request) {
        try {
            final var doc = this.documentRepository.findById(request.getId())
                    .orElseThrow(() -> new DocumentNotFoundException(request.getEntityType(), request.getEntityId(), request.getId()));

            storeService.delete(doc.getLocation());

            documentRepository.deleteById(request.getId());

            businessEventNotifierService.notifyPostBusinessEvent(new DocumentDeletedBusinessEvent(documentMapper.map(doc)));

            return DocumentDeleteResponse.builder().resourceId(doc.getId()).resourceIdentifier(request.getEntityType()).build();
        } catch (final Exception e) {
            throw ErrorHandler.getMappable(e, "error.msg.document.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    private String getPath(final String prefix, final String entityType, final Long entityId, final String fileName, String delimiter) {
        requireNonNull(prefix);
        requireNonNull(entityType);
        requireNonNull(entityId);
        requireNonNull(fileName);
        requireNonNull(delimiter);

        return String.join(delimiter, prefix, entityType, String.valueOf(entityId), fileName);
    }
}
