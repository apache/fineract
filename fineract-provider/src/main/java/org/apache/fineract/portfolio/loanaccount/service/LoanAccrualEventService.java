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
package org.apache.fineract.portfolio.loanaccount.service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.cob.service.InlineLoanCOBExecutorServiceImpl;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanCloseBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;

@Slf4j
@RequiredArgsConstructor
public class LoanAccrualEventService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanAccrualActivityProcessingService loanAccrualActivityProcessingService;
    private final InlineLoanCOBExecutorServiceImpl inlineLoanCOBExecutorService;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(LoanCloseBusinessEvent.class, new LoanCloseListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanBalanceChangedBusinessEvent.class, new LoanBalanceChangedListener());
    }

    private final class LoanCloseListener implements BusinessEventListener<LoanCloseBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanCloseBusinessEvent event) {
            final Loan loan = event.get();
            LoanStatus status = loan.getStatus();
            if (status.isClosedObligationsMet() || status.isOverpaid()) {
                validateAnRunInlineCOBIfRequired(loan, DateUtils.getBusinessLocalDate());
                log.debug("Loan closure on accrual for loan {}", loan.getId());
                loanAccrualsProcessingService.processAccrualsOnLoanClosure(loan, true);
                loanAccrualActivityProcessingService.processAccrualActivityForLoanClosure(loan);
            }
        }
    }

    private final class LoanBalanceChangedListener implements BusinessEventListener<LoanBalanceChangedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanBalanceChangedBusinessEvent event) {
            final Loan loan = event.get();
            LoanStatus status = loan.getStatus();
            if (status.isClosedObligationsMet() || status.isOverpaid()) {
                validateAnRunInlineCOBIfRequired(loan, DateUtils.getBusinessLocalDate());
                log.debug("Loan balance change on accrual for loan {}", loan.getId());
                loanAccrualsProcessingService.processAccrualsOnLoanClosure(loan, true);
                loanAccrualActivityProcessingService.processAccrualActivityForLoanClosure(loan);
            }
        }
    }

    private void validateAnRunInlineCOBIfRequired(Loan loan, final LocalDate businessDate) {
        if (loan.getLastClosedBusinessDate() == null || loan.getLastClosedBusinessDate().compareTo(businessDate.minusDays(1)) < 0) {
            log.debug("Inline COB execution for loan {}", loan.getId());
            inlineLoanCOBExecutorService.execute(List.of(loan.getId()), LoanCOBConstant.INLINE_LOAN_COB_JOB_NAME);
        }

    }
}
