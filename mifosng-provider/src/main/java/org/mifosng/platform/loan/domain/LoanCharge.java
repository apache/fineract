package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosplatform.infrastructure.configuration.domain.MonetaryCurrency;
import org.mifosplatform.infrastructure.configuration.domain.Money;
import org.mifosplatform.portfolio.charge.domain.Charge;
import org.mifosplatform.portfolio.charge.domain.ChargeCalculationType;
import org.mifosplatform.portfolio.charge.domain.ChargeTimeType;
import org.mifosplatform.portfolio.charge.exception.LoanChargeWithoutMandatoryFieldException;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity 
@Table(name = "m_loan_charge")
public class LoanCharge extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id", nullable=false)
    private Loan loan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable=false)
    private Charge charge;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;
    
	@Temporal(TemporalType.DATE)
	@Column(name = "due_for_collection_as_of_date")
	private Date dueForCollectionAsOfDate;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;
    
    @Column(name = "calculation_percentage", scale = 6, precision = 19, nullable = true)
    private BigDecimal percentage;
    
    @Column(name = "calculation_on_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPercentageAppliedTo;
    
    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "amount_paid_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPaid;
    
	@Column(name = "amount_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWaived;

    @Column(name = "amount_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWrittenOff;
    
	@Column(name = "amount_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountOutstanding;

	@Column(name = "is_penalty", nullable=false)
	private boolean penaltyCharge = false;
    
	@Column(name = "is_paid_derived", nullable=false)
	private boolean paid = false;

    public static LoanCharge createNew(final Loan loan, final Charge chargeDefinition, final LoanChargeCommand command) {
        return new LoanCharge(loan, chargeDefinition, command);
    }

    /*
     * loanPrincipal is required for charges that are percentage based
     */
	public static LoanCharge createNewWithoutLoan(final Charge chargeDefinition, final LoanChargeCommand command, final BigDecimal loanPrincipal) {
		return new LoanCharge(chargeDefinition, command, loanPrincipal);
	}
	
	public static LoanCharge createNew(final Charge chargeDefinition) {
		return new LoanCharge(null, chargeDefinition);
	}

    protected LoanCharge() {
    	//
    }
    
    public LoanCharge(final Charge chargeDefinition, final LoanChargeCommand command, final BigDecimal loanPrincipal) {
    	 this.loan = null;
    	 this.charge = chargeDefinition;
    	 this.penaltyCharge = chargeDefinition.isPenalty();
    	 
    	 if (command.isChargeTimeTypeChanged()) {
             this.chargeTime = command.getChargeTimeType();
         } else {
             this.chargeTime = chargeDefinition.getChargeTime();
         }
    	 
    	 if (ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.SPECIFIED_DUE_DATE)) {
    		 
    		 if (command.getSpecifiedDueDate() == null) {
    			final String defaultUserMessage = "Loan charge is missing specified due date";
				throw new LoanChargeWithoutMandatoryFieldException("loancharge", "specifiedDueDate", defaultUserMessage, command.getId(), chargeDefinition.getName());
    		 }
    		 
    		 this.dueForCollectionAsOfDate = command.getSpecifiedDueDate().toDate();
    	 } else {
    		 this.dueForCollectionAsOfDate = null;
    	 }

         if (command.isChargeCalculationTypeChanged()) {
             this.chargeCalculation = command.getChargeCalculationType();
         } else {
             this.chargeCalculation = chargeDefinition.getChargeCalculation();
         }
         
         BigDecimal chargeAmount = chargeDefinition.getAmount();
         if (command.isAmountChanged()){
             chargeAmount = command.getAmount();
         }
         
         populateDerivedFields(loanPrincipal, chargeAmount);
         this.paid = determineIfFullyPaid();
	}
    
    private LoanCharge(final Loan loan, final Charge chargeDefinition, final LoanChargeCommand command) {
        this.loan = loan;
        this.charge = chargeDefinition;
        this.penaltyCharge = chargeDefinition.isPenalty();
        if (command.isChargeTimeTypeChanged()){
            this.chargeTime = command.getChargeTimeType();
        } else {
            this.chargeTime = chargeDefinition.getChargeTime();
        }
        
		if (ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.SPECIFIED_DUE_DATE)) {

			if (command.getSpecifiedDueDate() == null) {
				final String defaultUserMessage = "Loan charge is missing specified due date";
				throw new LoanChargeWithoutMandatoryFieldException(
						"loancharge", "specifiedDueDate", defaultUserMessage,
						command.getId(), chargeDefinition.getName());
			}

			this.dueForCollectionAsOfDate = command.getSpecifiedDueDate().toDate();
		} else {
			this.dueForCollectionAsOfDate = null;
		}

        if (command.isChargeCalculationTypeChanged()){
            this.chargeCalculation = command.getChargeCalculationType();
        } else {
            this.chargeCalculation = chargeDefinition.getChargeCalculation();
        }
        
        BigDecimal chargeAmount = chargeDefinition.getAmount();
        if (command.isAmountChanged()){
        	chargeAmount = command.getAmount();
        }
        
        populateDerivedFields(loan.getPrincpal().getAmount(), chargeAmount);
        this.paid = determineIfFullyPaid();
    }

	private void populateDerivedFields(final BigDecimal loanPrincipal, final BigDecimal chargeAmount) {
		
		switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
		case INVALID:
			this.percentage = null;
			this.amount = null;
			this.amountPercentageAppliedTo = null;
			this.amountPaid = null;
			this.amountOutstanding = BigDecimal.ZERO;
			this.amountWaived = null;
			this.amountWrittenOff = null;
			break;
		case FLAT:
			this.percentage = null;
			this.amount = chargeAmount;
			this.amountPercentageAppliedTo = null;
			this.amountPaid = null;
			this.amountOutstanding = chargeAmount;
			this.amountWaived = null;
			this.amountWrittenOff = null;
			break;
		case PERCENT_OF_AMOUNT:
			this.percentage = chargeAmount;
			this.amountPercentageAppliedTo = loanPrincipal;
			this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
			this.amountPaid = null;
			this.amountOutstanding = calculateOutstanding();
			this.amountWaived = null;
			this.amountWrittenOff = null;
			break;
		case PERCENT_OF_AMOUNT_AND_INTEREST:
			this.percentage = null;
			this.amount = null;
			this.amountPercentageAppliedTo = null;
			this.amountPaid = null;
			this.amountOutstanding = BigDecimal.ZERO;
			this.amountWaived = null;
			this.amountWrittenOff = null;
			break;
		case PERCENT_OF_INTEREST:
			this.percentage = null;
			this.amount = null;
			this.amountPercentageAppliedTo = null;
			this.amountPaid = null;
			this.amountOutstanding = BigDecimal.ZERO;
			this.amountWaived = null;
			this.amountWrittenOff = null;
			break;
		}
	}

	private LoanCharge(final Loan loan, final Charge chargeDefinition) {
        this(loan, chargeDefinition, chargeDefinition.getAmount(), chargeDefinition.getChargeTime(), chargeDefinition.getChargeCalculation());
    }

    private LoanCharge(final Loan loan, final Charge charge, final BigDecimal amount, final Integer chargeTime, final Integer chargeCalculation) {
        this.loan = loan;
        this.charge = charge;
        this.amount = amount;
        this.chargeTime = chargeTime;
        this.chargeCalculation = chargeCalculation;
    }
    
    public void markAsFullyPaid() {
		this.amountPaid = this.amount;
		this.amountOutstanding = BigDecimal.ZERO;
		this.paid = true;
	}

	public void resetToOriginal(final MonetaryCurrency currency) {
		this.amountPaid = BigDecimal.ZERO;
		this.amountWaived = BigDecimal.ZERO;
		this.amountWrittenOff = BigDecimal.ZERO;
		this.amountOutstanding = calculateAmountOutstanding(currency);
		this.paid = false;
	}
	
	public void resetPaidAmount(final MonetaryCurrency currency) {
		this.amountPaid = BigDecimal.ZERO;
		this.amountOutstanding = calculateAmountOutstanding(currency);
		this.paid = false;
	}
    
    public Money waive(final MonetaryCurrency currency) {
    	this.amountWaived = this.amount;
		this.amountOutstanding = calculateAmountOutstanding(currency);
		this.paid = determineIfFullyPaid();
		return getAmountWaived(currency);
	}

	private BigDecimal calculateAmountOutstanding(final MonetaryCurrency currency) {
		return getAmount(currency).minus(getAmountWaived(currency)).minus(getAmountPaid(currency)).getAmount();
	}

	public void update(final Loan loan) {
		this.loan = loan;
	}

    public void update(final LoanChargeCommand command, final BigDecimal loanPrincipal){

        if (command.isChargeTimeTypeChanged()){
            this.chargeTime = ChargeTimeType.fromInt(command.getChargeTimeType()).getValue();
        }

        if (command.isChargeCalculationTypeChanged()){
            this.chargeCalculation = ChargeCalculationType.fromInt(command.getChargeCalculationType()).getValue();
        }
        
        if (command.isSpecifiedDueDateChanged()) {
        	this.dueForCollectionAsOfDate = command.getSpecifiedDueDate().toDate();
        }
        
        if (command.isAmountChanged()) {
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
			case INVALID:
				break;
			case FLAT:
				 this.amount = command.getAmount();
				break;
			case PERCENT_OF_AMOUNT:
				this.percentage = command.getAmount();
				this.amountPercentageAppliedTo = loanPrincipal;
				this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
				this.amountOutstanding = calculateOutstanding();
				break;
			case PERCENT_OF_AMOUNT_AND_INTEREST:
				this.percentage = command.getAmount();
				this.amount = null;
				this.amountPercentageAppliedTo = null;
				this.amountOutstanding = null;
				break;
			case PERCENT_OF_INTEREST:
				this.percentage = command.getAmount();
				this.amount = null;
				this.amountPercentageAppliedTo = null;
				this.amountOutstanding = null;
				break;
			}
        }
    }

	public boolean isDueAtDisbursement() {
		return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.DISBURSEMENT);
	}
	
	public boolean isSpecifiedDueDate() {
		return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.SPECIFIED_DUE_DATE);
	}

	private boolean isGreaterThanZero(final BigDecimal value) {
		return value.compareTo(BigDecimal.ZERO) == 1;
	}

	public LoanChargeCommand toCommand() {
		
		final LocalDate specifiedDueDate = getDueForCollectionAsOfLocalDate();
		final Set<String> modifiedParameters = new HashSet<String>();
		return new LoanChargeCommand(modifiedParameters, this.getId(), this.loan.getId(), this.charge.getId(),
                this.amount, this.chargeTime, this.chargeCalculation, specifiedDueDate);
	}

	public LocalDate getDueForCollectionAsOfLocalDate() {
		LocalDate specifiedDueDate = null;
		if (this.dueForCollectionAsOfDate != null) {
			specifiedDueDate = new LocalDate(this.dueForCollectionAsOfDate);
		}
		return specifiedDueDate;
	}
	
    private boolean determineIfFullyPaid() {
		return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
	}

	private BigDecimal calculateOutstanding() {
    	
    	BigDecimal amountPaidLocal = BigDecimal.ZERO;
    	if (this.amountPaid != null) {
    		amountPaidLocal = this.amountPaid;
    	}
    	
    	BigDecimal amountWaivedLocal = BigDecimal.ZERO;
    	if (this.amountWaived != null) {
    		amountWaivedLocal = this.amountWaived;
    	}
    	
    	BigDecimal amountWrittenOffLocal = BigDecimal.ZERO;
    	if (this.amountWrittenOff != null) {
    		amountWrittenOffLocal = this.amountWrittenOff;
    	}
    	
    	final BigDecimal totalAccountedFor = amountPaidLocal.add(amountWaivedLocal).add(amountWrittenOffLocal);
    	
		return this.amount.subtract(totalAccountedFor);
	}

	private BigDecimal percentageOf(final BigDecimal value, final BigDecimal percentage) {
		
		BigDecimal percentageOf = BigDecimal.ZERO;
		
		if (isGreaterThanZero(value)) {
			final MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
			BigDecimal multiplicand = percentage.divide(BigDecimal.valueOf(100l), mc);
			percentageOf = value.multiply(multiplicand, mc);
		}
		
    	return percentageOf;
	}

	public BigDecimal amount() {
		return this.amount;
	}
	
	public boolean hasNotLoanIdentifiedBy(final Long loanId) {
		return !hasLoanIdentifiedBy(loanId);
	}
	
	public boolean hasLoanIdentifiedBy(final Long loanId) {
		return this.loan.hasIdentifyOf(loanId);
	}

	public boolean isDueForCollectionBetween(final LocalDate fromNotInclusive, final LocalDate toInclusive) {
		final LocalDate specifiedDueDate = getDueForCollectionAsOfLocalDate();
		
		return specifiedDueDateFallsWithinPeriod(fromNotInclusive, toInclusive, specifiedDueDate);
	}

	private boolean specifiedDueDateFallsWithinPeriod(
			final LocalDate fromNotInclusive, 
			final LocalDate toInclusive,
			final LocalDate specifiedDueDate) {
		return specifiedDueDate != null && fromNotInclusive.isBefore(specifiedDueDate) && (toInclusive.isAfter(specifiedDueDate) || toInclusive.isEqual(specifiedDueDate));
	}

	public boolean isFeeCharge() {
		return !this.penaltyCharge;
	}
	
	public boolean isPenaltyCharge() {
		return this.penaltyCharge;
	}
	
	public boolean isNotFullyPaid() {
		return !isPaid();
	}

	public boolean isPaid() {
		return this.paid;
	}
	
	public boolean isPaidOrPartiallyPaid(final MonetaryCurrency currency) {
		
		final Money amountWaivedOrWrittenOff = getAmountWaived(currency).plus(getAmountWrittenOff(currency));
		return Money.of(currency, this.amountPaid).plus(amountWaivedOrWrittenOff).isGreaterThanZero();
	}
	
	public boolean isWaivedOrPartiallyWaived(final MonetaryCurrency currency) {
		return getAmountWaived(currency).isGreaterThanZero();
	}

	private Money getAmount(final MonetaryCurrency currency) {
		return Money.of(currency, this.amount);
	}
	
	private Money getAmountPaid(final MonetaryCurrency currency) {
		return Money.of(currency, this.amountPaid);
	}
	
	public Money getAmountWaived(final MonetaryCurrency currency) {
		return Money.of(currency, this.amountWaived);
	}
	
	public Money getAmountWrittenOff(final MonetaryCurrency currency) {
		return Money.of(currency, this.amountWrittenOff);
	}

	public Money updatePaidAmountBy(final Money incrementBy) {
		
		Money amountPaidToDate = Money.of(incrementBy.getCurrency(), this.amountPaid);
		Money amountOutstanding = Money.of(incrementBy.getCurrency(), this.amountOutstanding);
		
		Money amountPaidOnThisCharge = Money.zero(incrementBy.getCurrency());
		if (incrementBy.isGreaterThanOrEqualTo(amountOutstanding)) {
			amountPaidOnThisCharge = amountOutstanding;
			amountPaidToDate = amountPaidToDate.plus(amountOutstanding);
			this.amountPaid = amountPaidToDate.getAmount();
			this.amountOutstanding = BigDecimal.ZERO;
		} else {
			amountPaidOnThisCharge = incrementBy;
			amountPaidToDate = amountPaidToDate.plus(incrementBy);
			this.amountPaid = amountPaidToDate.getAmount();
			
			Money amountExpected = Money.of(incrementBy.getCurrency(), this.amount);
			this.amountOutstanding = amountExpected.minus(amountPaidToDate).getAmount();
		}
		
		this.paid = determineIfFullyPaid();
		
		return incrementBy.minus(amountPaidOnThisCharge);
	}

	public String name() {
		return this.charge.getName();
	}
}