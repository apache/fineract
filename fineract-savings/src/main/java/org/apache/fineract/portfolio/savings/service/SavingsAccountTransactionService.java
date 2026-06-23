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
package org.apache.fineract.portfolio.savings.service;

import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionDTO;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;

/**
 * Orchestrates value transactions (currently {@code withdraw}) on a {@link SavingsAccount}. The bodies were extracted
 * from the entity; behaviour is intentionally unchanged. Account state is read/written through the public API of the
 * entity and the withdrawal fee is applied via {@link SavingsAccountChargeProcessingService}, so the transaction
 * orchestration no longer lives on the domain entity.
 */
public interface SavingsAccountTransactionService {

    SavingsAccountTransaction withdraw(SavingsAccount account, SavingsAccountTransactionDTO transactionDTO, boolean applyWithdrawFee,
            boolean backdatedTxnsAllowedTill, Long relaxingDaysConfigForPivotDate, String refNo);

    SavingsAccountTransaction deposit(SavingsAccount account, SavingsAccountTransactionDTO transactionDTO, boolean backdatedTxnsAllowedTill,
            Long relaxingDaysConfigForPivotDate, String refNo);

    SavingsAccountTransaction deposit(SavingsAccount account, SavingsAccountTransactionDTO transactionDTO,
            SavingsAccountTransactionType savingsAccountTransactionType, boolean backdatedTxnsAllowedTill,
            Long relaxingDaysConfigForPivotDate, String refNo);
}
