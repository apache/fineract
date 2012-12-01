package org.mifosplatform.portfolio.client.data;

/**
 * Immutable data object for client loan accounts.
 */
public class ClientAccountSummaryData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String externalId;
    @SuppressWarnings("unused")
    private final Long productId;
    @SuppressWarnings("unused")
    private final String productName;
    private final Integer accountStatusId;

    public ClientAccountSummaryData(final Long id, final String externalId, final Long productId, final String loanProductName,
            final Integer loanStatusId) {
        this.id = id;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = loanProductName;
        this.accountStatusId = loanStatusId;
    }

    public Integer accountStatusId() {
        return accountStatusId;
    }
}