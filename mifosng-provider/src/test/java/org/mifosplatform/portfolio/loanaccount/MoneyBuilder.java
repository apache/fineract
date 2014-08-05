/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount;

import java.math.BigDecimal;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;

public class MoneyBuilder {

    private MonetaryCurrency currencyDetail = new MonetaryCurrencyBuilder().build();
    private BigDecimal newAmount = BigDecimal.ZERO;

    public Money build() {
        return Money.of(this.currencyDetail, this.newAmount);
    }

    public MoneyBuilder with(final MonetaryCurrency withDetail) {
        this.currencyDetail = withDetail;
        return this;
    }

    public MoneyBuilder with(final String withAmount) {
        this.newAmount = BigDecimal.valueOf(Double.valueOf(withAmount));
        return this;
    }
}