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

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.WorkingCapitalAccountLockRepository;
import org.apache.fineract.cob.domain.WorkingCapitalLoanAccountLock;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanRetrieveIdService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@Conditional(LoanCOBEnabledCondition.class)
public class InlineWorkingCapitalLoanCOBExecutorServiceImpl extends InlineCommonLockableCOBExecutorService<WorkingCapitalLoanAccountLock> {

    public InlineWorkingCapitalLoanCOBExecutorServiceImpl(WorkingCapitalAccountLockRepository loanAccountLockRepository,
            InlineLoanCOBExecutionDataParser dataParser, JobLauncher jobLauncher, JobLocator jobLocator, JobExplorer jobExplorer,
            TransactionTemplate transactionTemplate, CustomJobParameterRepository customJobParameterRepository,
            PlatformSecurityContext context, WorkingCapitalLoanRetrieveIdService retrieveIdService, FineractProperties fineractProperties) {
        super(loanAccountLockRepository, dataParser, jobLauncher, jobLocator, jobExplorer, transactionTemplate,
                customJobParameterRepository, context, retrieveIdService, fineractProperties);
    }

    @Override
    public WorkingCapitalLoanAccountLock createAccountLock(Long loanId, LockOwner loanInlineCobProcessing, LocalDate businessDate) {
        return new WorkingCapitalLoanAccountLock(loanId, loanInlineCobProcessing, businessDate);
    }
}
