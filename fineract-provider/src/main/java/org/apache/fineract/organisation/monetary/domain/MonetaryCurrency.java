/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MonetaryCurrency {

    @Column(name = "currency_code", length = 3, nullable = false)
    private final String code;

    @Column(name = "currency_digits", nullable = false)
    private final int digitsAfterDecimal;

    @Column(name = "currency_multiplesof")
    private final Integer inMultiplesOf;

    protected MonetaryCurrency() {
        this.code = null;
        this.digitsAfterDecimal = 0;
        this.inMultiplesOf = 0;
    }

    public MonetaryCurrency(final String code, final int digitsAfterDecimal, final Integer inMultiplesOf) {
        this.code = code;
        this.digitsAfterDecimal = digitsAfterDecimal;
        this.inMultiplesOf = inMultiplesOf;
    }

    public MonetaryCurrency copy() {
        return new MonetaryCurrency(this.code, this.digitsAfterDecimal, this.inMultiplesOf);
    }

    public String getCode() {
        return this.code;
    }

    public int getDigitsAfterDecimal() {
        return this.digitsAfterDecimal;
    }

    public Integer getCurrencyInMultiplesOf() {
        return this.inMultiplesOf;
    }
}