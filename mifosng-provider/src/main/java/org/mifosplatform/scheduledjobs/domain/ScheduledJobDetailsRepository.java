package org.mifosplatform.scheduledjobs.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduledJobDetailsRepository extends JpaRepository<ScheduledJobDetails, Long>,
        JpaSpecificationExecutor<ScheduledJobDetails> {

    @Query("from ScheduledJobDetails jobDetails where jobDetails.triggerKey = :triggerKey")
    ScheduledJobDetails findByTriggerKey(@Param("triggerKey") String triggerKey);

}
