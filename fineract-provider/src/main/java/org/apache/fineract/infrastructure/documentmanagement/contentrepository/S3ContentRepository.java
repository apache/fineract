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
package org.apache.fineract.infrastructure.documentmanagement.contentrepository;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.io.ByteSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3ContentRepository implements ContentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(S3ContentRepository.class);

    private final String s3BucketName;
    private final AmazonS3 s3Client;

    public S3ContentRepository(final String bucketName, final String secretKey, final String accessKey) {
        this.s3BucketName = bucketName;
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))).build();
    }

    @Override
    public String saveFile(final InputStream toUpload, final DocumentCommand documentCommand) {
        final String fileName = documentCommand.getFileName();
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(documentCommand.getSize(), fileName);

        final String uploadDocFolder = generateFileParentDirectory(documentCommand.getParentEntityType(),
                documentCommand.getParentEntityId());
        final String uploadDocFullPath = uploadDocFolder + File.separator + fileName;

        putObject(fileName, toUpload, uploadDocFullPath);
        return uploadDocFullPath;
    }

    @Override
    public void deleteFile(final String documentPath) {
        deleteObject(documentPath);
    }

    @Override
    public String saveImage(final InputStream toUploadInputStream, final Long resourceId, final String imageName, final Long fileSize) {
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(fileSize, imageName);
        final String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        final String fileLocation = uploadImageLocation + File.separator + imageName;

        putObject(imageName, toUploadInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public String saveImage(final Base64EncodedImage base64EncodedImage, final Long resourceId, final String imageName) {
        final String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        final String fileLocation = uploadImageLocation + File.separator + imageName + base64EncodedImage.getFileExtension();
        final InputStream toUploadInputStream = new ByteArrayInputStream(
                Base64.getMimeDecoder().decode(base64EncodedImage.getBase64EncodedString()));

        putObject(imageName, toUploadInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public void deleteImage(final String location) {
        deleteObject(location);
    }

    @Override
    public FileData fetchFile(final DocumentData documentData) throws DocumentNotFoundException {
        return new FileData(new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                final S3Object s3object = getObject(documentData.fileLocation());
                return s3object.getObjectContent();
            }
        }, documentData.fileName(), documentData.contentType());
    }

    @Override
    public FileData fetchImage(final ImageData imageData) {
        return new FileData(new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                final S3Object s3object = getObject(imageData.location());
                return s3object.getObjectContent();
            }
        }, imageData.getEntityDisplayName(), imageData.contentType().getValue());
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.S3;
    }

    private String generateFileParentDirectory(final String entityType, final Long entityId) {
        return "documents" + File.separator + entityType + File.separator + entityId + File.separator
                + ContentRepositoryUtils.generateRandomString();
    }

    private String generateClientImageParentDirectory(final Long resourceId) {
        return "images" + File.separator + "clients" + File.separator + resourceId;
    }

    private void deleteObject(final String location) {
        try {
            this.s3Client.deleteObject(new DeleteObjectRequest(this.s3BucketName, location));
        } catch (final AmazonServiceException ase) {
            throw new ContentManagementException(location, "message=" + ase.getMessage() + ", Error Type=" + ase.getErrorType(), ase);
        } catch (final AmazonClientException ace) {
            throw new ContentManagementException(location, ace.getMessage(), ace);
        }
    }

    private void putObject(final String filename, final InputStream inputStream, final String s3UploadLocation)
            throws ContentManagementException {
        try {
            LOG.info("Uploading a new object to S3 {}", s3UploadLocation);
            this.s3Client.putObject(new PutObjectRequest(this.s3BucketName, s3UploadLocation, inputStream, new ObjectMetadata()));
        } catch (AmazonServiceException ase) {
            throw new ContentManagementException(filename, ase.getMessage(), ase);
        } catch (final AmazonClientException ace) {
            throw new ContentManagementException(filename, ace.getMessage(), ace);
        }
    }

    private S3Object getObject(String key) {
        try {
            LOG.info("Downloading an object from Amazon S3 Bucket: {}, location: {}", this.s3BucketName, key);
            return this.s3Client.getObject(new GetObjectRequest(this.s3BucketName, key));
        } catch (AmazonServiceException ase) {
            throw new ContentManagementException(key, ase.getMessage(), ase);
        } catch (final AmazonClientException ace) {
            throw new ContentManagementException(key, ace.getMessage(), ace);
        }
    }
}
