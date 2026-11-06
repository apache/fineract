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
import java.util.ArrayList;
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
import org.apache.fineract.cob.resolver.BusinessDateResolver;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
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
 * Each account is loaded again before its business steps run. In scan mode the chunk items are entities of the
 * rolled-back chunk transaction, detached and possibly changed in memory by business steps that never committed;
 * running the steps over them again would apply those changes twice.
 * <p>
 * The persistence context is flushed before the locks are released, so that a failure the database reports only on
 * flush - an optimistic lock conflict on the account's version, a constraint violation - is raised inside the write and
 * so takes part in scan mode, instead of surfacing at commit, outside the write, where Batch fails the step.
 */
@RequiredArgsConstructor
public abstract class AbstractCOBItemWriter<I extends AbstractPersistableCustom<Long>> implements ItemWriter<I>, StepExecutionListener {

    private final COBBusinessStepService cobBusinessStepService;
    private final LockingService lockingService;
    private final JpaRepository<I, Long> repository;

    @Setter
    private ExecutionContext executionContext;

    @Getter
    private LocalDate businessDate;

    @Override
    public void write(@NonNull Chunk<? extends I> items) throws Exception {
        if (items.isEmpty()) {
            return;
        }
        TreeMap<Long, String> businessStepMap = getBusinessStepMap(getBusinessSteps());
        List<I> processedItems = new ArrayList<>(items.size());
        for (I item : items) {
            processedItems.add(runBusinessSteps(businessStepMap, item));
        }
        repository.saveAll(processedItems);
        repository.flush();
        List<Long> ids = processedItems.stream().map(AbstractPersistableCustom::getId).toList();
        lockingService.deleteByLoanIdInAndLockOwner(ids, getLockOwner());
    }

    private I runBusinessSteps(TreeMap<Long, String> businessStepMap, I item) throws Exception {
        I current = beforeBusinessSteps(reload(item));
        I processed = cobBusinessStepService.run(businessStepMap, current);
        setLastRun(processed);
        return processed;
    }

    private I reload(I item) {
        Long id = item.getId();
        return repository.findById(id).orElseThrow(() -> new BusinessStepException("COB account (id: " + id + ") does not exist"));
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

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
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
