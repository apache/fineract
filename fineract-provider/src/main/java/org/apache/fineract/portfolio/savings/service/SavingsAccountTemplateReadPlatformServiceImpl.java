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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.charge.util.ConvertChargeDataToSpecificChargeData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSubStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

public class SavingsAccountTemplateReadPlatformServiceImpl implements SavingsAccountTemplateReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final SavingsDropdownReadPlatformService dropdownReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;

    private final EntityDatatableChecksReadService entityDatatableChecksReadService;

    public SavingsAccountTemplateReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final ClientReadPlatformService clientReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final SavingsProductReadPlatformService savingProductReadPlatformService,
            final StaffReadPlatformService staffReadPlatformService, final SavingsDropdownReadPlatformService dropdownReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService,
            final EntityDatatableChecksReadService entityDatatableChecksReadService, final ColumnValidator columnValidator) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.savingsProductReadPlatformService = savingProductReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.entityDatatableChecksReadService = entityDatatableChecksReadService;
    }

    @Override
    public SavingsAccountData retrieveTemplate(final Long clientId, final Long groupId, final Long productId,
            final boolean staffInSelectedOfficeOnly) {

        final AppUser loggedInUser = this.context.authenticatedUser();
        Long officeId = loggedInUser.getOffice().getId();

        ClientData client = null;
        if (clientId != null) {
            client = this.clientReadPlatformService.retrieveOne(clientId);
            officeId = client.getOfficeId();
        }

        GroupGeneralData group = null;
        if (groupId != null) {
            group = this.groupReadPlatformService.retrieveOne(groupId);
            officeId = group.getOfficeId();
        }

        final Collection<SavingsProductData> productOptions = this.savingsProductReadPlatformService.retrieveAllForLookup();
        SavingsAccountData template = null;
        if (productId != null) {

            final SavingAccountTemplateMapper mapper = new SavingAccountTemplateMapper(client, group);

            final String sql = "select " + mapper.schema() + " where sp.id = ?";
            template = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { productId }); // NOSONAR

            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = this.dropdownReadPlatformService
                    .retrieveCompoundingInterestPeriodTypeOptions();

            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = this.dropdownReadPlatformService
                    .retrieveInterestPostingPeriodTypeOptions();

            final Collection<EnumOptionData> interestCalculationTypeOptions = this.dropdownReadPlatformService
                    .retrieveInterestCalculationTypeOptions();

            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = this.dropdownReadPlatformService
                    .retrieveInterestCalculationDaysInYearTypeOptions();

            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.dropdownReadPlatformService
                    .retrieveLockinPeriodFrequencyTypeOptions();

            final Collection<EnumOptionData> withdrawalFeeTypeOptions = this.dropdownReadPlatformService.retrievewithdrawalFeeTypeOptions();

            final Collection<SavingsAccountTransactionData> transactions = null;
            final Collection<ChargeData> productCharges = this.chargeReadPlatformService.retrieveSavingsProductCharges(productId);
            // update charges from Product charges
            final Collection<SavingsAccountChargeData> charges = fromChargesToSavingsCharges(productCharges);

            final boolean feeChargesOnly = false;
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService
                    .retrieveSavingsProductApplicableCharges(feeChargesOnly);

            Collection<StaffData> fieldOfficerOptions = null;

            if (officeId != null) {

                if (staffInSelectedOfficeOnly) {
                    // only bring back loan officers in selected branch/office
                    final Collection<StaffData> fieldOfficersInBranch = this.staffReadPlatformService
                            .retrieveAllLoanOfficersInOfficeById(officeId);

                    if (!CollectionUtils.isEmpty(fieldOfficersInBranch)) {
                        fieldOfficerOptions = new ArrayList<>(fieldOfficersInBranch);
                    }
                } else {
                    // by default bring back all officers in selected
                    // branch/office as well as officers in office above
                    // this office
                    final boolean restrictToLoanOfficersOnly = true;
                    final Collection<StaffData> loanOfficersInHierarchy = this.staffReadPlatformService
                            .retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeId, restrictToLoanOfficersOnly);

                    if (!CollectionUtils.isEmpty(loanOfficersInHierarchy)) {
                        fieldOfficerOptions = new ArrayList<>(loanOfficersInHierarchy);
                    }
                }
            }

            template = SavingsAccountData.withTemplateOptions(template, productOptions, fieldOfficerOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, transactions,
                    charges, chargeOptions);
        } else {

            String clientName = null;
            if (client != null) {
                clientName = client.getDisplayName();
            }

            String groupName = null;
            if (group != null) {
                groupName = group.getName();
            }

            template = SavingsAccountData.withClientTemplate(clientId, clientName, groupId, groupName);

            final Collection<StaffData> fieldOfficerOptions = null;
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
            final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

            final Collection<SavingsAccountTransactionData> transactions = null;
            final Collection<SavingsAccountChargeData> charges = null;

            final boolean feeChargesOnly = false;
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService
                    .retrieveSavingsProductApplicableCharges(feeChargesOnly);

            template = SavingsAccountData.withTemplateOptions(template, productOptions, fieldOfficerOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, transactions,
                    charges, chargeOptions);
        }

        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService.retrieveTemplates(StatusEnum.CREATE.getValue(),
                EntityTables.SAVINGS.getName(), productId);
        template.setDatatables(datatableTemplates);

        return template;
    }

    private static final class SavingAccountTemplateMapper implements RowMapper<SavingsAccountData> {

        private final ClientData client;
        private final GroupGeneralData group;

        private final String schemaSql;

        SavingAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
            this.client = client;
            this.group = group;

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append(
                    "sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, sp.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sp.nominal_annual_interest_rate as nominalAnnualIterestRate, ");
            sqlBuilder.append("sp.interest_compounding_period_enum as interestCompoundingPeriodType, ");
            sqlBuilder.append("sp.interest_posting_period_enum as interestPostingPeriodType, ");
            sqlBuilder.append("sp.interest_calculation_type_enum as interestCalculationType, ");
            sqlBuilder.append("sp.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
            sqlBuilder.append("sp.min_required_opening_balance as minRequiredOpeningBalance, ");
            sqlBuilder.append("sp.lockin_period_frequency as lockinPeriodFrequency,");
            sqlBuilder.append("sp.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
            // sqlBuilder.append("sp.withdrawal_fee_amount as
            // withdrawalFeeAmount,");
            // sqlBuilder.append("sp.withdrawal_fee_type_enum as
            // withdrawalFeeTypeEnum, ");
            sqlBuilder.append("sp.withdrawal_fee_for_transfer as withdrawalFeeForTransfers, ");
            sqlBuilder.append("sp.min_balance_for_interest_calculation as minBalanceForInterestCalculation, ");
            sqlBuilder.append("sp.allow_overdraft as allowOverdraft, ");
            sqlBuilder.append("sp.overdraft_limit as overdraftLimit, ");
            sqlBuilder.append("sp.nominal_annual_interest_rate_overdraft as nominalAnnualInterestRateOverdraft, ");
            sqlBuilder.append("sp.min_overdraft_for_interest_calculation as minOverdraftForInterestCalculation, ");
            sqlBuilder.append("sp.withhold_tax as withHoldTax,");
            sqlBuilder.append("tg.id as taxGroupId, tg.name as taxGroupName, ");

            // sqlBuilder.append("sp.annual_fee_amount as annualFeeAmount,");
            // sqlBuilder.append("sp.annual_fee_on_month as annualFeeOnMonth,
            // ");
            // sqlBuilder.append("sp.annual_fee_on_day as annualFeeOnDay ");
            sqlBuilder.append("sp.min_required_balance as minRequiredBalance, ");
            sqlBuilder.append("sp.enforce_min_required_balance as enforceMinRequiredBalance, ");
            sqlBuilder.append("sp.max_allowed_lien_limit as maxAllowedLienLimit, ");
            sqlBuilder.append("sp.is_lien_allowed as lienAllowed ");
            sqlBuilder.append("from m_savings_product sp ");
            sqlBuilder.append("join m_currency curr on curr.code = sp.currency_code ");
            sqlBuilder.append("left join m_tax_group tg on tg.id = sp.tax_group_id  ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

            final BigDecimal nominalAnnualIterestRate = rs.getBigDecimal("nominalAnnualIterestRate");

            final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations.compoundingInterestPeriodType(
                    SavingsCompoundingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs, "interestCompoundingPeriodType")));

            final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(
                    SavingsPostingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs, "interestPostingPeriodType")));

            final EnumOptionData interestCalculationType = SavingsEnumerations
                    .interestCalculationType(SavingsInterestCalculationType.fromInt(JdbcSupport.getInteger(rs, "interestCalculationType")));

            final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations.interestCalculationDaysInYearType(
                    SavingsInterestCalculationDaysInYearType.fromInt(JdbcSupport.getInteger(rs, "interestCalculationDaysInYearType")));

            final BigDecimal minRequiredOpeningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredOpeningBalance");

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                final SavingsPeriodFrequencyType lockinPeriodType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
                lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodType);
            }

            // final BigDecimal withdrawalFeeAmount =
            // rs.getBigDecimal("withdrawalFeeAmount");

            /*
             * EnumOptionData withdrawalFeeType = null; final Integer withdrawalFeeTypeValue =
             * JdbcSupport.getInteger(rs, "withdrawalFeeTypeEnum"); if (withdrawalFeeTypeValue != null) {
             * withdrawalFeeType = SavingsEnumerations.withdrawalFeeType(withdrawalFeeTypeValue); }
             */
            final boolean withdrawalFeeForTransfers = rs.getBoolean("withdrawalFeeForTransfers");

            final boolean allowOverdraft = rs.getBoolean("allowOverdraft");
            final BigDecimal overdraftLimit = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "overdraftLimit");
            final BigDecimal nominalAnnualInterestRateOverdraft = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "nominalAnnualInterestRateOverdraft");
            final BigDecimal minOverdraftForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "minOverdraftForInterestCalculation");

            final BigDecimal minRequiredBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredBalance");
            final boolean enforceMinRequiredBalance = rs.getBoolean("enforceMinRequiredBalance");
            final BigDecimal maxAllowedLienLimit = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maxAllowedLienLimit");
            final boolean lienAllowed = rs.getBoolean("lienAllowed");
            final BigDecimal minBalanceForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "minBalanceForInterestCalculation");

            // final BigDecimal annualFeeAmount =
            // JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
            // "annualFeeAmount");

            /*
             * MonthDay annualFeeOnMonthDay = null; final Integer annualFeeOnMonth = JdbcSupport.getInteger(rs,
             * "annualFeeOnMonth"); final Integer annualFeeOnDay = JdbcSupport.getInteger(rs, "annualFeeOnDay"); if
             * (annualFeeAmount != null && annualFeeOnDay != null) { annualFeeOnMonthDay = new
             * MonthDay(annualFeeOnMonth, annualFeeOnDay); }
             */

            final boolean withHoldTax = rs.getBoolean("withHoldTax");
            final Long taxGroupId = JdbcSupport.getLong(rs, "taxGroupId");
            final String taxGroupName = rs.getString("taxGroupName");
            TaxGroupData taxGroupData = null;
            if (taxGroupId != null) {
                taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
            }

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
            // final LocalDate annualFeeNextDueDate = null;
            final SavingsAccountSummaryData summary = null;
            final BigDecimal onHoldFunds = null;
            final BigDecimal savingsAmountOnHold = null;

            final SavingsAccountSubStatusEnumData subStatus = null;
            final String reasonForBlock = null;
            final LocalDate lastActiveTransactionDate = null;
            final boolean isDormancyTrackingActive = false;
            final Integer daysToInactive = null;
            final Integer daysToDormancy = null;
            final Integer daysToEscheat = null;

            final SavingsAccountApplicationTimelineData timeline = SavingsAccountApplicationTimelineData.templateDefault();
            final EnumOptionData depositType = null;
            return SavingsAccountData.instance(null, null, depositType, null, groupId, groupName, clientId, clientName, productId,
                    productName, fieldOfficerId, fieldOfficerName, status, subStatus, reasonForBlock, timeline, currency,
                    nominalAnnualIterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                    interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType,
                    withdrawalFeeForTransfers, summary, allowOverdraft, overdraftLimit, minRequiredBalance, enforceMinRequiredBalance,
                    maxAllowedLienLimit, lienAllowed, minBalanceForInterestCalculation, onHoldFunds, nominalAnnualInterestRateOverdraft,
                    minOverdraftForInterestCalculation, withHoldTax, taxGroupData, lastActiveTransactionDate, isDormancyTrackingActive,
                    daysToInactive, daysToDormancy, daysToEscheat, savingsAmountOnHold);
        }
    }

    private Collection<SavingsAccountChargeData> fromChargesToSavingsCharges(final Collection<ChargeData> productCharges) {
        final Collection<SavingsAccountChargeData> savingsCharges = new ArrayList<>();
        for (final ChargeData chargeData : productCharges) {
            final SavingsAccountChargeData savingsCharge = ConvertChargeDataToSpecificChargeData.toSavingsAccountChargeData(chargeData);
            savingsCharges.add(savingsCharge);
        }
        return savingsCharges;
    }

}
