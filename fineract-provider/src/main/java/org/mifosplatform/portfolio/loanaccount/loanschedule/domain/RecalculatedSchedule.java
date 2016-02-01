/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.domain;

public class RecalculatedSchedule {

    private final LoanScheduleModel loanScheduleModel;
    private final Integer installmentNumber;

    public RecalculatedSchedule(final LoanScheduleModel loanScheduleModel, final Integer installmentNumber) {
        this.loanScheduleModel = loanScheduleModel;
        this.installmentNumber = installmentNumber;
    }

    public LoanScheduleModel getLoanScheduleModel() {
        return this.loanScheduleModel;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }
}
