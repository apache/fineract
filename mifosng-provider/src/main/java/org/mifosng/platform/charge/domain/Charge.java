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

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "charge_applies_to_enum", nullable = false)
    private ChargeAppliesTo chargeAppliesTo;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "charge_time_enum", nullable = false)
    private ChargeTimeType chargeTime;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "charge_calculation_enum")
    private ChargeCalculationMethod chargeCalculationMethod;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_deleted", nullable=false)
    private boolean deleted = false;

    public static Charge createNew(String name, BigDecimal amount, String currencyCode, ChargeAppliesTo chargeAppliesTo,
                                   ChargeTimeType chargeTime, ChargeCalculationMethod chargeCalculationMethod, boolean active){
        return new Charge(name, amount, currencyCode, chargeAppliesTo, chargeTime, chargeCalculationMethod, active);
    }

    public Charge() {
        this.name = null;
        this.amount = null;
        this.currencyCode = null;
        this.chargeAppliesTo = null;
        this.chargeTime = null;
        this.chargeCalculationMethod = null;

        this.active = false;
    }

    public Charge(String name, BigDecimal amount, String currencyCode, ChargeAppliesTo chargeAppliesTo,
                  ChargeTimeType chargeTime, ChargeCalculationMethod chargeCalculationMethod, boolean active) {
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeAppliesTo = chargeAppliesTo;
        this.chargeTime = chargeTime;
        this.chargeCalculationMethod = chargeCalculationMethod;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
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
            this.chargeTime = ChargeTimeType.fromInt(command.getChargeTimeType());
        }

        if (command.isChargeAppliesToChanged()){
            this.chargeAppliesTo = ChargeAppliesTo.fromInt(command.getChargeAppliesTo());
        }

        if (command.isChargeCalculationTypeChanged()){
            this.chargeCalculationMethod = ChargeCalculationMethod.fromInt(command.getChargeCalculationType());
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
