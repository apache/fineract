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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionDownPaymentPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;

@Slf4j
@RequiredArgsConstructor
public class LoanDownPaymentHandlerServiceImpl implements LoanDownPaymentHandlerService {

    private final LoanTransactionRepository loanTransactionRepository;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public LoanTransaction handleDownPayment(ScheduleGeneratorDTO scheduleGeneratorDTO, JsonCommand command,
            LoanTransaction disbursementTransaction, Loan loan) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionDownPaymentPreBusinessEvent(loan));
        LoanTransaction downPaymentTransaction = loan.handleDownPayment(disbursementTransaction, command, scheduleGeneratorDTO);
        if (downPaymentTransaction != null) {
            downPaymentTransaction = loanTransactionRepository.saveAndFlush(downPaymentTransaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanTransactionDownPaymentPostBusinessEvent(downPaymentTransaction));
            businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        }
        return downPaymentTransaction;
    }
}
