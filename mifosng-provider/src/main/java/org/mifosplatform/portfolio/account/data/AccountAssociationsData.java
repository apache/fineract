/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

public class AccountAssociationsData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final PortfolioAccountData account;
    private final PortfolioAccountData linkedAccount;

    public AccountAssociationsData(final Long id, final PortfolioAccountData account, final PortfolioAccountData linkedAccount) {
        this.id = id;
        this.account = account;
        this.linkedAccount = linkedAccount;
    }

    public PortfolioAccountData linkedAccount() {
        return this.linkedAccount;
    }
}
