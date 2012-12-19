package org.mifosplatform.portfolio.savingsaccount.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountDepositCommand;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountEvent;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositAccountStatus;
import org.mifosplatform.portfolio.savingsdepositaccount.domain.DepositLifecycleStateMachine;
import org.mifosplatform.portfolio.savingsdepositaccount.exception.InvalidDepositStateTransitionException;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_saving_account", uniqueConstraints = @UniqueConstraint(name="saving_acc_external_id", columnNames = { "external_id" }))
public class SavingAccount extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private SavingProduct product;

    @Column(name = "savings_product_type", nullable = false)
    private Integer savingProductType;

    @Column(name = "external_id")
    private String externalId;

    @SuppressWarnings("unused")
    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "deposit_amount_per_period", scale = 6, precision = 19, nullable = false)
    private BigDecimal savingsDepositAmountPerPeriod;

    @SuppressWarnings("unused")
    @Column(name = "total_deposit_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalSavingsAmount;

    @Column(name = "reccuring_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal reccuringInterestRate;

    @Column(name = "regular_saving_nominal_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal savingInterestRate;

    @Column(name = "tenure", nullable = false)
    private Integer tenure;

    @Column(name = "tenure_type", nullable = false)
    private Integer tenureType;

    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    @Column(name = "interest_type", nullable = false)
    private Integer interestType;

    @Column(name = "interest_calculation_method")
    private Integer interestCalculationMethod;

    @Temporal(TemporalType.DATE)
    @Column(name = "projected_commencement_date")
    private Date projectedCommencementDate;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "actual_commencement_date")
    private Date actualCommencementDate;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "matures_on_date")
    private Date maturesOnDate;

    @SuppressWarnings("unused")
    @Column(name = "projected_interest_accrued_on_maturity", scale = 6, precision = 19, nullable = false)
    private BigDecimal projectedInterestAccruedOnMaturity;

    @SuppressWarnings("unused")
    @Column(name = "actual_interest_accrued", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestAccrued;

    @SuppressWarnings("unused")
    @Column(name = "projected_total_maturity_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal projectedTotalOnMaturity;

    @SuppressWarnings("unused")
    @Column(name = "actual_total_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal total;

    @Column(name = "pre_closure_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal preClosureInterestRate;
    
    @Column(name = "outstanding_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal outstandingAmount;

    @Column(name = "is_preclosure_allowed", nullable = false)
    private boolean preClosureAllowed = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "status_enum", nullable = false)
    private Integer accountStatus;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "closedon_date")
    private Date closedOnDate;

    @SuppressWarnings("unused")
    @Temporal(TemporalType.DATE)
    @Column(name = "rejectedon_date")
    private Date rejectedOnDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "withdrawnon_date")
    private Date withdrawnOnDate;

    @Column(name = "is_lock_in_period_allowed", nullable = false)
    private boolean isLockinPeriodAllowed = false;

    @Column(name = "lock_in_period", nullable = false)
    private Integer lockinPeriod;

    @Column(name = "lock_in_period_type", nullable = false)
    private Integer lockinPeriodType;
    
    @Column(name = "deposit_every", nullable = false)
    private Integer payEvery;
    
    @Column(name = "interest_posting_every", nullable = false)
    private Integer interestPostEvery;
    
    @Column(name = "interest_posting_frequency", nullable = false)
    private Integer interestPostFrequency;
    
    @Column(name = "interest_posted_amount", scale = 6, precision = 19)
    private BigDecimal interestPostedAmount;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_interest_posted_date")
    private Date lastInterestPostedDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "next_interest_posting_date")
    private Date nextInterestPostingDate;

    // see
    // http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingAccount", orphanRemoval = true)
    private final List<SavingScheduleInstallments> savingScheduleInstallments = new ArrayList<SavingScheduleInstallments>();
    
    @OrderBy(value = "id")
    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "savingAccount", orphanRemoval = true)
    private final List<SavingAccountTransaction> savingAccountTransactions = new ArrayList<SavingAccountTransaction>();
    
    public SavingAccount() {
		// TODO Auto-generated constructor stub
	}

    public static SavingAccount openNew(Client client, SavingProduct product, String externalId, Money savingsDeposit,
            BigDecimal reccuringInterestRate, BigDecimal savingInterestRate, Integer tenure, LocalDate commencementDate,
            TenureTypeEnum tenureTypeEnum, SavingProductType savingProductType, SavingFrequencyType savingFrequencyType,
            SavingsInterestType interestType, SavingInterestCalculationMethod savingInterestCalculationMethod,
            boolean isLockinPeriodAllowed, Integer lockinPeriod, PeriodFrequencyType lockinPeriodType,
            ReccuringDepositInterestCalculator reccuringDepositInterestCalculator, final DepositLifecycleStateMachine lifecycleStateMachine,
            Integer depositEvery, Integer interestPostEvery, Integer interestPostFrequency) {

        Money futureValueOnMaturity = null;
        if (savingProductType.isReccuring()) {
            futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure,
                    reccuringInterestRate, commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod,depositEvery);
        } else if (savingProductType.isRegular()) {
            futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure,
                    savingInterestRate, commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod,depositEvery);
        }

        return new SavingAccount(client, externalId, product, savingsDeposit, reccuringInterestRate, savingInterestRate, tenure,
                commencementDate, tenureTypeEnum, savingProductType, savingFrequencyType, interestType, savingInterestCalculationMethod,
                isLockinPeriodAllowed, lockinPeriod, lockinPeriodType, futureValueOnMaturity, lifecycleStateMachine,depositEvery,interestPostEvery, interestPostFrequency);
    }

    public SavingAccount(Client client, String externalId, SavingProduct product, Money savingsDeposit, BigDecimal reccuringInterestRate,
            BigDecimal savingInterestRate, Integer tenure, LocalDate commencementDate, TenureTypeEnum tenureTypeEnum,
            SavingProductType savingProductType, SavingFrequencyType savingFrequencyType, SavingsInterestType interestType,
            SavingInterestCalculationMethod savingInterestCalculationMethod, boolean isLockinPeriodAllowed, Integer lockinPeriod,
            PeriodFrequencyType lockinPeriodType, Money futureValueOnMaturity, final DepositLifecycleStateMachine lifecycleStateMachine,Integer payEvery,
            Integer interestPostEvery, Integer interestPostFrequency) {

        DepositAccountStatus from = null;
        if (accountStatus != null) {
            from = DepositAccountStatus.fromInt(accountStatus);
        }

        DepositAccountStatus statusEnum = lifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_CREATED, from);
        accountStatus = statusEnum.getValue();

        this.total = BigDecimal.ZERO;
        this.preClosureInterestRate = BigDecimal.ZERO;
        this.totalSavingsAmount = BigDecimal.ZERO;
        this.interestAccrued = BigDecimal.ZERO;

        this.client = client;
        this.externalId = externalId;
        this.product = product;
        this.savingsDepositAmountPerPeriod = savingsDeposit.getAmount();
        this.currency = savingsDeposit.getCurrency();
        this.reccuringInterestRate = reccuringInterestRate;
        this.savingInterestRate = savingInterestRate;
        this.tenure = tenure;
        this.projectedCommencementDate = commencementDate.toDate();
        this.tenureType = tenureTypeEnum.getValue();
        this.savingProductType = savingProductType.getValue();
        this.frequency = savingFrequencyType.getValue();
        this.interestType = interestType.getValue();
        this.interestCalculationMethod = savingInterestCalculationMethod.getValue();
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType.getValue();
        this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
        this.projectedInterestAccruedOnMaturity = futureValueOnMaturity.getAmount().subtract(
                BigDecimal.valueOf(savingsDeposit.getAmount().doubleValue() * tenure.doubleValue() / payEvery)); //presently only for months
        this.payEvery = payEvery;
        this.outstandingAmount = BigDecimal.ZERO;
        this.interestPostEvery = interestPostEvery;
        this.interestPostFrequency=interestPostFrequency;
    }

    public void modifyAccount(SavingProduct product, String externalId, Money savingsDeposit, BigDecimal reccuringInterestRate,
            BigDecimal savingInterestRate, Integer tenure, LocalDate commencementDate, TenureTypeEnum tenureTypeEnum,
            SavingProductType savingProductType, SavingFrequencyType savingFrequencyType,
            SavingInterestCalculationMethod savingInterestCalculationMethod, boolean isLockinPeriodAllowed, Integer lockinPeriod,
            PeriodFrequencyType lockinPeriodType, ReccuringDepositInterestCalculator reccuringDepositInterestCalculator,Integer payEvery) {

        Money futureValueOnMaturity = null;
        if (savingProductType.isReccuring()) {
            futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure,
                    reccuringInterestRate, commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod,payEvery);
        } else if (savingProductType.isRegular()) {
            futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsDeposit, tenure,
                    savingInterestRate, commencementDate, tenureTypeEnum, savingFrequencyType, savingInterestCalculationMethod,payEvery);
        }

        this.product = product;
        this.externalId = externalId;
        this.savingsDepositAmountPerPeriod = savingsDeposit.getAmount();
        this.reccuringInterestRate = reccuringInterestRate;
        this.savingInterestRate = savingInterestRate;
        this.tenure = tenure;
        this.projectedCommencementDate = commencementDate.toDate();
        this.tenureType = tenureTypeEnum.getValue();
        this.savingProductType = savingProductType.getValue();
        this.frequency = savingFrequencyType.getValue();
        this.interestCalculationMethod = savingInterestCalculationMethod.getValue();
        this.isLockinPeriodAllowed = isLockinPeriodAllowed;
        this.lockinPeriod = lockinPeriod;
        this.lockinPeriodType = lockinPeriodType.getValue();
        this.payEvery = payEvery;
        this.outstandingAmount = BigDecimal.ZERO;

        // FIXME - MADHUKAR - futureValueOnMaturity is possibly null
        this.projectedTotalOnMaturity = futureValueOnMaturity.getAmount();
        this.projectedInterestAccruedOnMaturity = futureValueOnMaturity.getAmount().subtract(
                BigDecimal.valueOf(savingsDeposit.getAmount().doubleValue() * tenure.doubleValue()));
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public boolean isPendingApproval() {
        return this.accountStatus.equals(100);
    }

    public Client getClient() {
        return client;
    }

    public SavingProduct getProduct() {
        return product;
    }

    public Integer getSavingProductType() {
        return savingProductType;
    }

    public String getExternalId() {
        return externalId;
    }

    public BigDecimal getSavingsDepositAmountPerPeriod() {
        return savingsDepositAmountPerPeriod;
    }

    public BigDecimal getReccuringInterestRate() {
        return reccuringInterestRate;
    }

    public BigDecimal getSavingInterestRate() {
        return savingInterestRate;
    }

    public Integer getTenure() {
        return tenure;
    }

    public Integer getTenureType() {
        return tenureType;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public Integer getInterestType() {
        return interestType;
    }

    public Integer getInterestCalculationMethod() {
        return interestCalculationMethod;
    }

    public Date getProjectedCommencementDate() {
        return projectedCommencementDate;
    }

    public BigDecimal getPreClosureInterestRate() {
        return preClosureInterestRate;
    }

    public boolean isPreClosureAllowed() {
        return preClosureAllowed;
    }

    public Date getWithdrawnOnDate() {
        return withdrawnOnDate;
    }

    public boolean isLockinPeriodAllowed() {
        return isLockinPeriodAllowed;
    }

    public Integer getLockinPeriod() {
        return lockinPeriod;
    }

    public Integer getLockinPeriodType() {
        return lockinPeriodType;
    }

    public Integer getPayEvery() {
		return this.payEvery;
	}

	public BigDecimal getOutstandingAmount() {
		return this.outstandingAmount;
	}

	public Integer getInterestPostEvery() {
		return this.interestPostEvery;
	}

	public Integer getInterestPostFrequency() {
		return this.interestPostFrequency;
	}

	public List<SavingAccountTransaction> getSavingAccountTransactions() {
		return this.savingAccountTransactions;
	}

	public void addSavingScheduleInstallment(final SavingScheduleInstallments installment) {
        installment.updateAccount(this);
        this.savingScheduleInstallments.add(installment);
    }

	public void reject(LocalDate rejectedOn, DepositLifecycleStateMachine depositLifecycleStateMachine) {


        DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_REJECTED,
                DepositAccountStatus.fromInt(this.accountStatus));
        this.accountStatus = statusEnum.getValue();

        this.maturesOnDate = null;
        this.rejectedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();
        this.closedOnDate = rejectedOn.toDateTimeAtCurrentTime().toDate();

        if (rejectedOn.isBefore(projectedCommencementDate())) {

            final String errorMessage = "The date on which a saving account is rejected cannot be before its submittal date: "
                    + projectedCommencementDate().toString();
            throw new InvalidDepositStateTransitionException("reject", "cannot.be.before.submittal.date", errorMessage, rejectedOn,
            		projectedCommencementDate());

        }
        if (rejectedOn.isAfter(new LocalDate())) {

            final String errorMessage = "The date on which a saving account is rejected cannot be in the future.";
            throw new InvalidDepositStateTransitionException("reject", "cannot.be.a.future.date", errorMessage, rejectedOn);

        }

    
	}
	
	public LocalDate projectedCommencementDate() {
        LocalDate date = null;
        if (this.projectedCommencementDate != null) {
            date = new LocalDate(this.projectedCommencementDate);
        }
        return date;
    }

	public void withdrawnByApplicant(LocalDate withdrawnOn, DepositLifecycleStateMachine depositLifecycleStateMachine) {
		
		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_WITHDRAWN,
                DepositAccountStatus.fromInt(this.accountStatus));
        this.accountStatus = statusEnum.getValue();

        this.maturesOnDate = null;
        this.withdrawnOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();
        this.closedOnDate = withdrawnOn.toDateTimeAtCurrentTime().toDate();

        if (withdrawnOn.isBefore(projectedCommencementDate())) {

            final String errorMessage = "The date on which a deposit is rejected cannot be before its submittal date: "
                    + projectedCommencementDate().toString();
            throw new InvalidDepositStateTransitionException("withdrawnbyclient", "cannot.be.before.submittal.date", errorMessage, withdrawnOn,
            		projectedCommencementDate());

        }

        if (withdrawnOn.isAfter(new LocalDate())) {
            final String errorMessage = "The date on which a deposit is rejected cannot be in the future.";
            throw new InvalidDepositStateTransitionException("withdrawnbyclient", "cannot.be.a.future.date", errorMessage, withdrawnOn);
        }
	}

	public void undoSavingAccountApproval(DepositLifecycleStateMachine depositLifecycleStateMachine) {
		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_APPROVAL_UNDO,
                DepositAccountStatus.fromInt(this.accountStatus));
        this.accountStatus = statusEnum.getValue();
        this.savingScheduleInstallments.clear();
        this.closedOnDate = new Date();
	}

	@SuppressWarnings("null")
	public void approveSavingAccount(LocalDate approvalDate, BigDecimal savingsDepositAmountPerPeriod, BigDecimal recurringInterestRate,
			BigDecimal savingInterestRate, Integer interestType, Integer tenure, Integer tenureType, Integer frequency, Integer payEvery, 
			DepositLifecycleStateMachine depositLifecycleStateMachine, ReccuringDepositInterestCalculator reccuringDepositInterestCalculator,
			Integer interestPostEvery, Integer interestPostFrequency) {
		
		DepositAccountStatus statusEnum = depositLifecycleStateMachine.transition(DepositAccountEvent.DEPOSIT_APPROVED,
                DepositAccountStatus.fromInt(this.accountStatus));
        this.accountStatus = statusEnum.getValue();
        
        Money savingsAmountPerPeriod = Money.of(currency, savingsDepositAmountPerPeriod);
        TenureTypeEnum tenureTypeEnum = TenureTypeEnum.fromInt(tenureType); 
        SavingFrequencyType savingFrequencyType = SavingFrequencyType.fromInt(frequency);
        SavingInterestCalculationMethod interestCalculationMethod = SavingInterestCalculationMethod.fromInt(this.interestCalculationMethod); 
        
        Money futureValueOnMaturity = null;
        if (SavingProductType.fromInt(product.getSavingProductRelatedDetail().getSavingProductType()).isReccuring()) {
            futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsAmountPerPeriod, tenure,
            		recurringInterestRate, approvalDate, tenureTypeEnum, savingFrequencyType, interestCalculationMethod,payEvery);
        } else if (SavingProductType.fromInt(product.getSavingProductRelatedDetail().getSavingProductType()).isRegular()) {
            futureValueOnMaturity = reccuringDepositInterestCalculator.calculateInterestOnMaturityFor(savingsAmountPerPeriod, tenure,
                    savingInterestRate, approvalDate, tenureTypeEnum, savingFrequencyType, interestCalculationMethod,payEvery);
        }
		
		this.actualCommencementDate = approvalDate.toDate();
		this.savingsDepositAmountPerPeriod = savingsDepositAmountPerPeriod;
		this.reccuringInterestRate = recurringInterestRate;
		this.savingInterestRate = savingInterestRate;
		this.interestType = interestType;
		this.tenure = tenure;
		this.tenureType = tenureType;
		this.frequency = frequency;
		this.payEvery = payEvery;
		this.savingScheduleInstallments.clear();
		this.total=futureValueOnMaturity.getAmount();
		this.interestAccrued = futureValueOnMaturity.getAmount().subtract(BigDecimal.valueOf(savingsDepositAmountPerPeriod.doubleValue() * tenure/payEvery.doubleValue()));
		this.interestPostEvery = interestPostEvery;
		this.interestPostFrequency = interestPostFrequency;
		
		// for interest posting initializing
        this.lastInterestPostedDate = this.actualCommencementDate;
        this.nextInterestPostingDate = new LocalDate(this.lastInterestPostedDate).plusMonths(this.interestPostEvery).toDate();
        this.interestPostedAmount = BigDecimal.ZERO;
	}

	public void depositMoney(SavingAccountDepositCommand command) {
		
		BigDecimal depositAmount = BigDecimal.ZERO;
		BigDecimal remainAmountToBePaid = BigDecimal.ZERO;
		BigDecimal commandRemainDepositAmount = command.getSavingsDepostiAmountPerPeriod();
		
		SavingAccountTransaction savingAccountTransaction = SavingAccountTransaction.deposit(command.getSavingsDepostiAmountPerPeriod(),command.getDepositDate());
		savingAccountTransaction.updateAccount(this);
		this.savingAccountTransactions.add(savingAccountTransaction);
		this.outstandingAmount = this.outstandingAmount.add(command.getSavingsDepostiAmountPerPeriod()); 
		this.totalSavingsAmount = this.totalSavingsAmount.add(command.getSavingsDepostiAmountPerPeriod());
		
		for(SavingScheduleInstallments savingScheduleInstallment : this.savingScheduleInstallments){
			if(!savingScheduleInstallment.isCompleted()){
				depositAmount = savingScheduleInstallment.getDeposit();
				remainAmountToBePaid = depositAmount.subtract(savingScheduleInstallment.getDepositPaid());
				if (remainAmountToBePaid.doubleValue() > 0) {
					if (commandRemainDepositAmount.doubleValue() > 0) {
						if (commandRemainDepositAmount.doubleValue() >= remainAmountToBePaid.doubleValue()) {
							savingScheduleInstallment.setDepositPaid(remainAmountToBePaid.add(savingScheduleInstallment.getDepositPaid()));
							savingScheduleInstallment.setPaymentDate(command.getDepositDate().toDate());
							savingScheduleInstallment.setCompleted(true);
							commandRemainDepositAmount = commandRemainDepositAmount.subtract(remainAmountToBePaid);
						} else if(commandRemainDepositAmount.doubleValue() < remainAmountToBePaid.doubleValue()){
							savingScheduleInstallment.setDepositPaid(commandRemainDepositAmount.add(savingScheduleInstallment.getDepositPaid()));
							savingScheduleInstallment.setPaymentDate(command.getDepositDate().toDate());
							commandRemainDepositAmount = BigDecimal.ZERO;
						}
					}
				}
			}
		}
	}

	public boolean isActive() {
		return DepositAccountStatus.fromInt(this.accountStatus).isActive();
	}

	public void withdrawAmount(BigDecimal amount, LocalDate transactionDate) {
		this.outstandingAmount = this.outstandingAmount.subtract(amount);
		SavingAccountTransaction savingAccountTransaction = SavingAccountTransaction.withdraw(amount,transactionDate);
		savingAccountTransaction.updateAccount(this);
		this.savingAccountTransactions.add(savingAccountTransaction);
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

	public void postInterest(ReccuringDepositInterestCalculator reccuringDepositInterestCalculator) {
		
		LocalDate lastInterestPostedDate = getLastInterestPostedDate();
        @SuppressWarnings("unused")
		LocalDate nextInterestPostingDate = getNextInterestPostedDate();
        @SuppressWarnings("unused")
		SavingInterestCalculationMethod savingInterestCalculationMethod = SavingInterestCalculationMethod.fromInt(this.interestCalculationMethod);
        
        Integer monthsForInterestCalculation = Months.monthsBetween(lastInterestPostedDate, new LocalDate()).getMonths();
        Integer postInterestItereations = monthsForInterestCalculation / this.interestPostEvery;
        MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
        BigDecimal interestRateAsFraction = reccuringInterestRate.divide(BigDecimal.valueOf(100), mc);
        final Integer monthsInYear = 12;
        
        BigDecimal totalDepositedAmount = BigDecimal.ZERO;
		BigDecimal totalWithdrawableAmount = BigDecimal.ZERO;
		BigDecimal totalOutstandingAmount = BigDecimal.ZERO;
		boolean isOutstandingAmountCalculated = false;
        
        while (postInterestItereations > 0 ) {
        	lastInterestPostedDate = getLastInterestPostedDate();
			nextInterestPostingDate = getNextInterestPostedDate();
			LocalDate transactionStartDate = lastInterestPostedDate;
			
			BigDecimal termTotalAmount = BigDecimal.ZERO;
			BigDecimal averageBalanceForTerm = BigDecimal.ZERO;
			
			for (int i=0; i< interestPostEvery; i++){
				//default for months here
				LocalDate transactionEndDate = transactionStartDate.plusMonths(1);
				BigDecimal depositedAmount = BigDecimal.ZERO;
				BigDecimal withdrawableAmount = BigDecimal.ZERO;
				BigDecimal outstandingAmount = BigDecimal.ZERO;
				
				for(SavingAccountTransaction savingAccountTransaction : this.savingAccountTransactions){
					LocalDate actualTransactionDate = savingAccountTransaction.getTransactionDate();
					if (actualTransactionDate != null && (actualTransactionDate.isAfter(transactionStartDate) || actualTransactionDate.isEqual(transactionStartDate))) {
						if (actualTransactionDate.isBefore(transactionEndDate)) {
							if(savingAccountTransaction.getTypeOf().isDeposit()){
								depositedAmount = depositedAmount.add(savingAccountTransaction.getAmount());
							} else if (savingAccountTransaction.getTypeOf().isWithdraw()) {
								withdrawableAmount = withdrawableAmount.add(savingAccountTransaction.getAmount());
							}
						}
					} else if (actualTransactionDate != null && (actualTransactionDate.isBefore(transactionStartDate))) {
						if(!isOutstandingAmountCalculated){
							if(savingAccountTransaction.getTypeOf().isDeposit()){
								depositedAmount = depositedAmount.add(savingAccountTransaction.getAmount());
							} else if (savingAccountTransaction.getTypeOf().isWithdraw()) {
								withdrawableAmount = withdrawableAmount.add(savingAccountTransaction.getAmount());
							}
						}
					}
				}
				outstandingAmount = depositedAmount.subtract(withdrawableAmount);
				totalDepositedAmount = totalDepositedAmount.add(depositedAmount);
				totalWithdrawableAmount = totalWithdrawableAmount.add(withdrawableAmount);
				totalOutstandingAmount = totalOutstandingAmount.add(outstandingAmount);
				termTotalAmount = termTotalAmount.add(totalOutstandingAmount);
				transactionStartDate = transactionEndDate;
				isOutstandingAmountCalculated = true;
			}
		//	FIXME-MADHUKAR calculate interest on calculation method 
			
		//	if (savingInterestCalculationMethod.isAverageBalance()) {
				averageBalanceForTerm =BigDecimal.valueOf(termTotalAmount.doubleValue()/this.interestPostEvery);
				BigDecimal interestRatePerTerm = BigDecimal.valueOf(interestRateAsFraction.doubleValue()/monthsInYear.doubleValue()*this.interestPostEvery.doubleValue());
				BigDecimal interestPerTerm = averageBalanceForTerm.multiply(interestRatePerTerm);
				this.interestPostedAmount = this.interestPostedAmount.add(interestPerTerm);
				totalOutstandingAmount = totalOutstandingAmount.add(interestPerTerm);
		//	} else if(savingInterestCalculationMethod.isMonthlyCollection()){
				
			//}
			this.outstandingAmount = this.outstandingAmount . add(interestPerTerm);
			this.lastInterestPostedDate = getNextInterestPostedDate().toDate();
			this.nextInterestPostingDate = getNextInterestPostedDate().plusMonths(this.interestPostEvery).toDate();
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
            nextInterestPostingDate = new LocalDate(this.nextInterestPostingDate).plusMonths(this.interestPostEvery);
        } else {
            nextInterestPostingDate = new LocalDate(this.nextInterestPostingDate);
        }
        return nextInterestPostingDate;
    }
    
    private final LocalDate getMaturityDate(){
    	LocalDate commencementDate;
    	if (this.actualCommencementDate == null) {
			commencementDate = new LocalDate(this.projectedCommencementDate).plusMonths(this.tenure);
		} else {
			commencementDate = new LocalDate(this.actualCommencementDate).plusMonths(this.tenure);
		}
    	return commencementDate;
    }
}
