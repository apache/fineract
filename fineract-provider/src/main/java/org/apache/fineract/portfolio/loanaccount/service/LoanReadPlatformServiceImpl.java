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
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.data.GroupRoleData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidByData;
import org.apache.fineract.portfolio.loanaccount.data.LoanRepaymentScheduleInstallmentData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionRelationData;
import org.apache.fineract.portfolio.loanaccount.data.PaidInAdvanceData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanaccount.mapper.LoanTransactionMapper;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
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
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final StaffReadService staffReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final FloatingRatesReadService floatingRatesReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanChargePaidByReadService loanChargePaidByReadService;
    private final LoanTransactionRelationReadService loanTransactionRelationReadService;
    private final LoanTransactionMapper loanTransactionMapper;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;
    private final LoanRepaymentScheduleService loanRepaymentScheduleService;
    private final LoanAccountDataReadService loanAccountDataReadService;
    private final LoanTemplateReadPlatformService loanTemplateReadPlatformService;

    @Override
    public LoanAccountData retrieveOne(Long loanId) {
        return loanAccountDataReadService.retrieveOne(loanId);
    }

    @Override
    public Integer retriveLoanCounter(Long groupId, Integer loanType, Long productId) {
        return loanAccountDataReadService.retriveLoanCounter(groupId, loanType, productId);
    }

    @Override
    public Integer retriveLoanCounter(Long clientId, Long productId) {
        return loanAccountDataReadService.retriveLoanCounter(clientId, productId);
    }

    @Override
    public PaidInAdvanceData retrieveTotalPaidInAdvance(Long loanId) {
        return loanAccountDataReadService.retrieveTotalPaidInAdvance(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveLoanTransactionTemplate(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(Long loanId, LoanTransactionType transactionType, Long transactionId) {
        return loanTemplateReadPlatformService.retrieveLoanTransactionTemplate(loanId, transactionType, transactionId);
    }

    @Override
    public LoanTransactionData retrieveWaiveInterestDetails(Long loanId) {
        return loanTemplateReadPlatformService.retrieveWaiveInterestDetails(loanId);
    }

    @Override
    public LoanTransactionData retrieveNewClosureDetails() {
        return loanTemplateReadPlatformService.retrieveNewClosureDetails();
    }

    @Override
    public LoanTransactionData retrieveDisbursalTemplate(Long loanId, boolean paymentDetailsRequired) {
        return loanTemplateReadPlatformService.retrieveDisbursalTemplate(loanId, paymentDetailsRequired);
    }

    @Override
    public LoanApprovalData retrieveApprovalTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveApprovalTemplate(loanId);
    }

    @Override
    public LoanAccountData retrieveLoanProductDetailsTemplate(Long productId, Long clientId, Long groupId, Long officeId) {
        return loanTemplateReadPlatformService.retrieveLoanProductDetailsTemplate(productId, clientId, groupId, officeId);
    }

    @Override
    public LoanTransactionData retrieveRecoveryPaymentTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveRecoveryPaymentTemplate(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanWriteoffTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveLoanWriteoffTemplate(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanChargeOffTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveLoanChargeOffTemplate(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanPrePaymentTemplate(LoanTransactionType repaymentTransactionType, Long loanId, LocalDate onDate) {
        return loanTemplateReadPlatformService.retrieveLoanPrePaymentTemplate(repaymentTransactionType, loanId, onDate);
    }

    @Override
    public LoanTransactionData retrieveLoanEarlyTerminationTemplate(Long loanId, LocalDate onDate) {
        return loanTemplateReadPlatformService.retrieveLoanEarlyTerminationTemplate(loanId, onDate);
    }

    @Override
    public LoanTransactionData retrieveRefundByCashTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveRefundByCashTemplate(loanId);
    }

    @Override
    public LoanTransactionData retrieveCreditBalanceRefundTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveCreditBalanceRefundTemplate(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanForeclosureTemplate(Long loanId, LocalDate transactionDate) {
        return loanTemplateReadPlatformService.retrieveLoanForeclosureTemplate(loanId, transactionDate);
    }

    @Override
    public LoanTransactionData retrieveManualInterestRefundTemplate(Long loanId, Long targetTransactionId) {
        return loanTemplateReadPlatformService.retrieveManualInterestRefundTemplate(loanId, targetTransactionId);
    }

    @Override
    public LoanTransactionData retrieveLoanReAgeTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveLoanReAgeTemplate(loanId);
    }

    @Override
    public LoanTransactionData retrieveLoanReAmortizationTemplate(Long loanId) {
        return loanTemplateReadPlatformService.retrieveLoanReAmortizationTemplate(loanId);
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
    public Integer countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(Long loanId) {
        return loanRepaymentScheduleService.countInstallmentsByLoanIdWhereIsAdditionalFalseAndIsDownPaymentFalse(loanId);
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

}
