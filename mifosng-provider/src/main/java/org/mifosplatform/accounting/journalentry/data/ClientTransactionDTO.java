/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.client.domain.ClientTransactionType;

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
