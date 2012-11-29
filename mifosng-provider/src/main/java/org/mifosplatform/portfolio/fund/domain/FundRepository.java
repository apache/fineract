package org.mifosplatform.portfolio.fund.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FundRepository extends JpaRepository<Fund, Long>, JpaSpecificationExecutor<Fund> {
	// no added behaviour
}