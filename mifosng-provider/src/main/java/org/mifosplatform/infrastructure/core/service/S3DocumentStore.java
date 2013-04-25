package org.mifosplatform.infrastructure.core.service;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.lowagie.text.pdf.codec.Base64;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.mifosplatform.portfolio.client.data.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class S3DocumentStore extends DocumentStore {

    private final static Logger logger = LoggerFactory.getLogger(S3DocumentStore.class);

    private final String s3BucketName;
    private AmazonS3 s3Client;

    public S3DocumentStore(String s3BucketName, AmazonS3 s3Client) {
        this.s3BucketName = s3BucketName;
        this.s3Client = s3Client;
    }

    @Override
    public String saveDocument(InputStream toUpload, DocumentCommand documentCommand) throws DocumentManagementException {
        String documentName = documentCommand.getFileName();
        validateFileSizeWithinPermissibleRange(documentCommand.getSize(), documentName, maxFileSize);

        String uploadDocFolder = generateFileParentDirectory(documentCommand.getParentEntityType(), documentCommand.getParentEntityId());
        String uploadDocFullPath = uploadDocFolder + File.separator + documentName;

        uploadDocument(toUpload, uploadDocFullPath);
        return uploadDocFullPath;
    }

    @Override
    public void deleteDocument(String documentName, String documentPath) throws DocumentManagementException {
        try{
            deleteObjectFromS3(documentPath);
        }catch (AmazonServiceException ase){
            deleteObjectAmazonServiceExceptionMessage(ase);
            throw new DocumentManagementException(documentName);
        }catch (AmazonClientException ace){
            deleteObjectAmazonClientExceptionMessage(ace);
            throw new DocumentManagementException(documentName);
        }
    }

    @Override
    public String saveImage(InputStream toUploadInputStream, Long resourceId, String imageName, Long fileSize) throws DocumentManagementException {
        validateFileSizeWithinPermissibleRange(fileSize, imageName, maxImageSize);

        String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        String fileLocation = uploadImageLocation + File.separator + imageName;

        uploadDocument(toUploadInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public String saveImage(Base64EncodedImage base64EncodedImage, Long resourceId, String imageName) throws DocumentManagementException {
        String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        String fileLocation = uploadImageLocation + File.separator + imageName + base64EncodedImage.getFileExtension();
        InputStream toUploadInputStream = new ByteArrayInputStream(Base64.decode(base64EncodedImage.getBase64EncodedString()));

        uploadDocument(toUploadInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public void deleteImage(Long resourceId, String location) {
        try {
            deleteObjectFromS3(location);
        } catch (AmazonServiceException ase) {
            deleteObjectAmazonServiceExceptionMessage(ase);
            logger.warn("Unable to delete image associated with clients with Id " + resourceId);
        } catch (AmazonClientException ace) {
            deleteObjectAmazonClientExceptionMessage(ace);
            logger.warn("Unable to delete image associated with clients with Id " + resourceId);
        }
    }

    private void deleteObjectAmazonClientExceptionMessage(AmazonClientException ace) {
        String message = "Caught an AmazonClientException." +
                "Error Message: " + ace.getMessage();
        logger.error(message);
    }

    private void deleteObjectAmazonServiceExceptionMessage(AmazonServiceException ase) {
        String message = "Caught an AmazonServiceException." +
                "Error Message:    " + ase.getMessage() +
                "HTTP Status Code: " + ase.getStatusCode() +
                "AWS Error Code:   " + ase.getErrorCode() +
                "Error Type:       " + ase.getErrorType() +
                "Request ID:       " + ase.getRequestId();
        logger.error(message);
    }

    private void deleteObjectFromS3(String location) {
        s3Client.deleteObject(new DeleteObjectRequest(s3BucketName, location));
    }

    @Override
    public DocumentStoreType getType() {
        return DocumentStoreType.S3;
    }

    @Override
    public FileData retrieveDocument(DocumentData documentData) throws DocumentNotFoundException {
        FileData fileData = null;
        String fileName = documentData.fileName();
        try {
            logger.info("Downloading an object");
            S3Object s3object = s3Client.getObject(new GetObjectRequest(s3BucketName, documentData.fileLocation()));
            fileData = new FileData(s3object.getObjectContent(), fileName, documentData.contentType());
        } catch (AmazonServiceException ase) {
            getObjectAmazonServiceExceptionMessage(ase,fileName);
            throw new DocumentNotFoundException(documentData.getParentEntityType(), documentData.getParentEntityId(), documentData.getId());
        } catch (AmazonClientException ace) {
            getObjectAmazonClientExceptionMessage(ace,fileName);
            throw new DocumentNotFoundException(documentData.getParentEntityType(), documentData.getParentEntityId(), documentData.getId());
        }
        return fileData;
    }

    @Override
    public ImageData retrieveImage(ImageData imageData) {
        S3Object s3object = s3Client.getObject(new GetObjectRequest(s3BucketName, imageData.imageKey()));
        imageData.setContent(s3object.getObjectContent());
        return imageData;
    }

    String generateFileParentDirectory(String entityType, Long entityId) {
        return "documents" + File.separator + entityType + File.separator + entityId + File.separator + RandomStringGenerator.generateRandomString();
    }

    String generateClientImageParentDirectory(Long resourceId) {
        return "images" + File.separator + "clients" + File.separator + resourceId;
    }

    private void uploadDocument(InputStream inputStream, String s3UploadLocation) throws DocumentManagementException {
        try {
            logger.info("Uploading a new object to S3 from a file to " + s3UploadLocation);
            s3Client.putObject(new PutObjectRequest(this.s3BucketName, s3UploadLocation, inputStream, new ObjectMetadata()));

        } catch (AmazonServiceException ase) {
            String message = "Caught an AmazonServiceException, which " + "means your request made it " + "to Amazon S3, but was rejected with an error response" +
                    " for some reason. \n Error Message:    " + ase.getMessage() +
                    "HTTP Status Code: " + ase.getStatusCode() +
                    "AWS Error Code:   " + ase.getErrorCode() +
                    "Error Type:       " + ase.getErrorType() +
                    "Request ID:       " + ase.getRequestId();

            logger.error(message);
            throw new DocumentManagementException(message);
        } catch (AmazonClientException ace) {
            String message = "Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network." +
                    "Error Message: " + ace.getMessage();
            logger.error(message);
            throw new DocumentManagementException(message);
        }
    }

    private void getObjectAmazonClientExceptionMessage(AmazonClientException ace, String filename) {
        String message = "Caught an AmazonClientException, which means the client encountered " +
                "an internal error while trying to communicate with S3, " +
                "such as not being able to access the network." +
                "Error Message: " + ace.getMessage() +
                "FileName:      " + filename;

        logger.error(message);
    }

    private void getObjectAmazonServiceExceptionMessage(AmazonServiceException ase, String filename) {
        String message = "Caught an AmazonServiceException, which means your request made it " +
                "to Amazon S3, but was rejected with an error response" +
                " for some reason." +
                "Error Message:    " + ase.getMessage() +
                "HTTP Status Code: " + ase.getStatusCode() +
                "AWS Error Code:   " + ase.getErrorCode() +
                "Error Type:       " + ase.getErrorType() +
                "Request ID:       " + ase.getRequestId() +
                "FileName:         " + filename;
        logger.error(message);
    }
}
