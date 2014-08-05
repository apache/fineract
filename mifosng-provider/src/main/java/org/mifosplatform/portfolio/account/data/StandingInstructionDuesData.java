/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class StandingInstructionDuesData {

    private final LocalDate dueDate;
    private final BigDecimal totalDueAmount;

    public StandingInstructionDuesData(final LocalDate dueDate, final BigDecimal totalDueAmount) {
        this.dueDate = dueDate;
        this.totalDueAmount = totalDueAmount;
    }

    public LocalDate dueDate() {
        return this.dueDate;
    }

    public BigDecimal totalDueAmount() {
        return this.totalDueAmount;
    }

}
