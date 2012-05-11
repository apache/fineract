package org.mifosng.platform;

import static org.mifosng.platform.Specifications.clientsThatMatch;
import static org.mifosng.platform.Specifications.loanTransactionsThatMatch;
import static org.mifosng.platform.Specifications.loansThatMatch;
import static org.mifosng.platform.Specifications.usersThatMatch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosng.data.AppUserData;
import org.mifosng.data.ClientData;
import org.mifosng.data.ClientDataWithAccountsData;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.DerivedLoanData;
import org.mifosng.data.EnumOptionReadModel;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanProductData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.MoneyData;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.NoteData;
import org.mifosng.data.OfficeData;
import org.mifosng.data.OrganisationReadModel;
import org.mifosng.data.PermissionData;
import org.mifosng.data.RoleData;
import org.mifosng.data.reports.GenericResultset;
import org.mifosng.data.reports.ResultsetColumnHeader;
import org.mifosng.data.reports.ResultsetDataRow;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.mifosng.platform.user.domain.PermissionGroup;
import org.mifosng.platform.user.domain.Role;
import org.mifosng.platform.user.domain.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ReadPlatformServiceImpl implements ReadPlatformService {

	private final static Logger logger = LoggerFactory
			.getLogger(ReadPlatformServiceImpl.class);

	private final SimpleJdbcTemplate jdbcTemplate;
	private final ClientRepository clientRepository;
	private final LoanRepository loanRepository;
	private final LoanTransactionRepository loanTransactionRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;
	private final DataSource dataSource;
	private final AppUserRepository appUserRepository;
	private final RoleRepository roleRepository;

	private final LoanProductReadPlatformService loanProductReadPlatformService;

	@Autowired
	public ReadPlatformServiceImpl(final LoanProductReadPlatformService loanProductReadPlatformService, 
			final DataSource dataSource,
			final ClientRepository clientRepository,
			final LoanRepository loanRepository,
			final LoanTransactionRepository loanTransactionRepository,
			final ApplicationCurrencyRepository applicationCurrencyRepository,
			final AppUserRepository appUserRepository,
			final RoleRepository roleRepository) {
		this.loanProductReadPlatformService = loanProductReadPlatformService;
		this.loanTransactionRepository = loanTransactionRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.appUserRepository = appUserRepository;
		this.roleRepository = roleRepository;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
		this.clientRepository = clientRepository;
		this.loanRepository = loanRepository;
		this.dataSource = dataSource;
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
	public Collection<ClientData> retrieveAllIndividualClients() {

		AppUser currentUser = extractAuthenticatedUser();
		
		List<OfficeData> offices = retrieveOffices();
		String officeIdsList = generateOfficeIdInClause(offices);
		ClientMapper rm = new ClientMapper(offices, currentUser.getOrganisation());

		String sql = "select "
				+ rm.clientSchema()
				+ " where c.org_id = ? and c.office_id in (" + officeIdsList + ") order by c.lastname ASC, c.firstname ASC";

		return this.jdbcTemplate.query(sql, rm, new Object[] { currentUser.getOrganisation().getId()});
	}

	private String generateOfficeIdInClause(List<OfficeData> offices) {
		String officeIdsList = "";
		for (int i=0; i < offices.size(); i++) {
			Long id = offices.get(i).getId();
			if (i==0) {
				officeIdsList = id.toString();
			} else {
				officeIdsList += "," + id.toString();
			}
		}
		return officeIdsList;
	}

	@Override
	public NoteData retrieveClientNote(Long clientId, Long noteId) {

		try {
			AppUser currentUser = extractAuthenticatedUser();

			Collection<AppUserData> allUsers = retrieveAllUsers();

			NoteMapper noteMapper = new NoteMapper(allUsers);

			String sql = "select " + noteMapper.schema()
					+ " where n.org_id = ? and n.client_id = ? and n.id = ?";

			return this.jdbcTemplate.queryForObject(sql, noteMapper,
					new Object[] { currentUser.getOrganisation().getId(),
							clientId, noteId });
		} catch (EmptyResultDataAccessException e) {
			throw new PlatformResourceNotFoundException("error.msg.client.id.invalid", "Client with identifier {0} does not exist", clientId);
		}
	}

	@Override
	public Collection<NoteData> retrieveAllClientNotes(Long clientId) {

		AppUser currentUser = extractAuthenticatedUser();

		Collection<AppUserData> allUsers = retrieveAllUsers();

		NoteMapper noteMapper = new NoteMapper(allUsers);

		String sql = "select "
				+ noteMapper.schema()
				+ " where n.org_id = ? and n.client_id = ? order by n.created_date DESC";

		return this.jdbcTemplate.query(sql, noteMapper, new Object[] {
				currentUser.getOrganisation().getId(), clientId });
	}

	protected static final class NoteMapper implements RowMapper<NoteData> {

		private final Collection<AppUserData> allUsers;

		public NoteMapper(Collection<AppUserData> allUsers) {
			this.allUsers = allUsers;
		}

		public String schema() {
			return "n.id as id, n.client_id as clientId, n.loan_id as loanId, n.loan_transaction_id as transactionId, n.note_type_enum as noteTypeEnum, n.note as note, "
					+ "n.created_date as createdDate, n.createdby_id as createdById, n.lastmodified_date as lastModifiedDate, n.lastmodifiedby_id as lastModifiedById"
					+ " from portfolio_note n";
		}

		@Override
		public NoteData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			Long clientId = rs.getLong("clientId");
			Long loanId = rs.getLong("loanId");
			Long transactionId = rs.getLong("transactionId");
			Integer noteTypeId = rs.getInt("noteTypeEnum");
			String note = rs.getString("note");

			DateTime createdDate = new DateTime(rs.getTimestamp("createdDate"));
			Long createdById = rs.getLong("createdById");
			String createdByUsername = findUserById(createdById, allUsers);

			DateTime lastModifiedDate = new DateTime(
					rs.getTimestamp("lastModifiedDate"));
			Long lastModifiedById = rs.getLong("lastModifiedById");
			String updatedByUsername = findUserById(createdById, allUsers);

			return new NoteData(id, clientId, loanId, transactionId,
					noteTypeId, note, createdDate, createdById,
					createdByUsername, lastModifiedDate, lastModifiedById,
					updatedByUsername);
		}

		private String findUserById(Long createdById,
				Collection<AppUserData> allUsers) {
			String username = "";
			for (AppUserData appUserData : allUsers) {
				if (appUserData.getId().equals(createdById)) {
					username = appUserData.getUsername();
					break;
				}
			}
			return username;
		}
	}

	protected static final class ClientMapper implements RowMapper<ClientData> {

		private final List<OfficeData> offices;
		private final Organisation organisation;

		public ClientMapper(final List<OfficeData> offices, Organisation organisation) {
			this.offices = offices;
			this.organisation = organisation;
		}

		public String clientSchema() {
			return "c.org_id as orgId, c.office_id as officeId, c.id as id, c.firstname as firstname, c.lastname as lastname, c.external_id as externalId, c.joining_date as joinedDate from portfolio_client c";
		}

		@Override
		public ClientData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long orgId = rs.getLong("orgId");
			Long officeId = rs.getLong("officeId");
			Long id = rs.getLong("id");
			String firstname = rs.getString("firstname");
			if (StringUtils.isBlank(firstname)) {
				firstname = "";
			}
			String lastname = rs.getString("lastname");
			String externalId = rs.getString("externalId");
			LocalDate joinedDate = new LocalDate(rs.getDate("joinedDate"));

			String officeName = fromOfficeList(this.offices, officeId);
			
			String orgname = "";
			if (organisation.getId().equals(orgId)) {
				orgname = organisation.getName();
			}

			return new ClientData(orgId, orgname, officeId, officeName, id, firstname, lastname, externalId, joinedDate);
		}

		private String fromOfficeList(final List<OfficeData> officeList,
				final Long officeId) {
			String match = "";
			for (OfficeData office : officeList) {
				if (office.getId().equals(officeId)) {
					match = office.getName();
				}
			}

			return match;
		}
	}

	private List<OfficeData> retrieveOffices() {

		AppUser currentUser = extractAuthenticatedUser();
		
		String hierarchy = currentUser.getOffice().getHierarchy();
		String hierarchySearchString = hierarchy + "%";
		
		OfficeMapper rm = new OfficeMapper();
		String sql = "select " + rm.officeSchema() + "where o.org_id = ? and o.hierarchy like ? order by o.hierarchy";

		return this.jdbcTemplate.query(sql, rm, new Object[] {currentUser.getOrganisation().getId(), hierarchySearchString});
	}

	protected static final class OfficeMapper implements RowMapper<OfficeData> {

		public String officeSchema() {
			return " o.id as id, o.name as name, o.external_id as externalId, o.opening_date as openingDate, o.hierarchy as hierarchy, parent.id as parentId, parent.name as parentName "
					+ "from org_office o LEFT JOIN org_office AS parent ON parent.id = o.parent_id ";
		}

		@Override
		public OfficeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String externalId = rs.getString("externalId");
			LocalDate openingDate = new LocalDate(rs.getDate("openingDate"));
			String hierarchy = rs.getString("hierarchy");
			Long parentId = rs.getLong("parentId");
			String parentName = rs.getString("parentName");

			return new OfficeData(id, name, externalId, openingDate, hierarchy, parentId, parentName);
		}
	}

	@Override
	public ClientData retrieveIndividualClient(final Long clientId) {

		try {
			AppUser currentUser = extractAuthenticatedUser();

			List<OfficeData> offices = retrieveOffices();
			ClientMapper rm = new ClientMapper(offices, currentUser.getOrganisation());

			String sql = "select " + rm.clientSchema()
					+ " where c.id = ? and c.org_id = ?";

			return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {
					clientId, currentUser.getOrganisation().getId() });
		} catch (EmptyResultDataAccessException e) {
			throw new PlatformResourceNotFoundException("error.msg.client.id.invalid", "Client with identifier {0} does not exist", clientId);
		}
	}

	@Override
	public ClientDataWithAccountsData retrieveClientAccountDetails(
			final Long clientId) {

		AppUser currentUser = extractAuthenticatedUser();

		ClientData clientAccount = retrieveIndividualClient(clientId);

		// TODO - rewrite using jdbc sql approach
		Client client = this.clientRepository.findOne(clientsThatMatch(
				currentUser.getOrganisation(), clientId));

		Collection<Loan> allLoans = this.loanRepository.findAll(loansThatMatch(
				currentUser.getOrganisation(), client));

		List<LoanAccountData> pendingApprovalLoans = new ArrayList<LoanAccountData>();
		List<LoanAccountData> awaitingDisbursalLoans = new ArrayList<LoanAccountData>();
		List<LoanAccountData> openLoans = new ArrayList<LoanAccountData>();
		List<LoanAccountData> closedLoans = new ArrayList<LoanAccountData>();

		for (Loan realLoan : allLoans) {

			ApplicationCurrency currency = this.applicationCurrencyRepository
					.findOneByCode(realLoan.getLoanRepaymentScheduleDetail()
							.getPrincipal().getCurrencyCode());

			CurrencyData currencyData = new CurrencyData(currency.getCode(),
					currency.getName(), currency.getDecimalPlaces(),
					currency.getDisplaySymbol(), currency.getNameCode());

			LoanAccountData loan = convertToData(realLoan, currencyData);

			if (loan.isClosed() || loan.isInterestRebateOutstanding()) {
				closedLoans.add(loan);
			} else if (loan.isPendingApproval()) {
				pendingApprovalLoans.add(loan);
			} else if (loan.isWaitingForDisbursal()) {
				awaitingDisbursalLoans.add(loan);
			} else if (loan.isOpen()) {
				openLoans.add(loan);
			}
		}

		Collections.sort(closedLoans, new ClosedLoanComparator());

		return new ClientDataWithAccountsData(clientAccount,
				pendingApprovalLoans, awaitingDisbursalLoans, openLoans,
				closedLoans);
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
	public NewLoanWorkflowStepOneData retrieveClientAndProductDetails(final Long clientId, final Long productId) {
		
		AppUser currentUser = extractAuthenticatedUser();

		NewLoanWorkflowStepOneData workflowData = new NewLoanWorkflowStepOneData();
		workflowData.setOrganisationId(currentUser.getOrganisation().getId());
		workflowData.setOrganisationName(currentUser.getOrganisation()
				.getName());

		Collection<LoanProductData> loanProducts = this.loanProductReadPlatformService.retrieveAllLoanProducts();
		workflowData.setAllowedProducts(new ArrayList<LoanProductData>(
				loanProducts));

		if (loanProducts.size() == 1) {
			LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(workflowData.getAllowedProducts().get(0).getId());
			
			workflowData.setProductId(selectedProduct.getId());
			workflowData.setProductName(selectedProduct.getName());
			workflowData.setSelectedProduct(selectedProduct);
		} else {
			LoanProductData selectedProduct = findLoanProductById(loanProducts, productId);
			
			workflowData.setProductId(selectedProduct.getId());
			workflowData.setProductName(selectedProduct.getName());
			workflowData.setSelectedProduct(selectedProduct);
		}

		ClientData clientAccount = retrieveIndividualClient(clientId);
		workflowData.setClientId(clientAccount.getId());
		workflowData.setClientName(clientAccount.getDisplayName());

		return workflowData;
	}
	
	private LoanProductData findLoanProductById(Collection<LoanProductData> loanProducts, Long productId) {
		LoanProductData match = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
		for (LoanProductData loanProductData : loanProducts) {
			if (loanProductData.getId().equals(productId)) {
				match = this.loanProductReadPlatformService.retrieveLoanProduct(loanProductData.getId());
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

	private LoanAccountData convertToData(final Loan realLoan, CurrencyData currencyData) {

		DerivedLoanData loanData = realLoan.deriveLoanData(currencyData);

		LocalDate expectedDisbursementDate = null;
		if (realLoan.getExpectedDisbursedOnDate() != null) {
			expectedDisbursementDate = new LocalDate(
					realLoan.getExpectedDisbursedOnDate());
		}

		Money loanPrincipal = realLoan.getLoanRepaymentScheduleDetail()
				.getPrincipal();
		MoneyData principal = MoneyData.of(currencyData, loanPrincipal.getAmount());

		Money loanArrearsTolerance = realLoan.getInArrearsTolerance();
		MoneyData tolerance = MoneyData.of(currencyData, loanArrearsTolerance.getAmount());

		Money interestRebate = realLoan.getInterestRebateOwed();
		MoneyData interestRebateOwed = MoneyData.of(currencyData, interestRebate.getAmount());

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
				realLoan.getDisbursedOnDate(), realLoan.getExpectedMaturityDate(), realLoan.getExpectedFirstRepaymentOnDate(), realLoan.getInterestCalculatedFromDate(),
				principal, 
				realLoan.getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate(), 
				realLoan.getLoanRepaymentScheduleDetail().getNominalInterestRatePerPeriod(), realLoan
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

	@Override
	public List<CurrencyData> retrieveAllowedCurrencies() {

		AppUser currentUser = extractAuthenticatedUser();

		String sql = "select c.code as code, c.name as name, c.decimal_places as decimalPlaces, c.display_symbol as displaySymbol, c.internationalized_name_code as nameCode from org_organisation_currency c where c.org_id = ?";

		RowMapper<CurrencyData> rm = new CurrencyMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] { currentUser
				.getOrganisation().getId() });
	}

	@Override
	public List<CurrencyData> retrieveAllPlatformCurrencies() {

		String sql = "select c.code as code, c.name as name, c.decimal_places as decimalPlaces, c.display_symbol as displaySymbol, c.internationalized_name_code as nameCode from ref_currency c";

		RowMapper<CurrencyData> rm = new CurrencyMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	protected static final class CurrencyMapper implements
			RowMapper<CurrencyData> {

		@Override
		public CurrencyData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			String code = rs.getString("code");
			String name = rs.getString("name");
			int decimalPlaces = rs.getInt("decimalPlaces");
			String displaySymbol = rs.getString("displaySymbol");
			String nameCode = rs.getString("nameCode");

			return new CurrencyData(code, name, decimalPlaces, displaySymbol,
					nameCode);
		}
	}
	
	@Override
	public Collection<AppUserData> retrieveAllUsers() {

		AppUser currentUser = extractAuthenticatedUser();

		List<OfficeData> offices = retrieveOffices();
		String officeIdsList = generateOfficeIdInClause(offices);

		AppUserMapper mapper = new AppUserMapper(offices);
		String sql = "select " + mapper.schema() + " where u.org_id = ? and u.office_id in (" + officeIdsList + ")";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { currentUser
				.getOrganisation().getId() });
	}

	@Override
	public AppUserData retrieveNewUserDetails() {

		List<OfficeData> offices = retrieveOffices();

		List<RoleData> availableRoles = new ArrayList<RoleData>(retrieveAllRoles());

		AppUserData userData = new AppUserData();
		userData.setAllowedOffices(offices);
		userData.setAvailableRoles(availableRoles);

		return userData;
	}

	@Override
	public AppUserData retrieveUser(Long userId) {

		AppUser currentUser = extractAuthenticatedUser();

		List<OfficeData> offices = retrieveOffices();

		List<RoleData> availableRoles = new ArrayList<RoleData>(
				retrieveAllRoles());

		AppUser user = this.appUserRepository.findOne(usersThatMatch(
				currentUser.getOrganisation(), userId));

		List<RoleData> userRoleData = new ArrayList<RoleData>();
		Set<Role> userRoles = user.getRoles();
		for (Role role : userRoles) {
			userRoleData.add(role.toData());
		}

		AppUserData userData = new AppUserData(user.getId(),
				user.getUsername(), user.getEmail(), user.getOrganisation()
						.getId(), user.getOffice().getId(), user.getOffice()
						.getName());
		userData.setFirstname(user.getFirstname());
		userData.setLastname(user.getLastname());

		userData.setAllowedOffices(offices);

		availableRoles.removeAll(userRoleData);
		userData.setAvailableRoles(availableRoles);
		userData.setSelectedRoles(userRoleData);

		return userData;
	}

	@Override
	public AppUserData retrieveCurrentUser() {
		AppUser currentUser = extractAuthenticatedUser();

		List<OfficeData> offices = retrieveOffices();

		List<RoleData> availableRoles = new ArrayList<RoleData>(retrieveAllRoles());

		AppUser user = this.appUserRepository.findOne(usersThatMatch(
				currentUser.getOrganisation(), currentUser.getId()));

		List<RoleData> userRoleData = new ArrayList<RoleData>();
		Set<Role> userRoles = user.getRoles();
		for (Role role : userRoles) {
			userRoleData.add(role.toData());
		}

		AppUserData userData = new AppUserData(user.getId(),
				user.getUsername(), user.getEmail(), user.getOrganisation()
						.getId(), user.getOffice().getId(), user.getOffice()
						.getName());
		userData.setFirstname(user.getFirstname());
		userData.setLastname(user.getLastname());

		userData.setAllowedOffices(offices);

		availableRoles.removeAll(userRoleData);
		userData.setAvailableRoles(availableRoles);
		userData.setSelectedRoles(userRoleData);

		return userData;
	}

	protected static final class AppUserMapper implements
			RowMapper<AppUserData> {

		private final List<OfficeData> offices;

		public AppUserMapper(final List<OfficeData> offices) {
			this.offices = offices;
		}

		@Override
		public AppUserData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String username = rs.getString("username");
			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String email = rs.getString("email");
			Long orgId = rs.getLong("orgId");
			Long officeId = rs.getLong("officeId");

			String officeName = fromOfficeList(this.offices, officeId);

			AppUserData user = new AppUserData(id, username, email, orgId,
					officeId, officeName);
			user.setLastname(lastname);
			user.setFirstname(firstname);

			return user;
		}

		public String schema() {
			return " u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email, u.org_id as orgId, u.office_id as officeId from admin_appuser u ";
		}

		private String fromOfficeList(final List<OfficeData> officeList,
				final Long officeId) {
			String match = "";
			for (OfficeData office : officeList) {
				if (office.getId().equals(officeId)) {
					match = office.getName();
				}
			}

			return match;
		}
	}

	@Override
	public Collection<RoleData> retrieveAllRoles() {
		AppUser currentUser = extractAuthenticatedUser();

		RoleMapper mapper = new RoleMapper();
		String sql = "select " + mapper.schema() + " where r.org_id = ?";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { currentUser
				.getOrganisation().getId() });
	}

	@Override
	public RoleData retrieveRole(Long roleId) {

		Role role = this.roleRepository.findOne(roleId);

		return role.toData();
	}

	protected static final class RoleMapper implements RowMapper<RoleData> {

		@Override
		public RoleData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			Long orgId = rs.getLong("orgId");
			String name = rs.getString("name");
			String description = rs.getString("description");

			return new RoleData(id, orgId, name, description);
		}

		public String schema() {
			return " r.id as id, r.org_id as orgId, r.name as name, r.description as description from admin_role r ";
		}
	}

	@Override
	public Collection<EnumOptionReadModel> retrieveAllPermissionGroups() {

		Collection<EnumOptionReadModel> options = new ArrayList<EnumOptionReadModel>();

		for (PermissionGroup group : PermissionGroup.values()) {
			options.add(new EnumOptionReadModel(group.name(), Integer.valueOf(
					group.ordinal()).longValue()));
		}
		return options;
	}

	@Override
	public Collection<PermissionData> retrieveAllPermissions() {

		AppUser currentUser = extractAuthenticatedUser();

		PermissionMapper mapper = new PermissionMapper();
		String sql = "select " + mapper.schema() + " where p.org_id = ?";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { currentUser
				.getOrganisation().getId() });
	}

	@Override
	public ClientData retrieveNewClientDetails() {

		AppUser currentUser = extractAuthenticatedUser();

		List<OfficeData> offices = retrieveOffices();

		ClientData clientData = new ClientData();
		clientData.setOfficeId(currentUser.getOffice().getId());
		clientData.setOrganisationId(currentUser.getOrganisation().getId());
		clientData.setAllowedOffices(offices);

		clientData.setDisplayName("");
		clientData.setFirstname("");
		clientData.setLastname("");
		clientData.setId(Long.valueOf(-1));
		clientData.setJoinedDate(new LocalDate());

		return clientData;
	}

	protected static final class PermissionMapper implements
			RowMapper<PermissionData> {

		@Override
		public PermissionData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			Long orgId = rs.getLong("orgId");
			String name = rs.getString("name");
			String description = rs.getString("description");
			String code = rs.getString("code");
			int groupType = rs.getInt("groupType");

			return new PermissionData(id, orgId, name, description, code,
					groupType);
		}

		public String schema() {
			return " p.id as id, p.org_id as orgId, p.default_name as name, p.default_description as description, p.code as code, p.group_enum as groupType from admin_permission p ";
		}
	}

	@Override
	public GenericResultset retrieveGenericResultset(final String rptDB,
			final String name, final String type,
			final Map<String, String> queryParams) {

		if (name == null) {
			logger.info("Report Name not Found");
			return null;
		}

		long startTime = System.currentTimeMillis();
		logger.info("STARTING REPORT: " + name + "   Type: " + type);

//		AppUser currentUser = extractAuthenticatedUser();
//		Collection<GrantedAuthority> permissions = currentUser.getAuthorities();
		/*
		 * AppUser currentUser = extractAuthenticatedUser(); Boolean validUser =
		 * verifyUserDetails(currentUser);
		 * 
		 * if (!validUser) { return null; }
		 * 
		 * String orgId = currentUser.getOrganisation().getId().toString(); put
		 * back in later
		 */
		String orgId = "1";

		String sql;
		try {
			sql = getSQLtoRun(rptDB, name, type, orgId, queryParams);
		} catch (SQLException e) {
			logger.info(name + ": Failed in getSQLtoRun");
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}
		//logger.info(name + ": RUNNING SQL");

		GenericResultset result = null;
		try {
			result = fillReportingGenericResultSet(sql);
		} catch (SQLException e) {
			logger.info("Error - SQL: " + sql);
			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
		}

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING Report/Request Name: " + name + " - " + type
				+ "     Elapsed Time: " + elapsed);
		return result;
	}


	private GenericResultset fillReportingGenericResultSet(final String sql)
			throws SQLException {

		GenericResultset result = new GenericResultset();

		Connection db_connection = dataSource.getConnection();
		Statement db_statement = db_connection.createStatement();
		ResultSet rs = db_statement.executeQuery(sql);

		ResultSetMetaData rsmd = rs.getMetaData();
		String columnName = null;
		String columnValue = null;
		List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
		for (int i = 0; i < rsmd.getColumnCount(); i++) {
			ResultsetColumnHeader rsch = new ResultsetColumnHeader();
			rsch.setColumnName(rsmd.getColumnName(i + 1));
			rsch.setColumnType(rsmd.getColumnTypeName(i + 1));
			columnHeaders.add(rsch);
		}
		result.setColumnHeaders(columnHeaders);

		List<ResultsetDataRow> resultsetDataRows = new ArrayList<ResultsetDataRow>();
		ResultsetDataRow resultsetDataRow;
		while (rs.next()) {
			resultsetDataRow = new ResultsetDataRow();
			List<String> columnValues = new ArrayList<String>();
			for (int i = 0; i < rsmd.getColumnCount(); i++) {
				columnName = rsmd.getColumnName(i + 1);
				columnValue = rs.getString(columnName);
				columnValues.add(columnValue);
			}
			resultsetDataRow.setRow(columnValues);
			resultsetDataRows.add(resultsetDataRow);
		}
		result.setData(resultsetDataRows);

		db_statement.close();
		db_statement = null;
		db_connection.close();
		db_connection = null;

		return result;

	}

	private String getSQLtoRun(final String rptDB, final String name,
			final String type, final String orgId,
			final Map<String, String> queryParams) throws SQLException {
		String sql = null;

		if (type.equals("report")) {
			sql = getReportSql(rptDB, name);
		} else {
			// todo - dont need to check for orgID if special parameter sql (but
			// prob need to check restrictions
			sql = getParameterSql(rptDB, name);
		}

		sql = replace(sql, "${orgId}", orgId);

		Set<String> keys = queryParams.keySet();
		for (String key : keys) {
			String pValue = queryParams.get(key);
			// logger.info("(" + key + " : " + pValue + ")");
			sql = replace(sql, key, pValue);
		}

		// wrap sql to prevent JDBC sql errors and also prevent malicious sql
		sql = "select x.* from (" + sql + ") x";

		return sql;

	}

//	private Boolean verifyUserDetails(AppUser usr) {
//
//		// some logs to be taken out after testing
//		String idDetails = usr.getId() + ", " + usr.getLastname() + ", "
//				+ usr.getFirstname();
//		logger.info("Id: " + idDetails + "   Organisation: "
//				+ usr.getOrganisation().getId() + "   Office: "
//				+ usr.getOffice().getId() + "   Role Names: "
//				+ usr.getRoleNames());
//		String otherDetails = "Head Officer User? " + usr.isHeadOfficeUser()
//				+ "  Enabled: " + usr.isEnabled();
//		logger.info(otherDetails);
//		if (usr.getAuthorities() != null) {
////			for (GrantedAuthority grantedAuthority : usr.getAuthorities()) {
//				// logger.info("Granted Authority: " +
//				// grantedAuthority.getAuthority());
////			}
//		}
//		logger.info("");
//
//		// some checks
//		if (usr.getOrganisation().getId() == null) {
//			logger.info("Organisation ID not Found");
//			return false;
//		}
//
//		return true;
//	}

	private String getReportSql(String rptDB, String reportName)
			throws SQLException {
		String sql = "select report_sql as the_sql from " + rptDB
				+ ".stretchy_report where report_name = '" + reportName + "'";
		// logger.info("Report SQL: " + sql);

		return getSql(sql);
	}

	private String getParameterSql(String rptDB, String parameterName)
			throws SQLException {
		String sql = "select parameter_sql as the_sql from " + rptDB
				+ ".stretchy_parameter where parameter_name = '"
				+ parameterName + "'";
		// logger.info("Parameter SQL: " + sql);

		return getSql(sql);
	}

	private String getSql(String inputSql) throws SQLException {

		Connection db_connection = dataSource.getConnection();
		Statement db_statement = db_connection.createStatement();
		ResultSet rs = db_statement.executeQuery(inputSql);

		String sql = null;

		while (rs.next()) {
			sql = rs.getString("the_sql");
		}

		db_statement.close();
		db_statement = null;
		db_connection.close();
		db_connection = null;

		return sql;
	}

	static String replace(String str, String pattern, String replace) {
		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();

		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}
	
	
}