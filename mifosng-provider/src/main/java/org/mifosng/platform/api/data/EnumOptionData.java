package org.mifosng.platform.api.data;

import java.io.Serializable;

public class EnumOptionData implements Serializable {

	private Long id;
	private String code;
	private String value;

	protected EnumOptionData() {
		//
	}

	public EnumOptionData(final Long id, final String code, final String value) {
		this.id = id;
		this.code = code;
		this.value = value;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}