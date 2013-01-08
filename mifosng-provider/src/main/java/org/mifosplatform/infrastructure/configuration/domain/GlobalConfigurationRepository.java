package org.mifosplatform.infrastructure.configuration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GlobalConfigurationRepository extends JpaRepository<GlobalConfigurationProperty, Long>,
        JpaSpecificationExecutor<GlobalConfigurationProperty> {

    GlobalConfigurationProperty findOneByName(String name);
}