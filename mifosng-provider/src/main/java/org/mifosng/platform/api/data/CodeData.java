package org.mifosng.platform.api.data;

import java.io.Serializable;

public class CodeData implements Serializable {

	private Long id;
	private String codeName;

	public CodeData() {
		//
	}

	public CodeData(final Long id, final String codeName) {
		this.id = id;
		this.codeName = codeName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setName(String codeName) {
		this.codeName = codeName;
	}

}