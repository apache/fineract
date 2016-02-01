/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

public class LoanBorrowerCycleData {

    @SuppressWarnings("unused")
    private final BigDecimal principal;
    @SuppressWarnings("unused")
    private final BigDecimal interestRatePerPeriod;
    @SuppressWarnings("unused")
    private final Integer numberOfRepayments;
    @SuppressWarnings("unused")
    private final Integer termFrequency;

    public LoanBorrowerCycleData(final BigDecimal principal, final BigDecimal interestRatePerPeriod, final Integer numberOfRepayments,
            final Integer termFrequency) {

        this.principal = principal;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.numberOfRepayments = numberOfRepayments;
        this.termFrequency = termFrequency;
    }
}
