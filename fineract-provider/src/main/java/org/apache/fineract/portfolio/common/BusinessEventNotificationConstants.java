/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.common;

import java.util.HashSet;
import java.util.Set;

public class BusinessEventNotificationConstants {

    public static enum BUSINESS_EVENTS {
        LOAN_APPROVED("loan_approved"), LOAN_REJECTED("loan_reject"), LOAN_UNDO_APPROVAL("loan_undo_approval"), LOAN_UNDO_DISBURSAL("loan_undo_disbursal"), LOAN_UNDO_LASTDISBURSAL(
                "loan_undo_lastdisbursal"), LOAN_UNDO_TRANSACTION("loan_undo_transaction"), LOAN_ADJUST_TRANSACTION(
                "loan_adjust_transaction"), LOAN_MAKE_REPAYMENT("loan_repayment_transaction"), LOAN_WRITTEN_OFF("loan_writtenoff"), LOAN_UNDO_WRITTEN_OFF(
                "loan_undo_writtenoff"), LOAN_DISBURSAL("loan_disbursal"), LOAN_WAIVE_INTEREST("loan_waive_interest"), LOAN_CLOSE(
                "loan_close"), LOAN_CLOSE_AS_RESCHEDULE("loan_close_as_reschedule"), LOAN_ADD_CHARGE("loan_add_charge"), LOAN_UPDATE_CHARGE(
                "loan_update_charge"), LOAN_WAIVE_CHARGE("loan_waive_charge"), LOAN_DELETE_CHARGE("loan_delete_charge"), LOAN_CHARGE_PAYMENT(
                "loan_charge_payment"), LOAN_INITIATE_TRANSFER("loan_initiate_transfer"), LOAN_ACCEPT_TRANSFER("loan_accept_transfer"), LOAN_WITHDRAW_TRANSFER(
                "loan_withdraw_transfer"), LOAN_REJECT_TRANSFER("loan_reject_transfer"), LOAN_REASSIGN_OFFICER("loan_reassign_officer"), LOAN_REMOVE_OFFICER(
                "loan_remove_officer"), LOAN_APPLY_OVERDUE_CHARGE("loan_apply_overdue_charge"), LOAN_INTEREST_RECALCULATION(
                "loan_interest_recalculation"), LOAN_REFUND("loan_refund"), LOAN_FORECLOSURE("loan_foreclosure"), SAVINGS_ACTIVATE(
                "savings_activated"), SAVINGS_ADJUST_TRANSACTION("savings_adjust_transaction"), SAVINGS_APPLY_ANNUAL_FEE(
                "savings_apply_annual_fee"), SAVINGS_CALCULATE_INTEREST("savings_calculate_interest"), SAVINGS_CLOSE("savings_close"), SAVINGS_POST_INTEREST(
                "savings_post_interest"), SAVINGS_REJECT("savings_reject"), SAVINGS_DEPOSIT("savings_deposit"), SAVINGS_WITHDRAWAL(
                "savings_withdrawal"), SAVINGS_UNDO("savings_undo"), SAVINGS_ADD_CHARGE("savings_add_charge"), SAVINGS_WAIVE_CHARGE(
                "savings_waive_charge"), SAVINGS_PAY_CHARGE("savings_pay_charge"), CLIENTS_ACCEPT_TRANSFER("clients_accept_transfer"), CLIENTS_ACTIVATE(
                "clients_activate"), CLIENTS_ASSIGN_STAFF("clients_assign_staff"), CLIENTS_CLOSE("clients_close"), CLIENTS_CREATE(
                "clients_create"), CLIENTS_DELETE("clients_delete"), CLIENTS_PROPOSE_TRANSFER("clients_propose_transfer"), CLIENTS_REACTIVATE(
                "clients_reactivate"), CLIENTS_REJECT("clients_reject"), CLIENTS_REJECT_TRANSFER("clients_reject_transfer"), CLIENTS_WITHDRAW(
                "clients_withdraw"), CLIENTS_WITHDRAW_TRANSFER("clients_withdraw_transfer");

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
                "loan_adjusted_transaction"), SAVING("saving"), CLIENT("client"), SAVINGS_TRANSACTION("Savings Transaction");

        private final String value;

        private BUSINESS_ENTITY(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

}
