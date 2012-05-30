package org.mifosng.platform.loan.service;

import static org.mifosng.platform.Specifications.loansThatMatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.LoanNotFoundException;
import org.mifosng.platform.exceptions.LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
import org.mifosng.platform.exceptions.LoanTransactionNotFoundException;
import org.mifosng.platform.exceptions.NoAuthorizationException;
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
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanRepository loanRepository;
	private final LoanStatusRepository loanStatusRepository;
	private final NoteRepository noteRepository;
	private final CalculationPlatformService calculationPlatformService;	
	private final LoanTransactionRepository loanTransactionRepository;
	private final LoanAssembler loanAssembler;
	
	@Autowired
	public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final LoanAssembler loanAssembler,
			final LoanRepository loanRepository, final LoanTransactionRepository loanTransactionRepository,
			final LoanStatusRepository loanStatusRepository, 
			final NoteRepository noteRepository, final CalculationPlatformService calculationPlatformService) {
		this.context = context;
		this.loanAssembler = loanAssembler;
		this.loanRepository = loanRepository;
		this.loanTransactionRepository = loanTransactionRepository;
		this.loanStatusRepository = loanStatusRepository;
		this.noteRepository = noteRepository;
		this.calculationPlatformService = calculationPlatformService;
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
	public EntityIdentifier submitLoanApplication(final SubmitLoanApplicationCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		SubmitLoanApplicationCommandValidator validator = new SubmitLoanApplicationCommandValidator(command);
		validator.validate();

		LocalDate submittedOn = command.getSubmittedOnLocalDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE")) {
			throw new NoAuthorizationException("Cannot add backdated loan.");
		}

		Loan loan = loanAssembler.assembleFrom(command, currentUser.getOrganisation());

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getSubmittedOnNote())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getSubmittedOnNote());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier deleteLoan(Long loanId) {
		
		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), loanId));
		if (loan == null) {
			throw new LoanNotFoundException(loanId);
		}
		
		if (loan.isNotSubmittedAndPendingApproval()) {
			throw new LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted(loanId);
		}
		
		List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
		this.noteRepository.deleteInBatch(relatedNotes);
		
		this.loanRepository.delete(loanId);
		
		return new EntityIdentifier(loanId);
	}
	
	@Transactional
	@Override
	public EntityIdentifier approveLoanApplication(final LoanStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LocalDate eventDate = command.getEventLocalDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotApproveLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to approve loan with a date in the past.");
		}
		
		loan.approve(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undoLoanApproval(final UndoStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		loan.undoApproval(defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier rejectLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		LocalDate eventDate = command.getEventLocalDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotRejectLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to reject loan with a date in the past.");
		}
		
		loan.reject(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier withdrawLoan(final LoanStateTransitionCommand command) {
		
		AppUser currentUser = context.authenticatedUser();

		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LocalDate eventDate = command.getEventLocalDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotWithdrawByClientLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to mark loan as withdrawn by client with a date in the past.");
		}
		
		loan.withdraw(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getClient().getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier disburseLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		String noteText = command.getNote();
		LocalDate actualDisbursementDate = command.getEventLocalDate();
		if (this.isBeforeToday(actualDisbursementDate) && currentUser.canNotDisburseLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to disburse loan with a date in the past.");
		}

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
										scheduledLoanInstallment.getPrincipalDue().getDigitsAfterDecimal());

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
			loan.disburseWithModifiedRepaymentSchedule(actualDisbursementDate, noteText, modifiedLoanRepaymentSchedule, defaultLoanLifecycleStateMachine());
		} else {
			loan.disburse(actualDisbursementDate, defaultLoanLifecycleStateMachine());
		}

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undoLoanDisbursal(final UndoStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		if (loan.isActualDisbursedOnDateEarlierOrLaterThanExpected()) {
			// FIXME - handle this use case - recalculate loan schedule using original settings.
		}

		loan.undoDisbursal(defaultLoanLifecycleStateMachine());

		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getClient().getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier makeLoanRepayment(final LoanTransactionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LocalDate transactionDate = command.getTransactionLocalDate();
		if (this.isBeforeToday(transactionDate) && currentUser.canNotMakeRepaymentOnLoanInPast()) {
			throw new NoAuthorizationException("error.msg.no.permission.to.make.repayment.on.loan.in.past");
		}

		Money repayment = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getTransactionAmountValue());

		LoanTransaction loanRepayment = LoanTransaction.repayment(repayment, transactionDate);
		loan.makeRepayment(loanRepayment, defaultLoanLifecycleStateMachine());
		this.loanTransactionRepository.save(loanRepayment);
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(), loan, loanRepayment, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}

	@Transactional
	@Override
	public EntityIdentifier adjustLoanTransaction(AdjustLoanTransactionCommand command) {

		AppUser currentUser = context.authenticatedUser();

		AdjustLoanTransactionCommandValidator validator = new AdjustLoanTransactionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(command.getTransactionId());
		if (transactionToAdjust == null) {
			throw new LoanTransactionNotFoundException(command.getTransactionId());
		}
		
		Money transactionAmount = Money.of(loan
				.getLoanRepaymentScheduleDetail().getPrincipal()
				.getCurrency(), command.getTransactionAmountValue());

		// adjustment is only supported for repayments and waivers at present
		LocalDate transactionDate = command.getTransactionLocalDate();
		LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, transactionDate);
		if (transactionToAdjust.isWaiver()) {
			newTransactionDetail = LoanTransaction.waiver(transactionAmount, transactionDate);
		}

		loan.adjustExistingTransaction(transactionToAdjust, newTransactionDetail, defaultLoanLifecycleStateMachine());

		this.loanTransactionRepository.save(newTransactionDetail);

		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(),
					loan, newTransactionDetail, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier waiveLoanAmount(LoanTransactionCommand command) {
		
		AppUser currentUser = context.authenticatedUser();

		LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		Money waived = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getTransactionAmountValue());

		LoanTransaction waiver = LoanTransaction.waiver(waived, command.getTransactionLocalDate());
		
		loan.waive(waiver, defaultLoanLifecycleStateMachine());
		
		this.loanTransactionRepository.save(waiver);
		
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(currentUser.getOrganisation(), loan, waiver, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getClient().getId());
	}
}