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

public interface LoanAccountService {

    LoanTransaction saveLoanTransactionWithDataIntegrityViolationChecks(LoanTransaction newRepaymentTransaction);

    Loan saveAndFlushLoanWithDataIntegrityViolationChecks(Loan loan);

    /**
     * Due to the "saveAndFlushLoanWithDataIntegrityViolationChecks" method the loan is saved and flushed in the middle
     * of the transaction. EclipseLink is in some situations are saving inconsistently the newly created associations,
     * like the newly created repayment schedule installments. The save and flush cannot be removed safely till any
     * native queries are used as part of this transaction either. See:
     * this.loanAccountDomainService.recalculateAccruals(loan);
     *
     * As a workaround, we iterate over and save each LoanRepaymentScheduleInstallment entity before saving and flushing
     * the Loan entity.
     */
    Loan saveAndFlushLoanAndInstallmentsWithDataIntegrityViolationChecks(Loan loan);

    Loan saveLoanWithDataIntegrityViolationChecks(Loan loan);

}
