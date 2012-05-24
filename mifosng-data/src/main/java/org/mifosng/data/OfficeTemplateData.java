package org.mifosng.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.LocalDate;

@XmlRootElement(name = "office template")
public class OfficeTemplateData implements Serializable {

	private LocalDate defaultOpeningDate;
	private List<OfficeLookup> allowedParents = new ArrayList<OfficeLookup>();

	public OfficeTemplateData() {
		//
	}

	public OfficeTemplateData(final LocalDate defaultOpeningDate,
			final List<OfficeLookup> allowedParents) {
		this.defaultOpeningDate = defaultOpeningDate;
		this.allowedParents = allowedParents;
	}

	public LocalDate getDefaultOpeningDate() {
		return defaultOpeningDate;
	}

	public void setDefaultOpeningDate(LocalDate defaultOpeningDate) {
		this.defaultOpeningDate = defaultOpeningDate;
	}

	public List<OfficeLookup> getAllowedParents() {
		return allowedParents;
	}

	public void setAllowedParents(List<OfficeLookup> allowedParents) {
		this.allowedParents = allowedParents;
	}

}