package org.mifosplatform.portfolio.account.data;

public class AccountAssociationsData {

    private final Long id;
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
