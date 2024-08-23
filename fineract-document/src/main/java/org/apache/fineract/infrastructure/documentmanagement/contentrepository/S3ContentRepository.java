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

import com.google.common.io.ByteSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.apache.fineract.infrastructure.security.utils.LogParameterEscapeUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty("fineract.content.s3.enabled")
public class S3ContentRepository implements ContentRepository {

    private final S3Client s3Client;
    private final FineractProperties fineractProperties;

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
                return s3Client.getObject(GetObjectRequest.builder().bucket(fineractProperties.getContent().getS3().getBucketName())
                        .key(documentData.fileLocation()).build(), ResponseTransformer.toBytes()).asInputStream();
            }
        }, documentData.fileName(), documentData.contentType());
    }

    @Override
    public FileData fetchImage(final ImageData imageData) {
        return new FileData(new ByteSource() {

            @Override
            public InputStream openStream() throws IOException {
                return s3Client.getObject(GetObjectRequest.builder().bucket(fineractProperties.getContent().getS3().getBucketName())
                        .key(imageData.location()).build(), ResponseTransformer.toBytes()).asInputStream();
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
            this.s3Client.deleteObject(builder -> builder.bucket(fineractProperties.getContent().getS3().getBucketName()).key(location));
        } catch (final AwsServiceException ase) {
            throw new ContentManagementException(location,
                    "message=" + ase.getMessage() + ", Error Code=" + ase.awsErrorDetails().errorCode(), ase);
        } catch (final SdkException ace) {
            throw new ContentManagementException(location, ace.getMessage(), ace);
        }
    }

    public void putObject(final String filename, final InputStream inputStream, final String s3UploadLocation)
            throws ContentManagementException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Uploading a new object to S3 {}", LogParameterEscapeUtil.escapeLogParameter(s3UploadLocation));
            }
            this.s3Client.putObject(
                    builder -> builder.bucket(fineractProperties.getContent().getS3().getBucketName()).key(s3UploadLocation),
                    RequestBody.fromBytes(IOUtils.toByteArray(inputStream)));
        } catch (AwsServiceException | IOException ase) {
            throw new ContentManagementException(filename, ase.getMessage(), ase);
        }
    }

    public ResponseBytes<GetObjectResponse> getObject(String key) {
        try {
            log.debug("Downloading an object from Amazon S3 Bucket: {}, location: {}",
                    fineractProperties.getContent().getS3().getBucketName(), key);
            return this.s3Client.getObject(builder -> builder.bucket(fineractProperties.getContent().getS3().getBucketName()).key(key),
                    ResponseTransformer.toBytes());
        } catch (SdkException ase) {
            throw new ContentManagementException(key, ase.getMessage(), ase);
        }
    }
}
