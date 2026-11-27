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
import java.util.List;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.jspecify.annotations.NonNull;

public interface LoanPeriodicAccrualService {

    void addPeriodicAccruals(@NonNull LocalDate tillDate) throws JobExecutionException;

    void addPeriodicAccruals(@NonNull LocalDate tillDate, @NonNull Loan loan);

    void addAccruals(@NonNull LocalDate tillDate) throws JobExecutionException;

    void processAccrualsOnInterestRecalculation(@NonNull Loan loan, boolean isInterestRecalculationEnabled, boolean addJournal);

    boolean isRealTimeChargeAccrualApplicable(@NonNull Loan loan);

    void processRealTimeChargeAccruals(@NonNull Loan loan, @NonNull List<LoanCharge> loanCharges);

    void addAccruals(@NonNull Loan loan, @NonNull LocalDate tillDate, boolean periodic, boolean isFinal, boolean addJournal,
            boolean chargeOnDueDate, boolean realTimeChargeAccrual);
}
