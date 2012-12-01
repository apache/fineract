package org.mifosplatform.infrastructure.core.domain;

public class Base64EncodedImage {

    private final String base64EncodedString;
    private final String fileExtension;

    public Base64EncodedImage(String base64EncodedString, String fileExtension) {
        this.base64EncodedString = base64EncodedString;
        this.fileExtension = fileExtension;
    }

    public String getBase64EncodedString() {
        return base64EncodedString;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}