/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.service;

import com.lowagie.text.pdf.codec.Base64;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.mifosplatform.portfolio.client.data.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileSystemDocumentStore extends DocumentStore {

    private final static Logger logger = LoggerFactory.getLogger(FileSystemDocumentStore.class);

    public static final String MIFOSX_BASE_DIR = System.getProperty("user.home") + File.separator + ".mifosx";


    @Override
    public String saveDocument(InputStream uploadedInputStream, DocumentCommand documentCommand) throws DocumentManagementException {
        String documentName = documentCommand.getFileName();
        String uploadDocumentLocation = generateFileParentDirectory(documentCommand.getParentEntityType(), documentCommand.getParentEntityId());

        validateFileSizeWithinPermissibleRange(documentCommand.getSize(), documentName, maxFileSize);
        makeDirectories(uploadDocumentLocation);

        String fileLocation = uploadDocumentLocation + File.separator + documentName;

        writeFileToFileSystem(uploadedInputStream, fileLocation);
        return fileLocation;
    }


    @Override
    public String saveImage(InputStream uploadedInputStream, Long resourceId, String imageName, Long fileSize) throws DocumentManagementException {
        String uploadImageLocation = generateClientImageParentDirectory(resourceId);

        validateFileSizeWithinPermissibleRange(fileSize, imageName, maxImageSize);
        makeDirectories(uploadImageLocation);

        String fileLocation = uploadImageLocation + File.separator + imageName;

        writeFileToFileSystem(uploadedInputStream, fileLocation);
        return fileLocation;
    }

    @Override
    public String saveImage(Base64EncodedImage base64EncodedImage, Long resourceId, String imageName) throws DocumentManagementException {
        String uploadImageLocation = generateClientImageParentDirectory(resourceId);

        makeDirectories(uploadImageLocation);

        String fileLocation = uploadImageLocation + File.separator + imageName + base64EncodedImage.getFileExtension();
        try {
            OutputStream out = new FileOutputStream(new File(fileLocation));
            byte[] imgBytes = Base64.decode(base64EncodedImage.getBase64EncodedString());
            out.write(imgBytes);
            out.flush();
            out.close();
        }catch (IOException ioe){
            throw new DocumentManagementException(ioe.getMessage());
        }
        return fileLocation;
    }

    @Override
    public void deleteImage(final Long resourceId, final String location) {
        final boolean fileDeleted = deleteFile(location);
        if (!fileDeleted) {
            // no need to throw an Error, simply log a warning
            logger.warn("Unable to delete image associated with clients with Id " + resourceId);
        }
    }

    @Override
    public void deleteDocument(String documentName, String documentPath) throws DocumentManagementException{
        final boolean fileDeleted = deleteFile(documentPath);
        if (!fileDeleted) { throw new DocumentManagementException(documentName); }
    }

    private boolean deleteFile(String documentPath) {
        final File fileToBeDeleted = new File(documentPath);
        return fileToBeDeleted.delete();
    }

    @Override
    public DocumentStoreType getType() {
        return DocumentStoreType.FILE_SYSTEM;
    }

    @Override
    public FileData retrieveDocument(DocumentData documentData){
        File file = new File(documentData.fileLocation());
        return new FileData(file, documentData.fileName(), documentData.contentType());
    }

    @Override
    public ImageData retrieveImage(ImageData imageData) {
        File file = new File(imageData.imageKey());
        imageData.setContent(file);
        return imageData;
    }

    /**
     * Generate the directory path for storing the new document
     *
     * @param entityType
     * @param entityId
     * @return
     */
    private String generateFileParentDirectory(String entityType, Long entityId) {
        return FileSystemDocumentStore.MIFOSX_BASE_DIR + File.separator + ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim()
                + File.separator + "documents" + File.separator + entityType + File.separator + entityId + File.separator
                + RandomStringGenerator.generateRandomString();
    }

    /**
     * Generate directory path for storing new Image
     */
    private String generateClientImageParentDirectory(final Long resourceId) {
        return FileSystemDocumentStore.MIFOSX_BASE_DIR + File.separator + ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim()
                + File.separator + "images" + File.separator + "clients" + File.separator + resourceId;
    }

    /**
     * Recursively create the directory if it does not exist *
     */
    private void makeDirectories(String uploadDocumentLocation) {
        if (!new File(uploadDocumentLocation).isDirectory()) {
            new File(uploadDocumentLocation).mkdirs();
        }
    }


    private void writeFileToFileSystem(InputStream uploadedInputStream, String fileLocation) throws DocumentManagementException {

        try {
            OutputStream out = new FileOutputStream(new File(fileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException ioException) {
            throw new DocumentManagementException("IO Exception caught while writing to FileSystem" + ioException.getMessage());
        }
    }

}