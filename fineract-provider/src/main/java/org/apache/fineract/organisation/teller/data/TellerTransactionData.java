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
package org.apache.fineract.organisation.teller.data;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;

import java.io.Serializable;
import java.util.Date;

/**
 * {@code TellerTransactionData} represents an immutable data object for a transction.
 *
 * @version 1.0.0

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
