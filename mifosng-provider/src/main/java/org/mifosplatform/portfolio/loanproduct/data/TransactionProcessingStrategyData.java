/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.data;

/**
 * Immutable data object representing a transaction strategy option for a loan.
 */
public final class TransactionProcessingStrategyData {

    private final Long id;
    @SuppressWarnings("unused")
    private final String code;
    private final String name;

    public TransactionProcessingStrategyData(final Long id, final String code, final String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Long id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }
}