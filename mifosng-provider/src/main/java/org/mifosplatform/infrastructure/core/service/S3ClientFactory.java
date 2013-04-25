package org.mifosplatform.infrastructure.core.service;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.mifosplatform.infrastructure.configuration.domain.ExternalServicesDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class S3ClientFactory {

    private ExternalServicesDomainService ExternalServicesDomainService;

    @Autowired
    public S3ClientFactory(final ExternalServicesDomainService ExternalServicesDomainService) {
        this.ExternalServicesDomainService = ExternalServicesDomainService;
    }

    public AmazonS3Client getS3Client() {
        String s3_access_key = ExternalServicesDomainService.getValue("s3_access_key");
        String s3_secret_key = ExternalServicesDomainService.getValue("s3_secret_key");
        return new AmazonS3Client(new BasicAWSCredentials(s3_access_key,s3_secret_key));
    }
}
