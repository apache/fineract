/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.service;

import org.mifosplatform.portfolio.account.data.PortfolioAccountData;

public interface AccountAssociationsReadPlatformService {

    public PortfolioAccountData retriveLoanAssociation(final Long loanId);

    public boolean isLinkedWithAnyActiveAccount(final Long savingsId);

    public PortfolioAccountData retriveSavingsAssociation(final Long savingsId);
}
