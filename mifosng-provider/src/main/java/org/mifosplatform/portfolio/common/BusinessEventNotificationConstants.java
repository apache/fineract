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
        LOAN_APPROVED("loan_approved"), LOAN_UNDO_APPROVAL("loan_undo_approval"), LOAN_UNDO_DISBURSAL("loan_undo_disbursal"), LOAN_UNDO_TRANSACTION(
                "loan_undo_transaction"), LOAN_MAKE_REPAYMENT("loan_repayment_transaction"), LOAN_WRITTEN_OFF("loan_writtenoff"), LOAN_UNDO_WRITTEN_OFF(
                "loan_undo_writtenoff");

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
}
