/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.savings.data.SavingsAccountData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountSummaryData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionData;
import org.mifosplatform.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
import org.mifosplatform.portfolio.savings.domain.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.domain.SavingsInterestPostingPeriodType;
import org.mifosplatform.portfolio.savings.domain.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingsAccountReadPlatformServiceImpl implements SavingsAccountReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final SavingsDropdownReadPlatformService dropdownReadPlatformService;
    private final SavingsAccountTransactionTemplateMapper transactionTemplateMapper;
    private final SavingsAccountTransactionsMapper transactionsMapper;

    @Autowired
    public SavingsAccountReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final SavingsProductReadPlatformService savingProductReadPlatformService,
            final SavingsDropdownReadPlatformService dropdownReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.savingsProductReadPlatformService = savingProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        transactionTemplateMapper = new SavingsAccountTransactionTemplateMapper();
        transactionsMapper = new SavingsAccountTransactionsMapper();
    }

    @Override
    public Collection<SavingsAccountData> retrieveAll() {

        this.context.authenticatedUser();
        final SavingAccountMapper mapper = new SavingAccountMapper();
        final String sql = "select " + mapper.schema();
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public SavingsAccountData retrieveOne(final Long accountId) {

        try {
            this.context.authenticatedUser();

            final SavingAccountMapper mapper = new SavingAccountMapper();
            final String sql = "select " + mapper.schema() + " where sa.id = ?";

            return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { accountId });
        } catch (EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }

    private static final class SavingAccountMapper implements RowMapper<SavingsAccountData> {

        private final String schemaSql;

        public SavingAccountMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId, ");
            sqlBuilder.append("sa.status_enum as statusEnum, sa.activation_date as activationDate, ");
            sqlBuilder.append("c.id as clientId, c.display_name as clientName, ");
            sqlBuilder.append("g.id as groupId, g.name as groupName, ");
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sa.nominal_annual_interest_rate as nominalAnnualInterestRate, ");
            sqlBuilder.append("sa.interest_compounding_period_enum as interestCompoundingPeriodType, ");
            sqlBuilder.append("sa.interest_posting_period_enum as interestPostingPeriodType, ");
            sqlBuilder.append("sa.interest_calculation_type_enum as interestCalculationType, ");
            sqlBuilder.append("sa.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
            sqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance, ");
            sqlBuilder.append("sa.lockin_period_frequency as lockinPeriodFrequency,");
            sqlBuilder.append("sa.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
            sqlBuilder.append("sa.total_deposits_derived as totalDeposits, ");
            sqlBuilder.append("sa.total_withdrawals_derived as totalWithdrawals, ");
            sqlBuilder.append("sa.total_interest_earned_derived as totalInterestEarned, ");
            sqlBuilder.append("sa.total_interest_posted_derived as totalInterestPosted, ");
            sqlBuilder.append("sa.account_balance_derived as accountBalance ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("left outer join m_client c ON c.id = sa.client_id ");
            sqlBuilder.append("left outer join m_group g ON g.id = sa.group_id ");
            sqlBuilder.append("join m_savings_product sp ON sa.product_id = sp.id ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusEnum);

            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            final BigDecimal nominalAnnualInterestRate = rs.getBigDecimal("nominalAnnualInterestRate");

            final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations
                    .compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCompoundingPeriodType")));

            final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(SavingsInterestPostingPeriodType
                    .fromInt(JdbcSupport.getInteger(rs, "interestPostingPeriodType")));

            final EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType
                    .fromInt(JdbcSupport.getInteger(rs, "interestCalculationType")));

            final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations
                    .interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCalculationDaysInYearType")));

            final BigDecimal minRequiredOpeningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredOpeningBalance");

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                final SavingsPeriodFrequencyType lockinPeriodType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
                lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodType);
            }

            final BigDecimal totalDeposits = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalDeposits");
            final BigDecimal totalWithdrawals = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithdrawals");
            final BigDecimal totalInterestEarned = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalInterestEarned");
            final BigDecimal totalInterestPosted = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalInterestPosted");
            final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "accountBalance");

            final SavingsAccountSummaryData summary = new SavingsAccountSummaryData(currency, totalDeposits, totalWithdrawals,
                    totalInterestEarned, totalInterestPosted, accountBalance);

            return SavingsAccountData.instance(id, accountNo, externalId, status, activationDate, groupId, groupName, clientId, clientName,
                    productId, productName, currency, nominalAnnualInterestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                    interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                    lockinPeriodFrequencyType, summary);
        }
    }

    @Override
    public SavingsAccountData retrieveTemplate(final Long clientId, final Long groupId, final Long productId) {

        context.authenticatedUser();

        ClientData client = null;
        if (clientId != null) {
            client = this.clientReadPlatformService.retrieveOne(clientId);
        }

        GroupGeneralData group = null;
        if (groupId != null) {
            group = this.groupReadPlatformService.retrieveOne(groupId);
        }

        final Collection<SavingsProductData> productOptions = this.savingsProductReadPlatformService.retrieveAllForLookup();
        SavingsAccountData template = null;
        if (productId != null) {

            SavingAccountTemplateMapper mapper = new SavingAccountTemplateMapper(client, group);

            final String sql = "select " + mapper.schema() + " where sp.id = ?";
            template = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { productId });

            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = this.dropdownReadPlatformService
                    .retrieveCompoundingInterestPeriodTypeOptions();

            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = this.dropdownReadPlatformService
                    .retrieveInterestPostingPeriodTypeOptions();

            final Collection<EnumOptionData> interestCalculationTypeOptions = this.dropdownReadPlatformService
                    .retrieveInterestCalculationTypeOptions();

            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = this.dropdownReadPlatformService
                    .retrieveInterestCalculationDaysInYearTypeOptions();

            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.dropdownReadPlatformService
                    .retrieveLockinPeriodFrequencyTypeOptions();
            final Collection<SavingsAccountTransactionData> transactions = null;

            template = SavingsAccountData.withTemplateOptions(template, productOptions, interestCompoundingPeriodTypeOptions,
                    interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                    lockinPeriodFrequencyTypeOptions, transactions);
        } else {

            String clientName = null;
            if (client != null) {
                clientName = client.displayName();
            }

            String groupName = null;
            if (group != null) {
                groupName = group.getName();
            }

            template = SavingsAccountData.withClientTemplate(clientId, clientName, groupId, groupName);

            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
            final Collection<SavingsAccountTransactionData> transactions = null;

            template = SavingsAccountData.withTemplateOptions(template, productOptions, interestCompoundingPeriodTypeOptions,
                    interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                    lockinPeriodFrequencyTypeOptions, transactions);
        }

        return template;
    }

    @Override
    public SavingsAccountTransactionData retrieveDepositTransactionTemplate(final Long savingsId) {

        try {
            this.context.authenticatedUser();

            final String sql = "select " + transactionTemplateMapper.schema() + " where sa.id = ?";

            return this.jdbcTemplate.queryForObject(sql, transactionTemplateMapper, new Object[] { savingsId });
        } catch (EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(savingsId);
        }
    }

    @Override
    public Collection<SavingsAccountTransactionData> retrieveAllTransactions(final Long savingsId) {

        final String sql = "select " + this.transactionsMapper.schema() + " where sa.id = ?";

        return this.jdbcTemplate.query(sql, this.transactionsMapper, new Object[] { savingsId });
    }

    private static final class SavingsAccountTransactionsMapper implements RowMapper<SavingsAccountTransactionData> {

        private final String schemaSql;

        public SavingsAccountTransactionsMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("tr.id as transactionId, tr.transaction_type_enum as transactionType, ");
            sqlBuilder.append("tr.transaction_date as transactionDate, tr.amount as transactionAmount,");
            sqlBuilder.append("sa.id as savingsId, sa.account_no as accountNo, ");
            sqlBuilder.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_savings_account_transaction tr on tr.savings_account_id = sa.id ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("transactionId");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");

            final Long savingsId = rs.getLong("savingsId");
            final String accountNo = rs.getString("accountNo");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            return SavingsAccountTransactionData.create(id, transactionType, savingsId, accountNo, date, currency, amount);
        }
    }

    private static final class SavingsAccountTransactionTemplateMapper implements RowMapper<SavingsAccountTransactionData> {

        private final String schemaSql;

        public SavingsAccountTransactionTemplateMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
            sqlBuilder.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long savingsId = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            return SavingsAccountTransactionData.template(savingsId, accountNo, DateUtils.getLocalDateOfTenant(), currency);
        }
    }

    private static final class SavingAccountTemplateMapper implements RowMapper<SavingsAccountData> {

        private final ClientData client;
        private final GroupGeneralData group;

        private final String schemaSql;

        public SavingAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
            this.client = client;
            this.group = group;

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append("sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sp.nominal_annual_interest_rate as nominalAnnualIterestRate, ");
            sqlBuilder.append("sp.interest_compounding_period_enum as interestCompoundingPeriodType, ");
            sqlBuilder.append("sp.interest_posting_period_enum as interestPostingPeriodType, ");
            sqlBuilder.append("sp.interest_calculation_type_enum as interestCalculationType, ");
            sqlBuilder.append("sp.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
            sqlBuilder.append("sp.min_required_opening_balance as minRequiredOpeningBalance, ");
            sqlBuilder.append("sp.lockin_period_frequency as lockinPeriodFrequency, ");
            sqlBuilder.append("sp.lockin_period_frequency_enum as lockinPeriodFrequencyType ");
            sqlBuilder.append("from m_savings_product sp ");
            sqlBuilder.append("join m_currency curr on curr.code = sp.currency_code ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);

            final BigDecimal nominalAnnualIterestRate = rs.getBigDecimal("nominalAnnualIterestRate");

            EnumOptionData interestCompoundingPeriodType = SavingsEnumerations
                    .compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCompoundingPeriodType")));

            final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(SavingsInterestPostingPeriodType
                    .fromInt(JdbcSupport.getInteger(rs, "interestPostingPeriodType")));

            EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType
                    .fromInt(JdbcSupport.getInteger(rs, "interestCalculationType")));

            EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations
                    .interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCalculationDaysInYearType")));

            final BigDecimal minRequiredOpeningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredOpeningBalance");

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                final SavingsPeriodFrequencyType lockinPeriodType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
                lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodType);
            }

            Long clientId = null;
            String clientName = null;
            if (client != null) {
                clientId = client.id();
                clientName = client.displayName();
            }

            Long groupId = null;
            String groupName = null;
            if (group != null) {
                groupId = group.getId();
                groupName = group.getName();
            }

            final SavingsAccountStatusEnumData status = null;
            final LocalDate activationDate = null;
            final SavingsAccountSummaryData summary = null;
            return SavingsAccountData.instance(null, null, null, status, activationDate, groupId, groupName, clientId, clientName,
                    productId, productName, currency, nominalAnnualIterestRate, interestCompoundingPeriodType, interestPostingPeriodType,
                    interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency,
                    lockinPeriodFrequencyType, summary);
        }
    }
}