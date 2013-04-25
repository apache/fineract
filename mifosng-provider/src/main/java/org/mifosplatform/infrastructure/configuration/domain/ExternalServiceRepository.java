package org.mifosplatform.infrastructure.configuration.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExternalServiceRepository extends JpaRepository<ExternalServiceProperty, Long>,
        JpaSpecificationExecutor<ExternalServiceProperty> {

    ExternalServiceProperty findOneByName(String name);
}