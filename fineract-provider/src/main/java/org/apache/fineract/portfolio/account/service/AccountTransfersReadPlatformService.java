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

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.joda.time.LocalDate;

public interface AccountTransfersReadPlatformService {

    AccountTransferData retrieveTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType);

    Page<AccountTransferData> retrieveAll(SearchParameters searchParameters, Long accountDetailId);

    AccountTransferData retrieveOne(Long transferId);

    boolean isAccountTransfer(Long transactionId, PortfolioAccountType accountType);

    Page<AccountTransferData> retrieveByStandingInstruction(Long id, SearchParameters searchParameters);

    Collection<Long> fetchPostInterestTransactionIds(Long accountId);
    
    AccountTransferData retrieveRefundByTransferTemplate(Long fromOfficeId, Long fromClientId, Long fromAccountId, Integer fromAccountType,
            Long toOfficeId, Long toClientId, Long toAccountId, Integer toAccountType);

	BigDecimal getTotalTransactionAmount(Long accountId, Integer accountType,
			LocalDate transactionDate);
}