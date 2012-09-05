package org.mifosng.platform.loan.domain;

import org.mifosng.platform.api.commands.LoanChargeCommand;
import org.mifosng.platform.charge.domain.Charge;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity 
@IdClass(LoanCharge.LoanChargePK.class)
@Table(name = "m_loan_charge")
public class LoanCharge {

    @SuppressWarnings("unused")
	@Id
    private Loan loan;

    @SuppressWarnings("unused")
	@Id
    private Charge charge;

    @SuppressWarnings("unused")
	@Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @SuppressWarnings("unused")
    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;

    @SuppressWarnings("unused")
    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    protected LoanCharge() {
    	//
    }

    public LoanCharge(Loan loan, Charge charge, LoanChargeCommand command) {
        this.loan = loan;
        this.charge = charge;

        if (command.isAmountChanged()){
            this.amount = command.getAmount();
        } else {
            this.amount = charge.getAmount();
        }

        if (command.isChargeTimeTypeChanged()){
            this.chargeTime = command.getChargeTimeType();
        } else {
            this.chargeTime = charge.getChargeTime();
        }

        if (command.isChargeCalculationTypeChanged()){
            this.chargeCalculation = command.getChargeCalculationType();
        } else {
            this.chargeCalculation = charge.getChargeCalculation();
        }
    }

    public LoanCharge(final Loan loan, final Charge charge) {
        this(loan, charge, charge.getAmount(), charge.getChargeTime(), charge.getChargeCalculation());
    }

    public LoanCharge(final Loan loan, final Charge charge, final BigDecimal amount, final Integer chargeTime, final Integer chargeCalculation) {
        this.loan = loan;
        this.charge = charge;
        this.amount = amount;
        this.chargeTime = chargeTime;
        this.chargeCalculation = chargeCalculation;
    }

    public static class LoanChargePK implements Serializable {
        @SuppressWarnings("unused")
		@Id
        @ManyToOne(optional = false)
        @JoinColumn(name = "loan_id", referencedColumnName = "id", nullable=false)
        private Loan loan;

        @SuppressWarnings("unused")
		@Id
        @ManyToOne(optional = false)
        @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable=false)
        private Charge charge;
    }
}
