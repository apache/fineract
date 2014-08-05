/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class LoanTermVariationsData {

    @SuppressWarnings("unused")
    private final Long id;
    private final EnumOptionData termType;
    private final LocalDate termVariationApplicableFrom;
    private final BigDecimal termValue;

    public LoanTermVariationsData(final Long id, final EnumOptionData termType, final LocalDate termVariationApplicableFrom,
            final BigDecimal termValue) {
        this.id = id;
        this.termType = termType;
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.termValue = termValue;
    }

    public EnumOptionData getTermType() {
        return this.termType;
    }

    public LocalDate getTermApplicableFrom() {
        return this.termVariationApplicableFrom;
    }

    public BigDecimal getTermValue() {
        return this.termValue;
    }
}
