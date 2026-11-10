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
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
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

public abstract class DepositAccountMapper implements RowMapper<DepositAccountData> {

    public static final String ACCOUNT_NO = "accountNo";
    public static final String ID = "id";
    public static final String EXTERNAL_ID = "externalId";
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_NAME = "groupName";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_NAME = "clientName";
    public static final String PRODUCT_ID = "productId";
    public static final String PRODUCT_NAME = "productName";
    public static final String FIELD_OFFICER_ID = "fieldOfficerId";
    public static final String FIELD_OFFICER_NAME = "fieldOfficerName";
    public static final String STATUS_ENUM = "statusEnum";
    public static final String SUBMITTED_ON_DATE = "submittedOnDate";
    public static final String SUBMITTED_BY_USERNAME = "submittedByUsername";
    public static final String SUBMITTED_BY_LASTNAME = "submittedByLastname";
    public static final String SUBMITTED_BY_FIRSTNAME = "submittedByFirstname";
    public static final String REJECTED_ON_DATE = "rejectedOnDate";
    public static final String REJECTED_BY_USERNAME = "rejectedByUsername";
    public static final String REJECTED_BY_FIRSTNAME = "rejectedByFirstname";
    public static final String REJECTED_BY_LASTNAME = "rejectedByLastname";
    public static final String WITHDRAWN_ON_DATE = "withdrawnOnDate";
    public static final String WITHDRAWN_BY_USERNAME = "withdrawnByUsername";
    public static final String WITHDRAWN_BY_FIRSTNAME = "withdrawnByFirstname";
    public static final String WITHDRAWN_BY_LASTNAME = "withdrawnByLastname";
    public static final String APPROVED_ON_DATE = "approvedOnDate";
    public static final String APPROVED_BY_USERNAME = "approvedByUsername";
    public static final String APPROVED_BY_FIRSTNAME = "approvedByFirstname";
    public static final String APPROVED_BY_LASTNAME = "approvedByLastname";
    public static final String ACTIVATED_ON_DATE = "activatedOnDate";
    public static final String ACTIVATED_BY_USERNAME = "activatedByUsername";
    public static final String ACTIVATED_BY_FIRSTNAME = "activatedByFirstname";
    public static final String ACTIVATED_BY_LASTNAME = "activatedByLastname";
    public static final String CLOSED_ON_DATE = "closedOnDate";
    public static final String CLOSED_BY_USERNAME = "closedByUsername";
    public static final String CLOSED_BY_FIRSTNAME = "closedByFirstname";
    public static final String CLOSED_BY_LASTNAME = "closedByLastname";
    public static final String DEPOSIT_TYPE_ID = "depositTypeId";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String CURRENCY_NAME = "currencyName";
    public static final String CURRENCY_NAME_CODE = "currencyNameCode";
    public static final String CURRENCY_DISPLAY_SYMBOL = "currencyDisplaySymbol";
    public static final String CURRENCY_DIGITS = "currencyDigits";
    public static final String IN_MULTIPLES_OF = "inMultiplesOf";
    public static final String NOMINAL_ANNUAL_INTEREST_RATE = "nominalAnnualInterestRate";
    public static final String INTEREST_COMPOUNDING_PERIOD_TYPE = "interestCompoundingPeriodType";
    public static final String INTEREST_POSTING_PERIOD_TYPE = "interestPostingPeriodType";
    public static final String INTEREST_CALCULATION_TYPE = "interestCalculationType";
    public static final String INTEREST_CALCULATION_DAYS_IN_YEAR_TYPE = "interestCalculationDaysInYearType";
    public static final String MIN_REQUIRED_OPENING_BALANCE = "minRequiredOpeningBalance";
    public static final String LOCKIN_PERIOD_FREQUENCY = "lockinPeriodFrequency";
    public static final String LOCKIN_PERIOD_FREQUENCY_TYPE = "lockinPeriodFrequencyType";
    public static final String WITHDRAWAL_FEE_FOR_TRANSFERS = "withdrawalFeeForTransfers";
    public static final String MIN_BALANCE_FOR_INTEREST_CALCULATION = "minBalanceForInterestCalculation";
    public static final String TOTAL_DEPOSITS = "totalDeposits";
    public static final String TOTAL_WITHDRAWALS = "totalWithdrawals";
    public static final String TOTAL_WITHDRAWAL_FEES = "totalWithdrawalFees";
    public static final String TOTAL_ANNUAL_FEES = "totalAnnualFees";
    public static final String TOTAL_INTEREST_EARNED = "totalInterestEarned";
    public static final String TOTAL_INTEREST_POSTED = "totalInterestPosted";
    public static final String ACCOUNT_BALANCE = "accountBalance";
    public static final String TOTAL_FEE_CHARGE = "totalFeeCharge";
    public static final String TOTAL_PENALTY_CHARGE = "totalPenaltyCharge";
    public static final String TOTAL_WITHHOLD_TAX = "totalWithholdTax";
    public static final String INTEREST_POSTED_TILL_DATE = "interestPostedTillDate";
    public static final String WITH_HOLD_TAX = "withHoldTax";
    public static final String TAX_GROUP_ID = "taxGroupId";
    public static final String TAX_GROUP_NAME = "taxGroupName";
    private final String selectFieldsSql;
    private final String selectTablesSql;

    protected DepositAccountMapper() {
        final StringBuilder selectFieldsSqlBuilder = new StringBuilder(400);
        selectFieldsSqlBuilder.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId, ");
        selectFieldsSqlBuilder.append("c.id as clientId, c.display_name as clientName, ");
        selectFieldsSqlBuilder.append("g.id as groupId, g.display_name as groupName, ");
        selectFieldsSqlBuilder.append("sp.id as productId, sp.name as productName, ");
        selectFieldsSqlBuilder.append("s.id fieldOfficerId, s.display_name as fieldOfficerName, ");
        selectFieldsSqlBuilder.append("sa.status_enum as statusEnum, ");
        selectFieldsSqlBuilder.append("sa.submittedon_date as submittedOnDate,");
        selectFieldsSqlBuilder.append("sbu.username as submittedByUsername,");
        selectFieldsSqlBuilder.append("sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname,");
        selectFieldsSqlBuilder.append("sa.rejectedon_date as rejectedOnDate,");
        selectFieldsSqlBuilder.append("rbu.username as rejectedByUsername,");
        selectFieldsSqlBuilder.append("rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname,");
        selectFieldsSqlBuilder.append("sa.withdrawnon_date as withdrawnOnDate,");
        selectFieldsSqlBuilder.append("wbu.username as withdrawnByUsername,");
        selectFieldsSqlBuilder.append("wbu.firstname as withdrawnByFirstname, wbu.lastname as withdrawnByLastname,");
        selectFieldsSqlBuilder.append("sa.approvedon_date as approvedOnDate,");
        selectFieldsSqlBuilder.append("abu.username as approvedByUsername,");
        selectFieldsSqlBuilder.append("abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname,");
        selectFieldsSqlBuilder.append("sa.activatedon_date as activatedOnDate,");
        selectFieldsSqlBuilder.append("avbu.username as activatedByUsername,");
        selectFieldsSqlBuilder.append("avbu.firstname as activatedByFirstname, avbu.lastname as activatedByLastname,");
        selectFieldsSqlBuilder.append("sa.closedon_date as closedOnDate,");
        selectFieldsSqlBuilder.append("cbu.username as closedByUsername,");
        selectFieldsSqlBuilder.append("cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname,");
        selectFieldsSqlBuilder.append(
                "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
        selectFieldsSqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
        selectFieldsSqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
        selectFieldsSqlBuilder.append("sa.nominal_annual_interest_rate as nominalAnnualInterestRate, ");
        selectFieldsSqlBuilder.append("sa.interest_compounding_period_enum as interestCompoundingPeriodType, ");
        selectFieldsSqlBuilder.append("sa.interest_posting_period_enum as interestPostingPeriodType, ");
        selectFieldsSqlBuilder.append("sa.interest_calculation_type_enum as interestCalculationType, ");
        selectFieldsSqlBuilder.append("sa.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
        selectFieldsSqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance, ");
        selectFieldsSqlBuilder.append("sa.lockin_period_frequency as lockinPeriodFrequency,");
        selectFieldsSqlBuilder.append("sa.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
        selectFieldsSqlBuilder.append("sa.withdrawal_fee_for_transfer as withdrawalFeeForTransfers, ");
        selectFieldsSqlBuilder.append("sa.total_deposits_derived as totalDeposits, ");
        selectFieldsSqlBuilder.append("sa.total_withdrawals_derived as totalWithdrawals, ");
        selectFieldsSqlBuilder.append("sa.interest_posted_till_date as interestPostedTillDate, ");
        selectFieldsSqlBuilder.append("sa.total_withdrawal_fees_derived as totalWithdrawalFees, ");
        selectFieldsSqlBuilder.append("sa.total_annual_fees_derived as totalAnnualFees, ");
        selectFieldsSqlBuilder.append("sa.total_interest_earned_derived as totalInterestEarned, ");
        selectFieldsSqlBuilder.append("sa.total_interest_posted_derived as totalInterestPosted, ");
        selectFieldsSqlBuilder.append("sa.account_balance_derived as accountBalance, ");
        selectFieldsSqlBuilder.append("sa.total_fees_charge_derived as totalFeeCharge, ");
        selectFieldsSqlBuilder.append("sa.total_penalty_charge_derived as totalPenaltyCharge, ");
        selectFieldsSqlBuilder.append("sa.total_withhold_tax_derived as totalWithholdTax,");
        selectFieldsSqlBuilder.append("sa.deposit_type_enum as depositTypeId, ");
        selectFieldsSqlBuilder.append("sa.min_balance_for_interest_calculation as minBalanceForInterestCalculation, ");
        selectFieldsSqlBuilder.append("sa.withhold_tax as withHoldTax,");
        selectFieldsSqlBuilder.append("tg.id as taxGroupId, tg.name as taxGroupName ");

        this.selectFieldsSql = selectFieldsSqlBuilder.toString();

        final StringBuilder selectTablesSqlBuilder = new StringBuilder(400);
        selectTablesSqlBuilder.append("from m_savings_account sa ");
        selectTablesSqlBuilder.append("left join m_deposit_account_term_and_preclosure datp on sa.id = datp.savings_account_id ");
        selectTablesSqlBuilder.append("join m_savings_product sp ON sa.product_id = sp.id ");
        selectTablesSqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
        selectTablesSqlBuilder.append("left join m_client c ON c.id = sa.client_id ");
        selectTablesSqlBuilder.append("left join m_group g ON g.id = sa.group_id ");
        selectTablesSqlBuilder.append("left join m_staff s ON s.id = sa.field_officer_id ");
        selectTablesSqlBuilder.append("left join m_appuser sbu on sbu.id = sa.submittedon_userid ");
        selectTablesSqlBuilder.append("left join m_appuser rbu on rbu.id = sa.rejectedon_userid ");
        selectTablesSqlBuilder.append("left join m_appuser wbu on wbu.id = sa.withdrawnon_userid ");
        selectTablesSqlBuilder.append("left join m_appuser abu on abu.id = sa.approvedon_userid ");
        selectTablesSqlBuilder.append("left join m_appuser avbu on rbu.id = sa.activatedon_userid ");
        selectTablesSqlBuilder.append("left join m_appuser cbu on cbu.id = sa.closedon_userid ");
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

        final Long id = rs.getLong(ID);
        final String accountNo = rs.getString(ACCOUNT_NO);
        final String externalId = rs.getString(EXTERNAL_ID);

        final Long groupId = JdbcSupport.getLong(rs, GROUP_ID);
        final String groupName = rs.getString(GROUP_NAME);
        final Long clientId = JdbcSupport.getLong(rs, CLIENT_ID);
        final String clientName = rs.getString(CLIENT_NAME);

        final Long productId = rs.getLong(PRODUCT_ID);
        final String productName = rs.getString(PRODUCT_NAME);

        final Long fieldOfficerId = rs.getLong(FIELD_OFFICER_ID);
        final String fieldOfficerName = rs.getString(FIELD_OFFICER_NAME);

        final Integer statusEnum = JdbcSupport.getInteger(rs, STATUS_ENUM);
        final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusEnum);

        final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, SUBMITTED_ON_DATE);
        final String submittedByUsername = rs.getString(SUBMITTED_BY_USERNAME);
        final String submittedByFirstname = rs.getString(SUBMITTED_BY_FIRSTNAME);
        final String submittedByLastname = rs.getString(SUBMITTED_BY_LASTNAME);

        final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, REJECTED_ON_DATE);
        final String rejectedByUsername = rs.getString(REJECTED_BY_USERNAME);
        final String rejectedByFirstname = rs.getString(REJECTED_BY_FIRSTNAME);
        final String rejectedByLastname = rs.getString(REJECTED_BY_LASTNAME);

        final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, WITHDRAWN_ON_DATE);
        final String withdrawnByUsername = rs.getString(WITHDRAWN_BY_USERNAME);
        final String withdrawnByFirstname = rs.getString(WITHDRAWN_BY_FIRSTNAME);
        final String withdrawnByLastname = rs.getString(WITHDRAWN_BY_LASTNAME);

        final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, APPROVED_ON_DATE);
        final String approvedByUsername = rs.getString(APPROVED_BY_USERNAME);
        final String approvedByFirstname = rs.getString(APPROVED_BY_FIRSTNAME);
        final String approvedByLastname = rs.getString(APPROVED_BY_LASTNAME);

        final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, ACTIVATED_ON_DATE);
        final String activatedByUsername = rs.getString(ACTIVATED_BY_USERNAME);
        final String activatedByFirstname = rs.getString(ACTIVATED_BY_FIRSTNAME);
        final String activatedByLastname = rs.getString(ACTIVATED_BY_LASTNAME);

        final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, CLOSED_ON_DATE);
        final String closedByUsername = rs.getString(CLOSED_BY_USERNAME);
        final String closedByFirstname = rs.getString(CLOSED_BY_FIRSTNAME);
        final String closedByLastname = rs.getString(CLOSED_BY_LASTNAME);

        final SavingsAccountApplicationTimelineData timeline = new SavingsAccountApplicationTimelineData(submittedOnDate,
                submittedByUsername, submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname,
                rejectedByLastname, withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname, withdrawnByLastname, approvedOnDate,
                approvedByUsername, approvedByFirstname, approvedByLastname, activatedOnDate, activatedByUsername, activatedByFirstname,
                activatedByLastname, closedOnDate, closedByUsername, closedByFirstname, closedByLastname);

        final Integer depositTypeId = JdbcSupport.getInteger(rs, DEPOSIT_TYPE_ID);
        final EnumOptionData depositType = (depositTypeId == null) ? null : SavingsEnumerations.depositType(depositTypeId);

        final String currencyCode = rs.getString(CURRENCY_CODE);
        final String currencyName = rs.getString(CURRENCY_NAME);
        final String currencyNameCode = rs.getString(CURRENCY_NAME_CODE);
        final String currencyDisplaySymbol = rs.getString(CURRENCY_DISPLAY_SYMBOL);
        final Integer currencyDigits = JdbcSupport.getInteger(rs, CURRENCY_DIGITS);
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, IN_MULTIPLES_OF);
        final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        final BigDecimal nominalAnnualInterestRate = rs.getBigDecimal(NOMINAL_ANNUAL_INTEREST_RATE);

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

        final BigDecimal totalDeposits = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_DEPOSITS);
        final BigDecimal totalWithdrawals = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_WITHDRAWALS);
        final BigDecimal totalWithdrawalFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_WITHDRAWAL_FEES);
        final BigDecimal totalAnnualFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_ANNUAL_FEES);

        final BigDecimal totalInterestEarned = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_INTEREST_EARNED);
        final BigDecimal totalInterestPosted = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_INTEREST_POSTED);
        final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, ACCOUNT_BALANCE);
        final BigDecimal totalFeeCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_FEE_CHARGE);
        final BigDecimal totalPenaltyCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_PENALTY_CHARGE);
        final BigDecimal totalWithholdTax = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, TOTAL_WITHHOLD_TAX);
        final BigDecimal totalOverdraftInterestDerived = null;
        final LocalDate interestPostedTillDate = JdbcSupport.getLocalDate(rs, INTEREST_POSTED_TILL_DATE);

        final boolean withHoldTax = rs.getBoolean(WITH_HOLD_TAX);
        final Long taxGroupId = JdbcSupport.getLong(rs, TAX_GROUP_ID);
        final String taxGroupName = rs.getString(TAX_GROUP_NAME);
        TaxGroupData taxGroupData = null;
        if (taxGroupId != null) {
            taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
        }

        final BigDecimal availableBalance = null;
        final SavingsAccountSummaryData summary = new SavingsAccountSummaryData(currency, totalDeposits, totalWithdrawals,
                totalWithdrawalFees, totalAnnualFees, totalInterestEarned, totalInterestPosted, accountBalance, totalFeeCharge,
                totalPenaltyCharge, totalOverdraftInterestDerived, totalWithholdTax, null, null, availableBalance, interestPostedTillDate);

        return DepositAccountData.instance(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, depositType,
                minBalanceForInterestCalculation, withHoldTax, taxGroupData);
    }
}
