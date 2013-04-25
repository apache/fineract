package org.mifosplatform.infrastructure.core.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")

@RunWith(MockitoJUnitRunner.class)
public class FileDocumentStoreTest {

    @Test
    public void shouldRetrieveDocument(){
        FileSystemDocumentStore fileSystemDocumentStore = new FileSystemDocumentStore();
        DocumentData documentDataMock = mock(DocumentData.class);
        when(documentDataMock.fileName()).thenReturn("some_file");
        when(documentDataMock.fileName()).thenReturn("content_type");
        when(documentDataMock.fileLocation()).thenReturn("abcd");

        FileData fileData = fileSystemDocumentStore.retrieveDocument(documentDataMock);

        assertEquals(fileData.name(), documentDataMock.fileName());
        assertEquals(fileData.contentType(), documentDataMock.contentType());
    }
}
