package org.mifosng.platform.loan.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.api.commands.LoanApplicationCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.LoanSchedule;
import org.mifosng.platform.api.data.ScheduledLoanInstallment;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.LoanNotFoundException;
import org.mifosng.platform.exceptions.LoanNotInSubmittedAndPendingApprovalStateCannotBeDeleted;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.exceptions.LoanTransactionNotFoundException;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.fund.domain.Fund;
import org.mifosng.platform.loan.domain.DefaultLoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanCharge;
import org.mifosng.platform.loan.domain.LoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.LoanRepaymentScheduleInstallment;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanTransaction;
import org.mifosng.platform.loan.domain.LoanTransactionProcessingStrategy;
import org.mifosng.platform.loan.domain.LoanTransactionRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.staff.domain.Staff;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanRepository loanRepository;
	private final NoteRepository noteRepository;
	private final CalculationPlatformService calculationPlatformService;	
	private final LoanTransactionRepository loanTransactionRepository;
	private final LoanAssembler loanAssembler;
	private final ClientRepository clientRepository;
	private final LoanProductRepository loanProductRepository;
	
	@Autowired
	public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final LoanAssembler loanAssembler,
			final LoanRepository loanRepository, final LoanTransactionRepository loanTransactionRepository,
			final NoteRepository noteRepository, final CalculationPlatformService calculationPlatformService,
			final ClientRepository clientRepository, final LoanProductRepository loanProductRepository) {
		this.context = context;
		this.loanAssembler = loanAssembler;
		this.loanRepository = loanRepository;
		this.loanTransactionRepository = loanTransactionRepository;
		this.noteRepository = noteRepository;
		this.calculationPlatformService = calculationPlatformService;
		this.clientRepository = clientRepository;
		this.loanProductRepository = loanProductRepository;
	}
	
	private boolean isBeforeToday(final LocalDate date) {
		return date.isBefore(new LocalDate());
	}
	
	private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
		List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
		return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
	}
	
	@Transactional
	@Override
	public EntityIdentifier submitLoanApplication(final LoanApplicationCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanApplicationCommandValidator validator = new LoanApplicationCommandValidator(command);
		validator.validate();

		LocalDate submittedOn = command.getSubmittedOnDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE")) {
			throw new NoAuthorizationException("Cannot add backdated loan.");
		}

		Loan loan = loanAssembler.assembleFrom(command);

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getSubmittedOnNote())) {
			Note note = Note.loanNote(loan, command.getSubmittedOnNote());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier modifyLoanApplication(final LoanApplicationCommand command) {
		AppUser currentUser = context.authenticatedUser();
		
		LoanApplicationCommandValidator validator = new LoanApplicationCommandValidator(command);
		validator.validate();

		// TODO - fix up permissions for loan modification
		LocalDate submittedOn = command.getSubmittedOnDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE")) {
			throw new NoAuthorizationException("Cannot modify backdated loan.");
		}

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		if (loanProduct == null) {
			throw new LoanProductNotFoundException(command.getProductId());
		}

		Client client = this.clientRepository.findOne(command.getClientId());
		if (client == null || client.isDeleted()) {
			throw new ClientNotFoundException(command.getClientId());
		}
		
		Fund fund = this.loanAssembler.findFundByIdIfProvided(command.getFundId());
		Staff loanOfficer = this.loanAssembler.findLoanOfficerByIdIfProvided(command.getLoanOfficerId());
		LoanTransactionProcessingStrategy strategy = this.loanAssembler.findStrategyByIdIfProvided(command.getTransactionProcessingStrategyId());
        Set<LoanCharge> charges = this.loanAssembler.assembleSetOfCharges(command, loan, loanProduct) ;

		LoanSchedule loanSchedule = this.calculationPlatformService.calculateLoanSchedule(command.toCalculateLoanScheduleCommand());
		loan.modifyLoanApplication(command, client, loanProduct, fund, strategy, loanSchedule, charges, loanOfficer);

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(command.getSubmittedOnNote())) {
			Note note = Note.loanNote(loan, command.getSubmittedOnNote());
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier deleteLoan(final Long loanId) {
		
		context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loanId);
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

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LocalDate eventDate = command.getEventDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotApproveLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to approve loan with a date in the past.");
		}
		
		loan.approve(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undoLoanApproval(final UndoStateTransitionCommand command) {

		context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		loan.undoApproval(defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier rejectLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		LocalDate eventDate = command.getEventDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotRejectLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to reject loan with a date in the past.");
		}
		
		loan.reject(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier withdrawLoan(final LoanStateTransitionCommand command) {
		
		AppUser currentUser = context.authenticatedUser();

		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LocalDate eventDate = command.getEventDate();
		if (this.isBeforeToday(eventDate) && currentUser.canNotWithdrawByClientLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to mark loan as withdrawn by client with a date in the past.");
		}
		
		loan.withdraw(eventDate, defaultLoanLifecycleStateMachine());
		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier disburseLoan(final LoanStateTransitionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanStateTransitionCommandValidator validator = new LoanStateTransitionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		String noteText = command.getNote();
		LocalDate actualDisbursementDate = command.getEventDate();
		if (this.isBeforeToday(actualDisbursementDate) && currentUser.canNotDisburseLoanInPast()) {
			throw new NoAuthorizationException("User has no authority to disburse loan with a date in the past.");
		}

		if (loan.isRepaymentScheduleRegenerationRequiredForDisbursement(actualDisbursementDate)) {
			
			LocalDate repaymentsStartingFromDate = loan.getExpectedFirstRepaymentOnDate();
			LocalDate interestCalculatedFromDate = loan.getInterestChargedFromDate();

			BigDecimal principalAsDecimal = loan.repaymentScheduleDetail().getPrincipal().getAmount();
			BigDecimal interestRatePerYear = loan.repaymentScheduleDetail().getAnnualNominalInterestRate();
			Integer numberOfInstallments = loan.repaymentScheduleDetail().getNumberOfRepayments();
			
			Integer repaidEvery = loan.repaymentScheduleDetail().getRepayEvery();
			Integer selectedRepaymentFrequency = loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType().getValue();
			Integer selectedAmortizationMethod = loan.repaymentScheduleDetail().getAmortizationMethod().getValue();
			
			// FIXME - use values from loan table here instead of inferring.
			Integer loanTermFrequency = repaidEvery * numberOfInstallments;
			Integer loanTermFrequencyType = selectedRepaymentFrequency;
			
			// use annual percentage rate to re-calculate loan schedule for late disbursement
			BigDecimal interestRatePerPeriod = interestRatePerYear;
			Integer interestRateFrequencyMethod = PeriodFrequencyType.YEARS.getValue();
			
			Integer interestMethod = loan.repaymentScheduleDetail().getInterestMethod().getValue();
			Integer interestCalculationInPeriod = loan.repaymentScheduleDetail().getInterestCalculationPeriodMethod().getValue();
			
			CalculateLoanScheduleCommand calculateCommand = new CalculateLoanScheduleCommand(
					loan.loanProduct().getId(),
					principalAsDecimal, 
					interestRatePerPeriod, interestRateFrequencyMethod, interestMethod, interestCalculationInPeriod,
					repaidEvery, selectedRepaymentFrequency, numberOfInstallments, selectedAmortizationMethod, 
					loanTermFrequency, loanTermFrequencyType,
					actualDisbursementDate, repaymentsStartingFromDate, interestCalculatedFromDate);

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
//						scheduledLoanInstallment.getPeriodStart(),
						scheduledLoanInstallment.getPeriodEnd(), principal.getAmount(),
						interest.getAmount());
				modifiedLoanRepaymentSchedule.add(installment);
			}
			loan.disburseWithModifiedRepaymentSchedule(actualDisbursementDate, modifiedLoanRepaymentSchedule, defaultLoanLifecycleStateMachine());
		} else {
			loan.disburse(actualDisbursementDate, defaultLoanLifecycleStateMachine());
		}

		this.loanRepository.save(loan);
		
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier undoLoanDisbursal(final UndoStateTransitionCommand command) {

		context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(command.getLoanId());
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
			Note note = Note.loanNote(loan, noteText);
			this.noteRepository.save(note);
		}
		
		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier makeLoanRepayment(final LoanTransactionCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		LocalDate transactionDate = command.getTransactionDate();
		if (this.isBeforeToday(transactionDate) && currentUser.canNotMakeRepaymentOnLoanInPast()) {
			throw new NoAuthorizationException("error.msg.no.permission.to.make.repayment.on.loan.in.past");
		}

		Money repayment = Money.of(loan.repaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getTransactionAmount());

		LoanTransaction loanRepayment = LoanTransaction.repayment(repayment, transactionDate);
		loan.makeRepayment(loanRepayment, defaultLoanLifecycleStateMachine());
		this.loanTransactionRepository.save(loanRepayment);
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(loan, loanRepayment, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier adjustLoanTransaction(final AdjustLoanTransactionCommand command) {

		context.authenticatedUser();

		AdjustLoanTransactionCommandValidator validator = new AdjustLoanTransactionCommandValidator(command);
		validator.validate();

		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}

		LoanTransaction transactionToAdjust = this.loanTransactionRepository.findOne(command.getTransactionId());
		if (transactionToAdjust == null) {
			throw new LoanTransactionNotFoundException(command.getTransactionId());
		}
		
		Money transactionAmount = Money.of(loan
				.repaymentScheduleDetail().getPrincipal()
				.getCurrency(), command.getTransactionAmount());

		// adjustment is only supported for repayments and waivers at present
		LocalDate transactionDate = command.getTransactionDate();
		LoanTransaction newTransactionDetail = LoanTransaction.repayment(transactionAmount, transactionDate);
		if (transactionToAdjust.isWaiver()) {
			newTransactionDetail = LoanTransaction.waiver(transactionAmount, transactionDate);
		}

		loan.adjustExistingTransaction(transactionToAdjust, newTransactionDetail, defaultLoanLifecycleStateMachine());

		this.loanTransactionRepository.save(newTransactionDetail);

		this.loanRepository.save(loan);

		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier waiveLoanAmount(LoanTransactionCommand command) {
		
		context.authenticatedUser();

		LoanTransactionCommandValidator validator = new LoanTransactionCommandValidator(command);
		validator.validate();
		
		Loan loan = this.loanRepository.findOne(command.getLoanId());
		if (loan == null) {
			throw new LoanNotFoundException(command.getLoanId());
		}
		
		Money waived = Money.of(loan.repaymentScheduleDetail()
				.getPrincipal().getCurrency(),
				command.getTransactionAmount());

		LoanTransaction waiver = LoanTransaction.waiver(waived, command.getTransactionDate());
		
		loan.waive(waiver, defaultLoanLifecycleStateMachine());
		
		this.loanTransactionRepository.save(waiver);
		
		this.loanRepository.save(loan);
		
		String noteText = command.getNote();
		if (StringUtils.isNotBlank(noteText)) {
			Note note = Note.loanTransactionNote(loan, waiver, noteText);
			this.noteRepository.save(note);
		}

		return new EntityIdentifier(loan.getId());
	}
}