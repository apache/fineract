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
package org.apache.fineract.portfolio.client.service.search;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.PagedRequest;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.ClientRepository;
import org.apache.fineract.portfolio.client.service.search.domain.ClientSearchData;
import org.apache.fineract.portfolio.client.service.search.domain.ClientTextSearch;
import org.apache.fineract.portfolio.client.service.search.mapper.ClientSearchDataMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClientSearchService {

    private final PlatformSecurityContext context;
    private final ClientRepository clientRepository;
    private final ClientSearchDataMapper clientSearchDataMapper;

    public Page<ClientSearchData> searchByText(PagedRequest<ClientTextSearch> searchRequest) {
        validateTextSearchRequest(searchRequest);
        return executeTextSearch(searchRequest);
    }

    private void validateTextSearchRequest(PagedRequest<ClientTextSearch> searchRequest) {
        Objects.requireNonNull(searchRequest, "searchRequest must not be null");

        context.isAuthenticated();
    }

    private Page<ClientSearchData> executeTextSearch(PagedRequest<ClientTextSearch> searchRequest) {
        final String hierarchy = context.authenticatedUser().getOffice().getHierarchy();

        Optional<ClientTextSearch> request = searchRequest.getRequest();
        String requestSearchText = request.map(ClientTextSearch::getText).orElse(null);
        String searchText = StringUtils.defaultString(requestSearchText, "");

        Pageable pageable = searchRequest.toPageable();

        return clientRepository.searchByText(searchText, pageable, hierarchy).map(clientSearchDataMapper::map);
    }
}
