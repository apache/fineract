/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.data.AccountTransferData;
import org.mifosplatform.portfolio.account.data.PortfolioAccountDTO;
import org.mifosplatform.portfolio.account.data.PortfolioAccountData;
import org.mifosplatform.portfolio.account.domain.AccountTransferType;
import org.mifosplatform.portfolio.account.exception.AccountTransferNotFoundException;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AccountTransfersReadPlatformServiceImpl implements AccountTransfersReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;

    // mapper
    private final AccountTransfersMapper accountTransfersMapper;

    // pagination
    private final PaginationHelper<AccountTransferData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public AccountTransfersReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformService;

        this.accountTransfersMapper = new AccountTransfersMapper();
    }

    @Override
    public AccountTransferData retrieveTemplate(final Long fromOfficeId, final Long fromClientId, final Long fromAccountId,
            final Integer fromAccountType, final Long toOfficeId, final Long toClientId, final Long toAccountId, final Integer toAccountType) {

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
            fromAccount = this.portfolioAccountReadPlatformService.retrieveOne(fromAccountId, accountType);

            // override provided fromClient with client of account
            mostRelevantFromClientId = fromAccount.clientId();
        }

        if (mostRelevantFromClientId != null) {
            fromClient = this.clientReadPlatformService.retrieveOne(mostRelevantFromClientId);
            mostRelevantFromOfficeId = fromClient.officeId();
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
        final LocalDate transferDate = DateUtils.getLocalDateOfTenant();
        Collection<OfficeData> toOfficeOptions = fromOfficeOptions;
        Collection<ClientData> toClientOptions = null;

        if (toAccountId != null && fromAccount != null) {
            toAccount = this.portfolioAccountReadPlatformService.retrieveOne(toAccountId, mostRelevantToAccountType,
                    fromAccount.currencyCode());
            mostRelevantToClientId = toAccount.clientId();
        }

        if (mostRelevantToClientId != null) {
            toClient = this.clientReadPlatformService.retrieveOne(mostRelevantToClientId);
            mostRelevantToOfficeId = toClient.officeId();

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

        final String currencyCode = excludeThisAccountFromOptions != null ? excludeThisAccountFromOptions.currencyCode() : null;

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
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.accountTransfersMapper.schema());
        Object[] finalObjectArray = {};
        if (accountDetailId != null) {
            sqlBuilder.append(" where att.account_transfer_details_id=?");
            finalObjectArray = new Object[] { accountDetailId };
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray,
                this.accountTransfersMapper);
    }

    @Override
    public AccountTransferData retrieveOne(final Long transferId) {

        try {
            final String sql = "select " + this.accountTransfersMapper.schema() + " where att.id = ?";

            return this.jdbcTemplate.queryForObject(sql, this.accountTransfersMapper, new Object[] { transferId });
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountTransferNotFoundException(transferId);
        }
    }

    @Override
    public Collection<Long> fetchPostInterestTransactionIds(final Long accountId) {
        final String sql = "select att.from_savings_transaction_id from m_account_transfer_transaction att inner join m_account_transfer_details atd on atd.id = att.account_transfer_details_id where atd.from_savings_account_id=? and att.is_reversed =0 and atd.transfer_type = ?";

        final List<Long> transactionId = this.jdbcTemplate.queryForList(sql, Long.class, accountId,
                AccountTransferType.INTEREST_TRANSFER.getValue());

        return transactionId;
    }

    private static final class AccountTransfersMapper implements RowMapper<AccountTransferData> {

        private final String schemaSql;

        public AccountTransfersMapper() {
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
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

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

    @Override
    public boolean isAccountTransfer(final Long transactionId, final PortfolioAccountType accountType) {
        final StringBuilder sql = new StringBuilder("select count(*) from m_account_transfer_transaction at where ");
        if (accountType.isLoanAccount()) {
            sql.append("at.from_loan_transaction_id=").append(transactionId).append(" or at.to_loan_transaction_id=").append(transactionId);
        } else {
            sql.append("at.from_savings_transaction_id=").append(transactionId).append(" or at.to_savings_transaction_id=")
                    .append(transactionId);
        }

        @SuppressWarnings("deprecation")
        final int count = this.jdbcTemplate.queryForInt(sql.toString());
        return count > 0;
    }

    @Override
    public Page<AccountTransferData> retrieveByStandingInstruction(final Long id, final SearchParameters searchParameters) {

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder
                .append(this.accountTransfersMapper.schema())
                .append(" join m_account_transfer_standing_instructions atsi on atsi.account_transfer_details_id = att.account_transfer_details_id ");
        sqlBuilder.append(" where atsi.id = ?");

        if (searchParameters != null) {
            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" limit ").append(searchParameters.getLimit());
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(" offset ").append(searchParameters.getOffset());
                }
            }
        }

        final Object[] finalObjectArray = { id };
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray,
                this.accountTransfersMapper);
    }

    @Override
    public AccountTransferData retrieveRefundByTransferTemplate(final Long fromOfficeId, final Long fromClientId, final Long fromAccountId,
            final Integer fromAccountType, final Long toOfficeId, final Long toClientId, final Long toAccountId, final Integer toAccountType) {
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
            mostRelevantFromClientId = fromAccount.clientId();
        }

        if (mostRelevantFromClientId != null) {
            fromClient = this.clientReadPlatformService.retrieveOne(mostRelevantFromClientId);
            mostRelevantFromOfficeId = fromClient.officeId();
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
        final LocalDate transferDate = DateUtils.getLocalDateOfTenant();
        Collection<OfficeData> toOfficeOptions = fromOfficeOptions;
        Collection<ClientData> toClientOptions = null;

        if (toAccountId != null && fromAccount != null) {
            toAccount = this.portfolioAccountReadPlatformService.retrieveOne(toAccountId, mostRelevantToAccountType,
                    fromAccount.currencyCode());
            mostRelevantToClientId = toAccount.clientId();
        }

        if (mostRelevantToClientId != null) {
            toClient = this.clientReadPlatformService.retrieveOne(mostRelevantToClientId);
            mostRelevantToOfficeId = toClient.officeId();

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

}