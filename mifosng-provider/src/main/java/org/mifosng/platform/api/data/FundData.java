package org.mifosng.platform.api.data;

import java.io.Serializable;

public class FundData implements Serializable {

	private Long id;
	private String name;
	private String externalId;

	public FundData() {
		//
	}

	public FundData(final Long id, final String name, final String externalId) {
		this.id = id;
		this.name = name;
		this.externalId = externalId;
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

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
}