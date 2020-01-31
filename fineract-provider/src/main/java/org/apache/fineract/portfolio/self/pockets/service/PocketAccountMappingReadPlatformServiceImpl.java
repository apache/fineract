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
package org.apache.fineract.portfolio.self.pockets.service;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.self.pockets.data.PocketAccountMappingData;
import org.apache.fineract.portfolio.self.pockets.domain.PocketAccountMapping;
import org.apache.fineract.portfolio.self.pockets.domain.PocketAccountMappingRepositoryWrapper;
import org.apache.fineract.portfolio.self.pockets.domain.PocketRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PocketAccountMappingReadPlatformServiceImpl implements PocketAccountMappingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PocketRepositoryWrapper pocketRepositoryWrapper;
    private final PocketAccountMappingRepositoryWrapper pocketAccountMappingRepositoryWrapper;

    @Autowired
    public PocketAccountMappingReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final PlatformSecurityContext context, final PocketRepositoryWrapper pocketRepositoryWrapper,
            final PocketAccountMappingRepositoryWrapper pocketAccountMappingRepositoryWrapper) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.context = context;
        this.pocketRepositoryWrapper = pocketRepositoryWrapper;
        this.pocketAccountMappingRepositoryWrapper = pocketAccountMappingRepositoryWrapper;
    }

    @Override
    public PocketAccountMappingData retrieveAll() {
        final Long pocketId = this.pocketRepositoryWrapper.findByAppUserId(this.context.authenticatedUser().getId());
        if (pocketId != null) {
            Collection<PocketAccountMapping> pocketAccountMappingList = this.pocketAccountMappingRepositoryWrapper
                    .findByPocketId(pocketId);
            if (pocketAccountMappingList != null && !pocketAccountMappingList.isEmpty()) {
                Collection<PocketAccountMapping> loanAccounts = new ArrayList<>();
                Collection<PocketAccountMapping> savingsAccounts = new ArrayList<>();
                Collection<PocketAccountMapping> shareAccounts = new ArrayList<>();
                for (PocketAccountMapping pocketMapping : pocketAccountMappingList) {

                    if (pocketMapping.getAccountType().equals(EntityAccountType.LOAN.getValue())) {
                        loanAccounts.add(pocketMapping);
                    } else if (pocketMapping.getAccountType().equals(EntityAccountType.SAVINGS.getValue())) {
                        savingsAccounts.add(pocketMapping);
                    } else {
                        shareAccounts.add(pocketMapping);
                    }
                }
                return PocketAccountMappingData.instance(loanAccounts, savingsAccounts, shareAccounts);
            }
        }
        return null;
    }

    @Override
    public boolean validatePocketAndAccountMapping(Long pocketId, Long accountId, Integer accountType) {
        final String sql = "select count(id) from m_pocket_accounts_mapping mapping where pocket_id = ? and account_id = ? and account_type = ?";
        try {
            return this.jdbcTemplate.queryForObject(sql, new Object[] { pocketId, accountId, accountType },
                    Boolean.class);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

}
