/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.domain;

import java.util.HashMap;
import java.util.Map;

public enum NoteType {

    CLIENT(100, "noteType.client", "clients"), //
    LOAN(200, "noteType.loan", "loans"), //
    LOAN_TRANSACTION(300, "noteType.loan.transaction", "loanTransactions"), //
    SAVING_ACCOUNT(500, "noteType.saving", "savings"), //
    GROUP(600, "noteType.group", "groups");

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
