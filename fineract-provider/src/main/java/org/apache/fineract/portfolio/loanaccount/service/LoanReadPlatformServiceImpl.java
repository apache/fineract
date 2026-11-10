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
package org.apache.fineract.portfolio.loanaccount.service;

import static java.lang.Boolean.TRUE;
import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.common.service.CommonEnumerations;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadService;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.data.GroupRoleData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanTransactionApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.LoanRepaymentScheduleInstallmentData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionRelationData;
import org.apache.fineract.portfolio.loanaccount.data.OutstandingAmountsDTO;
import org.apache.fineract.portfolio.loanaccount.data.PaidInAdvanceData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.domain.reamortization.LoanReAmortizationInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.SemiMonthlyScheduleDates;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionMapper;
import org.apache.fineract.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanForeclosureValidator;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReadPlatformServiceImpl implements LoanReadPlatformService, LoanReadPlatformServiceCommon {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final LoanDropdownReadPlatformService loanDropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final StaffReadService staffReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final PaymentTypeReadService paymentTypeReadPlatformService;
    private final FloatingRatesReadService floatingRatesReadPlatformService;
    private final LoanUtilService loanUtilService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanChargePaidByReadService loanChargePaidByReadService;
    private final LoanTransactionRelationReadService loanTransactionRelationReadService;
    private final LoanForeclosureValidator loanForeclosureValidator;
    private final LoanTransactionMapper loanTransactionMapper;
    private final LoanTransactionProcessingService loadTransactionProcessingService;
    private final LoanBalanceService loanBalanceService;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;
    private final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;
    private final InterestRefundServiceDelegate interestRefundServiceDelegate;
    private final LoanMaximumAmountCalculator loanMaximumAmountCalculator;
    private final LoanRepaymentScheduleService loanRepaymentScheduleService;

    @Override
    public LoanAccountData retrieveOne(final Long loanId) {

        try {
            final String hierarchy = getHierarchyString();
            final String hierarchySearchString = hierarchy + "%";

            final LoanAccountDataMapper rm = new LoanAccountDataMapper(sqlGenerator, delinquencyReadPlatformService);

            final StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select ");
            sqlBuilder.append(rm.loanSchema());
            sqlBuilder.append(" join m_office o on (o.id = c.office_id or o.id = g.office_id) ");
            sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
            sqlBuilder.append(" where l.id=? and ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), rm, loanId, hierarchySearchString, hierarchySearchString);
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId, e);
        }
    }

    private String getHierarchyString() {
        AppUser currentUser = null;
        if (this.context != null) {
            currentUser = this.context.getAuthenticatedUserIfPresent();
        }
        return Optional.ofNullable(currentUser).map(appUser -> appUser.getOffice().getHierarchy()).orElse(".");
    }

    @Override
    public LoanAccountData retrieveLoanByLoanAccount(String loanAccountNumber) {

        // final AppUser currentUser = this.context.authenticatedUser();
        this.context.authenticatedUser();
        final LoanAccountDataMapper rm = new LoanAccountDataMapper(sqlGenerator, delinquencyReadPlatformService);

        final String sql = "select " + rm.loanSchema() + " where l.account_no=?";

        return this.jdbcTemplate.queryForObject(sql, rm, loanAccountNumber); // NOSONAR

    }

    @Override
    public List<LoanAccountData> retrieveGLIMChildLoansByGLIMParentAccount(String parentloanAccountNumber) {
        this.context.authenticatedUser();
        final LoanAccountDataMapper rm = new LoanAccountDataMapper(sqlGenerator, delinquencyReadPlatformService);

        final String sql = "select " + rm.loanSchema()
                + " left join glim_parent_child_mapping as glim on glim.glim_child_account_id=l.account_no "
                + "where glim.glim_parent_account_id=?";

        return this.jdbcTemplate.query(sql, rm, parentloanAccountNumber); // NOSONAR

    }

    @Override
    public LoanAccountData fetchRepaymentScheduleData(LoanAccountData accountData) {
        final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData = new RepaymentScheduleRelatedLoanData(
                accountData.getTimeline().getExpectedDisbursementDate(), accountData.getTimeline().getActualDisbursementDate(),
                accountData.getCurrency(), accountData.getPrincipal(), accountData.getInArrearsTolerance(),
                accountData.getFeeChargesAtDisbursementCharged(), Boolean.TRUE.equals(accountData.getAllowFullTermForTranche()));

        final Collection<DisbursementData> disbursementData = retrieveLoanDisbursementDetails(accountData.getId());
        List<LoanTransactionRepaymentPeriodData> capitalizedIncomeData = loanCapitalizedIncomeBalanceRepository
                .findRepaymentPeriodDataByLoanId(accountData.getId());
        final LoanScheduleData repaymentSchedule = retrieveRepaymentSchedule(accountData.getId(), repaymentScheduleRelatedData,
                disbursementData, capitalizedIncomeData, accountData.isInterestRecalculationEnabled(),
                LoanScheduleType.fromEnumOptionData(accountData.getLoanScheduleType()));
        accountData.setRepaymentSchedule(repaymentSchedule);
        return accountData;
    }

    @Override
    public LoanScheduleData retrieveRepaymentSchedule(final Long loanId,
            final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData, Collection<DisbursementData> disbursementData,
            Collection<LoanTransactionRepaymentPeriodData> capitalizedIncomeData, boolean isInterestRecalculationEnabled,
            LoanScheduleType loanScheduleType) {
        this.context.authenticatedUser();
        return loanRepaymentScheduleService.findLoanScheduleData(loanId, repaymentScheduleRelatedLoanData, disbursementData,
                capitalizedIncomeData, isInterestRecalculationEnabled, loanScheduleType);
    }

    @Override
    public Collection<LoanTransactionData> retrieveLoanTransactions(final Long loanId) {
        try {
            this.context.authenticatedUser();

            final LoanTransactionsMapper rm = createLoanTransactionsMapper();

            // retrieve all loan transactions that are not invalid and have not
            // been 'contra'ed by another transaction
            // repayments at time of disbursement (e.g. charges)

            /***
             * TODO Vishwas: Remove references to "Contra" from the codebase
             ***/
            final String sql = "select " + rm.loanPaymentsSchema() + " where tr.loan_id = ? and tr.transaction_type_enum not in (0, 3) "
                    + " and (tr.is_reversed=false or tr.manually_adjusted_or_reversed = true)  order by tr.transaction_date, tr.created_on_utc, tr.id ";
            Collection<LoanTransactionData> loanTransactionData = this.jdbcTemplate.query(sql, rm, loanId); // NOSONAR
            // TODO: would worth to rework in the future. It is not nice to fetch relations one by one... might worth to
            // give a try to get rid of native queries
            final List<Long> loanIds = loanTransactionData.stream().map(LoanTransactionData::getId).collect(Collectors.toList());
            final List<LoanTransactionRelationData> loanTransactionRelationDatas = loanTransactionRelationReadService
                    .fetchLoanTransactionRelationDataFrom(loanIds);
            final List<LoanChargePaidByData> loanChargePaidByDatas = loanChargePaidByReadService
                    .fetchLoanChargesPaidByDataTransactionId(loanIds);
            for (LoanTransactionData loanTransaction : loanTransactionData) {
                loanTransaction.setTransactionRelations(loanTransactionRelationDatas.stream().filter(
                        loanTransactionRelationData -> loanTransactionRelationData.getFromLoanTransaction().equals(loanTransaction.getId()))
                        .toList());
                loanTransaction.setLoanChargePaidByList(loanChargePaidByDatas.stream()
                        .filter(loanChargePaidByData -> loanChargePaidByData.getTransactionId().equals(loanTransaction.getId())).toList());
            }
            return loanTransactionData;
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    protected LoanTransactionsMapper createLoanTransactionsMapper() {
        return new LoanTransactionsMapper(sqlGenerator);
    }

    @Override
    public Page<LoanAccountData> retrieveAll(final SearchParameters searchParameters) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";
        final LoanAccountDataMapper loanMapper = new LoanAccountDataMapper(sqlGenerator, delinquencyReadPlatformService);

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(loanMapper.loanSchema());

        // TODO - for time being this will data scope list of loans returned to
        // only loans that have a client associated.
        // to support scenario where loan has group_id only OR client_id will
        // probably require a UNION query
        // but that at present is an edge case
        sqlBuilder.append(" join m_office o on (o.id = c.office_id or o.id = g.office_id) ");
        sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
        sqlBuilder.append(" where ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

        int arrayPos = 2;
        List<Object> extraCriterias = new ArrayList<>();
        extraCriterias.add(hierarchySearchString);
        extraCriterias.add(hierarchySearchString);

        if (searchParameters != null) {

            if (StringUtils.isNotBlank(searchParameters.getStatus())) {
                sqlBuilder.append(" and l.loan_status_id = ?");
                extraCriterias.add(Integer.parseInt(searchParameters.getStatus()));
                arrayPos = arrayPos + 1;
            }

            if (StringUtils.isNotBlank(searchParameters.getExternalId())) {
                sqlBuilder.append(" and l.external_id = ?");
                extraCriterias.add(searchParameters.getExternalId());
                arrayPos = arrayPos + 1;
            }
            if (searchParameters.getOfficeId() != null) {
                sqlBuilder.append("and c.office_id =?");
                extraCriterias.add(searchParameters.getOfficeId());
                arrayPos = arrayPos + 1;
            }

            if (StringUtils.isNotBlank(searchParameters.getAccountNo())) {
                sqlBuilder.append(" and l.account_no = ?");
                extraCriterias.add(searchParameters.getAccountNo());
                arrayPos = arrayPos + 1;
            }

            if (searchParameters.getClientId() != null) {
                sqlBuilder.append(" and l.client_id = ?");
                extraCriterias.add(searchParameters.getClientId());
                arrayPos = arrayPos + 1;
            }

            if (searchParameters.hasOrderBy()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());

                if (searchParameters.hasSortOrder()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.hasLimit()) {
                sqlBuilder.append(" ");
                if (searchParameters.hasOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
        }
        final Object[] objectArray = extraCriterias.toArray();
        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), finalObjectArray, loanMapper);
    }

    @Override
    public LoanAccountData retrieveTemplateWithClientAndProductDetails(final Long clientId, final Long productId) {

        this.context.authenticatedUser();

        final ClientData clientAccount = this.clientReadPlatformService.retrieveOne(clientId);
        final LocalDate expectedDisbursementDate = DateUtils.getBusinessLocalDate();
        LoanAccountData loanDetails = new LoanAccountData().withClientData(clientAccount)
                .setExpectedDisbursementDate(expectedDisbursementDate);

        if (productId != null) {
            final LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanDetails = loanDetails.withProductData(selectedProduct, null);
        }

        return loanDetails;
    }

    @Override
    public LoanAccountData retrieveTemplateWithGroupAndProductDetails(final Long groupId, final Long productId) {

        this.context.authenticatedUser();

        final GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        final LocalDate expectedDisbursementDate = DateUtils.getBusinessLocalDate();
        LoanAccountData loanDetails = new LoanAccountData().setGroup(groupAccount).withExpectedDisbursementDate(expectedDisbursementDate);

        if (productId != null) {
            final LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanDetails = loanDetails.withProductData(selectedProduct, null);
        }

        return loanDetails;
    }

    @Override
    public LoanAccountData retrieveTemplateWithCompleteGroupAndProductDetails(final Long groupId, final Long productId) {

        this.context.authenticatedUser();

        GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        // get group associations
        final Collection<ClientData> membersOfGroup = this.clientReadPlatformService.retrieveClientMembersOfGroup(groupId);
        if (!CollectionUtils.isEmpty(membersOfGroup)) {
            final Collection<ClientData> activeClientMembers = null;
            final Collection<CalendarData> calendarsData = null;
            final CalendarData collectionMeetingCalendar = null;
            final Collection<GroupRoleData> groupRoles = null;
            groupAccount = GroupGeneralData.withAssocations(groupAccount, membersOfGroup, activeClientMembers, groupRoles, calendarsData,
                    collectionMeetingCalendar);
        }

        final LocalDate expectedDisbursementDate = DateUtils.getBusinessLocalDate();
        LoanAccountData loanDetails = new LoanAccountData().setGroup(groupAccount).withExpectedDisbursementDate(expectedDisbursementDate);

        if (productId != null) {
            final LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanDetails = loanDetails.withProductData(selectedProduct, null);
        }

        return loanDetails;
    }

    private CurrencyData retriveLoanCurrencyData(final Long loanId) {
        final LoanCurrencyDataMapper loanCurrencyMapper = new LoanCurrencyDataMapper(sqlGenerator);
        final String sql = "select " + loanCurrencyMapper.schema() + " where l.id = ?";

        return this.jdbcTemplate.queryForObject(sql, loanCurrencyMapper, loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(final Long loanId, final LoanTransactionType transactionType,
            final Long transactionId) {

        LoanTransactionData loanTransactionData = null;
        Collection<PaymentTypeData> paymentOptions = null;
        List<CodeValueData> classificationOptions = null;
        BigDecimal transactionAmount = BigDecimal.ZERO;
        switch (transactionType) {
            case CAPITALIZED_INCOME:
                final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
                BigDecimal capitalizedIncomeBalance = BigDecimal.ZERO;
                if (loan.getLoanProduct().getLoanProductRelatedDetail().isEnableIncomeCapitalization()) {
                    capitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository.findAllByLoanIdAndDeletedFalseAndClosedFalse(loanId)
                            .stream().map(LoanCapitalizedIncomeBalance::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                }
                transactionAmount = loan.getLoanProduct().isAllowApprovedDisbursedAmountsOverApplied()
                        ? loanMaximumAmountCalculator.getOverAppliedMax(loan)
                        : loan.getApprovedPrincipal();
                transactionAmount = transactionAmount.subtract(loan.getDisbursedAmount()).subtract(capitalizedIncomeBalance);
                paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
                classificationOptions = this.codeValueReadPlatformService
                        .retrieveCodeValuesByCode(LoanTransactionApiConstants.CAPITALIZED_INCOME_CLASSIFICATION_CODE);
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            case BUY_DOWN_FEE:
                paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
                classificationOptions = this.codeValueReadPlatformService
                        .retrieveCodeValuesByCode(LoanTransactionApiConstants.BUY_DOWN_FEE_CLASSIFICATION_CODE);
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            case CAPITALIZED_INCOME_ADJUSTMENT:
                final LoanCapitalizedIncomeBalance loanCapitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository
                        .findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(loanId, transactionId);

                transactionAmount = (loanCapitalizedIncomeBalance == null) ? BigDecimal.ZERO
                        : loanCapitalizedIncomeBalance.getAmount()
                                .subtract(MathUtil.nullToZero(loanCapitalizedIncomeBalance.getAmountAdjustment()));
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            case BUY_DOWN_FEE_ADJUSTMENT:
                final LoanBuyDownFeeBalance loanBuyDownFeeBalance = loanBuyDownFeeBalanceRepository
                        .findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(loanId, transactionId);

                transactionAmount = (loanBuyDownFeeBalance == null) ? BigDecimal.ZERO
                        : loanBuyDownFeeBalance.getAmount().subtract(MathUtil.nullToZero(loanBuyDownFeeBalance.getAmountAdjustment()));
                loanTransactionData = LoanTransactionData.loanTransactionDataForCreditTemplate(
                        LoanEnumerations.transactionType(transactionType), DateUtils.getBusinessLocalDate(), transactionAmount,
                        paymentOptions, retriveLoanCurrencyData(loanId), classificationOptions);
            break;
            default:
                loanTransactionData = LoanTransactionData.templateOnTop(retrieveLoanTransactionTemplate(loanId),
                        LoanEnumerations.transactionType(transactionType));
            break;
        }

        return loanTransactionData;
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(final Long loanId) {
        this.context.authenticatedUser();

        final RepaymentTransactionTemplateMapper mapper = new RepaymentTransactionTemplateMapper(sqlGenerator);
        LoanTransactionData loanTransactionData = this.jdbcTemplate.queryForObject("select " + mapper.schema(), mapper, // NOSONAR
                LoanTransactionType.REPAYMENT.getValue(), LoanTransactionType.DOWN_PAYMENT.getValue(), loanId);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        return LoanTransactionData.templateOnTop(loanTransactionData, paymentOptions);
    }

    @Override
    public LoanTransactionData retrieveLoanPrePaymentTemplate(final LoanTransactionType repaymentTransactionType, final Long loanId,
            LocalDate onDate) {
        return retrieveLoanPayOffTemplate(repaymentTransactionType, loanId, onDate, false);
    }

    @Override
    public LoanTransactionData retrieveLoanEarlyTerminationTemplate(final Long loanId, final LocalDate onDate) {
        return retrieveLoanPayOffTemplate(LoanTransactionType.REPAYMENT, loanId, onDate, true);
    }

    private LoanTransactionData retrieveLoanPayOffTemplate(final LoanTransactionType repaymentTransactionType, final Long loanId,
            final LocalDate onDate, final boolean earlyTermination) {

        this.context.authenticatedUser();
        this.loanUtilService.validateRepaymentTransactionType(repaymentTransactionType);

        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate earliestUnpaidInstallmentDate = DateUtils.getBusinessLocalDate();
        final LocalDate recalculateFrom = null;
        final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);
        final OutstandingAmountsDTO outstandingAmounts = earlyTermination
                ? loadTransactionProcessingService.fetchEarlyTerminationDetail(scheduleGeneratorDTO, onDate, loan)
                : loadTransactionProcessingService.fetchPrepaymentDetail(scheduleGeneratorDTO, onDate, loan);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(repaymentTransactionType);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final BigDecimal outstandingLoanBalance = outstandingAmounts.principal().getAmount();
        final BigDecimal unrecognizedIncomePortion = null;

        BigDecimal adjustedChargeAmount = adjustPrepayInstallmentCharge(loan, onDate);
        BigDecimal totalAdjusted = outstandingAmounts.getTotalOutstanding().getAmount().subtract(adjustedChargeAmount);

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(earliestUnpaidInstallmentDate)
                .amount(totalAdjusted).netDisbursalAmount(loan.getNetDisbursalAmount())
                .principalPortion(outstandingAmounts.principal().getAmount()).interestPortion(outstandingAmounts.interest().getAmount())
                .feeChargesPortion(outstandingAmounts.feeCharges().getAmount().subtract(adjustedChargeAmount))
                .penaltyChargesPortion(outstandingAmounts.penaltyCharges().getAmount()).unrecognizedIncomePortion(unrecognizedIncomePortion)
                .paymentTypeOptions(paymentOptions).externalId(ExternalId.empty()).outstandingLoanBalance(outstandingLoanBalance)
                .manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId()).build();
    }

    private BigDecimal adjustPrepayInstallmentCharge(Loan loan, final LocalDate onDate) {
        BigDecimal chargeAmount = BigDecimal.ZERO;
        /*
         * for(LoanCharge loanCharge: loan.charges()){ if(loanCharge.isInstalmentFee() &&
         * loanCharge.getCharge().getChargeCalculation()==ChargeCalculationType. FLAT.getValue()){ for
         * (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
         * if(onDate.isBefore(installment.getDueDate())){ LoanInstallmentCharge loanInstallmentCharge =
         * loanCharge.getInstallmentLoanCharge(installment.getInstallmentNumber( )); if(loanInstallmentCharge != null){
         * chargeAmount = chargeAmount.add(loanInstallmentCharge.getAmountOutstanding()); }
         *
         * break; } } } }
         */
        return chargeAmount;
    }

    @Override
    public LoanTransactionData retrieveWaiveInterestDetails(final Long loanId) {

        // TODO - KW -OPTIMIZE - write simple sql query to fetch back overdue
        // interest that can be waived along with the date of repayment period
        // interest is overdue.
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CurrencyData currencyData = applicationCurrency.toData();

        final LoanTransaction waiveOfInterest = deriveDefaultInterestWaiverTransaction(loan);

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WAIVE_INTEREST);

        final BigDecimal amount = waiveOfInterest.getAmount(currency).getAmount();
        final BigDecimal outstandingLoanBalance = null;
        final BigDecimal unrecognizedIncomePortion = null;

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(waiveOfInterest.getTransactionDate())
                .amount(amount).netDisbursalAmount(loan.getNetDisbursalAmount()).outstandingLoanBalance(outstandingLoanBalance)
                .unrecognizedIncomePortion(unrecognizedIncomePortion).externalId(ExternalId.empty()).manuallyReversed(false).loanId(loanId)
                .externalLoanId(loan.getExternalId()).build();
    }

    @Override
    public LoanTransactionData retrieveNewClosureDetails() {

        this.context.authenticatedUser();
        final BigDecimal outstandingLoanBalance = null;
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WRITEOFF);
        final BigDecimal unrecognizedIncomePortion = null;
        return LoanTransactionData.builder().type(transactionType).date(DateUtils.getBusinessLocalDate()).externalId(ExternalId.empty())
                .outstandingLoanBalance(outstandingLoanBalance).unrecognizedIncomePortion(unrecognizedIncomePortion).manuallyReversed(false)
                .build();

    }

    @Override
    public LoanApprovalData retrieveApprovalTemplate(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final ApplicationCurrency appCurrency = applicationCurrencyRepository.findOneWithNotFoundDetection(loan.getCurrency());
        final BigDecimal availableDisbursementAmountWithOverApplied = delinquencyReadPlatformService
                .calculateAvailableDisbursementAmountWithOverApplied(loan);
        return new LoanApprovalData(loan.getProposedPrincipal(), DateUtils.getBusinessLocalDate(), loan.getNetDisbursalAmount(),
                appCurrency.toData(), availableDisbursementAmountWithOverApplied, loan.getExpectedDisbursementDate());
    }

    @Override
    public LoanTransactionData retrieveDisbursalTemplate(final Long loanId, boolean paymentDetailsRequired) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.DISBURSEMENT);
        Collection<PaymentTypeData> paymentOptions = null;
        if (paymentDetailsRequired) {
            paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        }
        final ApplicationCurrency appCurrency = applicationCurrencyRepository.findOneWithNotFoundDetection(loan.getCurrency());
        final BigDecimal availableDisbursementAmountWithOverApplied = delinquencyReadPlatformService
                .calculateAvailableDisbursementAmountWithOverApplied(loan);
        return LoanTransactionData.loanTransactionDataForDisbursalTemplate(transactionType,
                loan.getExpectedDisbursedOnLocalDateForTemplate(), loan.getDisburseAmountForTemplate(), loan.getNetDisbursalAmount(),
                paymentOptions, loan.retriveLastEmiAmount(), loan.getNextPossibleRepaymentDateForRescheduling(), appCurrency.toData(),
                availableDisbursementAmountWithOverApplied);
    }

    @Override
    public Integer retrieveNumberOfRepayments(final Long loanId) {
        this.context.authenticatedUser();
        return this.loanRepositoryWrapper.getNumberOfRepayments(loanId);
    }

    @Override
    public List<LoanRepaymentScheduleInstallmentData> getRepaymentDataResponse(final Long loanId) {
        this.context.authenticatedUser();
        final List<LoanRepaymentScheduleInstallment> loanRepaymentScheduleInstallments = this.loanRepositoryWrapper
                .getLoanRepaymentScheduleInstallments(loanId);
        List<LoanRepaymentScheduleInstallmentData> loanRepaymentScheduleInstallmentData = new ArrayList<>();

        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loanRepaymentScheduleInstallments) {
            loanRepaymentScheduleInstallmentData.add(LoanRepaymentScheduleInstallmentData.instanceOf(
                    loanRepaymentScheduleInstallment.getId(), loanRepaymentScheduleInstallment.getInstallmentNumber(),
                    loanRepaymentScheduleInstallment.getDueDate(), loanRepaymentScheduleInstallment
                            .getTotalOutstanding(loanRepaymentScheduleInstallment.getLoan().getCurrency()).getAmount()));
        }
        return loanRepaymentScheduleInstallmentData;
    }

    @Override
    public LoanTransactionData retrieveLoanTransaction(final Long loanId, final Long transactionId) {
        this.context.authenticatedUser();
        try {
            final LoanTransactionsMapper rm = createLoanTransactionsMapper();
            final String sql = "select " + rm.loanPaymentsSchema() + " where l.id = ? and tr.id = ? ";
            LoanTransactionData loanTransactionData = this.jdbcTemplate.queryForObject(sql, rm, loanId, transactionId); // NOSONAR
            loanTransactionData.setTransactionRelations(
                    loanTransactionRelationReadService.fetchLoanTransactionRelationDataFrom(loanTransactionData.getId()));
            return loanTransactionData;
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanTransactionNotFoundException(transactionId, e);
        }
    }

    @Override
    public org.springframework.data.domain.Page<LoanTransactionData> retrieveLoanTransactions(
            @jakarta.validation.constraints.NotNull Long loanId, Set<LoanTransactionType> excludedTransactionTypes, Pageable pageable) {
        final org.springframework.data.domain.Page<LoanTransaction> transactionPage = loanTransactionRepository
                .findAll((root, query, builder) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    final Join<Object, Object> loanjoin = root.join("loan");
                    predicates.add(builder.equal(loanjoin.get("id"), loanId));

                    if (excludedTransactionTypes != null && !excludedTransactionTypes.isEmpty()) {
                        final List<LoanTransactionType> excludedTransactionTypeValues = excludedTransactionTypes.stream().toList();
                        predicates.add(builder.not(root.get("typeOf").in(excludedTransactionTypeValues)));
                    }
                    return builder.and(predicates.toArray(new Predicate[] {}));
                }, pageable);

        return transactionPage.map(loanTransactionMapper::mapLoanTransaction);
    }

    @Override
    public Long getLoanIdByLoanExternalId(String externalId) {
        ExternalId loanExternalId = ExternalIdFactory.produce(externalId);
        Long loanId = loanRepositoryWrapper.findIdByExternalId(loanExternalId);
        if (Objects.isNull(loanId)) {
            throw new LoanNotFoundException(loanExternalId);
        }
        return loanId;
    }

    @Override
    public LoanAccountData retrieveLoanProductDetailsTemplate(final Long productId, final Long clientId, final Long groupId,
            final Long officeId) {

        this.context.authenticatedUser();

        final LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
        final Collection<EnumOptionData> loanTermFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanTermFrequencyTypeOptions();
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyTypeOptions();
        final Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
        final Collection<EnumOptionData> repaymentFrequencyDaysOfWeekTypeOptions = this.loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyOptionsForDaysOfWeek();
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = this.loanDropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();
        final Collection<EnumOptionData> amortizationTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanAmortizationTypeOptions();
        Collection<EnumOptionData> interestTypeOptions = null;
        if (loanProduct.isLinkedToFloatingInterestRates()) {
            interestTypeOptions = List.of(interestType(InterestMethod.DECLINING_BALANCE));
        } else {
            interestTypeOptions = this.loanDropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        }
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = this.loanDropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();
        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = this.loanDropdownReadPlatformService
                .retrieveTransactionProcessingStrategies();
        final Collection<CodeValueData> loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
        final Collection<CodeValueData> loanCollateralOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode("LoanCollateral");
        Collection<ChargeData> chargeOptions = null;
        if (loanProduct.getMultiDisburseLoan()) {
            chargeOptions = this.chargeReadPlatformService.retrieveLoanProductApplicableCharges(productId,
                    new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT }, officeId);
        } else {
            chargeOptions = this.chargeReadPlatformService.retrieveLoanProductApplicableCharges(productId,
                    new ChargeTimeType[] { ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT }, officeId);
        }

        Integer loanCycleCounter = null;
        if (loanProduct.isUseBorrowerCycle()) {
            if (clientId == null) {
                loanCycleCounter = retriveLoanCounter(groupId, AccountType.GROUP.getValue(), loanProduct.getId());
            } else {
                loanCycleCounter = retriveLoanCounter(clientId, loanProduct.getId());
            }
        }

        Collection<LoanAccountSummaryData> activeLoanOptions = null;
        if (loanProduct.isCanUseForTopup() && clientId != null) {
            activeLoanOptions = this.accountDetailsReadPlatformService.retrieveClientActiveLoanAccountSummary(clientId);
        } else if (loanProduct.isCanUseForTopup() && groupId != null) {
            activeLoanOptions = this.accountDetailsReadPlatformService.retrieveGroupActiveLoanAccountSummary(groupId);
        }

        LoanAccountData loanAccountData = new LoanAccountData().withProductData(loanProduct, loanCycleCounter) //
                .setTermFrequencyTypeOptions(loanTermFrequencyTypeOptions) //
                .setRepaymentFrequencyTypeOptions(repaymentFrequencyTypeOptions) //
                .setRepaymentFrequencyNthDayTypeOptions(repaymentFrequencyNthDayTypeOptions) //
                .setRepaymentFrequencyDaysOfWeekTypeOptions(repaymentFrequencyDaysOfWeekTypeOptions) //
                .setTransactionProcessingStrategyOptions(repaymentStrategyOptions) //
                .setInterestRateFrequencyTypeOptions(interestRateFrequencyTypeOptions) //
                .setAmortizationTypeOptions(amortizationTypeOptions) //
                .setInterestTypeOptions(interestTypeOptions) //
                .setInterestCalculationPeriodTypeOptions(interestCalculationPeriodTypeOptions) //
                .setFundOptions(fundOptions) //
                .setChargeOptions(chargeOptions) //
                .setLoanPurposeOptions(loanPurposeOptions) //
                .setLoanCollateralOptions(loanCollateralOptions) //
                .setClientActiveLoanOptions(activeLoanOptions) //
                .setLoanScheduleTypeOptions(LoanScheduleType.getValuesAsEnumOptionDataList()) //
                .setLoanScheduleProcessingTypeOptions(LoanScheduleProcessingType.getValuesAsEnumOptionDataList()); //

        if (officeId != null) {
            final Set<Long> visibleChargeIds = this.chargeReadPlatformService.retrieveLoanProductCharges(productId, officeId).stream()
                    .map(ChargeData::getId).collect(Collectors.toSet());
            if (loanAccountData.getCharges() != null) {
                loanAccountData.setCharges(
                        loanAccountData.getCharges().stream().filter(charge -> visibleChargeIds.contains(charge.getChargeId())).toList());
            }
            if (loanAccountData.getOverdueCharges() != null) {
                loanAccountData.setOverdueCharges(
                        loanAccountData.getOverdueCharges().stream().filter(charge -> visibleChargeIds.contains(charge.getId())).toList());
            }
        }

        return loanAccountData;
    }

    @Override
    public Collection<CalendarData> retrieveCalendars(final Long groupId) {
        Collection<CalendarData> calendarsData = new ArrayList<>();
        calendarsData.addAll(
                this.calendarReadPlatformService.retrieveParentCalendarsByEntity(groupId, CalendarEntityType.GROUPS.getValue(), null));
        calendarsData
                .addAll(this.calendarReadPlatformService.retrieveCalendarsByEntity(groupId, CalendarEntityType.GROUPS.getValue(), null));
        calendarsData = this.calendarReadPlatformService.updateWithRecurringDates(calendarsData);
        return calendarsData;
    }

    @Override
    public Collection<StaffData> retrieveAllowedLoanOfficers(final Long selectedOfficeId, final boolean staffInSelectedOfficeOnly) {
        if (selectedOfficeId == null) {
            return null;
        }

        Collection<StaffData> allowedLoanOfficers = null;

        if (staffInSelectedOfficeOnly) {
            // only bring back loan officers in selected branch/office
            allowedLoanOfficers = this.staffReadPlatformService.retrieveAllLoanOfficersInOfficeById(selectedOfficeId);
        } else {
            // by default bring back all loan officers in selected
            // branch/office as well as loan officers in officer above
            // this office
            final boolean restrictToLoanOfficersOnly = true;
            allowedLoanOfficers = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(selectedOfficeId,
                    restrictToLoanOfficersOnly);
        }

        return allowedLoanOfficers;
    }

    @Override
    public Collection<OverdueLoanScheduleData> retrieveAllLoansWithOverdueInstallments(final Long penaltyWaitPeriod,
            final Boolean backdatePenalties) {
        final MusoniOverdueLoanScheduleMapper rm = new MusoniOverdueLoanScheduleMapper();

        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ").append(rm.schema())
                .append(" where " + sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "?", "day") + " > ls.duedate ")
                .append(" and ls.completed_derived <> true and mc.charge_applies_to_enum =1 ")
                .append(" and ls.recalculated_interest_component <> true ")
                .append(" and mc.charge_time_enum = 9 and ml.loan_status_id = 300 ");

        if (backdatePenalties) {
            return this.jdbcTemplate.query(sqlBuilder.toString(), rm, penaltyWaitPeriod);
        }
        // Only apply for duedate = yesterday (so that we don't apply
        // penalties on the duedate itself)
        sqlBuilder.append(" and ls.duedate >= " + sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "(? + 1)", "day"));

        return this.jdbcTemplate.query(sqlBuilder.toString(), rm, penaltyWaitPeriod, penaltyWaitPeriod);
    }

    @Override
    public Collection<OverdueLoanScheduleData> retrieveAllOverdueInstallmentsForLoan(final Loan loan) {
        Collection<OverdueLoanScheduleData> list = new ArrayList<>();

        if (!loan.isOpen()) {
            return list;
        }

        Optional<Charge> optPenaltyCharge = loan.getLoanProduct().getCharges().stream()
                .filter((e) -> ChargeTimeType.OVERDUE_INSTALLMENT.getValue().equals(e.getChargeTimeType()) && e.isLoanCharge()).findFirst();

        if (optPenaltyCharge.isEmpty()) {
            return list;
        }
        final Charge penaltyCharge = optPenaltyCharge.get();

        final Long penaltyWaitPeriod = configurationDomainService.retrievePenaltyWaitPeriod();
        final boolean backdatePenalties = configurationDomainService.isBackdatePenaltiesEnabled();

        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (installment.isObligationsMet() || installment.isRecalculatedInterestComponent()) {
                continue;
            }

            boolean isPenaltyDue = installment.isOverdueOn(DateUtils.getBusinessLocalDate().minusDays(penaltyWaitPeriod).plusDays(1));
            boolean isDueToday = installment.getDueDate().equals(DateUtils.getBusinessLocalDate().minusDays(penaltyWaitPeriod));

            if (isPenaltyDue) {
                if (!backdatePenalties && !isDueToday) {
                    continue;
                }

                list.add(new OverdueLoanScheduleData(loan.getId(), penaltyCharge.getId(),
                        DateUtils.DEFAULT_DATE_FORMATTER.format(installment.getDueDate()), penaltyCharge.getAmount(),
                        DateUtils.DEFAULT_DATE_FORMAT, Locale.ENGLISH.toLanguageTag(),
                        installment.getPrincipalOutstanding(loan.getCurrency()).getAmount(),
                        installment.getInterestOutstanding(loan.getCurrency()).getAmount(), installment.getInstallmentNumber()));
            }
        }
        return list;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Integer retriveLoanCounter(final Long groupId, final Integer loanType, Long productId) {
        final String sql = "Select COALESCE(MAX(l.loan_product_counter), 0) from m_loan l where l.group_id = ?  and l.loan_type_enum = ? and l.product_id=?";
        return this.jdbcTemplate.queryForObject(sql, new Object[] { groupId, loanType, productId }, Integer.class);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Integer retriveLoanCounter(final Long clientId, Long productId) {
        final String sql = "Select COALESCE(MAX(l.loan_product_counter), 0) from m_loan l where l.client_id = ? and l.product_id=?";
        return this.jdbcTemplate.queryForObject(sql, new Object[] { clientId, productId }, Integer.class);
    }

    @Override
    public Collection<DisbursementData> retrieveLoanDisbursementDetails(final Long loanId) {
        return retrieveLoanDisbursementDetails(List.of(loanId)).getOrDefault(loanId, Collections.emptyList());
    }

    @Override
    public Map<Long, List<DisbursementData>> retrieveLoanDisbursementDetails(final List<Long> loanIds) {
        Object[] parameters = sqlGenerator.inParametersFor(loanIds);
        final LoanDisbursementDetailMapper rm = new LoanDisbursementDetailMapper(sqlGenerator);
        final String sql = "select " + rm.schema() + " where " + sqlGenerator.in("dd.loan_id", loanIds)
                + " and dd.is_reversed=false group by dd.id, lc.amount_waived_derived order by dd.expected_disburse_date,dd.disbursedon_date,dd.id";
        return this.jdbcTemplate.query(sql, rm, parameters).stream().collect(Collectors.groupingBy(DisbursementData::getLoanId)); // NOSONAR
    }

    @Override
    public DisbursementData retrieveLoanDisbursementDetail(Long loanId, Long disbursementId) {
        final LoanDisbursementDetailMapper rm = new LoanDisbursementDetailMapper(sqlGenerator);
        final String sql = "select " + rm.schema() + " where dd.loan_id=? and dd.id=? group by dd.id, lc.amount_waived_derived";
        return this.jdbcTemplate.queryForObject(sql, rm, loanId, disbursementId); // NOSONAR
    }

    @Override
    public LoanTransactionData retrieveRecoveryPaymentTemplate(Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.RECOVERY_REPAYMENT);
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        BigDecimal outstandingLoanBalance = null;
        final BigDecimal unrecognizedIncomePortion = null;
        return LoanTransactionData.builder().type(transactionType).amount(loan.getTotalWrittenOff())
                .netDisbursalAmount(loan.getNetDisbursalAmount()).unrecognizedIncomePortion(unrecognizedIncomePortion)
                .paymentTypeOptions(paymentOptions).externalId(ExternalId.empty()).outstandingLoanBalance(outstandingLoanBalance)
                .manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId()).build();

    }

    @Override
    public LoanTransactionData retrieveLoanWriteoffTemplate(final Long loanId) {

        final LoanAccountData loan = this.retrieveOne(loanId);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WRITEOFF);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final List<CodeValueData> writeOffReasonOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.WRITEOFFREASONS));
        LoanTransactionData loanTransactionData = LoanTransactionData.builder().type(transactionType).currency(loan.getCurrency())
                .date(DateUtils.getBusinessLocalDate()).amount(totalOutstanding).netDisbursalAmount(loan.getNetDisbursalAmount())
                .externalId(ExternalId.empty()).manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId())
                .writeOffReasonOptions(writeOffReasonOptions).build();
        return loanTransactionData;
    }

    @Override
    public LoanTransactionData retrieveLoanReAgeTemplate(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REAGE);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final List<CodeValueData> reAgeReasonOptions = new ArrayList<>(
                codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.REAGE_REASONS));

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();

        int futureInstallmentCount = 0;
        int pastInstallmentCount = 0;
        LocalDate nextInstallmentDueDate = null;

        for (LoanRepaymentScheduleInstallment installment : installments) {
            LocalDate dueDate = installment.getDueDate();
            if (DateUtils.isAfter(dueDate, businessDate)) {
                futureInstallmentCount++;
                if (nextInstallmentDueDate == null || DateUtils.isBefore(dueDate, nextInstallmentDueDate)) {
                    nextInstallmentDueDate = dueDate;
                }
            } else {
                pastInstallmentCount++;
            }
        }

        final PeriodFrequencyType frequencyType = loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        final LocalDate calculatedStartDate = calculateReAgeStartDate(businessDate, frequencyType,
                loan.getLoanRepaymentScheduleDetail().getFirstRepaymentDayOfMonth(),
                loan.getLoanRepaymentScheduleDetail().getSecondRepaymentDayOfMonth());

        final CurrencyData currencyData = new CurrencyData(loan.getCurrencyCode(), null, loan.getCurrency().getDigitsAfterDecimal(),
                loan.getCurrency().getInMultiplesOf(), null, null);

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(businessDate).amount(totalOutstanding)
                .netDisbursalAmount(loan.getNetDisbursalAmount()).loanId(loanId).externalLoanId(loan.getExternalId())
                .periodFrequencyOptions(CommonEnumerations.BASIC_PERIOD_FREQUENCY_TYPES).reAgeReasonOptions(reAgeReasonOptions)
                .reAgeInterestHandlingOptions(LoanReAgeInterestHandlingType.getValuesAsEnumOptionDataList())
                .numberOfFutureInstallments(futureInstallmentCount).numberOfPastInstallments(pastInstallmentCount)
                .nextInstallmentDueDate(nextInstallmentDueDate).calculatedStartDate(calculatedStartDate).build();
    }

    private LocalDate calculateReAgeStartDate(LocalDate businessDate, PeriodFrequencyType frequencyType, Integer firstRepaymentDayOfMonth,
            Integer secondRepaymentDayOfMonth) {
        if (frequencyType == null) {
            return null;
        }
        // calculated start date = current date + 1 unit of original frequency type
        return switch (frequencyType) {
            case DAYS -> businessDate.plusDays(1);
            case WEEKS -> businessDate.plusWeeks(1);
            case MONTHS -> businessDate.plusMonths(1);
            case YEARS -> businessDate.plusYears(1);
            case SEMI_MONTHLY -> SemiMonthlyScheduleDates.next(businessDate,
                    SemiMonthlyScheduleDates.requireDueDays(firstRepaymentDayOfMonth, secondRepaymentDayOfMonth));
            case WHOLE_TERM, INVALID -> null;
        };
    }

    @Override
    public LoanTransactionData retrieveLoanReAmortizationTemplate(final Long loanId) {
        final LoanAccountData loan = this.retrieveOne(loanId);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REAMORTIZE);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final List<CodeValueData> reAmortizationReasonOptions = new ArrayList<>(
                codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.REAMORTIZATION_REASONS));
        return LoanTransactionData.builder().type(transactionType).currency(loan.getCurrency()).date(DateUtils.getBusinessLocalDate())
                .amount(totalOutstanding).netDisbursalAmount(loan.getNetDisbursalAmount()).loanId(loanId)
                .externalLoanId(loan.getExternalId()).reAmortizationReasonOptions(reAmortizationReasonOptions)
                .reAmortizationInterestHandlingOptions(LoanReAmortizationInterestHandlingType.getValuesAsEnumOptionDataList()).build();
    }

    @Override
    public Integer countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(Long loanId) {
        return loanRepaymentScheduleService.countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanChargeOffTemplate(final Long loanId) {

        final LoanAccountData loan = this.retrieveOne(loanId);
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.CHARGE_OFF);
        final BigDecimal totalOutstanding = loan.getSummary() != null ? loan.getSummary().getTotalOutstanding() : null;
        final BigDecimal totalPrincipalOutstanding = loan.getSummary() != null ? loan.getSummary().getPrincipalOutstanding() : null;
        final BigDecimal totalInterestOutstanding = loan.getSummary() != null ? loan.getSummary().getInterestOutstanding() : null;
        final BigDecimal totalFeeOutstanding = loan.getSummary() != null ? loan.getSummary().getFeeChargesOutstanding() : null;
        final BigDecimal totalPenaltyOutstanding = loan.getSummary() != null ? loan.getSummary().getPenaltyChargesOutstanding() : null;
        final List<CodeValueData> chargeOffReasonOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanApiConstants.CHARGE_OFF_REASONS));
        LoanTransactionData loanTransactionData = LoanTransactionData.builder().type(transactionType).currency(loan.getCurrency())
                .date(DateUtils.getBusinessLocalDate()).amount(totalOutstanding).netDisbursalAmount(loan.getNetDisbursalAmount())
                .principalPortion(totalPrincipalOutstanding).interestPortion(totalInterestOutstanding)
                .feeChargesPortion(totalFeeOutstanding).penaltyChargesPortion(totalPenaltyOutstanding).externalId(ExternalId.empty())
                .manuallyReversed(false).loanId(loanId).externalLoanId(loan.getExternalId()).chargeOffReasonOptions(chargeOffReasonOptions)
                .build();
        return loanTransactionData;
    }

    @Override
    public Collection<Long> fetchLoansForInterestRecalculation() {
        final String sql = """
                SELECT l.id
                FROM m_loan l
                INNER JOIN m_loan_repayment_schedule mr ON mr.loan_id = l.id
                LEFT JOIN m_loan_disbursement_detail dd ON dd.loan_id=l.id AND dd.disbursedon_date IS NULL
                -- for past due interest recalculation
                LEFT JOIN m_loan_recalculation_details rcd ON rcd.loan_id = l.id
                -- For Floating rate changes
                LEFT JOIN m_product_loan_floating_rates pfr
                    ON l.product_id = pfr.loan_product_id AND l.is_floating_interest_rate = TRUE
                LEFT JOIN m_floating_rates fr ON pfr.floating_rates_id = fr.id
                LEFT JOIN m_floating_rates_periods frp ON fr.id = frp.floating_rates_id
                LEFT JOIN m_loan_reschedule_request lrr ON lrr.loan_id = l.id
                -- this is to identify the applicable rates when base rate is changed
                LEFT JOIN m_floating_rates bfr ON bfr.is_base_lending_rate = TRUE
                LEFT JOIN m_floating_rates_periods bfrp ON bfr.id = bfrp.floating_rates_id AND bfrp.created_on_utc >= ?
                WHERE l.loan_status_id = ?
                  AND l.is_npa = FALSE
                  AND l.is_charged_off = FALSE
                  AND dd.is_reversed = FALSE
                  AND (
                        (l.interest_recalculation_enabled = TRUE
                            AND (l.interest_recalcualated_on IS NULL OR l.interest_recalcualated_on <> ?)
                            AND ((mr.completed_derived IS FALSE AND mr.duedate < ?) OR dd.expected_disburse_date < ?)
                            AND rcd.disallow_interest_calc_on_past_due = FALSE)
                       OR
                        -- float rate changes
                        (fr.is_active = TRUE
                            AND frp.is_active = TRUE
                            AND (frp.created_on_utc >= ?
                                 OR (bfrp.id IS NOT NULL
                                     AND frp.is_differential_to_base_lending_rate = TRUE
                                     AND frp.from_date >= bfrp.from_date))
                            AND lrr.loan_id IS NULL)
                  )
                GROUP BY l.id
                """;
        try {
            LocalDate currentdate = DateUtils.getBusinessLocalDate();
            // will look only for yesterday modified rates; compared against created_on_utc (a UTC timestamp), so
            // bind the tenant's actual start-of-day boundary rather than a bare date, which the DB would otherwise
            // interpret in its own session time zone instead of the tenant's.
            OffsetDateTime yesterday = DateUtils.getOffsetDateTimeOfTenantFromLocalDate(DateUtils.getBusinessLocalDate().minusDays(1));
            return this.jdbcTemplate.queryForList(sql, Long.class, yesterday, LoanStatus.ACTIVE.getValue(), currentdate, currentdate,
                    currentdate, yesterday);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Long> fetchLoansForInterestRecalculation(Integer pageSize, Long maxLoanIdInList, String officeHierarchy) {
        LocalDate currentdate = DateUtils.getBusinessLocalDate();
        // will look only for yesterday modified rates; compared against created_on_utc (a UTC timestamp), so bind
        // the tenant's actual start-of-day boundary rather than a bare date, which the DB would otherwise interpret
        // in its own session time zone instead of the tenant's.
        OffsetDateTime yesterday = DateUtils.getOffsetDateTimeOfTenantFromLocalDate(DateUtils.getBusinessLocalDate().minusDays(1));
        final String sql = """
                SELECT l.id
                FROM m_loan l
                LEFT JOIN m_client c ON c.id = l.client_id
                LEFT JOIN m_office o ON c.office_id = o.id
                INNER JOIN m_loan_repayment_schedule rps ON rps.loan_id = l.id
                LEFT JOIN m_loan_disbursement_detail dd
                    ON dd.loan_id=l.id AND dd.disbursedon_date IS NULL AND dd.is_reversed = FALSE
                -- for past due interest recalculation
                LEFT JOIN m_loan_recalculation_details rcd ON rcd.loan_id = l.id
                -- For Floating rate changes
                LEFT JOIN m_product_loan_floating_rates pfr
                    ON l.product_id = pfr.loan_product_id AND l.is_floating_interest_rate = TRUE
                LEFT JOIN m_floating_rates fr ON pfr.floating_rates_id = fr.id
                LEFT JOIN m_floating_rates_periods frp ON fr.id = frp.floating_rates_id
                LEFT JOIN m_loan_reschedule_request lrr ON lrr.loan_id = l.id
                -- this is to identify the applicable rates when base rate is changed
                LEFT JOIN m_floating_rates bfr ON bfr.is_base_lending_rate = TRUE
                LEFT JOIN m_floating_rates_periods bfrp ON bfr.id = bfrp.floating_rates_id AND bfrp.created_on_utc >= ?
                WHERE l.loan_status_id = ?
                    AND l.is_npa = FALSE
                    AND l.is_charged_off = FALSE
                    AND (
                         (l.interest_recalculation_enabled = TRUE
                             AND (l.interest_recalcualated_on IS NULL OR l.interest_recalcualated_on <> ?)
                             AND ((rps.completed_derived IS FALSE AND rps.duedate < ?) OR dd.expected_disburse_date < ?)
                             AND rcd.disallow_interest_calc_on_past_due = FALSE)
                        OR
                         (fr.is_active = TRUE
                             AND frp.is_active = TRUE
                             AND (frp.created_on_utc >= ?
                                  OR (bfrp.id IS NOT NULL
                                      AND frp.is_differential_to_base_lending_rate = TRUE
                                      AND frp.from_date >= bfrp.from_date))
                             AND lrr.loan_id IS NULL)
                    )
                    AND l.id > ?
                    AND o.hierarchy like ?
                GROUP BY l.id
                ORDER BY l.id
                LIMIT ?
                """;
        try {
            return Collections.synchronizedList(this.jdbcTemplate.queryForList(sql, Long.class, yesterday, LoanStatus.ACTIVE.getValue(),
                    currentdate, currentdate, currentdate, yesterday, maxLoanIdInList, officeHierarchy, pageSize));
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean isGuaranteeRequired(final Long loanId) {
        final String sql = "select pl.hold_guarantee_funds from m_loan ml inner join m_product_loan pl on pl.id = ml.product_id where ml.id=?";
        return TRUE.equals(this.jdbcTemplate.queryForObject(sql, Boolean.class, loanId));
    }

    @Override
    public LocalDate retrieveMinimumDateOfRepaymentTransaction(Long loanId) {
        return this.jdbcTemplate.queryForObject(
                "select min(transaction_date) from m_loan_transaction where loan_id=? and transaction_type_enum=2", LocalDate.class,
                loanId);
    }

    @Override
    public PaidInAdvanceData retrieveTotalPaidInAdvance(Long loanId) {
        try {
            final String sql = "  select (SUM(COALESCE(mr.principal_completed_derived, 0))"
                    + " + SUM(COALESCE(mr.interest_completed_derived, 0)) " + " + SUM(COALESCE(mr.fee_charges_completed_derived, 0)) "
                    + " + SUM(COALESCE(mr.penalty_charges_completed_derived, 0))) as total_in_advance_derived "
                    + " from m_loan ml INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id "
                    + " where ml.id=? and  mr.duedate >= " + sqlGenerator.currentBusinessDate() + " group by ml.id having "
                    + " (SUM(COALESCE(mr.principal_completed_derived, 0))  " + " + SUM(COALESCE(mr.interest_completed_derived, 0)) "
                    + " + SUM(COALESCE(mr.fee_charges_completed_derived, 0)) "
                    + "+  SUM(COALESCE(mr.penalty_charges_completed_derived, 0))) > 0";
            BigDecimal bigDecimal = this.jdbcTemplate.queryForObject(sql, BigDecimal.class, loanId); // NOSONAR
            return new PaidInAdvanceData(bigDecimal);
        } catch (DataAccessException e) {
            return new PaidInAdvanceData(new BigDecimal(0));
        }
    }

    @Override
    public LoanTransactionData retrieveRefundByCashTemplate(Long loanId) {
        this.context.authenticatedUser();

        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return retrieveRefundTemplate(loanId, LoanTransactionType.REFUND_FOR_ACTIVE_LOAN, paymentOptions, loan.getCurrency(),
                retrieveTotalPaidInAdvance(loan.getId()).getPaidInAdvance(), loan.getNetDisbursalAmount(), loan.getExternalId());
    }

    @Override
    public LoanTransactionData retrieveCreditBalanceRefundTemplate(Long loanId) {
        this.context.authenticatedUser();

        final Collection<PaymentTypeData> paymentOptions = null;
        final BigDecimal netDisbursal = null;
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return retrieveRefundTemplate(loanId, LoanTransactionType.CREDIT_BALANCE_REFUND, paymentOptions, loan.getCurrency(),
                loan.getTotalOverpaid(), netDisbursal, loan.getExternalId());

    }

    private LoanTransactionData retrieveRefundTemplate(Long loanId, LoanTransactionType loanTransactionType,
            Collection<PaymentTypeData> paymentOptions, MonetaryCurrency currency, BigDecimal transactionAmount, BigDecimal netDisbursal,
            ExternalId externalLoanId) {

        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate currentDate = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(loanTransactionType);
        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(currentDate).amount(transactionAmount)
                .netDisbursalAmount(netDisbursal).paymentTypeOptions(paymentOptions).externalId(ExternalId.empty()).manuallyReversed(false)
                .loanId(loanId).externalLoanId(externalLoanId).build();
    }

    @Override
    public Collection<InterestRatePeriodData> retrieveLoanInterestRatePeriodData(LoanAccountData loanData) {
        this.context.authenticatedUser();

        if (loanData.isLoanProductLinkedToFloatingRate()) {
            final Collection<InterestRatePeriodData> intRatePeriodData = new ArrayList<>();
            final Collection<InterestRatePeriodData> intRates = this.floatingRatesReadPlatformService
                    .retrieveInterestRatePeriods(loanData.getLoanProductId());
            for (final InterestRatePeriodData rate : intRates) {
                if (loanData.getTimeline() == null) {
                    continue;
                }
                boolean isAfterDisbursement = DateUtils.isAfter(rate.getFromDate(), loanData.getTimeline().getDisbursementDate());
                if (isAfterDisbursement && loanData.isFloatingInterestRate()) {
                    updateInterestRatePeriodData(rate, loanData);
                    intRatePeriodData.add(rate);
                } else if (!isAfterDisbursement) {
                    updateInterestRatePeriodData(rate, loanData);
                    intRatePeriodData.add(rate);
                    break;
                }
            }

            return intRatePeriodData;
        }
        return null;
    }

    private void updateInterestRatePeriodData(InterestRatePeriodData rate, LoanAccountData loan) {
        LoanProductData loanProductData = loanProductReadPlatformService.retrieveLoanProductFloatingDetails(loan.getLoanProductId());
        rate.setLoanProductDifferentialInterestRate(loanProductData.getInterestRateDifferential());
        rate.setLoanDifferentialInterestRate(loan.getInterestRateDifferential());

        BigDecimal effectiveInterestRate = BigDecimal.ZERO;
        effectiveInterestRate = effectiveInterestRate.add(rate.getLoanDifferentialInterestRate());
        effectiveInterestRate = effectiveInterestRate.add(rate.getLoanProductDifferentialInterestRate());
        effectiveInterestRate = effectiveInterestRate.add(rate.getInterestRate());
        if (rate.getBlrInterestRate() != null && rate.isDifferentialToBLR()) {
            effectiveInterestRate = effectiveInterestRate.add(rate.getBlrInterestRate());
        }
        rate.setEffectiveInterestRate(effectiveInterestRate);

        if (loan.getTimeline() != null && DateUtils.isBefore(rate.getFromDate(), loan.getTimeline().getDisbursementDate())) {
            rate.setFromDate(loan.getTimeline().getDisbursementDate());
        }
    }

    @Override
    public Collection<Long> retrieveLoanIdsWithPendingIncomePostingTransactions() {
        LocalDate currentdate = DateUtils.getBusinessLocalDate();
        final String sql = """
                select distinct loan.id from m_loan as loan
                inner join m_loan_recalculation_details as recdet on (recdet.loan_id = loan.id and recdet.is_compounding_to_be_posted_as_transaction is not null and recdet.is_compounding_to_be_posted_as_transaction = true)
                inner join m_loan_repayment_schedule as repsch on repsch.loan_id = loan.id
                inner join m_loan_interest_recalculation_additional_details as adddet on adddet.loan_repayment_schedule_id = repsch.id
                left join m_loan_transaction as trans on (trans.is_reversed <> true and trans.transaction_type_enum = 19 and trans.loan_id = loan.id and trans.transaction_date = adddet.effective_date)
                where loan.loan_status_id = 300  and loan.is_npa = false and loan.is_charged_off = false
                and adddet.effective_date is not null  and trans.transaction_date is null
                and adddet.effective_date < ?\s""";
        try {
            return this.jdbcTemplate.queryForList(sql, Long.class, new Object[] { currentdate });
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public LoanTransactionData retrieveLoanForeclosureTemplate(final Long loanId, final LocalDate transactionDate) {
        this.context.authenticatedUser();

        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        loanForeclosureValidator.validateForForeclosure(loan, transactionDate);
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate earliestUnpaidInstallmentDate = DateUtils.getBusinessLocalDate();

        final LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = loanBalanceService.fetchLoanForeclosureDetail(loan,
                transactionDate);
        BigDecimal unrecognizedIncomePortion = null;
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final BigDecimal outstandingLoanBalance = loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount();
        final Boolean isManuallyReversed = false;

        final Money outStandingAmount = loanRepaymentScheduleInstallment.getTotalOutstanding(currency);

        return LoanTransactionData.builder().type(transactionType).currency(currencyData).date(earliestUnpaidInstallmentDate)
                .amount(outStandingAmount.getAmount()).netDisbursalAmount(loan.getNetDisbursalAmount())
                .principalPortion(loanRepaymentScheduleInstallment.getPrincipalOutstanding(currency).getAmount())
                .interestPortion(loanRepaymentScheduleInstallment.getInterestOutstanding(currency).getAmount())
                .feeChargesPortion(loanRepaymentScheduleInstallment.getFeeChargesOutstanding(currency).getAmount())
                .penaltyChargesPortion(loanRepaymentScheduleInstallment.getPenaltyChargesOutstanding(currency).getAmount())
                .unrecognizedIncomePortion(unrecognizedIncomePortion).paymentTypeOptions(paymentTypeOptions).externalId(ExternalId.empty())
                .outstandingLoanBalance(outstandingLoanBalance).manuallyReversed(isManuallyReversed).loanId(loanId)
                .externalLoanId(loan.getExternalId()).build();
    }

    @Override
    public Long retrieveLoanIdByAccountNumber(String loanAccountNumber) {
        try {
            return this.jdbcTemplate.queryForObject("select l.id from m_loan l where l.account_no = ?", Long.class, loanAccountNumber);

        } catch (final EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public String retrieveAccountNumberByAccountId(Long accountId) {
        try {
            final String sql = "select loan.account_no from m_loan loan where loan.id = ?";
            return this.jdbcTemplate.queryForObject(sql, String.class, accountId);
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(accountId, e);
        }
    }

    @Override
    public Integer retrieveNumberOfActiveLoans() {
        final String sql = "select count(*) from m_loan";
        return this.jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public Long retrieveLoanTransactionIdByExternalId(ExternalId externalId) {
        return loanTransactionRepository.findIdByExternalId(externalId);
    }

    @Override
    public Long retrieveLoanIdByExternalId(ExternalId externalId) {
        return loanRepositoryWrapper.findIdByExternalId(externalId);
    }

    @Override
    public List<Long> retrieveLoanIdsByExternalIds(List<ExternalId> externalIds) {
        return loanRepositoryWrapper.findIdByExternalIds(externalIds);
    }

    @Override
    public boolean existsByLoanId(Long loanId) {
        return loanRepositoryWrapper.existsByLoanId(loanId);
    }

    @Override
    public LoanTransactionData retrieveManualInterestRefundTemplate(final Long loanId, final Long targetTransactionId) {
        final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final LoanTransaction targetTxn = loan.getLoanTransaction(txn -> txn.getId() != null && txn.getId().equals(targetTransactionId));
        if (targetTxn == null) {
            throw new LoanTransactionNotFoundException(targetTransactionId);
        }
        if (!(targetTxn.isMerchantIssuedRefund() || targetTxn.isPayoutRefund())) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.not.refund.type", "Only for refund transactions");
        }
        if (targetTxn.isReversed()) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.reversed", "Refund transaction is reversed");
        }
        final boolean alreadyExists = loan.getLoanTransactions().stream().anyMatch(txn -> txn.isInterestRefund() && !txn
                .getLoanTransactionRelations(rel -> rel.getToTransaction() != null && rel.getToTransaction().equals(targetTxn)).isEmpty());
        if (alreadyExists) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.transaction.interest.refund.already.exists",
                    "Interest Refund already exists for this refund");
        }

        final InterestRefundService interestRefundService = interestRefundServiceDelegate.lookupInterestRefundService(loan);
        final Money totalInterest = interestRefundService.totalInterestByTransactions(null, loan.getId(), targetTxn.getTransactionDate(),
                List.of(), loan.getLoanTransactions().stream().map(AbstractPersistableCustom::getId).toList(),
                loan.getActiveLoanTermVariations());
        final Money newTotalInterest = interestRefundService.totalInterestByTransactions(null, loan.getId(), targetTxn.getTransactionDate(),
                List.of(targetTxn), loan.getLoanTransactions().stream().map(AbstractPersistableCustom::getId).toList(),
                loan.getActiveLoanTermVariations());
        final BigDecimal interestRefundAmount = totalInterest.minus(newTotalInterest).getAmount();
        final Collection<PaymentTypeData> paymentTypeOptions = paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.INTEREST_REFUND);

        return LoanTransactionData.builder().transactionType(LoanTransactionType.INTEREST_REFUND.name()).type(transactionType)
                .date(targetTxn.getTransactionDate()).amount(interestRefundAmount).paymentTypeOptions(paymentTypeOptions)
                .currency(loan.getCurrency().toData()).build();
    }

    @Override
    public Long getResolvedLoanId(final ExternalId loanExternalId) {
        loanExternalId.throwExceptionIfEmpty();

        final Long resolvedLoanId = retrieveLoanIdByExternalId(loanExternalId);

        if (resolvedLoanId == null) {
            throw new LoanNotFoundException(loanExternalId);
        }

        return resolvedLoanId;
    }

    @Override
    public Long getResolvedLoanTransactionId(final Long transactionId, final ExternalId externalTransactionId) {
        Long resolvedLoanTransactionId = transactionId;
        if (resolvedLoanTransactionId == null) {
            externalTransactionId.throwExceptionIfEmpty();
            resolvedLoanTransactionId = retrieveLoanTransactionIdByExternalId(externalTransactionId);
            if (resolvedLoanTransactionId == null) {
                throw new LoanTransactionNotFoundException(externalTransactionId);
            }
        }
        return resolvedLoanTransactionId;
    }

    private LoanTransaction deriveDefaultInterestWaiverTransaction(final Loan loan) {
        final Money totalInterestOutstanding = loan.getTotalInterestOutstandingOnLoan();
        Money possibleInterestToWaive = totalInterestOutstanding.copy();
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();

        if (totalInterestOutstanding.isGreaterThanZero()) {
            // find earliest known instance of overdue interest and default to
            // that
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            for (final LoanRepaymentScheduleInstallment scheduledRepayment : installments) {

                final Money outstandingForPeriod = scheduledRepayment.getInterestOutstanding(loan.getCurrency());
                if (scheduledRepayment.isOverdueOn(DateUtils.getBusinessLocalDate()) && scheduledRepayment.isNotFullyPaidOff()
                        && outstandingForPeriod.isGreaterThanZero()) {
                    transactionDate = scheduledRepayment.getDueDate();
                    possibleInterestToWaive = outstandingForPeriod;
                    break;
                }
            }
        }

        return LoanTransaction.waiver(loan.getOffice(), loan, possibleInterestToWaive, transactionDate, possibleInterestToWaive,
                possibleInterestToWaive.zero(), ExternalId.empty());
    }

}
