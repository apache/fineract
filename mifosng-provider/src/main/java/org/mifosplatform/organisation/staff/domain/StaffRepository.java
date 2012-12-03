package org.mifosplatform.organisation.staff.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StaffRepository extends JpaRepository<Staff, Long>,
		JpaSpecificationExecutor<Staff> {
	// no added behaviour
}