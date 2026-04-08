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
package org.apache.fineract.portfolio.loanaccount.service.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reaging.LoanReAgeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reaging.LoanUndoReAgeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reamortization.LoanReAmortizeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.reamortization.LoanUndoReAmortizeTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanTransactionDelinquencyRecalculationListener
        implements InitializingBean, BusinessEventListener<LoanTransactionBusinessEvent> {

    // Extend this list to support more event types so the hardcoded delinquency recalculation can be removed from the
    // use-cases
    private static final List<Class<? extends LoanTransactionBusinessEvent>> SUPPORTED_EVENT_TYPES = List.of(//
            LoanReAgeTransactionBusinessEvent.class, //
            LoanUndoReAgeTransactionBusinessEvent.class, //
            LoanReAmortizeTransactionBusinessEvent.class, //
            LoanUndoReAmortizeTransactionBusinessEvent.class //
    );//

    private final LoanAccountDomainService loanAccountDomainService;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public void afterPropertiesSet() throws Exception {
        businessEventNotifierService.addPostBusinessEventListener(LoanTransactionBusinessEvent.class, this);
    }

    @Override
    public void onBusinessEvent(LoanTransactionBusinessEvent event) {
        if (SUPPORTED_EVENT_TYPES.contains(event.getClass())) {
            LoanTransaction tx = event.get();
            loanAccountDomainService.setLoanDelinquencyTag(tx.getLoan(), tx.getTransactionDate());
        }
    }
}
