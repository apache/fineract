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
package org.apache.fineract.infrastructure.bulkimport.data;

import java.util.HashMap;
import java.util.Map;

public enum GlobalEntityType {


    INVALID(0, "invalid"),
    CLIENTS_PERSON(1, "clients.person"),
    CLIENTS_ENTTTY(2,"clients.entity"),
    GROUPS(3, "groups"),
    CENTERS(4, "centers"),
    OFFICES(5, "offices"),
    STAFF(6, "staff"),
    USERS(7, "users"),
    SMS(8, "sms"),
    DOCUMENTS(9, "documents"),
    TEMPLATES(10, "templates"),
    NOTES(11, "templates"),
    CALENDAR(12, "calendar"),
    MEETINGS(13, "meetings"),
    HOLIDAYS(14, "holidays"),
    LOANS(15, "loans"),
    LOAN_PRODUCTS(16,"loancharges"),
    LOAN_TRANSACTIONS(18, "loantransactions"),
    GUARANTORS(19, "guarantors"),
    COLLATERALS(20, "collaterals"),
    FUNDS(21, "funds"),
    CURRENCY(22, "currencies"),
    SAVINGS_ACCOUNT(23, "savingsaccount"),
    SAVINGS_CHARGES(24, "savingscharges"),
    SAVINGS_TRANSACTIONS(25, "savingstransactions"),
    SAVINGS_PRODUCTS(26, "savingsproducts"),
    GL_JOURNAL_ENTRIES(27, "gljournalentries"),
    CODE_VALUE(28, "codevalue"),
    CODE(29, "code"),
    CHART_OF_ACCOUNTS(30,"chartofaccounts"),
    FIXED_DEPOSIT_ACCOUNTS(31,"fixeddepositaccounts"),
    FIXED_DEPOSIT_TRANSACTIONS(32,"fixeddeposittransactions"),
    SHARE_ACCOUNTS(33,"shareaccounts"),
    RECURRING_DEPOSIT_ACCOUNTS(34,"recurringdeposits"),
    RECURRING_DEPOSIT_ACCOUNTS_TRANSACTIONS(35,"recurringdepositstransactions"),
    CLIENT(36,"client");

    private final Integer value;
    private final String code;

    private static final Map<Integer, GlobalEntityType> intToEnumMap = new HashMap<>();
    private static final Map<String, GlobalEntityType> stringToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;

    static {
        int i = 0;
        for (final GlobalEntityType entityType : GlobalEntityType.values()) {
            if (i == 0) {
                minValue = entityType.value;
            }
            intToEnumMap.put(entityType.value, entityType);
            stringToEnumMap.put(entityType.code, entityType);
            if (minValue >= entityType.value) {
                minValue = entityType.value;
            }
            if (maxValue < entityType.value) {
                maxValue = entityType.value;
            }
            i = i + 1;
        }
    }

    private GlobalEntityType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static GlobalEntityType fromInt(final int i) {
        final GlobalEntityType entityType = intToEnumMap.get(Integer.valueOf(i));
        return entityType;
    }

    public static GlobalEntityType fromCode(final String key) {
        final GlobalEntityType entityType = stringToEnumMap.get(key);
        return entityType;
    }

    @Override
    public String toString() {
        return name().toString();
    }

}