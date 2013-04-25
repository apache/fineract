package org.mifosplatform.portfolio.client.data;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.mifosplatform.infrastructure.core.service.DocumentStore;
import org.mifosplatform.infrastructure.core.service.DocumentStoreType;
import org.mifosplatform.infrastructure.core.service.ImageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageData {

    private final Long clientId;
    private final String key;
    //    private final DocumentStoreType storeType;
    private final String storeType;
    private File file;
    private String contentType;
    private String name;
    private InputStream inputStream;


    public ImageData(Long clientId, String key, String storeType) {
        this.clientId = clientId;
        this.key = key;
        this.storeType = storeType;
    }

    public String imageKey() {
        return this.key;
    }

    public byte[] getContent() throws IOException {
        if(inputStream == null){
            FileInputStream fileInputStream = new FileInputStream(file);
            return IOUtils.toByteArray(fileInputStream);
        }
        else
        {
            return IOUtils.toByteArray(inputStream);
        }
    }

    private String setImageContentType() {
        String fileName = file.getName();
        String contentType = ImageUtils.IMAGE_MIME_TYPE.JPEG.getValue();
        if (StringUtils.endsWith(fileName, ImageUtils.IMAGE_FILE_EXTENSION.GIF.getValue())) {
            contentType = ImageUtils.IMAGE_MIME_TYPE.GIF.getValue();
        } else if (StringUtils.endsWith(fileName, ImageUtils.IMAGE_FILE_EXTENSION.PNG.getValue())) {
            contentType = ImageUtils.IMAGE_MIME_TYPE.PNG.getValue();
        }
        return contentType;
    }

    public String contentType(){
        return this.contentType;
    }

    public DocumentStoreType storeType() {
        return DocumentStore.getDocumentStoreType(storeType);
    }

    public String name(){
        return this.file.getName();
    }


    public void setContent(InputStream objectContent) {
        this.inputStream = objectContent;
    }

    public void setContent(File file) {
        this.file = file;
    }
}
