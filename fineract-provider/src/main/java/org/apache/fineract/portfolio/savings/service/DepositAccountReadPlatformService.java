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

import java.util.Collection;
import java.util.Map;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;

public interface DepositAccountReadPlatformService {

    Collection<DepositAccountData> retrieveAll(DepositAccountType depositAccountType, PaginationParameters paginationParameters);

    Page<DepositAccountData> retrieveAllPaged(DepositAccountType depositAccountType, PaginationParameters paginationParameters);

    Collection<DepositAccountData> retrieveAllForLookup(DepositAccountType depositAccountType);

    DepositAccountData retrieveOne(DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveOneWithClosureTemplate(DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveOneWithChartSlabs(DepositAccountType depositAccountType, Long productId);

    Collection<SavingsAccountTransactionData> retrieveAllTransactions(DepositAccountType depositAccountType, Long accountId);

    DepositAccountData retrieveTemplate(DepositAccountType depositAccountType, Long clientId, Long groupId, Long productId,
            boolean staffInSelectedOfficeOnly);

    Collection<DepositAccountData> retrieveForMaturityUpdate();

    SavingsAccountTransactionData retrieveRecurringAccountDepositTransactionTemplate(Long accountId);

    Collection<AccountTransferDTO> retrieveDataForInterestTransfer();

    Collection<Map<String, Object>> retriveDataForRDScheduleCreation();
}
