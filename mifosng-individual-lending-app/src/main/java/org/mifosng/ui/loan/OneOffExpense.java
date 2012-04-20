package org.mifosng.ui.loan;

import java.io.Serializable;
import java.math.BigDecimal;

public class OneOffExpense implements Serializable {

	private String name;
	private String description;
	private Number amount;

	public OneOffExpense() {
		//
	}
	
	public OneOffExpense(String name, BigDecimal amount) {
		this.name = name;
		this.amount = amount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Number getAmount() {
		return amount;
	}

	public void setAmount(Number amount) {
		this.amount = amount;
	}

}
