package org.mifosng.platform.saving.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SavingAccountRepository extends
JpaRepository<SavingAccount, Long>,
JpaSpecificationExecutor<SavingAccount>{

}
