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
package org.apache.fineract.portfolio.savings.domain;

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

    SavingsEvent(final String value) {
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
