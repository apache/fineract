package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrganisationList {

	private Collection<OrganisationReadModel> organisations = new ArrayList<OrganisationReadModel>();

	protected OrganisationList() {
		//
	}

	public OrganisationList(
			final Collection<OrganisationReadModel> organisations) {
		this.organisations = organisations;
	}

	public Collection<OrganisationReadModel> getOrganisations() {
		return this.organisations;
	}

	public void setOrganisations(
			final Collection<OrganisationReadModel> organisations) {
		this.organisations = organisations;
	}
}
