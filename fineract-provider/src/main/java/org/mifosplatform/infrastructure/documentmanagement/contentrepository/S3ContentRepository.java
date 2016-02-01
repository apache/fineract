/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
    private final AmazonS3 s3Client;

    public S3ContentRepository(final String bucketName, final String secretKey, final String accessKey) {
        this.s3BucketName = bucketName;
        this.s3Client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
    }

    @Override
    public String saveFile(final InputStream toUpload, final DocumentCommand documentCommand) {
        final String fileName = documentCommand.getFileName();
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(documentCommand.getSize(), fileName);

        final String uploadDocFolder = generateFileParentDirectory(documentCommand.getParentEntityType(),
                documentCommand.getParentEntityId());
        final String uploadDocFullPath = uploadDocFolder + File.separator + fileName;

        uploadDocument(fileName, toUpload, uploadDocFullPath);
        return uploadDocFullPath;
    }

    @Override
    public void deleteFile(final String documentName, final String documentPath) {
        try {
            deleteObjectFromS3(documentPath);
        } catch (final AmazonClientException ace) {
            throw new ContentManagementException(documentName, ace.getMessage());
        }
    }

    @Override
    public String saveImage(final InputStream toUploadInputStream, final Long resourceId, final String imageName, final Long fileSize) {
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(fileSize, imageName);
        final String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        final String fileLocation = uploadImageLocation + File.separator + imageName;

        uploadDocument(imageName, toUploadInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public String saveImage(final Base64EncodedImage base64EncodedImage, final Long resourceId, final String imageName) {
        final String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        final String fileLocation = uploadImageLocation + File.separator + imageName + base64EncodedImage.getFileExtension();
        final InputStream toUploadInputStream = new ByteArrayInputStream(Base64.decode(base64EncodedImage.getBase64EncodedString()));

        uploadDocument(imageName, toUploadInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public void deleteImage(final Long resourceId, final String location) {
        try {
            deleteObjectFromS3(location);
        } catch (final AmazonServiceException ase) {
            deleteObjectAmazonServiceExceptionMessage(ase);
            logger.warn("Unable to delete image associated with clients with Id " + resourceId);
        } catch (final AmazonClientException ace) {
            deleteObjectAmazonClientExceptionMessage(ace);
            logger.warn("Unable to delete image associated with clients with Id " + resourceId);
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.S3;
    }

    @Override
    public FileData fetchFile(final DocumentData documentData) throws DocumentNotFoundException {
        FileData fileData = null;
        final String fileName = documentData.fileName();
        try {
            logger.info("Downloading an object");
            final S3Object s3object = this.s3Client.getObject(new GetObjectRequest(this.s3BucketName, documentData.fileLocation()));
            fileData = new FileData(s3object.getObjectContent(), fileName, documentData.contentType());
        } catch (final AmazonClientException ace) {
            logger.error(ace.getMessage());
            throw new DocumentNotFoundException(documentData.getParentEntityType(), documentData.getParentEntityId(), documentData.getId());
        }
        return fileData;
    }

    @Override
    public ImageData fetchImage(final ImageData imageData) {
        final S3Object s3object = this.s3Client.getObject(new GetObjectRequest(this.s3BucketName, imageData.location()));
        imageData.updateContent(s3object.getObjectContent());
        return imageData;
    }

    private void deleteObjectAmazonClientExceptionMessage(final AmazonClientException ace) {
        final String message = "Caught an AmazonClientException." + "Error Message: " + ace.getMessage();
        logger.error(message);
    }

    private void deleteObjectAmazonServiceExceptionMessage(final AmazonServiceException ase) {
        final String message = "Caught an AmazonServiceException." + "Error Message:    " + ase.getMessage() + "HTTP Status Code: "
                + ase.getStatusCode() + "AWS Error Code:   " + ase.getErrorCode() + "Error Type:       " + ase.getErrorType()
                + "Request ID:       " + ase.getRequestId();
        logger.error(message);
    }

    private String generateFileParentDirectory(final String entityType, final Long entityId) {
        return "documents" + File.separator + entityType + File.separator + entityId + File.separator
                + ContentRepositoryUtils.generateRandomString();
    }

    private String generateClientImageParentDirectory(final Long resourceId) {
        return "images" + File.separator + "clients" + File.separator + resourceId;
    }

    private void deleteObjectFromS3(final String location) {
        this.s3Client.deleteObject(new DeleteObjectRequest(this.s3BucketName, location));
    }

    private void uploadDocument(final String filename, final InputStream inputStream, final String s3UploadLocation)
            throws ContentManagementException {
        try {
            logger.info("Uploading a new object to S3 from a file to " + s3UploadLocation);
            this.s3Client.putObject(new PutObjectRequest(this.s3BucketName, s3UploadLocation, inputStream, new ObjectMetadata()));
        } catch (final AmazonClientException ace) {
            final String message = ace.getMessage();
            throw new ContentManagementException(filename, message);
        }
    }
}
