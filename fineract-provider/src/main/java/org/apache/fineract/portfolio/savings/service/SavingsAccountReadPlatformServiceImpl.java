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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.DepositAccountType;
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
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountSubStatusEnum;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SavingsAccountReadPlatformServiceImpl implements SavingsAccountReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final SavingsDropdownReadPlatformService dropdownReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
	private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    // mappers
    private final SavingsAccountTransactionTemplateMapper transactionTemplateMapper;
    private final SavingsAccountTransactionsMapper transactionsMapper;
    private final SavingAccountMapper savingAccountMapper;
    // private final SavingsAccountAnnualFeeMapper annualFeeMapper;

    // pagination
    private final PaginationHelper<SavingsAccountData> paginationHelper = new PaginationHelper<>();

    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final ColumnValidator columnValidator;

    @Autowired
    public SavingsAccountReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final SavingsProductReadPlatformService savingProductReadPlatformService,
            final StaffReadPlatformService staffReadPlatformService, final SavingsDropdownReadPlatformService dropdownReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService,
            final EntityDatatableChecksReadService entityDatatableChecksReadService, final ColumnValidator columnValidator) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.savingsProductReadPlatformService = savingProductReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.transactionTemplateMapper = new SavingsAccountTransactionTemplateMapper();
        this.transactionsMapper = new SavingsAccountTransactionsMapper();
        this.savingAccountMapper = new SavingAccountMapper();
        // this.annualFeeMapper = new SavingsAccountAnnualFeeMapper();
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.entityDatatableChecksReadService = entityDatatableChecksReadService;
        this.columnValidator = columnValidator;
    }

    @Override
    public Collection<SavingsAccountData> retrieveAllForLookup(final Long clientId) {

        final StringBuilder sqlBuilder = new StringBuilder("select " + this.savingAccountMapper.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300 ");

        final Object[] queryParameters = new Object[] { clientId };
        return this.jdbcTemplate.query(sqlBuilder.toString(), this.savingAccountMapper, queryParameters);
    }

    @Override
    public Collection<SavingsAccountData> retrieveActiveForLookup(final Long clientId, DepositAccountType depositAccountType) {

        final StringBuilder sqlBuilder = new StringBuilder("select " + this.savingAccountMapper.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300 and sa.deposit_type_enum = ? ");

        final Object[] queryParameters = new Object[] { clientId, depositAccountType.getValue() };
        return this.jdbcTemplate.query(sqlBuilder.toString(), this.savingAccountMapper, queryParameters);
    }

    @Override
    public Collection<SavingsAccountData> retrieveActiveForLookup(final Long clientId, DepositAccountType depositAccountType, String currencyCode) {
        final StringBuilder sqlBuilder = new StringBuilder("select " + this.savingAccountMapper.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300 and sa.deposit_type_enum = ? and sa.currency_code = ? ");

        final Object[] queryParameters = new Object[] { clientId, depositAccountType.getValue(), currencyCode };
        return this.jdbcTemplate.query(sqlBuilder.toString(), this.savingAccountMapper, queryParameters);
    }
    
    @Override
    public Page<SavingsAccountData> retrieveAll(final SearchParameters searchParameters) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.savingAccountMapper.schema());

        sqlBuilder.append(" join m_office o on o.id = c.office_id");
        sqlBuilder.append(" where o.hierarchy like ?");

        final Object[] objectArray = new Object[2];
        objectArray[0] = hierarchySearchString;
        int arrayPos = 1;

        String sqlQueryCriteria = searchParameters.getSqlSearch();
        if (StringUtils.isNotBlank(sqlQueryCriteria)) {
            sqlQueryCriteria = sqlQueryCriteria.replaceAll("accountNo", "sa.account_no");
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), sqlQueryCriteria);
            sqlBuilder.append(" and (").append(sqlQueryCriteria).append(")");
        }

        if (StringUtils.isNotBlank(searchParameters.getExternalId())) {
            sqlBuilder.append(" and sa.external_id = ?");
            objectArray[arrayPos] = searchParameters.getExternalId();
            arrayPos = arrayPos + 1;
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray,
                this.savingAccountMapper);
    }

    @Override
    public SavingsAccountData retrieveOne(final Long accountId) {

        try {
            final String sql = "select " + this.savingAccountMapper.schema() + " where sa.id = ?";

            return this.jdbcTemplate.queryForObject(sql, this.savingAccountMapper, new Object[] { accountId });
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }

    private static final class SavingAccountMapper implements RowMapper<SavingsAccountData> {

        private final String schemaSql;

        public SavingAccountMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sa.id as id, sa.account_no as accountNo, sa.external_id as externalId, ");
            sqlBuilder.append("sa.deposit_type_enum as depositType, ");
            sqlBuilder.append("c.id as clientId, c.display_name as clientName, ");
            sqlBuilder.append("g.id as groupId, g.display_name as groupName, ");
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append("s.id fieldOfficerId, s.display_name as fieldOfficerName, ");
            sqlBuilder.append("sa.status_enum as statusEnum, ");
            sqlBuilder.append("sa.sub_status_enum as subStatusEnum, ");
            sqlBuilder.append("sa.submittedon_date as submittedOnDate,");
            sqlBuilder.append("sbu.username as submittedByUsername,");
            sqlBuilder.append("sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname,");

            sqlBuilder.append("sa.rejectedon_date as rejectedOnDate,");
            sqlBuilder.append("rbu.username as rejectedByUsername,");
            sqlBuilder.append("rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname,");

            sqlBuilder.append("sa.withdrawnon_date as withdrawnOnDate,");
            sqlBuilder.append("wbu.username as withdrawnByUsername,");
            sqlBuilder.append("wbu.firstname as withdrawnByFirstname, wbu.lastname as withdrawnByLastname,");

            sqlBuilder.append("sa.approvedon_date as approvedOnDate,");
            sqlBuilder.append("abu.username as approvedByUsername,");
            sqlBuilder.append("abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname,");

            sqlBuilder.append("sa.activatedon_date as activatedOnDate,");
            sqlBuilder.append("avbu.username as activatedByUsername,");
            sqlBuilder.append("avbu.firstname as activatedByFirstname, avbu.lastname as activatedByLastname,");

            sqlBuilder.append("sa.closedon_date as closedOnDate,");
            sqlBuilder.append("cbu.username as closedByUsername,");
            sqlBuilder.append("cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname,");

            sqlBuilder
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");

            sqlBuilder.append("sa.nominal_annual_interest_rate as nominalAnnualInterestRate, ");
            sqlBuilder.append("sa.interest_compounding_period_enum as interestCompoundingPeriodType, ");
            sqlBuilder.append("sa.interest_posting_period_enum as interestPostingPeriodType, ");
            sqlBuilder.append("sa.interest_calculation_type_enum as interestCalculationType, ");
            sqlBuilder.append("sa.interest_calculation_days_in_year_type_enum as interestCalculationDaysInYearType, ");
            sqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance, ");
            sqlBuilder.append("sa.lockin_period_frequency as lockinPeriodFrequency,");
            sqlBuilder.append("sa.lockin_period_frequency_enum as lockinPeriodFrequencyType, ");
            // sqlBuilder.append("sa.withdrawal_fee_amount as withdrawalFeeAmount,");
            // sqlBuilder.append("sa.withdrawal_fee_type_enum as withdrawalFeeTypeEnum, ");
            sqlBuilder.append("sa.withdrawal_fee_for_transfer as withdrawalFeeForTransfers, ");
            sqlBuilder.append("sa.allow_overdraft as allowOverdraft, ");
            sqlBuilder.append("sa.overdraft_limit as overdraftLimit, ");
            sqlBuilder.append("sa.nominal_annual_interest_rate_overdraft as nominalAnnualInterestRateOverdraft, ");
            sqlBuilder.append("sa.min_overdraft_for_interest_calculation as minOverdraftForInterestCalculation, ");
            // sqlBuilder.append("sa.annual_fee_amount as annualFeeAmount,");
            // sqlBuilder.append("sa.annual_fee_on_month as annualFeeOnMonth, ");
            // sqlBuilder.append("sa.annual_fee_on_day as annualFeeOnDay, ");
            // sqlBuilder.append("sa.annual_fee_next_due_date as annualFeeNextDueDate, ");
            sqlBuilder.append("sa.total_deposits_derived as totalDeposits, ");
            sqlBuilder.append("sa.total_withdrawals_derived as totalWithdrawals, ");
            sqlBuilder.append("sa.total_withdrawal_fees_derived as totalWithdrawalFees, ");
            sqlBuilder.append("sa.total_annual_fees_derived as totalAnnualFees, ");
            sqlBuilder.append("sa.total_interest_earned_derived as totalInterestEarned, ");
            sqlBuilder.append("sa.total_interest_posted_derived as totalInterestPosted, ");
            sqlBuilder.append("sa.total_overdraft_interest_derived as totalOverdraftInterestDerived, ");
            sqlBuilder.append("sa.account_balance_derived as accountBalance, ");
            sqlBuilder.append("sa.total_fees_charge_derived as totalFeeCharge, ");
            sqlBuilder.append("sa.total_penalty_charge_derived as totalPenaltyCharge, ");
            sqlBuilder.append("sa.min_balance_for_interest_calculation as minBalanceForInterestCalculation,");
            sqlBuilder.append("sa.min_required_balance as minRequiredBalance, ");
            sqlBuilder.append("sa.enforce_min_required_balance as enforceMinRequiredBalance, ");
            sqlBuilder.append("sa.on_hold_funds_derived as onHoldFunds, ");
            sqlBuilder.append("sa.withhold_tax as withHoldTax, ");
            sqlBuilder.append("sa.total_withhold_tax_derived as totalWithholdTax, ");
            sqlBuilder.append("sa.last_interest_calculation_date as lastInterestCalculationDate, ");
            sqlBuilder.append("tg.id as taxGroupId, tg.name as taxGroupName, ");
            sqlBuilder.append("(select IFNULL(max(sat.transaction_date),sa.activatedon_date) ");
            sqlBuilder.append("from m_savings_account_transaction as sat ");
            sqlBuilder.append("where sat.is_reversed = 0 ");
            sqlBuilder.append("and sat.transaction_type_enum in (1,2) ");
            sqlBuilder.append("and sat.savings_account_id = sa.id) as lastActiveTransactionDate, ");
            sqlBuilder.append("sp.is_dormancy_tracking_active as isDormancyTrackingActive, ");
            sqlBuilder.append("sp.days_to_inactive as daysToInactive, ");
            sqlBuilder.append("sp.days_to_dormancy as daysToDormancy, ");
            sqlBuilder.append("sp.days_to_escheat as daysToEscheat ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_savings_product sp ON sa.product_id = sp.id ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
            sqlBuilder.append("left join m_client c ON c.id = sa.client_id ");
            sqlBuilder.append("left join m_group g ON g.id = sa.group_id ");
            sqlBuilder.append("left join m_staff s ON s.id = sa.field_officer_id ");
            sqlBuilder.append("left join m_appuser sbu on sbu.id = sa.submittedon_userid ");
            sqlBuilder.append("left join m_appuser rbu on rbu.id = sa.rejectedon_userid ");
            sqlBuilder.append("left join m_appuser wbu on wbu.id = sa.withdrawnon_userid ");
            sqlBuilder.append("left join m_appuser abu on abu.id = sa.approvedon_userid ");
            sqlBuilder.append("left join m_appuser avbu on avbu.id = sa.activatedon_userid ");
            sqlBuilder.append("left join m_appuser cbu on cbu.id = sa.closedon_userid ");
            sqlBuilder.append("left join m_tax_group tg on tg.id = sa.tax_group_id  ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");
            final Integer depositTypeId = rs.getInt("depositType");
            final EnumOptionData depositType = SavingsEnumerations.depositType(depositTypeId);

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final Long fieldOfficerId = rs.getLong("fieldOfficerId");
            final String fieldOfficerName = rs.getString("fieldOfficerName");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusEnum);

            final Integer subStatusEnum = JdbcSupport.getInteger(rs, "subStatusEnum");
            final SavingsAccountSubStatusEnumData subStatus = SavingsEnumerations.subStatus(subStatusEnum);
            
            final LocalDate lastActiveTransactionDate = JdbcSupport.getLocalDate(rs, "lastActiveTransactionDate");
            final boolean isDormancyTrackingActive = rs.getBoolean("isDormancyTrackingActive");
            final Integer numDaysToInactive = JdbcSupport.getInteger(rs, "daysToInactive");
            final Integer numDaysToDormancy = JdbcSupport.getInteger(rs, "daysToDormancy");
            final Integer numDaysToEscheat = JdbcSupport.getInteger(rs, "daysToEscheat");
            Integer daysToInactive = null;
            Integer daysToDormancy = null;
            Integer daysToEscheat = null;

        	LocalDate localTenantDate = DateUtils.getLocalDateOfTenant();
            if(isDormancyTrackingActive && statusEnum.equals(SavingsAccountStatusType.ACTIVE.getValue())){
            	if(subStatusEnum < SavingsAccountSubStatusEnum.ESCHEAT.getValue()){
                    daysToEscheat = Days.daysBetween(localTenantDate,lastActiveTransactionDate.plusDays(numDaysToEscheat)).getDays();
            	}
            	if(subStatusEnum < SavingsAccountSubStatusEnum.DORMANT.getValue()){
                    daysToDormancy = Days.daysBetween(localTenantDate,lastActiveTransactionDate.plusDays(numDaysToDormancy)).getDays();
            	}
            	if(subStatusEnum < SavingsAccountSubStatusEnum.INACTIVE.getValue()){
                    daysToInactive = Days.daysBetween(localTenantDate,lastActiveTransactionDate.plusDays(numDaysToInactive)).getDays(); 
            	}
            }

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedOnDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");

            final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, "withdrawnOnDate");
            final String withdrawnByUsername = rs.getString("withdrawnByUsername");
            final String withdrawnByFirstname = rs.getString("withdrawnByFirstname");
            final String withdrawnByLastname = rs.getString("withdrawnByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate activatedOnDate = JdbcSupport.getLocalDate(rs, "activatedOnDate");
            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final SavingsAccountApplicationTimelineData timeline = new SavingsAccountApplicationTimelineData(submittedOnDate,
                    submittedByUsername, submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername,
                    rejectedByFirstname, rejectedByLastname, withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname,
                    withdrawnByLastname, approvedOnDate, approvedByUsername, approvedByFirstname, approvedByLastname, activatedOnDate,
                    activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate, closedByUsername, closedByFirstname,
                    closedByLastname);

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final BigDecimal nominalAnnualInterestRate = rs.getBigDecimal("nominalAnnualInterestRate");

            final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations
                    .compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCompoundingPeriodType")));

            final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType
                    .fromInt(JdbcSupport.getInteger(rs, "interestPostingPeriodType")));

            final EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType
                    .fromInt(JdbcSupport.getInteger(rs, "interestCalculationType")));

            final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations
                    .interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCalculationDaysInYearType")));

            final BigDecimal minRequiredOpeningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredOpeningBalance");

            final Integer lockinPeriodFrequency = JdbcSupport.getInteger(rs, "lockinPeriodFrequency");
            EnumOptionData lockinPeriodFrequencyType = null;
            final Integer lockinPeriodFrequencyTypeValue = JdbcSupport.getInteger(rs, "lockinPeriodFrequencyType");
            if (lockinPeriodFrequencyTypeValue != null) {
                final SavingsPeriodFrequencyType lockinPeriodType = SavingsPeriodFrequencyType.fromInt(lockinPeriodFrequencyTypeValue);
                lockinPeriodFrequencyType = SavingsEnumerations.lockinPeriodFrequencyType(lockinPeriodType);
            }

            /*
             * final BigDecimal withdrawalFeeAmount =
             * rs.getBigDecimal("withdrawalFeeAmount");
             * 
             * EnumOptionData withdrawalFeeType = null; final Integer
             * withdrawalFeeTypeValue = JdbcSupport.getInteger(rs,
             * "withdrawalFeeTypeEnum"); if (withdrawalFeeTypeValue != null) {
             * withdrawalFeeType =
             * SavingsEnumerations.withdrawalFeeType(withdrawalFeeTypeValue); }
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

            /*
             * final BigDecimal annualFeeAmount =
             * JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
             * "annualFeeAmount");
             * 
             * MonthDay annualFeeOnMonthDay = null; final Integer
             * annualFeeOnMonth = JdbcSupport.getInteger(rs,
             * "annualFeeOnMonth"); final Integer annualFeeOnDay =
             * JdbcSupport.getInteger(rs, "annualFeeOnDay"); if (annualFeeAmount
             * != null && annualFeeOnDay != null) { annualFeeOnMonthDay = new
             * MonthDay(annualFeeOnMonth, annualFeeOnDay); }
             * 
             * final LocalDate annualFeeNextDueDate =
             * JdbcSupport.getLocalDate(rs, "annualFeeNextDueDate");
             */
            final BigDecimal totalDeposits = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalDeposits");
            final BigDecimal totalWithdrawals = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithdrawals");
            final BigDecimal totalWithdrawalFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithdrawalFees");
            final BigDecimal totalAnnualFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalAnnualFees");

            final BigDecimal totalInterestEarned = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalInterestEarned");
            final BigDecimal totalInterestPosted = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalInterestPosted");
            final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "accountBalance");
            final BigDecimal totalFeeCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalFeeCharge");
            final BigDecimal totalPenaltyCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalPenaltyCharge");
            final BigDecimal totalOverdraftInterestDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                    "totalOverdraftInterestDerived");
            final BigDecimal totalWithholdTax = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithholdTax");

            final BigDecimal minBalanceForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "minBalanceForInterestCalculation");
            final BigDecimal onHoldFunds = rs.getBigDecimal("onHoldFunds");
            
            BigDecimal availableBalance = accountBalance;
            if (availableBalance != null && onHoldFunds != null) {

                availableBalance = availableBalance.subtract(onHoldFunds);
            }
            
            BigDecimal interestNotPosted = BigDecimal.ZERO;
            LocalDate lastInterestCalculationDate = null;
            if(totalInterestEarned != null){
            	interestNotPosted = totalInterestEarned.subtract(totalInterestPosted).add(totalOverdraftInterestDerived);
            	lastInterestCalculationDate = JdbcSupport.getLocalDate(rs, "lastInterestCalculationDate");
            }
            
            final SavingsAccountSummaryData summary = new SavingsAccountSummaryData(currency, totalDeposits, totalWithdrawals,
                    totalWithdrawalFees, totalAnnualFees, totalInterestEarned, totalInterestPosted, accountBalance, totalFeeCharge,
                    totalPenaltyCharge, totalOverdraftInterestDerived, totalWithholdTax, interestNotPosted, lastInterestCalculationDate, availableBalance);

            final boolean withHoldTax = rs.getBoolean("withHoldTax");
            final Long taxGroupId = JdbcSupport.getLong(rs, "taxGroupId");
            final String taxGroupName = rs.getString("taxGroupName");
            TaxGroupData taxGroupData = null;
            if (taxGroupId != null) {
                taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
            }

            return SavingsAccountData.instance(id, accountNo, depositType, externalId, groupId, groupName, clientId, clientName, productId,
                    productName, fieldOfficerId, fieldOfficerName, status, subStatus, timeline, currency,
                    nominalAnnualInterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                    interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers,
                    summary, allowOverdraft, overdraftLimit, minRequiredBalance, enforceMinRequiredBalance,
                    minBalanceForInterestCalculation, onHoldFunds, nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, 
                    taxGroupData, lastActiveTransactionDate, isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat);
        }
    }

    private static final class SavingAccountMapperForLookup implements RowMapper<SavingsAccountData> {

        private final String schemaSql;

        public SavingAccountMapperForLookup() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
            sqlBuilder.append("sa.deposit_type_enum as depositType, ");
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder.append("sa.status_enum as statusEnum ");

            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_savings_product sp ON sa.product_id = sp.id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final Integer depositTypeId = rs.getInt("depositType");
            final EnumOptionData depositType = SavingsEnumerations.depositType(depositTypeId);

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusEnum);

            return SavingsAccountData.lookupWithProductDetails(id, accountNo, depositType, productId, productName, status);
        }
    }

    @Override
    public SavingsAccountData retrieveTemplate(final Long clientId, final Long groupId, final Long productId,
            final boolean staffInSelectedOfficeOnly) {

        final AppUser loggedInUser = this.context.authenticatedUser();
        Long officeId = loggedInUser.getOffice().getId();

        ClientData client = null;
        if (clientId != null) {
            client = this.clientReadPlatformService.retrieveOne(clientId);
            officeId = client.officeId();
        }

        GroupGeneralData group = null;
        if (groupId != null) {
            group = this.groupReadPlatformService.retrieveOne(groupId);
            officeId = group.officeId();
        }

        final Collection<SavingsProductData> productOptions = this.savingsProductReadPlatformService.retrieveAllForLookup();
        SavingsAccountData template = null;
        if (productId != null) {

            final SavingAccountTemplateMapper mapper = new SavingAccountTemplateMapper(client, group);

            final String sql = "select " + mapper.schema() + " where sp.id = ?";
            template = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { productId });

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
                clientName = client.displayName();
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

        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.SAVING.getName(), productId);
        template.setDatatables(datatableTemplates);

        return template;
    }

    private Collection<SavingsAccountChargeData> fromChargesToSavingsCharges(final Collection<ChargeData> productCharges) {
        final Collection<SavingsAccountChargeData> savingsCharges = new ArrayList<>();
        for (final ChargeData chargeData : productCharges) {
            final SavingsAccountChargeData savingsCharge = chargeData.toSavingsAccountChargeData();
            savingsCharges.add(savingsCharge);
        }
        return savingsCharges;
    }

    @Override
    public SavingsAccountTransactionData retrieveDepositTransactionTemplate(final Long savingsId,
            final DepositAccountType depositAccountType) {

        try {
            final String sql = "select " + this.transactionTemplateMapper.schema() + " where sa.id = ? and sa.deposit_type_enum = ?";

            return this.jdbcTemplate.queryForObject(sql, this.transactionTemplateMapper,
                    new Object[] { savingsId, depositAccountType.getValue() });
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(savingsId);
        }
    }

    @Override
    public Collection<SavingsAccountTransactionData> retrieveAllTransactions(final Long savingsId, DepositAccountType depositAccountType) {

        final String sql = "select " + this.transactionsMapper.schema()
                + " where sa.id = ? and sa.deposit_type_enum = ? order by tr.transaction_date DESC, tr.created_date DESC, tr.id DESC";

        return this.jdbcTemplate.query(sql, this.transactionsMapper, new Object[] { savingsId, depositAccountType.getValue() });
    }

    @Override
    public SavingsAccountTransactionData retrieveSavingsTransaction(final Long savingsId, final Long transactionId,
            DepositAccountType depositAccountType) {

        final String sql = "select " + this.transactionsMapper.schema() + " where sa.id = ? and sa.deposit_type_enum = ? and tr.id= ?";

        return this.jdbcTemplate.queryForObject(sql, this.transactionsMapper, new Object[] { savingsId, depositAccountType.getValue(),
                transactionId });
    }

    /*
     * @Override public Collection<SavingsAccountAnnualFeeData>
     * retrieveAccountsWithAnnualFeeDue() { final String sql = "select " +
     * this.annualFeeMapper.schema() +
     * " where sa.annual_fee_next_due_date is not null and sa.annual_fee_next_due_date <= NOW()"
     * ;
     * 
     * return this.jdbcTemplate.query(sql, this.annualFeeMapper, new Object[]
     * {}); }
     */

    private static final class SavingsAccountTransactionsMapper implements RowMapper<SavingsAccountTransactionData> {

        private final String schemaSql;

        public SavingsAccountTransactionsMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("tr.id as transactionId, tr.transaction_type_enum as transactionType, ");
            sqlBuilder.append("tr.transaction_date as transactionDate, tr.amount as transactionAmount,");
            sqlBuilder.append("tr.created_date as submittedOnDate,");
            sqlBuilder.append("tr.running_balance_derived as runningBalance, tr.is_reversed as reversed,");
            sqlBuilder.append("fromtran.id as fromTransferId, fromtran.is_reversed as fromTransferReversed,");
            sqlBuilder.append("fromtran.transaction_date as fromTransferDate, fromtran.amount as fromTransferAmount,");
            sqlBuilder.append("fromtran.description as fromTransferDescription,");
            sqlBuilder.append("totran.id as toTransferId, totran.is_reversed as toTransferReversed,");
            sqlBuilder.append("totran.transaction_date as toTransferDate, totran.amount as toTransferAmount,");
            sqlBuilder.append("totran.description as toTransferDescription,");
            sqlBuilder.append("sa.id as savingsId, sa.account_no as accountNo,");
            sqlBuilder.append("pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, ");
            sqlBuilder.append("pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode, ");
            sqlBuilder
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("pt.value as paymentTypeName, ");
            sqlBuilder.append("tr.is_manual as postInterestAsOn ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_savings_account_transaction tr on tr.savings_account_id = sa.id ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
            sqlBuilder.append("left join m_account_transfer_transaction fromtran on fromtran.from_savings_transaction_id = tr.id ");
            sqlBuilder.append("left join m_account_transfer_transaction totran on totran.to_savings_transaction_id = tr.id ");
            sqlBuilder.append("left join m_payment_detail pd on tr.payment_detail_id = pd.id ");
            sqlBuilder.append("left join m_payment_type pt on pd.payment_type_id = pt.id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("transactionId");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
            final BigDecimal outstandingChargeAmount = null;
            final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "runningBalance");
            final boolean reversed = rs.getBoolean("reversed");

            final Long savingsId = rs.getLong("savingsId");
            final String accountNo = rs.getString("accountNo");
            final boolean postInterestAsOn = rs.getBoolean("postInterestAsOn");

            PaymentDetailData paymentDetailData = null;
            if (transactionType.isDepositOrWithdrawal()) {
                final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
                if (paymentTypeId != null) {
                    final String typeName = rs.getString("paymentTypeName");
                    final PaymentTypeData paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
                    final String accountNumber = rs.getString("accountNumber");
                    final String checkNumber = rs.getString("checkNumber");
                    final String routingCode = rs.getString("routingCode");
                    final String receiptNumber = rs.getString("receiptNumber");
                    final String bankNumber = rs.getString("bankNumber");
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
            }

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            AccountTransferData transfer = null;
            final Long fromTransferId = JdbcSupport.getLong(rs, "fromTransferId");
            final Long toTransferId = JdbcSupport.getLong(rs, "toTransferId");
            if (fromTransferId != null) {
                final LocalDate fromTransferDate = JdbcSupport.getLocalDate(rs, "fromTransferDate");
                final BigDecimal fromTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fromTransferAmount");
                final boolean fromTransferReversed = rs.getBoolean("fromTransferReversed");
                final String fromTransferDescription = rs.getString("fromTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(fromTransferId, currency, fromTransferAmount, fromTransferDate,
                        fromTransferDescription, fromTransferReversed);
            } else if (toTransferId != null) {
                final LocalDate toTransferDate = JdbcSupport.getLocalDate(rs, "toTransferDate");
                final BigDecimal toTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "toTransferAmount");
                final boolean toTransferReversed = rs.getBoolean("toTransferReversed");
                final String toTransferDescription = rs.getString("toTransferDescription");

                

                transfer = AccountTransferData.transferBasicDetails(toTransferId, currency, toTransferAmount, toTransferDate,
                        toTransferDescription, toTransferReversed);
            }

            return SavingsAccountTransactionData.create(id, transactionType, paymentDetailData, savingsId, accountNo, date, currency,
                    amount, outstandingChargeAmount, runningBalance, reversed, transfer, submittedOnDate, postInterestAsOn);
        }
    }

    private static final class SavingsAccountTransactionTemplateMapper implements RowMapper<SavingsAccountTransactionData> {

        private final String schemaSql;

        public SavingsAccountTransactionTemplateMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
            sqlBuilder
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sa.min_required_opening_balance as minRequiredOpeningBalance ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long savingsId = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return SavingsAccountTransactionData.template(savingsId, accountNo, DateUtils.getLocalDateOfTenant(), currency);
        }
    }

    private static final class SavingAccountTemplateMapper implements RowMapper<SavingsAccountData> {

        private final ClientData client;
        private final GroupGeneralData group;

        private final String schemaSql;

        public SavingAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
            this.client = client;
            this.group = group;

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sp.id as productId, sp.name as productName, ");
            sqlBuilder
                    .append("sp.currency_code as currencyCode, sp.currency_digits as currencyDigits, sp.currency_multiplesof as inMultiplesOf, ");
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
            // sqlBuilder.append("sp.withdrawal_fee_amount as withdrawalFeeAmount,");
            // sqlBuilder.append("sp.withdrawal_fee_type_enum as withdrawalFeeTypeEnum, ");
            sqlBuilder.append("sp.withdrawal_fee_for_transfer as withdrawalFeeForTransfers, ");
            sqlBuilder.append("sp.min_balance_for_interest_calculation as minBalanceForInterestCalculation, ");
            sqlBuilder.append("sp.allow_overdraft as allowOverdraft, ");
            sqlBuilder.append("sp.overdraft_limit as overdraftLimit, ");
            sqlBuilder.append("sp.nominal_annual_interest_rate_overdraft as nominalAnnualInterestRateOverdraft, ");
            sqlBuilder.append("sp.min_overdraft_for_interest_calculation as minOverdraftForInterestCalculation, ");
            sqlBuilder.append("sp.withhold_tax as withHoldTax,");
            sqlBuilder.append("tg.id as taxGroupId, tg.name as taxGroupName, ");

            // sqlBuilder.append("sp.annual_fee_amount as annualFeeAmount,");
            // sqlBuilder.append("sp.annual_fee_on_month as annualFeeOnMonth, ");
            // sqlBuilder.append("sp.annual_fee_on_day as annualFeeOnDay ");
            sqlBuilder.append("sp.min_required_balance as minRequiredBalance, ");
            sqlBuilder.append("sp.enforce_min_required_balance as enforceMinRequiredBalance ");
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
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final BigDecimal nominalAnnualIterestRate = rs.getBigDecimal("nominalAnnualIterestRate");

            final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations
                    .compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCompoundingPeriodType")));

            final EnumOptionData interestPostingPeriodType = SavingsEnumerations.interestPostingPeriodType(SavingsPostingInterestPeriodType
                    .fromInt(JdbcSupport.getInteger(rs, "interestPostingPeriodType")));

            final EnumOptionData interestCalculationType = SavingsEnumerations.interestCalculationType(SavingsInterestCalculationType
                    .fromInt(JdbcSupport.getInteger(rs, "interestCalculationType")));

            final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations
                    .interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.fromInt(JdbcSupport.getInteger(rs,
                            "interestCalculationDaysInYearType")));

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
             * EnumOptionData withdrawalFeeType = null; final Integer
             * withdrawalFeeTypeValue = JdbcSupport.getInteger(rs,
             * "withdrawalFeeTypeEnum"); if (withdrawalFeeTypeValue != null) {
             * withdrawalFeeType =
             * SavingsEnumerations.withdrawalFeeType(withdrawalFeeTypeValue); }
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
            final BigDecimal minBalanceForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "minBalanceForInterestCalculation");

            // final BigDecimal annualFeeAmount =
            // JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
            // "annualFeeAmount");

            /*
             * MonthDay annualFeeOnMonthDay = null; final Integer
             * annualFeeOnMonth = JdbcSupport.getInteger(rs,
             * "annualFeeOnMonth"); final Integer annualFeeOnDay =
             * JdbcSupport.getInteger(rs, "annualFeeOnDay"); if (annualFeeAmount
             * != null && annualFeeOnDay != null) { annualFeeOnMonthDay = new
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
                clientId = this.client.id();
                clientName = this.client.displayName();
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
            
            final SavingsAccountSubStatusEnumData subStatus = null;
            final LocalDate lastActiveTransactionDate = null;
            final boolean isDormancyTrackingActive = false;
            final Integer daysToInactive = null;
            final Integer daysToDormancy = null;
            final Integer daysToEscheat = null;

            final SavingsAccountApplicationTimelineData timeline = SavingsAccountApplicationTimelineData.templateDefault();
            final EnumOptionData depositType = null;
            return SavingsAccountData.instance(null, null, depositType, null, groupId, groupName, clientId, clientName, productId,
                    productName, fieldOfficerId, fieldOfficerName, status, subStatus, timeline, currency,
                    nominalAnnualIterestRate, interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType,
                    interestCalculationDaysInYearType, minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers,
                    summary, allowOverdraft, overdraftLimit, minRequiredBalance, enforceMinRequiredBalance,
                    minBalanceForInterestCalculation, onHoldFunds, nominalAnnualInterestRateOverdraft, minOverdraftForInterestCalculation, withHoldTax, 
                    taxGroupData, lastActiveTransactionDate, isDormancyTrackingActive, daysToInactive, daysToDormancy, daysToEscheat);
        }
    }

    @Override
    public Collection<SavingsAccountData> retrieveForLookup(Long clientId, Boolean overdraft) {

        SavingAccountMapperForLookup accountMapperForLookup = new SavingAccountMapperForLookup();
        final StringBuilder sqlBuilder = new StringBuilder("select " + accountMapperForLookup.schema());
        sqlBuilder.append(" where sa.client_id = ? and sa.status_enum = 300");
        Object[] queryParameters = null;
        if (overdraft == null) {
            queryParameters = new Object[] { clientId };
        } else {
            sqlBuilder.append(" and sa.allow_overdraft = ?");
            queryParameters = new Object[] { clientId, overdraft };
        }
        return this.jdbcTemplate.query(sqlBuilder.toString(), accountMapperForLookup, queryParameters);

    }

	@Override
	public List<Long> retrieveSavingsIdsPendingInactive(
			LocalDate tenantLocalDate) {
		List<Long> ret = null;
		StringBuilder sql = new StringBuilder("select sa.id ");
		sql.append(" from m_savings_account as sa ");
		sql.append(" inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = 1) ");
		sql.append(" where sa.status_enum = 300 ");
		sql.append(" and sa.sub_status_enum = 0 ");
		sql.append(" and DATEDIFF(?,(select IFNULL(max(sat.transaction_date),sa.activatedon_date) ");
		sql.append(" from m_savings_account_transaction as sat ");
		sql.append(" where sat.is_reversed = 0 ");
		sql.append(" and sat.transaction_type_enum in (1,2) ");
		sql.append(" and sat.savings_account_id = sa.id)) >= sp.days_to_inactive "); 
		
		try {
			ret =  this.jdbcTemplate.queryForList(sql.toString(), Long.class, new Object[] {formatter.print(tenantLocalDate)});
		} catch (EmptyResultDataAccessException e) {
			// ignore empty result scenario
		} catch (DataAccessException e) {
			throw e;
		}
		
		return ret;
	}

	@Override
	public List<Long> retrieveSavingsIdsPendingDormant(
			LocalDate tenantLocalDate) {
		List<Long> ret = null;
		StringBuilder sql = new StringBuilder("select sa.id ");
		sql.append(" from m_savings_account as sa ");
		sql.append(" inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = 1) ");
		sql.append(" where sa.status_enum = 300 ");
		sql.append(" and sa.sub_status_enum = 100 ");
		sql.append(" and DATEDIFF(?,(select IFNULL(max(sat.transaction_date),sa.activatedon_date) ");
		sql.append(" from m_savings_account_transaction as sat ");
		sql.append(" where sat.is_reversed = 0 ");
		sql.append(" and sat.transaction_type_enum in (1,2) ");
		sql.append(" and sat.savings_account_id = sa.id)) >= sp.days_to_dormancy "); 
		
		try {
			ret =  this.jdbcTemplate.queryForList(sql.toString(), Long.class, new Object[] {formatter.print(tenantLocalDate)});
		} catch (EmptyResultDataAccessException e) {
			// ignore empty result scenario
		} catch (DataAccessException e) {
			throw e;
		}
		
		return ret;
	}

	@Override
	public List<Long> retrieveSavingsIdsPendingEscheat(
			LocalDate tenantLocalDate) {
		List<Long> ret = null;
		StringBuilder sql = new StringBuilder("select sa.id ");
		sql.append(" from m_savings_account as sa ");
		sql.append(" inner join m_savings_product as sp on (sa.product_id = sp.id and sp.is_dormancy_tracking_active = 1) ");
		sql.append(" where sa.status_enum = 300 ");
		sql.append(" and sa.sub_status_enum = 200 ");
		sql.append(" and DATEDIFF(?,(select IFNULL(max(sat.transaction_date),sa.activatedon_date) ");
		sql.append(" from m_savings_account_transaction as sat ");
		sql.append(" where sat.is_reversed = 0 ");
		sql.append(" and sat.transaction_type_enum in (1,2) ");
		sql.append(" and sat.savings_account_id = sa.id)) >= sp.days_to_escheat "); 
		
		try {
			ret =  this.jdbcTemplate.queryForList(sql.toString(), Long.class, new Object[] {formatter.print(tenantLocalDate)});
		} catch (EmptyResultDataAccessException e) {
			// ignore empty result scenario
		} catch (DataAccessException e) {
			throw e;
		}
		
		return ret;
	}

    @Override
    public boolean isAccountBelongsToClient(final Long clientId, final Long accountId, final DepositAccountType depositAccountType,
            final String currencyCode) {
        try {
            final StringBuffer buff = new StringBuffer("select count(*) from m_savings_account sa ") ;
            buff.append(" where sa.id = ? and sa.client_id = ? and sa.deposit_type_enum = ? and sa.currency_code = ? and sa.status_enum = 300");
            return this.jdbcTemplate.queryForObject(buff.toString(), 
                    new Object[] { accountId, clientId, depositAccountType.getValue(), currencyCode }, Integer.class) > 0;
        } catch (final EmptyResultDataAccessException e) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }
    /*
     * private static final class SavingsAccountAnnualFeeMapper implements
     * RowMapper<SavingsAccountAnnualFeeData> {
     * 
     * private final String schemaSql;
     * 
     * public SavingsAccountAnnualFeeMapper() { final StringBuilder sqlBuilder =
     * new StringBuilder(200);
     * sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
     * sqlBuilder
     * .append("sa.annual_fee_next_due_date as annualFeeNextDueDate ");
     * sqlBuilder.append("from m_savings_account sa ");
     * 
     * this.schemaSql = sqlBuilder.toString(); }
     * 
     * public String schema() { return this.schemaSql; }
     * 
     * @Override public SavingsAccountAnnualFeeData mapRow(final ResultSet rs,
     * 
     * @SuppressWarnings("unused") final int rowNum) throws SQLException {
     * 
     * final Long id = rs.getLong("id"); final String accountNo =
     * rs.getString("accountNo"); final LocalDate annualFeeNextDueDate =
     * JdbcSupport.getLocalDate(rs, "annualFeeNextDueDate");
     * 
     * return SavingsAccountAnnualFeeData.instance(id, accountNo,
     * annualFeeNextDueDate); } }
     */
}