package org.mifosng.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExtraDatasetRow {

	private Integer id;
	private String name;
	private String type;
	protected ExtraDatasetRow() {
		//
	}

	public ExtraDatasetRow(final Integer id, final String name, final String type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}