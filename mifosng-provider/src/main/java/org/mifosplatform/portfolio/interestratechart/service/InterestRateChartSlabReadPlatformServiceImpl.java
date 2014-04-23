/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabData;
import org.mifosplatform.portfolio.interestratechart.exception.InterestRateChartSlabNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class InterestRateChartSlabReadPlatformServiceImpl implements InterestRateChartSlabReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final InterestRateChartSlabsMapper chartSlabRowMapper = new InterestRateChartSlabsMapper();
    private final InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService;

    @Autowired
    public InterestRateChartSlabReadPlatformServiceImpl(PlatformSecurityContext context, final RoutingDataSource dataSource,
            InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.chartDropdownReadPlatformService = chartDropdownReadPlatformService;
    }

    @Override
    public Collection<InterestRateChartSlabData> retrieveAll(Long chartId) {
        this.context.authenticatedUser();
        final String sql = "select " + this.chartSlabRowMapper.schema() + " where ircd.interest_rate_chart_id = ? order by ircd.id";
        return this.jdbcTemplate.query(sql, this.chartSlabRowMapper, new Object[] { chartId });
    }

    @Override
    public InterestRateChartSlabData retrieveOne(Long chartId, Long chartSlabId) {
        try {
            this.context.authenticatedUser();
            final String sql = "select " + this.chartSlabRowMapper.schema() + " where ircd.interest_rate_chart_id = ? and ircd.id = ?";
            return this.jdbcTemplate.queryForObject(sql, this.chartSlabRowMapper, new Object[] { chartId, chartSlabId });
        } catch (final EmptyResultDataAccessException e) {
            throw new InterestRateChartSlabNotFoundException(chartSlabId, chartId);
        }
    }
    
    @Override
    public InterestRateChartSlabData retrieveWithTemplate(InterestRateChartSlabData chartSlab) {
        return InterestRateChartSlabData.withTemplate(chartSlab, this.chartDropdownReadPlatformService.retrievePeriodTypeOptions());
    }

    @Override
    public InterestRateChartSlabData retrieveTemplate() {
        return InterestRateChartSlabData.template(this.chartDropdownReadPlatformService.retrievePeriodTypeOptions());
    }

    private static final class InterestRateChartSlabsMapper implements RowMapper<InterestRateChartSlabData> {

        private final String schemaSql;

        public String schema() {
            return this.schemaSql;
        }

        private InterestRateChartSlabsMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder
                    .append("ircd.id as ircdId, ircd.description as ircdDescription, ircd.period_type_enum ircdPeriodTypeId, ")
                    .append("ircd.from_period as ircdFromPeriod, ircd.to_period as ircdToPeriod, ircd.amount_range_from as ircdAmountRangeFrom, ")
                    .append("ircd.amount_range_to as ircdAmountRangeTo, ircd.annual_interest_rate as ircdAnnualInterestRate, ")
                    .append("ircd.interest_rate_for_female as ircdInterestRateForFemale, ircd.interest_rate_for_children as ircdInterestRateForChildren, ")
                    .append("ircd.interest_rate_for_senior_citizen as ircdInterestRateForSeniorCitizen, ")
                    .append("curr.code as currencyCode, curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append("curr.display_symbol as currencyDisplaySymbol, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf ")
                    .append("from ").append("m_interest_rate_slab ircd ")
                    .append("left join m_currency curr on ircd.currency_code= curr.code ");
            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public InterestRateChartSlabData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLongDefaultToNullIfZero(rs, "ircdId");
            // If there are not chart Slabs are associated then in
            // InterestRateChartExtractor the chart Slabs id will be null.
            if (id == null) { return null; }

            final String description = rs.getString("ircdDescription");
            final Integer fromPeriod = JdbcSupport.getInteger(rs, "ircdFromPeriod");
            final Integer toPeriod = JdbcSupport.getInteger(rs, "ircdToPeriod");
            final Integer periodTypeId = JdbcSupport.getInteger(rs, "ircdPeriodTypeId");
            final EnumOptionData periodType = InterestRateChartEnumerations.periodType(periodTypeId);
            final BigDecimal amountRangeFrom = rs.getBigDecimal("ircdAmountRangeFrom");
            final BigDecimal amountRangeTo = rs.getBigDecimal("ircdAmountRangeTo");
            final BigDecimal annualInterestRate = rs.getBigDecimal("ircdAnnualInterestRate");
            final BigDecimal interestRateForFemale = rs.getBigDecimal("ircdInterestRateForFemale");
            final BigDecimal interestRateForChildren = rs.getBigDecimal("ircdInterestRateForChildren");
            final BigDecimal interestRateForSeniorCitizen = rs.getBigDecimal("ircdInterestRateForSeniorCitizen");

            // currency Slabs
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            // currency
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return InterestRateChartSlabData.instance(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                    annualInterestRate, interestRateForFemale, interestRateForChildren, interestRateForSeniorCitizen, currency);
        }

    }
}