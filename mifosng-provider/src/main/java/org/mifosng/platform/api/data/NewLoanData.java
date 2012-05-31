package org.mifosng.platform.api.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.joda.time.LocalDate;
import org.mifosng.data.LoanSchedule;

@JsonIgnoreProperties({ "organisationId", "organisationName" })
@JsonFilter("myFilter")
public class NewLoanData implements Serializable {

	private Long organisationId;
	private String organisationName;
	private Long clientId;
	private String clientName;
	private Long productId;
	private String productName;

	private List<LoanProductLookup> allowedProducts = new ArrayList<LoanProductLookup>();

	// extra
	private String dateFormat;
	private String submittedOnDateFormatted;
	private LocalDate submittedOnDate;
	private String submittedOnNote = "";

	private String expectedDisbursementDateFormatted;
	private LocalDate expectedDisbursementDate = new LocalDate();

	private String repaymentsStartingFromDateFormatted;
	private LocalDate repaymentsStartingFromDate;

	private String interestCalculatedFromDateFormatted;
	private LocalDate interestCalculatedFromDate;

	private LoanSchedule repaymentSchedule;

	private LoanProductData selectedProduct;

	public NewLoanData() {
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

	public List<LoanProductLookup> getAllowedProducts() {
		return allowedProducts;
	}

	public void setAllowedProducts(List<LoanProductLookup> allowedProducts) {
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

	public void setRepaymentsStartingFromDate(
			LocalDate repaymentsStartingFromDate) {
		this.repaymentsStartingFromDate = repaymentsStartingFromDate;
	}

	public LocalDate getInterestCalculatedFromDate() {
		return interestCalculatedFromDate;
	}

	public void setInterestCalculatedFromDate(
			LocalDate interestCalculatedFromDate) {
		this.interestCalculatedFromDate = interestCalculatedFromDate;
	}

	public LoanSchedule getRepaymentSchedule() {
		return repaymentSchedule;
	}

	public void setRepaymentSchedule(LoanSchedule repaymentSchedule) {
		this.repaymentSchedule = repaymentSchedule;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getSubmittedOnDateFormatted() {
		return submittedOnDateFormatted;
	}

	public void setSubmittedOnDateFormatted(String submittedOnDateFormatted) {
		this.submittedOnDateFormatted = submittedOnDateFormatted;
	}

	public String getExpectedDisbursementDateFormatted() {
		return expectedDisbursementDateFormatted;
	}

	public void setExpectedDisbursementDateFormatted(
			String expectedDisbursementDateFormatted) {
		this.expectedDisbursementDateFormatted = expectedDisbursementDateFormatted;
	}

	public String getRepaymentsStartingFromDateFormatted() {
		return repaymentsStartingFromDateFormatted;
	}

	public void setRepaymentsStartingFromDateFormatted(
			String repaymentsStartingFromDateFormatted) {
		this.repaymentsStartingFromDateFormatted = repaymentsStartingFromDateFormatted;
	}

	public String getInterestCalculatedFromDateFormatted() {
		return interestCalculatedFromDateFormatted;
	}

	public void setInterestCalculatedFromDateFormatted(
			String interestCalculatedFromDateFormatted) {
		this.interestCalculatedFromDateFormatted = interestCalculatedFromDateFormatted;
	}
}