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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.time.LocalDate;

public interface LoanLifecycleStateMachine {

    LoanStatus dryTransition(LoanEvent loanEvent, Loan loan);

    void transition(LoanEvent loanEvent, Loan loan);

    /**
     * Determines the appropriate loan status based on the loan's current financial condition. This method is designed
     * to be called at the end of processing to ensure accurate status transitions.
     *
     * For example, it can determine whether a loan that was OVERPAID should transition to: - CLOSED_OBLIGATIONS_MET (if
     * overpayment amount is now zero) - ACTIVE (if there is still outstanding balance) - Remain as OVERPAID (if still
     * overpaid)
     *
     * @param loan
     *            The loan whose status should be evaluated
     * @param transactionDate
     *            The date of the transaction that may have affected the loan status
     */
    void determineAndTransition(Loan loan, LocalDate transactionDate);

}
