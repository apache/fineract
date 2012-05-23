package org.mifosng.data.command;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

/**
 * Command used for create and update office operations.
 */
@XmlRootElement
public class OfficeCommand {

	private Long id;
	private String name;
	private String externalId;

	private String dateFormat;
	private String openingDateFormatted;
	private LocalDate openingDate;

	private Long parentId;
	private Boolean rootOffice = false;

	protected OfficeCommand() {
		//
	}
	
	public OfficeCommand(final String officeName, final String externalId,
			final Long parentId, final LocalDate openingDate) {
		this.name = officeName;
		this.externalId = externalId;
		this.parentId = parentId;
		this.openingDate = openingDate;
	}

	public OfficeCommand(final Long id, final String officeName,
			final String externalId, final Long parentId,
			final LocalDate openingDate) {
		this.id = id;
		this.name = officeName;
		this.externalId = externalId;
		this.parentId = parentId;
		this.openingDate = openingDate;
	}

	public LocalDate getOpeningDate() {
		return this.openingDate;
	}

	public void setOpeningDate(final LocalDate openingDate) {
		this.openingDate = openingDate;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getExternalId() {
		return this.externalId;
	}

	public void setExternalId(final String externalId) {
		this.externalId = externalId;
	}

	public Long getParentId() {
		return this.parentId;
	}

	public void setParentId(final Long parentId) {
		this.parentId = parentId;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getOpeningDateFormatted() {
		return openingDateFormatted;
	}

	public void setOpeningDateFormatted(String openingDateFormatted) {
		this.openingDateFormatted = openingDateFormatted;
	}

	public boolean isRootOffice() {
		return rootOffice;
	}

	public void setRootOffice(boolean rootOffice) {
		this.rootOffice = rootOffice;
	}
}