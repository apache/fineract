/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import org.mifosplatform.organisation.monetary.domain.Money;

public class TransactionProccessingResult {

    private final Money overPaymentAmount;
    private final boolean overPayment;

    public TransactionProccessingResult(final Money overPaymentAmount, final boolean overPayment) {
        this.overPaymentAmount = overPaymentAmount;
        this.overPayment = overPayment;
    }

    public Money getOverPaymentAmount() {
        return this.overPaymentAmount;
    }

    public boolean isOverPayment() {
        return this.overPayment;
    }
}
