/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

public class LoanChargePaidByData {

    private final Long id;
    private final BigDecimal amount;
    private final Integer installmentNumber;
    private final Long chargeId;
    private final Long transactionId;

    public LoanChargePaidByData(final Long id, final BigDecimal amount, final Integer installmentNumber, final Long chargeId,
            final Long transactionId) {
        this.id = id;
        this.amount = amount;
        this.installmentNumber = installmentNumber;
        this.chargeId = chargeId;
        this.transactionId = transactionId;
    }

    public Long getId() {
        return this.id;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    public Long getTransactionId() {
        return this.transactionId;
    }

}
