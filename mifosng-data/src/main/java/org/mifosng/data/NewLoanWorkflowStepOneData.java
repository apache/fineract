package org.mifosng.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement(name = "newloanworkflow")
public class NewLoanWorkflowStepOneData implements Serializable {

	private Long organisationId;
	private String organisationName;
	private Long clientId;
	private String clientName;
	private Long productId;
	private String productName;
	
	private List<LoanProductData> allowedProducts = new ArrayList<LoanProductData>();
	
	// extra
	private LocalDate submittedOnDate;
	private String submittedOnNote = "";
	
	private LocalDate expectedDisbursementDate = new LocalDate();
	private LocalDate repaymentsStartingFromDate;
	
	private LocalDate interestCalculatedFromDate;
	
	private LoanSchedule repaymentSchedule;
	
	private LoanProductData selectedProduct;
	
	public NewLoanWorkflowStepOneData() {
		//
	}

	public Long getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(Long organisationId) {
		this.organisationId = organisationId;
	}

	public String getOrganisationName() {
		return organisationName;
	}

	public void setOrganisationName(String organisationName) {
		this.organisationName = organisationName;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public List<LoanProductData> getAllowedProducts() {
		return allowedProducts;
	}

	public void setAllowedProducts(List<LoanProductData> allowedProducts) {
		this.allowedProducts = allowedProducts;
	}

	public LocalDate getSubmittedOnDate() {
		return submittedOnDate;
	}

	public void setSubmittedOnDate(LocalDate submittedOnDate) {
		this.submittedOnDate = submittedOnDate;
	}

	public String getSubmittedOnNote() {
		return submittedOnNote;
	}

	public void setSubmittedOnNote(String submittedOnNote) {
		this.submittedOnNote = submittedOnNote;
	}

	public LoanProductData getSelectedProduct() {
		return selectedProduct;
	}

	public void setSelectedProduct(LoanProductData selectedProduct) {
		this.selectedProduct = selectedProduct;
	}

	public LocalDate getExpectedDisbursementDate() {
		return expectedDisbursementDate;
	}

	public void setExpectedDisbursementDate(LocalDate expectedDisbursementDate) {
		this.expectedDisbursementDate = expectedDisbursementDate;
	}

	public LocalDate getRepaymentsStartingFromDate() {
		return repaymentsStartingFromDate;
	}

	public void setRepaymentsStartingFromDate(LocalDate repaymentsStartingFromDate) {
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
	}

	public LocalDate getInterestCalculatedFromDate() {
		return interestCalculatedFromDate;
	}

	public void setInterestCalculatedFromDate(LocalDate interestCalculatedFromDate) {
		this.interestCalculatedFromDate = interestCalculatedFromDate;
	}

	public LoanSchedule getRepaymentSchedule() {
		return repaymentSchedule;
	}

	public void setRepaymentSchedule(LoanSchedule repaymentSchedule) {
		this.repaymentSchedule = repaymentSchedule;
	}
}