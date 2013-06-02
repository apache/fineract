package org.mifosplatform.infrastructure.documentmanagement.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.mifosplatform.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.mifosplatform.infrastructure.documentmanagement.domain.StorageType;

public class ImageData {

    @SuppressWarnings("unused")
    private final Long imageId;
    private final String location;
    private final Integer storageType;
    private final String entityDisplayName;

    private File file;
    private String contentType;
    private InputStream inputStream;

    public ImageData(Long imageId, String location, Integer storageType, String entityDisplayName) {
        this.imageId = imageId;
        this.location = location;
        this.storageType = storageType;
        this.entityDisplayName = entityDisplayName;
    }

    public byte[] getContent() {
        // TODO Vishwas Fix error handling
        try {
            if (inputStream == null) {
                FileInputStream fileInputStream = new FileInputStream(file);
                return IOUtils.toByteArray(fileInputStream);
            }

            return IOUtils.toByteArray(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    private String setImageContentType() {
        String contentType = ContentRepositoryUtils.IMAGE_MIME_TYPE.JPEG.getValue();

        if (file != null) {
            String fileName = file.getName();

            if (StringUtils.endsWith(fileName, ContentRepositoryUtils.IMAGE_FILE_EXTENSION.GIF.getValue())) {
                contentType = ContentRepositoryUtils.IMAGE_MIME_TYPE.GIF.getValue();
            } else if (StringUtils.endsWith(fileName, ContentRepositoryUtils.IMAGE_FILE_EXTENSION.PNG.getValue())) {
                contentType = ContentRepositoryUtils.IMAGE_MIME_TYPE.PNG.getValue();
            }
        }
        return contentType;
    }

    public String contentType() {
        return this.contentType;
    }

    public StorageType storageType() {
        return StorageType.fromInt(storageType);
    }

    public String name() {
        return this.file.getName();
    }

    public String location() {
        return this.location;
    }

    public void updateContent(InputStream objectContent) {
        this.inputStream = objectContent;
    }

    public void updateContent(File file) {
        this.file = file;
        setImageContentType();
    }

    public String getEntityDisplayName() {
        return this.entityDisplayName;
    }

}
