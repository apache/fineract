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
package org.apache.fineract.portfolio.charge.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.MonthDay;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainServiceJpa;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityType;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeAppliesTo;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.ChargeNotFoundException;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author vishwas
 *
 */
@RequiredArgsConstructor
public class ChargeReadPlatformServiceImpl implements ChargeReadPlatformService {

    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final ChargeDropdownReadPlatformService chargeDropdownReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DropdownReadPlatformService dropdownReadPlatformService;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final TaxReadPlatformService taxReadPlatformService;
    private final ConfigurationDomainServiceJpa configurationDomainServiceJpa;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    @Cacheable(value = "charges", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('ch')")
    public Collection<ChargeData> retrieveAllCharges() {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false ";

        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveAllChargesForCurrency(String currencyCode) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.currency_code= ? ";

        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { currencyCode }); // NOSONAR
    }

    @Override
    public ChargeData retrieveCharge(final Long chargeId) {
        try {
            final ChargeMapper rm = new ChargeMapper();

            String sql = "select " + rm.chargeSchema() + " where c.id = ? and c.is_deleted=false ";

            sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

            sql = sql + " ;";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { chargeId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new ChargeNotFoundException(chargeId, e);
        }
    }

    @Override
    public ChargeData retrieveNewChargeDetails() {

        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> allowedChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService.retrieveCalculationTypes();
        final List<EnumOptionData> allowedChargeAppliesToOptions = this.chargeDropdownReadPlatformService.retrieveApplicableToTypes();
        final List<EnumOptionData> allowedChargeTimeOptions = this.chargeDropdownReadPlatformService.retrieveCollectionTimeTypes();
        final List<EnumOptionData> chargePaymentOptions = this.chargeDropdownReadPlatformService.retrivePaymentModes();
        final List<EnumOptionData> loansChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveLoanCalculationTypes();
        final List<EnumOptionData> loansChargeTimeTypeOptions = this.chargeDropdownReadPlatformService.retrieveLoanCollectionTimeTypes();
        final List<EnumOptionData> savingsChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSavingsCalculationTypes();
        final List<EnumOptionData> savingsChargeTimeTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSavingsCollectionTimeTypes();
        final List<EnumOptionData> clientChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveClientCalculationTypes();
        final List<EnumOptionData> clientChargeTimeTypeOptions = this.chargeDropdownReadPlatformService.retrieveClientCollectionTimeTypes();
        final List<EnumOptionData> feeFrequencyOptions = this.dropdownReadPlatformService.retrievePeriodFrequencyTypeOptions();
        final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForCharges();
        final List<EnumOptionData> shareChargeCalculationTypeOptions = this.chargeDropdownReadPlatformService
                .retrieveSharesCalculationTypes();
        final List<EnumOptionData> shareChargeTimeTypeOptions = this.chargeDropdownReadPlatformService.retrieveSharesCollectionTimeTypes();
        final Collection<TaxGroupData> taxGroupOptions = this.taxReadPlatformService.retrieveTaxGroupsForLookUp();
        final String accountMappingForChargeConfig = this.configurationDomainServiceJpa.getAccountMappingForCharge();
        final List<GLAccountData> expenseAccountOptions = this.accountingDropdownReadPlatformService.retrieveExpenseAccountOptions();
        final List<GLAccountData> assetAccountOptions = this.accountingDropdownReadPlatformService.retrieveAssetAccountOptions();

        return ChargeData.builder().currencyOptions(currencyOptions).chargeCalculationTypeOptions(allowedChargeCalculationTypeOptions)
                .chargeAppliesToOptions(allowedChargeAppliesToOptions).chargeTimeTypeOptions(allowedChargeTimeOptions)
                .chargePaymetModeOptions(chargePaymentOptions).loanChargeCalculationTypeOptions(loansChargeCalculationTypeOptions)
                .loanChargeTimeTypeOptions(loansChargeTimeTypeOptions)
                .savingsChargeCalculationTypeOptions(savingsChargeCalculationTypeOptions)
                .savingsChargeTimeTypeOptions(savingsChargeTimeTypeOptions)
                .clientChargeCalculationTypeOptions(clientChargeCalculationTypeOptions)
                .clientChargeTimeTypeOptions(clientChargeTimeTypeOptions).feeFrequencyOptions(feeFrequencyOptions)
                .incomeOrLiabilityAccountOptions(incomeOrLiabilityAccountOptions).taxGroupOptions(taxGroupOptions)
                .shareChargeCalculationTypeOptions(shareChargeCalculationTypeOptions).shareChargeTimeTypeOptions(shareChargeTimeTypeOptions)
                .accountMappingForChargeConfig(accountMappingForChargeConfig).expenseAccountOptions(expenseAccountOptions)
                .assetAccountOptions(assetAccountOptions).build();
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductCharges(final Long loanProductId) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.loanProductChargeSchema() + " where c.is_deleted=false and c.is_active=true and plc.product_loan_id=? ";

        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductCharges(final Long loanProductId, final ChargeTimeType chargeTime) {

        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.loanProductChargeSchema()
                + " where c.is_deleted=false and c.is_active=true and plc.product_loan_id=? and c.charge_time_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanProductId, chargeTime.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicableFees() {
        final ChargeMapper rm = new ChargeMapper();
        Object[] params = new Object[] { ChargeAppliesTo.LOAN.getValue() };
        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=false and c.is_active=true and c.is_penalty=false and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, params); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveLoanAccountApplicableCharges(final Long loanId, ChargeTimeType[] excludeChargeTimes) {
        final ChargeMapper rm = new ChargeMapper();
        StringBuilder excludeClause = new StringBuilder("");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("loanId", loanId);
        paramMap.put("chargeAppliesTo", ChargeAppliesTo.LOAN.getValue());
        processChargeExclusionsForLoans(excludeChargeTimes, excludeClause);
        String sql = "select " + rm.chargeSchema() + " join m_loan la on la.currency_code = c.currency_code" + " where la.id=:loanId"
                + " and c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=:chargeAppliesTo" + excludeClause + " ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";
        return this.namedParameterJdbcTemplate.query(sql, paramMap, rm);
    }

    /**
     * @param excludeChargeTimes
     * @param excludeClause
     * @param params
     * @return
     */
    private void processChargeExclusionsForLoans(ChargeTimeType[] excludeChargeTimes, StringBuilder excludeClause) {
        if (excludeChargeTimes != null && excludeChargeTimes.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < excludeChargeTimes.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(excludeChargeTimes[i].getValue());
            }
            excludeClause = excludeClause.append(" and c.charge_time_enum not in(" + sb.toString() + ") ");
            excludeClause.append(" ");
        }
    }

    @Override
    public Collection<ChargeData> retrieveLoanProductApplicableCharges(final Long loanProductId, ChargeTimeType[] excludeChargeTimes) {
        final ChargeMapper rm = new ChargeMapper();
        StringBuilder excludeClause = new StringBuilder("");
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("productId", loanProductId);
        paramMap.put("chargeAppliesTo", ChargeAppliesTo.LOAN.getValue());
        processChargeExclusionsForLoans(excludeChargeTimes, excludeClause);
        String sql = "select " + rm.chargeSchema() + " join m_product_loan lp on lp.currency_code = c.currency_code"
                + " where lp.id=:productId" + " and c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=:chargeAppliesTo"
                + excludeClause + " ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.namedParameterJdbcTemplate.query(sql, paramMap, rm);
    }

    @Override
    public Collection<ChargeData> retrieveLoanApplicablePenalties() {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=false and c.is_active=true and c.is_penalty=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";
        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.LOAN.getValue() }); // NOSONAR
    }

    private String addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled() {

        String sql = "";

        // Check if branch specific products are enabled. If yes, fetch only
        // charges mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.CHARGE);
        if ((inClause != null) && !inClause.trim().isEmpty()) {
            sql += " and c.id in ( " + inClause + " ) ";
        }

        return sql;
    }

    private static final class ChargeMapper implements RowMapper<ChargeData> {

        public String chargeSchema() {
            return "c.id as id, c.name as name, c.amount as amount, c.currency_code as currencyCode, "
                    + "c.charge_applies_to_enum as chargeAppliesTo, c.charge_time_enum as chargeTime, "
                    + "c.charge_payment_mode_enum as chargePaymentMode, "
                    + "c.charge_calculation_enum as chargeCalculation, c.is_penalty as penalty, "
                    + "c.is_active as active, c.is_free_withdrawal as isFreeWithdrawal, c.free_withdrawal_charge_frequency as freeWithdrawalChargeFrequency, c.restart_frequency as restartFrequency, c.restart_frequency_enum as restartFrequencyEnum,"
                    + "oc.name as currencyName, oc.decimal_places as currencyDecimalPlaces, "
                    + "oc.currency_multiplesof as inMultiplesOf, oc.display_symbol as currencyDisplaySymbol, "
                    + "oc.internationalized_name_code as currencyNameCode, c.fee_on_day as feeOnDay, c.fee_on_month as feeOnMonth, "
                    + "c.fee_interval as feeInterval, c.fee_frequency as feeFrequency,c.min_cap as minCap,c.max_cap as maxCap, "
                    + "c.income_or_liability_account_id as glAccountId , acc.name as glAccountName, acc.gl_code as glCode, "
                    + "tg.id as taxGroupId, c.is_payment_type as isPaymentType, pt.id as paymentTypeId, pt.value as paymentTypeName, tg.name as taxGroupName "
                    + "from m_charge c " + "join m_organisation_currency oc on c.currency_code = oc.code "
                    + " LEFT JOIN acc_gl_account acc on acc.id = c.income_or_liability_account_id "
                    + " LEFT JOIN m_tax_group tg on tg.id = c.tax_group_id " + " LEFT JOIN m_payment_type pt on pt.id = c.payment_type_id ";
        }

        public String loanProductChargeSchema() {
            return chargeSchema() + " join m_product_loan_charge plc on plc.charge_id = c.id";
        }

        public String savingsProductChargeSchema() {
            return chargeSchema() + " join m_savings_product_charge spc on spc.charge_id = c.id";
        }

        public String shareProductChargeSchema() {
            return chargeSchema() + " join m_share_product_charge mspc on mspc.charge_id = c.id";
        }

        @Override
        public ChargeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final BigDecimal amount = rs.getBigDecimal("amount");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDecimalPlaces = JdbcSupport.getInteger(rs, "currencyDecimalPlaces");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDecimalPlaces, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final int chargeAppliesTo = rs.getInt("chargeAppliesTo");
            final EnumOptionData chargeAppliesToType = ChargeEnumerations.chargeAppliesTo(chargeAppliesTo);

            final int chargeTime = rs.getInt("chargeTime");
            final EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(chargeTime);

            final int chargeCalculation = rs.getInt("chargeCalculation");
            final EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(chargeCalculation);

            final int paymentMode = rs.getInt("chargePaymentMode");
            final EnumOptionData chargePaymentMode = ChargeEnumerations.chargePaymentMode(paymentMode);

            final boolean penalty = rs.getBoolean("penalty");
            final boolean active = rs.getBoolean("active");

            final Integer feeInterval = JdbcSupport.getInteger(rs, "feeInterval");
            EnumOptionData feeFrequencyType = null;
            final Integer feeFrequency = JdbcSupport.getInteger(rs, "feeFrequency");
            if (feeFrequency != null) {
                feeFrequencyType = CommonEnumerations.termFrequencyType(feeFrequency, "feeFrequency");
            }
            MonthDay feeOnMonthDay = null;
            final Integer feeOnMonth = JdbcSupport.getInteger(rs, "feeOnMonth");
            final Integer feeOnDay = JdbcSupport.getInteger(rs, "feeOnDay");
            if (feeOnDay != null && feeOnMonth != null) {
                feeOnMonthDay = MonthDay.now(DateUtils.getDateTimeZoneOfTenant()).withDayOfMonth(feeOnDay).withMonth(feeOnMonth);
            }
            final BigDecimal minCap = rs.getBigDecimal("minCap");
            final BigDecimal maxCap = rs.getBigDecimal("maxCap");

            // extract GL Account
            final Long glAccountId = JdbcSupport.getLong(rs, "glAccountId");
            final String glAccountName = rs.getString("glAccountName");
            final String glCode = rs.getString("glCode");
            GLAccountData glAccountData = null;
            if (glAccountId != null) {
                glAccountData = new GLAccountData().setId(glAccountId).setName(glAccountName).setGlCode(glCode);
            }

            final Long taxGroupId = JdbcSupport.getLong(rs, "taxGroupId");
            final String taxGroupName = rs.getString("taxGroupName");
            TaxGroupData taxGroupData = null;
            if (taxGroupId != null) {
                taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
            }

            final boolean isFreeWithdrawal = rs.getBoolean("isFreeWithdrawal");
            final int freeWithdrawalChargeFrequency = rs.getInt("freeWithdrawalChargeFrequency");
            final int restartFrequency = rs.getInt("restartFrequency");
            final int restartFrequencyEnum = rs.getInt("restartFrequencyEnum");

            final boolean isPaymentType = rs.getBoolean("isPaymentType");
            final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentTypeId");

            final String paymentTypeName = rs.getString("paymentTypeName");
            PaymentTypeData paymentTypeData = null;
            if (paymentTypeId != null) {
                paymentTypeData = PaymentTypeData.instance(paymentTypeId, paymentTypeName);
            }

            return ChargeData.builder().id(id).name(name).amount(amount).currency(currency).chargeTimeType(chargeTimeType)
                    .chargeAppliesTo(chargeAppliesToType).chargeCalculationType(chargeCalculationType).chargePaymentMode(chargePaymentMode)
                    .feeOnMonthDay(feeOnMonthDay).feeInterval(feeInterval).penalty(penalty).active(active).freeWithdrawal(isFreeWithdrawal)
                    .freeWithdrawalChargeFrequency(freeWithdrawalChargeFrequency).restartFrequency(restartFrequency)
                    .restartFrequencyEnum(restartFrequencyEnum).isPaymentType(isPaymentType).paymentTypeOptions(paymentTypeData)
                    .minCap(minCap).maxCap(maxCap).feeFrequency(feeFrequencyType).incomeOrLiabilityAccount(glAccountData)
                    .taxGroup(taxGroupData).build();

        }
    }

    @Override
    public Collection<ChargeData> retrieveSavingsProductApplicableCharges(final boolean feeChargesOnly) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? ";
        if (feeChargesOnly) {
            sql = "select " + rm.chargeSchema()
                    + " where c.is_deleted=false and c.is_active=true and c.is_penalty=false and c.charge_applies_to_enum=? ";
        }
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SAVINGS.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveSavingsApplicablePenalties() {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema()
                + " where c.is_deleted=false and c.is_active=true and c.is_penalty=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";
        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SAVINGS.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveSavingsProductCharges(final Long savingsProductId) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.savingsProductChargeSchema()
                + " where c.is_deleted=false and c.is_active=true and spc.savings_product_id=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { savingsProductId }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveShareProductCharges(final Long shareProductId) {
        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.shareProductChargeSchema() + " where c.is_deleted=false and c.is_active=true and mspc.product_id=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { shareProductId }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveSavingsAccountApplicableCharges(Long savingsAccountId) {

        final ChargeMapper rm = new ChargeMapper();

        String sql = "select " + rm.chargeSchema() + " join m_savings_account sa on sa.currency_code = c.currency_code"
                + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? " + " and sa.id = ?";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SAVINGS.getValue(), savingsAccountId }); // NOSONAR

    }

    @Override
    public Collection<ChargeData> retrieveAllChargesApplicableToClients() {
        final ChargeMapper rm = new ChargeMapper();
        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.CLIENT.getValue() }); // NOSONAR
    }

    @Override
    public Collection<ChargeData> retrieveSharesApplicableCharges() {
        final ChargeMapper rm = new ChargeMapper();
        String sql = "select " + rm.chargeSchema() + " where c.is_deleted=false and c.is_active=true and c.charge_applies_to_enum=? ";
        sql += addInClauseToSQL_toLimitChargesMappedToOffice_ifOfficeSpecificProductsEnabled();
        sql += " order by c.name ";

        return this.jdbcTemplate.query(sql, rm, new Object[] { ChargeAppliesTo.SHARES.getValue() }); // NOSONAR
    }
}
