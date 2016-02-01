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
package org.apache.fineract.infrastructure.accountnumberformat.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.infrastructure.accountnumberformat.data.AccountNumberFormatData;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.apache.fineract.infrastructure.accountnumberformat.exception.AccountNumberFormatNotFoundException;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountNumberFormatReadPlatformServiceImpl implements AccountNumberFormatReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    // data mapper
    private final AccountNumberFormatMapper accountNumberFormatMapper = new AccountNumberFormatMapper();

    @Autowired
    public AccountNumberFormatReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class AccountNumberFormatMapper implements RowMapper<AccountNumberFormatData> {

        private final String schema;

        public AccountNumberFormatMapper() {
            final StringBuilder builder = new StringBuilder(400);

            builder.append(" anf.id as id, anf.account_type_enum as accountTypeEnum, anf.prefix_type_enum as prefixTypeEnum");
            builder.append(" from c_account_number_format anf ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public AccountNumberFormatData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Integer accountTypeEnum = rs.getInt("accountTypeEnum");
            final Integer prefixTypeEnum = JdbcSupport.getInteger(rs, "prefixTypeEnum");

            final EnumOptionData accountNumberType = AccountNumberFormatEnumerations.entityAccountType(accountTypeEnum);
            EnumOptionData prefixType = null;
            if (prefixTypeEnum != null) {
                prefixType = AccountNumberFormatEnumerations.accountNumberPrefixType(prefixTypeEnum);
            }
            return new AccountNumberFormatData(id, accountNumberType, prefixType);
        }
    }

    @Override
    public List<AccountNumberFormatData> getAllAccountNumberFormats() {
        String sql = "select " + this.accountNumberFormatMapper.schema();
        return this.jdbcTemplate.query(sql, this.accountNumberFormatMapper, new Object[] {});
    }

    @Override
    public AccountNumberFormatData getAccountNumberFormat(Long id) {
        try {
            final String sql = "select " + this.accountNumberFormatMapper.schema() + " where anf.id = ?";

            final AccountNumberFormatData accountNumberFormatData = this.jdbcTemplate.queryForObject(sql, this.accountNumberFormatMapper,
                    new Object[] { id });
            return accountNumberFormatData;
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountNumberFormatNotFoundException(id);
        }
    }

    @Override
    public AccountNumberFormatData retrieveTemplate(EntityAccountType entityAccountTypeForTemplate) {
        final List<EnumOptionData> entityAccountTypeOptions = AccountNumberFormatEnumerations.entityAccountType(EntityAccountType.values());

        Map<String, List<EnumOptionData>> accountNumberPrefixTypeOptions = new HashMap<>();
        /***
         * If an Account type is passed in, return prefixes only for the passed
         * in account type, else return all allowed prefixes keyed by all
         * possible entity type
         **/
        if (entityAccountTypeForTemplate != null) {
            determinePrefixTypesForAccounts(accountNumberPrefixTypeOptions, entityAccountTypeForTemplate);
        } else {
            for (EntityAccountType entityAccountType : EntityAccountType.values()) {
                determinePrefixTypesForAccounts(accountNumberPrefixTypeOptions, entityAccountType);

            }
        }
        return new AccountNumberFormatData(entityAccountTypeOptions, accountNumberPrefixTypeOptions);
    }

    public void determinePrefixTypesForAccounts(Map<String, List<EnumOptionData>> accountNumberPrefixTypeOptions,
            EntityAccountType entityAccountType) {
        Set<AccountNumberPrefixType> accountNumberPrefixTypesSet = new HashSet<>();
        switch (entityAccountType) {
            case CLIENT:
                accountNumberPrefixTypesSet = AccountNumberFormatEnumerations.accountNumberPrefixesForClientAccounts;
            break;
            case LOAN:
                accountNumberPrefixTypesSet = AccountNumberFormatEnumerations.accountNumberPrefixesForLoanAccounts;
            break;
            case SAVINGS:
                accountNumberPrefixTypesSet = AccountNumberFormatEnumerations.accountNumberPrefixesForSavingsAccounts;
            break;
            case CENTER :
                accountNumberPrefixTypesSet = AccountNumberFormatEnumerations.accountNumberPrefixesForCenters;
            break;
            case GROUP :
                accountNumberPrefixTypesSet = AccountNumberFormatEnumerations.accountNumberPrefixesForGroups;
            break;
        }

        Object[] array = accountNumberPrefixTypesSet.toArray();
        AccountNumberPrefixType[] accountNumberPrefixTypes = Arrays.copyOf(array, array.length, AccountNumberPrefixType[].class);

        accountNumberPrefixTypeOptions.put(entityAccountType.getCode(),
                AccountNumberFormatEnumerations.accountNumberPrefixType(accountNumberPrefixTypes));
    }
}
