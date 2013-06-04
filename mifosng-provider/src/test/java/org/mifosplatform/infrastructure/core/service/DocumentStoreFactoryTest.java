package org.mifosplatform.infrastructure.core.service;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DocumentStoreFactoryTest {
/**
    @Test
    public void shouldReturnS3DocumentStoreObjectIfS3Enabled() {
        S3ClientFactory s3ClientFactoryMock = mock(S3ClientFactory.class);
        when(s3ClientFactoryMock.getS3Client()).thenReturn(null);

        ExternalServicesReadPlatformServiceImpl externalServicesDomainServiceMock = mock(ExternalServicesReadPlatformServiceImpl.class);
        when(externalServicesDomainServiceMock.getValue("s3_bucket_name")).thenReturn("bucket_name");

        ConfigurationDomainService configurationDomainServiceMock = mock(ConfigurationDomainService.class);
        when(configurationDomainServiceMock.isAmazonS3Enabled()).thenReturn(true);

        ApplicationContext applicationContextMock = mock(ApplicationContext.class);

        when(applicationContextMock.getBean("configurationDomainServiceJpa", ConfigurationDomainService.class)).thenReturn(
                configurationDomainServiceMock);
        when(applicationContextMock.getBean("s3ClientFactory", S3ClientFactory.class)).thenReturn(s3ClientFactoryMock);
        when(applicationContextMock.getBean("externalServicesDomainService", ExternalServicesReadPlatformServiceImpl.class)).thenReturn(
                externalServicesDomainServiceMock);

        ContentRepositoryFactory documentStoreFactory = new ContentRepositoryFactory(applicationContextMock);
        assertEquals(documentStoreFactory.getInstanceFromConfiguration().getClass(), S3ContentRepository.class);
    }

    @Test
    public void shouldReturnFileSystemDocumentStoreObjectIfS3IsDisabled() {
        ConfigurationDomainService configurationDomainService = mock(ConfigurationDomainService.class);
        when(configurationDomainService.isAmazonS3Enabled()).thenReturn(false);

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("configurationDomainServiceJpa", ConfigurationDomainService.class)).thenReturn(
                configurationDomainService);

        ContentRepositoryFactory documentStoreFactory = new ContentRepositoryFactory(applicationContext);
        assertEquals(documentStoreFactory.getInstanceFromConfiguration().getClass(), FileSystemContentRepository.class);
    }
**/
}
