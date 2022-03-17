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

import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemContentRepository implements ContentRepository {

    private static final Logger LOG = LoggerFactory.getLogger(FileSystemContentRepository.class);

    public static final String FINERACT_BASE_DIR = System.getProperty("user.home") + File.separator + ".fineract";

    @Override
    public String saveFile(final InputStream uploadedInputStream, final DocumentCommand documentCommand) {
        final String fileName = documentCommand.getFileName();
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(documentCommand.getSize(), fileName);

        final String fileLocation = generateFileParentDirectory(documentCommand.getParentEntityType(), documentCommand.getParentEntityId())
                + File.separator + fileName;

        writeFileToFileSystem(fileName, uploadedInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public String saveImage(final InputStream uploadedInputStream, final Long resourceId, final String imageName, final Long fileSize) {
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(fileSize, imageName);
        final String fileLocation = generateClientImageParentDirectory(resourceId) + File.separator + imageName;
        writeFileToFileSystem(imageName, uploadedInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public String saveImage(final Base64EncodedImage base64EncodedImage, final Long resourceId, final String imageName) {
        final String fileLocation = generateClientImageParentDirectory(resourceId) + File.separator + imageName
                + base64EncodedImage.getFileExtension();
        String base64EncodedImageString = base64EncodedImage.getBase64EncodedString();
        try {
            final InputStream toUploadInputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(base64EncodedImageString));
            writeFileToFileSystem(imageName, toUploadInputStream, fileLocation);
            return fileLocation;
        } catch (IllegalArgumentException iae) {
            LOG.error("IllegalArgumentException due to invalid Base64 encoding: {}", base64EncodedImageString, iae);
            throw iae;
        }
    }

    @Override
    public void deleteImage(final String location) {
        deleteFileInternal(location);
    }

    @Override
    public void deleteFile(final String documentPath) {
        deleteFileInternal(documentPath);
    }

    private void deleteFileInternal(final String documentPath) {
        final File fileToBeDeleted = new File(documentPath);
        final boolean fileDeleted = fileToBeDeleted.delete();
        if (!fileDeleted) {
            // no need to throw an Error, what's a caller going to do about it, so simply log a warning
            LOG.warn("Unable to delete file {}", documentPath);
        }
    }

    @Override
    public FileData fetchFile(final DocumentData documentData) {
        final File file = new File(documentData.fileLocation());
        return new FileData(Files.asByteSource(file), documentData.fileName(), documentData.contentType());
    }

    @Override
    public FileData fetchImage(final ImageData imageData) {
        final File file = new File(imageData.location());
        return new FileData(Files.asByteSource(file), imageData.getEntityDisplayName(), imageData.contentType().getValue());
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.FILE_SYSTEM;
    }

    /**
     * Generate the directory path for storing the new document
     */
    private String generateFileParentDirectory(final String entityType, final Long entityId) {
        return FileSystemContentRepository.FINERACT_BASE_DIR + File.separator
                + ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim() + File.separator + "documents" + File.separator
                + entityType + File.separator + entityId + File.separator + ContentRepositoryUtils.generateRandomString();
    }

    /**
     * Generate directory path for storing new Image
     */
    private String generateClientImageParentDirectory(final Long resourceId) {
        return FileSystemContentRepository.FINERACT_BASE_DIR + File.separator
                + ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim() + File.separator + "images" + File.separator
                + "clients" + File.separator + resourceId;
    }

    /**
     * Recursively create the directory if it does not exist.
     */
    private void makeDirectories(final String uploadDocumentLocation) throws IOException {
        Files.createParentDirs(new File(uploadDocumentLocation));
    }

    private void writeFileToFileSystem(final String fileName, final InputStream uploadedInputStream, final String fileLocation) {
        try {
            makeDirectories(fileLocation);
            FileUtils.copyInputStreamToFile(uploadedInputStream, new File(fileLocation)); // NOSONAR
        } catch (final IOException ioException) {
            LOG.warn("writeFileToFileSystem() IOException (logged because cause is not propagated in ContentManagementException)",
                    ioException);
            throw new ContentManagementException(fileName, ioException.getMessage(), ioException);
        }
    }
}
