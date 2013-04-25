package org.mifosplatform.infrastructure.core.service;

public enum DocumentStoreType {
    FILE_SYSTEM("file_system"), S3("s3");

    private String value;

    DocumentStoreType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
 }