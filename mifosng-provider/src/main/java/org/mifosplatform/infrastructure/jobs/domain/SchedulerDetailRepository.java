package org.mifosplatform.infrastructure.jobs.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SchedulerDetailRepository extends JpaRepository<SchedulerDetail, Long>, JpaSpecificationExecutor<SchedulerDetail> {

}
