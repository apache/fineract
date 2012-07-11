package org.mifosng.platform.organisation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfficeMonetaryTransferRepository extends
		JpaRepository<OfficeMonetaryTransfer, Long>,
		JpaSpecificationExecutor<OfficeMonetaryTransfer> {
	// no added behaviour
}
