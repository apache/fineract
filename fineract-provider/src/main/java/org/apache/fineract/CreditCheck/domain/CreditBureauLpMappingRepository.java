package org.apache.fineract.CreditCheck.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CreditBureauLpMappingRepository extends JpaRepository<CreditBureauLpMapping, Long>, JpaSpecificationExecutor<CreditBureauLpMapping> {

}
