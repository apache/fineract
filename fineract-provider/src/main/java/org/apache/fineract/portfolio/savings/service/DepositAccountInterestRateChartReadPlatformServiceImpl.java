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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import org.apache.fineract.portfolio.interestratechart.service.InterestIncentiveDropdownReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestIncentivesEnumerations;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartDropdownReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartEnumerations;
import org.apache.fineract.portfolio.savings.data.DepositAccountInterestIncentiveData;
import org.apache.fineract.portfolio.savings.data.DepositAccountInterestRateChartData;
import org.apache.fineract.portfolio.savings.data.DepositAccountInterestRateChartSlabData;
import org.apache.fineract.portfolio.savings.exception.DepositAccountInterestRateChartNotFoundException;
import org.joda.time.LocalDate;
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
    private final InterestIncentiveDropdownReadPlatformService interestIncentiveDropdownReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public DepositAccountInterestRateChartReadPlatformServiceImpl(PlatformSecurityContext context, final RoutingDataSource dataSource,
            InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService,
            final InterestIncentiveDropdownReadPlatformService interestIncentiveDropdownReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.chartDropdownReadPlatformService = chartDropdownReadPlatformService;
        this.interestIncentiveDropdownReadPlatformService = interestIncentiveDropdownReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
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
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append(this.chartExtractor.schema());
        sql.append(" where irc.id = ? order by irc.id asc, ");
        sql.append("CASE ");
        sql.append("WHEN isPrimaryGroupingByAmount then ircd.amount_range_from ");
        sql.append("WHEN isPrimaryGroupingByAmount then ircd.amount_range_to ");
        sql.append("END,");
        sql.append("ircd.from_period, ircd.to_period,");
        sql.append("CASE ");
        sql.append("WHEN !isPrimaryGroupingByAmount then ircd.amount_range_from ");
        sql.append("WHEN !isPrimaryGroupingByAmount then ircd.amount_range_to ");
        sql.append("END");
        Collection<DepositAccountInterestRateChartData> chartDatas = this.jdbcTemplate.query(sql.toString(), this.chartExtractor,
                new Object[] { chartId });
        if (chartDatas == null || chartDatas.isEmpty()) { throw new DepositAccountInterestRateChartNotFoundException(chartId); }

        return chartDatas.iterator().next();
    }

    @Override
    public DepositAccountInterestRateChartData retrieveWithTemplate(DepositAccountInterestRateChartData chartData) {

        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));

        return DepositAccountInterestRateChartData.withTemplate(chartData,
                this.chartDropdownReadPlatformService.retrievePeriodTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveEntityTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveAttributeNameOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveConditionTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveIncentiveTypeOptions(), genderOptions, clientTypeOptions,
                clientClassificationOptions);
    }

    @Override
    public DepositAccountInterestRateChartData retrieveOneWithSlabsOnAccountId(Long accountId) {
        this.context.authenticatedUser();
        StringBuilder sql = new StringBuilder();
        sql.append("select ");
        sql.append(this.chartExtractor.schema());
        sql.append(" where irc.savings_account_id = ? order by irc.id asc, ");
        sql.append("CASE ");
        sql.append("WHEN isPrimaryGroupingByAmount then ircd.amount_range_from ");
        sql.append("WHEN isPrimaryGroupingByAmount then ircd.amount_range_to ");
        sql.append("END,");
        sql.append("ircd.from_period, ircd.to_period,");
        sql.append("CASE ");
        sql.append("WHEN !isPrimaryGroupingByAmount then ircd.amount_range_from ");
        sql.append("WHEN !isPrimaryGroupingByAmount then ircd.amount_range_to ");
        sql.append("END");

        Collection<DepositAccountInterestRateChartData> chartDatas = this.jdbcTemplate.query(sql.toString(), this.chartExtractor,
                new Object[] { accountId });
        if (chartDatas == null || chartDatas.isEmpty()) { throw new DepositAccountInterestRateChartNotFoundException(accountId); }

        return chartDatas.iterator().next();
    }

    @Override
    public DepositAccountInterestRateChartData template() {

        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));
        return DepositAccountInterestRateChartData.template(this.chartDropdownReadPlatformService.retrievePeriodTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveEntityTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveAttributeNameOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveConditionTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveIncentiveTypeOptions(), genderOptions, clientTypeOptions,
                clientClassificationOptions);
    }

    private static final class DepositAccountInterestRateChartExtractor implements
            ResultSetExtractor<Collection<DepositAccountInterestRateChartData>> {

        DepositAccountInterestRateChartMapper chartMapper = new DepositAccountInterestRateChartMapper();
        InterestRateChartSlabExtractor chartSlabsMapper = new InterestRateChartSlabExtractor();

        private final String schemaSql;

        public String schema() {
            return this.schemaSql;
        }

        private DepositAccountInterestRateChartExtractor() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder
                    .append("irc.id as ircId, irc.name as ircName, irc.description as ircDescription,")
                    .append("irc.from_date as ircFromDate, irc.end_date as ircEndDate, ")
                    .append("irc.is_primary_grouping_by_amount as isPrimaryGroupingByAmount,")
                    .append("ircd.id as ircdId, ircd.description as ircdDescription, ircd.period_type_enum ircdPeriodTypeId, ")
                    .append("ircd.from_period as ircdFromPeriod, ircd.to_period as ircdToPeriod, ircd.amount_range_from as ircdAmountRangeFrom, ")
                    .append("ircd.amount_range_to as ircdAmountRangeTo, ircd.annual_interest_rate as ircdAnnualInterestRate, ")
                    .append("curr.code as currencyCode, curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append("curr.display_symbol as currencyDisplaySymbol, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf, ")
                    .append("sa.id as accountId, sa.account_no as accountNumber, ")
                    .append("iri.id as iriId, ")
                    .append(" iri.entiry_type as entityType, iri.attribute_name as attributeName ,")
                    .append(" iri.condition_type as conditionType, iri.attribute_value as attributeValue, ")
                    .append(" iri.incentive_type as incentiveType, iri.amount as amount, ")
                    .append("code.code_value as attributeValueDesc ")
                    .append("from ")
                    .append("m_savings_account_interest_rate_chart irc left join m_savings_account_interest_rate_slab ircd on irc.id=ircd.savings_account_interest_rate_chart_id ")
                    .append(" left join m_savings_interest_incentives  iri on iri.deposit_account_interest_rate_slab_id =ircd.id ")
                    .append(" left join m_code_value code on code.id = iri.attribute_value ")
                    .append("left join m_currency curr on ircd.currency_code= curr.code ")
                    .append("left join m_savings_account sa on irc.savings_account_id=sa.id ");

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public Collection<DepositAccountInterestRateChartData> extractData(ResultSet rs) throws SQLException, DataAccessException {

            List<DepositAccountInterestRateChartData> chartDataList = new ArrayList<>();

            DepositAccountInterestRateChartData chartData = null;
            Long interestRateChartId = null;
            int ircIndex = 0;// Interest rate chart index

            while (rs.next()) {
                Long tempIrcId = rs.getLong("ircId");
                // first row or when interest rate chart id changes
                if (chartData == null || (interestRateChartId != null && !interestRateChartId.equals(tempIrcId))) {

                    interestRateChartId = tempIrcId;
                    chartData = chartMapper.mapRow(rs, ircIndex++);
                    chartDataList.add(chartData);

                }
                final DepositAccountInterestRateChartSlabData chartSlabsData = chartSlabsMapper.extractData(rs);
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
                    .append("irc.is_primary_grouping_by_amount as isPrimaryGroupingByAmount,")
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
            final boolean isPrimaryGroupingByAmount = rs.getBoolean("isPrimaryGroupingByAmount");
            final Long accountId = rs.getLong("accountId");
            final String accountNumber = rs.getString("accountNumber");
            final Collection<EnumOptionData> periodTypes = InterestRateChartEnumerations.periodType(PeriodFrequencyType.values());

            return DepositAccountInterestRateChartData.instance(id, name, description, fromDate, endDate, isPrimaryGroupingByAmount,
                    accountId, accountNumber, null, periodTypes);
        }

    }

    private static final class InterestRateChartSlabExtractor implements ResultSetExtractor<DepositAccountInterestRateChartSlabData> {

        DepositAccountInterestRateChartSlabsMapper chartSlabsMapper = new DepositAccountInterestRateChartSlabsMapper();
        InterestIncentiveMapper incentiveMapper = new InterestIncentiveMapper();

        @Override
        public DepositAccountInterestRateChartSlabData extractData(ResultSet rs) throws SQLException, DataAccessException {

            DepositAccountInterestRateChartSlabData chartSlabData = null;
            Long interestRateChartSlabId = null;
            int ircIndex = 0;// Interest rate chart index
            int ircdIndex = 0;// Interest rate chart Slabs index
            rs.previous();
            while (rs.next()) {
                Long tempIrcdId = rs.getLong("ircdId");
                if (interestRateChartSlabId == null || interestRateChartSlabId.equals(tempIrcdId)) {
                    if (chartSlabData == null) {
                        interestRateChartSlabId = tempIrcdId;
                        chartSlabData = chartSlabsMapper.mapRow(rs, ircIndex++);
                    }
                    final DepositAccountInterestIncentiveData incentiveData = incentiveMapper.mapRow(rs, ircdIndex++);
                    if (incentiveData != null) {
                        chartSlabData.addIncentives(incentiveData);
                    }
                } else {
                    rs.previous();
                    break;
                }

            }
            return chartSlabData;
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
                    amountRangeTo, annualInterestRate, currency);
        }

    }

    private static final class InterestIncentiveMapper implements RowMapper<DepositAccountInterestIncentiveData> {

        @Override
        public DepositAccountInterestIncentiveData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLongDefaultToNullIfZero(rs, "iriId");
            // If there are not Incentive are associated then in
            // InterestRateChartExtractor the incentive id will be null.
            if (id == null) { return null; }

            final String attributeValue = rs.getString("attributeValue");
            String attributeValueDesc = null;
            final Integer entityType = JdbcSupport.getInteger(rs, "entityType");
            final EnumOptionData entityTypeData = InterestIncentivesEnumerations.entityType(entityType);

            final Integer attributeName = JdbcSupport.getInteger(rs, "attributeName");
            if (InterestIncentiveAttributeName.isCodeValueAttribute(InterestIncentiveAttributeName.fromInt(attributeName))) {
                attributeValueDesc = rs.getString("attributeValueDesc");
            }
            final EnumOptionData attributeNameData = InterestIncentivesEnumerations.attributeName(attributeName);
            final Integer conditionType = JdbcSupport.getInteger(rs, "conditionType");
            final EnumOptionData conditionTypeData = CommonEnumerations.conditionType(conditionType, "incentive");
            final Integer incentiveType = JdbcSupport.getInteger(rs, "incentiveType");
            final EnumOptionData incentiveTypeData = InterestIncentivesEnumerations.incentiveType(incentiveType);
            final BigDecimal amount = rs.getBigDecimal("amount");

            return DepositAccountInterestIncentiveData.instance(id, entityTypeData, attributeNameData, conditionTypeData, attributeValue,
                    attributeValueDesc, incentiveTypeData, amount);

        }

    }
}