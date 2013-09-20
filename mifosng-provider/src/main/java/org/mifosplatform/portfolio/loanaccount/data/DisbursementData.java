/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * Immutable data object representing disbursement information.
 */
public class DisbursementData {

    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final BigDecimal principal;

    public DisbursementData(final LocalDate expectedDisbursementDate, final LocalDate actualDisbursementDate,
            final BigDecimal principalDisbursed) {
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.principal = principalDisbursed;
    }

    public LocalDate disbursementDate() {
        LocalDate disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }

    public BigDecimal amount() {
        return this.principal;
    }

    public boolean isDisbursed() {
        return this.actualDisbursementDate != null;
    }
}