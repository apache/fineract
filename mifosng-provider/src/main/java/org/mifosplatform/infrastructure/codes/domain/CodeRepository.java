package org.mifosplatform.infrastructure.codes.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodeRepository extends JpaRepository<Code, Long>, JpaSpecificationExecutor<Code> {

    Code findOneByName(String name);
}