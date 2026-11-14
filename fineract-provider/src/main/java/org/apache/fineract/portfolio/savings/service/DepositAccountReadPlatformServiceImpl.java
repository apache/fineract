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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.charge.util.ConvertChargeDataToSpecificChargeData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.data.DepositAccountInterestRateChartData;
import org.apache.fineract.portfolio.savings.data.DepositProductData;
import org.apache.fineract.portfolio.savings.data.FixedDepositAccountData;
import org.apache.fineract.portfolio.savings.data.RecurringDepositAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.exception.DepositAccountNotFoundException;
import org.apache.fineract.portfolio.savings.mapper.AccountTransferMapper;
import org.apache.fineract.portfolio.savings.mapper.DepositAccountForMaturityMapper;
import org.apache.fineract.portfolio.savings.mapper.DepositAccountLookupMapper;
import org.apache.fineract.portfolio.savings.mapper.DepositAccountMapper;
import org.apache.fineract.portfolio.savings.mapper.DepositAccountTemplateMapper;
import org.apache.fineract.portfolio.savings.mapper.FixedDepositAccountMapper;
import org.apache.fineract.portfolio.savings.mapper.FixedDepositAccountTemplateMapper;
import org.apache.fineract.portfolio.savings.mapper.RecurringAccountDepositTransactionTemplateMapper;
import org.apache.fineract.portfolio.savings.mapper.RecurringDepositAccountMapper;
import org.apache.fineract.portfolio.savings.mapper.RecurringDepositAccountTemplateMapper;
import org.apache.fineract.portfolio.savings.mapper.SavingsAccountTransactionsMapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
public class DepositAccountReadPlatformServiceImpl implements DepositAccountReadPlatformService {

    private static final FixedDepositAccountMapper FIXED_DEPOSIT_ACCOUNT_MAPPER = new FixedDepositAccountMapper();
    private static final RecurringDepositAccountMapper RECURRING_DEPOSIT_ACCOUNT_MAPPER = new RecurringDepositAccountMapper();
    private static final DepositAccountLookupMapper DEPOSIT_ACCOUNT_LOOKUP_MAPPER = new DepositAccountLookupMapper();
    private static final DepositAccountForMaturityMapper DEPOSIT_ACCOUNT_FOR_MATURITY_MAPPER = new DepositAccountForMaturityMapper();
    // allowed column names for sorting the query result
    private static final Set<String> supportedOrderByValues = new HashSet<>(Arrays.asList("id", "accountNumbr", "officeId", "officeName"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final DepositAccountInterestRateChartReadPlatformService accountChartReadPlatformService;
    private final InterestRateChartReadService productChartReadPlatformService;
    private final PaginationParametersDataValidator paginationParametersDataValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final PaginationHelper paginationHelper;
    private final SavingsAccountTransactionsMapper transactionsMapper = new SavingsAccountTransactionsMapper();
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final SavingsDropdownReadPlatformService savingsDropdownReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final StaffReadService staffReadPlatformService;
    private final DepositsDropdownReadPlatformService depositsDropdownReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final RecurringAccountDepositTransactionTemplateMapper rdTransactionTemplateMapper = new RecurringAccountDepositTransactionTemplateMapper();
    private final DropdownReadPlatformService dropdownReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final PaymentTypeReadService paymentTypeReadPlatformService;

    @Override
    public Collection<DepositAccountData> retrieveAll(final DepositAccountType depositAccountType,
            final PaginationParameters paginationParameters) {

        this.context.authenticatedUser();
        this.paginationParametersDataValidator.validateParameterValues(paginationParameters, supportedOrderByValues,
                depositAccountType.resourceName());

        final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
        if (depositAccountMapper == null) {
            return null;
        }

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ");
        sqlBuilder.append(depositAccountMapper.schema());
        sqlBuilder.append(" where sa.deposit_type_enum = ? ");
        // always append at the end of a sql statement
        sqlBuilder.append(paginationParameters.paginationSql());

        return jdbcTemplate.query(sqlBuilder.toString(), depositAccountMapper, new Object[] { depositAccountType.getValue() }); // NOSONAR
    }

    @Override
    public Page<DepositAccountData> retrieveAllPaged(final DepositAccountType depositAccountType,
            final PaginationParameters paginationParameters) {

        this.paginationParametersDataValidator.validateParameterValues(paginationParameters, supportedOrderByValues,
                depositAccountType.resourceName());

        final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
        if (depositAccountMapper == null) {
            return null;
        }

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(depositAccountMapper.schema());
        sqlBuilder.append(" where sa.deposit_type_enum = ? ");
        sqlBuilder.append(paginationParameters.paginationSql());

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), new Object[] { depositAccountType.getValue() },
                depositAccountMapper);
    }

    @Override
    public Collection<DepositAccountData> retrieveAllForLookup(final DepositAccountType depositAccountType) {

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ");
        sqlBuilder.append(DEPOSIT_ACCOUNT_LOOKUP_MAPPER.schema());
        sqlBuilder.append(" where sa.deposit_type_enum = ? ");

        return this.jdbcTemplate.query(sqlBuilder.toString(), DEPOSIT_ACCOUNT_LOOKUP_MAPPER, depositAccountType.getValue());
    }

    @Override
    public Collection<DepositAccountData> retrieveForMaturityUpdate() {

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("SELECT ");
        sqlBuilder.append(DEPOSIT_ACCOUNT_FOR_MATURITY_MAPPER.schema());
        sqlBuilder.append(" WHERE da.deposit_type_enum in (?, ?) and da.status_enum = ?");

        return this.jdbcTemplate.query(sqlBuilder.toString(), DEPOSIT_ACCOUNT_FOR_MATURITY_MAPPER,
                DepositAccountType.FIXED_DEPOSIT.getValue(), DepositAccountType.RECURRING_DEPOSIT.getValue(),
                SavingsAccountStatusType.ACTIVE.getValue());
    }

    @Override
    public DepositAccountData retrieveOne(final DepositAccountType depositAccountType, final Long accountId) {
        try {
            this.context.authenticatedUser();

            final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
            if (depositAccountMapper == null) {
                return null;
            }

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("select ");
            sqlBuilder.append(depositAccountMapper.schema());
            sqlBuilder.append(" where sa.id = ? and sa.deposit_type_enum = ? ");

            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), depositAccountMapper, accountId, depositAccountType.getValue());

        } catch (final EmptyResultDataAccessException e) {
            throw new DepositAccountNotFoundException(depositAccountType, accountId, e);
        }
    }

    @Override
    public DepositAccountData retrieveOneWithClosureTemplate(final DepositAccountType depositAccountType, final Long accountId) {
        try {
            this.context.authenticatedUser();

            final DepositAccountMapper depositAccountMapper = this.getDepositAccountMapper(depositAccountType);
            if (depositAccountMapper == null) {
                return null;
            }

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("select ");
            sqlBuilder.append(depositAccountMapper.schema());
            sqlBuilder.append(" where sa.id = ? and sa.deposit_type_enum = ? ");

            DepositAccountData account = this.jdbcTemplate.queryForObject(sqlBuilder.toString(), depositAccountMapper, accountId,
                    depositAccountType.getValue());
            Collection<EnumOptionData> onAccountClosureOptions = SavingsEnumerations
                    .depositAccountOnClosureType(DepositAccountOnClosureType.values());
            final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
            final Collection<SavingsAccountData> savingsAccountDatas = this.savingsAccountReadPlatformService
                    .retrieveActiveForLookup(account.getClientId(), DepositAccountType.SAVINGS_DEPOSIT);
            if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {
                account = FixedDepositAccountData.withClosureTemplateDetails((FixedDepositAccountData) account, onAccountClosureOptions,
                        paymentTypeOptions, savingsAccountDatas);
            } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {
                account = RecurringDepositAccountData.withClosureTemplateDetails((RecurringDepositAccountData) account,
                        onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas);
            }

            return account;

        } catch (final EmptyResultDataAccessException e) {
            throw new DepositAccountNotFoundException(depositAccountType, accountId, e);
        }
    }

    @Override
    public DepositAccountData retrieveOneWithChartSlabs(final DepositAccountType depositAccountType, Long accountId) {
        DepositAccountData depositAccount = this.retrieveOne(depositAccountType, accountId);
        DepositAccountInterestRateChartData chart = this.accountChartReadPlatformService.retrieveOneWithSlabsOnAccountId(accountId);

        if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {
            depositAccount = FixedDepositAccountData.withInterestChart((FixedDepositAccountData) depositAccount, chart);
        } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {
            CalendarData calendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(accountId,
                    CalendarEntityType.SAVINGS.getValue());
            final Integer frequency = calendar.getInterval() == -1 ? 1 : calendar.getInterval();
            final CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.fromInt(calendar.getFrequency().getId().intValue());
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

        return this.jdbcTemplate.query(sql, this.transactionsMapper, new Object[] { accountId, depositAccountType.getValue() }); // NOSONAR
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
            officeId = client.getOfficeId();
            savingsAccountDatas = this.savingsAccountReadPlatformService.retrieveActiveForLookup(clientId,
                    DepositAccountType.SAVINGS_DEPOSIT);
        }

        GroupGeneralData group = null;
        if (groupId != null) {
            group = this.groupReadPlatformService.retrieveOne(groupId);
            officeId = group.getOfficeId();
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
            template = this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { productId, depositAccountType.getValue() }); // NOSONAR

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
            final Collection<SavingsAccountChargeData> charges = retrieveSavingsChargesForProduct(productId, officeId);

            final boolean feeChargesOnly = false;
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService
                    .retrieveSavingsProductApplicableCharges(feeChargesOnly, officeId);

            final Collection<EnumOptionData> maturityInstructionOptions = this.depositsDropdownReadPlatformService
                    .maturityInstructionOptions();

            final Collection<StaffData> fieldOfficerOptions = retrieveFieldOfficerOptions(officeId, staffInSelectedOfficeOnly);

            // retrieve chart Slabs
            final InterestRateChartData productChartData = this.productChartReadPlatformService.retrieveActiveChartWithTemplate(productId);
            final DepositAccountInterestRateChartData accountChart = DepositAccountInterestRateChartData.from(productChartData);

            if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {

                template = FixedDepositAccountData.withTemplateOptions((FixedDepositAccountData) template, productOptions,
                        fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                        interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                        withdrawalFeeTypeOptions, transactions, charges, chargeOptions, preClosurePenalInterestOnTypeOptions,
                        periodFrequencyTypeOptions, savingsAccountDatas, maturityInstructionOptions);

                template = FixedDepositAccountData.withInterestChart((FixedDepositAccountData) template, accountChart);
            } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {
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
                clientName = client.getDisplayName();
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
            final Collection<EnumOptionData> maturityInstructionOptions = null;

            final boolean feeChargesOnly = true;
            final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService
                    .retrieveSavingsProductApplicableCharges(feeChargesOnly, officeId);

            if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {

                template = FixedDepositAccountData.withClientTemplate(clientId, clientName, groupId, groupName);

                template = FixedDepositAccountData.withTemplateOptions((FixedDepositAccountData) template, productOptions,
                        fieldOfficerOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                        interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                        withdrawalFeeTypeOptions, transactions, charges, chargeOptions, preClosurePenalInterestOnTypeOptions,
                        periodFrequencyTypeOptions, savingsAccountDatas, maturityInstructionOptions);
            } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {

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
            return this.jdbcTemplate.queryForObject(sql, this.rdTransactionTemplateMapper, // NOSONAR
                    accountId, accountId, DepositAccountType.RECURRING_DEPOSIT.getValue());
        } catch (final EmptyResultDataAccessException e) {
            throw new DepositAccountNotFoundException(DepositAccountType.RECURRING_DEPOSIT, accountId, e);
        }
    }

    @Override
    public Collection<AccountTransferDTO> retrieveDataForInterestTransfer() {
        final StringBuilder sqlBuilder = new StringBuilder(300);
        AccountTransferMapper mapper = new AccountTransferMapper();
        sqlBuilder.append("SELECT ");
        sqlBuilder.append(mapper.schema());
        sqlBuilder.append(" where da.transfer_interest_to_linked_account = true and ");
        sqlBuilder.append(
                "st.transaction_date > (select coalesce(max(sat.transaction_date),sa.activatedon_date) from m_savings_account_transaction sat where sat.transaction_type_enum = ? and sat.savings_account_id = sa.id and sat.is_reversed=false) ");
        sqlBuilder.append(
                "and st.transaction_type_enum = ? and sa.status_enum = ? and st.is_reversed=false and st.transaction_date > coalesce(sa.lockedin_until_date_derived,sa.activatedon_date)");

        return this.jdbcTemplate.query(sqlBuilder.toString(), mapper, SavingsAccountTransactionType.WITHDRAWAL.getValue(),
                SavingsAccountTransactionType.INTEREST_POSTING.getValue(), SavingsAccountStatusType.ACTIVE.getValue());
    }

    @Override
    public Collection<Map<String, Object>> retriveDataForRDScheduleCreation() {
        String today = DATE_TIME_FORMATTER.format(DateUtils.getBusinessLocalDate());
        final StringBuilder sb = new StringBuilder(300);
        sb.append(" select rd.savings_account_id savingsId, rd.mandatory_recommended_deposit_amount as amount,");
        sb.append(" mc.recurrence as recurrence ,");
        sb.append(" max(ms.duedate) as dueDate , max(ms.installment) as installment,");
        sb.append(" count(ms.installment) as futureInstallments");
        sb.append(" from m_deposit_account_term_and_preclosure dat ");
        sb.append(" inner join m_savings_account sa on sa.id = dat.savings_account_id and sa.status_enum = ?");
        sb.append(" inner join m_deposit_account_recurring_detail rd on rd.savings_account_id = dat.savings_account_id ");
        sb.append(" inner join m_calendar_instance mci on mci.entity_type_enum = ? and mci.entity_id = dat.savings_account_id  ");
        sb.append(" inner join m_calendar mc  on mc.id = mci.calendar_id and mc.calendar_type_enum = ?");
        sb.append(" inner join m_mandatory_savings_schedule ms on ms.savings_account_id = dat.savings_account_id and ms.duedate > '" + today
                + "'");
        sb.append(" where dat.deposit_period is null");
        sb.append(" group by ms.savings_account_id, rd.mandatory_recommended_deposit_amount, mc.recurrence, rd.savings_account_id");

        return this.jdbcTemplate.queryForList(sb.toString(), SavingsAccountStatusType.ACTIVE.getValue(),
                CalendarEntityType.SAVINGS.getValue(), CalendarType.COLLECTION.getValue());
    }

    private DepositAccountMapper getDepositAccountMapper(final DepositAccountType depositAccountType) {
        if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {
            return FIXED_DEPOSIT_ACCOUNT_MAPPER;
        } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {
            return RECURRING_DEPOSIT_ACCOUNT_MAPPER;
        }
        return null;
    }

    private DepositAccountTemplateMapper getDepositAccountTemplaMapper(final DepositAccountType depositAccountType, ClientData client,
            GroupGeneralData group) {
        if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {
            return new FixedDepositAccountTemplateMapper(client, group);
        } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {
            return new RecurringDepositAccountTemplateMapper(client, group);
        }
        return null;
    }

    private Collection<SavingsAccountChargeData> fromChargesToSavingsCharges(final Collection<ChargeData> productCharges) {
        final Collection<SavingsAccountChargeData> savingsCharges = new ArrayList<>();
        for (final ChargeData chargeData : productCharges) {
            final SavingsAccountChargeData savingsCharge = ConvertChargeDataToSpecificChargeData.toSavingsAccountChargeData(chargeData);
            savingsCharges.add(savingsCharge);
        }
        return savingsCharges;
    }

    private Collection<StaffData> retrieveFieldOfficerOptions(final Long officeId, final boolean staffInSelectedOfficeOnly) {
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
        return fieldOfficerOptions;
    }

    private Collection<SavingsAccountChargeData> retrieveSavingsChargesForProduct(final Long productId, final Long officeId) {
        final Collection<ChargeData> productCharges = this.chargeReadPlatformService.retrieveSavingsProductCharges(productId, officeId);
        // update charges from Product charges
        return fromChargesToSavingsCharges(productCharges);
    }
}
