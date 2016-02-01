/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.api;

import java.util.HashSet;
import java.util.Set;

/***
 * Enum of all parameters passed in while creating/updating a GL Account
 ***/
public enum GLAccountJsonInputParams {
    ID("id"), NAME("name"), PARENT_ID("parentId"), GL_CODE("glCode"), DISABLED("disabled"), MANUAL_ENTRIES_ALLOWED("manualEntriesAllowed"), TYPE(
            "type"), USAGE("usage"), DESCRIPTION("description"), TAGID("tagId");

    private final String value;

    private GLAccountJsonInputParams(final String value) {
        this.value = value;
    }

    private static final Set<String> values = new HashSet<>();
    static {
        for (final GLAccountJsonInputParams type : GLAccountJsonInputParams.values()) {
            values.add(type.value);
        }
    }

    public static Set<String> getAllValues() {
        return values;
    }

    @Override
    public String toString() {
        return name().toString().replaceAll("_", " ");
    }

    public String getValue() {
        return this.value;
    }
}