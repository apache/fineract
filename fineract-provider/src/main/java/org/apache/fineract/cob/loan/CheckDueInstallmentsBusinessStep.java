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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountCustomSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckDueInstallmentsBusinessStep implements LoanCOBBusinessStep {

    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public Loan execute(Loan loan) {
        log.debug("start processing custom snapshot event trigger business step loan for loan with id [{}]", loan.getId());

        if (loan.getRepaymentScheduleInstallments() != null && loan.getRepaymentScheduleInstallments().size() > 0) {
            final LocalDate currentDate = DateUtils.getBusinessLocalDate();
            boolean shouldPostCustomSnapshotBusinessEvent = false;
            for (int i = 0; i < loan.getRepaymentScheduleInstallments().size(); i++) {
                if (loan.getRepaymentScheduleInstallments().get(i).getDueDate().equals(currentDate)
                        && loan.getRepaymentScheduleInstallments().get(i).isNotFullyPaidOff()) {
                    shouldPostCustomSnapshotBusinessEvent = true;
                }
            }
            if (shouldPostCustomSnapshotBusinessEvent) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountCustomSnapshotBusinessEvent(loan));
            }
        }

        log.debug("end processing custom snapshot event trigger business step for loan with id [{}]", loan.getId());
        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "CHECK_DUE_INSTALLMENTS";
    }

    @Override
    public String getHumanReadableName() {
        return "Check Due Installments";
    }

}
