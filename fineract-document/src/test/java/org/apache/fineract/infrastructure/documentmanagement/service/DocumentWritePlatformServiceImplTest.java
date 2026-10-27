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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentCreateRequest;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentRepository;
import org.apache.fineract.infrastructure.documentmanagement.mapping.DocumentMapper;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentWritePlatformServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentMapper documentMapper;
    @Mock
    private ContentStoreService contentStoreService;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    private DocumentWritePlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new DocumentWritePlatformServiceImpl(documentRepository, documentMapper, contentStoreService,
                businessEventNotifierService);
    }

    @Test
    void createDocumentShouldRejectContentPolicyViolationAsValidationError() {
        var message = "File name not allowed: test.txt";
        when(contentStoreService.getDelimiter()).thenReturn("/");
        when(contentStoreService.upload(anyString(), any(InputStream.class), eq("text/plain")))
                .thenThrow(new ContentPolicyException(message));

        DocumentCreateRequest request = DocumentCreateRequest.builder().entityType("clients").entityId(7L).fileName("test.txt")
                .type("text/plain").size(3L).stream(new ByteArrayInputStream(new byte[] { 1, 2, 3 })).build();

        var exception = assertThrows(PlatformApiDataValidationException.class, () -> underTest.createDocument(request));

        assertEquals(1, exception.getErrors().size());
        assertEquals("error.msg.content.policy", exception.getErrors().get(0).getUserMessageGlobalisationCode());
        assertEquals("file", exception.getErrors().get(0).getParameterName());
        assertEquals(message, exception.getErrors().get(0).getDefaultUserMessage());
        verifyNoInteractions(documentRepository, businessEventNotifierService);
    }
}
