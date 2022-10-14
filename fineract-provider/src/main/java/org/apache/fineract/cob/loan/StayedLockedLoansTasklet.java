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

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.domain.LoanAccountLock;
import org.apache.fineract.cob.domain.LoanAccountLockRepository;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class StayedLockedLoansTasklet implements Tasklet {

    private final LoanAccountLockRepository loanAccountLockRepository;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<LoanAccountLock> loanAccountLocks = loanAccountLockRepository.findAll();
        if (!loanAccountLocks.isEmpty()) {
            List<Long> loanIds = loanAccountLocks.stream().map(LoanAccountLock::getLoanId).toList();
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountStayedLockedBusinessEvent(loanIds));
        }
        return RepeatStatus.FINISHED;
    }
}
