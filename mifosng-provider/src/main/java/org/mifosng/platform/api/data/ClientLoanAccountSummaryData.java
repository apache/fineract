package org.mifosng.platform.api.data;

import java.io.Serializable;

public class ClientLoanAccountSummaryData implements Serializable {

	private Long id;
	private String externalId;
	private Long loanProductId;
	private String loanProductName;
	private Integer loanStatusId;

	public ClientLoanAccountSummaryData() {
		//
	}

	public ClientLoanAccountSummaryData(Long id, String externalId,
			Long productId, String loanProductName, Integer loanStatusId) {
		this.id = id;
		this.externalId = externalId;
		this.loanProductId = productId;
		this.loanProductName = loanProductName;
		this.loanStatusId = loanStatusId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getLoanProductName() {
		return loanProductName;
	}

	public void setLoanProductName(String loanProductName) {
		this.loanProductName = loanProductName;
	}

	public Long getLoanProductId() {
		return loanProductId;
	}

	public void setLoanProductId(Long loanProductId) {
		this.loanProductId = loanProductId;
	}

	public Integer getLoanStatusId() {
		return loanStatusId;
	}

	public void setLoanStatusId(Integer loanStatusId) {
		this.loanStatusId = loanStatusId;
	}
}