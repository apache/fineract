package org.mifosng.platform.api.commands;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Command used for create and update fund operations.
 */
@XmlRootElement
public class FundCommand {

	private Long id;
	private String name;
	private String externalId;

	protected FundCommand() {
		//
	}
	
	public FundCommand(final String fundName, final String externalId) {
		this.name = fundName;
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