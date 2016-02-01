/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;

public class MonetaryCurrencyBuilder {

    private String code = "XOF";
    private int digitsAfterDecimal = 0;
    private final Integer inMultiplesOf = null;

    public MonetaryCurrency build() {
        return new MonetaryCurrency(this.code, this.digitsAfterDecimal, this.inMultiplesOf);
    }

    public MonetaryCurrencyBuilder withCode(final String withCode) {
        this.code = withCode;
        return this;
    }

    public MonetaryCurrencyBuilder withDigitsAfterDecimal(final int withDigitsAfterDecimal) {
        this.digitsAfterDecimal = withDigitsAfterDecimal;
        return this;
    }
}
