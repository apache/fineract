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
package org.apache.fineract.cob.writer;

import static org.apache.fineract.cob.data.BusinessStepNameAndOrder.getBusinessStepMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.cob.domain.LockingService;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.cob.exceptions.COBAccountFailedException;
import org.apache.fineract.cob.resolver.BusinessDateResolver;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.persistence.AfterTransactionWork;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Runs the COB business steps of a chunk and closes the COB day of every account in it, all within the write phase.
 * <p>
 * The business steps run here rather than in an {@code ItemProcessor} because of how Spring Batch 6's
 * {@code ChunkOrientedStep} recovers from failures. A failed <em>process</em> is skipped inside the chunk transaction
 * it happened in. A business step failure has marked that transaction rollback-only (it runs through
 * {@code COBBusinessStepService.run}, a participating {@code @Transactional}), so Batch rolls the whole chunk back at
 * the end - the healthy accounts processed with the failing one included - and never replays it. A failed
 * <em>write</em> is handled differently: Batch rolls the chunk back and enters scan mode, writing each item again on
 * its own in a transaction of its own. The failing account is then skipped and only its transaction rolls back, and
 * every other account commits its business steps, its last closed business date and the release of its lock together.
 * That is what Spring Batch 5 achieved by replaying the chunk without the skipped item.
 * <p>
 * The items are account ids, and each account is loaded here, in the transaction its business steps run in. Loading in
 * the reader would put a failure to load into the read phase, where Batch 6 skips it inside the chunk transaction it
 * has already doomed; and in scan mode the entities of the rolled-back chunk transaction would come back detached,
 * possibly changed in memory by business steps that never committed, so running the steps over them again would apply
 * those changes twice.
 * <p>
 * The persistence context is flushed before the locks are released, so that a failure the database reports only on
 * flush - an optimistic lock conflict on the account's version, a constraint violation - is raised inside the write and
 * so takes part in scan mode, instead of surfacing at commit, outside the write, where Batch fails the step.
 * <p>
 * A failure that can be put down to one account - its loading, its business steps, or the flush of a chunk of that
 * account alone - is raised as a {@link COBAccountFailedException} naming it, so that {@code AbstractLoanItemListener}
 * records the failure on that account's lock and not on its chunk's, also where no scan follows to single it out.
 */
@RequiredArgsConstructor
public abstract class AbstractCOBItemWriter<I extends AbstractPersistableCustom<Long>> implements ItemWriter<Long>, StepExecutionListener {

    private final COBBusinessStepService cobBusinessStepService;
    private final LockingService lockingService;
    private final JpaRepository<I, Long> repository;

    @Setter
    private ExecutionContext executionContext;

    @Getter
    private LocalDate businessDate;

    @Override
    public void write(@NonNull Chunk<? extends Long> accountIds) throws Exception {
        if (accountIds.isEmpty()) {
            return;
        }
        TreeMap<Long, String> businessStepMap = getBusinessStepMap(getBusinessSteps());
        for (Long accountId : accountIds) {
            try {
                runBusinessSteps(businessStepMap, accountId);
            } catch (Exception e) {
                throw new COBAccountFailedException(accountId, e);
            }
        }
        List<Long> ids = List.copyOf(accountIds.getItems());
        try {
            // The accounts were loaded in this transaction and are managed, so flushing writes their changes; saving
            // them as well would only merge, and cascade the merge through every association of the account, to no
            // effect.
            repository.flush();
            lockingService.deleteByLoanIdInAndLockOwner(ids, getLockOwner());
        } catch (RuntimeException e) {
            // A flush covers the whole chunk, so its failure belongs to one account only when the chunk has one.
            if (ids.size() == 1) {
                throw new COBAccountFailedException(ids.getFirst(), e);
            }
            throw e;
        }
    }

    private void runBusinessSteps(TreeMap<Long, String> businessStepMap, Long accountId) throws Exception {
        I current = beforeBusinessSteps(load(accountId));
        I processed = cobBusinessStepService.run(businessStepMap, current);
        setLastRun(processed);
    }

    private I load(Long accountId) {
        return repository.findById(accountId)
                .orElseThrow(() -> new BusinessStepException("COB account (id: " + accountId + ") does not exist"));
    }

    @SuppressWarnings("unchecked")
    private Set<BusinessStepNameAndOrder> getBusinessSteps() {
        Set<BusinessStepNameAndOrder> businessSteps = (Set<BusinessStepNameAndOrder>) executionContext.get(COBConstant.BUSINESS_STEPS);
        if (businessSteps == null) {
            throw new IllegalStateException("No business steps found in the execution context");
        }
        return businessSteps;
    }

    protected void setBusinessDate(StepExecution stepExecution) {
        businessDate = BusinessDateResolver.resolve(stepExecution);
    }

    /**
     * Saves any COB error still waiting for its transaction's cleanup (see {@code AbstractLoanItemListener}), so that
     * none is left behind on this thread when the step ends.
     */
    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        AfterTransactionWork.runReadyWork();
        return ExitStatus.COMPLETED;
    }

    /**
     * Prepares a freshly loaded account before its business steps run, for example by bringing an outdated schedule
     * model up to date. Runs in the chunk transaction, so its writes commit or roll back with the account's.
     */
    protected I beforeBusinessSteps(I item) throws Exception {
        return item;
    }

    protected abstract void setLastRun(I processedItem);

    protected abstract LockOwner getLockOwner();
}
