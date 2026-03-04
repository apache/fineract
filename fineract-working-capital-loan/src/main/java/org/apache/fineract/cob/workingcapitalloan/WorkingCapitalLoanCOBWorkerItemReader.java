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
package org.apache.fineract.cob.workingcapitalloan;

import java.util.concurrent.LinkedBlockingQueue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.WorkingCapitalLoanAccountLock;
import org.apache.fineract.cob.exceptions.LockedReadException;
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalLoanCOBWorkerItemReader implements ItemReader<WorkingCapitalLoan> {

    private final WorkingCapitalLoanRepository repository;
    private final BeforeStepLockingItemReaderHelper<WorkingCapitalLoanAccountLock> itemReaderHelper;

    @Setter(AccessLevel.PROTECTED)
    private LinkedBlockingQueue<Long> remainingData;

    @Nullable
    @Override
    public WorkingCapitalLoan read() throws Exception {
        final Long loanId = remainingData.poll();
        if (loanId != null) {
            try {
                return repository.findById(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
            } catch (Exception e) {
                throw new LockedReadException(loanId, e);
            }
        }
        return null;
    }

    @BeforeStep
    @SuppressWarnings({ "unchecked" })
    public void beforeStep(@NonNull StepExecution stepExecution) {
        setRemainingData(itemReaderHelper.filterRemainingData(stepExecution));
    }

}
