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
package org.apache.fineract.cob.loan;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.lang.NonNull;

@Slf4j
public class LoanItemReader extends AbstractLoanItemReader<Loan> {

    private final BeforeStepLockingItemReaderHelper<LoanAccountLock> beforeStepLockingItemReaderHelper;

    public LoanItemReader(LoanRepository loanRepository,
            BeforeStepLockingItemReaderHelper<LoanAccountLock> beforeStepLockingItemReaderHelper) {
        super(loanRepository);
        this.beforeStepLockingItemReaderHelper = beforeStepLockingItemReaderHelper;
    }

    @BeforeStep
    public void beforeStep(@NonNull StepExecution stepExecution) {
        setRemainingData(beforeStepLockingItemReaderHelper.filterRemainingData(stepExecution));
    }

}
