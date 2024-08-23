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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

public class AccountTransfersReadPlatformServiceImpl implements AccountTransfersReadPlatformService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    // mapper
    private final AccountTransfersMapper accountTransfersMapper;
    // pagination
    private final PaginationHelper paginationHelper;

    public AccountTransfersReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate,
            final ClientReadPlatformService clientReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService, final ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        this.jdbcTemplate = jdbcTemplate;
        this.clientReadPlatformService = clientReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformService;
        this.columnValidator = columnValidator;
        this.sqlGenerator = sqlGenerator;
        this.accountTransfersMapper = new AccountTransfersMapper();
        this.paginationHelper = paginationHelper;
    }

    @Override
    public AccountTransferData retrieveTemplate(final Long fromOfficeId, final Long fromClientId, final Long fromAccountId,
            final Integer fromAccountType, final Long toOfficeId, final Long toClientId, final Long toAccountId,
            final Integer toAccountType) {

        final EnumOptionData loanAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
        final EnumOptionData savingsAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);

        final Integer mostRelevantFromAccountType = fromAccountType;
        final Collection<EnumOptionData> fromAccountTypeOptions = Arrays.asList(savingsAccountType, loanAccountType);
        final Collection<EnumOptionData> toAccountTypeOptions;
        if (mostRelevantFromAccountType != null && mostRelevantFromAccountType == 1) {
            // overpaid loan amt transfer to savings account
            toAccountTypeOptions = Arrays.asList(savingsAccountType);
        } else {
            toAccountTypeOptions = Arrays.asList(loanAccountType, savingsAccountType);
        }
        final Integer mostRelevantToAccountType = toAccountType;

        final EnumOptionData fromAccountTypeData = AccountTransferEnumerations.accountType(mostRelevantFromAccountType);
        final EnumOptionData toAccountTypeData = AccountTransferEnumerations.accountType(mostRelevantToAccountType);

        // from settings
        OfficeData fromOffice = null;
        ClientData fromClient = null;
        PortfolioAccountData fromAccount = null;

        OfficeData toOffice = null;
        ClientData toClient = null;
        PortfolioAccountData toAccount = null;

        // template
        Collection<PortfolioAccountData> fromAccountOptions = null;
        Collection<PortfolioAccountData> toAccountOptions = null;

        Long mostRelevantFromOfficeId = fromOfficeId;
        Long mostRelevantFromClientId = fromClientId;

        Long mostRelevantToOfficeId = toOfficeId;
        Long mostRelevantToClientId = toClientId;

        if (fromAccountId != null) {
            Integer accountType;
            if (mostRelevantFromAccountType == 1) {
                accountType = PortfolioAccountType.LOAN.getValue();
            } else {
                accountType = PortfolioAccountType.SAVINGS.getValue();
            }
            fromAccount = this.portfolioAccountReadPlatformService.retrieveOne(fromAccountId, accountType);

            // override provided fromClient with client of account
            mostRelevantFromClientId = fromAccount.getClientId();
        }

        if (mostRelevantFromClientId != null) {
            fromClient = this.clientReadPlatformService.retrieveOne(mostRelevantFromClientId);
            mostRelevantFromOfficeId = fromClient.getOfficeId();
            long[] loanStatus = null;
            if (mostRelevantFromAccountType == 1) {
                loanStatus = new long[] { 300, 700 };
            }
            PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(mostRelevantFromAccountType, mostRelevantFromClientId,
                    loanStatus);
            fromAccountOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
        }

        Collection<OfficeData> fromOfficeOptions = null;
        Collection<ClientData> fromClientOptions = null;
        if (mostRelevantFromOfficeId != null) {
            fromOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantFromOfficeId);
            fromOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
            fromClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantFromOfficeId);
        }

        // defaults
        final LocalDate transferDate = DateUtils.getBusinessLocalDate();
        Collection<OfficeData> toOfficeOptions = fromOfficeOptions;
        Collection<ClientData> toClientOptions = null;

        if (toAccountId != null && fromAccount != null) {
            toAccount = this.portfolioAccountReadPlatformService.retrieveOne(toAccountId, mostRelevantToAccountType,
                    fromAccount.getCurrencyCode());
            mostRelevantToClientId = toAccount.getClientId();
        }

        if (mostRelevantToClientId != null) {
            toClient = this.clientReadPlatformService.retrieveOne(mostRelevantToClientId);
            mostRelevantToOfficeId = toClient.getOfficeId();

            toClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantToOfficeId);

            toAccountOptions = retrieveToAccounts(fromAccount, mostRelevantToAccountType, mostRelevantToClientId);
        }

        if (mostRelevantToOfficeId != null) {
            toOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantToOfficeId);
            toOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

            toClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantToOfficeId);
            if (toClientOptions != null && toClientOptions.size() == 1) {
                toClient = new ArrayList<>(toClientOptions).get(0);

                toAccountOptions = retrieveToAccounts(fromAccount, mostRelevantToAccountType, mostRelevantToClientId);
            }
        }

        return AccountTransferData.template(fromOffice, fromClient, fromAccountTypeData, fromAccount, transferDate, toOffice, toClient,
                toAccountTypeData, toAccount, fromOfficeOptions, fromClientOptions, fromAccountTypeOptions, fromAccountOptions,
                toOfficeOptions, toClientOptions, toAccountTypeOptions, toAccountOptions);
    }

    private Collection<PortfolioAccountData> retrieveToAccounts(final PortfolioAccountData excludeThisAccountFromOptions,
            final Integer toAccountType, final Long toClientId) {

        final String currencyCode = excludeThisAccountFromOptions != null ? excludeThisAccountFromOptions.getCurrencyCode() : null;

        PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(toAccountType, toClientId, currencyCode, null, null);
        Collection<PortfolioAccountData> accountOptions = this.portfolioAccountReadPlatformService
                .retrieveAllForLookup(portfolioAccountDTO);
        if (!CollectionUtils.isEmpty(accountOptions)) {
            accountOptions.remove(excludeThisAccountFromOptions);
        } else {
            accountOptions = null;
        }

        return accountOptions;
    }

    @Override
    public Page<AccountTransferData> retrieveAll(final SearchParameters searchParameters, final Long accountDetailId) {

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(this.accountTransfersMapper.schema());
        Object[] finalObjectArray = {};
        if (accountDetailId != null) {
            sqlBuilder.append(" where att.account_transfer_details_id=?");
            finalObjectArray = new Object[] { accountDetailId };
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
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.hasOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), finalObjectArray, this.accountTransfersMapper);
    }

    @Override
    public AccountTransferData retrieveOne(final Long transferId) {

        try {
            final String sql = "select " + this.accountTransfersMapper.schema() + " where att.id = ?";

            return this.jdbcTemplate.queryForObject(sql, this.accountTransfersMapper, new Object[] { transferId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountTransferNotFoundException(transferId, e);
        }
    }

    @Override
    public Collection<Long> fetchPostInterestTransactionIds(final Long accountId) {
        final String sql = "select att.from_savings_transaction_id from m_account_transfer_transaction att inner join m_account_transfer_details atd on atd.id = att.account_transfer_details_id where atd.from_savings_account_id=? and att.is_reversed = false and atd.transfer_type = ?";

        return this.jdbcTemplate.queryForList(sql, Long.class, accountId, AccountTransferType.INTEREST_TRANSFER.getValue());
    }

    @Override
    public Collection<Long> fetchPostInterestTransactionIdsWithPivotDate(final Long accountId, final LocalDate pivotDate) {
        final String sql = "select att.from_savings_transaction_id from m_account_transfer_transaction att inner join m_account_transfer_details atd on atd.id = att.account_transfer_details_id where atd.from_savings_account_id=? and att.is_reversed = false and atd.transfer_type = ? and att.transaction_date >= ?";

        return this.jdbcTemplate.queryForList(sql, Long.class, accountId, AccountTransferType.INTEREST_TRANSFER.getValue(), pivotDate);
    }

    @Override
    public boolean isAccountTransfer(final Long transactionId, final PortfolioAccountType accountType) {
        final StringBuilder sql = new StringBuilder("select count(*) from m_account_transfer_transaction at where ");
        if (accountType.isLoanAccount()) {
            sql.append("at.from_loan_transaction_id=").append(transactionId).append(" or at.to_loan_transaction_id=").append(transactionId);
        } else {
            sql.append("at.from_savings_transaction_id=").append(transactionId).append(" or at.to_savings_transaction_id=")
                    .append(transactionId);
        }

        final int count = this.jdbcTemplate.queryForObject(sql.toString(), Integer.class);
        return count > 0;
    }

    @Override
    public Page<AccountTransferData> retrieveByStandingInstruction(final Long id, final SearchParameters searchParameters) {

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(this.accountTransfersMapper.schema()).append(
                " join m_account_transfer_standing_instructions atsi on atsi.account_transfer_details_id = att.account_transfer_details_id ");
        sqlBuilder.append(" where atsi.id = ?");

        if (searchParameters != null) {
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

        final Object[] finalObjectArray = { id };
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), finalObjectArray, this.accountTransfersMapper);
    }

    @Override
    public AccountTransferData retrieveRefundByTransferTemplate(final Long fromOfficeId, final Long fromClientId, final Long fromAccountId,
            final Integer fromAccountType, final Long toOfficeId, final Long toClientId, final Long toAccountId,
            final Integer toAccountType) {
        // TODO Auto-generated method stub
        final EnumOptionData loanAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
        final EnumOptionData savingsAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);

        final Integer mostRelevantFromAccountType = fromAccountType;
        final Collection<EnumOptionData> fromAccountTypeOptions = Arrays.asList(savingsAccountType, loanAccountType);
        final Collection<EnumOptionData> toAccountTypeOptions;
        if (mostRelevantFromAccountType == 1) {
            // overpaid loan amt transfer to savings account
            toAccountTypeOptions = Arrays.asList(savingsAccountType);
        } else {
            toAccountTypeOptions = Arrays.asList(loanAccountType, savingsAccountType);
        }
        final Integer mostRelevantToAccountType = toAccountType;

        final EnumOptionData fromAccountTypeData = AccountTransferEnumerations.accountType(mostRelevantFromAccountType);
        final EnumOptionData toAccountTypeData = AccountTransferEnumerations.accountType(mostRelevantToAccountType);

        // from settings
        OfficeData fromOffice = null;
        ClientData fromClient = null;
        PortfolioAccountData fromAccount = null;

        OfficeData toOffice = null;
        ClientData toClient = null;
        PortfolioAccountData toAccount = null;

        // template
        Collection<PortfolioAccountData> fromAccountOptions = null;
        Collection<PortfolioAccountData> toAccountOptions = null;

        Long mostRelevantFromOfficeId = fromOfficeId;
        Long mostRelevantFromClientId = fromClientId;

        Long mostRelevantToOfficeId = toOfficeId;
        Long mostRelevantToClientId = toClientId;

        if (fromAccountId != null) {
            Integer accountType;
            if (mostRelevantFromAccountType == 1) {
                accountType = PortfolioAccountType.LOAN.getValue();
            } else {
                accountType = PortfolioAccountType.SAVINGS.getValue();
            }
            fromAccount = this.portfolioAccountReadPlatformService.retrieveOneByPaidInAdvance(fromAccountId, accountType);

            // override provided fromClient with client of account
            mostRelevantFromClientId = fromAccount.getClientId();
        }

        if (mostRelevantFromClientId != null) {
            fromClient = this.clientReadPlatformService.retrieveOne(mostRelevantFromClientId);
            mostRelevantFromOfficeId = fromClient.getOfficeId();
            long[] loanStatus = null;
            if (mostRelevantFromAccountType == 1) {
                loanStatus = new long[] { 300, 700 };
            }
            PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(mostRelevantFromAccountType, mostRelevantFromClientId,
                    loanStatus);
            fromAccountOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);
        }

        Collection<OfficeData> fromOfficeOptions = null;
        Collection<ClientData> fromClientOptions = null;
        if (mostRelevantFromOfficeId != null) {
            fromOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantFromOfficeId);
            fromOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
            fromClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantFromOfficeId);
        }

        // defaults
        final LocalDate transferDate = DateUtils.getBusinessLocalDate();
        Collection<OfficeData> toOfficeOptions = fromOfficeOptions;
        Collection<ClientData> toClientOptions = null;

        if (toAccountId != null && fromAccount != null) {
            toAccount = this.portfolioAccountReadPlatformService.retrieveOne(toAccountId, mostRelevantToAccountType,
                    fromAccount.getCurrencyCode());
            mostRelevantToClientId = toAccount.getClientId();
        }

        if (mostRelevantToClientId != null) {
            toClient = this.clientReadPlatformService.retrieveOne(mostRelevantToClientId);
            mostRelevantToOfficeId = toClient.getOfficeId();

            toClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantToOfficeId);

            toAccountOptions = retrieveToAccounts(fromAccount, mostRelevantToAccountType, mostRelevantToClientId);
        }

        if (mostRelevantToOfficeId != null) {
            toOffice = this.officeReadPlatformService.retrieveOffice(mostRelevantToOfficeId);
            toOfficeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

            toClientOptions = this.clientReadPlatformService.retrieveAllForLookupByOfficeId(mostRelevantToOfficeId);
            if (toClientOptions != null && toClientOptions.size() == 1) {
                toClient = new ArrayList<>(toClientOptions).get(0);

                toAccountOptions = retrieveToAccounts(fromAccount, mostRelevantToAccountType, mostRelevantToClientId);
            }
        }

        return AccountTransferData.template(fromOffice, fromClient, fromAccountTypeData, fromAccount, transferDate, toOffice, toClient,
                toAccountTypeData, toAccount, fromOfficeOptions, fromClientOptions, fromAccountTypeOptions, fromAccountOptions,
                toOfficeOptions, toClientOptions, toAccountTypeOptions, toAccountOptions);
    }

    @Override
    public BigDecimal getTotalTransactionAmount(Long accountId, Integer accountType, LocalDate transactionDate) {
        StringBuilder sqlBuilder = new StringBuilder(" select sum(trans.amount) as totalTransactionAmount ");
        sqlBuilder.append(" from m_account_transfer_details as det ");
        sqlBuilder.append(" inner join m_account_transfer_transaction as trans ");
        sqlBuilder.append(" on det.id = trans.account_transfer_details_id ");
        sqlBuilder.append(" where trans.is_reversed = false ");
        sqlBuilder.append(" and trans.transaction_date = ? ");
        sqlBuilder.append(" and IF(1=?, det.from_loan_account_id = ?, det.from_savings_account_id = ?) ");

        return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), BigDecimal.class, DATE_TIME_FORMATTER.format(transactionDate),
                accountType, accountId, accountId);
    }

    private static final class AccountTransfersMapper implements RowMapper<AccountTransferData> {

        private final String schemaSql;

        AccountTransfersMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("att.id as id, att.is_reversed as isReversed,");
            sqlBuilder.append("att.transaction_date as transferDate, att.amount as transferAmount,");
            sqlBuilder.append("att.description as transferDescription,");
            sqlBuilder.append("att.currency_code as currencyCode, att.currency_digits as currencyDigits,");
            sqlBuilder.append("att.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("fromoff.id as fromOfficeId, fromoff.name as fromOfficeName,");
            sqlBuilder.append("tooff.id as toOfficeId, tooff.name as toOfficeName,");
            sqlBuilder.append("fromclient.id as fromClientId, fromclient.display_name as fromClientName,");
            sqlBuilder.append("toclient.id as toClientId, toclient.display_name as toClientName,");
            sqlBuilder.append("fromsavacc.id as fromSavingsAccountId, fromsavacc.account_no as fromSavingsAccountNo,");
            sqlBuilder.append("fromloanacc.id as fromLoanAccountId, fromloanacc.account_no as fromLoanAccountNo,");
            sqlBuilder.append("tosavacc.id as toSavingsAccountId, tosavacc.account_no as toSavingsAccountNo,");
            sqlBuilder.append("toloanacc.id as toLoanAccountId, toloanacc.account_no as toLoanAccountNo,");
            sqlBuilder.append("fromsavtran.id as fromSavingsAccountTransactionId,");
            sqlBuilder.append("fromsavtran.transaction_type_enum as fromSavingsAccountTransactionType,");
            sqlBuilder.append("tosavtran.id as toSavingsAccountTransactionId,");
            sqlBuilder.append("tosavtran.transaction_type_enum as toSavingsAccountTransactionType");
            sqlBuilder.append(" FROM m_account_transfer_transaction att ");
            sqlBuilder.append("left join m_account_transfer_details atd on atd.id = att.account_transfer_details_id ");
            sqlBuilder.append("join m_currency curr on curr.code = att.currency_code ");
            sqlBuilder.append("join m_office fromoff on fromoff.id = atd.from_office_id ");
            sqlBuilder.append("join m_office tooff on tooff.id = atd.to_office_id ");
            sqlBuilder.append("join m_client fromclient on fromclient.id = atd.from_client_id ");
            sqlBuilder.append("join m_client toclient on toclient.id = atd.to_client_id ");
            sqlBuilder.append("left join m_savings_account fromsavacc on fromsavacc.id = atd.from_savings_account_id ");
            sqlBuilder.append("left join m_loan fromloanacc on fromloanacc.id = atd.from_loan_account_id ");
            sqlBuilder.append("left join m_savings_account tosavacc on tosavacc.id = atd.to_savings_account_id ");
            sqlBuilder.append("left join m_loan toloanacc on toloanacc.id = atd.to_loan_account_id ");
            sqlBuilder.append("left join m_savings_account_transaction fromsavtran on fromsavtran.id = att.from_savings_transaction_id ");
            sqlBuilder.append("left join m_savings_account_transaction tosavtran on tosavtran.id = att.to_savings_transaction_id ");
            sqlBuilder.append("left join m_loan_transaction fromloantran on fromloantran.id = att.from_savings_transaction_id ");
            sqlBuilder.append("left join m_loan_transaction toloantran on toloantran.id = att.to_savings_transaction_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public AccountTransferData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final boolean reversed = rs.getBoolean("isReversed");

            final LocalDate transferDate = JdbcSupport.getLocalDate(rs, "transferDate");
            final BigDecimal transferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transferAmount");
            final String transferDescription = rs.getString("transferDescription");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

            final Long fromOfficeId = JdbcSupport.getLong(rs, "fromOfficeId");
            final String fromOfficeName = rs.getString("fromOfficeName");
            final OfficeData fromOffice = OfficeData.dropdown(fromOfficeId, fromOfficeName, null);

            final Long toOfficeId = JdbcSupport.getLong(rs, "toOfficeId");
            final String toOfficeName = rs.getString("toOfficeName");
            final OfficeData toOffice = OfficeData.dropdown(toOfficeId, toOfficeName, null);

            final Long fromClientId = JdbcSupport.getLong(rs, "fromClientId");
            final String fromClientName = rs.getString("fromClientName");
            final ClientData fromClient = ClientData.lookup(fromClientId, fromClientName, fromOfficeId, fromOfficeName);

            final Long toClientId = JdbcSupport.getLong(rs, "toClientId");
            final String toClientName = rs.getString("toClientName");
            final ClientData toClient = ClientData.lookup(toClientId, toClientName, toOfficeId, toOfficeName);

            final Long fromSavingsAccountId = JdbcSupport.getLong(rs, "fromSavingsAccountId");
            final String fromSavingsAccountNo = rs.getString("fromSavingsAccountNo");
            final Long fromLoanAccountId = JdbcSupport.getLong(rs, "fromLoanAccountId");
            final String fromLoanAccountNo = rs.getString("fromLoanAccountNo");
            PortfolioAccountData fromAccount = null;
            EnumOptionData fromAccountType = null;
            if (fromSavingsAccountId != null) {
                fromAccount = PortfolioAccountData.lookup(fromSavingsAccountId, fromSavingsAccountNo);
                fromAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);
            } else if (fromLoanAccountId != null) {
                fromAccount = PortfolioAccountData.lookup(fromLoanAccountId, fromLoanAccountNo);
                fromAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
            }

            PortfolioAccountData toAccount = null;
            EnumOptionData toAccountType = null;
            final Long toSavingsAccountId = JdbcSupport.getLong(rs, "toSavingsAccountId");
            final String toSavingsAccountNo = rs.getString("toSavingsAccountNo");
            final Long toLoanAccountId = JdbcSupport.getLong(rs, "toLoanAccountId");
            final String toLoanAccountNo = rs.getString("toLoanAccountNo");

            if (toSavingsAccountId != null) {
                toAccount = PortfolioAccountData.lookup(toSavingsAccountId, toSavingsAccountNo);
                toAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);
            } else if (toLoanAccountId != null) {
                toAccount = PortfolioAccountData.lookup(toLoanAccountId, toLoanAccountNo);
                toAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
            }

            return AccountTransferData.instance(id, reversed, transferDate, currency, transferAmount, transferDescription, fromOffice,
                    toOffice, fromClient, toClient, fromAccountType, fromAccount, toAccountType, toAccount);
        }
    }

}
