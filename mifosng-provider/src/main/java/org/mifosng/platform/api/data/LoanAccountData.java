package org.mifosng.platform.api.data;

import org.codehaus.jackson.map.annotate.JsonFilter;

@JsonFilter("loanFilter")
public class LoanAccountData {

	private Long id;
	private LoanBasicDetailsData basicDetails;
	private DerivedLoanData loanData;
	private LoanPermissionData permissions;
	
	protected LoanAccountData() {
		//
	}

	public LoanAccountData(Long id, LoanBasicDetailsData basicDetails, DerivedLoanData loanData, LoanPermissionData permissions) {
		this.id = id;
		this.basicDetails = basicDetails;
		this.loanData = loanData;
		this.permissions = permissions;
	}

	public LoanPermissionData getPermissions() {
		return permissions;
	}

	public void setPermissions(LoanPermissionData permissions) {
		this.permissions = permissions;
	}

	public LoanBasicDetailsData getBasicDetails() {
		return basicDetails;
	}

	public void setBasicDetails(LoanBasicDetailsData basicDetails) {
		this.basicDetails = basicDetails;
	}

	public DerivedLoanData getLoanData() {
		return loanData;
	}

	public void setLoanData(DerivedLoanData loanData) {
		this.loanData = loanData;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}