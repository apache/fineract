package org.mifosplatform.accounting.closure.api;

import java.util.HashSet;
import java.util.Set;

/***
 * Enum of all parameters passed in while creating/updating a loan product
 ***/
public enum GLClosureJsonInputParams {
    ID("id"), OFFICE_ID("officeId"), CLOSING_DATE("closingDate"), COMMENTS("comments"), LOCALE("locale"), DATE_FORMAT("dateFormat");

    private final String value;

    private GLClosureJsonInputParams(final String value) {
        this.value = value;
    }

    private static final Set<String> values = new HashSet<String>();
    static {
        for (final GLClosureJsonInputParams type : GLClosureJsonInputParams.values()) {
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