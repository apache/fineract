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
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import org.apache.fineract.portfolio.charge.service.ChargeDropdownReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeEnumerations;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsAccountAnnualFeeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class SavingsAccountChargeReadPlatformServiceImpl implements SavingsAccountChargeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;
    private final DropdownReadPlatformService dropdownReadPlatformService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    // mappers
    private final SavingsAccountChargeDueMapper chargeDueMapper;

    public SavingsAccountChargeReadPlatformServiceImpl(final PlatformSecurityContext context,
            final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, final JdbcTemplate jdbcTemplate,
            final DropdownReadPlatformService dropdownReadPlatformService, DatabaseSpecificSQLGenerator sqlGenerator) {
        this.context = context;
        this.chargeDropdownReadPlatformService = chargeDropdownReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
        this.sqlGenerator = sqlGenerator;
        this.chargeDueMapper = new SavingsAccountChargeDueMapper();
        this.dropdownReadPlatformService = dropdownReadPlatformService;
    }

    private static final class SavingsAccountChargeMapper implements RowMapper<SavingsAccountChargeData> {

        public String schema() {
            return "sc.id as id, c.id as chargeId, sc.savings_account_id as accountId, c.name as name, " + "sc.amount as amountDue, "
                    + "sc.amount_paid_derived as amountPaid, " + "sc.amount_waived_derived as amountWaived, "
                    + "sc.amount_writtenoff_derived as amountWrittenOff, " + "sc.amount_outstanding_derived as amountOutstanding, "
                    + "sc.calculation_percentage as percentageOf, sc.calculation_on_amount as amountPercentageAppliedTo, "
                    + "sc.charge_time_enum as chargeTime, " + "sc.is_penalty as penalty, " + "sc.charge_due_date as dueAsOfDate, "
                    + "sc.fee_on_month as feeOnMonth, " + "sc.fee_on_day as feeOnDay, sc.fee_interval as feeInterval, "
                    + "sc.charge_calculation_enum as chargeCalculation, "
                    + "sc.is_active as isActive, sc.inactivated_on_date as inactivationDate, "
                    + "c.is_free_withdrawal as isFreeWithdrawal, c.free_withdrawal_charge_frequency as freeWithdrawalChargeFrequency, c.restart_frequency as restartFrequency, c.restart_frequency_enum as restartFrequencyEnum, "
                    + "c.currency_code as currencyCode, oc.name as currencyName, "
                    + "oc.decimal_places as currencyDecimalPlaces, oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    + "oc.internationalized_name_code as currencyNameCode from m_charge c "
                    + "join m_organisation_currency oc on c.currency_code = oc.code "
                    + "join m_savings_account_charge sc on sc.charge_id = c.id ";
        }

        @Override
        public SavingsAccountChargeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long chargeId = rs.getLong("chargeId");
            final Long accountId = rs.getLong("accountId");
            final String name = rs.getString("name");
            final BigDecimal amount = rs.getBigDecimal("amountDue");
            final BigDecimal amountPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountPaid");
            final BigDecimal amountWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountWaived");
            final BigDecimal amountWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountWrittenOff");
            final BigDecimal amountOutstanding = rs.getBigDecimal("amountOutstanding");

            final BigDecimal percentageOf = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "percentageOf");
            final BigDecimal amountPercentageAppliedTo = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amountPercentageAppliedTo");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final int chargeTime = rs.getInt("chargeTime");
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            final LocalDate dueAsOfDate = JdbcSupport.getLocalDate(rs, "dueAsOfDate");
            final Integer feeInterval = JdbcSupport.getInteger(rs, "feeInterval");
            MonthDay feeOnMonthDay = null;
            final Integer feeOnMonth = JdbcSupport.getInteger(rs, "feeOnMonth");
            final Integer feeOnDay = JdbcSupport.getInteger(rs, "feeOnDay");
            if (feeOnDay != null && feeOnMonth != null) {
                feeOnMonthDay = MonthDay.now(DateUtils.getDateTimeZoneOfTenant()).withMonth(feeOnMonth).withDayOfMonth(feeOnDay);
            }

            final int chargeCalculation = rs.getInt("chargeCalculation");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);
            final boolean penalty = rs.getBoolean("penalty");
            final Boolean isActive = rs.getBoolean("isActive");
            final LocalDate inactivationDate = JdbcSupport.getLocalDate(rs, "inactivationDate");

            final Boolean isFreeWithdrawal = rs.getBoolean("isFreeWithdrawal");
            final Integer freeWithdrawalChargeFrequency = JdbcSupport.getInteger(rs, "freeWithdrawalChargeFrequency");
            final Integer restartFrequency = JdbcSupport.getInteger(rs, "restartFrequency");
            final Integer restartFrequencyEnum = JdbcSupport.getInteger(rs, "restartFrequencyEnum");

            final Collection<ChargeData> chargeOptions = null;

            return SavingsAccountChargeData.instance(id, chargeId, accountId, name, currency, amount, amountPaid, amountWaived,
                    amountWrittenOff, amountOutstanding, chargeTimeType, dueAsOfDate, chargeCalculationType, percentageOf,
                    amountPercentageAppliedTo, chargeOptions, penalty, feeOnMonthDay, feeInterval, isActive, isFreeWithdrawal,
                    freeWithdrawalChargeFrequency, restartFrequency, restartFrequencyEnum, inactivationDate);
        }
    }

    @Override
    public ChargeData retrieveSavingsAccountChargeTemplate() {
        this.context.authenticatedUser();

        final List<EnumOptionData> allowedChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService.retrieveCalculationTypes();
        final List<EnumOptionData> allowedChargeTimeOptions = this.chargeDropdownReadPlatformService.retrieveCollectionTimeTypes();
        final List<EnumOptionData> loansChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveLoanCalculationTypes();
        final List<EnumOptionData> loansChargeTimeTypeOptions = this.chargeDropdownReadPlatformService.retrieveLoanCollectionTimeTypes();
        final List<EnumOptionData> savingsChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSavingsCalculationTypes();
        final List<EnumOptionData> savingsChargeTimeTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSavingsCollectionTimeTypes();

        final List<EnumOptionData> feeFrequencyOptions = this.dropdownReadPlatformService.retrievePeriodFrequencyTypeOptions();
        // other fields is applicable only for client charges

        // TODO AA : revisit for merge conflict - Not sure method signature
        return ChargeData.builder().chargeCalculationTypeOptions(allowedChargeCalculationTypeOptions)
                .chargeTimeTypeOptions(allowedChargeTimeOptions).loanChargeCalculationTypeOptions(loansChargeCalculationTypeOptions)
                .loanChargeTimeTypeOptions(loansChargeTimeTypeOptions)
                .savingsChargeCalculationTypeOptions(savingsChargeCalculationTypeOptions)
                .savingsChargeTimeTypeOptions(savingsChargeTimeTypeOptions).feeFrequencyOptions(feeFrequencyOptions).build();
    }

    @Override
    public SavingsAccountChargeData retrieveSavingsAccountChargeDetails(final Long id, final Long savingsAccountId) {
        try {
            this.context.authenticatedUser();

            final SavingsAccountChargeMapper rm = new SavingsAccountChargeMapper();

            final String sql = "select " + rm.schema() + " where sc.id=? and sc.savings_account_id=?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { id, savingsAccountId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountChargeNotFoundException(savingsAccountId, e);
        }
    }

    @Override
    public Collection<SavingsAccountChargeData> retrieveSavingsAccountCharges(final Long loanId, final String status) {
        this.context.authenticatedUser();

        final SavingsAccountChargeMapper rm = new SavingsAccountChargeMapper();
        final StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select ").append(rm.schema()).append(" where sc.savings_account_id=? ");
        if (status.equalsIgnoreCase("active")) {
            sqlBuilder.append(" and sc.is_active = true ");
        } else if (status.equalsIgnoreCase("inactive")) {
            sqlBuilder.append(" and sc.is_active = false ");
        }
        sqlBuilder.append(" order by sc.charge_time_enum ASC, sc.charge_due_date ASC, sc.is_penalty ASC");

        return this.jdbcTemplate.query(sqlBuilder.toString(), rm, new Object[] { loanId });
    }

    private static final class SavingsAccountChargeDueMapper implements RowMapper<SavingsAccountAnnualFeeData> {

        private final String schemaSql;

        SavingsAccountChargeDueMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("sac.id as id, ");
            sqlBuilder.append("sa.id as accountId, ");
            sqlBuilder.append("sa.account_no as accountNo, ");
            sqlBuilder.append("sac.charge_due_date as dueDate ");
            sqlBuilder.append("from m_savings_account_charge sac join m_savings_account sa on sac.savings_account_id = sa.id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountAnnualFeeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long accountId = rs.getLong("accountId");
            final String accountNo = rs.getString("accountNo");
            final LocalDate annualFeeNextDueDate = JdbcSupport.getLocalDate(rs, "dueDate");

            return SavingsAccountAnnualFeeData.instance(id, accountId, accountNo, annualFeeNextDueDate);
        }
    }

    @Override
    public Collection<SavingsAccountAnnualFeeData> retrieveChargesWithAnnualFeeDue() {
        final String sql = "select " + this.chargeDueMapper.schema()
                + " where sac.charge_due_date is not null and sac.charge_time_enum = ? " + " and sac.charge_due_date <= "
                + sqlGenerator.currentBusinessDate() + " and sa.status_enum = ? ";

        return this.jdbcTemplate.query(sql, this.chargeDueMapper, // NOSONAR
                new Object[] { ChargeTimeType.ANNUAL_FEE.getValue(), SavingsAccountStatusType.ACTIVE.getValue() });
    }

    @Override
    public Collection<SavingsAccountAnnualFeeData> retrieveChargesWithDue() {
        final String sql = "select " + this.chargeDueMapper.schema()
                + " where sac.charge_due_date is not null and sac.charge_due_date <= ? and sac.waived = false and sac.is_paid_derived=false and sac.is_active=true and sa.status_enum = ? "
                + " order by sac.charge_due_date ";

        return this.jdbcTemplate.query(sql, this.chargeDueMapper, // NOSONAR
                new Object[] { DateUtils.getBusinessLocalDate(), SavingsAccountStatusType.ACTIVE.getValue() }); // NOSONAR
    }

}
