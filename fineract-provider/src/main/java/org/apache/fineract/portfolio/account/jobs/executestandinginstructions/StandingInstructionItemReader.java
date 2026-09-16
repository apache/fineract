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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;

/**
 * Reads the due instructions of one partition, a page at a time.
 *
 * <p>
 * The page is a keyset over the {@code (priority, id)} sort key rather than an offset, because executing an instruction
 * stamps its {@code last_run_date} and so removes it from the due set: an offset would step over unprocessed rows every
 * time a page committed. Only one page is held in memory at a time, whatever the size of the due set.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class StandingInstructionItemReader implements ItemReader<StandingInstructionData> {

    private final int pageSize;
    private final StandingInstructionReadPlatformService standingInstructionReadPlatformService;

    private final Deque<StandingInstructionData> page = new ArrayDeque<>();

    private Long minAccountKey;
    private Long maxAccountKey;
    private Integer lastPriority;
    private Long lastId;
    private boolean exhausted;

    @BeforeStep
    public void beforeStep(final StepExecution stepExecution) {
        this.minAccountKey = stepExecution.getExecutionContext().getLong(ExecuteStandingInstructionsConstant.MIN_ACCOUNT_KEY);
        this.maxAccountKey = stepExecution.getExecutionContext().getLong(ExecuteStandingInstructionsConstant.MAX_ACCOUNT_KEY);
    }

    @Override
    public StandingInstructionData read() {
        if (page.isEmpty() && !exhausted) {
            fetchNextPage();
        }
        return page.poll();
    }

    private void fetchNextPage() {
        final List<StandingInstructionData> nextPage = standingInstructionReadPlatformService
                .retrieveDuePage(StandingInstructionStatus.ACTIVE.getValue(), minAccountKey, maxAccountKey, lastPriority, lastId, pageSize);
        if (nextPage.size() < pageSize) {
            exhausted = true;
        }
        if (nextPage.isEmpty()) {
            return;
        }
        final StandingInstructionData last = nextPage.getLast();
        lastPriority = last.getPriority().getId().intValue();
        lastId = last.getId();
        page.addAll(nextPage);
        log.debug("Read {} due standing instructions for accounts {}..{}", nextPage.size(), minAccountKey, maxAccountKey);
    }
}
