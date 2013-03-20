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
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
import org.mifosplatform.portfolio.savings.exception.SavingsProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SavingsProductReadPlatformServiceImpl implements SavingsProductReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final SavingProductMapper savingsProductRowMapper = new SavingProductMapper();
    private final SavingProductLookupMapper savingsProductLookupsRowMapper = new SavingProductLookupMapper();

    @Autowired
    public SavingsProductReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<SavingsProductData> retrieveAll() {

        this.context.authenticatedUser();

        final String sql = "select " + savingsProductRowMapper.schema();

        return this.jdbcTemplate.query(sql, savingsProductRowMapper);
    }

    @Override
    public Collection<SavingsProductData> retrieveAllForLookup() {

        final String sql = "select " + savingsProductLookupsRowMapper.schema();

        return this.jdbcTemplate.query(sql, savingsProductLookupsRowMapper);
    }

    @Override
    public SavingsProductData retrieveOne(final Long savingProductId) {
        try {
            this.context.authenticatedUser();
            final String sql = "select " + savingsProductRowMapper.schema() + " where sp.id = ?";
            return this.jdbcTemplate.queryForObject(sql, savingsProductRowMapper, new Object[] { savingProductId });
        } catch (EmptyResultDataAccessException e) {
            throw new SavingsProductNotFoundException(savingProductId);
        }
    }

    private static final class SavingProductMapper implements RowMapper<SavingsProductData> {

        public String schema() {
            return "sp.id as id,sp.name as name, sp.description as description, "
                    + "sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, "
                    + "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, curr.display_symbol as currencyDisplaySymbol, "
                    + "sp.nominal_interest_rate_per_period as interestRate, sp.nominal_interest_rate_period_frequency_enum as interestRatePeriodFrequencyType, "
                    + "sp.min_required_opening_balance as minRequiredOpeningBalance, "
                    + "sp.lockin_period_frequency as lockinPeriodFrequency, sp.lockin_period_frequency_enum as lockinPeriodFrequencyType " //
                    + "from m_savings_product sp " //
                    + "join m_currency curr on curr.code = sp.currency_code";
        }

        @Override
        public SavingsProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");
            String description = rs.getString("description");

            String currencyCode = rs.getString("currencyCode");
            String currencyName = rs.getString("currencyName");
            String currencyNameCode = rs.getString("currencyNameCode");
            String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, currencyDisplaySymbol,
                    currencyNameCode);
            BigDecimal interestRate = rs.getBigDecimal("interestRate");
            EnumOptionData interestRatePeriodFrequencyType = SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType
                    .fromInt(JdbcSupport.getInteger(rs, "interestRatePeriodFrequencyType")));

            final BigDecimal minRequiredOpeningBalance = rs.getBigDecimal("minRequiredOpeningBalance");

            Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsEnumerations.interestRatePeriodFrequencyType(PeriodFrequencyType
                        .fromInt(lockinPeriodFrequencyTypeValue));
            }

            return SavingsProductData.instance(id, name, description, currency, interestRate, interestRatePeriodFrequencyType,
                    minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType);
        }
    }

    private static final class SavingProductLookupMapper implements RowMapper<SavingsProductData> {

        public String schema() {
            return " sp.id as id, sp.name as name from m_savings_product sp";
        }

        @Override
        public SavingsProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            return SavingsProductData.lookup(id, name);
        }
    }
}