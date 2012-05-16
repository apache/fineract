package org.mifosng.platform;

import static org.mifosng.platform.Specifications.loansThatMatch;
import static org.mifosng.platform.Specifications.usersThatMatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.UndoLoanApprovalCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
import org.mifosng.data.command.UpdateUsernamePasswordCommand;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.infrastructure.BasicPasswordEncodablePlatformUser;
import org.mifosng.platform.infrastructure.PlatformPasswordEncoder;
import org.mifosng.platform.infrastructure.PlatformUser;
import org.mifosng.platform.infrastructure.UsernameAlreadyExistsException;
import org.mifosng.platform.loan.domain.DefaultLoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.LoanRepaymentScheduleInstallment;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanStatusRepository;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loan.service.CalculationPlatformService;
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

	private final PlatformUserRepository platformUserRepository;
	private final PlatformPasswordEncoder platformPasswordEncoder;
	private final LoanRepository loanRepository;
	private final LoanStatusRepository loanStatusRepository;
	private final CalculationPlatformService calculationPlatformService;
	private final NoteRepository noteRepository;
	private final LoanTransactionRepository loanTransactionRepository;

	@Autowired
	public WritePlatformServiceJpaRepositoryImpl(
			final PlatformUserRepository platformUserRepository,
			final PlatformPasswordEncoder platformPasswordEncoder,
			final NoteRepository noteRepository,
			final LoanRepository loanRepository,
			final LoanTransactionRepository loanTransactionRepository,
			final LoanStatusRepository loanStatusRepository,
			final CalculationPlatformService calculationPlatformService
			) {
		this.platformUserRepository = platformUserRepository;
		this.platformPasswordEncoder = platformPasswordEncoder;
		this.noteRepository = noteRepository;
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

	private boolean isBeforeToday(final LocalDate date) {
		return date.isBefore(new LocalDate());
	}
	
	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = this.loanStatusRepository.findAll();
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
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
			
			// use annual percentage rate to re-calculate loan schedule for late disbursement
			Number interestRatePerPeriod = interestRatePerYear;
			Integer interestRateFrequencyMethod = PeriodFrequencyType.YEARS.getValue();
			
			Integer interestMethod = loan.getLoanRepaymentScheduleDetail().getInterestMethod().getValue();
			Integer interestCalculationInPeriod = loan.getLoanRepaymentScheduleDetail().getInterestCalculationPeriodMethod().getValue();
			
			CalculateLoanScheduleCommand calculateCommand = new CalculateLoanScheduleCommand(currencyCode, currencyDigits, principalAsDecimal, 
					interestRatePerPeriod, interestRateFrequencyMethod, interestMethod, interestCalculationInPeriod,
					repaidEvery, selectedRepaymentFrequency, numberOfInstallments, 
					selectedRepaymentSchedule, actualDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);

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
}