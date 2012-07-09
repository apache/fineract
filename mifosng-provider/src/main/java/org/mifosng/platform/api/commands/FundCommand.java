package org.mifosng.platform.api.commands;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

/**
 * Command used for create and update fund operations.
 */
@XmlRootElement
public class FundCommand {

	private Long id;
	private String name;
	private String externalId;

	private String dateFormat;
	private String activatedOnDate;
	private LocalDate activatedOnLocalDate;
	
	private String deactivatedOnDate;
	private LocalDate deactivatedOnLocalDate;

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

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getActivatedOnDate() {
		return activatedOnDate;
	}

	public void setActivatedOnDate(String activatedOnDate) {
		this.activatedOnDate = activatedOnDate;
	}

	public LocalDate getActivatedOnLocalDate() {
		return activatedOnLocalDate;
	}

	public void setActivatedOnLocalDate(LocalDate activatedOnLocalDate) {
		this.activatedOnLocalDate = activatedOnLocalDate;
	}

	public String getDeactivatedOnDate() {
		return deactivatedOnDate;
	}

	public void setDeactivatedOnDate(String deactivatedOnDate) {
		this.deactivatedOnDate = deactivatedOnDate;
	}

	public LocalDate getDeactivatedOnLocalDate() {
		return deactivatedOnLocalDate;
	}

	public void setDeactivatedOnLocalDate(LocalDate deactivatedOnLocalDate) {
		this.deactivatedOnLocalDate = deactivatedOnLocalDate;
	}
}