package org.mifosplatform.infrastructure.documentmanagement.domain;

import java.util.HashMap;
import java.util.Map;

public enum StorageType {
    FILE_SYSTEM(1), S3(2);

    private Integer value;

    StorageType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    private static final Map<Integer, StorageType> intToEnumMap = new HashMap<Integer, StorageType>();
    static {
        for (final StorageType type : StorageType.values()) {
            intToEnumMap.put(type.value, type);
        }
    }

    public static StorageType fromInt(final int i) {
        final StorageType type = intToEnumMap.get(Integer.valueOf(i));
        return type;
    }
}