/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartDropdownReadPlatformService;
import org.mifosplatform.portfolio.interestratechart.service.InterestRateChartEnumerations;
import org.mifosplatform.portfolio.savings.data.DepositAccountInterestRateChartData;
import org.mifosplatform.portfolio.savings.data.DepositAccountInterestRateChartSlabData;
import org.mifosplatform.portfolio.savings.exception.DepositAccountInterestRateChartNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DepositAccountInterestRateChartReadPlatformServiceImpl implements DepositAccountInterestRateChartReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final DepositAccountInterestRateChartMapper chartRowMapper = new DepositAccountInterestRateChartMapper();
    private final DepositAccountInterestRateChartExtractor chartExtractor = new DepositAccountInterestRateChartExtractor();
    private final InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService;

    @Autowired
    public DepositAccountInterestRateChartReadPlatformServiceImpl(PlatformSecurityContext context, final RoutingDataSource dataSource,
            InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.chartDropdownReadPlatformService = chartDropdownReadPlatformService;
    }

    @Override
    public DepositAccountInterestRateChartData retrieveOne(Long chartId) {
        try {
            this.context.authenticatedUser();
            final String sql = "select " + this.chartRowMapper.schema() + " where irc.id = ? ";
            return this.jdbcTemplate.queryForObject(sql, this.chartRowMapper, new Object[] { chartId });
        } catch (final EmptyResultDataAccessException e) {
            throw new DepositAccountInterestRateChartNotFoundException(chartId);
        }
    }

    @Override
    public DepositAccountInterestRateChartData retrieveOneWithSlabs(Long chartId) {
        this.context.authenticatedUser();
        final String sql = "select " + this.chartExtractor.schema() + " where irc.id = ? order by ircd.id asc";
        Collection<DepositAccountInterestRateChartData> chartDatas = this.jdbcTemplate.query(sql, this.chartExtractor,
                new Object[] { chartId });
        if (chartDatas == null || chartDatas.isEmpty()) { throw new DepositAccountInterestRateChartNotFoundException(chartId); }

        return chartDatas.iterator().next();
    }

    @Override
    public DepositAccountInterestRateChartData retrieveWithTemplate(DepositAccountInterestRateChartData chartData) {
        return DepositAccountInterestRateChartData.withTemplate(chartData,
                this.chartDropdownReadPlatformService.retrievePeriodTypeOptions());
    }

    @Override
    public DepositAccountInterestRateChartData retrieveOneWithSlabsOnAccountId(Long accountId) {
        this.context.authenticatedUser();
        final String sql = "select " + this.chartExtractor.schema() + " where irc.savings_account_id = ? order by ircd.id asc";
        Collection<DepositAccountInterestRateChartData> chartDatas = this.jdbcTemplate.query(sql, this.chartExtractor,
                new Object[] { accountId });
        if (chartDatas == null || chartDatas.isEmpty()) { throw new DepositAccountInterestRateChartNotFoundException(accountId); }

        return chartDatas.iterator().next();
    }

    @Override
    public DepositAccountInterestRateChartData template() {
        return DepositAccountInterestRateChartData.template(this.chartDropdownReadPlatformService.retrievePeriodTypeOptions());
    }

    private static final class DepositAccountInterestRateChartExtractor implements
            ResultSetExtractor<Collection<DepositAccountInterestRateChartData>> {

        DepositAccountInterestRateChartMapper chartMapper = new DepositAccountInterestRateChartMapper();
        DepositAccountInterestRateChartSlabsMapper chartSlabsMapper = new DepositAccountInterestRateChartSlabsMapper();

        private final String schemaSql;

        public String schema() {
            return this.schemaSql;
        }

        private DepositAccountInterestRateChartExtractor() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder
                    .append("irc.id as ircId, irc.name as ircName, irc.description as ircDescription,")
                    .append("irc.from_date as ircFromDate, irc.end_date as ircEndDate, ")
                    .append("ircd.id as ircdId, ircd.description as ircdDescription, ircd.period_type_enum ircdPeriodTypeId, ")
                    .append("ircd.from_period as ircdFromPeriod, ircd.to_period as ircdToPeriod, ircd.amount_range_from as ircdAmountRangeFrom, ")
                    .append("ircd.amount_range_to as ircdAmountRangeTo, ircd.annual_interest_rate as ircdAnnualInterestRate, ")
                    .append("ircd.interest_rate_for_female as ircdInterestRateForFemale, ircd.interest_rate_for_children as ircdInterestRateForChildren, ")
                    .append("ircd.interest_rate_for_senior_citizen as ircdInterestRateForSeniorCitizen, ")
                    .append("curr.code as currencyCode, curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append("curr.display_symbol as currencyDisplaySymbol, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf, ")
                    .append("sa.id as accountId, sa.account_no as accountNumber ")
                    .append("from ")
                    .append("m_savings_account_interest_rate_chart irc left join m_savings_account_interest_rate_slab ircd on irc.id=ircd.savings_account_interest_rate_chart_id ")
                    .append("left join m_currency curr on ircd.currency_code= curr.code ")
                    .append("left join m_savings_account sa on irc.savings_account_id=sa.id ");

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public Collection<DepositAccountInterestRateChartData> extractData(ResultSet rs) throws SQLException, DataAccessException {

            List<DepositAccountInterestRateChartData> chartDataList = new ArrayList<DepositAccountInterestRateChartData>();

            DepositAccountInterestRateChartData chartData = null;
            Long interestRateChartId = null;
            int ircIndex = 0;// Interest rate chart index
            int ircdIndex = 0;// Interest rate chart Slabs index

            while (rs.next()) {
                Long tempIrcId = rs.getLong("ircId");
                // first row or when interest rate chart id changes
                if (chartData == null || (interestRateChartId != null && !interestRateChartId.equals(tempIrcId))) {

                    interestRateChartId = tempIrcId;
                    chartData = chartMapper.mapRow(rs, ircIndex++);
                    chartDataList.add(chartData);
                    ircdIndex = 0;// reset index

                }
                final DepositAccountInterestRateChartSlabData chartSlabsData = chartSlabsMapper.mapRow(rs, ircdIndex++);
                if (chartSlabsData != null) {
                    chartData.addChartSlab(chartSlabsData);
                }
            }
            return chartDataList;
        }

    }

    public static final class DepositAccountInterestRateChartMapper implements RowMapper<DepositAccountInterestRateChartData> {

        private final String schemaSql;

        public String schema() {
            return this.schemaSql;
        }

        private DepositAccountInterestRateChartMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append("irc.id as ircId, irc.name as ircName, irc.description as ircDescription, ")
                    .append("irc.from_date as ircFromDate, irc.end_date as ircEndDate, ")
                    .append("sa.id as accountId, sa.account_no as accountNumber ").append("from ")
                    .append("m_savings_account_interest_rate_chart irc left join m_savings_account sa on irc.savings_account_id=sa.id ");
            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public DepositAccountInterestRateChartData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("ircId");
            final String name = rs.getString("ircName");
            final String description = rs.getString("ircDescription");
            final LocalDate fromDate = JdbcSupport.getLocalDate(rs, "ircFromDate");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "ircEndDate");
            final Long accountId = rs.getLong("accountId");
            final String accountNumber = rs.getString("accountNumber");
            final Collection<EnumOptionData> periodTypes = InterestRateChartEnumerations.periodType(PeriodFrequencyType.values());
            return DepositAccountInterestRateChartData.instance(id, name, description, fromDate, endDate, accountId, accountNumber, null,
                    periodTypes);
        }

    }

    private static final class DepositAccountInterestRateChartSlabsMapper implements RowMapper<DepositAccountInterestRateChartSlabData> {

        /*
         * private final String schemaSql;
         * 
         * public String schema() { return this.schemaSql; }
         * 
         * private DepositAccountInterestRateChartSlabsMapper() { final
         * StringBuilder sqlBuilder = new StringBuilder(400);
         * 
         * sqlBuilder .append(
         * "ircd.id as ircdId, ircd.description as ircdDescription, ircd.period_type_enum ircdPeriodTypeId, "
         * ) .append(
         * "ircd.from_period as ircdFromPeriod, ircd.to_period as ircdToPeriod, ircd.amount_range_from as ircdAmountRangeFrom, "
         * ) .append(
         * "ircd.amount_range_to as ircdAmountRangeTo, ircd.annual_interest_rate as ircdAnnualInterestRate, "
         * ) .append(
         * "curr.code as currencyCode, curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, "
         * ) .append(
         * "curr.display_symbol as currencyDisplaySymbol, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf "
         * ) .append("from ").append(
         * "m_savings_account_interest_rate_slab ircd ")
         * .append("left join m_currency curr on ircd.currency_code= curr.code "
         * ); this.schemaSql = sqlBuilder.toString(); }
         */

        @Override
        public DepositAccountInterestRateChartSlabData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
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

            return DepositAccountInterestRateChartSlabData.instance(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom,
                    amountRangeTo, annualInterestRate, interestRateForFemale, interestRateForChildren, interestRateForSeniorCitizen,
                    currency);
        }

    }
}