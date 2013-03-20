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

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savings.data.SavingsAccountData;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
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
            sqlBuilder.append("c.id as clientId, c.display_name as clientName, ");
            sqlBuilder.append("g.id as groupId, g.name as groupName, ");
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, ");
            sqlBuilder
                    .append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder
                    .append("sa.nominal_interest_rate_per_period as interestRate, sa.nominal_interest_rate_period_frequency_enum as interestRatePeriodFrequencyType, ");
            sqlBuilder.append("sa.annual_nominal_interest_rate as annualInterestRate, ");
            sqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance, ");
            sqlBuilder
                    .append("sa.lockin_period_frequency as lockinPeriodFrequency, sa.lockin_period_frequency_enum as lockinPeriodFrequencyType ");
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

            final Long groupId = rs.getLong("groupId");
            final String groupName = rs.getString("groupName");
            final Long clientId = rs.getLong("clientId");
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

            final BigDecimal interestRate = rs.getBigDecimal("interestRate");
            final EnumOptionData interestRatePeriodFrequencyType = SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType
                    .fromInt(JdbcSupport.getInteger(rs, "interestRatePeriodFrequencyType")));
            final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

            final BigDecimal minRequiredOpeningBalance = rs.getBigDecimal("minRequiredOpeningBalance");

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType
                        .fromInt(lockinPeriodFrequencyTypeValue));
            }

            return SavingsAccountData.instance(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                    currency, interestRate, interestRatePeriodFrequencyType, annualInterestRate, minRequiredOpeningBalance,
                    lockinPeriodFrequency, lockinPeriodFrequencyType);
        }
    }

    @Override
    public SavingsAccountData retrieveTemplate(final Long clientId, final Long groupId, final Long productId) {

        context.authenticatedUser();

        ClientData client = null;
        if (clientId != null) {
            client = this.clientReadPlatformService.retrieveIndividualClient(clientId);
        }

        GroupData group = null;
        if (groupId != null) {
            group = this.groupReadPlatformService.retrieveGroup(groupId);
        }

        final Collection<SavingsProductData> productOptions = this.savingsProductReadPlatformService.retrieveAllForLookup();
        SavingsAccountData template = null;
        if (productId != null) {

            SavingAccountTemplateMapper mapper = new SavingAccountTemplateMapper(client, group);

            final String sql = "select " + mapper.schema() + " where sp.id = ?";
            template = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { productId });

            final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions = this.dropdownReadPlatformService
                    .retrieveInterestRatePeriodFrequencyTypeOptions();
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.dropdownReadPlatformService
                    .retrieveLockinPeriodFrequencyTypeOptions();

            template = SavingsAccountData.withTemplateOptions(template, productOptions, interestRatePeriodFrequencyTypeOptions,
                    lockinPeriodFrequencyTypeOptions);
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

            final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions = null;
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
            template = SavingsAccountData.withTemplateOptions(template, productOptions, interestRatePeriodFrequencyTypeOptions,
                    lockinPeriodFrequencyTypeOptions);
        }

        return template;
    }

    private static final class SavingAccountTemplateMapper implements RowMapper<SavingsAccountData> {

        private final ClientData client;
        private final GroupData group;

        private final String schemaSql;

        public SavingAccountTemplateMapper(final ClientData client, final GroupData group) {
            this.client = client;
            this.group = group;

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append("sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sp.nominal_interest_rate_per_period as interestRate, ");
            sqlBuilder.append("sp.nominal_interest_rate_period_frequency_enum as interestRatePeriodFrequencyType, ");
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

            final BigDecimal interestRate = rs.getBigDecimal("interestRate");
            final EnumOptionData interestRatePeriodFrequencyType = SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType
                    .fromInt(JdbcSupport.getInteger(rs, "interestRatePeriodFrequencyType")));
            final BigDecimal annualInterestRate = null;

            final BigDecimal minRequiredOpeningBalance = rs.getBigDecimal("minRequiredOpeningBalance");

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType
                        .fromInt(lockinPeriodFrequencyTypeValue));
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

            return SavingsAccountData.instance(null, null, null, groupId, groupName, clientId, clientName, productId, productName,
                    currency, interestRate, interestRatePeriodFrequencyType, annualInterestRate, minRequiredOpeningBalance,
                    lockinPeriodFrequency, lockinPeriodFrequencyType);
        }
    }
}