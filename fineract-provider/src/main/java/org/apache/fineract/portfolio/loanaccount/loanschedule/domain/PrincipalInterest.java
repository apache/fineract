/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

import org.mifosplatform.organisation.monetary.domain.Money;

public class PrincipalInterest {

    private final Money principal;
    private final Money interest;
    private final Money interestPaymentDueToGrace;

    public PrincipalInterest(final Money principal, final Money interest, final Money interestPaymentDueToGrace) {
        this.principal = principal;
        this.interest = interest;
        this.interestPaymentDueToGrace = interestPaymentDueToGrace;
    }

    public Money principal() {
        return this.principal;
    }

    public Money interest() {
        return this.interest;
    }

    public Money interestPaymentDueToGrace() {
        return this.interestPaymentDueToGrace;
    }
}
