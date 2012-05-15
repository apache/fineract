package org.mifosng.platform;

import static org.mifosng.platform.Specifications.loanTransactionsThatMatch;
import static org.mifosng.platform.Specifications.loansThatMatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.joda.time.LocalDate;
import org.mifosng.data.ClientData;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.DerivedLoanData;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.MoneyData;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.OrganisationReadModel;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.service.AppUserReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ReadPlatformServiceImpl implements ReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final ClientRepository clientRepository;
	private final LoanRepository loanRepository;
	private final LoanTransactionRepository loanTransactionRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;
	private final LoanProductReadPlatformService loanProductReadPlatformService;
	private final AppUserReadPlatformService appUserReadPlatformService;
	private final ClientReadPlatformService clientReadPlatformService;

	@Autowired
	public ReadPlatformServiceImpl(
			final LoanProductReadPlatformService loanProductReadPlatformService,
			final AppUserReadPlatformService appUserReadPlatformService,
			final ClientReadPlatformService clientReadPlatformService,
			final DataSource dataSource,
			final ClientRepository clientRepository,
			final LoanRepository loanRepository,
			final LoanTransactionRepository loanTransactionRepository,
			final ApplicationCurrencyRepository applicationCurrencyRepository) {
		this.loanProductReadPlatformService = loanProductReadPlatformService;
		this.appUserReadPlatformService = appUserReadPlatformService;
		this.clientReadPlatformService = clientReadPlatformService;
		this.loanTransactionRepository = loanTransactionRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
		this.clientRepository = clientRepository;
		this.loanRepository = loanRepository;
	}

	private AppUser extractAuthenticatedUser() {
		AppUser currentUser = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			Authentication auth = context.getAuthentication();
			if (auth != null) {
				currentUser = (AppUser) auth.getPrincipal();
			}
		}

		if (currentUser == null) {
			throw new UnAuthenticatedUserException();
		}

		return currentUser;
	}

	@Override
	public Collection<OrganisationReadModel> retrieveAll() {

		String sql = "select o.id as id, o.name as name, o.contact_name as contactName, o.opening_date as openingDate from org_organisation o";

		RowMapper<OrganisationReadModel> rm = new OrganisationMapper();

		return this.jdbcTemplate.query(sql, rm);
	}

	protected static final class OrganisationMapper implements
			RowMapper<OrganisationReadModel> {

		@Override
		public OrganisationReadModel mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String contactName = rs.getString("contactName");
			LocalDate openingDate = new LocalDate(rs.getDate("openingDate"));

			return new OrganisationReadModel(id, name, contactName, openingDate);
		}
	}

	@Override
	public LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId) {

		AppUser currentUser = extractAuthenticatedUser();

		// TODO - OPTIMIZE - write simple sql query to fetch back date of
		// possible next transaction date.
		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));

		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(loan.getLoanRepaymentScheduleDetail()
						.getPrincipal().getCurrencyCode());

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());

		LocalDate earliestUnpaidInstallmentDate = loan
				.possibleNextRepaymentDate();
		Money possibleNextRepaymentAmount = loan.possibleNextRepaymentAmount();
		MoneyData possibleNextRepayment = MoneyData.of(currencyData,
				possibleNextRepaymentAmount.getAmount());

		LoanRepaymentData newRepaymentDetails = new LoanRepaymentData();
		newRepaymentDetails.setDate(earliestUnpaidInstallmentDate);
		newRepaymentDetails.setTotal(possibleNextRepayment);

		return newRepaymentDetails;
	}

	@Override
	public LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId) {

		AppUser currentUser = extractAuthenticatedUser();

		// TODO - OPTIMIZE - write simple sql query to fetch back date of
		// possible next transaction date.
		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));

		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(loan.getLoanRepaymentScheduleDetail()
						.getPrincipal().getCurrencyCode());

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());

		Money totalOutstanding = loan.getTotalOutstanding();
		MoneyData totalOutstandingData = MoneyData.of(currencyData,
				totalOutstanding.getAmount());

		LoanRepaymentData newWaiverDetails = new LoanRepaymentData();
		newWaiverDetails.setDate(new LocalDate());
		newWaiverDetails.setTotal(totalOutstandingData);

		return newWaiverDetails;
	}

	@Override
	public LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId,
			Long repaymentId) {

		AppUser currentUser = extractAuthenticatedUser();

		LoanTransaction transaction = this.loanTransactionRepository
				.findOne(loanTransactionsThatMatch(
						currentUser.getOrganisation(), repaymentId));

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));

		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(loan.getLoanRepaymentScheduleDetail()
						.getPrincipal().getCurrencyCode());

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());
		MoneyData total = MoneyData.of(currencyData, transaction.getAmount());
		LocalDate date = transaction.getTransactionDate();

		LoanRepaymentData loanRepaymentData = new LoanRepaymentData();
		loanRepaymentData.setId(repaymentId);
		loanRepaymentData.setTotal(total);
		loanRepaymentData.setDate(date);

		return loanRepaymentData;
	}

	@Override
	public NewLoanWorkflowStepOneData retrieveClientAndProductDetails(
			final Long clientId, final Long productId) {

		AppUser currentUser = extractAuthenticatedUser();

		NewLoanWorkflowStepOneData workflowData = new NewLoanWorkflowStepOneData();
		workflowData.setOrganisationId(currentUser.getOrganisation().getId());
		workflowData.setOrganisationName(currentUser.getOrganisation()
				.getName());

		Collection<LoanProductData> loanProducts = this.loanProductReadPlatformService
				.retrieveAllLoanProducts();
		workflowData.setAllowedProducts(new ArrayList<LoanProductData>(
				loanProducts));

		if (loanProducts.size() == 1) {
			LoanProductData selectedProduct = this.loanProductReadPlatformService
					.retrieveLoanProduct(workflowData.getAllowedProducts()
							.get(0).getId());

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

		ClientData clientAccount = this.clientReadPlatformService.retrieveIndividualClient(clientId);
		workflowData.setClientId(clientAccount.getId());
		workflowData.setClientName(clientAccount.getDisplayName());

		return workflowData;
	}

	private LoanProductData findLoanProductById(
			Collection<LoanProductData> loanProducts, Long productId) {
		LoanProductData match = this.loanProductReadPlatformService
				.retrieveNewLoanProductDetails();
		for (LoanProductData loanProductData : loanProducts) {
			if (loanProductData.getId().equals(productId)) {
				match = this.loanProductReadPlatformService
						.retrieveLoanProduct(loanProductData.getId());
				break;
			}
		}
		return match;
	}

	@Override
	public LoanAccountData retrieveLoanAccountDetails(Long loanId) {

		// TODO - OPTIMISE - prefer jdbc sql approach to return only what we
		// need of loan information.
		AppUser currentUser = extractAuthenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));

		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(loan.getLoanRepaymentScheduleDetail()
						.getPrincipal().getCurrencyCode());

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());

		LoanAccountData loanData = convertToData(loan, currencyData);

		return loanData;
	}

	private LoanAccountData convertToData(final Loan realLoan,
			CurrencyData currencyData) {

		DerivedLoanData loanData = realLoan.deriveLoanData(currencyData);

		LocalDate expectedDisbursementDate = null;
		if (realLoan.getExpectedDisbursedOnDate() != null) {
			expectedDisbursementDate = new LocalDate(
					realLoan.getExpectedDisbursedOnDate());
		}

		Money loanPrincipal = realLoan.getLoanRepaymentScheduleDetail()
				.getPrincipal();
		MoneyData principal = MoneyData.of(currencyData,
				loanPrincipal.getAmount());

		Money loanArrearsTolerance = realLoan.getInArrearsTolerance();
		MoneyData tolerance = MoneyData.of(currencyData,
				loanArrearsTolerance.getAmount());

		Money interestRebate = realLoan.getInterestRebateOwed();
		MoneyData interestRebateOwed = MoneyData.of(currencyData,
				interestRebate.getAmount());

		boolean interestRebateOutstanding = false; // realLoan.isInterestRebateOutstanding(),

		// permissions
		boolean waiveAllowed = loanData.getSummary().isWaiveAllowed(tolerance)
				&& realLoan.isNotClosed();
		boolean undoDisbursalAllowed = realLoan.isDisbursed()
				&& realLoan.isOpenWithNoRepaymentMade();
		boolean makeRepaymentAllowed = realLoan.isDisbursed()
				&& realLoan.isNotClosed();

		LocalDate loanStatusDate = realLoan.getLoanStatusSinceDate();

		boolean rejectAllowed = realLoan.isNotApproved()
				&& realLoan.isNotDisbursed() && realLoan.isNotClosed();
		boolean withdrawnByApplicantAllowed = realLoan.isNotDisbursed()
				&& realLoan.isNotClosed();
		boolean undoApprovalAllowed = realLoan.isApproved()
				&& realLoan.isNotClosed();
		boolean disbursalAllowed = realLoan.isApproved()
				&& realLoan.isNotDisbursed() && realLoan.isNotClosed();

		return new LoanAccountData(realLoan.isClosed(), realLoan.isOpen(),
				realLoan.isOpenWithRepaymentMade(), interestRebateOutstanding,
				realLoan.isSubmittedAndPendingApproval(),
				realLoan.isWaitingForDisbursal(), undoDisbursalAllowed,
				makeRepaymentAllowed, rejectAllowed,
				withdrawnByApplicantAllowed, undoApprovalAllowed,
				disbursalAllowed, realLoan.getLoanStatusDisplayName(),
				loanStatusDate, realLoan.getId(), realLoan.getExternalId(),
				realLoan.getLoanProduct().getName(),
				realLoan.getClosedOnDate(), realLoan.getSubmittedOnDate(),
				realLoan.getApprovedOnDate(), expectedDisbursementDate,
				realLoan.getDisbursedOnDate(),
				realLoan.getExpectedMaturityDate(),
				realLoan.getExpectedFirstRepaymentOnDate(),
				realLoan.getInterestCalculatedFromDate(), principal, realLoan
						.getLoanRepaymentScheduleDetail()
						.getAnnualNominalInterestRate(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getNominalInterestRatePerPeriod(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getInterestPeriodFrequencyType().getValue(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getInterestPeriodFrequencyType().toString(), realLoan
						.getLoanRepaymentScheduleDetail().getInterestMethod()
						.getValue(), realLoan.getLoanRepaymentScheduleDetail()
						.getInterestMethod().toString(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getAmortizationMethod().getValue(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getAmortizationMethod().toString(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getNumberOfRepayments(), realLoan
						.getLoanRepaymentScheduleDetail().getRepayEvery(),
				realLoan.getLoanRepaymentScheduleDetail()
						.getRepaymentPeriodFrequencyType().getValue(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getRepaymentPeriodFrequencyType().toString(),
				tolerance, loanData, waiveAllowed, interestRebateOwed);
	}
}