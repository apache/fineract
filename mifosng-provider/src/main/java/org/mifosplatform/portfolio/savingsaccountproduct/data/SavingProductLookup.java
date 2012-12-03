package org.mifosplatform.portfolio.savingsaccountproduct.data;

import java.io.Serializable;

public class SavingProductLookup implements Serializable {
	private Long id;
	private String name;

	public SavingProductLookup() {
		//
	}

	public SavingProductLookup(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
