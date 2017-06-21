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
import java.util.Map;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadPlatformService;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.DepositAccountInterestRateChartData;
import org.apache.fineract.portfolio.savings.data.DepositProductData;
import org.apache.fineract.portfolio.savings.data.FixedDepositAccountData;
import org.apache.fineract.portfolio.savings.data.RecurringDepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountApplicationTimelineData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.exception.DepositAccountNotFoundException;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DepositAccountReadPlatformServiceImpl implements DepositAccountReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final DepositAccountInterestRateChartReadPlatformService accountChartReadPlatformService;
    private final InterestRateChartReadPlatformService productChartReadPlatformService;
    private final FixedDepositAccountMapper fixedDepositAccountRowMapper = new FixedDepositAccountMapper();
    private final RecurringDepositAccountMapper recurringDepositAccountRowMapper = new RecurringDepositAccountMapper();
    private final DepositAccountLookupMapper depositAccountLookupsRowMapper = new DepositAccountLookupMapper();
    private final DepositAccountForMaturityMapper depositAccountForMaturityRowMapper = new DepositAccountForMaturityMapper();
    private final PaginationParametersDataValidator paginationParametersDataValidator;
    private final PaginationHelper<DepositAccountData> paginationHelper = new PaginationHelper<>();
    private final SavingsAccountTransactionsMapper transactionsMapper;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final SavingsDropdownReadPlatformService savingsDropdownReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final DepositsDropdownReadPlatformService depositsDropdownReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final RecurringAccountDepositTransactionTemplateMapper rdTransactionTemplateMapper;
    private final DropdownReadPlatformService dropdownReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Autowired
    public DepositAccountReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final DepositAccountInterestRateChartReadPlatformService chartReadPlatformService,
            final PaginationParametersDataValidator paginationParametersDataValidator,
            final ClientReadPlatformService clientReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final DepositProductReadPlatformService depositProductReadPlatformService,
            final SavingsDropdownReadPlatformService savingsDropdownReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final DepositsDropdownReadPlatformService depositsDropdownReadPlatformService,
            final InterestRateChartReadPlatformService productChartReadPlatformService,
            final SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            final DropdownReadPlatformService dropdownReadPlatformService, final CalendarReadPlatformService calendarReadPlatformService,
            PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.accountChartReadPlatformService = chartReadPlatformService;
        this.paginationParametersDataValidator = paginationParametersDataValidator;
        this.transactionsMapper = new SavingsAccountTransactionsMapper();
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.depositProductReadPlatformService = depositProductReadPlatformService;
        this.savingsDropdownReadPlatformService = savingsDropdownReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.depositsDropdownReadPlatformService = depositsDropdownReadPlatformService;
        this.productChartReadPlatformService = productChartReadPlatformService;
        this.savingsAccountReadPlatformService = savingsAccountReadPlatformService;
        this.rdTransactionTemplateMapper = new RecurringAccountDepositTransactionTemplateMapper();
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    @Override
    public Collection<DepositAccountData> retrieveAll(final DepositAccountType depositAccountType,
            final PaginationParameters paginationParameters) {

        this.context.authenticatedUser();
        final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
        if (depositAccountMapper == null) return null;

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ");
        sqlBuilder.append(depositAccountMapper.schema());
        sqlBuilder.append(" where sa.deposit_type_enum = ? ");
        // always append at the end of a sql statement
        sqlBuilder.append(paginationParameters.paginationSql());

        return this.jdbcTemplate.query(sqlBuilder.toString(), depositAccountMapper, new Object[] { depositAccountType.getValue() });
    }

    @Override
    public Page<DepositAccountData> retrieveAllPaged(final DepositAccountType depositAccountType,
            final PaginationParameters paginationParameters) {

        this.paginationParametersDataValidator.validateParameterValues(paginationParameters, DepositsApiConstants.supportedOrderByValues,
                depositAccountType.resourceName());

        final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
        if (depositAccountMapper == null) return null;

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(depositAccountMapper.schema());
        sqlBuilder.append(" where sa.deposit_type_enum = ? ");
        sqlBuilder.append(paginationParameters.paginationSql());

        final String sqlCountRows = "SELECT FOUND_ROWS()";

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                new Object[] { depositAccountType.getValue() }, depositAccountMapper);
    }

    @Override
    public Collection<DepositAccountData> retrieveAllForLookup(final DepositAccountType depositAccountType) {

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ");
        sqlBuilder.append(this.depositAccountLookupsRowMapper.schema());
        sqlBuilder.append(" where sa.deposit_type_enum = ? ");

        return this.jdbcTemplate.query(sqlBuilder.toString(), this.depositAccountLookupsRowMapper,
                new Object[] { depositAccountType.getValue() });
    }

    @Override
    public Collection<DepositAccountData> retrieveForMaturityUpdate() {

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("SELECT ");
        sqlBuilder.append(this.depositAccountForMaturityRowMapper.schema());
        sqlBuilder.append(" WHERE da.deposit_type_enum in (?, ?) and da.status_enum = ?");

        LocalDate today = DateUtils.getLocalDateOfTenant();

        return this.jdbcTemplate.query(
                sqlBuilder.toString(),
                this.depositAccountForMaturityRowMapper,
                new Object[] { formatter.print(today), DepositAccountType.FIXED_DEPOSIT.getValue(),
                        DepositAccountType.RECURRING_DEPOSIT.getValue(), SavingsAccountStatusType.ACTIVE.getValue() });
    }

    @Override
    public DepositAccountData retrieveOne(final DepositAccountType depositAccountType, final Long accountId) {
        try {
            this.context.authenticatedUser();

            final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
            if (depositAccountMapper == null) return null;

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("select ");
            sqlBuilder.append(depositAccountMapper.schema());
            sqlBuilder.append(" where sa.id = ? and sa.deposit_type_enum = ? ");

            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), depositAccountMapper, new Object[] { accountId,
                    depositAccountType.getValue() });

        } catch (final EmptyResultDataAccessException e) {
            throw new DepositAccountNotFoundException(depositAccountType, accountId);
        }
    }

    @Override
    public DepositAccountData retrieveOneWithClosureTemplate(final DepositAccountType depositAccountType, final Long accountId) {
        try {
            this.context.authenticatedUser();

            final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
            if (depositAccountMapper == null) return null;

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("select ");
            sqlBuilder.append(depositAccountMapper.schema());
            sqlBuilder.append(" where sa.id = ? and sa.deposit_type_enum = ? ");

            DepositAccountData account = this.jdbcTemplate.queryForObject(sqlBuilder.toString(), depositAccountMapper, new Object[] {
                    accountId, depositAccountType.getValue() });
            Collection<EnumOptionData> onAccountClosureOptions = SavingsEnumerations
                    .depositAccountOnClosureType(DepositAccountOnClosureType.values());
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            final Collection<SavingsAccountData> savingsAccountDatas = this.savingsAccountReadPlatformService.retrieveActiveForLookup(
                    account.clientId(), DepositAccountType.SAVINGS_DEPOSIT);
            if (depositAccountType.isFixedDeposit()) {
                account = FixedDepositAccountData.withClosureTemplateDetails((FixedDepositAccountData) account, onAccountClosureOptions,
                        paymentTypeOptions, savingsAccountDatas);
            } else if (depositAccountType.isRecurringDeposit()) {
                account = RecurringDepositAccountData.withClosureTemplateDetails((RecurringDepositAccountData) account,
                        onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas);
            }

            return account;

        } catch (final EmptyResultDataAccessException e) {
            throw new DepositAccountNotFoundException(depositAccountType, accountId);
        }
    }

    @Override
    public DepositAccountData retrieveOneWithChartSlabs(final DepositAccountType depositAccountType, Long accountId) {
        DepositAccountData depositAccount = this.retrieveOne(depositAccountType, accountId);
        DepositAccountInterestRateChartData chart = this.accountChartReadPlatformService.retrieveOneWithSlabsOnAccountId(accountId);

        if (depositAccountType.isFixedDeposit()) {
            depositAccount = FixedDepositAccountData.withInterestChart((FixedDepositAccountData) depositAccount, chart);
        } else if (depositAccountType.isRecurringDeposit()) {
            CalendarData calendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(accountId,
                    CalendarEntityType.SAVINGS.getValue());
            final Integer frequency = calendar.interval() == -1 ? 1 : calendar.interval();
            final CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.fromInt(calendar.frequencyType().getId().intValue());
            final PeriodFrequencyType periodFrequencyType = CalendarFrequencyType.from(calendarFrequencyType);
            final EnumOptionData frequencyType = CommonEnumerations.termFrequencyType(periodFrequencyType, "recurring.deposit.frequency.");
            depositAccount = RecurringDepositAccountData.withInterestChartAndRecurringDetails((RecurringDepositAccountData) depositAccount,
                    chart, frequency, frequencyType);
        }

        return depositAccount;
    }

    @Override
    public Collection<SavingsAccountTransactionData> retrieveAllTransactions(final DepositAccountType depositAccountType,
            final Long accountId) {

        final String sql = "select " + this.transactionsMapper.schema()
                + " where sa.id = ? and sa.deposit_type_enum = ? order by tr.transaction_date DESC, tr.id DESC";

        return this.jdbcTemplate.query(sql, this.transactionsMapper, new Object[] { accountId, depositAccountType.getValue() });
    }

    @Override
    public DepositAccountData retrieveTemplate(final DepositAccountType depositAccountType, final Long clientId, final Long groupId,
            final Long productId, final boolean staffInSelectedOfficeOnly) {

        final AppUser loggedInUser = this.context.authenticatedUser();
        Long officeId = loggedInUser.getOffice().getId();

        ClientData client = null;
        Collection<SavingsAccountData> savingsAccountDatas = null;
        if (clientId != null) {
            client = this.clientReadPlatformService.retrieveOne(clientId);
            officeId = client.officeId();
            savingsAccountDatas = this.savingsAccountReadPlatformService.retrieveActiveForLookup(clientId,
                    DepositAccountType.SAVINGS_DEPOSIT);
        }

        GroupGeneralData group = null;
        if (groupId != null) {
            group = this.groupReadPlatformService.retrieveOne(groupId);
            officeId = group.officeId();
        }

        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = this.depositsDropdownReadPlatformService
                .retrievePreClosurePenalInterestOnTypeOptions();

        final Collection<EnumOptionData> periodFrequencyTypeOptions = this.dropdownReadPlatformService.retrievePeriodFrequencyTypeOptions();
        final Collection<DepositProductData> productOptions = this.depositProductReadPlatformService
                .retrieveAllForLookup(depositAccountType);
        DepositAccountData template = null;
        if (productId != null) {

            final DepositAccountTemplateMapper mapper = getDepositAccountTemplaMapper(depositAccountType, client, group);

            final String sql = "select " + mapper.schema() + " where sa.id = ? and sa.deposit_type_enum = ? ";
            template = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { productId, depositAccountType.getValue() });

            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = this.savingsDropdownReadPlatformService
                    .retrieveCompoundingInterestPeriodTypeOptions();

            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = this.savingsDropdownReadPlatformService
                    .retrieveInterestPostingPeriodTypeOptions();

            final Collection<EnumOptionData> interestCalculationTypeOptions = this.savingsDropdownReadPlatformService
                    .retrieveInterestCalculationTypeOptions();

            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = this.savingsDropdownReadPlatformService
                    .retrieveInterestCalculationDaysInYearTypeOptions();

            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.savingsDropdownReadPlatformService
                    .retrieveLockinPeriodFrequencyTypeOptions();

            final Collection<EnumOptionData> withdrawalFeeTypeOptions = this.savingsDropdownReadPlatformService
                    .retrievewithdrawalFeeTypeOptions();

            final Collection<SavingsAccountTransactionData> transactions = null;
            final Collection<ChargeData> productCharges = this.chargeReadPlatformService.retrieveSavingsProductCharges(productId);
            // update charges from Product charges
            final Collection<SavingsAccountChargeData> charges = fromChargesToSavingsCharges(productCharges);

            final boolean feeChargesOnly = false;
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSavingsProductApplicableCharges(feeChargesOnly);

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

            // retrieve chart Slabs
            final InterestRateChartData productChartData = this.productChartReadPlatformService.retrieveActiveChartWithTemplate(productId);
            final DepositAccountInterestRateChartData accountChart = DepositAccountInterestRateChartData.from(productChartData);

            if (depositAccountType.isFixedDeposit()) {

                template = FixedDepositAccountData.withTemplateOptions((FixedDepositAccountData) template, productOptions,
                        fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                        interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                        withdrawalFeeTypeOptions, transactions, charges, chargeOptions, preClosurePenalInterestOnTypeOptions,
                        periodFrequencyTypeOptions, savingsAccountDatas);

                template = FixedDepositAccountData.withInterestChart((FixedDepositAccountData) template, accountChart);
            } else if (depositAccountType.isRecurringDeposit()) {
                template = RecurringDepositAccountData.withTemplateOptions((RecurringDepositAccountData) template, productOptions,
                        fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                        interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                        withdrawalFeeTypeOptions, transactions, charges, chargeOptions, preClosurePenalInterestOnTypeOptions,
                        periodFrequencyTypeOptions);
                template = RecurringDepositAccountData.withInterestChartAndRecurringDetails((RecurringDepositAccountData) template,
                        accountChart, null, null);

            }

        } else {

            String clientName = null;
            if (client != null) {
                clientName = client.displayName();
            }

            String groupName = null;
            if (group != null) {
                groupName = group.getName();
            }

            final Collection<StaffData> fieldOfficerOptions = null;
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationTypeOptions = null;
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
            final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;

            final Collection<SavingsAccountTransactionData> transactions = null;
            final Collection<SavingsAccountChargeData> charges = null;

            final boolean feeChargesOnly = true;
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSavingsProductApplicableCharges(feeChargesOnly);

            if (depositAccountType.isFixedDeposit()) {

                template = FixedDepositAccountData.withClientTemplate(clientId, clientName, groupId, groupName);

                template = FixedDepositAccountData.withTemplateOptions((FixedDepositAccountData) template, productOptions,
                        fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                        interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                        withdrawalFeeTypeOptions, transactions, charges, chargeOptions, preClosurePenalInterestOnTypeOptions,
                        periodFrequencyTypeOptions, savingsAccountDatas);
            } else if (depositAccountType.isRecurringDeposit()) {

                template = RecurringDepositAccountData.withClientTemplate(clientId, clientName, groupId, groupName);

                template = RecurringDepositAccountData.withTemplateOptions((RecurringDepositAccountData) template, productOptions,
                        fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                        interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                        withdrawalFeeTypeOptions, transactions, charges, chargeOptions, preClosurePenalInterestOnTypeOptions,
                        periodFrequencyTypeOptions);
            }
        }

        return template;
    }

    @Override
    public SavingsAccountTransactionData retrieveRecurringAccountDepositTransactionTemplate(final Long accountId) {

        try {
            final String sql = "select " + this.rdTransactionTemplateMapper.schema()
                    + " where sa.id = ? and sa.deposit_type_enum = ? order by mss.installment limit 1";
            return this.jdbcTemplate.queryForObject(sql, this.rdTransactionTemplateMapper, new Object[] { accountId, accountId,
                    DepositAccountType.RECURRING_DEPOSIT.getValue() });
        } catch (final EmptyResultDataAccessException e) {
            throw new DepositAccountNotFoundException(DepositAccountType.RECURRING_DEPOSIT, accountId);
        }
    }

    @Override
    public Collection<AccountTransferDTO> retrieveDataForInterestTransfer() {
        final StringBuilder sqlBuilder = new StringBuilder(300);
        AccountTransferMapper mapper = new AccountTransferMapper();
        sqlBuilder.append("SELECT ");
        sqlBuilder.append(mapper.schema());
        sqlBuilder.append(" where da.transfer_interest_to_linked_account = 1 and ");
        sqlBuilder
                .append("st.transaction_date > (select IFNULL(max(sat.transaction_date),sa.activatedon_date) from m_savings_account_transaction sat where sat.transaction_type_enum = ? and sat.savings_account_id = sa.id and sat.is_reversed=0) ");
        sqlBuilder
                .append("and st.transaction_type_enum = ? and sa.status_enum = ? and st.is_reversed=0 and st.transaction_date > IFNULL(sa.lockedin_until_date_derived,sa.activatedon_date)");

        return this.jdbcTemplate.query(sqlBuilder.toString(), mapper, new Object[] { SavingsAccountTransactionType.WITHDRAWAL.getValue(),
                SavingsAccountTransactionType.INTEREST_POSTING.getValue(), SavingsAccountStatusType.ACTIVE.getValue() });
    }

    @Override
    public Collection<Map<String, Object>> retriveDataForRDScheduleCreation() {
        final StringBuilder sb = new StringBuilder(300);
        sb.append(" select rd.savings_account_id savingsId, rd.mandatory_recommended_deposit_amount as amount,");
        sb.append(" mc.recurrence as recurrence ,");
        sb.append(" max(ms.duedate) as dueDate , max(ms.installment) as installment,");
        sb.append(" count(ms.installment) as futureInstallemts");
        sb.append(" from m_deposit_account_term_and_preclosure dat ");
        sb.append(" inner join m_savings_account sa on sa.id = dat.savings_account_id and sa.status_enum = ?");
        sb.append(" inner join m_deposit_account_recurring_detail rd on rd.savings_account_id = dat.savings_account_id ");
        sb.append(" inner join m_calendar_instance mci on mci.entity_type_enum = ? and mci.entity_id = dat.savings_account_id  ");
        sb.append(" inner join m_calendar mc  on mc.id = mci.calendar_id and mc.calendar_type_enum = ?");
        sb.append(" inner join m_mandatory_savings_schedule ms on ms.savings_account_id = dat.savings_account_id and ms.duedate > ?");
        sb.append(" where dat.deposit_period is null");
        sb.append(" group by ms.savings_account_id");

        return this.jdbcTemplate.queryForList(sb.toString(), SavingsAccountStatusType.ACTIVE.getValue(),
                CalendarEntityType.SAVINGS.getValue(), CalendarType.COLLECTION.getValue(),
                formatter.print(DateUtils.getLocalDateOfTenant()));
    }

    private static abstract class DepositAccountMapper implements RowMapper<DepositAccountData> {

        private final String selectFieldsSql;
        private final String selectTablesSql;

        @Override
        public abstract DepositAccountData mapRow(ResultSet rs, int rowNum) throws SQLException;

        public DepositAccountMapper() {
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
            selectFieldsSqlBuilder
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
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

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

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

            final Integer depositTypeId = JdbcSupport.getInteger(rs, "depositTypeId");
            final EnumOptionData depositType = (depositTypeId == null) ? null : SavingsEnumerations.depositType(depositTypeId);

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

            final boolean withdrawalFeeForTransfers = rs.getBoolean("withdrawalFeeForTransfers");
            final BigDecimal minBalanceForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "minBalanceForInterestCalculation");

            final BigDecimal totalDeposits = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalDeposits");
            final BigDecimal totalWithdrawals = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithdrawals");
            final BigDecimal totalWithdrawalFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithdrawalFees");
            final BigDecimal totalAnnualFees = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalAnnualFees");

            final BigDecimal totalInterestEarned = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalInterestEarned");
            final BigDecimal totalInterestPosted = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalInterestPosted");
            final BigDecimal accountBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "accountBalance");
            final BigDecimal totalFeeCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalFeeCharge");
            final BigDecimal totalPenaltyCharge = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalPenaltyCharge");
            final BigDecimal totalWithholdTax = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalWithholdTax");
            final BigDecimal totalOverdraftInterestDerived = null;
            
            final boolean withHoldTax = rs.getBoolean("withHoldTax");
            final Long taxGroupId = JdbcSupport.getLong(rs, "taxGroupId");
            final String taxGroupName = rs.getString("taxGroupName");
            TaxGroupData taxGroupData = null;
            if (taxGroupId != null) {
                taxGroupData = TaxGroupData.lookup(taxGroupId, taxGroupName);
            }

            final BigDecimal availableBalance = null;
            final SavingsAccountSummaryData summary = new SavingsAccountSummaryData(currency, totalDeposits, totalWithdrawals,
                    totalWithdrawalFees, totalAnnualFees, totalInterestEarned, totalInterestPosted, accountBalance, totalFeeCharge,
                    totalPenaltyCharge, totalOverdraftInterestDerived, totalWithholdTax, null, null, availableBalance);

            return DepositAccountData.instance(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                    fieldOfficerId, fieldOfficerName, status, timeline, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                    interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                    lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, summary, depositType,
                    minBalanceForInterestCalculation, withHoldTax, taxGroupData);
        }
    }

    private static class FixedDepositAccountMapper extends DepositAccountMapper {

        private final String schemaSql;

        public FixedDepositAccountMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(super.selectFieldsSql());

            sqlBuilder.append(", datp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
            sqlBuilder.append("datp.pre_closure_penal_interest as preClosurePenalInterest, ");
            sqlBuilder.append("datp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
            sqlBuilder.append("datp.min_deposit_term as minDepositTerm, ");
            sqlBuilder.append("datp.max_deposit_term as maxDepositTerm, ");
            sqlBuilder.append("datp.min_deposit_term_type_enum as minDepositTermTypeId, ");
            sqlBuilder.append("datp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
            sqlBuilder.append("datp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
            sqlBuilder.append("datp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId, ");
            sqlBuilder.append("datp.deposit_amount as depositAmount, ");
            sqlBuilder.append("datp.maturity_amount as maturityAmount, ");
            sqlBuilder.append("datp.maturity_date as maturityDate, ");
            sqlBuilder.append("datp.deposit_period as depositPeriod, ");
            sqlBuilder.append("datp.deposit_period_frequency_enum as depositPeriodFrequencyTypeId, ");
            sqlBuilder.append("datp.on_account_closure_enum as onAccountClosureId, ");
            sqlBuilder.append("datp.transfer_interest_to_linked_account as transferInterestToSavings ");

            sqlBuilder.append(super.selectTablesSql());

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public String schema() {
            return this.schemaSql;
        }

        @Override
        public FixedDepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final DepositAccountData depositAccountData = super.mapRow(rs);
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

            final BigDecimal depositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "depositAmount");
            final BigDecimal maturityAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maturityAmount");
            final LocalDate maturityDate = JdbcSupport.getLocalDate(rs, "maturityDate");
            final Integer depositPeriod = JdbcSupport.getInteger(rs, "depositPeriod");
            final Integer depositPeriodFrequencyTypeId = JdbcSupport.getInteger(rs, "depositPeriodFrequencyTypeId");
            final EnumOptionData depositPeriodFrequencyType = (depositPeriodFrequencyTypeId == null) ? null : SavingsEnumerations
                    .depositPeriodFrequency(depositPeriodFrequencyTypeId);

            final Integer onAccountClosureId = JdbcSupport.getInteger(rs, "onAccountClosureId");
            final EnumOptionData onAccountClosureType = (onAccountClosureId == null) ? null : SavingsEnumerations
                    .depositAccountOnClosureType(onAccountClosureId);
            final Boolean transferInterestToSavings = rs.getBoolean("transferInterestToSavings");

            return FixedDepositAccountData.instance(depositAccountData, preClosurePenalApplicable, preClosurePenalInterest,
                    preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                    inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                    depositPeriodFrequencyType, onAccountClosureType, transferInterestToSavings);
        }
    }

    private static class RecurringDepositAccountMapper extends DepositAccountMapper {

        private final String schemaSql;

        public RecurringDepositAccountMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(this.selectFieldsSql());

            sqlBuilder.append(", datp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
            sqlBuilder.append("datp.pre_closure_penal_interest as preClosurePenalInterest, ");
            sqlBuilder.append("datp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
            sqlBuilder.append("dard.mandatory_recommended_deposit_amount as mandatoryRecommendedDepositAmount, ");
            sqlBuilder.append("dard.total_overdue_amount as totalOverdueAmount, ");
            sqlBuilder.append("dard.no_of_overdue_installments as noOfOverdueInstallments, ");
            sqlBuilder.append("dard.is_mandatory as isMandatoryDeposit, ");
            sqlBuilder.append("dard.allow_withdrawal as allowWithdrawal, ");
            sqlBuilder.append("dard.adjust_advance_towards_future_payments as adjustAdvanceTowardsFuturePayments, ");
            sqlBuilder.append("dard.is_calendar_inherited as isCalendarInherited, ");
            sqlBuilder.append("datp.min_deposit_term as minDepositTerm, ");
            sqlBuilder.append("datp.max_deposit_term as maxDepositTerm, ");
            sqlBuilder.append("datp.min_deposit_term_type_enum as minDepositTermTypeId, ");
            sqlBuilder.append("datp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
            sqlBuilder.append("datp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
            sqlBuilder.append("datp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId, ");
            sqlBuilder.append("datp.deposit_amount as depositAmount, ");
            sqlBuilder.append("datp.maturity_amount as maturityAmount, ");
            sqlBuilder.append("datp.expected_firstdepositon_date as expectedFirstDepositOnDate, ");
            sqlBuilder.append("datp.maturity_date as maturityDate, ");
            sqlBuilder.append("datp.deposit_period as depositPeriod, ");
            sqlBuilder.append("datp.deposit_period_frequency_enum as depositPeriodFrequencyTypeId, ");
            sqlBuilder.append("datp.on_account_closure_enum as onAccountClosureId ");

            sqlBuilder.append(this.selectTablesSql());
            sqlBuilder.append("left join m_deposit_account_recurring_detail dard on sa.id = dard.savings_account_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public String schema() {
            return this.schemaSql;
        }

        @Override
        public RecurringDepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final DepositAccountData depositAccountData = super.mapRow(rs);

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

            final BigDecimal depositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "depositAmount");
            final BigDecimal maturityAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maturityAmount");
            final LocalDate maturityDate = JdbcSupport.getLocalDate(rs, "maturityDate");
            final Integer depositPeriod = JdbcSupport.getInteger(rs, "depositPeriod");
            final Integer depositPeriodFrequencyTypeId = JdbcSupport.getInteger(rs, "depositPeriodFrequencyTypeId");
            final EnumOptionData depositPeriodFrequencyType = (depositPeriodFrequencyTypeId == null) ? null : SavingsEnumerations
                    .depositPeriodFrequency(depositPeriodFrequencyTypeId);
            final BigDecimal mandatoryRecommendedDepositAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "mandatoryRecommendedDepositAmount");
            final BigDecimal totalOverdueAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "totalOverdueAmount");
            final Integer noOfOverdueInstallments = JdbcSupport.getInteger(rs, "noOfOverdueInstallments");
            final boolean isMandatoryDeposit = rs.getBoolean("isMandatoryDeposit");
            final boolean allowWithdrawal = rs.getBoolean("allowWithdrawal");
            final boolean adjustAdvanceTowardsFuturePayments = rs.getBoolean("adjustAdvanceTowardsFuturePayments");
            final boolean isCalendarInherited = rs.getBoolean("isCalendarInherited");

            final Integer onAccountClosureId = JdbcSupport.getInteger(rs, "onAccountClosureId");
            final EnumOptionData onAccountClosureType = (onAccountClosureId == null) ? null : SavingsEnumerations
                    .depositAccountOnClosureType(onAccountClosureId);
            final LocalDate expectedFirstDepositOnDate = JdbcSupport.getLocalDate(rs, "expectedFirstDepositOnDate");

            return RecurringDepositAccountData.instance(depositAccountData, preClosurePenalApplicable, preClosurePenalInterest,
                    preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                    inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                    depositPeriodFrequencyType, mandatoryRecommendedDepositAmount, onAccountClosureType, expectedFirstDepositOnDate,
                    totalOverdueAmount, noOfOverdueInstallments, isMandatoryDeposit, allowWithdrawal, adjustAdvanceTowardsFuturePayments,
                    isCalendarInherited);

        }
    }

    private static final class DepositAccountLookupMapper implements RowMapper<DepositAccountData> {

        public String schema() {
            return " sa.id as id, sa.account_no as accountNumber, sa.deposit_type_enum as depositTypeId from m_savings_account sa ";
        }

        @Override
        public DepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("accountNumber");
            final Integer depositTypeId = JdbcSupport.getInteger(rs, "depositTypeId");
            final EnumOptionData depositType = (depositTypeId == null) ? null : SavingsEnumerations.depositType(depositTypeId);

            return DepositAccountData.lookup(id, name, depositType);
        }
    }

    private DepositAccountMapper getDepositAccountMapper(final DepositAccountType depositAccountType) {
        if (depositAccountType.isFixedDeposit()) {
            return this.fixedDepositAccountRowMapper;
        } else if (depositAccountType.isRecurringDeposit()) { return this.recurringDepositAccountRowMapper; }
        return null;
    }

    private static final class SavingsAccountTransactionsMapper implements RowMapper<SavingsAccountTransactionData> {

        private final String schemaSql;

        public SavingsAccountTransactionsMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("tr.id as transactionId, tr.transaction_type_enum as transactionType, ");
            sqlBuilder.append("tr.transaction_date as transactionDate, tr.amount as transactionAmount,");
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
            sqlBuilder.append("pt.value as paymentTypeName ");
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
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
            final BigDecimal outstandingChargeAmount = null;
            final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "runningBalance");
            final boolean reversed = rs.getBoolean("reversed");

            final Long savingsId = rs.getLong("savingsId");
            final String accountNo = rs.getString("accountNo");

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
            final boolean postInterestAsOn = false;
            return SavingsAccountTransactionData.create(id, transactionType, paymentDetailData, savingsId, accountNo, date, currency,
                    amount, outstandingChargeAmount, runningBalance, reversed, transfer, postInterestAsOn);
        }
    }

    private static abstract class DepositAccountTemplateMapper implements RowMapper<DepositAccountData> {

        private final String selectFieldsSql;
        private final String selectTablesSql;

        private final ClientData client;
        private final GroupGeneralData group;

        public DepositAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
            this.client = client;
            this.group = group;

            final StringBuilder selectFieldsSqlBuilder = new StringBuilder(400);
            selectFieldsSqlBuilder.append("sa.id as productId, sa.name as productName, ");
            selectFieldsSqlBuilder
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
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

        @Override
        public abstract DepositAccountData mapRow(final ResultSet rs, final int rowNum) throws SQLException;

        public DepositAccountData mapRow(final ResultSet rs) throws SQLException {

            final Integer depositTypeId = JdbcSupport.getInteger(rs, "depositTypeId");
            final EnumOptionData depositType = (depositTypeId == null) ? null : SavingsEnumerations.depositType(depositTypeId);
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

            final boolean withdrawalFeeForTransfers = rs.getBoolean("withdrawalFeeForTransfers");
            final BigDecimal minBalanceForInterestCalculation = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                    "minBalanceForInterestCalculation");

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
            final SavingsAccountSummaryData summary = null;
            final SavingsAccountApplicationTimelineData timeline = SavingsAccountApplicationTimelineData.templateDefault();
            
            final boolean withHoldTax = rs.getBoolean("withHoldTax");
            final Long taxGroupId = JdbcSupport.getLong(rs, "taxGroupId");
            final String taxGroupName = rs.getString("taxGroupName");
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

    private static class FixedDepositAccountTemplateMapper extends DepositAccountTemplateMapper {

        private final String schemaSql;

        public FixedDepositAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
            super(client, group);
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(super.selectFieldsSql());

            sqlBuilder.append(", dptp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest as preClosurePenalInterest, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
            sqlBuilder.append("dptp.min_deposit_term as minDepositTerm, ");
            sqlBuilder.append("dptp.max_deposit_term as maxDepositTerm, ");
            sqlBuilder.append("dptp.min_deposit_term_type_enum as minDepositTermTypeId, ");
            sqlBuilder.append("dptp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId ");

            sqlBuilder.append(super.selectTablesSql());

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public String schema() {
            return this.schemaSql;
        }

        @Override
        public FixedDepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final DepositAccountData depositAccountData = super.mapRow(rs);

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

            final BigDecimal depositAmount = null;
            final BigDecimal maturityAmount = null;
            final LocalDate maturityDate = null;
            final Integer depositPeriod = null;
            final EnumOptionData depositPeriodFrequencyType = null;
            final EnumOptionData onAccountClosureType = null;
            final Boolean transferInterestToSavings = false;

            return FixedDepositAccountData.instance(depositAccountData, preClosurePenalApplicable, preClosurePenalInterest,
                    preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                    inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                    depositPeriodFrequencyType, onAccountClosureType, transferInterestToSavings);
        }
    }

    private static class RecurringDepositAccountTemplateMapper extends DepositAccountTemplateMapper {

        private final String schemaSql;

        public RecurringDepositAccountTemplateMapper(final ClientData client, final GroupGeneralData group) {
            super(client, group);
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append(this.selectFieldsSql());

            sqlBuilder.append(", dptp.pre_closure_penal_applicable as preClosurePenalApplicable, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest as preClosurePenalInterest, ");
            sqlBuilder.append("dptp.pre_closure_penal_interest_on_enum as preClosurePenalInterestOnId, ");
            sqlBuilder.append("dprd.is_mandatory as isMandatoryDeposit, ");
            sqlBuilder.append("dprd.allow_withdrawal as allowWithdrawal, ");
            sqlBuilder.append("dprd.adjust_advance_towards_future_payments as adjustAdvanceTowardsFuturePayments, ");
            sqlBuilder.append("dptp.min_deposit_term as minDepositTerm, ");
            sqlBuilder.append("dptp.max_deposit_term as maxDepositTerm, ");
            sqlBuilder.append("dptp.min_deposit_term_type_enum as minDepositTermTypeId, ");
            sqlBuilder.append("dptp.max_deposit_term_type_enum as maxDepositTermTypeId, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term as inMultiplesOfDepositTerm, ");
            sqlBuilder.append("dptp.in_multiples_of_deposit_term_type_enum as inMultiplesOfDepositTermTypeId ");

            sqlBuilder.append(this.selectTablesSql());
            sqlBuilder.append("left join m_deposit_product_recurring_detail dprd on sa.id = dprd.savings_product_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        @Override
        public String schema() {
            return this.schemaSql;
        }

        @Override
        public RecurringDepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final DepositAccountData depositAccountData = super.mapRow(rs);

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
            final boolean isMandatoryDeposit = rs.getBoolean("isMandatoryDeposit");
            final boolean allowWithdrawal = rs.getBoolean("allowWithdrawal");
            final boolean adjustAdvanceTowardsFuturePayments = rs.getBoolean("adjustAdvanceTowardsFuturePayments");
            final boolean isCalendarInherited = false;

            final BigDecimal depositAmount = null;
            final BigDecimal maturityAmount = null;
            final LocalDate maturityDate = null;
            final Integer depositPeriod = null;
            final EnumOptionData depositPeriodFrequencyType = null;
            final LocalDate expectedFirstDepositOnDate = null;
            final BigDecimal mandatoryRecommendedDepositAmount = null;
            final EnumOptionData onAccountClosureType = null;
            final BigDecimal totalOverdueAmount = null;
            final Integer noOfOverdueInstallments = null;

            return RecurringDepositAccountData.instance(depositAccountData, preClosurePenalApplicable, preClosurePenalInterest,
                    preClosurePenalInterestOnType, minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                    inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, depositAmount, maturityAmount, maturityDate, depositPeriod,
                    depositPeriodFrequencyType, mandatoryRecommendedDepositAmount, onAccountClosureType, expectedFirstDepositOnDate,
                    totalOverdueAmount, noOfOverdueInstallments, isMandatoryDeposit, allowWithdrawal, adjustAdvanceTowardsFuturePayments,
                    isCalendarInherited);
        }
    }

    private DepositAccountTemplateMapper getDepositAccountTemplaMapper(final DepositAccountType depositAccountType, ClientData client,
            GroupGeneralData group) {
        if (depositAccountType.isFixedDeposit()) {
            return new FixedDepositAccountTemplateMapper(client, group);
        } else if (depositAccountType.isRecurringDeposit()) { return new RecurringDepositAccountTemplateMapper(client, group); }
        return null;
    }

    private Collection<SavingsAccountChargeData> fromChargesToSavingsCharges(final Collection<ChargeData> productCharges) {
        final Collection<SavingsAccountChargeData> savingsCharges = new ArrayList<>();
        for (final ChargeData chargeData : productCharges) {
            final SavingsAccountChargeData savingsCharge = chargeData.toSavingsAccountChargeData();
            savingsCharges.add(savingsCharge);
        }
        return savingsCharges;
    }

    private static final class DepositAccountForMaturityMapper implements RowMapper<DepositAccountData> {

        private final String schemaSql;

        public DepositAccountForMaturityMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("da.id as id, ");
            sqlBuilder.append("da.account_no as accountNumber, ");
            sqlBuilder.append("da.deposit_type_enum as depositTypeId ");
            sqlBuilder.append("FROM m_savings_account da ");
            sqlBuilder.append("inner join m_deposit_account_term_and_preclosure dat on dat.savings_account_id = da.id ");
            sqlBuilder.append("and dat.maturity_date is not null and dat.maturity_date <= ? ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public DepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("accountNumber");
            final Integer depositTypeId = JdbcSupport.getInteger(rs, "depositTypeId");
            final EnumOptionData depositType = (depositTypeId == null) ? null : SavingsEnumerations.depositType(depositTypeId);

            return DepositAccountData.lookup(id, name, depositType);
        }
    }

    private static final class RecurringAccountDepositTransactionTemplateMapper implements RowMapper<SavingsAccountTransactionData> {

        private final String schemaSql;

        public RecurringAccountDepositTransactionTemplateMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
            sqlBuilder
                    .append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("sa.account_balance_derived as runningBalance, ");
            sqlBuilder
                    .append("mss.duedate as duedate, (mss.deposit_amount - ifnull(mss.deposit_amount_completed_derived,0)) as dueamount, ");
            sqlBuilder.append("IFNULL(sac.amount_outstanding_derived,0.0) AS outstandingChargeAmount ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_mandatory_savings_schedule mss  on mss.savings_account_id=sa.id and mss.completed_derived = false ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
            sqlBuilder.append("LEFT JOIN(SELECT s.savings_account_id AS savings_account_id ");
            sqlBuilder.append(",SUM(IFNULL(s.amount_outstanding_derived,0.0)) AS amount_outstanding_derived  ");
            sqlBuilder.append("FROM m_savings_account_charge s  ");
            sqlBuilder.append("JOIN m_charge c ON c.id = s.charge_id AND c.charge_time_enum = 3 ");
            sqlBuilder.append("WHERE s.savings_account_id = ? ");
            sqlBuilder.append("AND s.is_active = TRUE GROUP BY s.savings_account_id)sac ON sac.savings_account_id = sa.id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long savingsId = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final BigDecimal dueamount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "dueamount");
            final BigDecimal outstandingChargeAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "outstandingChargeAmount");
            final LocalDate duedate = JdbcSupport.getLocalDate(rs, "duedate");
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);
            final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations
                    .transactionType(SavingsAccountTransactionType.DEPOSIT.getValue());
            final PaymentDetailData paymentDetailData = null;
            final AccountTransferData transfer = null;
            final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "runningBalance");
            final boolean postInterestAsOn = false;
            return SavingsAccountTransactionData.create(savingsId, transactionType, paymentDetailData, savingsId, accountNo, duedate,
                    currency, dueamount, outstandingChargeAmount, runningBalance, false, transfer, postInterestAsOn);
        }
    }

    private class AccountTransferMapper implements RowMapper<AccountTransferDTO> {

        private final String schemaSql;

        public AccountTransferMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder
                    .append("sa.id as fromAcc ,aa.linked_savings_account_id as toAcc,st.amount as amount, st.transaction_date as transactionDate ")
                    .append(" from m_deposit_account_term_and_preclosure da ")
                    .append(" inner join m_savings_account sa on da.savings_account_id = sa.id")
                    .append(" inner join m_savings_account_transaction st on st.savings_account_id = sa.id")
                    .append(" inner join m_portfolio_account_associations aa on aa.savings_account_id=sa.id");
            schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public AccountTransferDTO mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long fromAccountId = rs.getLong("fromAcc");
            final Long toAccountId = rs.getLong("toAcc");
            final BigDecimal transactionAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "amount");
            final boolean isRegularTransaction = false;
            final boolean isExceptionForBalanceCheck = false;
            final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transactionDate");
            return new AccountTransferDTO(transactionDate, transactionAmount, PortfolioAccountType.SAVINGS, PortfolioAccountType.SAVINGS,
                    fromAccountId, toAccountId, "trasfer interest to savings", null, null, null, null, null, null, null,
                    AccountTransferType.INTEREST_TRANSFER.getValue(), null, null, null, null, null, null, isRegularTransaction,
                    isExceptionForBalanceCheck);
        }

    }
}