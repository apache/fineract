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
package org.apache.fineract.infrastructure.event.external.service.serialization.serializer.document;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.infrastructure.event.business.domain.BusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.document.DocumentBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.document.DocumentCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.document.DocumentDeletedBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.document.DocumentDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentBusinessEventSerializer implements BusinessEventSerializer {

    private static final Logger log = LoggerFactory.getLogger(DocumentBusinessEventSerializer.class);
    private final DocumentReadPlatformService service;
    private final DocumentDataMapper mapper;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof DocumentCreatedBusinessEvent || event instanceof DocumentDeletedBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {

        DocumentBusinessEvent event = (DocumentBusinessEvent) rawEvent;
        Document entity = event.get(); // domain entity

        DocumentData dto = null;
        if (rawEvent instanceof DocumentCreatedBusinessEvent) {
            try {
                dto = service.retrieveDocument(entity.getParentEntityType(), entity.getParentEntityId(), entity.getId());
            } catch (Exception ex) {
                // log at DEBUG and fall back to entity mapping
                log.debug("DocumentData not found, falling back to entity", ex);
            }
        }

        // If we have the DTO, let MapStruct do the work. Otherwise, build from the entity

        DocumentDataV1 avro;
        if (dto != null) {
            avro = mapper.map(dto);
        } else {
            avro = DocumentDataV1.newBuilder().setId(entity.getId()).setParentEntityType(entity.getParentEntityType())
                    .setParentEntityId(entity.getParentEntityId()).setName(entity.getName()).setFileName(entity.getFileName())
                    .setSize(entity.getSize()).setType(entity.getType()).setDescription(entity.getDescription()).build();
        }

        Integer storageTypeCode = (dto != null && dto.getStorageType() != null && dto.storageType() != null) ? dto.getStorageType()
                : (entity.storageType() != null ? entity.storageType().getValue() : null);
        avro.setStorageType(storageTypeCode);

        return avro;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return DocumentDataV1.class;
    }
}
