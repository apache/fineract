/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.data;

/**
 * Immutable data object for group loan accounts.
 */
public class GroupAccountSummaryData {

    private final Long id;
    private final String externalId;
    private final Long productId;
    private final String productName;
    private final Integer accountStatusId;
    private final String accountNo;

    public GroupAccountSummaryData(final Long id, final String externalId, final Long productId, final String productName,
            final Integer accountStatusId, final String accountNo) {
        this.id = id;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = productName;
        this.accountStatusId = accountStatusId;
        this.accountNo = accountNo;
    }

    public Long getId() {
        return this.id;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getProductName() {
        return this.productName;
    }

    public Integer getAccountStatusId() {
        return this.accountStatusId;
    }
}
