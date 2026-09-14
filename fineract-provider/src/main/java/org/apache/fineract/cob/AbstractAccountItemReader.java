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
package org.apache.fineract.cob;

import java.util.concurrent.LinkedBlockingQueue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.exceptions.LockedReadException;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

/**
 * Account-agnostic Spring Batch reader that drains a pre-filtered queue of account ids and loads each entity from the
 * repository. Concrete account types (loan, savings, ...) supply the typed not-found exception via {@link #notFound}
 * and may post-process each loaded entity via {@link #afterFind}.
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAccountItemReader<T extends AbstractPersistableCustom<Long>> implements ItemReader<T> {

    protected final CrudRepository<T, Long> repository;

    @Setter(AccessLevel.PROTECTED)
    private LinkedBlockingQueue<Long> remainingData;

    @Override
    public T read() throws Exception {
        final Long accountId = remainingData.poll();
        if (accountId != null) {
            try {
                T item = repository.findById(accountId).orElseThrow(() -> notFound(accountId));
                afterFind(item);
                return item;
            } catch (Exception e) {
                throw new LockedReadException(accountId, e);
            }
        }
        return null;
    }

    /**
     * Typed exception thrown when the account id is not found in the repository.
     */
    protected abstract RuntimeException notFound(Long accountId);

    /**
     * Hook for account types that need to populate transient helpers on the freshly loaded entity (no-op by default).
     */
    protected void afterFind(T item) {
        // no-op by default
    }

    @AfterStep
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

}
