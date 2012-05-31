package org.mifosng.platform.loan.service;

import static org.mifosng.platform.Specifications.loanTransactionsThatMatch;
import static org.mifosng.platform.Specifications.loansThatMatch;

import java.util.ArrayList;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.ClientData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.DerivedLoanData;
import org.mifosng.platform.api.data.LoanAccountData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanPermissionData;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.data.LoanProductLookup;
import org.mifosng.platform.api.data.LoanRepaymentData;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.NewLoanData;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.CurrencyNotFoundException;
import org.mifosng.platform.exceptions.LoanNotFoundException;
import org.mifosng.platform.exceptions.LoanTransactionNotFoundException;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanReadPlatformServiceImpl implements LoanReadPlatformService {

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
			final ClientReadPlatformService clientReadPlatformService) {
		this.context = context;
		this.loanRepository = loanRepository;
		this.loanTransactionRepository = loanTransactionRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.loanProductReadPlatformService = loanProductReadPlatformService;
		this.clientReadPlatformService = clientReadPlatformService;
	}

	@Override
	public LoanAccountData retrieveLoanAccountDetails(Long loanId) {

		// TODO - OPTIMISE - prefer jdbc sql approach to return only what we
		// need of loan information.
		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), loanId));
		if (loan == null) {
			throw new LoanNotFoundException(loanId);
		}

		final String currencyCode = loan.getCurrencyCode();
		ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(currencyCode);
		if (currency == null) {
			throw new CurrencyNotFoundException(currencyCode);
		}

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());

		return convertToData(loan, currencyData);
	}

	private LoanAccountData convertToData(final Loan realLoan, CurrencyData currencyData) {

		DerivedLoanData loanData = realLoan.deriveLoanData(currencyData);

		LoanBasicDetailsData basicDetails = realLoan.toBasicDetailsData(currencyData);
		
		// permissions
		boolean waiveAllowed = loanData.getSummary().isWaiveAllowed(basicDetails.getInArrearsTolerance())
				&& realLoan.isNotClosed();
		boolean undoDisbursalAllowed = realLoan.isDisbursed()
				&& realLoan.isOpenWithNoRepaymentMade();
		boolean makeRepaymentAllowed = realLoan.isDisbursed()
				&& realLoan.isNotClosed();

		boolean rejectAllowed = realLoan.isNotApproved()
				&& realLoan.isNotDisbursed() && realLoan.isNotClosed();
		boolean withdrawnByApplicantAllowed = realLoan.isNotDisbursed()
				&& realLoan.isNotClosed();
		boolean undoApprovalAllowed = realLoan.isApproved()
				&& realLoan.isNotClosed();
		boolean disbursalAllowed = realLoan.isApproved()
				&& realLoan.isNotDisbursed() && realLoan.isNotClosed();
		
		LoanPermissionData permissions = new LoanPermissionData(waiveAllowed, makeRepaymentAllowed, rejectAllowed, withdrawnByApplicantAllowed, 
				undoApprovalAllowed, undoDisbursalAllowed, disbursalAllowed, realLoan.isSubmittedAndPendingApproval(),
				realLoan.isWaitingForDisbursal());
		
		return new LoanAccountData(realLoan.getId(), basicDetails, loanData, permissions);

//		return new LoanAccountData(realLoan.isClosed(), realLoan.isOpen(),
//				realLoan.isOpenWithRepaymentMade(), interestRebateOutstanding,
//				realLoan.isSubmittedAndPendingApproval(),
//				realLoan.isWaitingForDisbursal(), undoDisbursalAllowed,
//				makeRepaymentAllowed, rejectAllowed,
//				withdrawnByApplicantAllowed, undoApprovalAllowed,
//				disbursalAllowed, realLoan.getLoanStatusDisplayName(),
//				loanStatusDate, realLoan.getId(), realLoan.getExternalId(),
//				realLoan.getLoanProduct().getName(),
//				realLoan.getClosedOnDate(), realLoan.getSubmittedOnDate(),
//				realLoan.getApprovedOnDate(), expectedDisbursementDate,
//				realLoan.getDisbursedOnDate(),
//				realLoan.getExpectedMaturityDate(),
//				realLoan.getExpectedFirstRepaymentOnDate(),
//				realLoan.getInterestCalculatedFromDate(), principal, realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getAnnualNominalInterestRate(), realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getNominalInterestRatePerPeriod(), realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getInterestPeriodFrequencyType().getValue(), realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getInterestPeriodFrequencyType().toString(), realLoan
//						.getLoanRepaymentScheduleDetail().getInterestMethod()
//						.getValue(), realLoan.getLoanRepaymentScheduleDetail()
//						.getInterestMethod().toString(), realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getAmortizationMethod().getValue(), realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getAmortizationMethod().toString(), realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getNumberOfRepayments(), realLoan
//						.getLoanRepaymentScheduleDetail().getRepayEvery(),
//				realLoan.getLoanRepaymentScheduleDetail()
//						.getRepaymentPeriodFrequencyType().getValue(), realLoan
//						.getLoanRepaymentScheduleDetail()
//						.getRepaymentPeriodFrequencyType().toString(),
//				tolerance, loanData, waiveAllowed, interestRebateOwed);
	}

	@Override
	public NewLoanData retrieveClientAndProductDetails(final Long clientId, final Long productId) {

		context.authenticatedUser();

		NewLoanData workflowData = new NewLoanData();

		Collection<LoanProductLookup> loanProducts = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
		workflowData.setAllowedProducts(new ArrayList<LoanProductLookup>(loanProducts));

		if (loanProducts.size() == 1) {
			Long allowedProductId = workflowData.getAllowedProducts().get(0).getId();
			LoanProductData selectedProduct = this.loanProductReadPlatformService.retrieveLoanProduct(allowedProductId);

			workflowData.setProductId(selectedProduct.getId());
			workflowData.setProductName(selectedProduct.getName());
			workflowData.setSelectedProduct(selectedProduct);
		} else {
			LoanProductData selectedProduct = findLoanProductById(loanProducts, productId);
			
			workflowData.setProductId(selectedProduct.getId());
			workflowData.setProductName(selectedProduct.getName());
			workflowData.setSelectedProduct(selectedProduct);
		}

		ClientData clientAccount = this.clientReadPlatformService.retrieveIndividualClient(clientId);
		workflowData.setClientId(clientAccount.getId());
		workflowData.setClientName(clientAccount.getDisplayName());
		
		workflowData.setExpectedDisbursementDate(new LocalDate());

		return workflowData;
	}

	private LoanProductData findLoanProductById(Collection<LoanProductLookup> loanProducts, Long productId) {
		LoanProductData match = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
		for (LoanProductLookup loanProductLookup : loanProducts) {
			if (loanProductLookup.getId().equals(productId)) {
				match = this.loanProductReadPlatformService.retrieveLoanProduct(loanProductLookup.getId());
				break;
			}
		}
		return match;
	}

	@Override
	public LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId) {

		AppUser currentUser = context.authenticatedUser();

		// TODO - OPTIMIZE - write simple sql query to fetch back date of
		// possible next transaction date.
		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));
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

		LoanRepaymentData newRepaymentDetails = new LoanRepaymentData();
		newRepaymentDetails.setDate(earliestUnpaidInstallmentDate);
		newRepaymentDetails.setTotal(possibleNextRepayment);

		return newRepaymentDetails;
	}

	@Override
	public LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId) {

		AppUser currentUser = context.authenticatedUser();

		// TODO - OPTIMIZE - write simple sql query to fetch back date of
		// possible next transaction date.
		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));
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

		LoanRepaymentData newWaiverDetails = new LoanRepaymentData();
		newWaiverDetails.setDate(new LocalDate());
		newWaiverDetails.setTotal(totalOutstandingData);

		return newWaiverDetails;
	}

	@Override
	public LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId,
			Long transactionId) {

		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));
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
				.findOne(loanTransactionsThatMatch(
						currentUser.getOrganisation(), transactionId));
		if (transaction == null) {
			throw new LoanTransactionNotFoundException(transactionId);
		}

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());
		MoneyData total = MoneyData.of(currencyData, transaction.getAmount());
		LocalDate date = transaction.getTransactionDate();

		LoanRepaymentData loanRepaymentData = new LoanRepaymentData();
		loanRepaymentData.setId(transactionId);
		loanRepaymentData.setTotal(total);
		loanRepaymentData.setDate(date);

		return loanRepaymentData;
	}
}