package org.mifosng.platform.charge.domain;

import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "m_charge", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "name")
})
public class Charge extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

	@Column(name = "currency_code", length=3)
    private String currencyCode;

    @SuppressWarnings("unused")
	@Column(name = "charge_applies_to_enum", nullable = false)
    private Integer chargeAppliesTo;

	@Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;

	@Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

	@Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_deleted", nullable=false)
    private boolean deleted = false;

    public static Charge createNew(final String name, final BigDecimal amount, final String currencyCode, 
    		final ChargeAppliesTo chargeAppliesTo, final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculationType, final boolean active) {
        return new Charge(name, amount, currencyCode, chargeAppliesTo, chargeTime, chargeCalculationType, active);
    }

    protected Charge() {
    	//
    }

    private Charge(final String name, final BigDecimal amount, final String currencyCode, 
    		final ChargeAppliesTo chargeAppliesTo, final ChargeTimeType chargeTime, 
    		final ChargeCalculationType chargeCalculationType, final boolean active) {
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeAppliesTo = chargeAppliesTo.getValue();
        this.chargeTime = chargeTime.getValue();
        this.chargeCalculation = chargeCalculationType.getValue();
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getChargeTime() {
        return chargeTime;
    }

    public Integer getChargeCalculation() {
        return chargeCalculation;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void update(final ChargeCommand command){
        if (command.isNameChanged()) {
            this.name = command.getName();
        }

        if (command.isAmountChanged()) {
            this.amount = command.getAmount();
        }

        if (command.isCurrencyCodeChanged()){
            this.currencyCode = command.getCurrencyCode();
        }

        if (command.isChargeTimeTypeChanged()){
            this.chargeTime = ChargeTimeType.fromInt(command.getChargeTimeType()).getValue();
        }

        if (command.isChargeAppliesToChanged()){
            this.chargeAppliesTo = ChargeAppliesTo.fromInt(command.getChargeAppliesTo()).getValue();
        }

        if (command.isChargeCalculationTypeChanged()){
            this.chargeCalculation = ChargeCalculationType.fromInt(command.getChargeCalculationType()).getValue();
        }

        if (command.isActiveChanged()){
            this.active = command.isActive();
        }
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on charge so it wont appear in query/report results.
     *
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.name = this.getId() + "_" + this.name;
    }
}
