package org.mifosplatform.infrastructure.core.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.configuration.domain.ExternalServicesDomainService;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")

@RunWith(MockitoJUnitRunner.class)
public class DocumentStoreFactoryTest {

    @Test
    public void shouldReturnS3DocumentStoreObjectIfS3Enabled(){
        S3ClientFactory s3ClientFactoryMock = mock(S3ClientFactory.class);
        when(s3ClientFactoryMock.getS3Client()).thenReturn(null);

        ExternalServicesDomainService externalServicesDomainServiceMock = mock(ExternalServicesDomainService.class);
        when(externalServicesDomainServiceMock.getValue("s3_bucket_name")).thenReturn("bucket_name");

        ConfigurationDomainService configurationDomainServiceMock = mock(ConfigurationDomainService.class);
        when(configurationDomainServiceMock.isAmazonS3Enabled()).thenReturn(true);

        ApplicationContext applicationContextMock = mock(ApplicationContext.class);

        when(applicationContextMock.getBean("configurationDomainServiceJpa", ConfigurationDomainService.class)).thenReturn(configurationDomainServiceMock);
        when(applicationContextMock.getBean("s3ClientFactory", S3ClientFactory.class)).thenReturn(s3ClientFactoryMock);
        when(applicationContextMock.getBean("externalServicesDomainService", ExternalServicesDomainService.class)).thenReturn(externalServicesDomainServiceMock);


        DocumentStoreFactory documentStoreFactory = new DocumentStoreFactory(applicationContextMock);
        assertEquals(documentStoreFactory.getInstanceFromConfiguration().getClass(),S3DocumentStore.class);
    }

    @Test
    public void shouldReturnFileSystemDocumentStoreObjectIfS3IsDisabled(){
        ConfigurationDomainService configurationDomainService = mock(ConfigurationDomainService.class);
        when(configurationDomainService.isAmazonS3Enabled()).thenReturn(false);

        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean("configurationDomainServiceJpa", ConfigurationDomainService.class)).thenReturn(configurationDomainService);

        DocumentStoreFactory documentStoreFactory = new DocumentStoreFactory(applicationContext);
        assertEquals(documentStoreFactory.getInstanceFromConfiguration().getClass(),FileSystemDocumentStore.class);
    }


}
