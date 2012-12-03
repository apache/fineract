package org.mifosplatform.portfolio.group.data;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.portfolio.client.data.ClientLookup;

/**
 * Immutable data object representing groups.
 */
public class GroupData {

	private final Long id;
	private final String name;
	private final String externalId;
	private final Long officeId;
	private final String officeName;

	private final Collection<ClientLookup> clientMembers;
	@SuppressWarnings("unused")
	private final Collection<ClientLookup> allowedClients;
	@SuppressWarnings("unused")
	private final Collection<OfficeLookup> allowedOffices;

	public GroupData(
			final Long id, 
			final Long officeId,
			final String officeName, 
			final String name, 
			final String externalId) {
		this.id = id;
		this.officeId = officeId;
		this.officeName = officeName;
		this.name = name;
		this.externalId = externalId;

		this.clientMembers = null;
		this.allowedClients = null;
		this.allowedOffices = null;
	}

	public GroupData(final GroupData group, final Collection<ClientLookup> clientMembers, final Collection<ClientLookup> allowedClients,
			final Collection<OfficeLookup> allowedOffices) {
		this.id = group.getId();
		this.officeId = group.getOfficeId();
		this.officeName = group.getOfficeName();
		this.name = group.getName();
		this.externalId = group.getExternalId();
		
		this.clientMembers = clientMembers;
		this.allowedClients = allowedClients;
		this.allowedOffices = allowedOffices;
	}
	
	public GroupData(final Long officeId, final Collection<ClientLookup> allowedClients, final Collection<OfficeLookup> allowedOffices) {
		this.id = null;
		this.officeId = officeId;
		this.officeName = null;
		this.name = null;
		this.externalId = null;
		this.clientMembers = null;
		this.allowedClients = allowedClients;
		this.allowedOffices = allowedOffices;
	}
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getExternalId() {
		return externalId;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public String getOfficeName() {
		return officeName;
	}

	public Collection<ClientLookup> clientMembers() {
		return this.clientMembers;
	}
}