/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.domain;

import java.util.HashMap;
import java.util.Map;

public enum JournalEntryType {

    CREDIT(1, "journalEntryType.credit"), DEBIT(2, "journalEntrytType.debit");

    private final Integer value;
    private final String code;

    private JournalEntryType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    private static final Map<Integer, JournalEntryType> intToEnumMap = new HashMap<Integer, JournalEntryType>();
    static {
        for (final JournalEntryType type : JournalEntryType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static JournalEntryType fromInt(final int i) {
        final JournalEntryType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    @Override
    public String toString() {
        return name().toString();
    }

}
