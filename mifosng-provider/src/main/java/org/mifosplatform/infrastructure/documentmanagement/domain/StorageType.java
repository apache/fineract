/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.domain;

import java.util.HashMap;
import java.util.Map;

public enum StorageType {
    FILE_SYSTEM(1), S3(2);

    private Integer value;

    StorageType(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }

    private static final Map<Integer, StorageType> intToEnumMap = new HashMap<>();
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