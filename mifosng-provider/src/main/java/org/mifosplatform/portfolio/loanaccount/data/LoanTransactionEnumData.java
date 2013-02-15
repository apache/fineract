/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

/**
 * Immutable data object represent loan status enumerations.
 */
@SuppressWarnings("unused")
public class LoanTransactionEnumData {

    private final Long id;
    private final String code;
    private final String value;

    private final boolean disbursement;
    private final boolean repaymentAtDisbursement;
    private final boolean repayment;
    private final boolean contra;
    private final boolean waiveInterest;
    private final boolean waiveCharges;
    private final boolean writeOff;
    private final boolean recoveryRepayment;

    public LoanTransactionEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
        this.disbursement = Long.valueOf(1).equals(this.id);
        this.repaymentAtDisbursement = Long.valueOf(5).equals(this.id);
        this.repayment = Long.valueOf(2).equals(this.id);
        this.contra = Long.valueOf(3).equals(this.id);
        this.waiveInterest = Long.valueOf(4).equals(this.id);
        this.waiveCharges = Long.valueOf(9).equals(this.id);
        this.writeOff = Long.valueOf(6).equals(this.id);
        this.recoveryRepayment = Long.valueOf(8).equals(this.id);
    }

    public Long id() {
        return this.id;
    }
}