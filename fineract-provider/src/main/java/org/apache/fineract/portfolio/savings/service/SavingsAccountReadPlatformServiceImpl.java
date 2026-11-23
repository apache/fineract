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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccrualData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class SavingsAccountReadPlatformServiceImpl implements SavingsAccountReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    // mappers
    private final SavingAccountMapper savingAccountMapper;
    // private final SavingsAccountAnnualFeeMapper annualFeeMapper;

    // pagination
    private final PaginationHelper paginationHelper;

    private final ColumnValidator columnValidator;

    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final SavingsAccountTransactionReadService savingsAccountTransactionReadService;
    private final SavingsAccountInterestPostingReadService savingsAccountInterestPostingReadService;

    public SavingsAccountReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            PaginationHelper paginationHelper, ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator,
            SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            SavingsAccountTransactionReadService savingsAccountTransactionReadService,
            SavingsAccountInterestPostingReadService savingsAccountInterestPostingReadService) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.sqlGenerator = sqlGenerator;
        this.savingsAccountRepositoryWrapper = savingsAccountRepositoryWrapper;
        this.savingAccountMapper = new SavingAccountMapper();
        this.columnValidator = columnValidator;
        this.paginationHelper = paginationHelper;
        this.savingsAccountTransactionReadService = savingsAccountTransactionReadService;
        this.savingsAccountInterestPostingReadService = savingsAccountInterestPostingReadService;
    }

    @Override
    public Collection<SavingsAccountData> retrieveAllForLookup(final Long clientId) {

        final StringBuilder sqlBuilder = new StringBuilder("select " + this.savingAccountMapper.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300 ");

        final Object[] queryParameters = new Object[] { clientId };
        return this.jdbcTemplate.query(sqlBuilder.toString(), this.savingAccountMapper, queryParameters);
    }

    @Override
    public Collection<SavingsAccountData> retrieveActiveForLookup(final Long clientId, DepositAccountType depositAccountType) {

        final StringBuilder sqlBuilder = new StringBuilder("select " + this.savingAccountMapper.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300 and sa.deposit_type_enum = ? ");

        final Object[] queryParameters = new Object[] { clientId, depositAccountType.getValue() };
        return this.jdbcTemplate.query(sqlBuilder.toString(), this.savingAccountMapper, queryParameters);
    }

    @Override
    public Collection<SavingsAccountData> retrieveActiveForLookup(final Long clientId, DepositAccountType depositAccountType,
            String currencyCode) {
        final StringBuilder sqlBuilder = new StringBuilder("select " + this.savingAccountMapper.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300 and sa.deposit_type_enum = ? and sa.currency_code = ? ");

        final Object[] queryParameters = new Object[] { clientId, depositAccountType.getValue(), currencyCode };
        return this.jdbcTemplate.query(sqlBuilder.toString(), this.savingAccountMapper, queryParameters);
    }

    @Override
    public Page<SavingsAccountData> retrieveAll(final SearchParameters searchParameters) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(this.savingAccountMapper.schema());

        sqlBuilder.append(" join m_office o on o.id = c.office_id");
        sqlBuilder.append(" where o.hierarchy like ?");

        final Object[] objectArray = new Object[3];
        objectArray[0] = hierarchySearchString;
        int arrayPos = 1;
        if (searchParameters != null) {

            if (StringUtils.isNotBlank(searchParameters.getStatus())) {
                sqlBuilder.append(" and sa.status_enum = ?");
                objectArray[arrayPos] = Integer.parseInt(searchParameters.getStatus());
                arrayPos = arrayPos + 1;
            }

            if (StringUtils.isNotBlank(searchParameters.getExternalId())) {
                sqlBuilder.append(" and sa.external_id = ?");
                objectArray[arrayPos] = searchParameters.getExternalId();
                arrayPos = arrayPos + 1;
            }
            if (searchParameters.getOfficeId() != null) {
                sqlBuilder.append(" and c.office_id = ?");
                objectArray[arrayPos++] = searchParameters.getOfficeId();
            }
            if (searchParameters.hasOrderBy()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());

                if (searchParameters.hasSortOrder()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.hasLimit()) {
                sqlBuilder.append(" ");
                if (searchParameters.hasOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
        }
        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), finalObjectArray, this.savingAccountMapper);
    }

    @Override
    public SavingsAccountData retrieveOne(final Long accountId) {

        try {
            final String sql = "select " + this.savingAccountMapper.schema() + " where sa.id = ?";

            return this.jdbcTemplate.queryForObject(sql, this.savingAccountMapper, new Object[] { accountId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(accountId, e);
        }
    }

    @Override
    public SavingsAccountTransactionData retrieveDepositTransactionTemplate(Long savingsId, DepositAccountType depositAccountType) {
        return savingsAccountTransactionReadService.retrieveDepositTransactionTemplate(savingsId, depositAccountType);
    }

    @Override
    public Collection<SavingsAccountTransactionData> retrieveAllTransactions(Long savingsId, DepositAccountType depositAccountType) {
        return savingsAccountTransactionReadService.retrieveAllTransactions(savingsId, depositAccountType);
    }

    @Override
    public SavingsAccountTransactionData retrieveSavingsTransaction(Long savingsId, Long transactionId,
            DepositAccountType depositAccountType) {
        return savingsAccountTransactionReadService.retrieveSavingsTransaction(savingsId, transactionId, depositAccountType);
    }

    @Override
    public List<SavingsAccountTransactionData> retrieveAllTransactionData(List<String> refNo) {
        return savingsAccountTransactionReadService.retrieveAllTransactionData(refNo);
    }

    @Override
    public Long retrieveSavingsTransactionIdByExternalId(ExternalId externalId) {
        return savingsAccountTransactionReadService.retrieveSavingsTransactionIdByExternalId(externalId);
    }

    @Override
    public List<SavingsAccountData> retrieveAllSavingsDataForInterestPosting(boolean backdatedTxnsAllowedTill, int pageSize, Integer status,
            Long maxSavingsId) {
        return savingsAccountInterestPostingReadService.retrieveAllSavingsDataForInterestPosting(backdatedTxnsAllowedTill, pageSize, status,
                maxSavingsId);
    }

    /*
     * @Override public Collection<SavingsAccountAnnualFeeData> retrieveAccountsWithAnnualFeeDue() { final String sql =
     * "select " + this.annualFeeMapper.schema() +
     * " where sa.annual_fee_next_due_date is not null and sa.annual_fee_next_due_date <= NOW()" ;
     *
     * return this.jdbcTemplate.query(sql, this.annualFeeMapper, new Object[] {}); }
     */

    @Override
    public Collection<SavingsAccountData> retrieveForLookup(Long clientId, Boolean overdraft) {

        SavingAccountMapperForLookup accountMapperForLookup = new SavingAccountMapperForLookup();
        final StringBuilder sqlBuilder = new StringBuilder("select " + accountMapperForLookup.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300");
        Object[] queryParameters = null;
        if (overdraft == null) {
            queryParameters = new Object[] { clientId };
        } else {
            sqlBuilder.append(" and sa.allow_overdraft = ?");
            queryParameters = new Object[] { clientId, overdraft };
        }
        return this.jdbcTemplate.query(sqlBuilder.toString(), accountMapperForLookup, queryParameters);

    }

    @Override
    public List<Long> retrieveSavingsIdsPendingInactive(LocalDate tenantLocalDate) {
        List<Long> ret = null;
        StringBuilder sql = new StringBuilder("select sa.id ");
        sql.append(" from m_savings_account as sa ");
        sql.append(" inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = true) ");
        sql.append(" where sa.status_enum = 300 ");
        sql.append(" and sa.sub_status_enum = 0 ");
        String compareDate = "(select COALESCE(max(sat.transaction_date), sa.activatedon_date) "
                + "from m_savings_account_transaction as sat where sat.is_reversed = false and sat.is_reversal = false"
                + " and sat.transaction_type_enum in (1,2) and sat.savings_account_id = sa.id)";
        sql.append(" and ").append(sqlGenerator.dateDiff("?", compareDate)).append(" >= sp.days_to_inactive ");

        try {
            ret = this.jdbcTemplate.queryForList(sql.toString(), Long.class, tenantLocalDate);
        } catch (EmptyResultDataAccessException e) {
            // ignore empty result scenario
        } catch (DataAccessException e) {
            throw e;
        }

        return ret;
    }

    @Override
    public List<Long> retrieveSavingsIdsPendingDormant(LocalDate tenantLocalDate) {
        List<Long> ret = null;
        StringBuilder sql = new StringBuilder("select sa.id ");
        sql.append(" from m_savings_account as sa ");
        sql.append(" inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = true) ");
        sql.append(" where sa.status_enum = 300 ");
        sql.append(" and sa.sub_status_enum = 100 ");
        sql.append(" and " + sqlGenerator.dateDiff("?",
                "(select COALESCE(max(sat.transaction_date),sa.activatedon_date) from m_savings_account_transaction as sat where sat.is_reversed = false and sat.is_reversal = false and sat.transaction_type_enum in (1,2) and sat.savings_account_id = sa.id)")
                + " ");
        sql.append(" >= sp.days_to_dormancy ");

        try {
            ret = this.jdbcTemplate.queryForList(sql.toString(), Long.class, tenantLocalDate);
        } catch (EmptyResultDataAccessException e) {
            // ignore empty result scenario
        } catch (DataAccessException e) {
            throw e;
        }

        return ret;
    }

    @Override
    public List<Long> retrieveSavingsIdsPendingEscheat(LocalDate tenantLocalDate) {
        List<Long> ret = null;
        StringBuilder sql = new StringBuilder("select sa.id ");
        sql.append(" from m_savings_account as sa ");
        sql.append(" inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = true) ");
        sql.append(" where sa.status_enum = 300 ");
        sql.append(" and sa.sub_status_enum = 200 ");
        sql.append(" and " + sqlGenerator.dateDiff("?",
                "(select COALESCE(max(sat.transaction_date),sa.activatedon_date) from m_savings_account_transaction as sat where sat.is_reversed = false and sat.is_reversal = false and sat.transaction_type_enum in (1,2) and sat.savings_account_id = sa.id)")
                + " ");
        sql.append(" >= sp.days_to_escheat ");

        try {
            ret = this.jdbcTemplate.queryForList(sql.toString(), Long.class, new Object[] { tenantLocalDate });
        } catch (EmptyResultDataAccessException e) {
            // ignore empty result scenario
        } catch (DataAccessException e) {
            throw e;
        }

        return ret;
    }

    @Override
    public boolean isAccountBelongsToClient(final Long clientId, final Long accountId, final DepositAccountType depositAccountType,
            final String currencyCode) {
        try {
            final StringBuilder buff = new StringBuilder("select count(*) from m_savings_account sa ");
            buff.append(
                    " where sa.id = ? and sa.client_id = ? and sa.deposit_type_enum = ? and sa.currency_code = ? and sa.status_enum = 300");
            return this.jdbcTemplate.queryForObject(buff.toString(), Integer.class, accountId, clientId, depositAccountType.getValue(),
                    currencyCode) > 0;
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(accountId, e);
        }
    }
    /*
     * private static final class SavingsAccountAnnualFeeMapper implements RowMapper<SavingsAccountAnnualFeeData> {
     *
     * private final String schemaSql;
     *
     * public SavingsAccountAnnualFeeMapper() { final StringBuilder sqlBuilder = new StringBuilder(200);
     * sqlBuilder.append("sa.id as id, sa.account_no as accountNo, "); sqlBuilder
     * .append("sa.annual_fee_next_due_date as annualFeeNextDueDate "); sqlBuilder.append("from m_savings_account sa ");
     *
     * this.schemaSql = sqlBuilder.toString(); }
     *
     * public String schema() { return this.schemaSql; }
     *
     * @Override public SavingsAccountAnnualFeeData mapRow(final ResultSet rs,
     *
     * @SuppressWarnings("unused") final int rowNum) throws SQLException {
     *
     * final Long id = rs.getLong("id"); final String accountNo = rs.getString("accountNo"); final LocalDate
     * annualFeeNextDueDate = JdbcSupport.getLocalDate(rs, "annualFeeNextDueDate");
     *
     * return SavingsAccountAnnualFeeData.instance(id, accountNo, annualFeeNextDueDate); } }
     */

    @Override
    public String retrieveAccountNumberByAccountId(Long accountId) {
        try {
            final String sql = "select s.account_no from m_savings_account s where s.id = ?";
            return this.jdbcTemplate.queryForObject(sql, String.class, accountId);
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(accountId, e);
        }
    }

    @Override
    public List<Long> getAccountsIdsByStatusPaged(Integer status, int pageSize, Long maxSavingsIdInList) {
        final String sql = """
                SELECT sa.id FROM m_savings_account sa
                where sa.id > ? and sa.status_enum  = ?  order by sa.id limit ?\s""";

        try {
            return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] { maxSavingsIdInList, status, pageSize });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Long retrieveAccountIdByExternalId(final ExternalId externalId) {
        return savingsAccountRepositoryWrapper.findIdByExternalId(externalId);
    }

    @Override
    public List<SavingsAccrualData> retrievePeriodicAccrualData(LocalDate tillDate, SavingsAccount savings) {
        Long savingsId = (savings != null) ? savings.getId() : null;
        Integer status = SavingsAccountStatusType.ACTIVE.getValue();
        Integer accountingRule = AccountingRuleType.ACCRUAL_PERIODIC.getValue();

        return this.savingsAccountRepositoryWrapper.findAccrualData(tillDate, savingsId, status, accountingRule);
    }
}
