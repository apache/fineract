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
package org.apache.fineract.accounting.journalentry.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.client.domain.ClientTransactionType;

public class ClientTransactionDTO {

    private final Long officeId;
    private final Long clientId;
    private String currencyCode;

    private final Long transactionId;
    private final Date transactionDate;
    private final Long paymentTypeId;
    private final EnumOptionData transactionType;

    private final BigDecimal amount;

    private final Boolean accountingEnabled;

    /*** Boolean values determines if the transaction is reversed ***/
    private final boolean reversed;

    /** Breakdowns of fees this Transaction pays **/
    private final List<ClientChargePaymentDTO> chargePayments;

    public ClientTransactionDTO(final Long clientId, final Long officeId, final Long paymentTypeId, final Long transactionId,
            final Date transactionDate, final EnumOptionData transactionType, final String currencyCode, final BigDecimal amount,
            final boolean reversed, final boolean accountingEnabled, final List<ClientChargePaymentDTO> clientChargePayments) {
        this.clientId = clientId;
        this.paymentTypeId = paymentTypeId;
        this.transactionId = transactionId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.reversed = reversed;
        this.transactionType = transactionType;
        this.chargePayments = clientChargePayments;
        this.officeId = officeId;
        this.accountingEnabled = accountingEnabled;
        this.currencyCode = currencyCode;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public Date getTransactionDate() {
        return this.transactionDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public Long getPaymentTypeId() {
        return this.paymentTypeId;
    }

    public EnumOptionData getTransactionType() {
        return transactionType;
    }

    public boolean isChargePayment() {
        return ClientTransactionType.PAY_CHARGE.getValue().equals(new Integer(this.transactionType.getId().intValue()));
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public List<ClientChargePaymentDTO> getChargePayments() {
        return this.chargePayments;
    }

    public Long getTransactionId() {
        return this.transactionId;
    }

    public Boolean getAccountingEnabled() {
        return this.accountingEnabled;
    }

    public Long getClientId() {
        return this.clientId;
    }

}
