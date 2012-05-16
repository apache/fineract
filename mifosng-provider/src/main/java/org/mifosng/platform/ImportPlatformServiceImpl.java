package org.mifosng.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.MoneyData;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.EnrollClientCommand;
import org.mifosng.data.command.ImportClientCommand;
import org.mifosng.data.command.ImportLoanCommand;
import org.mifosng.data.command.ImportLoanRepaymentsCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.SubmitApproveDisburseLoanCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.DefaultLoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanBuilder;
import org.mifosng.platform.loan.domain.LoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.LoanRepaymentScheduleInstallment;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanStatusRepository;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loan.service.CalculationPlatformService;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csvreader.CsvReader;

@Service
public class ImportPlatformServiceImpl implements ImportPlatformService {

	private final OfficeRepository officeRepository;
	private final ClientRepository clientRepository;
	private final LoanProductRepository loanProductRepository;
	private final CalculationPlatformService calculationPlatformService;
	private final LoanRepository loanRepository;
	private final LoanStatusRepository loanStatusRepository;

	@Autowired
	public ImportPlatformServiceImpl(
			final OfficeRepository officeRepository, final ClientRepository clientRepository, final LoanProductRepository loanProductRepository,
			final CalculationPlatformService calculationPlatformService, final LoanRepository loanRepository, final LoanStatusRepository loanStatusRepository) {
		this.officeRepository = officeRepository;
		this.clientRepository = clientRepository;
		this.loanProductRepository = loanProductRepository;
		this.calculationPlatformService = calculationPlatformService;
		this.loanRepository = loanRepository;
		this.loanStatusRepository = loanStatusRepository;
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

	@Transactional
	@Override
	public void importClients(ImportClientCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		List<Office> allOffices = this.officeRepository.findAll(officesBelongingTo(currentUser.getOrganisation()));
		
		List<Client> newClientCollection = new ArrayList<Client>();
		
		for (EnrollClientCommand client : command.getClients()) {
			Office clientOffice = findById(allOffices, client.getOfficeId());

			Client newClient = Client.newClient(currentUser.getOrganisation(), clientOffice, client.getFirstname(), client.getLastname(), client.getJoiningDate(), client.getExternalId());
			
			newClientCollection.add(newClient);
		}
		
		this.clientRepository.save(newClientCollection);
	}
	
	@Transactional
	@Override
	public void importLoans(ImportLoanCommand importCommand) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		List<Loan> newLoanCollection = new ArrayList<Loan>();
		
		for (SubmitApproveDisburseLoanCommand combinedCommand : importCommand.getLoans()) {
			
			SubmitLoanApplicationCommand command = combinedCommand.getSubmitLoanApplicationCommand();
			
			// most of this code can be extracted out into an Assembler
			LoanProduct loanProduct = this.loanProductRepository.findOne(command
					.getProductId());
			Client client = this.clientRepository.findOne(command.getApplicantId());
			
			MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
			
			final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(command
					.getInterestRatePerPeriod().doubleValue());
			final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
			
			// apr calculator
			BigDecimal defaultAnnualNominalInterestRate = BigDecimal.ZERO;
			switch (interestPeriodFrequencyType) {
			case DAYS:
				break;
			case WEEKS:
				defaultAnnualNominalInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(52));
				break;
			case MONTHS:
				defaultAnnualNominalInterestRate = command.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(12));
				break;
			case YEARS:
				defaultAnnualNominalInterestRate = command.getInterestRatePerPeriod();
				break;
			case INVALID:
				break;
			}
			
			final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
			final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodMethod());
			
			final Integer repayEvery = command.getRepaymentEvery();
			final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType
					.fromInt(command.getRepaymentFrequency());
			final Integer defaultNumberOfInstallments = command.getNumberOfRepayments();
			final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationMethod());
			final boolean flexibleRepaymentSchedule = command.isFlexibleRepaymentSchedule();
			final boolean interestRebateAllowed = command.isInterestRebateAllowed();
			
			LoanProductRelatedDetail loanRepaymentScheduleDetail = new LoanProductRelatedDetail(currency, command.getPrincipal(),
					defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, interestMethod, interestCalculationPeriodMethod,
					repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, command.getInArrearsToleranceAmount(), flexibleRepaymentSchedule, interestRebateAllowed);

			LoanSchedule loanSchedule = command.getLoanSchedule();
			List<ScheduledLoanInstallment> loanRepaymentSchedule = loanSchedule
					.getScheduledLoanInstallments();

			Loan loan = new LoanBuilder().with(currentUser.getOrganisation())
					.with(loanProduct).with(client)
					.withExternalSystemId(command.getExternalId())
					.with(loanRepaymentScheduleDetail).build();

			for (ScheduledLoanInstallment scheduledLoanInstallment : loanRepaymentSchedule) {

				MoneyData readPrincipalDue = scheduledLoanInstallment
						.getPrincipalDue();

				MoneyData readInterestDue = scheduledLoanInstallment
						.getInterestDue();

				LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
						loan, scheduledLoanInstallment.getInstallmentNumber(),
						scheduledLoanInstallment.getPeriodStart(),
						scheduledLoanInstallment.getPeriodEnd(), readPrincipalDue.getAmount(),
						readInterestDue.getAmount());
				loan.addRepaymentScheduleInstallment(installment);
			}
			
			LoanLifecycleStateMachine loanLifecycleStateMachine = defaultLoanLifecycleStateMachine();
			
			loan.submitApplication(command.getSubmittedOnDate(), command.getExpectedDisbursementDate(), command.getRepaymentsStartingFromDate(), command.getInterestCalculatedFromDate(), loanLifecycleStateMachine);
			
			LoanStateTransitionCommand approveLoanCommand = combinedCommand.getApproveLoanCommand();
			if (approveLoanCommand != null) {
				loan.approve(approveLoanCommand.getEventDate(), loanLifecycleStateMachine);
			}
			
			LoanStateTransitionCommand disburseLoanCommand = combinedCommand.getDisburseLoanCommand();
			if (disburseLoanCommand != null) {
				loan.disburse(disburseLoanCommand.getEventDate(), loanLifecycleStateMachine);
			}
			
			newLoanCollection.add(loan);
		}
		
		this.loanRepository.save(newLoanCollection);
	}
	
	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = this.loanStatusRepository.findAll();
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
	}

	@Transactional
	@Override
	public void importLoanRepayments(ImportLoanRepaymentsCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		LoanLifecycleStateMachine loanLifecycleStateMachine = defaultLoanLifecycleStateMachine();
		
		List<Loan> newLoanCollection = new ArrayList<Loan>();
		
		List<Loan> allLoans = this.loanRepository.findAll(loansBelongingTo(currentUser.getOrganisation()));
		for (LoanTransactionCommand repaymentDetail : command.getRepayments()) {
			Loan loan = findLoanByIdentifier(allLoans, repaymentDetail.getLoanId().toString());
			
			Money repaymentAmount = Money.of(loan.getCurrency(), repaymentDetail.getPaymentAmount());
			
			LoanTransaction loanRepayment = LoanTransaction.repayment(repaymentAmount, repaymentDetail.getPaymentDate());
			loan.makeRepayment(loanRepayment, loanLifecycleStateMachine);
		}
		
		this.loanRepository.save(newLoanCollection);
		
	}

	public static Specification<Office> officesBelongingTo(final Organisation organisation) {
		return new Specification<Office>() {

			@Override
			public Predicate toPredicate(final Root<Office> root,
					final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.equal(root.get("organisation"), organisation);
			}
		};
	}
	
	public static Specification<LoanProduct> productsBelongingTo(final Organisation organisation) {
		return new Specification<LoanProduct>() {

			@Override
			public Predicate toPredicate(final Root<LoanProduct> root,
					final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.equal(root.get("organisation"), organisation);
			}
		};
	}
	
	public static Specification<Loan> loansBelongingTo(final Organisation organisation) {
		return new Specification<Loan>() {

			@Override
			public Predicate toPredicate(final Root<Loan> root,
					final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.equal(root.get("organisation"), organisation);
			}
		};
	}
	
	public static Specification<Client> clientsBelongingTo(final Organisation organisation) {
		return new Specification<Client>() {

			@Override
			public Predicate toPredicate(final Root<Client> root,
					final CriteriaQuery<?> query, final CriteriaBuilder cb) {
				return cb.equal(root.get("organisation"), organisation);
			}
		};
	}
	
	@Override
	public ImportLoanRepaymentsCommand populateLoanRepaymentsImportFromCsv() {
		
		ImportLoanRepaymentsCommand command = new ImportLoanRepaymentsCommand();
		
		AppUser currentUser = extractAuthenticatedUser();
		
		List<Loan> allLoans = this.loanRepository.findAll(loansBelongingTo(currentUser.getOrganisation()));
		ClassPathResource repaymentsCsvFile = new ClassPathResource("creocore-loan-repayments.csv");
		
		File file = null;
		FileInputStream fileInputStream = null;
		CsvReader loans = null;
		try {
			DateTimeFormatter isoParser = ISODateTimeFormat.date();
			loans = new CsvReader(repaymentsCsvFile.getInputStream(),
					Charset.defaultCharset());
			loans.readHeaders();
			
			List<LoanTransactionCommand> repaymentDetails = new ArrayList<LoanTransactionCommand>();
			
			while (loans.readRecord()) {
				String loanExternalId = loans.get("LoanExternalId");
				Loan matchingLoan = findLoanByIdentifier(allLoans, loanExternalId.trim());
				
				String paymentDate = loans.get("PaymentDate");
				LocalDate paidOnDate = null;
				if (StringUtils.isNotBlank(paymentDate)) {
					paidOnDate = new LocalDate(isoParser.parseDateTime(paymentDate));
				}

				String paymentAmount = loans.get("PaymentAmount");
				BigDecimal repayment = BigDecimal.ZERO;
				if (StringUtils.isNotBlank(paymentAmount)) {
					repayment = BigDecimal.valueOf(Double.valueOf(paymentAmount));
				}
				
				String noNoteComment = "No note due to migration from mifos.";
				
				LoanTransactionCommand repaymentCommand = new LoanTransactionCommand(matchingLoan.getId(), paidOnDate, noNoteComment, repayment);
				
				repaymentDetails.add(repaymentCommand);
			}
			
			command.setRepayments(repaymentDetails);
			
			return command;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (file != null) {
				file = null;
			}
			if (fileInputStream != null) {
				fileInputStream = null;
			}
			if (loans != null) {
				loans = null;
			}
		}
	}

	@Override
	public ImportLoanCommand populateLoanImportFromCsv() {
		
		ImportLoanCommand command = new ImportLoanCommand();
		
		AppUser currentUser = extractAuthenticatedUser();
		
		List<LoanProduct> allProducts = this.loanProductRepository.findAll(productsBelongingTo(currentUser.getOrganisation()));
		
		List<Client> allClients = this.clientRepository.findAll(clientsBelongingTo(currentUser.getOrganisation()));
		
		ClassPathResource loanCsvFile = new ClassPathResource("creocore-loans.csv");
		
		File file = null;
		FileInputStream fileInputStream = null;
		CsvReader loans = null;
		try {
			DateTimeFormatter isoParser = ISODateTimeFormat.date();
			loans = new CsvReader(loanCsvFile.getInputStream(),
					Charset.defaultCharset());
			loans.readHeaders();
			
			List<SubmitApproveDisburseLoanCommand> parsedLoanDetails = new ArrayList<SubmitApproveDisburseLoanCommand>();
			
			while (loans.readRecord()) {
				String clientIdentifier = loans.get("ClientExternalId");
				Client matchingClient = findClientByIdentifier(allClients, clientIdentifier.trim());
				
				String loanExternalId = loans.get("LoanExternalId");
				
				String productIdentifier = loans.get("ProductExternalId");
				LoanProduct matchingProduct = findProductByIdentifier(allProducts, productIdentifier);
				
				String loanPrincipal = loans.get("LoanPrincipal");
				Number principal = BigDecimal.ZERO;
				if (StringUtils.isNotBlank(loanPrincipal)) {
					principal = Double.valueOf(loanPrincipal);
				}
				
				String interestRateAsString = loans.get("InterestRate");
				Number interestRatePerYear = BigDecimal.ZERO;
				if (StringUtils.isNotBlank(interestRateAsString)) {
					interestRatePerYear = Double.valueOf(interestRateAsString);
				}
				
				String submitted = loans.get("SubmittedOn");
				LocalDate submittedOnDate = null;
				if (StringUtils.isNotBlank(submitted)) {
					submittedOnDate = new LocalDate(isoParser.parseDateTime(submitted));
				}

				String noNoteComment = "No note due to migration from mifos.";
				
				String installmentsAsString = loans.get("Installments");
				Integer numberOfRepayments = Integer.valueOf(0);
				if (StringUtils.isNotBlank(installmentsAsString)) {
					numberOfRepayments = Integer.valueOf(installmentsAsString);
				}
				
				String approved = loans.get("ApprovedOn");
				LocalDate approvedDate = null;
				if (StringUtils.isNotBlank(approved)) {
					approvedDate = new LocalDate(isoParser.parseDateTime(approved));
				}
				
				String disbursed = loans.get("DateDisbursalTransaction");
				LocalDate disbursedDate = null;
				LocalDate expectedDisbursementDate = null;
				
				if (StringUtils.isNotBlank(disbursed)) {
					disbursedDate = new LocalDate(isoParser.parseDateTime(disbursed));
					expectedDisbursementDate = disbursedDate;
				} else {
					expectedDisbursementDate = approvedDate;
				}
				
				String repaymentsStartOn = loans.get("FirstInstallmentDate");
				LocalDate repaymentsStartingFromDate = null;
				LocalDate interestCalculatedFromDate = null;
				if (StringUtils.isNotBlank(repaymentsStartOn)) {
					repaymentsStartingFromDate = new LocalDate(isoParser.parseDateTime(repaymentsStartOn));
					interestCalculatedFromDate = expectedDisbursementDate;
				}
				
				String currencyCode = matchingProduct.getCurrency().getCode();
				int digitsAfterDecimal = matchingProduct.getCurrency().getDigitsAfterDecimal();
				Integer repaymentEvery = matchingProduct.getRepayEvery();
				Integer repaymentFrequency = matchingProduct.getRepaymentPeriodFrequencyType().getValue();
				Integer amortizationMethod = matchingProduct.getAmortizationMethod().getValue();
				
				final Money toleranceAmount = matchingProduct.getInArrearsTolerance();
				
				Number interestRatePerPeriod = interestRatePerYear;
				Integer interestRatePeriodFrequency = PeriodFrequencyType.YEARS.getValue();
				Integer interestMethod = matchingProduct.getInterestMethod().getValue();
				Integer interestCalculationPeriodMethod = InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD.getValue();
				
				CalculateLoanScheduleCommand calculateLoanScheduleCommand = new CalculateLoanScheduleCommand(currencyCode, digitsAfterDecimal, principal, 
						interestRatePerPeriod, 
						interestRatePeriodFrequency, 
						interestMethod, interestCalculationPeriodMethod,
						repaymentEvery, 
						repaymentFrequency, numberOfRepayments, amortizationMethod, expectedDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);
				
				LoanSchedule loanSchedule = this.calculationPlatformService.calculateLoanSchedule(calculateLoanScheduleCommand);
				
				SubmitLoanApplicationCommand parsedNewLoanDetails = new SubmitLoanApplicationCommand(matchingClient.getId(), matchingProduct.getId(), submittedOnDate, noNoteComment, 
						expectedDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate, loanSchedule, 
						currencyCode, digitsAfterDecimal, principal, 
						interestRatePerPeriod, interestRatePeriodFrequency, interestMethod, interestCalculationPeriodMethod,
						repaymentEvery, repaymentFrequency, numberOfRepayments, amortizationMethod, toleranceAmount.getAmount());
				
				parsedNewLoanDetails.setExternalId(loanExternalId);
				
				LoanStateTransitionCommand approvedLoanCommand = null;
				if (approvedDate != null) {
					approvedLoanCommand = new LoanStateTransitionCommand(null, approvedDate, noNoteComment);
				}
				
				LoanStateTransitionCommand disburseLoanCommand = null;
				if (disbursedDate != null) {
					disburseLoanCommand = new LoanStateTransitionCommand(null, disbursedDate, noNoteComment);
				}
				
				SubmitApproveDisburseLoanCommand submitApproveDisburseLoanCommand = new SubmitApproveDisburseLoanCommand();
				submitApproveDisburseLoanCommand.setSubmitLoanApplicationCommand(parsedNewLoanDetails);
				submitApproveDisburseLoanCommand.setApproveLoanCommand(approvedLoanCommand);
				submitApproveDisburseLoanCommand.setDisburseLoanCommand(disburseLoanCommand);
				
				parsedLoanDetails.add(submitApproveDisburseLoanCommand);
			}
			
			command.setLoans(parsedLoanDetails);
			
			return command;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
//			throw new BulkuploadFileNotFoundException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
//			throw new BulkuploadInvalidDataException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
//			throw new BulkuploadParseException(e);
		} finally {
			if (file != null) {
				file = null;
			}
			if (fileInputStream != null) {
				fileInputStream = null;
			}
			if (loans != null) {
				loans = null;
			}
		}
	}

	@Override
	public ImportClientCommand populateClientImportFromCsv() {
		
		ImportClientCommand command = new ImportClientCommand();
		
		AppUser currentUser = extractAuthenticatedUser();

		List<Office> allOffices = this.officeRepository.findAll(officesBelongingTo(currentUser.getOrganisation()));
		
		ClassPathResource clientCsv = new ClassPathResource("creocore-clients.csv");

		File file = null;
		FileInputStream fileInputStream = null;
		CsvReader clients = null;
		try {
			DateTimeFormatter isoParser = ISODateTimeFormat.date();
			clients = new CsvReader(clientCsv.getInputStream(),
					Charset.defaultCharset());
			clients.readHeaders();
			
//			int clientCsvHeaderCount = clients.getHeaderCount();
//			if (clientCsvHeaderCount != 5) {
//				success = false;
//			}

			List<EnrollClientCommand> parsedClients = new ArrayList<EnrollClientCommand>();
			while (clients.readRecord()) {
				String externalId = clients.get("ExternalId");
				String firstname = clients.get("FirstName");
				String lastname = clients.get("LastName");
				String joined = clients.get("Joined");
				String officeIdentifier = clients.get("Office");
				Office matchingOffice = findOfficeByIdentifier(allOffices, officeIdentifier.trim());
				
				LocalDate joiningDate = new LocalDate(isoParser.parseDateTime(joined));

				EnrollClientCommand parsedClient = new EnrollClientCommand(firstname, lastname, "", matchingOffice.getId(), joiningDate);
				parsedClient.setExternalId(externalId);
				
				parsedClients.add(parsedClient);
			}
			
			command.setClients(parsedClients);

			return command;
		} catch (FileNotFoundException e) {
//			throw new BulkuploadFileNotFoundException(e);
		} catch (IllegalArgumentException e) {
//			throw new BulkuploadInvalidDataException(e);
		} catch (IOException e) {
//			throw new BulkuploadParseException(e);
		} finally {
			if (file != null) {
				file = null;
			}
			if (fileInputStream != null) {
				fileInputStream = null;
			}
			if (clients != null) {
				clients = null;
			}
		}
		
		return command;
	}

	private Office findById(List<Office> allOffices, Long id) {
		
		Office match = null;
		
		for (Office office : allOffices) {
			if (id.equals(office.getId())) {
				match = office;
				break;
			}
		}
	
		return match;
	}
	
	
	private Loan findLoanByIdentifier(List<Loan> allEntities, String identifier) {
		Loan match = null;
		
		for (Loan entity : allEntities) {
			if (entity.identifiedBy(identifier)) {
				match = entity;
				break;
			}
		}
	
		return match;
	}
	
	private Office findOfficeByIdentifier(List<Office> allOffices, String officeIdentifier) {
		Office match = null;
		
		for (Office office : allOffices) {
			if (office.identifiedBy(officeIdentifier)) {
				match = office;
				break;
			}
		}
	
		return match;
	}
	
	private Client findClientByIdentifier(List<Client> allClients, String identifier) {
		Client match = null;
		
		for (Client client : allClients) {
			if (client.identifiedBy(identifier)) {
				match = client;
				break;
			}
		}
	
		return match;
	}
	
	private LoanProduct findProductByIdentifier(List<LoanProduct> allProducts, String identifier) {
		LoanProduct match = null;
		
		for (LoanProduct client : allProducts) {
			if (client.identifiedBy(identifier)) {
				match = client;
				break;
			}
		}
	
		return match;
	}
}