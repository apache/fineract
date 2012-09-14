package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanPermissionData;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.data.LoanRepaymentPeriodData;
import org.mifosng.platform.api.data.LoanRepaymentTransactionData;
import org.mifosng.platform.api.data.LoanScheduleData;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.CurrencyNotFoundException;
import org.mifosng.platform.exceptions.LoanNotFoundException;
import org.mifosng.platform.exceptions.LoanTransactionNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loan.domain.LoanTransactionType;
import org.mifosng.platform.loanproduct.service.LoanEnumerations;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoanReadPlatformServiceImpl implements LoanReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final LoanRepository loanRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;
	private final LoanProductReadPlatformService loanProductReadPlatformService;
	private final ClientReadPlatformService clientReadPlatformService;
	private final LoanTransactionRepository loanTransactionRepository;

	@Autowired
	public LoanReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final LoanRepository loanRepository,
			final LoanTransactionRepository loanTransactionRepository,
			final ApplicationCurrencyRepository applicationCurrencyRepository,
			final LoanProductReadPlatformService loanProductReadPlatformService,
			final ClientReadPlatformService clientReadPlatformService,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.loanRepository = loanRepository;
		this.loanTransactionRepository = loanTransactionRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.loanProductReadPlatformService = loanProductReadPlatformService;
		this.clientReadPlatformService = clientReadPlatformService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public LoanBasicDetailsData retrieveLoanAccountDetails(final Long loanId) {

		try {
			context.authenticatedUser();

			LoanMapper rm = new LoanMapper();
			
			String sql = "select " + rm.loanSchema() + " where l.id = ?";
			
			return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanId });

		} catch (EmptyResultDataAccessException e) {
			throw new LoanNotFoundException(loanId);
		}

	}

	@Override
	public LoanScheduleData retrieveRepaymentSchedule(final Long loanId, final CurrencyData currency, final Collection<LoanRepaymentTransactionData> loanRepayments) {

		try {
			context.authenticatedUser();

			LoanScheduleMapper rm = new LoanScheduleMapper();
			String sql = "select " + rm.loanScheduleSchema()
					+ " where l.id = ? order by ls.loan_id, ls.installment";
			Collection<LoanRepaymentPeriodData> repaymentSchedulePeriods = this.jdbcTemplate.query(sql, rm, new Object[] { loanId });
			
			return new LoanScheduleData(currency, repaymentSchedulePeriods, loanRepayments);
		} catch (EmptyResultDataAccessException e) {
			throw new LoanNotFoundException(loanId);
		}
	}

	@Override
	public Collection<LoanRepaymentTransactionData> retrieveLoanPayments(Long loanId) {
		try {
			context.authenticatedUser();

			LoanPaymentsMapper rm = new LoanPaymentsMapper();

			// retrieve all loan transactions that are not invalid (0) and not
			// disbursements (1) and have not been 'contra'ed by another transaction
			String sql = "select "
					+ rm.LoanPaymentsSchema()
					+ " where tr.loan_id = ? and tr.transaction_type_enum not in (0, 1) and tr.contra_id is null order by tr.transaction_date ASC";
			return this.jdbcTemplate.query(sql, rm, new Object[] { loanId });

		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public LoanPermissionData retrieveLoanPermissions(
			LoanBasicDetailsData loanBasicDetails, boolean isWaiverAllowed,
			int repaymentAndWaiveCount) {

		boolean pendingApproval = (loanBasicDetails.getStatus().getId().equals(100L));
		boolean waitingForDisbursal = (loanBasicDetails.getStatus().getId().equals(200L));
		boolean isActive = (loanBasicDetails.getStatus().getId().equals(300L));

		boolean waiveAllowed = isWaiverAllowed && isActive;
		boolean makeRepaymentAllowed = isActive;

		boolean rejectAllowed = pendingApproval;
		boolean withdrawnByApplicantAllowed = waitingForDisbursal
				|| pendingApproval;

		boolean undoApprovalAllowed = waitingForDisbursal;

		boolean undoDisbursalAllowed = isActive
				&& (repaymentAndWaiveCount == 0);

		boolean disbursalAllowed = waitingForDisbursal;

		return new LoanPermissionData(waiveAllowed, makeRepaymentAllowed,
				rejectAllowed, withdrawnByApplicantAllowed,
				undoApprovalAllowed, undoDisbursalAllowed, disbursalAllowed,
				pendingApproval, waitingForDisbursal);
	}

	@Override
	public LoanBasicDetailsData retrieveClientAndProductDetails(final Long clientId, final Long productId) {

		context.authenticatedUser();

		final LocalDate expectedDisbursementDate = new LocalDate();
		final ClientData clientAccount = this.clientReadPlatformService.retrieveIndividualClient(clientId);
		
		LoanBasicDetailsData loanDetails = LoanBasicDetailsData.populateForNewLoanCreation(clientAccount.getId(), clientAccount.getDisplayName(), expectedDisbursementDate,
				clientAccount.getOfficeId());
		
		if (productId != null) {
			LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
			loanDetails = LoanBasicDetailsData.populateForNewLoanCreation(loanDetails, selectedProduct);
		}

		return loanDetails;
	}

	@Override
	public LoanTransactionData retrieveNewLoanRepaymentDetails(final Long loanId) {

		context.authenticatedUser();

		// TODO - OPTIMIZE - write simple sql query to fetch back date of
		// possible next transaction date.
		Loan loan = this.loanRepository.findOne(loanId);
		if (loan == null) {
			throw new LoanNotFoundException(loanId);
		}

		final String currencyCode = loan.repaymentScheduleDetail()
				.getPrincipal().getCurrencyCode();
		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(currencyCode);
		if (currency == null) {
			throw new CurrencyNotFoundException(currencyCode);
		}

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());

		LocalDate earliestUnpaidInstallmentDate = loan
				.possibleNextRepaymentDate();
		Money possibleNextRepaymentAmount = loan.possibleNextRepaymentAmount();
		MoneyData possibleNextRepayment = MoneyData.of(currencyData,
				possibleNextRepaymentAmount.getAmount());

		LoanTransactionData newRepaymentDetails = new LoanTransactionData();
		newRepaymentDetails.setTransactionType(LoanEnumerations
				.transactionType(LoanTransactionType.REPAYMENT));
		newRepaymentDetails.setDate(earliestUnpaidInstallmentDate);
		newRepaymentDetails.setTotal(possibleNextRepayment);

		return newRepaymentDetails;
	}

	@Override
	public LoanTransactionData retrieveNewLoanWaiverDetails(final Long loanId) {

		context.authenticatedUser();

		// TODO - OPTIMIZE - write simple sql query to fetch back date of
		// possible next transaction date.
		Loan loan = this.loanRepository.findOne(loanId);
		if (loan == null) {
			throw new LoanNotFoundException(loanId);
		}

		final String currencyCode = loan.repaymentScheduleDetail()
				.getPrincipal().getCurrencyCode();
		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(currencyCode);
		if (currency == null) {
			throw new CurrencyNotFoundException(currencyCode);
		}

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());

		Money totalOutstanding = loan.getTotalOutstanding();
		MoneyData totalOutstandingData = MoneyData.of(currencyData,
				totalOutstanding.getAmount());

		LoanTransactionData newWaiverDetails = new LoanTransactionData();
		newWaiverDetails.setTransactionType(LoanEnumerations
				.transactionType(LoanTransactionType.WAIVED));
		newWaiverDetails.setDate(new LocalDate());
		newWaiverDetails.setTotal(totalOutstandingData);

		return newWaiverDetails;
	}

	@Override
	public LoanTransactionData retrieveLoanTransactionDetails(
			final Long loanId, final Long transactionId) {

		context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loanId);
		if (loan == null) {
			throw new LoanNotFoundException(loanId);
		}

		final String currencyCode = loan.repaymentScheduleDetail()
				.getPrincipal().getCurrencyCode();
		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(currencyCode);
		if (currency == null) {
			throw new CurrencyNotFoundException(currencyCode);
		}

		LoanTransaction transaction = this.loanTransactionRepository
				.findOne(transactionId);
		if (transaction == null) {
			throw new LoanTransactionNotFoundException(transactionId);
		}

		if (transaction.isNotBelongingToLoanOf(loan)) {
			throw new LoanTransactionNotFoundException(transactionId, loanId);
		}

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());
		MoneyData total = MoneyData.of(currencyData, transaction.getAmount());
		LocalDate date = transaction.getTransactionDate();

		LoanTransactionData loanRepaymentData = new LoanTransactionData();
		loanRepaymentData.setTransactionType(LoanEnumerations
				.transactionType(transaction.getTypeOf()));
		loanRepaymentData.setId(transactionId);
		loanRepaymentData.setTotal(total);
		loanRepaymentData.setDate(date);

		return loanRepaymentData;
	}

	private static final class LoanMapper implements
			RowMapper<LoanBasicDetailsData> {

		public String loanSchema() {
			return "l.id as id, l.external_id as externalId, l.fund_id as fundId, f.name as fundName, " 
					+ " lp.id as loanProductId, lp.name as loanProductName, lp.description as loanProductDescription, c.id as clientId, c.display_name as clientName, " 
					+ " c.office_id as clientOfficeId,l.submittedon_date as submittedOnDate,"
					+ " l.approvedon_date as approvedOnDate, l.expected_disbursedon_date as expectedDisbursementDate, l.disbursedon_date as actualDisbursementDate, l.expected_firstrepaymenton_date as expectedFirstRepaymentOnDate,"
					+ " l.interest_calculated_from_date as interestChargedFromDate, l.closedon_date as closedOnDate, l.expected_maturedon_date as expectedMaturityDate, "
					+ " l.principal_amount as principal, l.arrearstolerance_amount as inArrearsTolerance, l.number_of_repayments as numberOfRepayments, l.repay_every as repaymentEvery,"
					+ " l.nominal_interest_rate_per_period as interestRatePerPeriod, l.annual_nominal_interest_rate as annualInterestRate, "
					+ " l.repayment_period_frequency_enum as repaymentFrequencyType, l.interest_period_frequency_enum as interestRateFrequencyType, "
					+ " l.term_frequency as termFrequency, l.term_period_frequency_enum as termPeriodFrequencyType, "
					+ " l.amortization_method_enum as amortizationType, l.interest_method_enum as interestType, l.interest_calculated_in_period_enum as interestCalculationPeriodType,"
					+ " l.loan_status_id as lifeCycleStatusId, l.loan_transaction_strategy_id as transactionStrategyId, "
					+ " l.currency_code as currencyCode, l.currency_digits as currencyDigits, rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
					+ " l.loan_officer_id as loanOfficerId, s.display_name as loanOfficerName"
					+ " from m_loan l"
					+ " join m_client c on c.id = l.client_id"
					+ " join m_product_loan lp on lp.id = l.product_id"
					+ " join m_currency rc on rc.`code` = l.currency_code"
					+ " left join m_fund f on f.id = l.fund_id"
					+ " left join m_staff s on s.id = l.loan_officer_id";
		}

		@Override
		public LoanBasicDetailsData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			String currencyCode = rs.getString("currencyCode");
			String currencyName = rs.getString("currencyName");
			String currencyNameCode = rs.getString("currencyNameCode");
			String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
			Integer currencyDigits = JdbcSupport.getInteger(rs,"currencyDigits");
			CurrencyData currencyData = new CurrencyData(currencyCode,
					currencyName, currencyDigits, currencyDisplaySymbol,
					currencyNameCode);

			Long id = rs.getLong("id");
			String externalId = rs.getString("externalId");
			Long clientId = JdbcSupport.getLong(rs, "clientId");
			Long clientOfficeId = JdbcSupport.getLong(rs, "clientOfficeId");
			String clientName = rs.getString("clientName");
			Long fundId = JdbcSupport.getLong(rs, "fundId");
			String fundName = rs.getString("fundName");
			Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
			String loanOfficerName = rs.getString("loanOfficerName");
			Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
			String loanProductName = rs.getString("loanProductName");
			String loanProductDescription = rs.getString("loanProductDescription");
			
			LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs,
					"submittedOnDate");
			LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs,
					"approvedOnDate");
			LocalDate expectedDisbursementDate = JdbcSupport.getLocalDate(rs,
					"expectedDisbursementDate");
			LocalDate actualDisbursementDate = JdbcSupport.getLocalDate(rs,
					"actualDisbursementDate");
			LocalDate expectedFirstRepaymentOnDate = JdbcSupport.getLocalDate(
					rs, "expectedFirstRepaymentOnDate");
			LocalDate interestChargedFromDate = JdbcSupport.getLocalDate(rs,
					"interestChargedFromDate");
			LocalDate closedOnDate = JdbcSupport.getLocalDate(rs,
					"closedOnDate");
			LocalDate expectedMaturityDate = JdbcSupport.getLocalDate(rs,
					"expectedMaturityDate");

			BigDecimal principal = rs.getBigDecimal("principal");
			BigDecimal inArrearsTolerance = rs.getBigDecimal("inArrearsTolerance");

			Integer numberOfRepayments = JdbcSupport.getInteger(rs,"numberOfRepayments");
			Integer repaymentEvery = JdbcSupport.getInteger(rs,"repaymentEvery");
			BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
			BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

			Integer termFrequency = JdbcSupport.getInteger(rs,"termFrequency");
			Integer termPeriodFrequencyTypeInt = JdbcSupport.getInteger(rs,"termPeriodFrequencyType");
			EnumOptionData termPeriodFrequencyType = LoanEnumerations.termFrequencyType(termPeriodFrequencyTypeInt);
			
			int repaymentFrequencyTypeInt = JdbcSupport.getInteger(rs,"repaymentFrequencyType");
			EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(repaymentFrequencyTypeInt);
			
			int interestRateFrequencyTypeInt = JdbcSupport.getInteger(rs,"interestRateFrequencyType");
			EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(interestRateFrequencyTypeInt);

			Integer transactionStrategyId = JdbcSupport.getInteger(rs,"transactionStrategyId");
			
			int amortizationTypeInt = JdbcSupport.getInteger(rs,"amortizationType");
			int interestTypeInt = JdbcSupport.getInteger(rs, "interestType");
			int interestCalculationPeriodTypeInt = JdbcSupport.getInteger(rs,"interestCalculationPeriodType");
			
			EnumOptionData amortizationType = LoanEnumerations.amortizationType(amortizationTypeInt);
			EnumOptionData interestType = LoanEnumerations.interestType(interestTypeInt);
			EnumOptionData interestCalculationPeriodType = LoanEnumerations.interestCalculationPeriodType(interestCalculationPeriodTypeInt);

			Integer lifeCycleStatusId = JdbcSupport.getInteger(rs, "lifeCycleStatusId");
			EnumOptionData status = LoanEnumerations.status(lifeCycleStatusId);
			
			LocalDate lifeCycleStatusDate = submittedOnDate;
			if (approvedOnDate != null) {
				lifeCycleStatusDate = approvedOnDate;
			}
			if (actualDisbursementDate != null) {
				lifeCycleStatusDate = actualDisbursementDate;
			}
			if (closedOnDate != null) {
				lifeCycleStatusDate = closedOnDate;
			}

			Collection<ChargeData> charges = null;
			return new LoanBasicDetailsData(id, externalId, clientId, clientName, clientOfficeId,
					loanProductId, loanProductName, loanProductDescription,
					fundId, fundName, 
					closedOnDate,
					submittedOnDate, approvedOnDate, expectedDisbursementDate,
					actualDisbursementDate, expectedMaturityDate,
					expectedFirstRepaymentOnDate, interestChargedFromDate,
					currencyData,
					principal, inArrearsTolerance, numberOfRepayments,
					repaymentEvery, interestRatePerPeriod, annualInterestRate,
					repaymentFrequencyType, interestRateFrequencyType,
					amortizationType, interestType,
					interestCalculationPeriodType, 
					status,
					lifeCycleStatusDate, termFrequency, termPeriodFrequencyType, transactionStrategyId, charges,
					loanOfficerId, loanOfficerName);
		}
	}

	private static final class LoanScheduleMapper implements
			RowMapper<LoanRepaymentPeriodData> {

		public String loanScheduleSchema() {

			return " ls.loan_id as loanId, ls.installment as period, ls.duedate as `date`, "
					+ " ls.principal_amount as principal, ls.principal_completed_derived as principalPaid, "
					+ " ls.interest_amount as interest, ls.interest_completed_derived as interestPaid, ls.interest_waived_derived as interestWaived "
					+ " from m_loan l "
					+ " join m_loan_repayment_schedule ls on ls.loan_id = l.id ";
		}

		@Override
		public LoanRepaymentPeriodData mapRow(final ResultSet rs,
				final int rowNum) throws SQLException {

			Long loanId = rs.getLong("loanId");
			Integer period = JdbcSupport.getInteger(rs, "period");
			LocalDate date = JdbcSupport.getLocalDate(rs, "date");

			BigDecimal principal = rs.getBigDecimal("principal");
			BigDecimal principalPaid = rs.getBigDecimal("principalPaid");
			BigDecimal principalOutstanding = principal.subtract(principalPaid);

			BigDecimal interest = rs.getBigDecimal("interest");
			BigDecimal interestPaid = rs.getBigDecimal("interestPaid");
			BigDecimal interestWaived = rs.getBigDecimal("interestWaived");
			if (interestWaived == null) {
				interestWaived = BigDecimal.ZERO;
			}
			BigDecimal interestOutstanding = interest.subtract(interestPaid).subtract(interestWaived);

			BigDecimal total = principal.add(interest);
			BigDecimal totalPaid = principalPaid.add(interestPaid);
			BigDecimal totalWaived = interestWaived;
			BigDecimal totalOutstanding = principalOutstanding.add(interestOutstanding);

			return new LoanRepaymentPeriodData(loanId, period, date,
					principal, principalPaid, principalOutstanding, interest,
					interestPaid, interestWaived, interestOutstanding, total, totalPaid, totalWaived,
					totalOutstanding);
		}
	}

	private static final class LoanPaymentsMapper implements
			RowMapper<LoanRepaymentTransactionData> {

		public String LoanPaymentsSchema() {

			return " tr.id as id, tr.transaction_type_enum as transactionType, tr.transaction_date as `date`, tr.amount as total, "
					+ " l.currency_code as currencyCode, l.currency_digits as currencyDigits, rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode "
					+ " from m_loan l "
					+ " join m_loan_transaction tr on tr.loan_id = l.id"
					+ " join m_currency rc on rc.`code` = l.currency_code ";
		}

		@Override
		public LoanRepaymentTransactionData mapRow(final ResultSet rs,
				final int rowNum) throws SQLException {

			String currencyCode = rs.getString("currencyCode");
			String currencyName = rs.getString("currencyName");
			String currencyNameCode = rs.getString("currencyNameCode");
			String currencyDisplaySymbol = rs
					.getString("currencyDisplaySymbol");
			Integer currencyDigits = JdbcSupport.getInteger(rs,
					"currencyDigits");
			CurrencyData currencyData = new CurrencyData(currencyCode,
					currencyName, currencyDigits, currencyDisplaySymbol,
					currencyNameCode);

			Long id = rs.getLong("id");
			int transactionTypeInt = JdbcSupport.getInteger(rs,
					"transactionType");
			EnumOptionData transactionType = LoanEnumerations
					.transactionType(transactionTypeInt);
			LocalDate date = JdbcSupport.getLocalDate(rs, "date");
			BigDecimal totalBD = rs.getBigDecimal("total");
			MoneyData total = MoneyData.of(currencyData, totalBD);

			return new LoanRepaymentTransactionData(id, transactionType, date, total);
		}
	}

}