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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fineract.avro.document.v1.DocumentDataV1;
import org.apache.fineract.avro.generator.ByteBufferSerializable;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.infrastructure.event.business.domain.document.DocumentCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.external.service.serialization.mapper.document.DocumentDataMapper;
import org.apache.fineract.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DocumentBusinessEventSerializerTest {

    @Mock
    private DocumentReadPlatformService readService;
    @Mock
    private DocumentDataMapper mapper;

    private BusinessEventSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new DocumentBusinessEventSerializer(readService, mapper);
    }

    @Test
    void documentStorageTypeIsPatchedIntoAvro() {

        long docId = 42L;
        String parentEntity = "loans";
        long parentEntityId = 979L;
        String name = "test_document";
        String fileName = "test_document.pdf";
        String fileType = "application/pdf";
        String description = "Test document description";
        Integer storageTypeInt = StorageType.FILE_SYSTEM.getValue();

        Document document = mock(Document.class);
        when(document.getId()).thenReturn(docId);
        when(document.getParentEntityType()).thenReturn(parentEntity);
        when(document.getParentEntityId()).thenReturn(parentEntityId);
        when(document.getName()).thenReturn(name);
        when(document.getFileName()).thenReturn(fileName);
        when(document.getType()).thenReturn(fileType);
        when(document.getDescription()).thenReturn(description);
        when(document.storageType()).thenReturn(StorageType.fromInt(storageTypeInt));

        DocumentCreatedBusinessEvent event = new DocumentCreatedBusinessEvent(document);

        DocumentData dtoFromReadService = mock(DocumentData.class);
        when(readService.retrieveDocument(parentEntity, parentEntityId, docId)).thenReturn(dtoFromReadService);

        DocumentDataV1 avroFromMapper = DocumentDataV1.newBuilder().setId(docId).setParentEntityType(parentEntity)
                .setParentEntityId(parentEntityId).setName(name).setFileName(fileName).setType(fileType).setDescription(description)
                .build();
        when(mapper.map(any(DocumentData.class))).thenReturn(avroFromMapper);

        ByteBufferSerializable serialised = serializer.toAvroDTO(event);
        assertNotNull(serialised);

        DocumentDataV1 avro = (DocumentDataV1) serialised;

        assertEquals(storageTypeInt, avro.getStorageType(), "Serializer must patch storageType taken from the domain entity");
    }
}
