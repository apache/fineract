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

import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.AbstractAccountItemReader;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.service.RemainingDataFilter;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.lang.NonNull;

@Slf4j
public class SavingsItemReader extends AbstractAccountItemReader<SavingsAccount> {

    private final SavingsAccountAssembler savingsAccountAssembler;
    private final RetrieveSavingsIdService retrieveSavingsIdService;
    private final LockingService savingsLockingService;

    public SavingsItemReader(SavingsAccountRepository savingsAccountRepository, SavingsAccountAssembler savingsAccountAssembler,
            RetrieveSavingsIdService retrieveSavingsIdService, LockingService savingsLockingService) {
        super(savingsAccountRepository);
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.retrieveSavingsIdService = retrieveSavingsIdService;
        this.savingsLockingService = savingsLockingService;
    }

    @Override
    protected RuntimeException notFound(Long savingsId) {
        return new SavingsAccountNotFoundException(savingsId);
    }

    @Override
    protected void afterFind(SavingsAccount savingsAccount) {
        // Populate the transient helpers (e.g. SavingsHelper) which are not part of the persisted entity and are
        // required by interest posting and other business steps.
        savingsAccountAssembler.setHelpers(savingsAccount);
    }

    @BeforeStep
    public void beforeStep(@NonNull StepExecution stepExecution) {
        setRemainingData(filterRemainingData(stepExecution));
    }

    private LinkedBlockingQueue<Long> filterRemainingData(StepExecution stepExecution) {
        return RemainingDataFilter.filterRemainingData(stepExecution, SavingsCOBConstant.SAVINGS_COB_PARAMETER,
                retrieveSavingsIdService::retrieveAllNonClosedSavingsByLastClosedBusinessDateAndMinAndMaxSavingsId,
                savingsIds -> savingsLockingService.findLockIdsByLoanIdInAndLockOwner(savingsIds, LockOwner.SAVINGS_COB_CHUNK_PROCESSING));
    }
}
