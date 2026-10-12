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

package org.apache.fineract.cob.internal;

import static org.apache.fineract.infrastructure.jobs.service.JobName.WORKING_CAPITAL_LOAN_COB_JOB;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.listener.FineractCOBBeforeJobListener;
import org.apache.fineract.infrastructure.core.boot.FineractProfiles;
import org.springframework.batch.core.JobExecution;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile(FineractProfiles.TEST)
@Component
@RequiredArgsConstructor
public class InternalTestCobJobBeforeListener implements FineractCOBBeforeJobListener {

    private final TestData testData;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        testData.getData().put(TestData.COB_JOB_BEFORE_LISTENER, System.currentTimeMillis());
    }

    @Override
    public String getJobName() {
        return WORKING_CAPITAL_LOAN_COB_JOB.name();
    }
}
