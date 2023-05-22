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
package org.apache.fineract.portfolio.savings.service.search;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionSearchValidator;
import org.apache.fineract.portfolio.savings.data.SavingsTransactionSearchResult;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionRepository;
import org.apache.fineract.portfolio.savings.domain.search.SavingsTransactionSearch;
import org.apache.fineract.portfolio.savings.domain.search.SavingsTransactionSearch.Filters;
import org.apache.fineract.portfolio.savings.domain.search.SavingsTransactionSearchParameters;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SavingsAccountTransactionsSearchServiceImpl implements SavingsAccountTransactionSearchService {

    private final PlatformSecurityContext context;

    private final SavingsAccountTransactionRepository savingsTransactionRepository;

    private final SavingsAccountTransactionSearchValidator searchValidator;

    @Override
    public Page<SavingsAccountTransactionData> searchTransactions(Long savingsId, PagedRequest<SavingsTransactionSearch> searchRequest) {
        validateSearchRequest(searchRequest);
        return executeSearch(savingsId, DepositAccountType.SAVINGS_DEPOSIT, searchRequest);
    }

    private void validateSearchRequest(PagedRequest<SavingsTransactionSearch> searchRequest) {
        Objects.requireNonNull(searchRequest, "searchRequest must not be null");
        context.isAuthenticated();
        Optional<SavingsTransactionSearch> request = searchRequest.getRequest();
        Filters searchFilters = request.map(SavingsTransactionSearch::getFilters).orElse(null);
        searchValidator.validateSearchFilters(searchFilters);
    }

    private Page<SavingsAccountTransactionData> executeSearch(Long savingsId, DepositAccountType depositType,
            PagedRequest<SavingsTransactionSearch> searchRequest) {
        Optional<SavingsTransactionSearch> request = searchRequest.getRequest();
        Pageable pageable = searchRequest.toPageable();
        Filters searchFilters = request.map(SavingsTransactionSearch::getFilters).orElse(null);
        SavingsTransactionSearchParameters searchParameters = SavingsTransactionSearchParameters.builder().savingsId(savingsId)
                .depositAccountType(DepositAccountType.SAVINGS_DEPOSIT).filters(searchFilters).pageable(pageable).build();
        org.springframework.data.domain.Page<SavingsAccountTransactionData> pageResult = savingsTransactionRepository
                .searchTransactions(searchParameters).map(SavingsTransactionSearchResult::toSavingsAccountTransactionData);
        return new Page<>(pageResult.getContent(), Long.valueOf(pageResult.getTotalElements()).intValue());
    }

}
