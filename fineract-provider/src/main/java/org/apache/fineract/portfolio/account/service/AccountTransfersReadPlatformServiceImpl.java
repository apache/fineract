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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.account.mapper.AccountTransfersMapper;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
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
    private final SqlValidator sqlValidator;

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
                    fromAccount.getCurrencyCodeFromCurrency());
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

        final String currencyCode = excludeThisAccountFromOptions != null ? excludeThisAccountFromOptions.getCurrencyCodeFromCurrency()
                : null;

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
        Stream.of(searchParameters.getOrderBy(), searchParameters.getSortOrder(), searchParameters.getExternalId())
                .forEach(sqlValidator::validate);
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
                    fromAccount.getCurrencyCodeFromCurrency());
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
}
