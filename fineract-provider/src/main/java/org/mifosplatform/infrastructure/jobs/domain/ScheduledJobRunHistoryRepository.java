/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.jobs.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduledJobRunHistoryRepository extends JpaRepository<ScheduledJobRunHistory, Long>,
        JpaSpecificationExecutor<ScheduledJobRunHistory> {

    @Query("select max(sjrh.version) from ScheduledJobRunHistory sjrh where sjrh.scheduledJobDetail.jobKey = :jobKey")
    Long findMaxVersionByJobKey(@Param("jobKey") String jobKey);

}
