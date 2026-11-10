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

import java.util.concurrent.LinkedBlockingQueue;
import lombok.AccessLevel;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemReader;

/**
 * Hands the ids of the accounts to close to the writer, which loads each account itself.
 * <p>
 * Reading touches no database on purpose. Spring Batch 6 skips a failed read inside the chunk transaction, and loading
 * an account goes through a repository whose {@code @Transactional} would mark that transaction rollback-only on any
 * failure: the rest of the chunk would then be written and silently rolled back with it. A failure to load an account
 * in the writer instead enters scan mode, where only that account is skipped.
 */
public abstract class AbstractLoanItemReader implements ItemReader<Long>, StepExecutionListener {

    @Setter(AccessLevel.PROTECTED)
    private LinkedBlockingQueue<Long> remainingData;

    @Override
    public @Nullable Long read() {
        return remainingData.poll();
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
