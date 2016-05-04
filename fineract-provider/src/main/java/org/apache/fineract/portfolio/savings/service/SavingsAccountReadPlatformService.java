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
import java.util.List;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.joda.time.LocalDate;

public interface SavingsAccountReadPlatformService {

    Page<SavingsAccountData> retrieveAll(SearchParameters searchParameters);

    Collection<SavingsAccountData> retrieveAllForLookup(Long clientId);

    Collection<SavingsAccountData> retrieveActiveForLookup(Long clientId, DepositAccountType depositAccountType);

    public Collection<SavingsAccountData> retrieveActiveForLookup(final Long clientId, DepositAccountType depositAccountType, String currencyCode) ;
    
    SavingsAccountData retrieveOne(Long savingsId);

    SavingsAccountData retrieveTemplate(Long clientId, Long groupId, Long productId, boolean staffInSelectedOfficeOnly);

    SavingsAccountTransactionData retrieveDepositTransactionTemplate(Long savingsId, DepositAccountType depositAccountType);

    Collection<SavingsAccountTransactionData> retrieveAllTransactions(Long savingsId, DepositAccountType depositAccountType);

    // Collection<SavingsAccountAnnualFeeData>
    // retrieveAccountsWithAnnualFeeDue();

    SavingsAccountTransactionData retrieveSavingsTransaction(Long savingsId, Long transactionId, DepositAccountType depositAccountType);

    Collection<SavingsAccountData> retrieveForLookup(Long clientId, Boolean overdraft);
    
    List<Long> retrieveSavingsIdsPendingInactive(LocalDate tenantLocalDate);

    List<Long> retrieveSavingsIdsPendingDormant(LocalDate tenantLocalDate);

    List<Long> retrieveSavingsIdsPendingEscheat(LocalDate tenantLocalDate);
    boolean isAccountBelongsToClient(final Long clientId, final Long accountId, final DepositAccountType depositAccountType, final String currencyCode) ;
    
}