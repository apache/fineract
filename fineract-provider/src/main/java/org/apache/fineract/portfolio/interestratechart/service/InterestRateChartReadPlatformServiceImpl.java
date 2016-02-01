/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.mifosplatform.portfolio.common.service.CommonEnumerations;
import org.mifosplatform.portfolio.interestratechart.data.InterestIncentiveData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabData;
import org.mifosplatform.portfolio.interestratechart.exception.InterestRateChartNotFoundException;
import org.mifosplatform.portfolio.interestratechart.incentive.InterestIncentiveAttributeName;
import org.mifosplatform.portfolio.savings.data.DepositProductData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class InterestRateChartReadPlatformServiceImpl implements InterestRateChartReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final InterestRateChartMapper chartRowMapper = new InterestRateChartMapper();
    private final InterestRateChartExtractor chartExtractor = new InterestRateChartExtractor();
    private final InterestRateChartDropdownReadPlatformService chartDropdownReadPlatformService;
    private final InterestIncentiveDropdownReadPlatformService interestIncentiveDropdownReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public InterestRateChartReadPlatformServiceImpl(PlatformSecurityContext context, final RoutingDataSource dataSource,
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
    public InterestRateChartData retrieveOne(Long chartId) {
        try {
            this.context.authenticatedUser();
            final String sql = "select " + this.chartRowMapper.schema() + " where irc.id = ?";
            return this.jdbcTemplate.queryForObject(sql, this.chartRowMapper, new Object[] { chartId });
        } catch (final EmptyResultDataAccessException e) {
            throw new InterestRateChartNotFoundException(chartId);
        }
    }

    @Override
    public Collection<InterestRateChartData> retrieveAllWithSlabs(Long productId) {
        this.context.authenticatedUser();
        String sql = "select " + this.chartExtractor.schema() + " where sp.id = ? order by irc.id, ircd.id";
        return this.jdbcTemplate.query(sql, this.chartExtractor, new Object[] { productId });
    }

    @Override
    public Collection<InterestRateChartData> retrieveAllWithSlabsWithTemplate(Long productId) {
        Collection<InterestRateChartData> chartDatas = new ArrayList<>();

        for (InterestRateChartData chartData : retrieveAllWithSlabs(productId)) {
            chartDatas.add(retrieveWithTemplate(chartData));
        }

        return chartDatas;
    }

    @Override
    public InterestRateChartData retrieveActiveChartWithTemplate(Long productId) {
        Collection<InterestRateChartData> chartDatas = this.retrieveAllWithSlabsWithTemplate(productId);
        return DepositProductData.activeChart(chartDatas);
    }

    @Override
    public InterestRateChartData retrieveOneWithSlabs(Long chartId) {
        this.context.authenticatedUser();
        final String sql = "select " + this.chartExtractor.schema() + " where irc.id = ? order by ircd.id asc";
        Collection<InterestRateChartData> chartDatas = this.jdbcTemplate.query(sql, this.chartExtractor, new Object[] { chartId });
        if (chartDatas == null || chartDatas.isEmpty()) { throw new InterestRateChartNotFoundException(chartId); }

        return chartDatas.iterator().next();
    }

    @Override
    public InterestRateChartData retrieveWithTemplate(InterestRateChartData chartData) {

        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));
        return InterestRateChartData.withTemplate(chartData, this.chartDropdownReadPlatformService.retrievePeriodTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveEntityTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveAttributeNameOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveConditionTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveIncentiveTypeOptions(), genderOptions, clientTypeOptions,
                clientClassificationOptions);
    }

    @Override
    public InterestRateChartData retrieveOneWithSlabsOnProductId(@SuppressWarnings("unused") Long productId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InterestRateChartData template() {
        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));
        return InterestRateChartData.template(this.chartDropdownReadPlatformService.retrievePeriodTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveEntityTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveAttributeNameOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveConditionTypeOptions(),
                this.interestIncentiveDropdownReadPlatformService.retrieveIncentiveTypeOptions(), genderOptions, clientTypeOptions,
                clientClassificationOptions);
    }

    private static final class InterestRateChartExtractor implements ResultSetExtractor<Collection<InterestRateChartData>> {

        InterestRateChartMapper chartMapper = new InterestRateChartMapper();
        InterestRateChartSlabExtractor chartSlabsMapper = new InterestRateChartSlabExtractor();

        private final String schemaSql;

        public String schema() {
            return this.schemaSql;
        }

        private InterestRateChartExtractor() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder
                    .append("irc.id as ircId, irc.name as ircName, irc.description as ircDescription,")
                    .append("irc.from_date as ircFromDate, irc.end_date as ircEndDate, ")
                    .append("ircd.id as ircdId, ircd.description as ircdDescription, ircd.period_type_enum ircdPeriodTypeId, ")
                    .append("ircd.from_period as ircdFromPeriod, ircd.to_period as ircdToPeriod, ircd.amount_range_from as ircdAmountRangeFrom, ")
                    .append("ircd.amount_range_to as ircdAmountRangeTo, ircd.annual_interest_rate as ircdAnnualInterestRate, ")
                    .append("curr.code as currencyCode, curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append("curr.display_symbol as currencyDisplaySymbol, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf, ")
                    .append("sp.id as savingsProductId, sp.name as savingsProductName, ").append("iri.id as iriId, ")
                    .append(" iri.entiry_type as entityType, iri.attribute_name as attributeName ,")
                    .append(" iri.condition_type as conditionType, iri.attribute_value as attributeValue, ")
                    .append(" iri.incentive_type as incentiveType, iri.amount as amount, ")
                    .append("code.code_value as attributeValueDesc ").append("from ")
                    .append("m_interest_rate_chart irc left join m_interest_rate_slab ircd on irc.id=ircd.interest_rate_chart_id ")
                    .append(" left join m_interest_incentives iri on iri.interest_rate_slab_id = ircd.id ")
                    .append(" left join m_code_value code on code.id = iri.attribute_value ")
                    .append("left join m_currency curr on ircd.currency_code= curr.code ")
                    .append("left join m_deposit_product_interest_rate_chart dpirc on irc.id=dpirc.interest_rate_chart_id ")
                    .append("left join m_savings_product sp on sp.id=dpirc.deposit_product_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public Collection<InterestRateChartData> extractData(ResultSet rs) throws SQLException, DataAccessException {

            List<InterestRateChartData> chartDataList = new ArrayList<>();

            InterestRateChartData chartData = null;
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
                final InterestRateChartSlabData chartSlabsData = chartSlabsMapper.extractData(rs);
                if (chartSlabsData != null) {
                    chartData.addChartSlab(chartSlabsData);
                }
            }
            return chartDataList;
        }

    }

    public static final class InterestRateChartMapper implements RowMapper<InterestRateChartData> {

        private final String schemaSql;

        public String schema() {
            return this.schemaSql;
        }

        private InterestRateChartMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append("irc.id as ircId, irc.name as ircName, irc.description as ircDescription, ")
                    .append("irc.from_date as ircFromDate, irc.end_date as ircEndDate, ")
                    .append("sp.id as savingsProductId, sp.name as savingsProductName ").append("from ")
                    .append("m_interest_rate_chart irc ")
                    .append("left join m_deposit_product_interest_rate_chart dpirc on irc.id=dpirc.interest_rate_chart_id ")
                    .append("left join m_savings_product sp on sp.id=dpirc.deposit_product_id ");
            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public InterestRateChartData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("ircId");
            final String name = rs.getString("ircName");
            final String description = rs.getString("ircDescription");
            final LocalDate fromDate = JdbcSupport.getLocalDate(rs, "ircFromDate");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "ircEndDate");
            final Long savingsProductId = JdbcSupport.getLongDefaultToNullIfZero(rs, "savingsProductId");
            final String savingsProductName = rs.getString("savingsProductName");

            return InterestRateChartData.instance(id, name, description, fromDate, endDate, savingsProductId, savingsProductName);
        }

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
                    .append("curr.code as currencyCode, curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ")
                    .append("curr.display_symbol as currencyDisplaySymbol, curr.decimal_places as currencyDigits, curr.currency_multiplesof as inMultiplesOf, ")
                    .append("iri.id as iriId, ").append(" iri.entiry_type as entityType, iri.attribute_name as attributeName ,")
                    .append(" iri.condition_type as conditionType, iri.attribute_value as attributeValue, ")
                    .append(" iri.incentive_type as incentiveType, iri.amount as amount, ")
                    .append("code.code_value as attributeValueDesc ").append("from ").append("m_interest_rate_slab ircd ")
                    .append(" left join m_interest_incentives iri on iri.interest_rate_slab_id = ircd.id ")
                    .append(" left join m_code_value code on code.id = iri.attribute_value ")
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
                    annualInterestRate, currency);
        }

    }

    private static final class InterestRateChartSlabExtractor implements ResultSetExtractor<InterestRateChartSlabData> {

        InterestRateChartSlabsMapper chartSlabsMapper = new InterestRateChartSlabsMapper();
        InterestIncentiveMapper incentiveMapper = new InterestIncentiveMapper();

        private final String schemaSql;

        @SuppressWarnings("unused")
        public String schema() {
            return this.schemaSql;
        }

        private InterestRateChartSlabExtractor() {
            this.schemaSql = chartSlabsMapper.schema();
        }

        @Override
        public InterestRateChartSlabData extractData(ResultSet rs) throws SQLException, DataAccessException {

            InterestRateChartSlabData chartSlabData = null;
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
                    final InterestIncentiveData incentiveData = incentiveMapper.mapRow(rs, ircdIndex++);
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

        private static final class InterestIncentiveMapper implements RowMapper<InterestIncentiveData> {

            @Override
            public InterestIncentiveData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
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

                return InterestIncentiveData.instance(id, entityTypeData, attributeNameData, conditionTypeData, attributeValue,
                        attributeValueDesc, incentiveTypeData, amount);

            }
        }
    }

}