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

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.transaction.annotation.Transactional;

public interface LoanTransactionWritePlatformService {

    @Transactional
    CommandProcessingResult makeGLIMLoanRepayment(Long loanId, JsonCommand command);

    @Transactional
    CommandProcessingResult makeLoanRepayment(LoanTransactionType repaymentTransactionType, Long loanId, JsonCommand command,
            boolean isRecoveryRepayment);

    @Transactional
    CommandProcessingResult makeLoanRepaymentWithChargeRefundChargeType(LoanTransactionType repaymentTransactionType, Long loanId,
            JsonCommand command, boolean isRecoveryRepayment, String chargeRefundChargeType);

    @Transactional
    CommandProcessingResult makeInterestPaymentWaiver(JsonCommand command);

    @Transactional
    CommandProcessingResult adjustLoanTransaction(Long loanId, Long transactionId, JsonCommand command);

    @Transactional
    CommandProcessingResult chargebackLoanTransaction(Long loanId, Long transactionId, JsonCommand command);

    @Transactional
    CommandProcessingResult waiveInterestOnLoan(Long loanId, JsonCommand command);

    CommandProcessingResult recoverFromGuarantor(Long loanId);

    CommandProcessingResult creditBalanceRefund(Long loanId, JsonCommand command);

    @Transactional
    CommandProcessingResult makeLoanRefund(Long loanId, JsonCommand command);

    CommandProcessingResult makeRefund(Long loanId, LoanTransactionType loanTransactionType, JsonCommand command);

    @Transactional
    CommandProcessingResult makeManualInterestRefund(Long loanId, Long transactionId, JsonCommand command);
}
