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

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.jspecify.annotations.NonNull;

public interface LoanAccrualTransactionService {

    void postChargeAccrual(@NonNull LoanTransaction chargeAccrual);

    void ensureAccrualTransactionMappings(Loan loan, boolean chargeOnDueDate);

    boolean isChargeOnDueDate();

    boolean resolveChargeOnDueDate(boolean chargeOnDueDateConfigured, boolean realTimeChargeAccrual);

    boolean isRealTimeChargeAccrual(Loan loan);

    boolean isRealTimeChargeAccrual(Loan loan, boolean plChargeAccrualRealTime);

    void determineFeeDetails(Loan loan, LocalDate fromDate, LocalDate toDate, Map<String, Object> feeDetails);

    void updateLoanChargesPaidBy(Loan loan, LoanTransaction accrual, Map<String, Object> feeDetails,
            LoanRepaymentScheduleInstallment installment);

    void reverseTransactionsAfter(Loan loan, Set<LoanTransactionType> types, LocalDate effectiveDate, boolean addEvent);

    void reverseTransactionsOnOrAfter(Loan loan, Set<LoanTransactionType> types, LocalDate date);

    void adjustAccrualsAfter(Loan loan, LocalDate lastDueDate, boolean addEvent);

    void reverseTransaction(LoanTransaction transaction, boolean addEvent);

    boolean isProgressiveAccrual(@NonNull Loan loan);
}
