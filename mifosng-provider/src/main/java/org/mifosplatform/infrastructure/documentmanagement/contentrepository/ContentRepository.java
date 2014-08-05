/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.contentrepository;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;
import org.mifosplatform.infrastructure.documentmanagement.data.ImageData;
import org.mifosplatform.infrastructure.documentmanagement.domain.StorageType;

public interface ContentRepository {

    public StorageType type = null;

    // TODO:Vishwas Need to move these settings to the Database
    public static final Integer MAX_FILE_UPLOAD_SIZE_IN_MB = 5;

    // TODO:Vishwas Need to move these settings to the Database
    public static final Integer MAX_IMAGE_UPLOAD_SIZE_IN_MB = 1;

    public abstract String saveFile(InputStream uploadedInputStream, DocumentCommand documentCommand);

    public abstract void deleteFile(String fileName, String documentPath);

    public abstract FileData fetchFile(DocumentData documentData);

    public abstract String saveImage(InputStream uploadedInputStream, Long resourceId, String imageName, Long fileSize);

    public abstract String saveImage(Base64EncodedImage base64EncodedImage, Long resourceId, String imageName);

    public abstract void deleteImage(final Long resourceId, final String location);

    public abstract ImageData fetchImage(ImageData imageData);

    public abstract StorageType getStorageType();

}
