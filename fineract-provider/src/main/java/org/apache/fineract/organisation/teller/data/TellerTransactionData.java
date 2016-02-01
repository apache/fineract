/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

import java.io.Serializable;
import java.util.Date;

/**
 * {@code TellerTransactionData} represents an immutable data object for a transction.
 *
 * @version 1.0.0
<<<<<<< HEAD
 * @since 2.0.0
 * @see java.io.Serializable
 * @since 2.0.0
 */
public final class TellerTransactionData implements Serializable {

    private final Long id;
    private final Long officeId;
    private final Long tellerId;
    private final Long cashierId;
    private final Long clientId;
    private final EnumOptionData type;
    private final Double amount;
    private final Date postingDate;

    /*
     * Sole private CTOR to create a new instance
     */
    private TellerTransactionData(final Long id, final Long officeId, final Long tellerId, final Long cashierId,
                                  final Long clientId, final EnumOptionData type, final Double amount,
                                  final Date postingDate) {
        this.id = id;
        this.officeId = officeId;
        this.tellerId = tellerId;
        this.cashierId = cashierId;
        this.clientId = clientId;
        this.type = type;
        this.amount = amount;
        this.postingDate = postingDate;
    }

    /**
     * Creates a new teller transaction data object.
     *
     * @param id          - id of the transaction
     * @param officeId    - id of the related office
     * @param tellerId    - id of the related teller
     * @param cashierId   - id of the cashier
     * @param clientId    - id of the client
     * @param type        - type of transaction (eg receipt, payment, open, close, settle)
     * @param amount      - amount of the transaction
     * @param postingDate - posting date of the transaction
     * @return the new created {@code TellerTransactionData}
     */
    public static TellerTransactionData instance(final Long id, final Long officeId, final Long tellerId, final Long cashierId,
                                                 final Long clientId, final EnumOptionData type, final Double amount,
                                                 final Date postingDate) {
        return new TellerTransactionData(id, officeId, tellerId, cashierId, clientId, type, amount, postingDate);
    }

    public Long getId() {
        return id;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public Long getTellerId() {
        return tellerId;
    }

    public Long getCashierId() {
        return cashierId;
    }

    public Long getClientId() {
        return clientId;
    }

    public EnumOptionData getType() {
        return type;
    }

    public Double getAmount() {
        return amount;
    }

    public Date getPostingDate() {
        return postingDate;
    }
}
