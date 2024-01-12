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

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.exceptions.LoanReadException;
import org.apache.fineract.cob.loan.LoanLockingService;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.serialization.ThrowableSerialization;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.annotation.OnProcessError;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.core.annotation.OnWriteError;
import org.springframework.batch.item.Chunk;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLoanItemListener {

    private final LoanLockingService loanLockingService;

    private final TransactionTemplate transactionTemplate;

    private void updateAccountLockWithError(List<Long> loanIds, String msg, Throwable e) {
        transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(@NotNull TransactionStatus status) {
                for (Long loanId : loanIds) {
                    LoanAccountLock loanAccountLock = loanLockingService.findByLoanIdAndLockOwner(loanId, getLockOwner());
                    if (loanAccountLock != null) {
                        loanAccountLock.setError(String.format(msg, loanId), ThrowableSerialization.serialize(e));
                    }
                }
            }
        });
    }

    @OnReadError
    public void onReadError(Exception e) {
        if (e instanceof LoanReadException ee) {
            log.warn("Error was triggered during reading of Loan (id={}) due to: {}", ee.getId(), ThrowableSerialization.serialize(e));
            updateAccountLockWithError(List.of(ee.getId()), "Loan (id: %d) reading is failed", e);
        } else {
            log.error("Could not handle read error", e);
        }
    }

    @OnProcessError
    public void onProcessError(@NotNull Loan item, Exception e) {
        log.warn("Error was triggered during processing of Loan (id={}) due to: {}", item.getId(), ThrowableSerialization.serialize(e));
        updateAccountLockWithError(List.of(item.getId()), "Loan (id: %d) processing is failed", e);
    }

    @OnWriteError
    public void onWriteError(Exception e, @NotNull Chunk<? extends Loan> items) {
        List<Long> loanIds = items.getItems().stream().map(AbstractPersistableCustom::getId).toList();
        log.warn("Error was triggered during writing of Loans (ids={}) due to: {}", loanIds, ThrowableSerialization.serialize(e));

        updateAccountLockWithError(loanIds, "Loan (id: %d) writing is failed", e);
    }

    @OnSkipInRead
    public void onSkipInRead(@NotNull Throwable e) {
        log.warn("Skipping was triggered during read!");
    }

    @OnSkipInProcess
    public void onSkipInProcess(@NotNull Loan item, @NotNull Throwable e) {
        log.warn("Skipping was triggered during processing of Loan (id={})", item.getId());
    }

    @OnSkipInWrite
    public void onSkipInWrite(@NotNull Loan item, @NotNull Throwable e) {
        log.warn("Skipping was triggered during writing of Loan (id={})", item.getId());
    }

    protected abstract LockOwner getLockOwner();

}
