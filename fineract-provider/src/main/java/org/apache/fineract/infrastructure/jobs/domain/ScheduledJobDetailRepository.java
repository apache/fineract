/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.jobs.domain;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.jobs.data.JobDetailData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduledJobDetailRepository
        extends JpaRepository<ScheduledJobDetail, Long>, JpaSpecificationExecutor<ScheduledJobDetail> {

    @Query("select jobDetail from ScheduledJobDetail jobDetail where jobDetail.jobKey = :jobKey")
    ScheduledJobDetail findByJobKey(@Param("jobKey") String jobKey);

    @Query("select jobDetail from ScheduledJobDetail jobDetail where jobDetail.id=:jobId")
    ScheduledJobDetail findByJobId(@Param("jobId") Long jobId);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select jobDetail from ScheduledJobDetail jobDetail where jobDetail.jobKey = :jobKey")
    ScheduledJobDetail findByJobKeyWithLock(@Param("jobKey") String jobKey);

    @Query("select jobDetail from ScheduledJobDetail jobDetail where jobDetail.isMismatchedJob = :isMismatchedJob")
    List<ScheduledJobDetail> findAllMismatchedJobs(@Param("isMismatchedJob") boolean isMismatchedJob);

    @Query("select jobDetail from ScheduledJobDetail jobDetail where jobDetail.nodeId = :nodeId or jobDetail.nodeId = 0")
    List<ScheduledJobDetail> findAllJobs(@Param("nodeId") Integer nodeId);

    ScheduledJobDetail findByJobName(String jobName);

    String GET_DATA = "select new org.apache.fineract.infrastructure.jobs.data.JobDetailData(j.id, j.jobDisplayName, j.shortName, j.nextRunTime, "
            + "j.errorLog, j.cronExpression, j.activeSchedular, j.currentlyRunning, "
            + "jh.version, jh.startTime, jh.endTime, jh.status, jh.errorMessage, jh.triggerType, jh.errorLog) "
            + "from ScheduledJobDetail j left join ScheduledJobRunHistory jh on jh.scheduledJobDetail = j and j.previousRunStartTime = jh.startTime ";

    @Query(GET_DATA + "where j.id = :jobId")
    JobDetailData getDataById(@Param("jobId") Long jobId);

    @Query(GET_DATA + "where j.shortName = :shortName")
    JobDetailData getDataByShortName(@Param("shortName") String shortName);

    @Query(GET_DATA + "order by j.id")
    List<JobDetailData> getAllData();

    boolean existsByShortName(String shortName);

    @Query("select j.id from ScheduledJobDetail j where j.shortName = :shortName")
    Optional<Long> findIdByShortName(@Param("shortName") String shortName);
}
