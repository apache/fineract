package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.charge.domain.Charge;
import org.mifosng.platform.charge.domain.ChargeCalculationType;
import org.mifosng.platform.charge.domain.ChargeTimeType;
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
    
    @SuppressWarnings("unused")
	@Column(name = "amount_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountOutstanding;
    
    @SuppressWarnings("unused")
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
    	 
    	 if (command.isChargeTimeTypeChanged()){
             this.chargeTime = command.getChargeTimeType();
         } else {
             this.chargeTime = chargeDefinition.getChargeTime();
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
         
         populateDerivedFields(loanPrincipal, chargeAmount);
         this.paid = determineIfFullyPaid();
	}
    
    private LoanCharge(final Loan loan, final Charge chargeDefinition, final LoanChargeCommand command) {
        this.loan = loan;
        this.charge = chargeDefinition;

        if (command.isChargeTimeTypeChanged()){
            this.chargeTime = command.getChargeTimeType();
        } else {
            this.chargeTime = chargeDefinition.getChargeTime();
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
			this.amountOutstanding = null;
			break;
		case FLAT:
			this.percentage = null;
			this.amount = chargeAmount;
			this.amountPercentageAppliedTo = null;
			this.amountPaid = null;
			this.amountOutstanding = chargeAmount;
			break;
		case PERCENT_OF_AMOUNT:
			this.percentage = chargeAmount;
			this.amountPercentageAppliedTo = loanPrincipal;
			this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
			this.amountPaid = null;
			this.amountOutstanding = calculateOutstanding();
			break;
		case PERCENT_OF_AMOUNT_AND_INTEREST:
			this.percentage = null;
			this.amount = null;
			this.amountPercentageAppliedTo = null;
			this.amountPaid = null;
			this.amountOutstanding = null;
			break;
		case PERCENT_OF_INTEREST:
			this.percentage = null;
			this.amount = null;
			this.amountPercentageAppliedTo = null;
			this.amountPaid = null;
			this.amountOutstanding = null;
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

//	public BigDecimal calculateMonetaryAmount(final Money principalDisbursed) {
//		BigDecimal calculatedAmount = BigDecimal.ZERO;
//		
//		   switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
//			case INVALID:
//				calculatedAmount = this.amount;
//				break;
//			case FLAT:
//				calculatedAmount = this.amount;
//				break;
//			case PERCENT_OF_AMOUNT:
//				this.amountPercentageAppliedTo = principalDisbursed.getAmount();
//				this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
//				this.amountPaid = null;
//				this.amountOutstanding = calculateOutstanding();
//				calculatedAmount = this.amount;
//				break;
//			case PERCENT_OF_AMOUNT_AND_INTEREST:
//				this.percentage = null;
//				this.amount = null;
//				this.amountPercentageAppliedTo = null;
//				this.amountPaid = null;
//				this.amountOutstanding = null;
//				break;
//			case PERCENT_OF_INTEREST:
//				this.percentage = null;
//				this.amount = null;
//				this.amountPercentageAppliedTo = null;
//				this.amountPaid = null;
//				this.amountOutstanding = null;
//				break;
//			}
//		
//		return calculatedAmount;
//	}
//	
//	public BigDecimal calculateMonetaryAmount() {
//		return calculateMonetaryAmount(this.loan.getPrincpal());
//	}

	private boolean isGreaterThanZero(final BigDecimal value) {
		return value.compareTo(BigDecimal.ZERO) == 1;
	}

	public LoanChargeCommand toData() {
		Set<String> modifiedParameters = new HashSet<String>();
		return new LoanChargeCommand(modifiedParameters, this.getId(), this.loan.getId(), this.charge.getId(),
                this.amount, this.chargeTime, this.chargeCalculation);
	}
	
    private boolean determineIfFullyPaid() {
		return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
	}

	private BigDecimal calculateOutstanding() {
    	
    	BigDecimal amountPaidLocal = BigDecimal.ZERO;
    	if (this.amountPaid != null) {
    		amountPaidLocal = this.amountPaid;
    	}
    	
		return this.amount.subtract(amountPaidLocal);
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

	public void markAsFullyPaid() {
		this.amountPaid = this.amount;
		this.amountOutstanding = BigDecimal.ZERO;
		this.paid = true;
	}

	public void resetToOriginal() {
		this.amountPaid = BigDecimal.ZERO;
		this.amountOutstanding = this.amount;
		this.paid = false;
	}

	public BigDecimal amount() {
		return this.amount;
	}
}