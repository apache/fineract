/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import org.joda.time.LocalDate;

public class SavingsAccountAnnualFeeData {

    private final Long id;
    private final Long accountId;
    private final String accountNo;
    private final LocalDate nextAnnualFeeDueDate;

    public static SavingsAccountAnnualFeeData instance(final Long id, final Long accountId, final String accountNo,
            final LocalDate nextAnnualFeeDueDate) {
        return new SavingsAccountAnnualFeeData(id, accountId, accountNo, nextAnnualFeeDueDate);
    }

    private SavingsAccountAnnualFeeData(final Long id, final Long accountId, final String accountNo, final LocalDate nextAnnualFeeDueDate) {
        this.id = id;
        this.accountId = accountId;
        this.accountNo = accountNo;
        this.nextAnnualFeeDueDate = nextAnnualFeeDueDate;
    }

    public Long getId() {
        return this.id;
    }

    public Long getAccountId() {
        return this.accountId;
    }

    public LocalDate getNextAnnualFeeDueDate() {
        return this.nextAnnualFeeDueDate;
    }

    public String getAccountNo() {
        return this.accountNo;
    }
}