/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

/**
 *
 */
public enum SavingsEvent {

    SAVINGS_APPLICATION_REJECTED("application.rejected"), //
    SAVINGS_APPLICATION_WITHDRAWAL_BY_CUSTOMER("application.withdrawal"), //
    SAVINGS_APPLICATION_APPROVED("application.approval"), //
    SAVINGS_APPLICATION_APPROVAL_UNDO("application.approval.undo"), //
    SAVINGS_ACTIVATE("activate"), //
    SAVINGS_DEPOSIT("deposit"), //
    SAVINGS_WITHDRAWAL("withdraw"), //
    SAVINGS_POST_INTEREST("interest.post"), //
    SAVINGS_UNDO_TRANSACTION("transaction.undo"), //
    SAVINGS_ADJUST_TRANSACTION("transaction.adjust"), //
    SAVINGS_APPLY_CHARGE("charge.apply"), //
    SAVINGS_WAIVE_CHARGE("charge.waive"), //
    SAVINGS_PAY_CHARGE("charge.pay"), //
    SAVINGS_CLOSE_ACCOUNT("account.close");

    private final String value;

    private SavingsEvent(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
