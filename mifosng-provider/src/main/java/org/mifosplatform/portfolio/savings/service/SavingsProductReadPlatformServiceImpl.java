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

import org.joda.time.MonthDay;
import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
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
    public SavingsProductReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
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

        private final String schemaSql;

        public SavingProductMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sp.id as id, sp.name as name, sp.description as description, ");
            sqlBuilder
                    .append("sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, sp.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sp.nominal_annual_interest_rate as nominalAnnualInterestRate, ");
            sqlBuilder.append("sp.interest_compounding_period_enum as compoundingInterestPeriodType, ");
            sqlBuilder.append("sp.interest_posting_period_enum as interestPostingPeriodType, ");
            sqlBuilder.append("sp.interest_calculation_type_enum as interestCalculationType, ");
            sqlBuilder.append("sp.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
            sqlBuilder.append("sp.min_required_opening_balance as minRequiredOpeningBalance, ");
            sqlBuilder.append("sp.lockin_period_frequency as lockinPeriodFrequency,");
            sqlBuilder.append("sp.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
            sqlBuilder.append("sp.withdrawal_fee_amount as withdrawalFeeAmount,");
            sqlBuilder.append("sp.withdrawal_fee_type_enum as withdrawalFeeTypeEnum, ");
            sqlBuilder.append("sp.annual_fee_amount as annualFeeAmount,");
            sqlBuilder.append("sp.annual_fee_on_month as annualFeeOnMonth, ");
            sqlBuilder.append("sp.annual_fee_on_day as annualFeeOnDay, ");
            sqlBuilder.append("sp.accounting_type as accountingType ");
            sqlBuilder.append("from m_savings_product sp ");
            sqlBuilder.append("join m_currency curr on curr.code = sp.currency_code ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
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
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);
            final BigDecimal nominalAnnualInterestRate = rs.getBigDecimal("nominalAnnualInterestRate");

            final Integer compoundingInterestPeriodTypeValue = JdbcSupport.getInteger(rs, "compoundingInterestPeriodType");
            EnumOptionData compoundingInterestPeriodType = SavingsEnumerations
                    .compoundingInterestPeriodType(compoundingInterestPeriodTypeValue);

            final Integer interestPostingPeriodTypeValue = JdbcSupport.getInteger(rs, "interestPostingPeriodType");
            EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(interestPostingPeriodTypeValue);

            final Integer interestCalculationTypeValue = JdbcSupport.getInteger(rs, "interestCalculationType");
            EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(interestCalculationTypeValue);

            EnumOptionData interestCalculationDaysInYearType = null;
            final Integer interestCalculationDaysInYearTypeValue = JdbcSupport.getInteger(rs, "interestCalculationDaysInYearType");
            if (interestCalculationDaysInYearTypeValue != null) {
                interestCalculationDaysInYearType = SavingsEnumerations
                        .interestCalculationDaysInYearType(interestCalculationDaysInYearTypeValue);
            }

            final Integer accountingRuleId = JdbcSupport.getInteger(rs, "accountingType");
            final EnumOptionData accountingRuleType = AccountingEnumerations.accountingRuleType(accountingRuleId);

            final BigDecimal minRequiredOpeningBalance = rs.getBigDecimal("minRequiredOpeningBalance");

            Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodFrequencyTypeValue);
            }

            final BigDecimal withdrawalFeeAmount = rs.getBigDecimal("withdrawalFeeAmount");

            EnumOptionData withdrawalFeeType = null;
            final Integer withdrawalFeeTypeValue = JdbcSupport.getInteger(rs, "withdrawalFeeTypeEnum");
            if (withdrawalFeeTypeValue != null) {
                withdrawalFeeType = SavingsEnumerations.withdrawalFeeType(withdrawalFeeTypeValue);
            }

            final BigDecimal annualFeeAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "annualFeeAmount");

            MonthDay annualFeeOnMonthDay = null;
            final Integer annualFeeOnMonth = JdbcSupport.getInteger(rs, "annualFeeOnMonth");
            final Integer annualFeeOnDay = JdbcSupport.getInteger(rs, "annualFeeOnDay");
            if (annualFeeAmount != null && annualFeeOnDay != null) {
                annualFeeOnMonthDay = new MonthDay(annualFeeOnMonth, annualFeeOnDay);
            }

            return SavingsProductData.instance(id, name, description, currency, nominalAnnualInterestRate, compoundingInterestPeriodType,
                    interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                    lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType, annualFeeAmount,
                    annualFeeOnMonthDay, accountingRuleType);
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