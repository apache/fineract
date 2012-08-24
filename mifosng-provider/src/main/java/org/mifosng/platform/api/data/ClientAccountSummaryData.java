package org.mifosng.platform.api.data;

/**
 * Immutable data object for client loan accounts.
 */
public class ClientAccountSummaryData {

	private final Long id;
	private final String externalId;
	private final Long productId;
	private final String productName;
	private final Integer accountStatusId;

	public ClientAccountSummaryData(
			final Long id, 
			final String externalId,
			final Long productId, 
			final String loanProductName, 
			final Integer loanStatusId) {
		this.id = id;
		this.externalId = externalId;
		this.productId = productId;
		this.productName = loanProductName;
		this.accountStatusId = loanStatusId;
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