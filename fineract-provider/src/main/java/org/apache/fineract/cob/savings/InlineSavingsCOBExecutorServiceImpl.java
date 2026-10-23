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
package org.apache.fineract.cob.savings;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.SavingsAccountLockRepository;
import org.apache.fineract.cob.service.InlineCommonLockableCOBExecutorService;
import org.apache.fineract.cob.service.InlineLoanCOBExecutionDataParser;
import org.apache.fineract.commands.configuration.RetryConfigurationAssembler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Inline Savings COB. Shares the lock-takeover rules, retry and failure handling of the loan inline COB: a lock held by
 * another COB run without an error cannot be overruled, and a failed attempt records its error on the locks it placed.
 */
@Service
public class InlineSavingsCOBExecutorServiceImpl extends InlineCommonLockableCOBExecutorService<SavingsAccountLock> {

    public static final String SAVINGS_IDS_PARAMETER_NAME = "savingsIds";

    private final InlineLoanCOBExecutionDataParser dataParser;
    private final RetrieveSavingsIdService retrieveSavingsIdService;

    public InlineSavingsCOBExecutorServiceImpl(SavingsAccountLockRepository savingsAccountLockRepository,
            InlineLoanCOBExecutionDataParser dataParser, JobOperator jobOperator, JobRegistry jobRegistry, JobRepository jobRepository,
            @Qualifier("requiresNewTransactionTemplate") TransactionTemplate requiresNewTransactionTemplate,
            CustomJobParameterRepository customJobParameterRepository, PlatformSecurityContext context,
            RetrieveSavingsIdService retrieveSavingsIdService, FineractProperties fineractProperties,
            RetryConfigurationAssembler retryConfigurationAssembler) {
        super(savingsAccountLockRepository, dataParser, jobOperator, jobRegistry, jobRepository, requiresNewTransactionTemplate,
                customJobParameterRepository, context, fineractProperties, retryConfigurationAssembler);
        this.dataParser = dataParser;
        this.retrieveSavingsIdService = retrieveSavingsIdService;
    }

    @Override
    public SavingsAccountLock createAccountLock(Long savingsId, LockOwner lockOwner, LocalDate businessDate) {
        return new SavingsAccountLock(savingsId, lockOwner, businessDate);
    }

    @Override
    protected List<COBIdAndLastClosedBusinessDate> retrieveAccountIdsBehindDateOrNull(LocalDate cobBusinessDate, List<Long> accountIds) {
        return retrieveSavingsIdService.retrieveSavingsIdsBehindDateOrNull(cobBusinessDate, accountIds);
    }

    @Override
    protected LockOwner getInlineLockOwner() {
        return LockOwner.SAVINGS_INLINE_COB_PROCESSING;
    }

    @Override
    protected List<Long> parseAccountIds(JsonCommand command) {
        return dataParser.parseExecution(command, SAVINGS_IDS_PARAMETER_NAME);
    }

    @Override
    protected String getAccountTypeName() {
        return "savings";
    }
}
