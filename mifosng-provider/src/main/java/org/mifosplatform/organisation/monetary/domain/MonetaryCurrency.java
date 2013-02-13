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

    protected MonetaryCurrency() {
        this.code = null;
        this.digitsAfterDecimal = 0;
    }

    public MonetaryCurrency(final String code, final int digitsAfterDecimal) {
        this.code = code;
        this.digitsAfterDecimal = digitsAfterDecimal;
    }

    public MonetaryCurrency copy() {
        return new MonetaryCurrency(this.code, this.digitsAfterDecimal);
    }

    public String getCode() {
        return code;
    }

    public int getDigitsAfterDecimal() {
        return digitsAfterDecimal;
    }
}