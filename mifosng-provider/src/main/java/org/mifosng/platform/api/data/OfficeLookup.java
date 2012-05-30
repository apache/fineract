package org.mifosng.platform.api.data;

import java.io.Serializable;

public class OfficeLookup implements Serializable {

	private Long id;
	private String name;

	public OfficeLookup() {
		//
	}

	public OfficeLookup(final Long id, final String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

}