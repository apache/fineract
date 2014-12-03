/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.accountnumberformat.data.AccountNumberFormatData;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations;
import org.mifosplatform.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.mifosplatform.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.mifosplatform.infrastructure.accountnumberformat.exception.AccountNumberFormatNotFoundException;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
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
        }

        Object[] array = accountNumberPrefixTypesSet.toArray();
        AccountNumberPrefixType[] accountNumberPrefixTypes = Arrays.copyOf(array, array.length, AccountNumberPrefixType[].class);

        accountNumberPrefixTypeOptions.put(entityAccountType.getCode(),
                AccountNumberFormatEnumerations.accountNumberPrefixType(accountNumberPrefixTypes));
    }
}
