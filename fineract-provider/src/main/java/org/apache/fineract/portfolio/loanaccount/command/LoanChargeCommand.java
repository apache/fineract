/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.command;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;

/**
 * Java object representation of {@link LoanCharge} API JSON.
 */
public class LoanChargeCommand implements Comparable<LoanChargeCommand> {

    @SuppressWarnings("unused")
    private final Long id;
    private final Long chargeId;
    private final BigDecimal amount;
    @SuppressWarnings("unused")
    private final Integer chargeTimeType;
    @SuppressWarnings("unused")
    private final Integer chargeCalculationType;
    @SuppressWarnings("unused")
    private final LocalDate dueDate;

    public LoanChargeCommand(final Long id, final Long chargeId, final BigDecimal amount, final Integer chargeTimeType,
            final Integer chargeCalculationType, final LocalDate dueDate) {
        this.id = id;
        this.chargeId = chargeId;
        this.amount = amount;
        this.chargeTimeType = chargeTimeType;
        this.chargeCalculationType = chargeCalculationType;
        this.dueDate = dueDate;
    }

    @Override
    public int compareTo(final LoanChargeCommand o) {
        int comparison = this.chargeId.compareTo(o.chargeId);
        if (comparison == 0) {
            comparison = this.amount.compareTo(o.amount);
        }
        return comparison;
    }
}