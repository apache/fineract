package org.mifosplatform.portfolio.client.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_client_charge_paid_by")
public class ClientChargePaidBy extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "client_transaction_id", nullable = false)
    private ClientTransaction clientTransaction;

    @ManyToOne
    @JoinColumn(name = "client_charge_id", nullable = false)
    private ClientCharge clientCharge;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected ClientChargePaidBy() {

    }

    public ClientChargePaidBy(final ClientTransaction clientTransaction, final ClientCharge clientCharge, final BigDecimal amount) {
        this.clientTransaction = clientTransaction;
        this.clientCharge = clientCharge;
        this.amount = amount;
    }

}
