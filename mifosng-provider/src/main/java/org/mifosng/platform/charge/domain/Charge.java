package org.mifosng.platform.charge.domain;

import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "o_charge", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "name")
})
public class Charge extends AbstractAuditableCustom<AppUser, Long> {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "is_deleted", nullable=false)
    private boolean deleted = false;

    public static Charge createNew(String name, BigDecimal amount){
        return new Charge(name, amount);
    }

    public Charge() {
        this.name = null;
        this.amount = null;
    }

    public Charge(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
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
