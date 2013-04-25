package org.mifosplatform.infrastructure.core.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")

@RunWith(MockitoJUnitRunner.class)
public class S3DocumentStoreTest {

    InputStream inputStream;
    AmazonS3Client s3ClientMock;
    DocumentCommand documentCommand;
    DocumentData documentDataMock;

    @Test
    public void shouldGenerateParentDirectoryFromEntityDetails(){
        S3DocumentStore s3DocumentStore=new S3DocumentStore("bucketName", null);
        String actualParentDirectory = s3DocumentStore.generateFileParentDirectory("entity_type", 1234L);
        assertTrue(actualParentDirectory.startsWith("documents/entity_type/1234"));
    }

    @Test
    public void shouldGenerateClientImageDirectoryFromResourceId(){
        S3DocumentStore s3DocumentStore=new S3DocumentStore("bucketName", null);
        String actualParentDirectory = s3DocumentStore.generateClientImageParentDirectory(1234L);
        assertTrue(actualParentDirectory.startsWith("images/clients/1234"));
    }

    @Test
    public void shouldSaveADocumentToS3() throws IOException {
        initUpload();
        when(s3ClientMock.putObject(Matchers.<PutObjectRequest>anyObject())).thenReturn(new PutObjectResult());
        S3DocumentStore s3DocumentStore = new S3DocumentStore("bucketName", s3ClientMock);

        s3DocumentStore.saveDocument(inputStream, documentCommand);

        verify(s3ClientMock, times(1)).putObject(Matchers.<PutObjectRequest>anyObject());
    }

    @Test(expected = DocumentManagementException.class)
    public void shouldCatchAmazonServiceExceptionWhenUnableToUploadDocumentToS3() throws IOException {
        initUpload();
        when(s3ClientMock.putObject(Matchers.<PutObjectRequest>anyObject())).thenThrow(new AmazonServiceException("serviceException"));

        S3DocumentStore s3DocumentStore = new S3DocumentStore("bucketName", s3ClientMock);

        s3DocumentStore.saveDocument(inputStream, documentCommand);
    }

    @Test(expected = DocumentManagementException.class)
    public void shouldCatchAmazonClientExceptionWhenUnableToUploadDocumentToS3() throws IOException {
        initUpload();
        when(s3ClientMock.putObject(Matchers.<PutObjectRequest>anyObject())).thenThrow(new AmazonClientException("clientException"));

        S3DocumentStore s3DocumentStore = new S3DocumentStore("bucketName", s3ClientMock);

        s3DocumentStore.saveDocument(inputStream, documentCommand);
    }

    @Test
    public void shouldRetrieveDocumentFromS3() throws IOException {
        initRetrieve();
        when(s3ClientMock.getObject(Matchers.<GetObjectRequest>anyObject())).thenReturn(new S3Object());
        S3DocumentStore s3DocumentStore = new S3DocumentStore("bucketName",s3ClientMock);

        FileData fileData = s3DocumentStore.retrieveDocument(documentDataMock);

        verify(s3ClientMock, times(1)).getObject(Matchers.<GetObjectRequest>anyObject());
        assertEquals(fileData.name(), "name");
        assertEquals(fileData.contentType(), "type");
    }

    @Test(expected = DocumentNotFoundException.class)
    public void shouldCatchAmazonServiceExceptionWhenUnableToRetrieveDocumentFromS3() throws IOException {
        initRetrieve();
        when(s3ClientMock.getObject(Matchers.<GetObjectRequest>anyObject())).thenThrow(new AmazonServiceException("serviceException"));
        S3DocumentStore s3DocumentStore = new S3DocumentStore("bucketName",s3ClientMock);

        s3DocumentStore.retrieveDocument(documentDataMock);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void shouldCatchAmazonClientExceptionWhenUnableToRetrieveDocumentFromS3() throws IOException {
        initRetrieve();
        when(s3ClientMock.getObject(Matchers.<GetObjectRequest>anyObject())).thenThrow(new AmazonClientException("clientException"));
        S3DocumentStore s3DocumentStore = new S3DocumentStore("bucketName",s3ClientMock);

        s3DocumentStore.retrieveDocument(documentDataMock);
    }

    @Test
    public void shouldDeleteImageFromS3() throws IOException {
        AmazonS3Client s3ClientMock = mock(AmazonS3Client.class);
        S3DocumentStore s3DocumentStore = new S3DocumentStore("bucketName",s3ClientMock);

        s3DocumentStore.deleteImage(123L,"imagekey");

        verify(s3ClientMock, times(1)).deleteObject(Matchers.<DeleteObjectRequest>anyObject());
    }

    private void initRetrieve() {
        s3ClientMock = mock(AmazonS3Client.class);

        documentDataMock = mock(DocumentData.class);
        when(documentDataMock.fileLocation()).thenReturn("path");
        when(documentDataMock.fileName()).thenReturn("name");
        when(documentDataMock.contentType()).thenReturn("type");
    }


    private void initUpload(){
        inputStream = S3DocumentStoreTest.class.getResourceAsStream("DummyString");
        s3ClientMock = mock(AmazonS3Client.class);
        documentCommand = new DocumentCommand(null, -1l, "parentEntityType", 1l, null, "someFileName", 3l,  null, null, null);
    }
}
