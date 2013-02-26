/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.data;

import java.math.BigDecimal;

/**
 * Immutable data object representing currency.
 */
public class MoneyData {

    private final String code;
    private final BigDecimal amount;
    private final int decimalPlaces;

    public MoneyData(final String code, final BigDecimal amount, final int decimalPlaces) {
        this.code = code;
        this.amount = amount;
        this.decimalPlaces = decimalPlaces;
    }

    public String getCode() {
        return this.code;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public int getDecimalPlaces() {
        return this.decimalPlaces;
    }

}