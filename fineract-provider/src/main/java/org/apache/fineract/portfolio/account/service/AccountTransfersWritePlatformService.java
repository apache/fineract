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
package org.apache.fineract.portfolio.account.service;

import java.util.Collection;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;

public interface AccountTransfersWritePlatformService {

    CommandProcessingResult create(JsonCommand command);

    void reverseTransfersWithFromAccountType(Long accountNumber, PortfolioAccountType accountTypeId);

    Long transferFunds(AccountTransferDTO accountTransferDTO);

    void reverseAllTransactions(Long accountId, PortfolioAccountType accountTypeId);

    void updateLoanTransaction(Long loanTransactionId, LoanTransaction newLoanTransaction);
    
    CommandProcessingResult refundByTransfer(JsonCommand command);

    void reverseTransfersWithFromAccountTransactions(Collection<Long> fromTransactionIds, PortfolioAccountType accountTypeId);

    AccountTransferDetails repayLoanWithTopup(AccountTransferDTO accountTransferDTO);
}