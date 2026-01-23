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
package org.apache.fineract.accounting.reconciliation.api;

import java.util.HashSet;
import java.util.Set;

public enum ReconciliationJsonInputParams {

    GL_ACCOUNT_ID("glAccountId"), //
    FILE_NAME("fileName"), //
    FROM_DATE("fromDate"), //
    TO_DATE("toDate"), //
    OPENING_BALANCE("openingBalance"), //
    CLOSING_BALANCE("closingBalance"), //
    TRANSACTIONS("transactions"), //
    BANK_TRANSACTION_ID("bankTransactionId"), //
    GL_ENTRY_ID("glEntryId"), //
    ADJUSTMENT_DATE("adjustmentDate"), //
    DESCRIPTION("description"), //
    DEBIT_ACCOUNT_ID("debitAccountId"), //
    CREDIT_ACCOUNT_ID("creditAccountId"), //
    AMOUNT("amount"), //
    RULE_NAME("ruleName"), //
    RULE_TYPE("ruleType"), //
    MATCH_FIELD("matchField"), //
    MATCH_PATTERN("matchPattern"), //
    DATE_TOLERANCE_DAYS("dateToleranceDays"), //
    AMOUNT_TOLERANCE("amountTolerance"), //
    PRIORITY("priority"), //
    IS_ACTIVE("isActive"); //

    private final String value;

    ReconciliationJsonInputParams(final String value) {
        this.value = value;
    }

    private static final Set<String> values = new HashSet<>();

    static {
        for (final ReconciliationJsonInputParams type : ReconciliationJsonInputParams.values()) {
            values.add(type.value);
        }
    }

    public static Set<String> getAllValues() {
        return values;
    }

    @Override
    public String toString() {
        return name().replace("_", " ");
    }

    public String getValue() {
        return this.value;
    }
}
