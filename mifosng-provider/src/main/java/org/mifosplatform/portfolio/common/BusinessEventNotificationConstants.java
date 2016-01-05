/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.common;

import java.util.HashSet;
import java.util.Set;

public class BusinessEventNotificationConstants {

    public static enum BUSINESS_EVENTS {
        LOAN_APPROVED("loan_approved"), LOAN_UNDO_APPROVAL("loan_undo_approval"), LOAN_UNDO_DISBURSAL("loan_undo_disbursal"), LOAN_UNDO_LASTDISBURSAL("loan_undo_lastdisbursal"),LOAN_UNDO_TRANSACTION(
                "loan_undo_transaction"), LOAN_ADJUST_TRANSACTION("loan_adjust_transaction"), LOAN_MAKE_REPAYMENT(
                "loan_repayment_transaction"), LOAN_WRITTEN_OFF("loan_writtenoff"), LOAN_UNDO_WRITTEN_OFF("loan_undo_writtenoff"), LOAN_DISBURSAL(
                "loan_disbursal"), LOAN_WAIVE_INTEREST("loan_waive_interest"), LOAN_CLOSE("loan_close"), LOAN_CLOSE_AS_RESCHEDULE(
                "loan_close_as_reschedule"), LOAN_ADD_CHARGE("loan_add_charge"), LOAN_UPDATE_CHARGE("loan_update_charge"), LOAN_WAIVE_CHARGE(
                "loan_waive_charge"), LOAN_DELETE_CHARGE("loan_delete_charge"), LOAN_CHARGE_PAYMENT("loan_charge_payment"), LOAN_INITIATE_TRANSFER(
                "loan_initiate_transfer"), LOAN_ACCEPT_TRANSFER("loan_accept_transfer"), LOAN_WITHDRAW_TRANSFER("loan_withdraw_transfer"), LOAN_REJECT_TRANSFER(
                "loan_reject_transfer"), LOAN_REASSIGN_OFFICER("loan_reassign_officer"), LOAN_REMOVE_OFFICER("loan_remove_officer"), LOAN_APPLY_OVERDUE_CHARGE(
                "loan_apply_overdue_charge"), LOAN_INTEREST_RECALCULATION("loan_interest_recalculation"), LOAN_REFUND("loan_refund");

        private final String value;

        private BUSINESS_EVENTS(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();
        static {
            for (final BUSINESS_EVENTS type : BUSINESS_EVENTS.values()) {
                values.add(type.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static enum BUSINESS_ENTITY {
        LOAN("loan"), LOAN_TRANSACTION("loan_transaction"), LOAN_CHARGE("loan_charge"), LOAN_ADJUSTED_TRANSACTION(
                "loan_adjusted_transaction");

        private final String value;

        private BUSINESS_ENTITY(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
