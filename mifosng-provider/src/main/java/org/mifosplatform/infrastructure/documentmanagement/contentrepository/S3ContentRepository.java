package org.mifosplatform.infrastructure.documentmanagement.contentrepository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mifosplatform.infrastructure.documentmanagement.data.ImageData;
import org.mifosplatform.infrastructure.documentmanagement.domain.StorageType;
import org.mifosplatform.infrastructure.documentmanagement.exception.ContentManagementException;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.lowagie.text.pdf.codec.Base64;

public class S3ContentRepository implements ContentRepository {

    private final static Logger logger = LoggerFactory.getLogger(S3ContentRepository.class);

    private final String s3BucketName;
    private AmazonS3 s3Client;

    public S3ContentRepository(String bucketName, String secretKey, String accessKey) {
        this.s3BucketName = bucketName;
        this.s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
    }

    @Override
    public String saveFile(InputStream toUpload, DocumentCommand documentCommand) {
        String fileName = documentCommand.getFileName();
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(documentCommand.getSize(), fileName);

        String uploadDocFolder = generateFileParentDirectory(documentCommand.getParentEntityType(), documentCommand.getParentEntityId());
        String uploadDocFullPath = uploadDocFolder + File.separator + fileName;

        uploadDocument(fileName, toUpload, uploadDocFullPath);
        return uploadDocFullPath;
    }

    @Override
    public void deleteFile(String documentName, String documentPath) {
        try {
            deleteObjectFromS3(documentPath);
        } catch (AmazonClientException ace) {
            throw new ContentManagementException(documentName, ace.getMessage());
        }
    }

    @Override
    public String saveImage(InputStream toUploadInputStream, Long resourceId, String imageName, Long fileSize) {
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(fileSize, imageName);
        String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        String fileLocation = uploadImageLocation + File.separator + imageName;

        uploadDocument(imageName, toUploadInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public String saveImage(Base64EncodedImage base64EncodedImage, Long resourceId, String imageName) {
        String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        String fileLocation = uploadImageLocation + File.separator + imageName + base64EncodedImage.getFileExtension();
        InputStream toUploadInputStream = new ByteArrayInputStream(Base64.decode(base64EncodedImage.getBase64EncodedString()));

        uploadDocument(imageName, toUploadInputStream, fileLocation);
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

    @Override
    public StorageType getStorageType() {
        return StorageType.S3;
    }

    @Override
    public FileData fetchFile(DocumentData documentData) throws DocumentNotFoundException {
        FileData fileData = null;
        String fileName = documentData.fileName();
        try {
            logger.info("Downloading an object");
            S3Object s3object = s3Client.getObject(new GetObjectRequest(s3BucketName, documentData.fileLocation()));
            fileData = new FileData(s3object.getObjectContent(), fileName, documentData.contentType());
        } catch (AmazonClientException ace) {
            logger.error(ace.getMessage());
            throw new DocumentNotFoundException(documentData.getParentEntityType(), documentData.getParentEntityId(), documentData.getId());
        }
        return fileData;
    }

    @Override
    public ImageData fetchImage(ImageData imageData) {
        S3Object s3object = s3Client.getObject(new GetObjectRequest(s3BucketName, imageData.location()));
        imageData.updateContent(s3object.getObjectContent());
        return imageData;
    }

    private void deleteObjectAmazonClientExceptionMessage(AmazonClientException ace) {
        String message = "Caught an AmazonClientException." + "Error Message: " + ace.getMessage();
        logger.error(message);
    }

    private void deleteObjectAmazonServiceExceptionMessage(AmazonServiceException ase) {
        String message = "Caught an AmazonServiceException." + "Error Message:    " + ase.getMessage() + "HTTP Status Code: "
                + ase.getStatusCode() + "AWS Error Code:   " + ase.getErrorCode() + "Error Type:       " + ase.getErrorType()
                + "Request ID:       " + ase.getRequestId();
        logger.error(message);
    }

    private String generateFileParentDirectory(String entityType, Long entityId) {
        return "documents" + File.separator + entityType + File.separator + entityId + File.separator
                + ContentRepositoryUtils.generateRandomString();
    }

    private String generateClientImageParentDirectory(Long resourceId) {
        return "images" + File.separator + "clients" + File.separator + resourceId;
    }

    private void deleteObjectFromS3(String location) {
        s3Client.deleteObject(new DeleteObjectRequest(s3BucketName, location));
    }

    private void uploadDocument(String filename, InputStream inputStream, String s3UploadLocation) throws ContentManagementException {
        try {
            logger.info("Uploading a new object to S3 from a file to " + s3UploadLocation);
            s3Client.putObject(new PutObjectRequest(this.s3BucketName, s3UploadLocation, inputStream, new ObjectMetadata()));
        } catch (AmazonClientException ace) {
            String message = ace.getMessage();
            throw new ContentManagementException(filename, message);
        }
    }
}
