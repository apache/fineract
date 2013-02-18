package org.mifosplatform.accounting.glaccount.api;

import java.util.HashSet;
import java.util.Set;

/***
 * Enum of all parameters passed in while creating/updating a loan product
 ***/
public enum GLAccountJsonInputParams {
    ID("id"), NAME("name"), PARENT_ID("parentId"), GL_CODE("glCode"), DISABLED("disabled"), MANUAL_ENTRIES_ALLOWED("manualEntriesAllowed"), TYPE(
            "type"), USAGE("usage"), DESCRIPTION("description");

    private final String value;

    private GLAccountJsonInputParams(final String value) {
        this.value = value;
    }

    private static final Set<String> values = new HashSet<String>();
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