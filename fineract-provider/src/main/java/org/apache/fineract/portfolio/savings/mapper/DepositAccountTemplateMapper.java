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
package org.apache.fineract.portfolio.savings.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.springframework.jdbc.core.RowMapper;

public abstract class DepositAccountTemplateMapper implements RowMapper<DepositAccountData> {

    public static final String DEPOSIT_TYPE_ID = "depositTypeId";
    public static final String PRODUCT_ID = "productId";
    public static final String PRODUCT_NAME = "productName";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String CURRENCY_NAME = "currencyName";
    public static final String CURRENCY_NAME_CODE = "currencyNameCode";
    public static final String CURRENCY_DISPLAY_SYMBOL = "currencyDisplaySymbol";
    public static final String CURRENCY_DIGITS = "currencyDigits";
    public static final String IN_MULTIPLES_OF = "inMultiplesOf";
    public static final String NOMINAL_ANNUAL_ITEREST_RATE = "nominalAnnualIterestRate";
    public static final String INTEREST_COMPOUNDING_PERIOD_TYPE = "interestCompoundingPeriodType";
    public static final String INTEREST_POSTING_PERIOD_TYPE = "interestPostingPeriodType";
    public static final String INTEREST_CALCULATION_TYPE = "interestCalculationType";
    public static final String INTEREST_CALCULATION_DAYS_IN_YEAR_TYPE = "interestCalculationDaysInYearType";
    public static final String MIN_REQUIRED_OPENING_BALANCE = "minRequiredOpeningBalance";
    public static final String LOCKIN_PERIOD_FREQUENCY = "lockinPeriodFrequency";
    public static final String LOCKIN_PERIOD_FREQUENCY_TYPE = "lockinPeriodFrequencyType";
    public static final String WITHDRAWAL_FEE_FOR_TRANSFERS = "withdrawalFeeForTransfers";
    public static final String MIN_BALANCE_FOR_INTEREST_CALCULATION = "minBalanceForInterestCalculation";
    public static final String WITH_HOLD_TAX = "withHoldTax";
    public static final String TAX_GROUP_ID = "taxGroupId";
    public static final String TAX_GROUP_NAME = "taxGroupName";
    private final String selectFieldsSql;
    private final String selectTablesSql;

    private final ClientData client;
    private final GroupGeneralData group;

    protected DepositAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
        this.client = client;
        this.group = group;

        final StringBuilder selectFieldsSqlBuilder = new StringBuilder(400);
        selectFieldsSqlBuilder.append("sa.id as productId, sa.name as productName, ");
        selectFieldsSqlBuilder.append(
                "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
        selectFieldsSqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
        selectFieldsSqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
        selectFieldsSqlBuilder.append("sa.nominal_annual_interest_rate as nominalAnnualIterestRate, ");
        selectFieldsSqlBuilder.append("sa.interest_compounding_period_enum as interestCompoundingPeriodType, ");
        selectFieldsSqlBuilder.append("sa.interest_posting_period_enum as interestPostingPeriodType, ");
        selectFieldsSqlBuilder.append("sa.interest_calculation_type_enum as interestCalculationType, ");
        selectFieldsSqlBuilder.append("sa.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
        selectFieldsSqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance, ");
        selectFieldsSqlBuilder.append("sa.lockin_period_frequency as lockinPeriodFrequency,");
        selectFieldsSqlBuilder.append("sa.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
        selectFieldsSqlBuilder.append("sa.withdrawal_fee_for_transfer as withdrawalFeeForTransfers, ");
        selectFieldsSqlBuilder.append("sa.deposit_type_enum as depositTypeId, ");
        selectFieldsSqlBuilder.append("sa.min_balance_for_interest_calculation as minBalanceForInterestCalculation, ");
        selectFieldsSqlBuilder.append("sa.withhold_tax as withHoldTax,");
        selectFieldsSqlBuilder.append("tg.id as taxGroupId, tg.name as taxGroupName ");

        this.selectFieldsSql = selectFieldsSqlBuilder.toString();

        final StringBuilder selectTablesSqlBuilder = new StringBuilder(400);
        selectTablesSqlBuilder.append("from m_savings_product sa ");
        selectTablesSqlBuilder.append("left join m_deposit_product_term_and_preclosure dptp on sa.id = dptp.savings_product_id ");
        selectTablesSqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
        selectTablesSqlBuilder.append("left join m_tax_group tg on tg.id = sa.tax_group_id  ");

        this.selectTablesSql = selectTablesSqlBuilder.toString();
    }

    public String selectFieldsSql() {
        return this.selectFieldsSql;
    }

    public String selectTablesSql() {
        return this.selectTablesSql;
    }

    public abstract String schema();

    public DepositAccountData mapRow(final ResultSet rs) throws SQLException {

        final Integer depositTypeId = JdbcSupport.getInteger(rs, DEPOSIT_TYPE_ID);
        final EnumOptionData depositType = (depositTypeId == null) ? null : SavingsEnumerations.depositType(depositTypeId);
        final Long productId = rs.getLong(PRODUCT_ID);
        final String productName = rs.getString(PRODUCT_NAME);

        final String currencyCode = rs.getString(CURRENCY_CODE);
        final String currencyName = rs.getString(CURRENCY_NAME);
        final String currencyNameCode = rs.getString(CURRENCY_NAME_CODE);
        final String currencyDisplaySymbol = rs.getString(CURRENCY_DISPLAY_SYMBOL);
        final Integer currencyDigits = JdbcSupport.getInteger(rs, CURRENCY_DIGITS);
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, IN_MULTIPLES_OF);
        final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        final BigDecimal nominalAnnualIterestRate = rs.getBigDecimal(NOMINAL_ANNUAL_ITEREST_RATE);

        final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations.compoundingInterestPeriodType(
                SavingsCompoundingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs, INTEREST_COMPOUNDING_PERIOD_TYPE)));

        final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(
                SavingsPostingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs, INTEREST_POSTING_PERIOD_TYPE)));

        final EnumOptionData interestCalculationType = SavingsEnumerations
                .interestCalculationType(SavingsInterestCalculationType.fromInt(JdbcSupport.getInteger(rs, INTEREST_CALCULATION_TYPE)));

        final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations.interestCalculationDaysInYearType(
                SavingsInterestCalculationDaysInYearType.fromInt(JdbcSupport.getInteger(rs, INTEREST_CALCULATION_DAYS_IN_YEAR_TYPE)));

        final BigDecimal minRequiredOpeningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, MIN_REQUIRED_OPENING_BALANCE);

        final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, LOCKIN_PERIOD_FREQUENCY);
        EnumOptionData lockinPeriodFrequencyType = null;
        final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, LOCKIN_PERIOD_FREQUENCY_TYPE);
        if (lockinPeriodFrequencyTypeValue != null) {
            final SavingsPeriodFrequencyType lockinPeriodType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
            lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodType);
        }

        final boolean withdrawalFeeForTransfers = rs.getBoolean(WITHDRAWAL_FEE_FOR_TRANSFERS);
        final BigDecimal minBalanceForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                MIN_BALANCE_FOR_INTEREST_CALCULATION);

        Long clientId = null;
        String clientName = null;
        if (this.client != null) {
            clientId = this.client.getId();
            clientName = this.client.getDisplayName();
        }

        Long groupId = null;
        String groupName = null;
        if (this.group != null) {
            groupId = this.group.getId();
            groupName = this.group.getName();
        }

        final Long fieldOfficerId = null;
        final String fieldOfficerName = null;
        final SavingsAccountStatusEnumData status = null;
        final SavingsAccountSummaryData summary = null;
        final SavingsAccountApplicationTimelineData timeline = SavingsAccountApplicationTimelineData.templateDefault();

        final boolean withHoldTax = rs.getBoolean(WITH_HOLD_TAX);
        final Long taxGroupId = JdbcSupport.getLong(rs, TAX_GROUP_ID);
        final String taxGroupName = rs.getString(TAX_GROUP_NAME);
        TaxGroupData taxGroupData = null;
        if (taxGroupId != null) {
            taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
        }

        return DepositAccountData.instance(null, null, null, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualIterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, depositType,
                minBalanceForInterestCalculation, withHoldTax, taxGroupData);
    }
}
