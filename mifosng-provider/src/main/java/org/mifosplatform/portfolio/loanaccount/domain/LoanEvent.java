/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

/**
 *
 */
public enum LoanEvent {

    LOAN_CREATED, //
    LOAN_REJECTED, //
    LOAN_WITHDRAWN, //
    LOAN_APPROVED, //
    LOAN_APPROVAL_UNDO, //
    LOAN_DISBURSED, //
    LOAN_DISBURSAL_UNDO, //
    LOAN_REPAYMENT_OR_WAIVER, //
    REPAID_IN_FULL, //
    WRITE_OFF_OUTSTANDING, //
    LOAN_RESCHEDULE, //
    INTERST_REBATE_OWED, //
    LOAN_OVERPAYMENT, LOAN_CHARGE_PAYMENT;
}
