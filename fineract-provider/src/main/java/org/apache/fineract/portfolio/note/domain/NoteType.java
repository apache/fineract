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
package org.apache.fineract.portfolio.note.domain;

import java.util.HashMap;
import java.util.Map;

public enum NoteType {

    CLIENT(100, "noteType.client", "clients"), //
    LOAN(200, "noteType.loan", "loans"), //
    LOAN_TRANSACTION(300, "noteType.loan.transaction", "loanTransactions"), //
    SAVING_ACCOUNT(500, "noteType.saving", "savings"), //
    GROUP(600, "noteType.group", "groups"),
    SHARE_ACCOUNT(700, "noteType.shares", "accounts/share"),
    SAVINGS_TRANSACTION(800, "noteType.savings.transaction", "savingsTransactions");
    
    private Integer value;
    private String code;
    private String apiUrl;

    NoteType(final Integer value, final String code, final String apiUrl) {
        this.value = value;
        this.code = code;
        this.apiUrl = apiUrl;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public String getApiUrl() {
        return this.apiUrl;
    }

    private static final Map<Integer, NoteType> intToEnumMap = new HashMap<>();
    private static int minValue;
    private static int maxValue;
    static {
        int i = 0;
        for (final NoteType type : NoteType.values()) {
            if (i == 0) {
                minValue = type.value;
            }
            intToEnumMap.put(type.value, type);
            if (minValue >= type.value) {
                minValue = type.value;
            }
            if (maxValue < type.value) {
                maxValue = type.value;
            }
            i = i + 1;
        }
    }

    public static NoteType fromInt(final int i) {
        final NoteType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    public static int getMinValue() {
        return minValue;
    }

    public static int getMaxValue() {
        return maxValue;
    }

    @Override
    public String toString() {
        return name().toString();
    }

    private static final Map<String, NoteType> apiUrlToEnumMap = new HashMap<>();

    static {
        for (final NoteType type : NoteType.values()) {
            apiUrlToEnumMap.put(type.apiUrl, type);
        }
    }

    public static NoteType fromApiUrl(final String url) {
        final NoteType type = apiUrlToEnumMap.get(url);
        return type;
    }

}
