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
package org.apache.fineract.cob.processor;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Processes and writes one COB item in a transaction of its own.
 * <p>
 * The business steps write in the transaction they run in. Run inside the chunk transaction, one failing account marks
 * that transaction rollback-only: the retry then runs in a doomed transaction, and once the account is skipped the
 * whole chunk is rolled back without an error, so every account of the chunk stays locked and is not closed. Here an
 * account's business step writes, its last closed business date and the release of its lock commit or roll back
 * together, and a retry starts from a clean transaction. An account that still fails keeps its lock, on which the item
 * listener records the error.
 * <p>
 * The item is loaded again inside the new transaction, so the business steps work on an instance it manages rather than
 * on the one the reader loaded in the chunk transaction. The step's own writer has nothing left to do.
 */
@RequiredArgsConstructor
public class ItemTransactionProcessor<I extends AbstractPersistableCustom<Long>> implements ItemProcessor<I, I>, StepExecutionListener {

    private final AbstractItemProcessor<I> delegate;
    private final ItemWriter<I> writer;
    private final Function<Long, I> loader;
    private final TransactionTemplate requiresNewTransactionTemplate;

    @Override
    public I process(@NonNull I item) throws Exception {
        PlatformTransactionManager transactionManager = requiresNewTransactionTemplate.getTransactionManager();
        TransactionStatus status = transactionManager.getTransaction(requiresNewTransactionTemplate);
        try {
            I processed = delegate.process(loader.apply(item.getId()));
            writer.write(Chunk.of(processed));
            transactionManager.commit(status);
            return processed;
        } finally {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
        }
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        delegate.beforeStep(stepExecution);
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return delegate.afterStep(stepExecution);
    }

    /**
     * The step's writer when its processor is an {@link ItemTransactionProcessor}: each item is already written.
     */
    public static <T> ItemWriter<T> noOpWriter() {
        return items -> {
            // each item was written by process
        };
    }
}
