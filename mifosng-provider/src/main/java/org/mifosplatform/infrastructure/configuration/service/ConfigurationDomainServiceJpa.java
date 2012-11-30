package org.mifosplatform.infrastructure.configuration.service;

import org.mifosplatform.infrastructure.user.domain.Permission;
import org.mifosplatform.infrastructure.user.domain.PermissionRepository;
import org.mifosplatform.infrastructure.user.exception.PermissionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationDomainServiceJpa implements ConfigurationDomainService {

    private final PermissionRepository permissionRepository;
    
    /*
     * hard code to false to disable maker checker across the board.
     */
    private final boolean makerCheckerGloablConfigurationEnabled = false;

    @Autowired
    public ConfigurationDomainServiceJpa(final PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }
    
    @Override
    public boolean isMakerCheckerEnabledForTask(final String taskPermissionCode) {
        final Permission thisTask = this.permissionRepository.findOneByCode(taskPermissionCode);
        if (thisTask == null) {
            throw new PermissionNotFoundException(taskPermissionCode);
        }
        
        return thisTask.hasMakerCheckerEnabled() && this.makerCheckerGloablConfigurationEnabled;
    }
}