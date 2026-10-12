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
package org.apache.fineract.cob.listener;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.api.COBCatchUpExecutorHelper;
import org.apache.fineract.cob.resolver.CatchUpFlagResolver;
import org.apache.fineract.cob.service.COBCatchUpService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;

/**
 * A regular COB run only closes the loans that are exactly one day behind, so after a replayed run (see
 * {@code fineract.job.execute-missed-runs}) loans that are further behind still lag. When such a run has completed,
 * start the same catch-up that {@code POST /loans/catch-up} starts. It starts nothing when all loans are up to date, or
 * when a catch-up is already running. A catch-up run itself (its execution context carries {@code IS_CATCH_UP=true})
 * never starts another one: the final status of a run is saved before afterJob, so {@code isCatchUpRunning()} does not
 * see the day that has just finished, and the next day of the same catch-up has not started yet.
 */
@RequiredArgsConstructor
public abstract class CatchUpAfterJobListener implements FineractCOBAfterJobListener {

    private final FineractProperties fineractProperties;
    private final COBCatchUpService catchUpService;
    private final String jobName;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (!fineractProperties.getJob().isExecuteMissedRuns() || jobExecution.getStatus() != BatchStatus.COMPLETED
                || CatchUpFlagResolver.resolve(jobExecution)) {
            return;
        }
        COBCatchUpExecutorHelper.executeLoanCOBCatchUp(catchUpService);
    }

    @Override
    public String getJobName() {
        return jobName;
    }
}
