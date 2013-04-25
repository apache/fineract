package org.mifosplatform.infrastructure.configuration.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalServicesDomainService {
    private ExternalServiceRepository externalServiceRepository;

    @Autowired
    public ExternalServicesDomainService(final ExternalServiceRepository externalServiceRepository) {
        this.externalServiceRepository = externalServiceRepository;
    }

    public String getValue(String name){
        return externalServiceRepository.findOneByName(name).getValue();
    }
}
