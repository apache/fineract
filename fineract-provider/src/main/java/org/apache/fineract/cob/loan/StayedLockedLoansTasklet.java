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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.data.LoanAccountStayedLockedData;
import org.apache.fineract.cob.data.LoanAccountsStayedLockedData;
import org.apache.fineract.cob.data.LoanIdAndExternalIdAndAccountNo;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class StayedLockedLoansTasklet implements Tasklet {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final RetrieveLoanIdService retrieveLoanIdService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LoanAccountsStayedLockedData lockedLoanAccounts = buildLoanAccountData();
        if (!lockedLoanAccounts.getLoanAccounts().isEmpty()) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountsStayedLockedBusinessEvent(lockedLoanAccounts));
        }
        return RepeatStatus.FINISHED;
    }

    private LoanAccountsStayedLockedData buildLoanAccountData() {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        List<LoanIdAndExternalIdAndAccountNo> stayedLockedLoanAccounts = retrieveLoanIdService
                .findAllStayedLockedByCobBusinessDate(cobBusinessDate);
        List<LoanAccountStayedLockedData> loanAccounts = new ArrayList<>();
        stayedLockedLoanAccounts.forEach(loanAccount -> {
            loanAccounts.add(new LoanAccountStayedLockedData(loanAccount.getId(), loanAccount.getExternalId(), loanAccount.getAccountNo()));
        });
        return new LoanAccountsStayedLockedData(loanAccounts);
    }
}
