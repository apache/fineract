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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanStatusChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;

@Slf4j
@RequiredArgsConstructor
public class LoanStatusChangePlatformServiceImpl implements LoanStatusChangePlatformService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanAccrualActivityProcessingService loanAccrualActivityProcessingService;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(LoanStatusChangedBusinessEvent.class, new LoanStatusChangedListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanStatusChangedBusinessEvent.class,
                new LoanAccrualActivityPostingLoanStatusChangedListener());
    }

    private final class LoanStatusChangedListener implements BusinessEventListener<LoanStatusChangedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanStatusChangedBusinessEvent event) {
            final Loan loan = event.get();
            log.debug("Loan Status change for loan {}", loan.getId());
            if (loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid()) {
                log.debug("Loan Status {} for loan {}", loan.getStatus().getCode(), loan.getId());
                loanAccrualsProcessingService.processAccrualsForLoanClosure(loan);
            }
            if (loan.isOpen()) {
                loan.handleMaturityDateActivate();
            }
        }
    }

    private final class LoanAccrualActivityPostingLoanStatusChangedListener
            implements BusinessEventListener<LoanStatusChangedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanStatusChangedBusinessEvent event) {
            final Loan loan = event.get();
            if (loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
                if (loan.getStatus().isClosedObligationsMet() || loan.getStatus().isOverpaid()) {
                    loanAccrualActivityProcessingService.processAccrualActivityForLoanClosure(loan);
                }
                if (loan.isOpen()) {
                    loanAccrualActivityProcessingService.processAccrualActivityForLoanReopen(loan);
                }
            }
        }
    }
}
