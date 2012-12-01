package org.mifosplatform.portfolio.client.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientIdentifierRepository extends JpaRepository<ClientIdentifier, Long>, JpaSpecificationExecutor<ClientIdentifier> {
    // no behaviour
}