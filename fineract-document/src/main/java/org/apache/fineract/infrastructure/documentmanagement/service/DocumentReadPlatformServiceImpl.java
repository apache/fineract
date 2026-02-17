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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentContent;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentRepository;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.mapping.DocumentMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentReadPlatformServiceImpl implements DocumentReadPlatformService {

    private final ContentStoreService storeService;
    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    @Override
    public List<DocumentData> retrieveAllDocuments(final String entityType, final Long entityId) {
        final var docs = documentRepository.findAllByParentEntityTypeAndParentEntityId(entityType, entityId);

        return docs.stream().map(documentMapper::map).toList();
    }

    @Override
    public DocumentData retrieveDocument(final Long id) {
        return documentRepository.findById(id).map(documentMapper::map).orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Override
    public DocumentContent retrieveDocumentContent(final String entityType, final Long entityId, final Long documentId) {
        final var doc = documentRepository.findByIdAndParentEntityTypeAndParentEntityId(documentId, entityType, entityId)
                .orElseThrow(() -> new DocumentNotFoundException(entityType, entityId, documentId));
        final var is = storeService.download(doc.getLocation());

        return documentMapper.map(doc, is);
    }

    @Override
    public DocumentData retrieveDocument(final String entityType, final Long entityId, final Long documentId) {
        final var doc = documentRepository.findByIdAndParentEntityTypeAndParentEntityId(documentId, entityType, entityId)
                .orElseThrow(() -> new DocumentNotFoundException(entityType, entityId, documentId));

        return documentMapper.map(doc);
    }
}
