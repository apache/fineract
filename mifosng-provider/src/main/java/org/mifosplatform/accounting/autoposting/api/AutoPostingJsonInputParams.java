package org.mifosplatform.accounting.autoposting.api;

import java.util.HashSet;
import java.util.Set;

/***
 * Enum of all parameters passed in while creating/updating a loan product
 ***/
public enum AutoPostingJsonInputParams {
    ID("id"), OFFICE_ID("officeId"), NAME("name"), DESCRIPTION("description"), PRODUCT_TYPE_ENUM("productTypeEnum"), PRODUCT_ID("productId"), CHARGE_ID(
            "chargeId"), EVENT_ID("eventCodeId"), EVENT_ATTRIBUTE_ID("eventAttributeId"), ACCOUNTING_RULE_ID("accountingRuleId");

    private final String value;

    private AutoPostingJsonInputParams(final String value) {
        this.value = value;
    }

    private static final Set<String> values = new HashSet<String>();
    static {
        for (final AutoPostingJsonInputParams type : AutoPostingJsonInputParams.values()) {
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