package org.mifosng.platform;

import static org.mifosng.platform.Specifications.loansThatMatch;
import static org.mifosng.platform.Specifications.officesThatMatch;
import static org.mifosng.platform.Specifications.usersThatMatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.MoneyData;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.EnrollClientCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.data.command.UndoLoanApprovalCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
import org.mifosng.data.command.UpdateUsernamePasswordCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.infrastructure.BasicPasswordEncodablePlatformUser;
import org.mifosng.platform.infrastructure.PlatformPasswordEncoder;
import org.mifosng.platform.infrastructure.PlatformUser;
import org.mifosng.platform.infrastructure.UsernameAlreadyExistsException;
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
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.mifosng.platform.user.domain.PlatformUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WritePlatformServiceJpaRepositoryImpl implements WritePlatformService {

	private final OfficeRepository officeRepository;
	private final PlatformUserRepository platformUserRepository;
	private final PlatformPasswordEncoder platformPasswordEncoder;
	private final ClientRepository clientRepository;
	private final LoanProductRepository loanProductRepository;
	private final LoanRepository loanRepository;
	private final LoanStatusRepository loanStatusRepository;
	private final CalculationPlatformService calculationPlatformService;
	private final NoteRepository noteRepository;
	private final LoanTransactionRepository loanTransactionRepository;

	@Autowired
	public WritePlatformServiceJpaRepositoryImpl(
			final OfficeRepository officeRepository,
			final PlatformUserRepository platformUserRepository,
			final PlatformPasswordEncoder platformPasswordEncoder,
			final ClientRepository clientRepository,
			final NoteRepository noteRepository,
			final LoanProductRepository loanProductRepository,
			final LoanRepository loanRepository,
			final LoanTransactionRepository loanTransactionRepository,
			final LoanStatusRepository loanStatusRepository,
			final CalculationPlatformService calculationPlatformService
			) {
		this.officeRepository = officeRepository;
		this.platformUserRepository = platformUserRepository;
		this.platformPasswordEncoder = platformPasswordEncoder;
		this.clientRepository = clientRepository;
		this.noteRepository = noteRepository;
		this.loanProductRepository = loanProductRepository;
		this.loanRepository = loanRepository;
		this.loanTransactionRepository = loanTransactionRepository;
		this.loanStatusRepository = loanStatusRepository;
		this.calculationPlatformService = calculationPlatformService;
	}
	
	@Transactional
	@Override
	public void updateUsernamePasswordOnFirstTimeLogin(final UpdateUsernamePasswordCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		try {
			PlatformUser platformUser = ((AppUserRepository) this.platformUserRepository).findOne(usersThatMatch(currentUser.getOrganisation(), command.getOldUsername()));

			PlatformUser dummyPlatformUser = new BasicPasswordEncodablePlatformUser(
					((AppUser) platformUser).getId(),
					platformUser.getUsername(), command.getPassword());

			String encodePassword = this.platformPasswordEncoder.encode(dummyPlatformUser);

			if (command.isUsernameToBeChanged()) {
				platformUser.updateUsernamePasswordOnFirstTimeLogin(
						command.getUsername(), encodePassword);
			} else {
				platformUser.updatePasswordOnFirstTimeLogin(encodePassword);
			}

			((AppUserRepository) this.platformUserRepository).save((AppUser) platformUser);
		} catch (DataIntegrityViolationException e) {
			throw new UsernameAlreadyExistsException(e);
		}
	}

	@Transactional
	@Override
	public Long enrollClient(final EnrollClientCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		EnrollClientCommandValidator validator = new EnrollClientCommandValidator(command);
		validator.validate();

		Office clientOffice = this.officeRepository.findOne(officesThatMatch(
				currentUser.getOrganisation(), command.getOfficeId()));
		
		String firstname = command.getFirstname();
		String lastname = command.getLastname();
		if (StringUtils.isNotBlank(command.getFullname())) {
			lastname = command.getFullname();
			firstname = null;
		}

		Client newClient = Client.newClient(currentUser.getOrganisation(), clientOffice, firstname, lastname, command.getJoiningDate(), command.getExternalId());
				
		this.clientRepository.save(newClient);

		return newClient.getId();
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
	public EntityIdentifier submitLoanApplication(final SubmitLoanApplicationCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		SubmitLoanApplicationCommandValidator validator = new SubmitLoanApplicationCommandValidator(command);
		validator.validate();

		LocalDate submittedOn = command.getSubmittedOnDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE")) {
			throw new NoAuthorizationException("Cannot add backdated loan.");
		}

		LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
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
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod()
					.multiply(BigDecimal.valueOf(52));
			break;
		case MONTHS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod()
					.multiply(BigDecimal.valueOf(12));
			break;
		case YEARS:
			defaultAnnualNominalInterestRate = command
					.getInterestRatePerPeriod().multiply(BigDecimal.valueOf(1));
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
		
		LoanProductRelatedDetail loanRepaymentScheduleDetail = new LoanProductRelatedDetail(currency,
				command.getPrincipal(), defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, 
				interestMethod, interestCalculationPeriodMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, command.getInArrearsToleranceAmount(),
				flexibleRepaymentSchedule, interestRebateAllowed);

		LoanSchedule loanSchedule = command.getLoanSchedule();
		List<ScheduledLoanInstallment> loanRepaymentSchedule = loanSchedule.getScheduledLoanInstallments();

		Loan loan = new LoanBuilder()
				.with(currentUser.getOrganisation())
				.with(loanProduct).with(client)
				.with(loanRepaymentScheduleDetail)
				.build();

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
		
		loan.submitApplication(submittedOn, command.getExpectedDisbursementDate(), command.getRepaymentsStartingFromDate(), command.getInterestCalculatedFromDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getSubmittedOnNote())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getSubmittedOnNote());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}

	private boolean isBeforeToday(final LocalDate date) {
		return date.isBefore(new LocalDate());
	}
	
	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = this.loanStatusRepository.findAll();
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
	}
	
	@Transactional
	@Override
	public EntityIdentifier deleteLoan(Long loanId) {
		
		AppUser currentUser = extractAuthenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));
		
		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(loanId), "No loan exists with id: " + loanId);
		}
		
		if (loan.isNotSubmittedAndPendingApproval()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.cannot.delete.loan.in.its.present.state", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse), "Loan must be in pending approval state.");
		}
		
		Long clientId = loan.getClient().getId();
		
		List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
		this.noteRepository.deleteInBatch(relatedNotes);
		
		this.loanRepository.delete(loanId);
		
		return new EntityIdentifier(clientId);
	}

	private List<ErrorResponse> loanIdentifierDoesNotExistError(Long loanId) {
		ErrorResponse errorResponse = new ErrorResponse("error.msg.no.loan.with.identifier.exists", "id", loanId);
		return Arrays.asList(errorResponse);
	}

	@Transactional
	@Override
	public EntityIdentifier approveLoanApplication(final LoanStateTransitionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotApproveLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.approve.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}

		loan.approve(command.getEventDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undoLoanApproval(final UndoLoanApprovalCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));
		
		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		loan.undoApproval(defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		Note note = Note.loanNote(currentUser.getOrganisation(), loan, "Undo of approval.");
		this.noteRepository.save(note);

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier rejectLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = (AppUser) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotRejectLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.reject.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}
		
		loan.reject(command.getEventDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier withdrawLoan(final LoanStateTransitionCommand command) {
		AppUser currentUser = extractAuthenticatedUser();

		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotWithdrawByClientLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.withdraw.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}
		
		loan.withdraw(command.getEventDate(), defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);

		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier disburseLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotDisburseLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.disburse.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}
		
		LocalDate disbursedOn = command.getEventDate();
		String comment = command.getComment();

		LocalDate actualDisbursementDate = new LocalDate(disbursedOn);
		
		if (loan.isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate)) {
			
			LocalDate repaymentsStartingFromDate = loan.getExpectedFirstRepaymentOnDate();
			LocalDate interestCalculatedFromDate = loan.getInterestCalculatedFromDate();

			Number principalAsDecimal = loan.getLoanRepaymentScheduleDetail().getPrincipal().getAmount();
			String currencyCode = loan.getLoanRepaymentScheduleDetail().getPrincipal().getCurrencyCode();
			int currencyDigits = loan.getLoanRepaymentScheduleDetail().getPrincipal().getCurrencyDigitsAfterDecimal();
			
			Number interestRatePerYear = loan.getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate();
			Integer numberOfInstallments = loan.getLoanRepaymentScheduleDetail().getNumberOfRepayments();
			
			Integer repaidEvery = loan.getLoanRepaymentScheduleDetail().getRepayEvery();
			Integer selectedRepaymentFrequency = loan.getLoanRepaymentScheduleDetail().getRepaymentPeriodFrequencyType().getValue();
			Integer selectedRepaymentSchedule = loan.getLoanRepaymentScheduleDetail().getAmortizationMethod().getValue();
			boolean flexibleRepaymentSchedule = loan.isFlexibleRepaymentSchedule();
			
			// use annual percentage rate to re-calculate loan schedule for late disbursement
			Number interestRatePerPeriod = interestRatePerYear;
			Integer interestRateFrequencyMethod = PeriodFrequencyType.YEARS.getValue();
			
			Integer interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod().getValue();
			Integer interestCalculationInPeriod = loan.getLoanRepaymentScheduleDetail().getInterestCalculationPeriodMethod().getValue();
			boolean interestRebateAllowed = loan.getLoanRepaymentScheduleDetail().isInterestRebateAllowed();
			
			CalculateLoanScheduleCommand calculateCommand = new CalculateLoanScheduleCommand(currencyCode, currencyDigits, principalAsDecimal, 
					interestRatePerPeriod, interestRateFrequencyMethod, interestMethod, interestCalculationInPeriod,
					repaidEvery, selectedRepaymentFrequency, numberOfInstallments, 
					selectedRepaymentSchedule, flexibleRepaymentSchedule, interestRebateAllowed, actualDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);

			LoanSchedule loanSchedule = this.calculationPlatformService.calculateLoanSchedule(calculateCommand);

			List<LoanRepaymentScheduleInstallment> modifiedLoanRepaymentSchedule = new ArrayList<LoanRepaymentScheduleInstallment>();
			
			for (ScheduledLoanInstallment scheduledLoanInstallment : loanSchedule
					.getScheduledLoanInstallments()) {
				
				final MonetaryCurrency monetaryCurrency = new MonetaryCurrency(
										scheduledLoanInstallment.getPrincipalDue().getCurrencyCode(), 
										scheduledLoanInstallment.getPrincipalDue().getCurrencyDigitsAfterDecimal());

				Money principal = Money.of(monetaryCurrency,
						scheduledLoanInstallment.getPrincipalDue().getAmount());

				Money interest = Money.of(monetaryCurrency,
						scheduledLoanInstallment.getInterestDue().getAmount());

				LoanRepaymentScheduleInstallment installment = new LoanRepaymentScheduleInstallment(
						loan, scheduledLoanInstallment.getInstallmentNumber(),
						scheduledLoanInstallment.getPeriodStart(),
						scheduledLoanInstallment.getPeriodEnd(), principal.getAmount(),
						interest.getAmount());
				modifiedLoanRepaymentSchedule.add(installment);
			}
			loan.disburseWithModifiedRepaymentSchedule(disbursedOn, comment, modifiedLoanRepaymentSchedule, defaultLoanLifecycleStateMachine());
		} else {
			loan.disburse(disbursedOn, defaultLoanLifecycleStateMachine());
		}

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undloLoanDisbursal(
			final UndoLoanDisbursalCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		if (loan.isActualDisbursedOnDateEarlierOrLaterThanExpected()) {
			// recalculate loan schedule using original settings.
		}

		loan.undoDisbursal(defaultLoanLifecycleStateMachine());

		this.loanRepository.save(loan);

		// TODO - this may not be wanted.
		Note note = Note.loanNote(currentUser.getOrganisation(), loan, "Undo of disbursal.");
		this.noteRepository.save(note);
		
		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier makeLoanRepayment(final LoanTransactionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();
		
		LoanTransactionValidator validator = new LoanTransactionValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}
		
		if (this.isBeforeToday(command.getPaymentDate()) && currentUser.canNotMakeRepaymentOnLoanInPast()) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission.to.make.repayment.on.loan.in.past", "eventDate");
			throw new ApplicationDomainRuleException(Arrays.asList(errorResponse));
		}

		Money repayment = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getPaymentAmount());

		LoanTransaction loanRepayment = LoanTransaction.repayment(repayment, command.getPaymentDate());
		loan.makeRepayment(loanRepayment, defaultLoanLifecycleStateMachine());
		this.loanTransactionRepository.save(loanRepayment);
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(), loan, loanRepayment, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier adjustLoanTransaction(AdjustLoanTransactionCommand command) {

		AppUser currentUser = extractAuthenticatedUser();

		AdjustLoanTransactionCommandValidator validator = new AdjustLoanTransactionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(
					loanIdentifierDoesNotExistError(command.getLoanId()),
					"No loan exists with id: " + command.getLoanId());
		}

		LoanTransaction transactionToAdjust = this.loanTransactionRepository
				.findOne(command.getRepaymentId());

		Money transactionAmount = Money.of(loan
				.getLoanRepaymentScheduleDetail().getPrincipal()
				.getCurrency(), command.getPaymentAmount());

		// adjustment is only supported for repayments and waivers at present
		LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, command.getPaymentDate());
		if (transactionToAdjust.isWaiver()) {
			newTransactionDetail = LoanTransaction.waiver(transactionAmount, command.getPaymentDate());
		}

		loan.adjustExistingTransaction(transactionToAdjust, newTransactionDetail, defaultLoanLifecycleStateMachine());

		this.loanTransactionRepository.save(newTransactionDetail);

		this.loanRepository.save(loan);

		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(),
					loan, newTransactionDetail, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier waiveLoanAmount(LoanTransactionCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();

		LoanTransactionValidator validator = new LoanTransactionValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));

		if (loan == null) {
			throw new ApplicationDomainRuleException(loanIdentifierDoesNotExistError(command.getLoanId()), "No loan exists with id: " + command.getLoanId());
		}

		Money waived = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getPaymentAmount());

		LoanTransaction waiver = LoanTransaction.waiver(waived, command.getPaymentDate());
		
		loan.waive(waiver, defaultLoanLifecycleStateMachine());
		
		this.loanTransactionRepository.save(waiver);
		
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(), loan, waiver, command.getComment());
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier addClientNote(NoteCommand command) {
		
		AppUser currentUser = extractAuthenticatedUser();
		
		Client clientForUpdate = this.clientRepository.findOne(command.getClientId());
		
		Note note = Note.clientNote(currentUser.getOrganisation(), clientForUpdate, command.getNote());
		
		this.noteRepository.save(note);
		
		return new EntityIdentifier(note.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier updateNote(NoteCommand command) {
		
		Note noteForUpdate = this.noteRepository.findOne(command.getId());
		
		noteForUpdate.update(command.getNote());
		
		return new EntityIdentifier(noteForUpdate.getId());
	}
}