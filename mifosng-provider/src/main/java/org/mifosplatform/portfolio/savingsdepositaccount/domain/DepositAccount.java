package org.mifosplatform.portfolio.savingsdepositaccount.domain;

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
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsdepositaccount.command.DepositStateTransitionApprovalCommand;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.InvalidDepositStateTransitionException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.DepositProduct;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_deposit_account", uniqueConstraints = @UniqueConstraint(name = "deposit_acc_external_id", columnNames = { "external_id" }))
public class DepositAccount extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private DepositProduct product;

    @Column(name = "external_id")
    private String externalId;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "deposit_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "maturity_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureInMonths;

    @Column(name = "interest_compounded_every", nullable = false)
    private Integer interestCompoundedEvery;

    @Column(name = "interest_compounded_every_period_enum", nullable = false)
    private Integer interestCompoundedFrequencyType;

    @Temporal(TemporalType.DATE)
    @Column(name = "projected_commencement_date")
    private Date projectedCommencementDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "actual_commencement_date")
    private Date actualCommencementDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "matures_on_date")
    private Date maturesOnDate;

    @Column(name = "projected_interest_accrued_on_maturity", scale = 6, precision = 19, nullable = false)
    private BigDecimal projectedInterestAccruedOnMaturity;

    @Column(name = "actual_interest_accrued", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestAccrued;

    @Column(name = "projected_total_maturity_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal projectedTotalOnMaturity;

    @Column(name = "actual_total_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal total;

    @Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal preClosureInterestRate;

    @Column(name = "is_renewal_allowed", nullable = false)
    private boolean renewalAllowed = false;

    @Column(name = "is_preclosure_allowed", nullable = false)
    private boolean preClosureAllowed = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "status_enum", nullable = false)
    private Integer depositStatus;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "closedon_date")
    private Date closedOnDate;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "rejectedon_date")
    private Date rejectedOnDate;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "withdrawnon_date")
    private Date withdrawnOnDate;

    @Column(name = "interest_paid", scale = 6, precision = 19, nullable = false)
    private BigDecimal interstPaid;

    @Column(name = "is_interest_withdrawable", nullable = false)
    private boolean isInterestWithdrawable = false;

    @Column(name = "is_compounding_interest_allowed", nullable = false)
    private boolean interestCompoundingAllowed = false;

    // see
    // http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
    @OrderBy(value = "id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "depositAccount", orphanRemoval = true)
    private final List<DepositAccountTransaction> depositaccountTransactions = new ArrayList<DepositAccountTransaction>();

    @SuppressWarnings("unused")
    @OneToOne(optional = true, cascade = { CascadeType.PERSIST })
    @JoinColumn(name = "renewed_account_id")
    private DepositAccount renewdAccount;

    @Column(name = "is_lock_in_period_allowed", nullable = false)
    private boolean isLockinPeriodAllowed = false;

    @Column(name = "lock_in_period", nullable = false)
    private Integer lockinPeriod;

    @Column(name = "lock_in_period_type", nullable = false)
    private PeriodFrequencyType lockinPeriodType;

    // for interest posting
    @Column(name = "available_interest", scale = 6, precision = 19)
    private BigDecimal availableInterest;

    @Column(name = "interest_posted_amount", scale = 6, precision = 19)
    private BigDecimal interestPostedAmount;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_interest_posted_date")
    private Date lastInterestPostedDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "next_interest_posting_date")
    private Date nextInterestPostingDate;

    public DepositAccount openNew(final Client client, final DepositProduct product, final String externalId, final Money deposit,
            final BigDecimal maturityInterestRate, final BigDecimal preClosureInterestRate, final Integer tenureInMonths,
            final Integer interestCompoundedEvery, final PeriodFrequencyType interestCompoundedFrequencyPeriodType,
            final LocalDate commencementDate, final boolean renewalAllowed, final boolean preClosureAllowed,
            final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator,
            final DepositLifecycleStateMachine depositLifecycleStateMachine, final boolean isInterstWithdrawable,
            final boolean interestCompoundingAllowed, final boolean isLockinPeriodAllowed, final Integer lockinPeriod,
            final PeriodFrequencyType lockinPeriodType) {

        Money futureValueOnMaturity = null;

        if (interestCompoundingAllowed) {
            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit, tenureInMonths,
                    maturityInterestRate, interestCompoundedEvery, interestCompoundedFrequencyPeriodType);
        } else {
            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityForSimpleInterest(deposit,
                    tenureInMonths, maturityInterestRate, interestCompoundedEvery, interestCompoundedFrequencyPeriodType);
        }

        DepositAccountStatus from = null;
        if (depositStatus != null) {
            from = DepositAccountStatus.fromInt(depositStatus);
        }

        DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_CREATED, from);
        depositStatus = statusEnum.getValue();

        return new DepositAccount(client, product, externalId, deposit, maturityInterestRate, preClosureInterestRate, tenureInMonths,
                interestCompoundedEvery, interestCompoundedFrequencyPeriodType, commencementDate, renewalAllowed, preClosureAllowed,
                futureValueOnMaturity, depositStatus, isInterstWithdrawable, interestCompoundingAllowed, isLockinPeriodAllowed,
                lockinPeriod, lockinPeriodType);
    }

    public DepositAccount() {
        this.product = null;
    }

    public DepositAccount(final Client client, final DepositProduct product, final String externalId, final Money deposit,
            final BigDecimal interestRate, final BigDecimal preClosureInterestRate, final Integer termInMonths,
            final Integer interestCompoundedEvery, final PeriodFrequencyType interestCompoundedFrequencyPeriodType,
            final LocalDate commencementDate, final boolean renewalAllowed, final boolean preClosureAllowed,
            final Money futureValueOnMaturity, final Integer depositStatus, final boolean isInterestWithdrawable,
            final boolean interestCompoundingAllowed, final boolean isLockinPeriodAllowed, final Integer lockinPeriod,
            final PeriodFrequencyType lockinPeriodType) {
        this.client = client;
        this.product = product;
        setExternalId(externalId);

        this.currency = deposit.getCurrency();
        this.depositAmount = deposit.getAmount();
        product.validateDepositInRange(this.depositAmount);
        this.interestRate = interestRate;
        product.validateInterestRateInRange(interestRate);
        this.tenureInMonths = termInMonths;

        this.interestCompoundedEvery = interestCompoundedEvery;
        this.interestCompoundedFrequencyType = interestCompoundedFrequencyPeriodType.getValue();
        if (commencementDate != null) {
            this.projectedCommencementDate = commencementDate.toDate();
            this.maturesOnDate = commencementDate.plusMonths(this.tenureInMonths).minusDays(1).toDate();
        }

        this.renewalAllowed = renewalAllowed;
        this.preClosureAllowed = preClosureAllowed;

        this.preClosureInterestRate = preClosureInterestRate;
        this.isInterestWithdrawable = isInterestWithdrawable;
        this.interestCompoundingAllowed = interestCompoundingAllowed;
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;

        // derived fields
        this.projectedInterestAccruedOnMaturity = futureValueOnMaturity.minus(deposit).getAmount();
        this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
        this.depositStatus = depositStatus;
        this.interestAccrued = BigDecimal.ZERO;
        this.interstPaid = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }

    private void setExternalId(final String externalId) {
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
    }

    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on account so it wont appear
     * in query/report results.
     * 
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.externalId = this.getId() + "_" + this.externalId;
    }

    public void approve(final LocalDate actualCommencementDate, final DepositLifecycleStateMachine depositLifecycleStateMachine,
            final DepositStateTransitionApprovalCommand command, final FixedTermDepositInterestCalculator calculator) {

        DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_APPROVED,
                DepositAccountStatus.fromInt(this.depositStatus));
        this.depositStatus = statusEnum.getValue();

        if (command.getTenureInMonths() != null) {
            this.tenureInMonths = command.getTenureInMonths();
        }

        if (command.getMaturityInterestRate() != null) {
            this.interestRate = command.getMaturityInterestRate();
        }
        this.product.validateInterestRateInRange(this.interestRate);

        if (command.getDepositAmount() != null) {
            this.depositAmount = command.getDepositAmount();
        }
        this.product.validateDepositInRange(this.depositAmount);

        this.interestCompoundedEvery = command.getInterestCompoundedEvery();

        if (command.getInterestCompoundedEveryPeriodType() != null) {
            this.interestCompoundedFrequencyType = PeriodFrequencyType.fromInt(command.getInterestCompoundedEveryPeriodType()).getValue();
        }

        this.actualCommencementDate = actualCommencementDate.toDate();
        this.maturesOnDate = getActualCommencementDate().plusMonths(this.tenureInMonths).minusDays(1).toDate();

        Money futureValueOnMaturity = null;

        if (this.interestCompoundingAllowed) {
            futureValueOnMaturity = calculator.calculateInterestOnMaturityFor(getDeposit(), this.tenureInMonths, this.interestRate,
                    this.interestCompoundedEvery, getInterestCompoundedFrequencyType());
        } else {
            futureValueOnMaturity = calculator.calculateInterestOnMaturityForSimpleInterest(getDeposit(), this.tenureInMonths,
                    this.interestRate, this.interestCompoundedEvery, getInterestCompoundedFrequencyType());
        }
        this.interestAccrued = futureValueOnMaturity.minus(getDeposit()).getAmount();
        this.total = futureValueOnMaturity.getAmount();

        DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.deposit(getDeposit(), getActualCommencementDate(), null);
        depositaccountTransaction.updateAccount(this);
        this.depositaccountTransactions.add(depositaccountTransaction);

        // for interest posting initializing
        this.lastInterestPostedDate = this.actualCommencementDate;
        this.nextInterestPostingDate = new LocalDate(this.lastInterestPostedDate).plusMonths(this.interestCompoundedEvery).toDate();
        this.availableInterest = BigDecimal.ZERO;
        this.interestPostedAmount = BigDecimal.ZERO;

        LocalDate submittalDate = new LocalDate(this.projectedCommencementDate);
        if (actualCommencementDate.isBefore(submittalDate)) {
            final String errorMessage = "The date on which a deposit is approved cannot be before its submittal date: "
                    + submittalDate.toString();
            throw new InvalidDepositStateTransitionException("approval", "cannot.be.before.submittal.date", errorMessage,
                    getActualCommencementDate(), submittalDate);
        }

        if (actualCommencementDate.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a deposit is approved cannot be in the future.";
            throw new InvalidDepositStateTransitionException("approval", "cannot.be.a.future.date", errorMessage,
                    getActualCommencementDate());
        }
    }

    public LocalDate getActualCommencementDate() {
        LocalDate date = null;
        if (this.actualCommencementDate != null) {
            date = new LocalDate(this.actualCommencementDate);
        }
        return date;
    }

    public LocalDate getProjectedCommencementDate() {
        LocalDate date = null;
        if (this.projectedCommencementDate != null) {
            date = new LocalDate(this.projectedCommencementDate);
        }
        return date;
    }

    public LocalDate getMaturityDate() {
        LocalDate date = null;
        if (this.maturesOnDate != null) {
            date = new LocalDate(this.maturesOnDate);
        }
        return date;
    }

    public List<DepositAccountTransaction> getDepositaccountTransactions() {
        return depositaccountTransactions;
    }

    public BigDecimal getProjectedTotalOnMaturity() {
        return projectedTotalOnMaturity;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public boolean isInterestCompoundingAllowed() {
        return interestCompoundingAllowed;
    }

    public boolean isLockinPeriodAllowed() {
        return isLockinPeriodAllowed;
    }

    public Integer getLockinPeriod() {
        return lockinPeriod;
    }

    public PeriodFrequencyType getLockinPeriodType() {
        return lockinPeriodType;
    }

    public void reject(final LocalDate rejectedOn, final DepositLifecycleStateMachine depositLifecycleStateMachine) {

        DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_REJECTED,
                DepositAccountStatus.fromInt(this.depositStatus));
        this.depositStatus = statusEnum.getValue();

        this.maturesOnDate = null;
        this.rejectedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();
        this.closedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();

        if (rejectedOn.isBefore(getProjectedCommencementDate())) {

            final String errorMessage = "The date on which a deposit is rejected cannot be before its submittal date: "
                    + getProjectedCommencementDate().toString();
            throw new InvalidDepositStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, rejectedOn,
                    getProjectedCommencementDate());

        }
        if (rejectedOn.isAfter(new LocalDate())) {

            final String errorMessage = "The date on which a deposit is rejected cannot be in the future.";
            throw new InvalidDepositStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, rejectedOn);

        }

    }

    public void withdrawnByApplicant(final LocalDate withdrawnOn, final DepositLifecycleStateMachine depositLifecycleStateMachine) {

        DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_WITHDRAWN,
                DepositAccountStatus.fromInt(this.depositStatus));
        this.depositStatus = statusEnum.getValue();

        this.maturesOnDate = null;
        this.withdrawnOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();
        this.closedOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();

        if (withdrawnOn.isBefore(getProjectedCommencementDate())) {

            final String errorMessage = "The date on which a deposit is rejected cannot be before its submittal date: "
                    + getProjectedCommencementDate().toString();
            throw new InvalidDepositStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, withdrawnOn,
                    getProjectedCommencementDate());

        }

        if (withdrawnOn.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a deposit is rejected cannot be in the future.";
            throw new InvalidDepositStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, withdrawnOn);
        }
    }

    public void undoDepositApproval(final DepositLifecycleStateMachine depositLifecycleStateMachine) {

        DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_APPROVAL_UNDO,
                DepositAccountStatus.fromInt(this.depositStatus));
        this.depositStatus = statusEnum.getValue();

        this.actualCommencementDate = null;
        this.projectedInterestAccruedOnMaturity = this.interestAccrued;
        this.projectedTotalOnMaturity = this.depositAmount.add(this.projectedInterestAccruedOnMaturity);
        this.maturesOnDate = getProjectedCommencementDate().plusMonths(this.tenureInMonths).minusDays(1).toDate();
        this.total = null;
        this.interestAccrued = null;
        this.depositaccountTransactions.clear();
    }

    public PeriodFrequencyType getInterestCompoundedFrequencyType() {
        return PeriodFrequencyType.fromInt(this.interestCompoundedFrequencyType);
    }

    public Money getDeposit() {
        return Money.of(this.currency, this.depositAmount);
    }

    public Money getAccuredInterest() {
        return Money.of(this.currency, this.interestAccrued);
    }

    public Client client() {
        return this.client;
    }

    public DepositProduct product() {
        return this.product;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public boolean isRenewalAllowed() {
        return this.renewalAllowed;
    }

    public BigDecimal getInterstPaid() {
        return this.interstPaid;
    }

    public boolean isInterestWithdrawable() {
        return isInterestWithdrawable;
    }

    public Integer getTenureInMonths() {
        return tenureInMonths;
    }

    public Integer getInterestCompoundedEvery() {
        return this.interestCompoundedEvery;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public Date getMaturesOnDate() {
        return maturesOnDate;
    }

    public BigDecimal getPreClosureInterestRate() {
        return preClosureInterestRate;
    }

    public boolean isPreClosureAllowed() {
        return preClosureAllowed;
    }

    public Integer getDepositStatus() {
        return depositStatus;
    }

    public LocalDate maturesOnDate() {
        LocalDate date = null;
        if (this.maturesOnDate != null) {
            date = new LocalDate(this.maturesOnDate);
        }
        return date;
    }

    public void updateAccount(DepositAccount account) {

        this.renewdAccount = account;

    }

    public void withdrawDepositAccountMoney(DepositLifecycleStateMachine depositLifecycleStateMachine,
            FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator, LocalDate eventDate) {

        if (eventDate.isBefore(getLastInterestPostedDate())) {
            final String errorMessage = "Deposit Account Preclosed date \"" + eventDate + "\"cannot before last interest posted date \""
                    + getLastInterestPostedDate() + "\"";
            throw new InvalidDepositStateTransitionException("withdraw", "event.date.cannot.before.last.interest.posted.date",
                    errorMessage, getLastInterestPostedDate(), eventDate);
        }
        if (eventDate.isAfter(maturesOnDate()) || eventDate.equals(maturesOnDate())) {
            DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_CLOSED,
                    DepositAccountStatus.fromInt(this.depositStatus));
            this.depositStatus = statusEnum.getValue();
        } else if (eventDate.isBefore(maturesOnDate())) {
            DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_PRECLOSED,
                    DepositAccountStatus.fromInt(this.depositStatus));
            this.depositStatus = statusEnum.getValue();
        }

        Integer days = Days.daysBetween(getLastInterestPostedDate(), eventDate).getDays();
        Money accuredInterestAfterMaturityToTillDate = fixedTermDepositInterestCalculator.calculateRemainInterest(getDeposit(), days,
                preClosureInterestRate);
        this.interestAccrued = this.interestAccrued.add(accuredInterestAfterMaturityToTillDate.getAmount());
        this.availableInterest = this.availableInterest.add(accuredInterestAfterMaturityToTillDate.getAmount());
        DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.withdraw(getDeposit(), eventDate,Money.of(currency, this.availableInterest),getDeposit().getAmount().add(this.availableInterest));
        //DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.withdraw(getDeposit(), eventDate, getAccuredInterest());
        depositaccountTransaction.updateAccount(this);
        this.depositaccountTransactions.add(depositaccountTransaction);

        this.closedOnDate = eventDate.toDate();
        this.withdrawnOnDate = null;
        this.rejectedOnDate = null;

    }

    /*
     * public void adjustTotalAmountForPreclosureInterest(DepositAccount
     * account, FixedTermDepositInterestCalculator
     * fixedTermDepositInterestCalculator,LocalDate eventDate) {
     * 
     * LocalDate commnencementDate = getActualCommencementDate(); LocalDate
     * lastInterestPosedDate = getLastInterestPostedDate(); LocalDate
     * preClosedDate = eventDate;
     * 
     * if (preClosedDate.isBefore(lastInterestPosedDate)) { throw new
     * DepositAccountTransactionsException
     * ("deposit.account.preclosed.date.should.be.after.last.interest.posted.date"
     * , null); }
     * 
     * Integer tenure = Months.monthsBetween(lastInterestPosedDate,
     * preClosedDate).getMonths(); LocalDate actualPrecloseCalculationDate =
     * lastInterestPosedDate.plusMonths(tenure); Integer missedDays =
     * Days.daysBetween(actualPrecloseCalculationDate, preClosedDate).getDays();
     * 
     * Money deposit = Money.of(account.getDeposit().getCurrency(),
     * account.getDeposit().getAmount()); Money accuredtotalAmount = null; Money
     * remainDaysAmount =
     * fixedTermDepositInterestCalculator.calculateRemainInterest(deposit,
     * missedDays, preClosureInterestRate); if
     * (account.isInterestCompoundingAllowed()) { accuredtotalAmount =
     * fixedTermDepositInterestCalculator
     * .calculateInterestOnMaturityFor(deposit, tenure, preClosureInterestRate,
     * interestCompoundedEvery,
     * this.product.getInterestCompoundedEveryPeriodType()); accuredtotalAmount
     * = accuredtotalAmount.plus(remainDaysAmount); } else { accuredtotalAmount
     * = fixedTermDepositInterestCalculator
     * .calculateInterestOnMaturityForSimpleInterest(deposit, tenure,
     * preClosureInterestRate, interestCompoundedEvery,
     * this.product.getInterestCompoundedEveryPeriodType()); accuredtotalAmount
     * = accuredtotalAmount.plus(remainDaysAmount); } this.total =
     * account.isInterestCompoundingAllowed
     * ()?accuredtotalAmount.getAmount():BigDecimal
     * .valueOf(accuredtotalAmount.getAmount
     * ().doubleValue()-this.interstPaid.doubleValue()); this.interestAccrued =
     * accuredtotalAmount.minus(deposit).getAmount();
     * 
     * }
     */

    public void withdrawInterest(Money interest) {
        @SuppressWarnings("unused")
        boolean statustest = isActive();
        if (isActive()) {
            //DepositAccountTransaction depositAccountTransaction = DepositAccountTransaction.withdraw(null, new LocalDate(), interest);
        	DepositAccountTransaction depositAccountTransaction = DepositAccountTransaction.withdraw(null, new LocalDate(), interest,getDeposit().getAmount().add(this.availableInterest).subtract(interest.getAmount()));
            depositAccountTransaction.updateAccount(this);
            this.depositaccountTransactions.add(depositAccountTransaction);
            this.interstPaid = this.interstPaid.add(interest.getAmount());
            this.availableInterest = this.availableInterest.subtract(interest.getAmount());
            this.total = this.total.subtract(interest.getAmount());
        }

    }

    public void closeDepositAccount(DepositLifecycleStateMachine depositLifecycleStateMachine) {
        DepositAccountStatus statusEnumForClose = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_CLOSED,
                DepositAccountStatus.fromInt(this.depositStatus));
        this.depositStatus = statusEnumForClose.getValue();

      //DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.withdraw(getDeposit(), new LocalDate(),Money.of(getDeposit().getCurrency(), getTotal()).minus(getDeposit()));
        DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.withdraw(getDeposit(), new LocalDate(),Money.of(getDeposit().getCurrency(), getTotal()).minus(getDeposit()),
        		getDeposit().plus(Money.of(getDeposit().getCurrency(), getTotal()).minus(getDeposit())).getAmount());
        depositaccountTransaction.updateAccount(this);
        this.depositaccountTransactions.add(depositaccountTransaction);

        this.closedOnDate = new LocalDate().toDate();
    }

    public void update(final DepositProduct product, final String externalId, final LocalDate commencementDate, final Money deposit,
            final Integer tenureInMonths, final BigDecimal maturityInterestRate, final BigDecimal preClosureInterestRate,
            final Integer interestCompoundedEvery, final PeriodFrequencyType compoundingInterestFrequency, final boolean renewalAllowed,
            final boolean preClosureAllowed, final boolean isInterestWithdrawable, final boolean isInterestCompoundingAllowed,
            final FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator, final boolean isLockinPeriodAllowed,
            final Integer lockinPeriod, final PeriodFrequencyType lockinPeriodType) {

        Money futureValueOnMaturity = null;

        if (isInterestCompoundingAllowed) {
            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit, tenureInMonths,
                    maturityInterestRate, interestCompoundedEvery, compoundingInterestFrequency);
        } else {
            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityForSimpleInterest(deposit,
                    tenureInMonths, maturityInterestRate, interestCompoundedEvery, compoundingInterestFrequency);
        }

        setExternalId(externalId);
        if (commencementDate != null) {
            this.projectedCommencementDate = commencementDate.toDate();
            this.maturesOnDate = commencementDate.plusMonths(this.tenureInMonths).minusDays(1).toDate();
        }
        this.product = product;
        this.depositAmount = deposit.getAmount();
        this.tenureInMonths = tenureInMonths;
        this.interestRate = maturityInterestRate;
        this.product.validateInterestRateInRange(maturityInterestRate);
        this.preClosureInterestRate = preClosureInterestRate;
        this.interestCompoundedEvery = interestCompoundedEvery;
        this.interestCompoundedFrequencyType = compoundingInterestFrequency.getValue();
        this.renewalAllowed = renewalAllowed;
        this.preClosureAllowed = preClosureAllowed;
        this.isInterestWithdrawable = isInterestWithdrawable;
        this.interestCompoundingAllowed = isInterestCompoundingAllowed;
        this.projectedInterestAccruedOnMaturity = futureValueOnMaturity.minus(deposit).getAmount();
        this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType;
        this.interestAccrued = BigDecimal.ZERO;

    }

    public void update(final boolean renewalAllowed, final boolean isInterestWithdrawable) {
        this.renewalAllowed = renewalAllowed;
        this.isInterestWithdrawable = isInterestWithdrawable;
    }

    public boolean isSubmittedAndPendingApproval() {
        return DepositAccountStatus.fromInt(this.depositStatus).isSubmittedAndPendingApproval();
    }

    public boolean isActive() {
        return DepositAccountStatus.fromInt(this.depositStatus).isActive();
    }

    public void postInterestForDepositAccount(DepositAccount account, FixedTermDepositInterestCalculator fixedTermDepositInterestCalculator) {

        LocalDate lastInterestPostedDate = getLastInterestPostedDate();
        LocalDate nextInterestPostingDate = getNextInterestPostedDate();
        Integer monthsForInterestCalculation = Months.monthsBetween(lastInterestPostedDate, new LocalDate()).getMonths();
        Integer postInterestItereations = monthsForInterestCalculation / this.interestCompoundedEvery;
        Money deposit = Money.of(this.currency, this.depositAmount);

        while (postInterestItereations > 0) {

            lastInterestPostedDate = getLastInterestPostedDate();
            nextInterestPostingDate = getNextInterestPostedDate();
            if (lastInterestPostedDate.isBefore(getMaturityDate())) {
                if (nextInterestPostingDate.isAfter(getMaturityDate())) {
                    Integer noofRemainMonthsAfterMaturity = Months.monthsBetween(getMaturityDate(), nextInterestPostingDate).getMonths();
                    Integer noOfMonthsBeforeMaturity = Months.monthsBetween(lastInterestPostedDate, getMaturityDate().plusDays(1)).getMonths();
                    Money futureValueOnMaturity = null;
                    if (noOfMonthsBeforeMaturity > 0) {

                        if (this.interestCompoundingAllowed) {
                            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit,
                                    noOfMonthsBeforeMaturity, this.interestRate, this.interestCompoundedEvery,
                                    getInterestCompoundedFrequencyType());
                        } else {
                            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityForSimpleInterest(
                                    deposit, noOfMonthsBeforeMaturity, this.interestRate, this.interestCompoundedEvery,
                                    getInterestCompoundedFrequencyType());
                        }
                        this.interestPostedAmount = this.interestPostedAmount.add(futureValueOnMaturity.minus(deposit).getAmount());
                        this.availableInterest = this.availableInterest.add(futureValueOnMaturity.minus(deposit).getAmount());
                        this.lastInterestPostedDate = nextInterestPostingDate.toDate();
                        this.nextInterestPostingDate = nextInterestPostingDate.plusMonths(this.interestCompoundedEvery).toDate();

                    }
                    if (noofRemainMonthsAfterMaturity > 0) {

                        if (this.interestCompoundingAllowed) {
                            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit,
                                    noofRemainMonthsAfterMaturity, this.preClosureInterestRate, this.interestCompoundedEvery,
                                    getInterestCompoundedFrequencyType());
                        } else {
                            futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityForSimpleInterest(
                                    deposit, noofRemainMonthsAfterMaturity, this.preClosureInterestRate, this.interestCompoundedEvery,
                                    getInterestCompoundedFrequencyType());
                        }
                        this.interestPostedAmount = this.interestPostedAmount.add(futureValueOnMaturity.minus(deposit).getAmount());
                        this.availableInterest = this.availableInterest.add(futureValueOnMaturity.minus(deposit).getAmount());
                        this.lastInterestPostedDate = nextInterestPostingDate.toDate();
                        this.nextInterestPostingDate = nextInterestPostingDate.plusMonths(this.interestCompoundedEvery).toDate();
                    }

                    // add transaction entry for each interest posting
                    DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.postInterest(null, nextInterestPostingDate,futureValueOnMaturity.minus(deposit),deposit.getAmount().add(this.availableInterest));
                  //DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.postInterest(null, nextInterestPostingDate, futureValueOnMaturity.minus(deposit));

                    depositaccountTransaction.updateAccount(this);
                    this.depositaccountTransactions.add(depositaccountTransaction);
                } else {
                    Money futureValueOnMaturity = null;
                    if (this.interestCompoundingAllowed) {
                        futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit,
                                this.interestCompoundedEvery, this.interestRate, this.interestCompoundedEvery,
                                getInterestCompoundedFrequencyType());
                    } else {
                        futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityForSimpleInterest(deposit,
                                this.interestCompoundedEvery, this.interestRate, this.interestCompoundedEvery,
                                getInterestCompoundedFrequencyType());
                    }
                    this.interestPostedAmount = this.interestPostedAmount.add(futureValueOnMaturity.minus(deposit).getAmount());
                    this.availableInterest = this.availableInterest.add(futureValueOnMaturity.minus(deposit).getAmount());
                    this.lastInterestPostedDate = nextInterestPostingDate.toDate();
                    this.nextInterestPostingDate = nextInterestPostingDate.plusMonths(this.interestCompoundedEvery).toDate();

                    // add transaction entry for each interest posting
                 // DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.postInterest(null, nextInterestPostingDate, futureValueOnMaturity.minus(deposit));
                    DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.postInterest(null, nextInterestPostingDate,futureValueOnMaturity.minus(deposit),deposit.getAmount().add(this.availableInterest));
                    depositaccountTransaction.updateAccount(this);
                    this.depositaccountTransactions.add(depositaccountTransaction);
                }
            } else {
                Money futureValueOnMaturity = null;
                if (this.interestCompoundingAllowed) {
                    futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityFor(deposit,
                            this.interestCompoundedEvery, this.preClosureInterestRate, this.interestCompoundedEvery,
                            getInterestCompoundedFrequencyType());
                } else {
                    futureValueOnMaturity = fixedTermDepositInterestCalculator.calculateInterestOnMaturityForSimpleInterest(deposit,
                            this.interestCompoundedEvery, this.preClosureInterestRate, this.interestCompoundedEvery,
                            getInterestCompoundedFrequencyType());
                }
                this.interestPostedAmount = this.interestPostedAmount.add(futureValueOnMaturity.minus(deposit).getAmount());
                this.availableInterest = this.availableInterest.add(futureValueOnMaturity.minus(deposit).getAmount());
                this.lastInterestPostedDate = nextInterestPostingDate.toDate();
                this.nextInterestPostingDate = nextInterestPostingDate.plusMonths(this.interestCompoundedEvery).toDate();

                // add transaction entry for each interest posting
                DepositAccountTransaction depositaccountTransaction = DepositAccountTransaction.postInterest(null, nextInterestPostingDate, futureValueOnMaturity.minus(deposit),deposit.getAmount().add(this.availableInterest));
                depositaccountTransaction.updateAccount(this);
                this.depositaccountTransactions.add(depositaccountTransaction);
            }
            postInterestItereations--;
        }

    }

    private final LocalDate getLastInterestPostedDate() {
        LocalDate lastInterestPostedDate;
        if (this.lastInterestPostedDate == null) {
            lastInterestPostedDate = new LocalDate(this.actualCommencementDate);
        } else {
            lastInterestPostedDate = new LocalDate(this.lastInterestPostedDate);
        }
        return lastInterestPostedDate;
    }

    private final LocalDate getNextInterestPostedDate() {
        LocalDate nextInterestPostingDate;
        if (this.nextInterestPostingDate == null) {
            nextInterestPostingDate = new LocalDate(this.nextInterestPostingDate).plusMonths(this.interestCompoundedEvery);
        } else {
            nextInterestPostingDate = new LocalDate(this.nextInterestPostingDate);
        }
        return nextInterestPostingDate;
    }

    public BigDecimal getAvailableInterest() {
        return availableInterest;
    }

    public BigDecimal getInterestPostedAmount() {
        return interestPostedAmount;
    }
}