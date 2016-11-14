/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.client.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_client_charge_paid_by")
public class ClientChargePaidBy extends AbstractPersistableCustom<Long> {

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
