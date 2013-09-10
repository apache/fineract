/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.portfolio.account.data.AccountTransferData;
import org.mifosplatform.portfolio.accountdetails.service.AccountEnumerations;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.service.CalendarReadPlatformService;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.domain.ClientEnumerations;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.service.FundReadPlatformService;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.data.GroupRoleData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.mifosplatform.portfolio.loanaccount.data.DisbursementData;
import org.mifosplatform.portfolio.loanaccount.data.LoanAccountData;
import org.mifosplatform.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.mifosplatform.portfolio.loanaccount.data.LoanStatusEnumData;
import org.mifosplatform.portfolio.loanaccount.data.LoanSummaryData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.mifosplatform.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransaction;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionType;
import org.mifosplatform.portfolio.loanaccount.exception.LoanNotFoundException;
import org.mifosplatform.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.mifosplatform.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LoanReadPlatformServiceImpl implements LoanReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanRepository loanRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanDropdownReadPlatformService loanDropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final PaginationHelper<LoanAccountData> paginationHelper = new PaginationHelper<LoanAccountData>();
    private final LoanMapper loaanLoanMapper = new LoanMapper();

    @Autowired
    public LoanReadPlatformServiceImpl(final PlatformSecurityContext context, final LoanRepository loanRepository,
            final LoanTransactionRepository loanTransactionRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final LoanProductReadPlatformService loanProductReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final GroupReadPlatformService groupReadPlatformService, final LoanDropdownReadPlatformService loanDropdownReadPlatformService,
            final FundReadPlatformService fundReadPlatformService, final ChargeReadPlatformService chargeReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final RoutingDataSource dataSource,
            final CalendarReadPlatformService calendarReadPlatformService, final StaffReadPlatformService staffReadPlatformService) {
        this.context = context;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.loanDropdownReadPlatformService = loanDropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public LoanAccountData retrieveOne(final Long loanId) {

        try {
            final AppUser currentUser = context.authenticatedUser();
            final String hierarchy = currentUser.getOffice().getHierarchy();
            final String hierarchySearchString = hierarchy + "%";

            LoanMapper rm = new LoanMapper();

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select ");
            sqlBuilder.append(rm.loanSchema());
            sqlBuilder.append(" join m_office o on (o.id = c.office_id or o.id = g.office_id) ");
            sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
            sqlBuilder.append(" where l.id=? and ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), rm, new Object[] { loanId, hierarchySearchString,
                    hierarchySearchString });
        } catch (EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId);
        }
    }

    @Override
    public LoanScheduleData retrieveRepaymentSchedule(final Long loanId,
            final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData) {

        try {
            context.authenticatedUser();

            final LoanScheduleResultSetExtractor fullResultsetExtractor = new LoanScheduleResultSetExtractor(
                    repaymentScheduleRelatedLoanData);
            final String sql = "select " + fullResultsetExtractor.schema() + " where ls.loan_id = ? order by ls.loan_id, ls.installment";

            return this.jdbcTemplate.query(sql, fullResultsetExtractor, new Object[] { loanId });
        } catch (EmptyResultDataAccessException e) {
            throw new LoanNotFoundException(loanId);
        }
    }

    @Override
    public Collection<LoanTransactionData> retrieveLoanTransactions(final Long loanId) {
        try {
            context.authenticatedUser();

            LoanTransactionsMapper rm = new LoanTransactionsMapper();

            // retrieve all loan transactions that are not invalid and have not
            // been 'contra'ed by another transaction
            // repayments at time of disbursement (e.g. charges)

            /*** TODO Vishwas: Remove references to "Contra" from the codebase ***/
            String sql = "select "
                    + rm.LoanPaymentsSchema()
                    + " where tr.loan_id = ? and tr.transaction_type_enum not in (0, 3) and tr.is_reversed=0 order by tr.transaction_date ASC,id ";
            return this.jdbcTemplate.query(sql, rm, new Object[] { loanId });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public Page<LoanAccountData> retrieveAll(final SearchParameters searchParameters) {

        final AppUser currentUser = context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(loaanLoanMapper.loanSchema());

        // TODO - for time being this will data scope list of loans returned to
        // only loans that have a client associated.
        // to support senario where loan has group_id only OR client_id will
        // probably require a UNION query
        // but that at present is an edge case
        sqlBuilder.append(" join m_office o on o.id = c.office_id");
        sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
        sqlBuilder.append(" where ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

        final Object[] objectArray = new Object[2];
        objectArray[0] = hierarchySearchString;
        objectArray[1] = hierarchySearchString;
        int arrayPos = 2;

        String sqlQueryCriteria = searchParameters.getSqlSearch();
        if (StringUtils.isNotBlank(sqlQueryCriteria)) {
            sqlQueryCriteria = sqlQueryCriteria.replaceAll("accountNo", "l.account_no");
            sqlBuilder.append(" and (").append(sqlQueryCriteria).append(")");
        }

        if (StringUtils.isNotBlank(searchParameters.getExternalId())) {
            sqlBuilder.append(" and l.external_id = ?");
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
                this.loaanLoanMapper);
    }

    @Override
    public LoanAccountData retrieveTemplateWithClientAndProductDetails(final Long clientId, final Long productId) {

        context.authenticatedUser();

        final ClientData clientAccount = this.clientReadPlatformService.retrieveOne(clientId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        LoanAccountData loanTemplateDetails = LoanAccountData.clientDefaults(clientAccount.id(), clientAccount.displayName(),
                clientAccount.officeId(), expectedDisbursementDate);

        if (productId != null) {
            final LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanTemplateDetails = LoanAccountData.populateLoanProductDefaults(loanTemplateDetails, selectedProduct);
        }

        return loanTemplateDetails;
    }

    @Override
    public LoanAccountData retrieveTemplateWithGroupAndProductDetails(final Long groupId, final Long productId) {

        context.authenticatedUser();

        final GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        LoanAccountData loanDetails = LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);

        if (productId != null) {
            LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanDetails = LoanAccountData.populateLoanProductDefaults(loanDetails, selectedProduct);
        }

        return loanDetails;
    }

    @Override
    public LoanAccountData retrieveTemplateWithCompleteGroupAndProductDetails(final Long groupId, final Long productId) {

        context.authenticatedUser();

        GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        // get group associations
        Collection<ClientData> membersOfGroup = this.clientReadPlatformService.retrieveClientMembersOfGroup(groupId);
        if (!CollectionUtils.isEmpty(membersOfGroup)) {
            final Collection<CalendarData> calendarsData = null;
            final CalendarData collectionMeetingCalendar = null;
            final Collection<GroupRoleData> groupRoles = null;
            groupAccount = GroupGeneralData.withAssocations(groupAccount, membersOfGroup, groupRoles, calendarsData,
                    collectionMeetingCalendar);
        }

        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        LoanAccountData loanDetails = LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);

        if (productId != null) {
            LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
            loanDetails = LoanAccountData.populateLoanProductDefaults(loanDetails, selectedProduct);
        }

        return loanDetails;
    }

    @Override
    public LoanTransactionData retrieveLoanTransactionTemplate(final Long loanId) {

        context.authenticatedUser();

        // TODO - KW - OPTIMIZE - write simple sql query to fetch back date of
        // possible next transaction date.
        Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);

        final CurrencyData currencyData = applicationCurrency.toData();

        final LocalDate earliestUnpaidInstallmentDate = loan.possibleNextRepaymentDate();

        final Money possibleNextRepaymentAmount = loan.possibleNextRepaymentAmount();
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
        final Collection<CodeValueData> paymentOptions = codeValueReadPlatformService
                .retrieveCodeValuesByCode(PaymentDetailConstants.paymentTypeCodeName);
        return new LoanTransactionData(null, null, null, transactionType, null, currencyData, earliestUnpaidInstallmentDate,
                possibleNextRepaymentAmount.getAmount(), null, null, null, null, paymentOptions, null,null);
    }

    @Override
    public LoanTransactionData retrieveWaiveInterestDetails(final Long loanId) {

        context.authenticatedUser();

        // TODO - KW -OPTIMIZE - write simple sql query to fetch back overdue
        // interest that can be waived along with the date of repayment period
        // interest is overdue.
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CurrencyData currencyData = applicationCurrency.toData();

        final LoanTransaction waiveOfInterest = loan.deriveDefaultInterestWaiverTransaction();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WAIVE_INTEREST);

        final BigDecimal amount = waiveOfInterest.getAmount(currency).getAmount();
        return new LoanTransactionData(null, null, null, transactionType, null, currencyData, waiveOfInterest.getTransactionDate(), amount,
                null, null, null, null, null, null);
    }

    @Override
    public LoanTransactionData retrieveNewClosureDetails() {

        context.authenticatedUser();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.WRITEOFF);
        return new LoanTransactionData(null, null, null, transactionType, null, null, DateUtils.getLocalDateOfTenant(), null, null, null,
                null, null, null, null);
    }

    @Override
    public LoanTransactionData retrieveDisbursalTemplate(final Long loanId) {
        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.DISBURSEMENT);
        final Collection<CodeValueData> paymentOptions = codeValueReadPlatformService
                .retrieveCodeValuesByCode(PaymentDetailConstants.paymentTypeCodeName);
        return new LoanTransactionData(null, null, null, transactionType, null, null, loan.getExpectedDisbursedOnLocalDate(), null, null,
                null, null, null, paymentOptions, null,null);
    }

    @Override
    public LoanTransactionData retrieveLoanTransaction(final Long loanId, final Long transactionId) {

        context.authenticatedUser();

        final Loan loan = this.loanRepository.findOne(loanId);
        if (loan == null) { throw new LoanNotFoundException(loanId); }

        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        final CurrencyData currencyData = applicationCurrency.toData();

        final LoanTransaction transaction = this.loanTransactionRepository.findOne(transactionId);
        if (transaction == null) { throw new LoanTransactionNotFoundException(transactionId); }

        if (transaction.isNotBelongingToLoanOf(loan)) { throw new LoanTransactionNotFoundException(transactionId, loanId); }
        
        LoanTransactionsAccountTransferMapper trasfermapper = new LoanTransactionsAccountTransferMapper();
        String sql = "select "
                + trasfermapper.accountTransferSchema()
                + " where tr.loan_id = ? and tr.id = ?";
        AccountTransferData accountTransferData = this.jdbcTemplate.queryForObject(sql, trasfermapper, loanId, transactionId);
        return transaction.toData(currencyData,accountTransferData);
    }

    private static final class LoanMapper implements RowMapper<LoanAccountData> {

        public String loanSchema() {
            return "l.id as id, l.account_no as accountNo, l.external_id as externalId, l.fund_id as fundId, f.name as fundName,"
                    + " l.loan_type_enum as loanType, l.loanpurpose_cv_id as loanPurposeId, cv.code_value as loanPurposeName,"
                    + " lp.id as loanProductId, lp.name as loanProductName, lp.description as loanProductDescription,"
                    + " c.id as clientId, c.display_name as clientName, c.office_id as clientOfficeId,"
                    + " g.id as groupId, g.display_name as groupName,"
                    + " g.office_id as groupOfficeId, g.staff_id As groupStaffId , g.parent_id as groupParentId, "
                    + " g.hierarchy As groupHierarchy , g.external_id As groupExternalId, "
                    + " g.status_enum as statusEnum, g.activation_date as activationDate, "
                    + " l.submittedon_date as submittedOnDate, sbu.username as submittedByUsername, sbu.firstname as submittedByFirstname, sbu.lastname as submittedByLastname,"
                    + " l.rejectedon_date as rejectedOnDate, rbu.username as rejectedByUsername, rbu.firstname as rejectedByFirstname, rbu.lastname as rejectedByLastname,"
                    + " l.withdrawnon_date as withdrawnOnDate, wbu.username as withdrawnByUsername, wbu.firstname as withdrawnByFirstname, wbu.lastname as withdrawnByLastname,"
                    + " l.approvedon_date as approvedOnDate, abu.username as approvedByUsername, abu.firstname as approvedByFirstname, abu.lastname as approvedByLastname,"
                    + " l.expected_disbursedon_date as expectedDisbursementDate, l.disbursedon_date as actualDisbursementDate, dbu.username as disbursedByUsername, dbu.firstname as disbursedByFirstname, dbu.lastname as disbursedByLastname,"
                    + " l.closedon_date as closedOnDate, cbu.username as closedByUsername, cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname, l.writtenoffon_date as writtenOffOnDate, "
                    + " l.expected_firstrepaymenton_date as expectedFirstRepaymentOnDate, l.interest_calculated_from_date as interestChargedFromDate, l.expected_maturedon_date as expectedMaturityDate, "
                    + " l.principal_amount as principal, l.arrearstolerance_amount as inArrearsTolerance, l.number_of_repayments as numberOfRepayments, l.repay_every as repaymentEvery,"
                    + " l.grace_on_principal_periods as graceOnPrincipalPayment, l.grace_on_interest_periods as graceOnInterestPayment, l.grace_interest_free_periods as graceOnInterestCharged,"
                    + " l.nominal_interest_rate_per_period as interestRatePerPeriod, l.annual_nominal_interest_rate as annualInterestRate, "
                    + " l.repayment_period_frequency_enum as repaymentFrequencyType, l.interest_period_frequency_enum as interestRateFrequencyType, "
                    + " l.term_frequency as termFrequency, l.term_period_frequency_enum as termPeriodFrequencyType, "
                    + " l.amortization_method_enum as amortizationType, l.interest_method_enum as interestType, l.interest_calculated_in_period_enum as interestCalculationPeriodType,"
                    + " l.loan_status_id as lifeCycleStatusId, l.loan_transaction_strategy_id as transactionStrategyId, "
                    + " lps.name as transactionStrategyName, "
                    + " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                    + " l.loan_officer_id as loanOfficerId, s.display_name as loanOfficerName, "
                    + " l.principal_disbursed_derived as principalDisbursed,"
                    + " l.principal_repaid_derived as principalPaid,"
                    + " l.principal_writtenoff_derived as principalWrittenOff,"
                    + " l.principal_outstanding_derived as principalOutstanding,"
                    + " l.interest_charged_derived as interestCharged,"
                    + " l.interest_repaid_derived as interestPaid,"
                    + " l.interest_waived_derived as interestWaived,"
                    + " l.interest_writtenoff_derived as interestWrittenOff,"
                    + " l.interest_outstanding_derived as interestOutstanding,"
                    + " l.fee_charges_charged_derived as feeChargesCharged,"
                    + " l.total_charges_due_at_disbursement_derived as feeChargesDueAtDisbursementCharged,"
                    + " l.fee_charges_repaid_derived as feeChargesPaid,"
                    + " l.fee_charges_waived_derived as feeChargesWaived,"
                    + " l.fee_charges_writtenoff_derived as feeChargesWrittenOff,"
                    + " l.fee_charges_outstanding_derived as feeChargesOutstanding,"
                    + " l.penalty_charges_charged_derived as penaltyChargesCharged,"
                    + " l.penalty_charges_repaid_derived as penaltyChargesPaid,"
                    + " l.penalty_charges_waived_derived as penaltyChargesWaived,"
                    + " l.penalty_charges_writtenoff_derived as penaltyChargesWrittenOff,"
                    + " l.penalty_charges_outstanding_derived as penaltyChargesOutstanding,"
                    + " l.total_expected_repayment_derived as totalExpectedRepayment,"
                    + " l.total_repayment_derived as totalRepayment,"
                    + " l.total_expected_costofloan_derived as totalExpectedCostOfLoan,"
                    + " l.total_costofloan_derived as totalCostOfLoan,"
                    + " l.total_waived_derived as totalWaived,"
                    + " l.total_writtenoff_derived as totalWrittenOff,"
                    + " l.total_outstanding_derived as totalOutstanding,"
                    + " l.total_overpaid_derived as totalOverpaid,"
                    + " la.principal_overdue_derived as principalOverdue,"
                    + " la.interest_overdue_derived as interestOverdue,"
                    + " la.fee_charges_overdue_derived as feeChargesOverdue,"
                    + " la.penalty_charges_overdue_derived as penaltyChargesOverdue,"
                    + " la.total_overdue_derived as totalOverdue,"
                    + " la.overdue_since_date_derived as overdueSinceDate,"
                    + " l.sync_disbursement_with_meeting as syncDisbursementWithMeeting,"
                    + " l.loan_counter as loanCounter, l.loan_product_counter as loanProductCounter"
                    + " from m_loan l" //
                    + " join m_product_loan lp on lp.id = l.product_id" //
                    + " join m_currency rc on rc.`code` = l.currency_code" //
                    + " left join m_client c on c.id = l.client_id" //
                    + " left join m_group g on g.id = l.group_id" //
                    + " left join m_loan_arrears_aging la on la.loan_id = l.id" //
                    + " left join m_fund f on f.id = l.fund_id" //
                    + " left join m_staff s on s.id = l.loan_officer_id" //
                    + " left join m_appuser sbu on sbu.id = l.submittedon_userid"
                    + " left join m_appuser rbu on rbu.id = l.rejectedon_userid"
                    + " left join m_appuser wbu on wbu.id = l.withdrawnon_userid"
                    + " left join m_appuser abu on abu.id = l.approvedon_userid"
                    + " left join m_appuser dbu on dbu.id = l.disbursedon_userid"
                    + " left join m_appuser cbu on cbu.id = l.closedon_userid"
                    + " left join m_code_value cv on cv.id = l.loanpurpose_cv_id"
                    + " left join ref_loan_transaction_processing_strategy lps on lps.id = l.loan_transaction_strategy_id";
        }

        @Override
        public LoanAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long clientOfficeId = JdbcSupport.getLong(rs, "clientOfficeId");
            final String clientName = rs.getString("clientName");

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final String groupExternalId = rs.getString("groupExternalId");
            final Long groupOfficeId = JdbcSupport.getLong(rs, "groupOfficeId");
            final Long groupStaffId = JdbcSupport.getLong(rs, "groupStaffId");
            final Long groupParentId = JdbcSupport.getLong(rs, "groupParentId");
            final String groupHierarchy = rs.getString("groupHierarchy");

            final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
            final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeId);

            final Long fundId = JdbcSupport.getLong(rs, "fundId");
            final String fundName = rs.getString("fundName");

            final Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
            final String loanOfficerName = rs.getString("loanOfficerName");

            final Long loanPurposeId = JdbcSupport.getLong(rs, "loanPurposeId");
            final String loanPurposeName = rs.getString("loanPurposeName");

            final Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
            final String loanProductName = rs.getString("loanProductName");
            final String loanProductDescription = rs.getString("loanProductDescription");

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

            final LocalDate expectedDisbursementDate = JdbcSupport.getLocalDate(rs, "expectedDisbursementDate");
            final LocalDate actualDisbursementDate = JdbcSupport.getLocalDate(rs, "actualDisbursementDate");
            final String disbursedByUsername = rs.getString("disbursedByUsername");
            final String disbursedByFirstname = rs.getString("disbursedByFirstname");
            final String disbursedByLastname = rs.getString("disbursedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate writtenOffOnDate = JdbcSupport.getLocalDate(rs, "writtenOffOnDate");

            final LocalDate expectedMaturityDate = JdbcSupport.getLocalDate(rs, "expectedMaturityDate");

            final LoanApplicationTimelineData timeline = new LoanApplicationTimelineData(submittedOnDate, submittedByUsername,
                    submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname,
                    withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername,
                    approvedByFirstname, approvedByLastname, expectedDisbursementDate, actualDisbursementDate, disbursedByUsername,
                    disbursedByFirstname, disbursedByLastname, closedOnDate, closedByUsername, closedByFirstname, closedByLastname,
                    expectedMaturityDate, writtenOffOnDate, closedByUsername, closedByFirstname, closedByLastname);

            final BigDecimal principal = rs.getBigDecimal("principal");
            final BigDecimal totalOverpaid = rs.getBigDecimal("totalOverpaid");
            final BigDecimal inArrearsTolerance = rs.getBigDecimal("inArrearsTolerance");

            final Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
            final Integer repaymentEvery = JdbcSupport.getInteger(rs, "repaymentEvery");
            final BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
            final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

            final Integer graceOnPrincipalPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnPrincipalPayment");
            final Integer graceOnInterestPayment = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestPayment");
            final Integer graceOnInterestCharged = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "graceOnInterestCharged");

            final Integer termFrequency = JdbcSupport.getInteger(rs, "termFrequency");
            final Integer termPeriodFrequencyTypeInt = JdbcSupport.getInteger(rs, "termPeriodFrequencyType");
            final EnumOptionData termPeriodFrequencyType = LoanEnumerations.termFrequencyType(termPeriodFrequencyTypeInt);

            final int repaymentFrequencyTypeInt = JdbcSupport.getInteger(rs, "repaymentFrequencyType");
            final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeInt);

            final int interestRateFrequencyTypeInt = JdbcSupport.getInteger(rs, "interestRateFrequencyType");
            final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeInt);

            final Long transactionStrategyId = JdbcSupport.getLong(rs, "transactionStrategyId");
            final String transactionStrategyName = rs.getString("transactionStrategyName");

            final int amortizationTypeInt = JdbcSupport.getInteger(rs, "amortizationType");
            final int interestTypeInt = JdbcSupport.getInteger(rs, "interestType");
            final int interestCalculationPeriodTypeInt = JdbcSupport.getInteger(rs, "interestCalculationPeriodType");

            final EnumOptionData amortizationType = LoanEnumerations.amortizationType(amortizationTypeInt);
            final EnumOptionData interestType = LoanEnumerations.interestType(interestTypeInt);
            final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                    .interestCalculationPeriodType(interestCalculationPeriodTypeInt);

            final Integer lifeCycleStatusId = JdbcSupport.getInteger(rs, "lifeCycleStatusId");
            final LoanStatusEnumData status = LoanEnumerations.status(lifeCycleStatusId);

            // settings
            final LocalDate expectedFirstRepaymentOnDate = JdbcSupport.getLocalDate(rs, "expectedFirstRepaymentOnDate");
            final LocalDate interestChargedFromDate = JdbcSupport.getLocalDate(rs, "interestChargedFromDate");

            final Boolean syncDisbursementWithMeeting = rs.getBoolean("syncDisbursementWithMeeting");

            final BigDecimal feeChargesDueAtDisbursementCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                    "feeChargesDueAtDisbursementCharged");
            LoanSummaryData loanSummary = null;
            if (status.id().intValue() >= 300) {

                // loan summary
                final BigDecimal principalDisbursed = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDisbursed");
                final BigDecimal principalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalPaid");
                final BigDecimal principalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalWrittenOff");
                final BigDecimal principalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalOutstanding");
                final BigDecimal principalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalOverdue");

                final BigDecimal interestCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestCharged");
                final BigDecimal interestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestPaid");
                final BigDecimal interestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWaived");
                final BigDecimal interestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");
                final BigDecimal interestOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestOutstanding");
                final BigDecimal interestOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestOverdue");

                final BigDecimal feeChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesCharged");
                final BigDecimal feeChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesPaid");
                final BigDecimal feeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWaived");
                final BigDecimal feeChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWrittenOff");
                final BigDecimal feeChargesOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesOutstanding");
                final BigDecimal feeChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesOverdue");

                final BigDecimal penaltyChargesCharged = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesCharged");
                final BigDecimal penaltyChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesPaid");
                final BigDecimal penaltyChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWaived");
                final BigDecimal penaltyChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWrittenOff");
                final BigDecimal penaltyChargesOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesOutstanding");
                final BigDecimal penaltyChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesOverdue");

                final BigDecimal totalExpectedRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalExpectedRepayment");
                final BigDecimal totalRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRepayment");
                final BigDecimal totalExpectedCostOfLoan = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalExpectedCostOfLoan");
                final BigDecimal totalCostOfLoan = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalCostOfLoan");
                final BigDecimal totalWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalWaived");
                final BigDecimal totalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalWrittenOff");
                final BigDecimal totalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOutstanding");
                final BigDecimal totalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdue");

                final LocalDate overdueSinceDate = JdbcSupport.getLocalDate(rs, "overdueSinceDate");

                loanSummary = new LoanSummaryData(currencyData, principalDisbursed, principalPaid, principalWrittenOff,
                        principalOutstanding, principalOverdue, interestCharged, interestPaid, interestWaived, interestWrittenOff,
                        interestOutstanding, interestOverdue, feeChargesCharged, feeChargesDueAtDisbursementCharged, feeChargesPaid,
                        feeChargesWaived, feeChargesWrittenOff, feeChargesOutstanding, feeChargesOverdue, penaltyChargesCharged,
                        penaltyChargesPaid, penaltyChargesWaived, penaltyChargesWrittenOff, penaltyChargesOutstanding,
                        penaltyChargesOverdue, totalExpectedRepayment, totalRepayment, totalExpectedCostOfLoan, totalCostOfLoan,
                        totalWaived, totalWrittenOff, totalOutstanding, totalOverdue, overdueSinceDate);
            }

            GroupGeneralData groupData = null;
            if (groupId != null) {
                final Integer groupStatusEnum = JdbcSupport.getInteger(rs, "statusEnum");
                final EnumOptionData groupStatus = ClientEnumerations.status(groupStatusEnum);
                final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
                groupData = GroupGeneralData.instance(groupId, groupName, groupExternalId, groupStatus, activationDate, groupOfficeId,
                        null, groupParentId, null, groupStaffId, null, groupHierarchy);
            }

            final Integer loanCounter = JdbcSupport.getInteger(rs, "loanCounter");
            final Integer loanProductCounter = JdbcSupport.getInteger(rs, "loanProductCounter");

            return LoanAccountData.basicLoanDetails(id, accountNo, status, externalId, clientId, clientName, clientOfficeId, groupData,
                    loanType, loanProductId, loanProductName, loanProductDescription, fundId, fundName, loanPurposeId, loanPurposeName,
                    loanOfficerId, loanOfficerName, currencyData, principal, totalOverpaid, inArrearsTolerance, termFrequency,
                    termPeriodFrequencyType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, transactionStrategyId,
                    transactionStrategyName, amortizationType, interestRatePerPeriod, interestRateFrequencyType, annualInterestRate,
                    interestType, interestCalculationPeriodType, expectedFirstRepaymentOnDate, graceOnPrincipalPayment,
                    graceOnInterestPayment, graceOnInterestCharged, interestChargedFromDate, timeline, loanSummary,
                    feeChargesDueAtDisbursementCharged, syncDisbursementWithMeeting, loanCounter, loanProductCounter);
        }
    }

    private static final class LoanScheduleResultSetExtractor implements ResultSetExtractor<LoanScheduleData> {

        private final CurrencyData currency;
        private final DisbursementData disbursement;
        private final BigDecimal totalFeeChargesDueAtDisbursement;
        private LocalDate lastDueDate;
        private BigDecimal outstandingLoanPrincipalBalance;

        public LoanScheduleResultSetExtractor(final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData) {
            this.currency = repaymentScheduleRelatedLoanData.getCurrency();
            this.disbursement = repaymentScheduleRelatedLoanData.disbursementData();
            this.totalFeeChargesDueAtDisbursement = repaymentScheduleRelatedLoanData.getTotalFeeChargesAtDisbursement();
            this.lastDueDate = disbursement.disbursementDate();
            this.outstandingLoanPrincipalBalance = disbursement.amount();
        }

        public String schema() {

            return " ls.loan_id as loanId, ls.installment as period, ls.fromdate as fromDate, ls.duedate as dueDate, ls.obligations_met_on_date as obligationsMetOnDate, ls.completed_derived as complete,"
                    + " ls.principal_amount as principalDue, ls.principal_completed_derived as principalPaid, ls.principal_writtenoff_derived as principalWrittenOff, "
                    + " ls.interest_amount as interestDue, ls.interest_completed_derived as interestPaid, ls.interest_waived_derived as interestWaived, ls.interest_writtenoff_derived as interestWrittenOff, "
                    + " ls.fee_charges_amount as feeChargesDue, ls.fee_charges_completed_derived as feeChargesPaid, ls.fee_charges_waived_derived as feeChargesWaived, ls.fee_charges_writtenoff_derived as feeChargesWrittenOff, "
                    + " ls.penalty_charges_amount as penaltyChargesDue, ls.penalty_charges_completed_derived as penaltyChargesPaid, ls.penalty_charges_waived_derived as penaltyChargesWaived, ls.penalty_charges_writtenoff_derived as penaltyChargesWrittenOff, "
                    + " ls.total_paid_in_advance_derived as totalPaidInAdvanceForPeriod, ls.total_paid_late_derived as totalPaidLateForPeriod "
                    + " from m_loan_repayment_schedule ls ";
        }

        @Override
        public LoanScheduleData extractData(final ResultSet rs) throws SQLException, DataAccessException {

            final LoanSchedulePeriodData disbursementPeriod = LoanSchedulePeriodData.disbursementOnlyPeriod(
                    this.disbursement.disbursementDate(), this.disbursement.amount(), this.totalFeeChargesDueAtDisbursement,
                    this.disbursement.isDisbursed());

            final Collection<LoanSchedulePeriodData> periods = new ArrayList<LoanSchedulePeriodData>();
            periods.add(disbursementPeriod);

            final MonetaryCurrency monCurrency = new MonetaryCurrency(this.currency.code(), this.currency.decimalPlaces(),
                    this.currency.currencyInMultiplesOf());
            final BigDecimal totalPrincipalDisbursed = Money.of(monCurrency, this.disbursement.amount()).getAmount();
            Money totalPrincipalExpected = Money.zero(monCurrency);
            Money totalPrincipalPaid = Money.zero(monCurrency);
            Money totalInterestCharged = Money.zero(monCurrency);
            Money totalFeeChargesCharged = Money.zero(monCurrency);
            Money totalPenaltyChargesCharged = Money.zero(monCurrency);
            Money totalWaived = Money.zero(monCurrency);
            Money totalWrittenOff = Money.zero(monCurrency);
            Money totalRepaymentExpected = Money.zero(monCurrency);
            Money totalRepayment = Money.zero(monCurrency);
            Money totalPaidInAdvance = Money.zero(monCurrency);
            Money totalPaidLate = Money.zero(monCurrency);
            Money totalOutstanding = Money.zero(monCurrency);

            // update totals with details of fees charged during disbursement
            totalFeeChargesCharged = totalFeeChargesCharged.plus(disbursementPeriod.feeChargesDue());
            totalRepaymentExpected = totalRepaymentExpected.plus(disbursementPeriod.feeChargesDue());
            totalRepayment = totalRepayment.plus(disbursementPeriod.feeChargesPaid());
            totalOutstanding = totalOutstanding.plus(disbursementPeriod.feeChargesDue()).minus(disbursementPeriod.feeChargesPaid());

            Integer loanTermInDays = Integer.valueOf(0);
            while (rs.next()) {

                final Long loanId = rs.getLong("loanId");
                final Integer period = JdbcSupport.getInteger(rs, "period");
                LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
                final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
                final LocalDate obligationsMetOnDate = JdbcSupport.getLocalDate(rs, "obligationsMetOnDate");
                final boolean complete = rs.getBoolean("complete");

                Integer daysInPeriod = Integer.valueOf(0);
                if (fromDate != null) {
                    daysInPeriod = Days.daysBetween(fromDate, dueDate).getDays();
                    loanTermInDays = Integer.valueOf(loanTermInDays.intValue() + daysInPeriod.intValue());
                }

                final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
                totalPrincipalExpected = totalPrincipalExpected.plus(principalDue);
                final BigDecimal principalPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalPaid");
                totalPrincipalPaid = totalPrincipalPaid.plus(principalPaid);
                final BigDecimal principalWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalWrittenOff");

                final BigDecimal principalOutstanding = principalDue.subtract(principalPaid).subtract(principalWrittenOff);

                final BigDecimal interestExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
                totalInterestCharged = totalInterestCharged.plus(interestExpectedDue);
                final BigDecimal interestPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestPaid");
                final BigDecimal interestWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWaived");
                final BigDecimal interestWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestWrittenOff");

                final BigDecimal interestActualDue = interestExpectedDue.subtract(interestWaived).subtract(interestWrittenOff);
                final BigDecimal interestOutstanding = interestActualDue.subtract(interestPaid);

                final BigDecimal feeChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesDue");
                totalFeeChargesCharged = totalFeeChargesCharged.plus(feeChargesExpectedDue);
                final BigDecimal feeChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesPaid");
                final BigDecimal feeChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWaived");
                final BigDecimal feeChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesWrittenOff");

                final BigDecimal feeChargesActualDue = feeChargesExpectedDue.subtract(feeChargesWaived).subtract(feeChargesWrittenOff);
                final BigDecimal feeChargesOutstanding = feeChargesActualDue.subtract(feeChargesPaid);

                final BigDecimal penaltyChargesExpectedDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesDue");
                totalPenaltyChargesCharged = totalPenaltyChargesCharged.plus(penaltyChargesExpectedDue);
                final BigDecimal penaltyChargesPaid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesPaid");
                final BigDecimal penaltyChargesWaived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWaived");
                final BigDecimal penaltyChargesWrittenOff = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesWrittenOff");

                final BigDecimal totalPaidInAdvanceForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs,
                        "totalPaidInAdvanceForPeriod");
                final BigDecimal totalPaidLateForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalPaidLateForPeriod");

                final BigDecimal penaltyChargesActualDue = penaltyChargesExpectedDue.subtract(penaltyChargesWaived).subtract(
                        penaltyChargesWrittenOff);
                final BigDecimal penaltyChargesOutstanding = penaltyChargesActualDue.subtract(penaltyChargesPaid);

                final BigDecimal totalExpectedCostOfLoanForPeriod = interestExpectedDue.add(feeChargesExpectedDue).add(
                        penaltyChargesExpectedDue);

                final BigDecimal totalDueForPeriod = principalDue.add(totalExpectedCostOfLoanForPeriod);
                final BigDecimal totalPaidForPeriod = principalPaid.add(interestPaid).add(feeChargesPaid).add(penaltyChargesPaid);
                final BigDecimal totalWaivedForPeriod = interestWaived.add(feeChargesWaived).add(penaltyChargesWaived);
                totalWaived = totalWaived.plus(totalWaivedForPeriod);
                final BigDecimal totalWrittenOffForPeriod = principalWrittenOff.add(interestWrittenOff).add(feeChargesWrittenOff)
                        .add(penaltyChargesWrittenOff);
                totalWrittenOff = totalWrittenOff.plus(totalWrittenOffForPeriod);
                final BigDecimal totalOutstandingForPeriod = principalOutstanding.add(interestOutstanding).add(feeChargesOutstanding)
                        .add(penaltyChargesOutstanding);

                final BigDecimal totalActualCostOfLoanForPeriod = interestActualDue.add(feeChargesActualDue).add(penaltyChargesActualDue);

                totalRepaymentExpected = totalRepaymentExpected.plus(totalDueForPeriod);
                totalRepayment = totalRepayment.plus(totalPaidForPeriod);
                totalPaidInAdvance = totalPaidInAdvance.plus(totalPaidInAdvanceForPeriod);
                totalPaidLate = totalPaidLate.plus(totalPaidLateForPeriod);
                totalOutstanding = totalOutstanding.plus(totalOutstandingForPeriod);

                if (fromDate == null) {
                    fromDate = this.lastDueDate;
                }
                final BigDecimal outstandingPrincipalBalanceOfLoan = outstandingLoanPrincipalBalance.subtract(principalDue);

                // update based on current period values
                this.lastDueDate = dueDate;
                outstandingLoanPrincipalBalance = outstandingLoanPrincipalBalance.subtract(principalDue);

                final LoanSchedulePeriodData periodData = LoanSchedulePeriodData.repaymentPeriodWithPayments(loanId, period, fromDate,
                        dueDate, obligationsMetOnDate, complete, principalDue, principalPaid, principalWrittenOff, principalOutstanding,
                        outstandingPrincipalBalanceOfLoan, interestExpectedDue, interestPaid, interestWaived, interestWrittenOff,
                        interestOutstanding, feeChargesExpectedDue, feeChargesPaid, feeChargesWaived, feeChargesWrittenOff,
                        feeChargesOutstanding, penaltyChargesExpectedDue, penaltyChargesPaid, penaltyChargesWaived,
                        penaltyChargesWrittenOff, penaltyChargesOutstanding, totalDueForPeriod, totalPaidForPeriod,
                        totalPaidInAdvanceForPeriod, totalPaidLateForPeriod, totalWaivedForPeriod, totalWrittenOffForPeriod,
                        totalOutstandingForPeriod, totalActualCostOfLoanForPeriod);

                periods.add(periodData);
            }

            return new LoanScheduleData(this.currency, periods, loanTermInDays, totalPrincipalDisbursed,
                    totalPrincipalExpected.getAmount(), totalPrincipalPaid.getAmount(), totalInterestCharged.getAmount(),
                    totalFeeChargesCharged.getAmount(), totalPenaltyChargesCharged.getAmount(), totalWaived.getAmount(),
                    totalWrittenOff.getAmount(), totalRepaymentExpected.getAmount(), totalRepayment.getAmount(),
                    totalPaidInAdvance.getAmount(), totalPaidLate.getAmount(), totalOutstanding.getAmount());
        }
    }

    private static final class LoanTransactionsMapper implements RowMapper<LoanTransactionData> {

        public String LoanPaymentsSchema() {

            return " tr.id as id, tr.transaction_type_enum as transactionType, tr.transaction_date as `date`, tr.amount as total, "
                    + " tr.principal_portion_derived as principal, tr.interest_portion_derived as interest, "
                    + " tr.fee_charges_portion_derived as fees, tr.penalty_charges_portion_derived as penalties, "
                    + " pd.payment_type_cv_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, "
                    + " pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode, "
                    + " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc.`name` as currencyName, "
                    + " rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                    + " cv.code_value as paymentTypeName, tr.external_id as externalId, tr.office_id as officeId, office.name as officeName, "
                    + " fromtran.id as fromTransferId, fromtran.is_reversed as fromTransferReversed,"
                    + " fromtran.transaction_date as fromTransferDate, fromtran.amount as fromTransferAmount,"
                    + " fromtran.description as fromTransferDescription,"
                    + " totran.id as toTransferId, totran.is_reversed as toTransferReversed,"
                    + " totran.transaction_date as toTransferDate, totran.amount as toTransferAmount,"
                    + " totran.description as toTransferDescription "
                    + " from m_loan l join m_loan_transaction tr on tr.loan_id = l.id"
                    + " join m_currency rc on rc.`code` = l.currency_code "
                    + " left JOIN m_payment_detail pd ON tr.payment_detail_id = pd.id"
                    + " left join m_code_value cv on pd.payment_type_cv_id = cv.id"
                    + " left join m_office office on office.id=tr.office_id"
                    + " left join m_savings_account_transfer fromtran on fromtran.from_loan_transaction_id = tr.id "
                    + " left join m_savings_account_transfer totran on totran.to_loan_transaction_id = tr.id ";
        }

        @Override
        public LoanTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            final Long id = rs.getLong("id");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(transactionTypeInt);

            PaymentDetailData paymentDetailData = null;

            if (transactionType.isPaymentOrReceipt()) {
                final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
                if (paymentTypeId != null) {
                    final String typeName = rs.getString("paymentTypeName");
                    CodeValueData paymentType = CodeValueData.instance(paymentTypeId, typeName);
                    final String accountNumber = rs.getString("accountNumber");
                    final String checkNumber = rs.getString("checkNumber");
                    final String routingCode = rs.getString("routingCode");
                    final String receiptNumber = rs.getString("receiptNumber");
                    final String bankNumber = rs.getString("bankNumber");
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
            }
            final LocalDate date = JdbcSupport.getLocalDate(rs, "date");
            final BigDecimal totalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "total");
            final BigDecimal principalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
            final BigDecimal interestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interest");
            final BigDecimal feeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fees");
            final BigDecimal penaltyChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penalties");
            final String externalId = rs.getString("externalId");
            
            AccountTransferData transfer = null;
            final Long fromTransferId = JdbcSupport.getLong(rs, "fromTransferId");
            final Long toTransferId = JdbcSupport.getLong(rs, "toTransferId");
            if (fromTransferId != null) {
                final LocalDate fromTransferDate = JdbcSupport.getLocalDate(rs, "fromTransferDate");
                final BigDecimal fromTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fromTransferAmount");
                final boolean fromTransferReversed = rs.getBoolean("fromTransferReversed");
                final String fromTransferDescription = rs.getString("fromTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(fromTransferId, currencyData, fromTransferAmount, fromTransferDate,
                        fromTransferDescription, fromTransferReversed);
            } else if (toTransferId != null) {
                final LocalDate toTransferDate = JdbcSupport.getLocalDate(rs, "toTransferDate");
                final BigDecimal toTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "toTransferAmount");
                final boolean toTransferReversed = rs.getBoolean("toTransferReversed");
                final String toTransferDescription = rs.getString("toTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(toTransferId, currencyData, toTransferAmount, toTransferDate,
                        toTransferDescription, toTransferReversed);
            }


            return new LoanTransactionData(id, officeId, officeName, transactionType, paymentDetailData, currencyData, date, totalAmount,
                    principalPortion, interestPortion, feeChargesPortion, penaltyChargesPortion, externalId, transfer);
        }
    }

    private static final class LoanTransactionsAccountTransferMapper implements RowMapper<AccountTransferData> {

        public String accountTransferSchema() {

            return " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc.`name` as currencyName, "
                    + " rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                    + " fromtran.id as fromTransferId, fromtran.is_reversed as fromTransferReversed,"
                    + " fromtran.transaction_date as fromTransferDate, fromtran.amount as fromTransferAmount,"
                    + " fromtran.description as fromTransferDescription,"
                    + " totran.id as toTransferId, totran.is_reversed as toTransferReversed,"
                    + " totran.transaction_date as toTransferDate, totran.amount as toTransferAmount,"
                    + " totran.description as toTransferDescription "
                    + " from m_loan l join m_loan_transaction tr on tr.loan_id = l.id"
                    + " join m_currency rc on rc.`code` = l.currency_code "
                    + " left join m_savings_account_transfer fromtran on fromtran.from_loan_transaction_id = tr.id "
                    + " left join m_savings_account_transfer totran on totran.to_loan_transaction_id = tr.id ";
        }

        @Override
        public AccountTransferData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            AccountTransferData transfer = null;
            final Long fromTransferId = JdbcSupport.getLong(rs, "fromTransferId");
            final Long toTransferId = JdbcSupport.getLong(rs, "toTransferId");
            if (fromTransferId != null) {
                final LocalDate fromTransferDate = JdbcSupport.getLocalDate(rs, "fromTransferDate");
                final BigDecimal fromTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fromTransferAmount");
                final boolean fromTransferReversed = rs.getBoolean("fromTransferReversed");
                final String fromTransferDescription = rs.getString("fromTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(fromTransferId, currencyData, fromTransferAmount, fromTransferDate,
                        fromTransferDescription, fromTransferReversed);
            } else if (toTransferId != null) {
                final LocalDate toTransferDate = JdbcSupport.getLocalDate(rs, "toTransferDate");
                final BigDecimal toTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "toTransferAmount");
                final boolean toTransferReversed = rs.getBoolean("toTransferReversed");
                final String toTransferDescription = rs.getString("toTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(toTransferId, currencyData, toTransferAmount, toTransferDate,
                        toTransferDescription, toTransferReversed);
            }
            return transfer;
        }
    }


    @Override
    public LoanAccountData retrieveLoanProductDetailsTemplate(final Long productId) {

        context.authenticatedUser();

        final LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
        final Collection<EnumOptionData> loanTermFrequencyTypeOptions = loanDropdownReadPlatformService
                .retrieveLoanTermFrequencyTypeOptions();
        final Collection<EnumOptionData> repaymentFrequencyTypeOptions = loanDropdownReadPlatformService
                .retrieveRepaymentFrequencyTypeOptions();
        final Collection<EnumOptionData> interestRateFrequencyTypeOptions = loanDropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();
        final Collection<EnumOptionData> amortizationTypeOptions = loanDropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
        final Collection<EnumOptionData> interestTypeOptions = loanDropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        final Collection<EnumOptionData> interestCalculationPeriodTypeOptions = loanDropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();
        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = this.loanDropdownReadPlatformService
                .retreiveTransactionProcessingStrategies();
        final Collection<CodeValueData> loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
        final Collection<CodeValueData> loanCollateralOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode("LoanCollateral");
        final boolean feeChargesOnly = false;
        final Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);

        return LoanAccountData.loanProductWithTemplateDefaults(loanProduct, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, loanPurposeOptions, loanCollateralOptions);
    }

    @Override
    public LoanAccountData retrieveClientDetailsTemplate(final Long clientId) {

        context.authenticatedUser();

        final ClientData clientAccount = this.clientReadPlatformService.retrieveOne(clientId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();

        return LoanAccountData.clientDefaults(clientAccount.id(), clientAccount.displayName(), clientAccount.officeId(),
                expectedDisbursementDate);
    }

    @Override
    public LoanAccountData retrieveGroupDetailsTemplate(final Long groupId) {
        context.authenticatedUser();
        final GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();
        return LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);
    }

    @Override
    public LoanAccountData retrieveGroupAndMembersDetailsTemplate(final Long groupId) {
        GroupGeneralData groupAccount = this.groupReadPlatformService.retrieveOne(groupId);
        final LocalDate expectedDisbursementDate = DateUtils.getLocalDateOfTenant();

        // get group associations
        Collection<ClientData> membersOfGroup = this.clientReadPlatformService.retrieveActiveClientMembersOfGroup(groupId);
        if (!CollectionUtils.isEmpty(membersOfGroup)) {
            final Collection<CalendarData> calendarsData = null;
            final CalendarData collectionMeetingCalendar = null;
            final Collection<GroupRoleData> groupRoles = null;
            groupAccount = GroupGeneralData.withAssocations(groupAccount, membersOfGroup, groupRoles, calendarsData,
                    collectionMeetingCalendar);
        }

        return LoanAccountData.groupDefaults(groupAccount, expectedDisbursementDate);
    }

    @Override
    public Collection<CalendarData> retrieveCalendars(final Long groupId) {
        Collection<CalendarData> calendarsData = new ArrayList<CalendarData>();
        calendarsData.addAll(this.calendarReadPlatformService.retrieveParentCalendarsByEntity(groupId,
                CalendarEntityType.GROUPS.getValue(), null));
        calendarsData
                .addAll(this.calendarReadPlatformService.retrieveCalendarsByEntity(groupId, CalendarEntityType.GROUPS.getValue(), null));
        calendarsData = this.calendarReadPlatformService.generateRecurringDates(calendarsData);
        return calendarsData;
    }

    @Override
    public Collection<StaffData> retrieveAllowedLoanOfficers(Long selectedOfficeId, boolean staffInSelectedOfficeOnly) {
        if (selectedOfficeId == null) return null;

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

}