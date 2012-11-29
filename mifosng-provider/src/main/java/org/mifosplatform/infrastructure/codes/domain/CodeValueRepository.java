package org.mifosplatform.infrastructure.codes.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodeValueRepository extends JpaRepository<CodeValue, Long>, JpaSpecificationExecutor<CodeValue> {
    // no added behaviour
}
