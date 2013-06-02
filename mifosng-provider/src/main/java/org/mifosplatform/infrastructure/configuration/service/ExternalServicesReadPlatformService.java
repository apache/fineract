package org.mifosplatform.infrastructure.configuration.service;

import org.mifosplatform.infrastructure.configuration.data.S3CredentialsData;

public interface ExternalServicesReadPlatformService {

    S3CredentialsData getS3Credentials();

}