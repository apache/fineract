/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

import java.math.BigDecimal;

public class ClientChargePaymentDTO {

    private final Long chargeId;
    private final BigDecimal amount;
    private final Long clientChargeId;
    private final boolean isPenalty;
    private final Long incomeAccountId;

    public ClientChargePaymentDTO(Long chargeId, BigDecimal amount, Long clientChargeId, boolean isPenalty, Long incomeAccountId) {
        super();
        this.chargeId = chargeId;
        this.amount = amount;
        this.clientChargeId = clientChargeId;
        this.isPenalty = isPenalty;
        this.incomeAccountId = incomeAccountId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Long getClientChargeId() {
        return this.clientChargeId;
    }

    public boolean isPenalty() {
        return this.isPenalty;
    }

    public Long getChargeId() {
        return chargeId;
    }

    public Long getIncomeAccountId() {
        return this.incomeAccountId;
    }

}
