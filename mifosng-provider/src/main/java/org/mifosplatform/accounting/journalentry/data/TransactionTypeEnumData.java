/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.data;

/**
 * Immutable data object represent loan and Savings status enumerations.
 */
@SuppressWarnings("unused")
public class TransactionTypeEnumData {

    private final Long id;
    private final String code;
    private final String value;

    public TransactionTypeEnumData(final Long id, final String code, final String value) {
        this.id = id;
        this.code = code;
        this.value = value;
    }

    public Long id() {
        return this.id;
    }

}