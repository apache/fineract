package org.mifosng.platform.api.data;

import java.io.Serializable;

public class LoanProductLookup implements Serializable {

	private Long id;
	private String name;

	public LoanProductLookup() {
		//
	}

	public LoanProductLookup(Long id, String name) {
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