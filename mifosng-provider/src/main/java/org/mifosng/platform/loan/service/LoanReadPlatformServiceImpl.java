package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.DerivedLoanData;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.data.LoanAccountData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.data.LoanProductLookup;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.NewLoanData;
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

		LoanBasicDetailsData selectedLoan = null;
		try {
			context.authenticatedUser();

			LoanMapper rm = new LoanMapper();
			String sql = "select " + rm.loanSchema() + " where l.id = ?";

			selectedLoan = this.jdbcTemplate.queryForObject(sql, rm,
					new Object[] { loanId });

		} catch (EmptyResultDataAccessException e) {
			throw new LoanNotFoundException(loanId);
		}

		return selectedLoan;
	}
//jpw delete after
	public LoanAccountData convertToData(LoanBasicDetailsData loanBasic) {

		MoneyData princ = loanBasic.getPrincipal();
		
		CurrencyData currencyData = new CurrencyData(princ.getCurrencyCode(),
				princ.getDefaultName(), princ.getDigitsAfterDecimal(), princ.getDisplaySymbol(),
				princ.getNameCode());
		
		final Loan realLoan = this.loanRepository.findOne(loanBasic.getId());
		if (realLoan == null) {
			throw new LoanNotFoundException(loanBasic.getId());
		}
		DerivedLoanData loanData = realLoan.deriveLoanData(currencyData);

		return realLoan.toLoanAccountData(loanBasic, loanData.getSummary(),
				loanData.getRepaymentSchedule(), loanData.getLoanRepayments());
	}

	@Override
	public NewLoanData retrieveClientAndProductDetails(final Long clientId,
			final Long productId) {

		context.authenticatedUser();

		NewLoanData workflowData = new NewLoanData();

		Collection<LoanProductLookup> loanProducts = this.loanProductReadPlatformService
				.retrieveAllLoanProductsForLookup();
		workflowData.setAllowedProducts(new ArrayList<LoanProductLookup>(
				loanProducts));

		if (loanProducts.size() == 1) {
			Long allowedProductId = workflowData.getAllowedProducts().get(0)
					.getId();
			LoanProductData selectedProduct = this.loanProductReadPlatformService
					.retrieveLoanProduct(allowedProductId);

			workflowData.setProductId(selectedProduct.getId());
			workflowData.setProductName(selectedProduct.getName());
			workflowData.setSelectedProduct(selectedProduct);
		} else {
			LoanProductData selectedProduct = findLoanProductById(loanProducts,
					productId);

			workflowData.setProductId(selectedProduct.getId());
			workflowData.setProductName(selectedProduct.getName());
			workflowData.setSelectedProduct(selectedProduct);
		}

		ClientData clientAccount = this.clientReadPlatformService
				.retrieveIndividualClient(clientId);
		workflowData.setClientId(clientAccount.getId());
		workflowData.setClientName(clientAccount.getDisplayName());

		workflowData.setExpectedDisbursementDate(new LocalDate());

		return workflowData;
	}

	private LoanProductData findLoanProductById(
			Collection<LoanProductLookup> loanProducts, Long productId) {
		LoanProductData match = this.loanProductReadPlatformService
				.retrieveNewLoanProductDetails();
		for (LoanProductLookup loanProductLookup : loanProducts) {
			if (loanProductLookup.getId().equals(productId)) {
				match = this.loanProductReadPlatformService
						.retrieveLoanProduct(loanProductLookup.getId());
				break;
			}
		}
		return match;
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

		final String currencyCode = loan.getLoanRepaymentScheduleDetail()
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

		final String currencyCode = loan.getLoanRepaymentScheduleDetail()
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

		final String currencyCode = loan.getLoanRepaymentScheduleDetail()
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
			return "l.id as id, l.external_id as externalId, l.fund_id as fundId, f.name as fundName, lp.id as loanProductId, lp.name as loanProductName, l.submittedon_date as submittedOnDate,"
					+ " l.approvedon_date as approvedOnDate, l.expected_disbursedon_date as expectedDisbursementDate, l.disbursedon_date as actualDisbursementDate, l.expected_firstrepaymenton_date as expectedFirstRepaymentOnDate,"
					+ " l.interest_calculated_from_date as interestChargedFromDate, l.closedon_date as closedOnDate, l.expected_maturedon_date as expectedMaturityDate, "
					+ " l.principal_amount as principal, l.arrearstolerance_amount as inArrearsTolerance, l.number_of_repayments as numberOfRepayments, l.repay_every as repaymentEvery,"
					+ " l.nominal_interest_rate_per_period as interestRatePerPeriod, l.annual_nominal_interest_rate as annualInterestRate, "
					+ " l.repayment_period_frequency_enum as repaymentFrequencyType, l.interest_period_frequency_enum as interestRateFrequencyType, "
					+ " l.amortization_method_enum as amortizationType, l.interest_method_enum as interestType, l.interest_calculated_in_period_enum as interestCalculationPeriodType,"
					+ " l.loan_status_id as lifeCycleStatusId, st.display_name as lifeCycleStatusText, "
					+ " l.currency_code as currencyCode, l.currency_digits as currencyDigits, rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode"
					+ " from portfolio_loan l"
					+ " join portfolio_product_loan lp on lp.id = l.product_id"
					+ " join ref_currency rc on rc.`code` = l.currency_code"
					+ " join ref_loan_status st on st.id = l.loan_status_id"
					+ " left join org_fund f on f.id = l.fund_id";
		}

		@Override
		public LoanBasicDetailsData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

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
			String externalId = rs.getString("externalId");
			Long fundId = JdbcSupport.getLong(rs, "fundId");
			String fundName = rs.getString("fundName");
			Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
			String loanProductName = rs.getString("loanProductName");
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

			BigDecimal principalBD = rs.getBigDecimal("principal");
			MoneyData principal = MoneyData.of(currencyData, principalBD);
			BigDecimal inArrearsToleranceBD = rs
					.getBigDecimal("inArrearsTolerance");
			MoneyData inArrearsTolerance = MoneyData.of(currencyData,
					inArrearsToleranceBD);

			Integer numberOfRepayments = JdbcSupport.getInteger(rs,
					"numberOfRepayments");
			Integer repaymentEvery = JdbcSupport.getInteger(rs,
					"repaymentEvery");
			BigDecimal interestRatePerPeriod = rs
					.getBigDecimal("interestRatePerPeriod");
			BigDecimal annualInterestRate = rs
					.getBigDecimal("annualInterestRate");

			int repaymentFrequencyTypeInt = JdbcSupport.getInteger(rs,
					"repaymentFrequencyType");
			int interestRateFrequencyTypeInt = JdbcSupport.getInteger(rs,
					"interestRateFrequencyType");
			int amortizationTypeInt = JdbcSupport.getInteger(rs,
					"amortizationType");
			int interestTypeInt = JdbcSupport.getInteger(rs, "interestType");
			int interestCalculationPeriodTypeInt = JdbcSupport.getInteger(rs,
					"interestCalculationPeriodType");
			EnumOptionData repaymentFrequencyType = LoanEnumerations
					.repaymentFrequencyType(repaymentFrequencyTypeInt);
			EnumOptionData interestRateFrequencyType = LoanEnumerations
					.interestRateFrequencyType(interestRateFrequencyTypeInt);
			EnumOptionData amortizationType = LoanEnumerations
					.amortizationType(amortizationTypeInt);
			EnumOptionData interestType = LoanEnumerations
					.interestType(interestTypeInt);
			EnumOptionData interestCalculationPeriodType = LoanEnumerations
					.interestCalculationPeriodType(interestCalculationPeriodTypeInt);

			Integer lifeCycleStatusId = JdbcSupport.getInteger(rs,
					"lifeCycleStatusId");
			String lifeCycleStatusText = rs.getString("lifeCycleStatusText");

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

			return new LoanBasicDetailsData(id, externalId, loanProductId,
					loanProductName, fundId, fundName, closedOnDate,
					submittedOnDate, approvedOnDate, expectedDisbursementDate,
					actualDisbursementDate, expectedMaturityDate,
					expectedFirstRepaymentOnDate, interestChargedFromDate,
					principal, inArrearsTolerance, numberOfRepayments,
					repaymentEvery, interestRatePerPeriod, annualInterestRate,
					repaymentFrequencyType, interestRateFrequencyType,
					amortizationType, interestType,
					interestCalculationPeriodType, lifeCycleStatusId,
					lifeCycleStatusText, lifeCycleStatusDate);
		}
	}

}