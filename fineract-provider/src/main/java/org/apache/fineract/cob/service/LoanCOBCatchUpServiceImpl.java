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
package org.apache.fineract.cob.service;

import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.infrastructure.jobs.domain.JobExecutionRepository;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@Conditional(LoanCOBEnabledCondition.class)
public class LoanCOBCatchUpServiceImpl extends CommonCOBCatchUpService<LoanAccountLock> implements LoanCOBCatchUpService {

    public LoanCOBCatchUpServiceImpl(AsyncLoanCOBExecutorService asyncLoanCOBExecutorService, JobExecutionRepository jobExecutionRepository,
            RetrieveLoanIdService retrieveIdService, LoanAccountLockService accountLockService) {
        super(asyncLoanCOBExecutorService, jobExecutionRepository, retrieveIdService, accountLockService);
    }

    @Override
    public String getJobName() {
        return LoanCOBConstant.JOB_NAME;
    }
}
