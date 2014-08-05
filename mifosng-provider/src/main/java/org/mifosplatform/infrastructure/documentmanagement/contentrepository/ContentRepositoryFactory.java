/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.contentrepository;

import org.mifosplatform.infrastructure.configuration.data.S3CredentialsData;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.configuration.service.ExternalServicesReadPlatformService;
import org.mifosplatform.infrastructure.documentmanagement.domain.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ContentRepositoryFactory {

    private final ApplicationContext applicationContext;
    private final ExternalServicesReadPlatformService externalServicesReadPlatformService;

    @Autowired
    public ContentRepositoryFactory(final ApplicationContext applicationContext,
            final ExternalServicesReadPlatformService externalServicesReadPlatformService) {
        this.applicationContext = applicationContext;
        this.externalServicesReadPlatformService = externalServicesReadPlatformService;
    }

    public ContentRepository getRepository() {
        final ConfigurationDomainService configurationDomainServiceJpa = this.applicationContext.getBean("configurationDomainServiceJpa",
                ConfigurationDomainService.class);
        if (configurationDomainServiceJpa.isAmazonS3Enabled()) { return createS3DocumentStore(); }
        return new FileSystemContentRepository();
    }

    public ContentRepository getRepository(final StorageType documentStoreType) {
        if (documentStoreType == StorageType.FILE_SYSTEM) { return new FileSystemContentRepository(); }
        return createS3DocumentStore();
    }

    private ContentRepository createS3DocumentStore() {
        final S3CredentialsData s3CredentialsData = this.externalServicesReadPlatformService.getS3Credentials();
        return new S3ContentRepository(s3CredentialsData.getBucketName(), s3CredentialsData.getSecretKey(),
                s3CredentialsData.getAccessKey());
    }
}
