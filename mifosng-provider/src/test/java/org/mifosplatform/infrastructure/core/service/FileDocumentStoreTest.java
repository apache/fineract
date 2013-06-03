package org.mifosplatform.infrastructure.core.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.infrastructure.documentmanagement.contentrepository.FileSystemContentRepository;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileDocumentStoreTest {
/**
    @Test
    public void shouldRetrieveDocument() {
        FileSystemContentRepository fileSystemDocumentStore = new FileSystemContentRepository();
        DocumentData documentDataMock = mock(DocumentData.class);
        when(documentDataMock.fileName()).thenReturn("some_file");
        when(documentDataMock.fileName()).thenReturn("content_type");
        when(documentDataMock.fileLocation()).thenReturn("abcd");

        FileData fileData = fileSystemDocumentStore.retrieveDocument(documentDataMock);

        assertEquals(fileData.name(), documentDataMock.fileName());
        assertEquals(fileData.contentType(), documentDataMock.contentType());
    }
**/
}
