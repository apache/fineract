package org.mifosng.ui.loan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CashflowFormBean implements Serializable {

	private String clientName;
	private String businessName;

	private Integer expectedLoanTerm = Integer.valueOf(12);
	
	private List<MultiTermExpense> investments = new ArrayList<MultiTermExpense>();
	private List<OneOffExpense> fixedExpenses = new ArrayList<OneOffExpense>();
	
	private List<UnitInformationOfProduct> products = new ArrayList<UnitInformationOfProduct>();
	
	private Number entrepreneurInput = Double.valueOf("0");

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public List<OneOffExpense> getFixedExpenses() {
		return fixedExpenses;
	}

	public void setFixedExpenses(List<OneOffExpense> fixedExpenses) {
		this.fixedExpenses = fixedExpenses;
	}

	public List<UnitInformationOfProduct> getProducts() {
		return products;
	}

	public void setProducts(List<UnitInformationOfProduct> products) {
		this.products = products;
	}

	public Integer getExpectedLoanTerm() {
		return expectedLoanTerm;
	}

	public void setExpectedLoanTerm(Integer expectedLoanTerm) {
		this.expectedLoanTerm = expectedLoanTerm;
	}

	public List<MultiTermExpense> getInvestments() {
		return investments;
	}

	public void setInvestments(List<MultiTermExpense> investments) {
		this.investments = investments;
	}

	public Number getEntrepreneurInput() {
		return entrepreneurInput;
	}

	public void setEntrepreneurInput(Number entrepreneurInput) {
		this.entrepreneurInput = entrepreneurInput;
	}
}