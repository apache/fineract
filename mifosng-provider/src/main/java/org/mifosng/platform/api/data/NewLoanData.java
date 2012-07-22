package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

public class NewLoanData {

	private Long clientId;
	private String clientName;
	private Long productId;
	private String productName;

	private LoanProductData selectedProduct;
	
	private LocalDate expectedDisbursementDate;

	private List<LoanProductLookup> allowedProducts = new ArrayList<LoanProductLookup>();
	
	public NewLoanData() {
		//
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
}