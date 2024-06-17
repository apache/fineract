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
package org.apache.fineract.infrastructure.jobs.service;

import java.util.List;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetail;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;

public interface SchedularWritePlatformService {

    List<ScheduledJobDetail> retrieveAllJobs(String nodeId);

    ScheduledJobDetail findByJobKey(String triggerKey);

    void saveOrUpdate(ScheduledJobDetail scheduledJobDetails);

    void saveOrUpdate(ScheduledJobDetail scheduledJobDetails, ScheduledJobRunHistory scheduledJobRunHistory);

    Long fetchMaxVersionBy(String triggerKey);

    ScheduledJobDetail findByJobId(Long jobId);

    CommandProcessingResult updateJobDetail(Long jobId, JsonCommand command);

    SchedulerDetail retriveSchedulerDetail();

    void updateSchedulerDetail(SchedulerDetail schedulerDetail);

    boolean processJobDetailForExecution(String jobKey, String triggerType);

}
