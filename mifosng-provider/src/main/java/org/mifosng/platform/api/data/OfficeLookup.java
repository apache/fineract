package org.mifosng.platform.api.data;

import java.io.Serializable;

public class OfficeLookup implements Serializable {

	private Long id;
	private String name;
	private String nameDecorated;

	public OfficeLookup() {
		//
	}

	public OfficeLookup(final Long id, final String name,
			final String nameDecorated) {
		this.id = id;
		this.name = name;
		this.nameDecorated = nameDecorated;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getNameDecorated() {
		return nameDecorated;
	}

}