/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.api;

import java.util.HashSet;
import java.util.Set;

/***
 * Enum of all parameters passed in while creating/updating a journal Entry
 ***/
public enum JournalEntryJsonInputParams {
    OFFICE_ID("officeId"), TRANSACTION_DATE("transactionDate"), COMMENTS("comments"), CREDITS("credits"), DEBITS("debits"), LOCALE("locale"), DATE_FORMAT(
            "dateFormat"), REFERENCE_NUMBER("referenceNumber"), USE_ACCOUNTING_RULE("useAccountingRule"), ACCOUNTING_RULE("accountingRule"), AMOUNT(
            "amount"), CURRENCY_CODE("currencyCode"), PAYMENT_TYPE_ID("paymentTypeId"), ACCOUNT_NUMBER("accountNumber"), CHECK_NUMBER(
            "checkNumber"), ROUTING_CODE("routingCode"), RECEIPT_NUMBER("receiptNumber"), BANK_NUMBER("bankNumber");

    private final String value;

    private JournalEntryJsonInputParams(final String value) {
        this.value = value;
    }

    private static final Set<String> values = new HashSet<>();
    static {
        for (final JournalEntryJsonInputParams type : JournalEntryJsonInputParams.values()) {
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