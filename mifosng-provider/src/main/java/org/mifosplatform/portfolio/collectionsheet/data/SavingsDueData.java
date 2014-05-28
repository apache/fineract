/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.data;

import java.math.BigDecimal;

import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for representing loan with dues (example: loan is due
 * for disbursement, repayments).
 */
public class SavingsDueData {

    @SuppressWarnings("unused")
    private final Long savingsId;
    @SuppressWarnings("unused")
    private final String accountId;
    @SuppressWarnings("unused")
    private final Integer accountStatusId;
    private final String productName;
    private final Long productId;
    @SuppressWarnings("unused")
    private final CurrencyData currency;
    @SuppressWarnings("unused")
    private BigDecimal dueAmount = BigDecimal.ZERO;

    public static SavingsDueData instance(final Long savingsId, final String accountId, final Integer accountStatusId,
            final String productName, final Long productId, final CurrencyData currency, final BigDecimal dueAmount) {
        return new SavingsDueData(savingsId, accountId, accountStatusId, productName, productId, currency, dueAmount);
    }

    private SavingsDueData(final Long savingsId, final String accountId, final Integer accountStatusId, final String productName,
            final Long productId, final CurrencyData currency, final BigDecimal dueAmount) {
        this.savingsId = savingsId;
        this.accountId = accountId;
        this.accountStatusId = accountStatusId;
        this.productName = productName;
        this.productId = productId;
        this.currency = currency;
        this.dueAmount = dueAmount;
    }
    
    public String productName() {
        return this.productName;
    }
    
    public Long productId() {
        return this.productId;
    }
    
}