package org.mifosplatform.portfolio.client.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object for client loan accounts.
 */
public class ClientAccountSummaryData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String accountNo;
    @SuppressWarnings("unused")
    private final String externalId;
    @SuppressWarnings("unused")
    private final Long productId;
    @SuppressWarnings("unused")
    private final String productName;

    private final Integer accountStatusId;
    private final EnumOptionData status;

    public ClientAccountSummaryData(final Long id, final String externalId, final Long productId, final String loanProductName,
            final Integer loanStatusId) {
        this.id = id;
        this.accountNo = null;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = loanProductName;
        this.accountStatusId = loanStatusId;
        this.status = null;
    }

    public ClientAccountSummaryData(final Long id, final String accountNo, final String externalId, final Long productId, final String loanProductName,
            final EnumOptionData loanStatus) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = loanProductName;
        this.accountStatusId = null;
        this.status = loanStatus;
    }

    public Integer accountStatusId() {
        Integer accountStatus = this.accountStatusId;
        if (accountStatus == null && this.status != null) {
            accountStatus = this.status.getId().intValue();
        }
        return accountStatus;
    }
}