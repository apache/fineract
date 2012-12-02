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

    public GroupAccountSummaryData(Long id, String externalId,
                                   Long productId,
                                   String productName,
                                   Integer accountStatusId) {
        this.id = id;
        this.externalId = externalId;
        this.productId = productId;
        this.productName = productName;
        this.accountStatusId = accountStatusId;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getAccountStatusId() {
        return accountStatusId;
    }
}
