package org.mifosplatform.accounting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GLAccountRepository extends JpaRepository<GLAccount, Long>, JpaSpecificationExecutor<GLAccount> {
    // no added behaviour
}
