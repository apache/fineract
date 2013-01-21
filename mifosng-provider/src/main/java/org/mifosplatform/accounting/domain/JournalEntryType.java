package org.mifosplatform.accounting.domain;

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
        return code;
    }

    private static final Map<Integer, JournalEntryType> intToEnumMap = new HashMap<Integer, JournalEntryType>();
    static {
        for (JournalEntryType type : JournalEntryType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static JournalEntryType fromInt(int i) {
        JournalEntryType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }

    @Override
    public String toString() {
        return name().toString();
    }

}
