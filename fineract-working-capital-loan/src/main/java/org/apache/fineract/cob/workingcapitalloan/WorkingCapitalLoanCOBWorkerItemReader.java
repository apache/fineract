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
import org.apache.fineract.cob.service.BeforeStepLockingItemReaderHelper;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemReader;

/**
 * Hands the ids of the loans to close to the writer, which loads each loan itself; see {@code AbstractLoanItemReader}
 * for why reading touches no database.
 */
@RequiredArgsConstructor
public class WorkingCapitalLoanCOBWorkerItemReader implements ItemReader<Long>, StepExecutionListener {

    private final BeforeStepLockingItemReaderHelper itemReaderHelper;

    @Setter(AccessLevel.PROTECTED)
    private LinkedBlockingQueue<Long> remainingData;

    @Nullable
    @Override
    public Long read() {
        return remainingData.poll();
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        setRemainingData(itemReaderHelper.filterRemainingData(stepExecution));
    }
}
