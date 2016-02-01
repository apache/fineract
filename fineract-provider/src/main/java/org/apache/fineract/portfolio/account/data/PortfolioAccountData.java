/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object representing a savings account.
 */
@SuppressWarnings("unused")
public class PortfolioAccountData {

    private final Long id;
    private final String accountNo;
    private final String externalId;
    private final Long groupId;
    private final String groupName;
    private final Long clientId;
    private final String clientName;
    private final Long productId;
    private final String productName;
    private final Long fieldOfficerId;
    private final String fieldOfficerName;
    private final CurrencyData currency;
    private final BigDecimal amtForTransfer;

    public static PortfolioAccountData lookup(final Long accountId, final String accountNo) {
        return new PortfolioAccountData(accountId, accountNo, null, null, null, null, null, null, null, null, null, null, null);
    }

    public PortfolioAccountData(final Long id, final String accountNo, final String externalId, final Long groupId, final String groupName,
            final Long clientId, final String clientName, final Long productId, final String productName, final Long fieldofficerId,
            final String fieldofficerName, final CurrencyData currency) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.fieldOfficerId = fieldofficerId;
        this.fieldOfficerName = fieldofficerName;
        this.currency = currency;
        this.amtForTransfer = null;
    }

    public PortfolioAccountData(final Long id, final String accountNo, final String externalId, final Long groupId, final String groupName,
            final Long clientId, final String clientName, final Long productId, final String productName, final Long fieldofficerId,
            final String fieldofficerName, final CurrencyData currency, final BigDecimal amtForTransfer) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.fieldOfficerId = fieldofficerId;
        this.fieldOfficerName = fieldofficerName;
        this.currency = currency;
        this.amtForTransfer = amtForTransfer;
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final PortfolioAccountData rhs = (PortfolioAccountData) obj;
        return new EqualsBuilder().append(this.id, rhs.id).append(this.accountNo, rhs.accountNo).append(this.productId, rhs.productId)
                .append(this.productName, rhs.productName).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.id).append(this.accountNo).append(this.productId).append(this.productName)
                .toHashCode();
    }

    public Long clientId() {
        return this.clientId;
    }

    public CurrencyData currency() {
        return this.currency;
    }

    public String currencyCode() {
        return this.currency.code();
    }

    public BigDecimal getAmtForTransfer() {
        return this.amtForTransfer;
    }

    public Long accountId() {
        return this.id;
    }
}