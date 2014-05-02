package org.mifosplatform.portfolio.account.service;

import org.mifosplatform.portfolio.account.data.PortfolioAccountData;

public interface AccountAssociationsReadPlatformService {

    public PortfolioAccountData retriveLoanAssociation(final Long loanId);

    public boolean isLinkedWithAnyActiveAccount(final Long savingsId);

    public PortfolioAccountData retriveSavingsAssociation(final Long savingsId);
}
