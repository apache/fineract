package org.mifosplatform.infrastructure.configuration.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.useradministration.domain.Permission;
import org.mifosplatform.useradministration.domain.PermissionRepository;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unused")
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationDomainServiceJpaTest {

    private final String S3 = "amazon-S3";
    private PermissionRepository permissionRepositoryMock = mock(PermissionRepository.class);;
    private GlobalConfigurationRepository globalConfigurationRepositoryMock = mock(GlobalConfigurationRepository.class);

    @Test
    public void shouldReturnTrueIfAmazonS3ConfigurationIsEnabled(){
        when(globalConfigurationRepositoryMock.findOneByName(S3)).thenReturn(new GlobalConfigurationProperty(S3, true));
        ConfigurationDomainServiceJpa configurationDomainServiceJpa = new ConfigurationDomainServiceJpa(permissionRepositoryMock, globalConfigurationRepositoryMock);

        boolean actualIsS3Enabled = configurationDomainServiceJpa.isAmazonS3Enabled();

        assertTrue(actualIsS3Enabled);
    }

    @Test
    public void shouldReturnFalseIfAmazonS3ConfigurationIsDisabled(){
        when(globalConfigurationRepositoryMock.findOneByName(S3)).thenReturn(new GlobalConfigurationProperty(S3, false));
        ConfigurationDomainServiceJpa configurationDomainServiceJpa = new ConfigurationDomainServiceJpa(permissionRepositoryMock, globalConfigurationRepositoryMock);

        boolean actualIsS3Enabled = configurationDomainServiceJpa.isAmazonS3Enabled();

        assertFalse(actualIsS3Enabled);
    }
}
