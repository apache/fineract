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
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;

public interface LoanTemplateReadPlatformService {

    LoanTransactionData retrieveLoanTransactionTemplate(Long loanId);

    LoanTransactionData retrieveLoanTransactionTemplate(Long loanId, LoanTransactionType transactionType, Long transactionId);

    LoanTransactionData retrieveWaiveInterestDetails(Long loanId);

    LoanTransactionData retrieveNewClosureDetails();

    LoanTransactionData retrieveDisbursalTemplate(Long loanId, boolean paymentDetailsRequired);

    LoanApprovalData retrieveApprovalTemplate(Long loanId);

    LoanAccountData retrieveLoanProductDetailsTemplate(Long productId, Long clientId, Long groupId, Long officeId);

    LoanTransactionData retrieveRecoveryPaymentTemplate(Long loanId);

    LoanTransactionData retrieveLoanWriteoffTemplate(Long loanId);

    LoanTransactionData retrieveLoanChargeOffTemplate(Long loanId);

    LoanTransactionData retrieveLoanPrePaymentTemplate(LoanTransactionType repaymentTransactionType, Long loanId, LocalDate onDate);

    LoanTransactionData retrieveLoanEarlyTerminationTemplate(Long loanId, LocalDate onDate);

    LoanTransactionData retrieveRefundByCashTemplate(Long loanId);

    LoanTransactionData retrieveCreditBalanceRefundTemplate(Long loanId);

    LoanTransactionData retrieveLoanForeclosureTemplate(Long loanId, LocalDate transactionDate);

    LoanTransactionData retrieveManualInterestRefundTemplate(Long loanId, Long targetTransactionId);

    LoanTransactionData retrieveLoanReAgeTemplate(Long loanId);

    LoanTransactionData retrieveLoanReAmortizationTemplate(Long loanId);
}
