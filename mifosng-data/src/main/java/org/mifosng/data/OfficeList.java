package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OfficeList {

	private Collection<OfficeData> offices = new ArrayList<OfficeData>();

	protected OfficeList() {
		//
	}

	public OfficeList(final Collection<OfficeData> offices) {
		this.offices = offices;
	}

	public Collection<OfficeData> getOffices() {
		return this.offices;
	}

	public void setOffices(final Collection<OfficeData> offices) {
		this.offices = offices;
	}
}