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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanCloseBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeOffPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargeOffPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoChargeOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

@Slf4j
@RequiredArgsConstructor
public class LoanBuyDownFeeAmortizationEventService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanBuyDownFeeAmortizationProcessingService loanBuyDownFeeAmortizationProcessingService;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPreBusinessEventListener(LoanCloseBusinessEvent.class, new LoanCloseListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanBalanceChangedBusinessEvent.class, new LoanBalanceChangedListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanChargeOffPostBusinessEvent.class, new LoanChargeOffEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanUndoChargeOffBusinessEvent.class,
                new LoanUndoChargeOffEventListener());
        businessEventNotifierService.addPreBusinessEventListener(LoanChargeOffPreBusinessEvent.class, new LoanChargeOffPreEventListener());
    }

    private final class LoanCloseListener implements BusinessEventListener<LoanCloseBusinessEvent> {

        @Override
        public void onBusinessEvent(final LoanCloseBusinessEvent event) {
            final Loan loan = event.get();
            final LoanStatus status = loan.getStatus();
            if (loan.getLoanProductRelatedDetail().isEnableBuyDownFee()
                    && (status.isClosedObligationsMet() || status.isClosedWrittenOff() || status.isOverpaid())) {
                log.debug("Loan closure on buy down fee amortization for loan {}", loan.getId());
                loanBuyDownFeeAmortizationProcessingService.processBuyDownFeeAmortizationOnLoanClosure(loan, false);
            }
        }
    }

    private final class LoanBalanceChangedListener implements BusinessEventListener<LoanBalanceChangedBusinessEvent> {

        @Override
        public void onBusinessEvent(final LoanBalanceChangedBusinessEvent event) {
            final Loan loan = event.get();
            final LoanStatus status = loan.getStatus();
            if (loan.getLoanProductRelatedDetail().isEnableBuyDownFee()
                    && (status.isClosedObligationsMet() || status.isClosedWrittenOff() || status.isOverpaid())) {
                log.debug("Loan balance change on buy down fee amortization for loan {}", loan.getId());
                loanBuyDownFeeAmortizationProcessingService.processBuyDownFeeAmortizationOnLoanClosure(loan, true);
            }
        }
    }

    private final class LoanChargeOffEventListener implements BusinessEventListener<LoanChargeOffPostBusinessEvent> {

        @Override
        public void onBusinessEvent(final LoanChargeOffPostBusinessEvent event) {
            final LoanTransaction loanTransaction = event.get();
            final Loan loan = loanTransaction.getLoan();
            if (loan.getLoanProductRelatedDetail().isEnableBuyDownFee() && loan.isChargedOff() && loanTransaction.isChargeOff()) {
                log.debug("Loan charge-off on buy down fee amortization for loan {}", loan.getId());
                loanBuyDownFeeAmortizationProcessingService.processBuyDownFeeAmortizationOnLoanChargeOff(loan, loanTransaction);
            }
        }
    }

    private final class LoanChargeOffPreEventListener implements BusinessEventListener<LoanChargeOffPreBusinessEvent> {

        @Override
        public void onBusinessEvent(final LoanChargeOffPreBusinessEvent event) {
            final Loan loan = event.get();
            if (loan.getLoanProductRelatedDetail().isEnableBuyDownFee()) {
                log.debug("Loan pre charge-off buy down fee amortization for loan {}", loan.getId());
                loanBuyDownFeeAmortizationProcessingService.processBuyDownFeeAmortizationTillDate(loan, DateUtils.getBusinessLocalDate(),
                        true);
            }
        }
    }

    private final class LoanUndoChargeOffEventListener implements BusinessEventListener<LoanUndoChargeOffBusinessEvent> {

        @Override
        public void onBusinessEvent(final LoanUndoChargeOffBusinessEvent event) {
            final LoanTransaction loanTransaction = event.get();
            final Loan loan = loanTransaction.getLoan();
            final LoanStatus status = loan.getStatus();
            if (loan.getLoanProductRelatedDetail().isEnableBuyDownFee() && loanTransaction.getTypeOf().isChargeOff()
                    && !(loan.isChargedOff() || status.isClosedObligationsMet() || status.isClosedWrittenOff() || status.isOverpaid())) {
                log.debug("Loan undo charge-off on buy down fee amortization for loan {}", loan.getId());
                loanBuyDownFeeAmortizationProcessingService.processBuyDownFeeAmortizationOnLoanUndoChargeOff(loanTransaction);
            }
        }
    }
}
