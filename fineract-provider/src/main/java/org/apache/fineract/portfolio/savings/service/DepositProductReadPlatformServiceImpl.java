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
import java.util.Collection;

import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.DepositProductData;
import org.apache.fineract.portfolio.savings.data.FixedDepositProductData;
import org.apache.fineract.portfolio.savings.data.RecurringDepositProductData;
import org.apache.fineract.portfolio.savings.exception.FixedDepositProductNotFoundException;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DepositProductReadPlatformServiceImpl implements DepositProductReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final InterestRateChartReadPlatformService chartReadPlatformService;
    private final FixedDepositProductMapper fixedDepositProductRowMapper = new FixedDepositProductMapper();
    private final RecurringDepositProductMapper recurringDepositProductRowMapper = new RecurringDepositProductMapper();
    private final DepositProductLookupMapper depositProductLookupsRowMapper = new DepositProductLookupMapper();

    @Autowired
    public DepositProductReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final InterestRateChartReadPlatformService chartReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.chartReadPlatformService = chartReadPlatformService;
    }

    @Override
    public Collection<DepositProductData> retrieveAll(final DepositAccountType depositAccountType) {

        this.context.authenticatedUser();
        final DepositProductMapper depositProductMapper = this.getDepositProductMapper(depositAccountType);
        if (depositProductMapper == null) return null;

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ");
        sqlBuilder.append(depositProductMapper.schema());
        sqlBuilder.append(" where sp.deposit_type_enum = ? ");

        return this.jdbcTemplate.query(sqlBuilder.toString(), depositProductMapper, new Object[] { depositAccountType.getValue() });
    }

    @Override
    public Collection<DepositProductData> retrieveAllForLookup(final DepositAccountType depositAccountType) {

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ");
        sqlBuilder.append(this.depositProductLookupsRowMapper.schema());
        sqlBuilder.append(" where sp.deposit_type_enum = ? ");

        return this.jdbcTemplate.query(sqlBuilder.toString(), this.depositProductLookupsRowMapper,
                new Object[] { depositAccountType.getValue() });
    }

    @Override
    public DepositProductData retrieveOne(final DepositAccountType depositAccountType, final Long fixedDepositProductId) {
        try {
            this.context.authenticatedUser();

            final DepositProductMapper depositProductMapper = this.getDepositProductMapper(depositAccountType);
            if (depositProductMapper == null) return null;

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("select ");
            sqlBuilder.append(depositProductMapper.schema());
            sqlBuilder.append(" where sp.id = ? and sp.deposit_type_enum = ? ");

            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), depositProductMapper, new Object[] { fixedDepositProductId,
                    depositAccountType.getValue() });

        } catch (final EmptyResultDataAccessException e) {
            throw new FixedDepositProductNotFoundException(fixedDepositProductId);
        }
    }

    @Override
    public DepositProductData retrieveOneWithChartSlabs(final DepositAccountType depositAccountType, Long depositProductId) {
        DepositProductData depositProduct = this.retrieveOne(depositAccountType, depositProductId);
        Collection<InterestRateChartData> charts = this.chartReadPlatformService.retrieveAllWithSlabsWithTemplate(depositProductId);

        if (depositAccountType.isFixedDeposit()) {
            depositProduct = FixedDepositProductData.withInterestChart(depositProduct, charts);
        } else if (depositAccountType.isRecurringDeposit()) {
            depositProduct = RecurringDepositProductData.withInterestChart(depositProduct, charts);
        }

        return depositProduct;
    }

    private static abstract class DepositProductMapper implements RowMapper<DepositProductData> {

        private final String schemaSql;

        @Override
        public abstract DepositProductData mapRow(ResultSet rs, int rowNum) throws SQLException;

        public DepositProductMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sp.id as id, sp.name as name, sp.short_name as shortName, sp.description as description, ");
            sqlBuilder
                    .append("sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, sp.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sp.nominal_annual_interest_rate as nominalAnnualInterestRate, ");
            sqlBuilder.append("sp.interest_compounding_period_enum as compoundingInterestPeriodType, ");
            sqlBuilder.append("sp.interest_posting_period_enum as interestPostingPeriodType, ");
            sqlBuilder.append("sp.interest_calculation_type_enum as interestCalculationType, ");
            sqlBuilder.append("sp.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
            sqlBuilder.append("sp.lockin_period_frequency as lockinPeriodFrequency,");
            sqlBuilder.append("sp.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
            sqlBuilder.append("sp.accounting_type as accountingType, ");
            sqlBuilder.append("sp.min_balance_for_interest_calculation as minBalanceForInterestCalculation, ");
            sqlBuilder.append("sp.withhold_tax as withHoldTax,");
            sqlBuilder.append("tg.id as taxGroupId, tg.name as taxGroupName ");
            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        public DepositProductData mapRow(final ResultSet rs) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String shortName = rs.getString("shortName");
            final String description = rs.getString("description");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);
            final BigDecimal nominalAnnualInterestRate = rs.getBigDecimal("nominalAnnualInterestRate");

            final Integer compoundingInterestPeriodTypeValue = JdbcSupport.getInteger(rs, "compoundingInterestPeriodType");
            final EnumOptionData compoundingInterestPeriodType = SavingsEnumerations
                    .compoundingInterestPeriodType(compoundingInterestPeriodTypeValue);

            final Integer interestPostingPeriodTypeValue = JdbcSupport.getInteger(rs, "interestPostingPeriodType");
            final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(interestPostingPeriodTypeValue);

            final Integer interestCalculationTypeValue = JdbcSupport.getInteger(rs, "interestCalculationType");
            final EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(interestCalculationTypeValue);

            EnumOptionData interestCalculationDaysInYearType = null;
            final Integer interestCalculationDaysInYearTypeValue = JdbcSupport.getInteger(rs, "interestCalculationDaysInYearType");
            if (interestCalculationDaysInYearTypeValue != null) {
                interestCalculationDaysInYearType = SavingsEnumerations
                        .interestCalculationDaysInYearType(interestCalculationDaysInYearTypeValue);
            }

            final Integer accountingRuleId = JdbcSupport.getInteger(rs, "accountingType");
            final EnumOptionData accountingRuleType = AccountingEnumerations.accountingRuleType(accountingRuleId);

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodFrequencyTypeValue);
            }
            final BigDecimal minBalanceForInterestCalculation = rs.getBigDecimal("minBalanceForInterestCalculation");

            final boolean withHoldTax = rs.getBoolean("withHoldTax");
            final Long taxGroupId = JdbcSupport.getLong(rs, "taxGroupId");
            final String taxGroupName = rs.getString("taxGroupName");
            TaxGroupData taxGroupData = null;
            if (taxGroupId != null) {
                taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
            }

            return DepositProductData.instance(id, name, shortName, description, currency, nominalAnnualInterestRate,
                    compoundingInterestPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                    lockinPeriodFrequency, lockinPeriodFrequencyType, accountingRuleType, minBalanceForInterestCalculation, withHoldTax,
                    taxGroupData);
        }
    }

    private static class FixedDepositProductMapper extends DepositProductMapper {

        private final String schemaSql;

        public FixedDepositProductMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(super.schema());
            sqlBuilder.append(", dptp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest as preClosurePenalInterest, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
            sqlBuilder.append("dptp.min_deposit_term as minDepositTerm, ");
            sqlBuilder.append("dptp.max_deposit_term as maxDepositTerm, ");
            sqlBuilder.append("dptp.min_deposit_term_type_enum as minDepositTermTypeId, ");
            sqlBuilder.append("dptp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId, ");
            sqlBuilder.append("dptp.min_deposit_amount as minDepositAmount, dptp.deposit_amount as depositAmount, ");
            sqlBuilder.append("dptp.max_deposit_amount as maxDepositAmount ");
            sqlBuilder.append("from m_savings_product sp ");
            sqlBuilder.append("left join m_deposit_product_term_and_preclosure dptp on sp.id=dptp.savings_product_id ");
            sqlBuilder.append("join m_currency curr on curr.code = sp.currency_code ");
            sqlBuilder.append(" left join m_tax_group tg on tg.id = sp.tax_group_id  ");

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public String schema() {
            return this.schemaSql;
        }

        @Override
        public FixedDepositProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final DepositProductData depositProductData = super.mapRow(rs);

            final boolean preClosurePenalApplicable = rs.getBoolean("preClosurePenalApplicable");
            final BigDecimal preClosurePenalInterest = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "preClosurePenalInterest");
            final Integer preClosurePenalInterestOnTypeId = JdbcSupport.getInteger(rs, "preClosurePenalInterestOnId");
            final EnumOptionData preClosurePenalInterestOnType = (preClosurePenalInterestOnTypeId == null) ? null : SavingsEnumerations
                    .preClosurePenaltyInterestOnType(preClosurePenalInterestOnTypeId);

            final Integer minDepositTerm = JdbcSupport.getInteger(rs, "minDepositTerm");
            final Integer maxDepositTerm = JdbcSupport.getInteger(rs, "maxDepositTerm");
            final Integer minDepositTermTypeId = JdbcSupport.getInteger(rs, "minDepositTermTypeId");
            final EnumOptionData minDepositTermType = (minDepositTermTypeId == null) ? null : SavingsEnumerations
                    .depositTermFrequencyType(minDepositTermTypeId);
            final Integer maxDepositTermTypeId = JdbcSupport.getInteger(rs, "maxDepositTermTypeId");
            final EnumOptionData maxDepositTermType = (maxDepositTermTypeId == null) ? null : SavingsEnumerations
                    .depositTermFrequencyType(maxDepositTermTypeId);
            final Integer inMultiplesOfDepositTerm = JdbcSupport.getInteger(rs, "inMultiplesOfDepositTerm");
            final Integer inMultiplesOfDepositTermTypeId = JdbcSupport.getInteger(rs, "inMultiplesOfDepositTermTypeId");
            final EnumOptionData inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null : SavingsEnumerations
                    .depositTermFrequencyType(inMultiplesOfDepositTermTypeId);
            final BigDecimal minDepositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minDepositAmount");
            final BigDecimal depositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "depositAmount");
            final BigDecimal maxDepositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maxDepositAmount");

            return FixedDepositProductData.instance(depositProductData, preClosurePenalApplicable, preClosurePenalInterest,
                    preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                    inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, minDepositAmount, depositAmount, maxDepositAmount);
        }
    }

    private static class RecurringDepositProductMapper extends DepositProductMapper {

        private final String schemaSql;

        public RecurringDepositProductMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(super.schema());
            sqlBuilder.append(", dptp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest as preClosurePenalInterest, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
            sqlBuilder.append("dptp.min_deposit_amount as minDepositAmount, ");
            sqlBuilder.append("dptp.deposit_amount as depositAmount, ");
            sqlBuilder.append("dptp.max_deposit_amount as maxDepositAmount, ");
            sqlBuilder.append("dprd.is_mandatory as isMandatoryDeposit, ");
            sqlBuilder.append("dprd.allow_withdrawal as allowWithdrawal, ");
            sqlBuilder.append("dprd.adjust_advance_towards_future_payments as adjustAdvanceTowardsFuturePayments, ");
            sqlBuilder.append("dptp.min_deposit_term as minDepositTerm, ");
            sqlBuilder.append("dptp.max_deposit_term as maxDepositTerm, ");
            sqlBuilder.append("dptp.min_deposit_term_type_enum as minDepositTermTypeId, ");
            sqlBuilder.append("dptp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId ");
            sqlBuilder.append("from m_savings_product sp ");
            sqlBuilder.append("left join m_deposit_product_term_and_preclosure dptp on sp.id=dptp.savings_product_id ");
            sqlBuilder.append("left join m_deposit_product_recurring_detail dprd on sp.id=dprd.savings_product_id ");
            sqlBuilder.append("join m_currency curr on curr.code = sp.currency_code ");
            sqlBuilder.append(" left join m_tax_group tg on tg.id = sp.tax_group_id  ");

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public String schema() {
            return this.schemaSql;
        }

        @Override
        public RecurringDepositProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final DepositProductData depositProductData = super.mapRow(rs);

            final boolean isMandatoryDeposit = rs.getBoolean("isMandatoryDeposit");
            final boolean allowWithdrawal = rs.getBoolean("allowWithdrawal");
            final boolean adjustAdvanceTowardsFuturePayments = rs.getBoolean("adjustAdvanceTowardsFuturePayments");
            final boolean preClosurePenalApplicable = rs.getBoolean("preClosurePenalApplicable");
            final BigDecimal preClosurePenalInterest = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "preClosurePenalInterest");
            final Integer preClosurePenalInterestOnTypeId = JdbcSupport.getInteger(rs, "preClosurePenalInterestOnId");
            final EnumOptionData preClosurePenalInterestOnType = (preClosurePenalInterestOnTypeId == null) ? null : SavingsEnumerations
                    .preClosurePenaltyInterestOnType(preClosurePenalInterestOnTypeId);
            final BigDecimal minDepositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minDepositAmount");
            final BigDecimal depositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "depositAmount");
            final BigDecimal maxDepositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maxDepositAmount");
            final Integer minDepositTerm = JdbcSupport.getInteger(rs, "minDepositTerm");
            final Integer maxDepositTerm = JdbcSupport.getInteger(rs, "maxDepositTerm");
            final Integer minDepositTermTypeId = JdbcSupport.getInteger(rs, "minDepositTermTypeId");
            final EnumOptionData minDepositTermType = (minDepositTermTypeId == null) ? null : SavingsEnumerations
                    .depositTermFrequencyType(minDepositTermTypeId);
            final Integer maxDepositTermTypeId = JdbcSupport.getInteger(rs, "maxDepositTermTypeId");
            final EnumOptionData maxDepositTermType = (maxDepositTermTypeId == null) ? null : SavingsEnumerations
                    .depositTermFrequencyType(maxDepositTermTypeId);
            final Integer inMultiplesOfDepositTerm = JdbcSupport.getInteger(rs, "inMultiplesOfDepositTerm");
            final Integer inMultiplesOfDepositTermTypeId = JdbcSupport.getInteger(rs, "inMultiplesOfDepositTermTypeId");
            final EnumOptionData inMultiplesOfDepositTermType = (inMultiplesOfDepositTermTypeId == null) ? null : SavingsEnumerations
                    .depositTermFrequencyType(inMultiplesOfDepositTermTypeId);

            return RecurringDepositProductData.instance(depositProductData, preClosurePenalApplicable, preClosurePenalInterest,
                    preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                    inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, isMandatoryDeposit, allowWithdrawal,
                    adjustAdvanceTowardsFuturePayments, minDepositAmount, depositAmount, maxDepositAmount);
        }
    }

    private static final class DepositProductLookupMapper implements RowMapper<DepositProductData> {

        public String schema() {
            return " sp.id as id, sp.name as name from m_savings_product sp ";
        }

        @Override
        public DepositProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            return DepositProductData.lookup(id, name);
        }
    }

    private DepositProductMapper getDepositProductMapper(final DepositAccountType depositAccountType) {
        if (depositAccountType.isFixedDeposit()) {
            return this.fixedDepositProductRowMapper;
        } else if (depositAccountType.isRecurringDeposit()) { return this.recurringDepositProductRowMapper; }
        return null;
    }
}