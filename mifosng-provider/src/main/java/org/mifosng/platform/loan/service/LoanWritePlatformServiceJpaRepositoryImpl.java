package org.mifosng.platform.loan.service;

import static org.mifosng.platform.Specifications.loansThatMatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.CalculateLoanScheduleCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.data.command.UndoStateTransitionCommand;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.LoanNotFoundException;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.exceptions.PlatformDomainRuleException;
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
			throw new PlatformDomainRuleException("error.msg.cannot.delete.loan.in.its.present.state", "Loan with identifier {0} cannot be deleted in its current state.", loanId);
		}
		
		Long clientId = loan.getClient().getId();
		
		List<Note> relatedNotes = this.noteRepository.findByLoanId(loan.getId());
		this.noteRepository.deleteInBatch(relatedNotes);
		
		this.loanRepository.delete(loanId);
		
		return new EntityIdentifier(clientId);
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

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotApproveLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to approve loan with a date in the past.");
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
	public EntityIdentifier undoLoanApproval(final UndoStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		loan.undoApproval(defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
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

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotRejectLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to reject loan with a date in the past.");
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
		
		AppUser currentUser = context.authenticatedUser();

		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotWithdrawByClientLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to mark loan as withdrawn by client with a date in the past.");
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

		AppUser currentUser = context.authenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		if (this.isBeforeToday(command.getEventDate()) && currentUser.canNotDisburseLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to disburse loan with a date in the past.");
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

		if (StringUtils.isNotBlank(command.getComment())) {
			Note note = Note.loanNote(currentUser.getOrganisation(), loan, command.getComment());
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
		
		if (this.isBeforeToday(command.getTransactionDate()) && currentUser.canNotMakeRepaymentOnLoanInPast()) {
			throw new NoAuthorizationException("error.msg.no.permission.to.make.repayment.on.loan.in.past");
		}

		Money repayment = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getTransactionAmount());

		LoanTransaction loanRepayment = LoanTransaction.repayment(repayment, command.getTransactionDate());
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

		AppUser currentUser = context.authenticatedUser();

		AdjustLoanTransactionCommandValidator validator = new AdjustLoanTransactionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		LoanTransaction transactionToAdjust = this.loanTransactionRepository
				.findOne(command.getRepaymentId());

		Money transactionAmount = Money.of(loan
				.getLoanRepaymentScheduleDetail().getPrincipal()
				.getCurrency(), command.getTransactionAmount());

		// adjustment is only supported for repayments and waivers at present
		LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, command.getTransactionDate());
		if (transactionToAdjust.isWaiver()) {
			newTransactionDetail = LoanTransaction.waiver(transactionAmount, command.getTransactionDate());
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
		
		AppUser currentUser = context.authenticatedUser();

		LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), command.getLoanId()));
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		Money waived = Money.of(loan.getLoanRepaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getTransactionAmount());

		LoanTransaction waiver = LoanTransaction.waiver(waived, command.getTransactionDate());
		
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