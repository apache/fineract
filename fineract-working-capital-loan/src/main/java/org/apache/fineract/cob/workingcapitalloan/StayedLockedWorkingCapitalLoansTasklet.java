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

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.data.COBIdAndExternalIdAndAccountNo;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/**
 * Reports the working capital loans whose COB lock was placed on the current COB date and is still held after the
 * partitioned step: they were not processed and stay one COB day behind until the lock is released.
 */
@RequiredArgsConstructor
public class StayedLockedWorkingCapitalLoansTasklet implements Tasklet {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final WorkingCapitalLoanRetrieveIdService retrieveIdService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        List<COBIdAndExternalIdAndAccountNo> stayedLockedAccounts = retrieveIdService.findAllStayedLockedByCobBusinessDate(cobBusinessDate);
        if (!stayedLockedAccounts.isEmpty()) {
            businessEventNotifierService.notifyPostBusinessEvent(new WorkingCapitalLoanAccountsStayedLockedBusinessEvent(
                    new WorkingCapitalLoanAccountsStayedLockedData(stayedLockedAccounts)));
        }
        return RepeatStatus.FINISHED;
    }
}
