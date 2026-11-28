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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import org.apache.fineract.infrastructure.contentstore.data.ContentStoreType;
import org.apache.fineract.infrastructure.contentstore.exception.ContentPolicyException;
import org.apache.fineract.infrastructure.contentstore.service.ContentStoreService;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentUpdateRequest;
import org.apache.fineract.infrastructure.documentmanagement.domain.Document;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentRepository;
import org.apache.fineract.infrastructure.documentmanagement.mapping.DocumentMapper;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentWritePlatformServiceImplUpdateLocationTest {

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
    void updateDocumentOnAKeyScopedStoreUploadsBeforeDeletingThePreviousObject() {
        final var document = existingDocument("tenants/t1/documents/clients/7/old.txt");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(contentStoreService.getType()).thenReturn(ContentStoreType.S3);
        when(contentStoreService.getDelimiter()).thenReturn("/");
        when(contentStoreService.upload(eq("documents/clients/7/new.txt"), any(InputStream.class), eq("text/plain")))
                .thenReturn("tenants/t1/documents/clients/7/new.txt");

        underTest.updateDocument(updateRequest("new.txt", new ByteArrayInputStream(new byte[] { 1 })));

        final InOrder inOrder = inOrder(contentStoreService);
        inOrder.verify(contentStoreService).upload(eq("documents/clients/7/new.txt"), any(InputStream.class), eq("text/plain"));
        inOrder.verify(contentStoreService).delete("tenants/t1/documents/clients/7/old.txt");
        assertEquals("tenants/t1/documents/clients/7/new.txt", document.getLocation());
        assertEquals("new.txt", document.getFileName());
    }

    @Test
    void updateDocumentOnAKeyScopedStoreKeepsTheObjectWhenTheStoredKeyIsTheSame() {
        final var document = existingDocument("tenants/t1/documents/clients/7/new.txt");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(contentStoreService.getType()).thenReturn(ContentStoreType.S3);
        when(contentStoreService.getDelimiter()).thenReturn("/");
        when(contentStoreService.upload(eq("documents/clients/7/new.txt"), any(InputStream.class), eq("text/plain")))
                .thenReturn("tenants/t1/documents/clients/7/new.txt");

        underTest.updateDocument(updateRequest("new.txt", new ByteArrayInputStream(new byte[] { 1 })));

        verify(contentStoreService, never()).delete(anyString());
        assertEquals("tenants/t1/documents/clients/7/new.txt", document.getLocation());
    }

    @Test
    void updateDocumentOnAKeyScopedStoreKeepsThePreviousObjectWhenTheUploadFails() {
        final var document = existingDocument("tenants/t1/documents/clients/7/old.txt");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(contentStoreService.getType()).thenReturn(ContentStoreType.S3);
        when(contentStoreService.getDelimiter()).thenReturn("/");
        when(contentStoreService.upload(anyString(), any(InputStream.class), eq("text/plain")))
                .thenThrow(new ContentPolicyException("File name not allowed"));

        final var request = updateRequest("new.txt", new ByteArrayInputStream(new byte[] { 1 }));
        assertThrows(PlatformApiDataValidationException.class, () -> underTest.updateDocument(request));

        verify(contentStoreService, never()).delete(anyString());
        assertEquals("tenants/t1/documents/clients/7/old.txt", document.getLocation());
    }

    @Test
    void updateDocumentOnTheFileSystemStoreDeletesThePreviousFileBeforeTheUpload() {
        final var document = existingDocument("documents/clients/7/old.txt");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(contentStoreService.getType()).thenReturn(ContentStoreType.FILE_SYSTEM);
        when(contentStoreService.getDelimiter()).thenReturn("/");
        when(contentStoreService.upload(eq("documents/clients/7/new.txt"), any(InputStream.class), eq("text/plain")))
                .thenReturn("documents/clients/7/new.txt");

        underTest.updateDocument(updateRequest("new.txt", new ByteArrayInputStream(new byte[] { 1 })));

        final InOrder inOrder = inOrder(contentStoreService);
        inOrder.verify(contentStoreService).delete("documents/clients/7/old.txt");
        inOrder.verify(contentStoreService).upload(eq("documents/clients/7/new.txt"), any(InputStream.class), eq("text/plain"));
        assertEquals("documents/clients/7/new.txt", document.getLocation());
    }

    @Test
    void updateDocumentContentOnTheFileSystemStoreNeverDeletesTheFileJustWritten() {
        final var document = existingDocument("./documents/clients/7/file.txt");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(contentStoreService.getType()).thenReturn(ContentStoreType.FILE_SYSTEM);
        when(contentStoreService.upload(eq("./documents/clients/7/file.txt"), any(InputStream.class), eq("text/plain")))
                .thenReturn("legacy/documents/clients/7/file.txt");

        underTest.updateDocument(updateRequest(null, new ByteArrayInputStream(new byte[] { 1 })));

        verify(contentStoreService, never()).delete(anyString());
        assertEquals("./documents/clients/7/file.txt", document.getLocation());
    }

    @Test
    void updateDocumentContentOnAKeyScopedStoreMovesALegacyLocationToTheStoredKey() {
        final var document = existingDocument("documents/clients/7/file.txt");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(contentStoreService.getType()).thenReturn(ContentStoreType.S3);
        when(contentStoreService.upload(eq("documents/clients/7/file.txt"), any(InputStream.class), eq("text/plain")))
                .thenReturn("tenants/t1/documents/clients/7/file.txt");

        underTest.updateDocument(updateRequest(null, new ByteArrayInputStream(new byte[] { 1 })));

        verify(contentStoreService).delete("documents/clients/7/file.txt");
        assertEquals("tenants/t1/documents/clients/7/file.txt", document.getLocation());
    }

    @Test
    void updateDocumentContentOnAKeyScopedStoreDoesNotDeleteWhenTheKeyIsUnchanged() {
        final var document = existingDocument("tenants/t1/documents/clients/7/file.txt");
        when(documentRepository.findById(5L)).thenReturn(Optional.of(document));
        when(contentStoreService.getType()).thenReturn(ContentStoreType.S3);
        when(contentStoreService.upload(eq("tenants/t1/documents/clients/7/file.txt"), any(InputStream.class), eq("text/plain")))
                .thenReturn("tenants/t1/documents/clients/7/file.txt");

        underTest.updateDocument(updateRequest(null, new ByteArrayInputStream(new byte[] { 1 })));

        verify(contentStoreService, never()).delete(anyString());
    }

    private static Document existingDocument(final String location) {
        return new Document().setId(5L).setParentEntityType("clients").setParentEntityId(7L).setName("doc").setFileName("old.txt")
                .setLocation(location);
    }

    private static DocumentUpdateRequest updateRequest(final String fileName, final InputStream stream) {
        return DocumentUpdateRequest.builder().id(5L).entityType("clients").entityId(7L).fileName(fileName).type("text/plain").size(1L)
                .stream(stream).build();
    }
}
