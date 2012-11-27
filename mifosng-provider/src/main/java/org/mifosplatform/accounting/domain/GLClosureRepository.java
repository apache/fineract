package org.mifosplatform.accounting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GLClosureRepository extends JpaRepository<GLClosure, Long>, JpaSpecificationExecutor<GLClosure> {
    // no added behaviour
}
