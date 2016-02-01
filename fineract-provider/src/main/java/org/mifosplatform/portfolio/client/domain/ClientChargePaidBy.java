/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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

    public static ClientChargePaidBy instance(final ClientTransaction clientTransaction, final ClientCharge clientCharge,
            final BigDecimal amount) {
        return new ClientChargePaidBy(clientTransaction, clientCharge, amount);
    }

    public ClientChargePaidBy(final ClientTransaction clientTransaction, final ClientCharge clientCharge, final BigDecimal amount) {
        this.clientTransaction = clientTransaction;
        this.clientCharge = clientCharge;
        this.amount = amount;
    }

    public ClientTransaction getClientTransaction() {
        return this.clientTransaction;
    }

    public void setClientTransaction(ClientTransaction clientTransaction) {
        this.clientTransaction = clientTransaction;
    }

    public ClientCharge getClientCharge() {
        return this.clientCharge;
    }

    public void setClientCharge(ClientCharge clientCharge) {
        this.clientCharge = clientCharge;

    }

    public BigDecimal getAmount() {
        return this.amount;
    }

}
