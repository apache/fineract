package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.DerivedLoanData;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "portfolio_loan", uniqueConstraints = @UniqueConstraint(columnNames = {"org_id", "external_id" }))
public class Loan extends AbstractAuditableCustom<AppUser, Long> {

	@ManyToOne
	@JoinColumn(name = "org_id", nullable = false)
	private final Organisation organisation;

	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private final Client client;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private final LoanProduct loanProduct;

	@Column(name = "external_id")
	private String externalId;

	@Embedded
	private final LoanProductRelatedDetail loanRepaymentScheduleDetail;

	@ManyToOne
	@JoinColumn(name = "loan_status_id", nullable = false)
	private LoanStatus loanStatus;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "submittedon_date")
	private Date submittedOnDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "rejectedon_date")
	private Date rejectedOnDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "withdrawnon_date")
	private Date withdrawnOnDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "approvedon_date")
	private Date approvedOnDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "expected_disbursedon_date")
	private Date expectedDisbursedOnDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "expected_firstrepaymenton_date")
	private Date expectedFirstRepaymentOnDate;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "interest_calculated_from_date")
	private Date interestCalculatedFromDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "disbursedon_date")
	private Date disbursedOnDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "closedon_date")
	private Date closedOnDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "writtenoffon_date")
	private Date writtenOffOnDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "rescheduledon_date")
	private Date rescheduledOnDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "expected_maturedon_date")
	private Date expectedMaturityDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "maturedon_date")
	private Date maturedOnDate;

	// see
	// http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
	private final List<LoanRepaymentScheduleInstallment> repaymentScheduleInstallments = new ArrayList<LoanRepaymentScheduleInstallment>();

	// see
	// http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
	@OrderBy(value = "dateOf, id")
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "loan", orphanRemoval = true)
	private final List<LoanTransaction> loanTransactions = new ArrayList<LoanTransaction>();

	@Column(name = "interest_rebate_amount", scale = 6, precision = 19)
	private BigDecimal interestRebateOwed;

	@Transient
	private final InterestRebateCalculatorFactory interestRebateCalculatorFactory = new DailyEquivalentInterestRebateCalculatorFactory();

	public Loan() {
		this.organisation = null;
		this.client = null;
		this.loanProduct = null;
		this.loanRepaymentScheduleDetail = null;
	}

	public Loan(final Organisation organisation, final Client client,
			final LoanProduct loanProduct,
			final LoanProductRelatedDetail loanRepaymentScheduleDetail,
			final LoanStatus loanStatus) {
		this.organisation = organisation;
		this.client = client;
		this.loanProduct = loanProduct;
		this.loanRepaymentScheduleDetail = loanRepaymentScheduleDetail;
		this.loanStatus = loanStatus;
		this.interestRebateOwed = BigDecimal.ZERO;
	}

	public Organisation getOrganisation() {
		return this.organisation;
	}

	public Client getClient() {
		return this.client;
	}

	public LoanProduct getLoanProduct() {
		return this.loanProduct;
	}

	public LoanProductRelatedDetail getLoanRepaymentScheduleDetail() {
		return this.loanRepaymentScheduleDetail;
	}

	public void submitApplication(final LocalDate submittedOn, final LocalDate expectedDisbursementDate, 
			LocalDate repaymentsStartingFromDate, LocalDate interestCalculatedFromDate, LoanLifecycleStateMachine lifecycleStateMachine) {
		
		this.loanStatus = lifecycleStateMachine.transition(LoanEvent.LOAN_CREATED, this.loanStatus);
		
		this.submittedOnDate = submittedOn.toDateTimeAtCurrentTime().toDate();

		this.expectedMaturityDate = this.repaymentScheduleInstallments
				.get(this.repaymentScheduleInstallments.size() - 1)
				.getDueDate().toDateMidnight().toDate();
		if (expectedDisbursementDate != null) {
			// can be null during bulk upload of loans
			this.expectedDisbursedOnDate = expectedDisbursementDate.toDateMidnight().toDate();
		}
		
		if (repaymentsStartingFromDate != null) {
			this.expectedFirstRepaymentOnDate = repaymentsStartingFromDate.toDateMidnight().toDate();
		}
		
		if (interestCalculatedFromDate != null) {
			this.interestCalculatedFromDate = interestCalculatedFromDate.toDateMidnight().toDate();
		}
		
		if (new LocalDate(this.submittedOnDate).isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The loan application submit date cannot be in the future.",
					"invalid.submit.date.as.date.is.in.future");
		}
		if (new LocalDate(this.submittedOnDate).isAfter(new LocalDate(
				this.expectedDisbursedOnDate))) {
			throw new InvalidLoanTimelineDate(
					"The loan application submit date cannot be after its expected disbursement date.",
					"invalid.submit.date.as.date.is.after.expected.disbursement.date");
		}
	}

	public void reject(final LocalDate rejectedOn, LoanLifecycleStateMachine loanLifecycleStateMachine) {
		
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REJECTED, this.loanStatus);
		
		this.rejectedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();
		this.closedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();
		if (new LocalDate(this.rejectedOnDate).isBefore(new LocalDate(
				this.submittedOnDate))) {
			throw new InvalidLoanTimelineDate(
					"The loan rejection date cannot be before its submittal date.",
					"invalid.rejection.date.as.date.is.before.submittal.date");
		}
		if (new LocalDate(this.rejectedOnDate).isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The loan application rejection date cannot be in the future.",
					"invalid.rejection.date.as.date.is.in.future");
		}
	}

	public void withdraw(final LocalDate withdrawnOn, LoanLifecycleStateMachine loanLifecycleStateMachine) {
		
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_WITHDRAWN, this.loanStatus);
		
		this.withdrawnOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();
		this.closedOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();
		if (new LocalDate(this.withdrawnOnDate).isBefore(new LocalDate(
				this.submittedOnDate))) {
			throw new InvalidLoanTimelineDate(
					"The date of when loan is withdrawn cannot be before its submittal date.",
					"invalid.withdrawal.date.as.date.is.before.submittal.date");
		}
		if (new LocalDate(this.withdrawnOnDate).isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The loan application withdraw date cannot be in the future.",
					"invalid.withdrawal.date.as.date.is.in.future");
		}
	}

	public void approve(final LocalDate approvedOn, LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVED, this.loanStatus);
		this.approvedOnDate = approvedOn.toDateTimeAtCurrentTime().toDate();
		if (new LocalDate(this.approvedOnDate).isBefore(new LocalDate(
				this.submittedOnDate))) {
			throw new InvalidLoanTimelineDate(
					"The date of when loan is approved cannot be before its submittal date.",
					"invalid.approval.date.as.date.is.before.submittal.date");
		}
		if (new LocalDate(this.approvedOnDate).isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The date of when loan is approved cannot be in the future.",
					"invalid.approval.date.as.date.is.in.future");
		}
	}

	public void undoApproval(LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_APPROVAL_UNDO, this.loanStatus);
		this.approvedOnDate = null;
	}

	public void disburseWithModifiedRepaymentSchedule(
			final LocalDate disbursedOn,
			final String comment,
			final List<LoanRepaymentScheduleInstallment> modifiedLoanRepaymentSchedule, LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.repaymentScheduleInstallments.clear();
		for (LoanRepaymentScheduleInstallment modifiedInstallment : modifiedLoanRepaymentSchedule) {
			modifiedInstallment.updateOrgnaisation(this.organisation);
			modifiedInstallment.updateLoan(this);
			this.repaymentScheduleInstallments.add(modifiedInstallment);
		}
		disburse(disbursedOn, loanLifecycleStateMachine);
	}

	public void disburse(final LocalDate disbursedOn, LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSED, this.loanStatus);
		this.disbursedOnDate = disbursedOn.toDateTimeAtCurrentTime().toDate();
		this.expectedMaturityDate = this.repaymentScheduleInstallments
				.get(this.repaymentScheduleInstallments.size() - 1)
				.getDueDate().toDateMidnight().toDate();

		LoanTransaction loanTransaction = LoanTransaction.disbursement(
				this.loanRepaymentScheduleDetail.getPrincipal(), disbursedOn);
		loanTransaction.updateLoan(this);
		loanTransaction.updateOrganisation(organisation);
		this.loanTransactions.add(loanTransaction);

		if (disbursedOn.isBefore(new LocalDate(this.approvedOnDate))) {
			throw new InvalidLoanTimelineDate(
					"The date of when loan is disbursed cannot be before its approval date.",
					"invalid.disbursal.as.disbursement.date.is.before.approved.date");
		}
		if (disbursedOn.isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The date of when loan is disbursed cannot be in the future.",
					"invalid.disbursal.as.disbursement.date.is.in.future");
		}
		LocalDate firstRepaymentDueDate = this.repaymentScheduleInstallments
				.get(0).getDueDate();
		if (disbursedOn.isAfter(firstRepaymentDueDate)) {
			throw new InvalidLoanTimelineDate(
					"The date of when loan is disbursed cannot be after the first expected repayment.",
					"invalid.disbursal.as.disbursement.date.is.after.first.repayment.due.date");
		}
	}

	public void undoDisbursal(LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_DISBURSAL_UNDO, this.loanStatus);
		this.loanTransactions.clear();
		this.disbursedOnDate = null;
	}

	public void waive(LoanTransaction loanTransaction, LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT, this.loanStatus);
		loanTransaction.updateLoan(this);
		loanTransaction.updateOrganisation(organisation);
		this.loanTransactions.add(loanTransaction);

		if (loanTransaction.getTransactionDate().isBefore(
				this.getDisbursementDate())) {
			throw new InvalidLoanTimelineDate(
					"The repayment date cannot be before the loan disbursement date.",
					"invalid.repayment.date.as.date.is.before.disbursement.date");
		}
		if (loanTransaction.getTransactionDate().isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The repayment date cannot be in the future.",
					"invalid.repayment.date.as.date.is.in.future");
		}

		if (this.isRepaidInFull()) {
			this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, this.loanStatus);
			this.closedOnDate = loanTransaction.getTransactionDate().toDate();
			this.maturedOnDate = loanTransaction.getTransactionDate().toDate();
			
			if (isInterestRebateAllowed()) {
				Money rebateDue = calculateRebateWhenPaidInFullOn(loanTransaction
						.getTransactionDate());
				if (rebateDue.isGreaterThanZero()) {
					this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.INTERST_REBATE_OWED, this.loanStatus);
					this.interestRebateOwed = rebateDue.getAmount();
				}
			}
		}
	}

	public void makeRepayment(final LoanTransaction loanTransaction, LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_REPAYMENT, this.loanStatus);
		loanTransaction.updateLoan(this);
		loanTransaction.updateOrganisation(organisation);
		this.loanTransactions.add(loanTransaction);
		
		deriveLoanRepaymentScheduleCompletedData();

		if (loanTransaction.isNotRepayment()) {
			throw new IllegalArgumentException(
					"Only repayment transactions can be passed to makeRepayment.");
		}

		if (loanTransaction.getTransactionDate().isBefore(
				this.getDisbursementDate())) {
			throw new InvalidLoanTimelineDate(
					"The repayment date cannot be before the loan disbursement date.",
					"invalid.repayment.date.as.date.is.before.disbursement.date");
		}
		if (loanTransaction.getTransactionDate().isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The repayment date cannot be in the future.",
					"invalid.repayment.date.as.date.is.in.future");
		}
		if (this.isRepaidInFull()) {
			this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.REPAID_IN_FULL, this.loanStatus);
			this.closedOnDate = loanTransaction.getTransactionDate().toDate();
			this.maturedOnDate = loanTransaction.getTransactionDate().toDate();
			
			if (isInterestRebateAllowed()) {
				Money rebateDue = calculateRebateWhenPaidInFullOn(loanTransaction
						.getTransactionDate());
				if (rebateDue.isGreaterThanZero()) {
					this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.INTERST_REBATE_OWED, this.loanStatus);
					this.interestRebateOwed = rebateDue.getAmount();
				}
			}
		}
	}

	private void deriveLoanRepaymentScheduleCompletedData() {
		
		Money totalRepaidOrWaivedAgainstLoan = calculateTotalPaidOrWaived();
		
		Money remainingToPayoffAgainstLoanSchedule = totalRepaidOrWaivedAgainstLoan;
		Money totalOverpaid = Money.zero(totalRepaidOrWaivedAgainstLoan.getCurrency());
		int repaymentInstallmentIndex = 0;
		while (remainingToPayoffAgainstLoanSchedule.isGreaterThanZero()) {
			
			if (repaymentInstallmentIndex == this.repaymentScheduleInstallments.size()) {
				totalOverpaid = remainingToPayoffAgainstLoanSchedule;
				
				// to exit while loop
				remainingToPayoffAgainstLoanSchedule = remainingToPayoffAgainstLoanSchedule.minus(totalOverpaid);
			} else {
				LoanRepaymentScheduleInstallment scheduledRepaymentInstallment = this.repaymentScheduleInstallments.get(repaymentInstallmentIndex);
				remainingToPayoffAgainstLoanSchedule = scheduledRepaymentInstallment.updateDerivedComponents(remainingToPayoffAgainstLoanSchedule);
				repaymentInstallmentIndex++;
			}
		}
	}

	private Money calculateTotalPaidOrWaived() {
		
		Money totalRepaidOrWaived = Money.zero(this.loanRepaymentScheduleDetail.getPrincipal().getCurrency());
		
		for (LoanTransaction transaction : this.loanTransactions) {
			if (transaction.isRepayment() || transaction.isWaiver()) {
				totalRepaidOrWaived = totalRepaidOrWaived.plus(transaction.getAmount());
			}
		}
		
		return totalRepaidOrWaived;
	}
	
	public LocalDate possibleNextRepaymentDate() {
		LocalDate earliestUnpaidInstallmentDate = new LocalDate();
		for (LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
			if (installment.unpaid()) {
				earliestUnpaidInstallmentDate = installment.getDueDate();
				break;
			}
		}
		
		LocalDate lastTransactionDate = null; 
		for (LoanTransaction transaction : this.loanTransactions) {
			if (transaction.isRepayment() && transaction.isNonZero()) {
				lastTransactionDate = transaction.getTransactionDate();
			}
		}
		
		LocalDate possibleNextRepaymentDate = earliestUnpaidInstallmentDate;
		if (lastTransactionDate != null && lastTransactionDate.isAfter(earliestUnpaidInstallmentDate)) {
			possibleNextRepaymentDate = lastTransactionDate;
		}
		
		return possibleNextRepaymentDate;
	}
	
	public Money possibleNextRepaymentAmount() {
		MonetaryCurrency currency = this.loanRepaymentScheduleDetail.getPrincipal().getCurrency();
		Money possibleNextRepaymentAmount = Money.zero(currency);
		
		for (LoanRepaymentScheduleInstallment installment : this.repaymentScheduleInstallments) {
			if (installment.unpaid()) {
				possibleNextRepaymentAmount = installment.getTotalDue(currency);
				break;
			}
		}
		
		return possibleNextRepaymentAmount;
	}

	public void adjustExistingTransaction(LoanTransaction transactionForAdjustment,
			LoanTransaction newTransactionDetail, LoanLifecycleStateMachine loanLifecycleStateMachine) {

		if (transactionForAdjustment.isNotRepayment() && transactionForAdjustment.isNotWaiver()) {
			throw new IllegalArgumentException("Only repayment and waiver transactions can be adjusted.");
		}

		transactionForAdjustment.contra();
		if (newTransactionDetail.isRepayment()) {
			makeRepayment(newTransactionDetail, loanLifecycleStateMachine);
		}
		
		if (newTransactionDetail.isWaiver()) {
			waive(newTransactionDetail, loanLifecycleStateMachine);
		}
	}

	private boolean isRepaidInFull() {

		Money cumulativePrincipal = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());
		Money cumulativeInterest = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());
		Money cumulativeTotal = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());
		Money cumulativePaid = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());
		Money cumulativeWaived = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());

		for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {
			cumulativePrincipal = cumulativePrincipal.plus(scheduledRepayment.getPrincipal(loanCurrency()));
			cumulativeInterest = cumulativeInterest.plus(scheduledRepayment
					.getInterest(loanCurrency()));
			cumulativeTotal = cumulativeTotal.plus(scheduledRepayment
					.getTotal(loanCurrency()));
		}

		for (LoanTransaction transaction : this.loanTransactions) {
			if (transaction.isRepayment()) {
				cumulativePaid = cumulativePaid.plus(transaction.getAmount());
			}
			if (transaction.isWaiver()) {
				cumulativeWaived = cumulativeWaived.plus(transaction
						.getAmount());
			}
		}

		return cumulativePaid.plus(cumulativeWaived).isGreaterThanOrEqualTo(
				cumulativeTotal);
	}

	private MonetaryCurrency loanCurrency() {
		return this.loanRepaymentScheduleDetail.getCurrency();
	}

	public void writeOff(final DateTime writtenOffOn, final LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_WRITE_OFF, this.loanStatus);
		this.closedOnDate = writtenOffOn.toDate();
		this.writtenOffOnDate = writtenOffOn.toDate();
		if (new LocalDate(this.writtenOffOnDate).isBefore(this
				.getDisbursementDate())) {
			throw new InvalidLoanTimelineDate(
					"The date the loan is written off cannot be before the loan disbursement date.",
					"invalid.writeoff.date.as.date.is.before.disbursement.date");
		}
		if (new LocalDate(this.writtenOffOnDate).isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The date the loan is written off cannot be in the future.",
					"invalid.writeoff.date.as.date.is.in.future");
		}
	}

	public void reschedule(final DateTime rescheduledOn, final LoanLifecycleStateMachine loanLifecycleStateMachine) {
		this.loanStatus = loanLifecycleStateMachine.transition(LoanEvent.LOAN_RESCHEDULE, this.loanStatus);
		this.closedOnDate = rescheduledOn.toDate();
		this.rescheduledOnDate = rescheduledOn.toDate();
		if (new LocalDate(this.rescheduledOnDate).isBefore(this
				.getDisbursementDate())) {
			throw new InvalidLoanTimelineDate(
					"The date the loan is rescheduled cannot be before the loan disbursement date.",
					"invalid.reschedule.date.as.date.is.before.disbursement.date");
		}
		if (new LocalDate(this.rescheduledOnDate).isAfter(new LocalDate())) {
			throw new InvalidLoanTimelineDate(
					"The date the loan is rescheduled cannot be in the future.",
					"invalid.reschedule.date.as.date.is.in.future");
		}
	}

	public boolean isNotSubmittedAndPendingApproval() {
		return !isSubmittedAndPendingApproval();
	}
	
	public boolean isSubmittedAndPendingApproval() {
		return this.loanStatus.isSubmittedAndPendingApproval();
	}

	public boolean isApproved() {
		return this.loanStatus.isApproved();
	}

	public boolean isNotApproved() {
		return !isApproved();
	}

	public boolean isWaitingForDisbursal() {
		return this.isApproved() && this.isNotDisbursed();
	}

	public boolean isNotDisbursed() {
		return !this.isDisbursed();
	}

	public boolean isDisbursed() {
		return hasDisbursementTransaction();
	}

	public boolean isUndoDisbursalAllowed() {
		return isDisbursed() && this.hasNoRepaymentTransaction();
	}

	public boolean isClosed() {
		return this.loanStatus.isClosed() || this.isCancelled();
	}

	public boolean isCancelled() {
		return this.isRejected() || this.isWithdrawn();
	}

	public boolean isWithdrawn() {
		return this.loanStatus.isWithdrawnByClient();
	}

	public boolean isRejected() {
		return this.loanStatus.isRejected();
	}

	public boolean isNotClosed() {
		return !this.isClosed();
	}

	public boolean isOpen() {
		return this.loanStatus.isActive();
	}

	public boolean isOpenWithNoRepaymentMade() {
		return this.isOpen() && hasNoRepaymentTransaction();
	}

	private boolean hasNoRepaymentTransaction() {
		return !hasRepaymentTransaction();
	}

	private boolean hasRepaymentTransaction() {
		boolean hasRepaymentTransaction = false;
		for (LoanTransaction loanTransaction : this.loanTransactions) {
			if (loanTransaction.isRepayment()) {
				hasRepaymentTransaction = true;
				break;
			}
		}
		return hasRepaymentTransaction;
	}

	private boolean hasDisbursementTransaction() {
		boolean hasRepaymentTransaction = false;
		for (LoanTransaction loanTransaction : this.loanTransactions) {
			if (loanTransaction.isDisbursement()) {
				hasRepaymentTransaction = true;
				break;
			}
		}
		return hasRepaymentTransaction;
	}

	public boolean isOpenWithRepaymentMade() {
		return this.isOpen() && this.hasRepaymentTransaction();
	}

	public List<LoanRepaymentScheduleInstallment> getRepaymentScheduleInstallments() {
		return this.repaymentScheduleInstallments;
	}

	public DerivedLoanData deriveLoanData(CurrencyData currencyData) {

		List<LoanTransaction> repaymentTransactions = new ArrayList<LoanTransaction>();
		for (LoanTransaction loanTransaction : this.loanTransactions) {
			if (loanTransaction.isRepayment() || loanTransaction.isWaiver()) {
				repaymentTransactions.add(loanTransaction);
			}
		}

		Money arrearsTolerance = this.loanRepaymentScheduleDetail
				.getInArrearsTolerance();

		return new DerivedLoanDataProcessor().process(
				new ArrayList<LoanRepaymentScheduleInstallment>(
						this.repaymentScheduleInstallments),
				repaymentTransactions, currencyData, arrearsTolerance);
	}

	public String getExternalId() {
		return this.externalId;
	}

	public void setExternalId(final String externalSystemIdentifer) {
		if (StringUtils.isNotBlank(externalSystemIdentifer)) {
			this.externalId = externalSystemIdentifer.trim();
		} else {
			this.externalId = externalSystemIdentifer;
		}
	}

	public LocalDate getSubmittedOnDate() {
		return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(
				this.submittedOnDate), null);
	}

	public LocalDate getRejectedOnDate() {
		return (LocalDate) ObjectUtils.defaultIfNull(new LocalDate(
				this.rejectedOnDate), null);
	}

	public LocalDate getApprovedOnDate() {
		LocalDate date = null;
		if (this.approvedOnDate != null) {
			date = new LocalDate(this.approvedOnDate);
		}
		return date;
	}

	public LocalDate getDisbursedOnDate() {
		LocalDate date = null;
		if (this.disbursedOnDate != null) {
			date = new LocalDate(this.disbursedOnDate);
		}
		return date;
	}

	public LocalDate getClosedOnDate() {
		LocalDate date = null;
		if (this.closedOnDate != null) {
			date = new LocalDate(this.closedOnDate);
		}
		return date;
	}

	public LocalDate getWrittenOffOnDate() {
		LocalDate date = null;
		if (this.writtenOffOnDate != null) {
			date = new LocalDate(this.writtenOffOnDate);
		}
		return date;
	}

	public Date getExpectedDisbursedOnDate() {
		return this.expectedDisbursedOnDate;
	}

	public LocalDate getExpectedFirstRepaymentOnDate() {
		LocalDate firstRepaymentDate = null;
		if (this.expectedFirstRepaymentOnDate != null) {
			firstRepaymentDate = new LocalDate(this.expectedFirstRepaymentOnDate);
		}
		return firstRepaymentDate;
	}

	public LocalDate getDisbursementDate() {
		LocalDate disbursementDate = new LocalDate(this.expectedDisbursedOnDate);
		if (this.disbursedOnDate != null) {
			disbursementDate = new LocalDate(this.disbursedOnDate);
		}
		return disbursementDate;
	}
	
	public LocalDate getExpectedMaturityDate() {
		LocalDate possibleMaturityDate = null;
		if (this.expectedMaturityDate != null) {
			possibleMaturityDate = new LocalDate(this.expectedMaturityDate);
		}
		return possibleMaturityDate;
	}
	
	public LocalDate getActualMaturityDate() {
		LocalDate possibleMaturityDate = null;
		if (this.maturedOnDate != null) {
			possibleMaturityDate = new LocalDate(this.maturedOnDate);
		}
		return possibleMaturityDate;
	}

	public LocalDate getMaturityDate() {
		LocalDate possibleMaturityDate = null;

		if (this.expectedMaturityDate != null) {
			possibleMaturityDate = new LocalDate(this.expectedMaturityDate);
		}
		if (this.maturedOnDate != null) {
			possibleMaturityDate = new LocalDate(this.maturedOnDate);
		}
		return possibleMaturityDate;
	}

	public void addRepaymentScheduleInstallment(final LoanRepaymentScheduleInstallment installment) {
		installment.updateOrgnaisation(this.organisation);
		installment.updateLoan(this);
		this.repaymentScheduleInstallments.add(installment);
	}

	public boolean isActualDisbursedOnDateEarlierOrLaterThanExpected() {
		return isActualDisbursedOnDateEarlierOrLaterThanExpected(new LocalDate(
				this.disbursedOnDate));
	}

	public boolean isActualDisbursedOnDateEarlierOrLaterThanExpected(
			final LocalDate actualDisbursedOnDate) {
		return !new LocalDate(this.expectedDisbursedOnDate)
				.isEqual(actualDisbursedOnDate);
	}

	public boolean isFlexibleRepaymentSchedule() {
		return this.loanProduct.isFlexibleRepaymentSchedule();
	}

	public boolean isInterestRebateAllowed() {
		return this.loanProduct.isInterestRebateAllowed();
	}

	public boolean isRepaymentScheduleRegenerationRequiredForDisbursement(
			final LocalDate actualDisbursementDate) {

		boolean regenerationRequired = false;

		if (isFlexibleRepaymentSchedule()) {
			regenerationRequired = false;
		} else {
			if (isActualDisbursedOnDateEarlierOrLaterThanExpected(actualDisbursementDate)) {
				regenerationRequired = true;
			}
		}

		return regenerationRequired;
	}

	public LoanPayoffSummary getPayoffSummaryOn(
			final LocalDate projectedPayoffDate) {

		LocalDate acutalDisbursementDate = new LocalDate(this.disbursedOnDate);

		Money totalPaidToDate = this.getTotalPaid();

		Money totalOutstandingBasedOnExpectedMaturityDate = this
				.getTotalOutstanding();
		Money totalOutstandingBasedOnPayoffDate = totalOutstandingBasedOnExpectedMaturityDate;

		Money rebateGivenOnProjectedPayoffDate = Money
				.zero(this.loanRepaymentScheduleDetail.getPrincipal()
						.getCurrency());
		if (isInterestRebateAllowed()) {
			rebateGivenOnProjectedPayoffDate = calculateRebateWhenPaidInFullOn(projectedPayoffDate);
			totalOutstandingBasedOnPayoffDate = totalOutstandingBasedOnExpectedMaturityDate
					.minus(rebateGivenOnProjectedPayoffDate);
		}

		return new LoanPayoffSummary(this.getId(), acutalDisbursementDate,
				this.getMaturityDate(), projectedPayoffDate, totalPaidToDate,
				totalOutstandingBasedOnExpectedMaturityDate,
				totalOutstandingBasedOnPayoffDate,
				rebateGivenOnProjectedPayoffDate);
	}

	public Money getTotalOutstanding() {
		return getTotalPrincipalOnLoan().plus(getTotalInterestOnLoan()).minus(
				getTotalPaid());
	}

	private Money getTotalPaid() {
		Money cumulativePaid = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());

		for (LoanTransaction repayment : this.loanTransactions) {
			if (repayment.isRepayment()) {
				cumulativePaid = cumulativePaid.plus(repayment.getAmount());
			}
		}

		return cumulativePaid;
	}

	public Money calculateRebateWhenPaidInFullOn(final LocalDate paidInFullDate) {

		Money loanPrincipal = this.loanRepaymentScheduleDetail.getPrincipal();
		Money rebate = Money.zero(loanPrincipal.getCurrency());

		if (this.isDisbursed()
				&& !paidInFullDate.isBefore(this.getDisbursementDate())) {

			InterestRebateCalculator interestRebateCalculator = this.interestRebateCalculatorFactory
					.createCalcualtor(this.loanRepaymentScheduleDetail
							.getInterestMethod(),
							this.loanRepaymentScheduleDetail
									.getAmortizationMethod());

			rebate = interestRebateCalculator.calculate(this
					.getDisbursementDate(), paidInFullDate, loanPrincipal,
					this.loanRepaymentScheduleDetail
							.getAnnualNominalInterestRate(),
					this.repaymentScheduleInstallments, this.loanTransactions);
		}

		return rebate;
	}

	private Money getTotalInterestOnLoan() {
		Money cumulativeInterest = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());

		for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {
			cumulativeInterest = cumulativeInterest.plus(scheduledRepayment.getInterest(loanCurrency()));
		}

		return cumulativeInterest;
	}

	private Money getTotalPrincipalOnLoan() {
		Money cumulativePrincipal = Money.zero(this.loanRepaymentScheduleDetail
				.getPrincipal().getCurrency());

		for (LoanRepaymentScheduleInstallment scheduledRepayment : this.repaymentScheduleInstallments) {
			cumulativePrincipal = cumulativePrincipal.plus(scheduledRepayment
					.getPrincipal(loanCurrency()));
		}

		return cumulativePrincipal;
	}

	public Money getInterestRebateOwed() {
		return Money.of(this.loanRepaymentScheduleDetail.getCurrency(), this.interestRebateOwed);
	}

	public Money getInArrearsTolerance() {
		return this.loanRepaymentScheduleDetail.getInArrearsTolerance();
	}

	public boolean identifiedBy(String identifier) {
		return identifier.equalsIgnoreCase(this.externalId)
				|| identifier.equalsIgnoreCase(this.getId().toString());
	}

	public String getLoanStatusDisplayName() {
		return this.loanStatus.getDisplayName();
	}

	public MonetaryCurrency getCurrency() {
		return this.loanRepaymentScheduleDetail.getCurrency();
	}

	public LocalDate getInterestCalculatedFromDate() {
		
		LocalDate interestCalculatedFrom = null;
		if (this.interestCalculatedFromDate != null) {
			interestCalculatedFrom = new LocalDate(this.interestCalculatedFromDate);
		}
		
		return interestCalculatedFrom;
	}

	public LocalDate getLoanStatusSinceDate() {
		
		LocalDate statusSinceDate = new LocalDate(this.submittedOnDate);
		if (isApproved()) {
			statusSinceDate = new LocalDate(this.approvedOnDate);
		}
		
		if (isDisbursed()) {
			statusSinceDate = new LocalDate(this.disbursedOnDate);
		}
		
		if (isClosed()) {
			statusSinceDate = new LocalDate(this.closedOnDate);
		}
		
		return statusSinceDate;
	}
}