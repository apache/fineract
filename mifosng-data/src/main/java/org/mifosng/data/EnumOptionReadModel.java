package org.mifosng.data;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "option")
public class EnumOptionReadModel implements Serializable {

	private Long id;
	private String value;

	public EnumOptionReadModel() {
		//
	}

	public EnumOptionReadModel(final String value, final Long id) {
		this.value = value;
		this.id = id;
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
}