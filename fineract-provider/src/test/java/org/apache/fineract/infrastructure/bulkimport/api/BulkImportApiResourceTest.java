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
package org.apache.fineract.infrastructure.bulkimport.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import org.apache.fineract.infrastructure.bulkimport.data.ImportData;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentContent;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkImportApiResourceTest {

    @Mock
    private BulkImportWorkbookService bulkImportWorkbookService;

    @Mock
    private DocumentReadPlatformService documentReadPlatformService;

    @Mock
    private DefaultToApiJsonSerializer bulkImportSerializer;

    @Mock
    private ApiRequestParameterHelper apiRequestParameterHelper;

    @InjectMocks
    private BulkImportApiResource underTest;

    @Test
    void getOutputTemplate_success() {
        Long importDocumentId = 2L;
        Long documentId = 10L;

        ImportData importData = ImportData.instance(importDocumentId, documentId, null, null, false, "file.xls", 1L, 1, 0, 0);

        DocumentData documentData = DocumentData.builder().id(documentId).parentEntityType("IMPORT").parentEntityId(1L).fileName("file.xls")
                .build();

        DocumentContent documentContent = DocumentContent.builder().fileName("file.xls").contentType("application/vnd.ms-excel")
                .stream(new ByteArrayInputStream(new byte[] { 1, 2, 3 })).build();

        when(bulkImportWorkbookService.getImport(importDocumentId)).thenReturn(importData);
        when(documentReadPlatformService.retrieveDocument(documentId)).thenReturn(documentData);
        when(documentReadPlatformService.retrieveDocumentContent("IMPORT", 1L, documentId)).thenReturn(documentContent);

        underTest.getOutputTemplate(importDocumentId);

        verify(bulkImportWorkbookService).getImport(importDocumentId);
        verify(documentReadPlatformService).retrieveDocument(documentId);
        verify(documentReadPlatformService).retrieveDocumentContent("IMPORT", 1L, documentId);
    }

    @Test
    void getOutputTemplate_importNotFound_throwsDocumentNotFoundException() {
        Long importDocumentId = 99L;
        when(bulkImportWorkbookService.getImport(importDocumentId)).thenReturn(null);

        assertThatThrownBy(() -> underTest.getOutputTemplate(importDocumentId)).isInstanceOf(DocumentNotFoundException.class);

        verifyNoInteractions(documentReadPlatformService);
    }

    @Test
    void getOutputTemplate_documentNotFound_throwsDocumentNotFoundException() {
        Long importDocumentId = 2L;
        Long documentId = 10L;

        ImportData importData = ImportData.instance(importDocumentId, documentId, null, null, false, "file.xls", 1L, 1, 0, 0);

        when(bulkImportWorkbookService.getImport(importDocumentId)).thenReturn(importData);
        when(documentReadPlatformService.retrieveDocument(documentId)).thenReturn(null);

        assertThatThrownBy(() -> underTest.getOutputTemplate(importDocumentId)).isInstanceOf(DocumentNotFoundException.class);

        verify(documentReadPlatformService).retrieveDocument(documentId);
    }

    @Test
    void getOutputTemplate_usesDocumentIdFromImportData_notImportDocumentId() {
        Long importDocumentId = 5L;
        Long documentId = 10L;

        ImportData importData = ImportData.instance(importDocumentId, documentId, null, null, false, "file.xls", 1L, 1, 0, 0);

        DocumentData documentData = DocumentData.builder().id(documentId).parentEntityType("IMPORT").parentEntityId(1L).fileName("file.xls")
                .build();

        DocumentContent documentContent = DocumentContent.builder().fileName("file.xls").contentType("application/vnd.ms-excel")
                .stream(new ByteArrayInputStream(new byte[0])).build();

        when(bulkImportWorkbookService.getImport(importDocumentId)).thenReturn(importData);
        when(documentReadPlatformService.retrieveDocument(documentId)).thenReturn(documentData);
        when(documentReadPlatformService.retrieveDocumentContent("IMPORT", 1L, documentId)).thenReturn(documentContent);

        underTest.getOutputTemplate(importDocumentId);

        verify(documentReadPlatformService).retrieveDocument(documentId);
        verify(documentReadPlatformService).retrieveDocumentContent("IMPORT", 1L, documentId);
    }
}
