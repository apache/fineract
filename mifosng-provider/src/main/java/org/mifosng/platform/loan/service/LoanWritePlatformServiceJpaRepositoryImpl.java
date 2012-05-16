package org.mifosng.platform.loan.service;

import static org.mifosng.platform.Specifications.loansThatMatch;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.LoanSchedule;
import org.mifosng.data.MoneyData;
import org.mifosng.data.ScheduledLoanInstallment;
import org.mifosng.data.command.SubmitLoanApplicationCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.exceptions.PlatformDomainRuleException;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.DefaultLoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanLifecycleStateMachine;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.LoanRepaymentScheduleInstallment;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.loan.domain.LoanStatus;
import org.mifosng.platform.loan.domain.LoanStatusRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanWritePlatformServiceJpaRepositoryImpl implements LoanWritePlatformService {

	private final PlatformSecurityContext context;
	private final LoanRepository loanRepository;
	private final LoanProductRepository loanProductRepository;
	private final LoanStatusRepository loanStatusRepository;
	private final ClientRepository clientRepository;
	private final NoteRepository noteRepository;
	
	private final AprCalculator aprCalculator = new AprCalculator();
	
	@Autowired
	public LoanWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, 
			final LoanRepository loanRepository, final LoanStatusRepository loanStatusRepository, final LoanProductRepository loanProductRepository, 
			final ClientRepository clientRepository, final NoteRepository noteRepository) {
		this.context = context;
		this.loanRepository = loanRepository;
		this.loanProductRepository = loanProductRepository;
		this.clientRepository = clientRepository;
		this.loanStatusRepository = loanStatusRepository;
		this.noteRepository = noteRepository;
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

		LocalDate submittedOn = command.getSubmittedOnDate();
		if (this.isBeforeToday(submittedOn) && currentUser.hasNotPermissionForAnyOf("CAN_SUBMIT_HISTORIC_LOAN_APPLICATION_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE")) {
			throw new NoAuthorizationException("Cannot add backdated loan.");
		}

		// FIXME - looks like assembly of LoanProductRelatedDetail is common with loan product and calculate loan schedule areas. remove duplication into an assembler.
		LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		Client client = this.clientRepository.findOne(command.getApplicantId());

		MonetaryCurrency currency = new MonetaryCurrency(command.getCurrencyCode(), command.getDigitsAfterDecimal());
		
		final BigDecimal defaultNominalInterestRatePerPeriod = BigDecimal.valueOf(command.getInterestRatePerPeriod().doubleValue());
		final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyMethod());
		
		BigDecimal defaultAnnualNominalInterestRate = aprCalculator.calculateFrom(interestPeriodFrequencyType, command.getInterestRatePerPeriod());
		
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestMethod());
		final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodMethod());
		
		final Integer repayEvery = command.getRepaymentEvery();
		final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequency());
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

		Loan loan = Loan.createNew(currentUser.getOrganisation(), loanProduct, client, loanRepaymentScheduleDetail);
		
		for (ScheduledLoanInstallment scheduledLoanInstallment : loanRepaymentSchedule) {

			MoneyData readPrincipalDue = scheduledLoanInstallment.getPrincipalDue();
			MoneyData readInterestDue = scheduledLoanInstallment.getInterestDue();

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
	
	@Transactional
	@Override
	public EntityIdentifier deleteLoan(Long loanId) {
		
		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(currentUser.getOrganisation(), loanId));
		if (loan == null) {
			throw new PlatformResourceNotFoundException("error.msg.loan.id.invalid", "Loan with identifier {0} does not exist", loanId);
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
}