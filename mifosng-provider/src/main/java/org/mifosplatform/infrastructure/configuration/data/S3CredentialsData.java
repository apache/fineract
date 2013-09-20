package org.mifosplatform.infrastructure.configuration.data;

public class S3CredentialsData {

    private final String bucketName;
    private final String accessKey;
    private final String secretKey;

    public S3CredentialsData(final String bucketName, final String accessKey, final String secretKey) {
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

}
