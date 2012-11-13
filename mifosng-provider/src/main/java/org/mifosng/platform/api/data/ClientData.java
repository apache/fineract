package org.mifosng.platform.api.data;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;

/**
 * Immutable data object representing client data.
 */
final public class ClientData {

	private final Long id;
	private final String firstname;
	private final String lastname;
	private final String clientOrBusinessName;
	private final String displayName;
	private final Long officeId;
	private final String officeName;
	private final String externalId;
	private final LocalDate joinedDate;
	private final String imageKey;
	@SuppressWarnings("unused")
	private final boolean imagePresent;
	private final List<OfficeLookup> allowedOffices;
	
	// essential override existing data with data from command
	public static ClientData mergeWithCommand(final ClientData clientData, final ClientCommand command) {
		
		final Long officeId = (Long) ObjectUtils.defaultIfNull(command.getId(), clientData.officeId);
		final String firstname = (String) ObjectUtils.defaultIfNull(command.getFirstname(), clientData.firstname);
		final String lastname = (String) ObjectUtils.defaultIfNull(command.getLastname(), clientData.lastname);
		final String clientOrBusinessName = (String) ObjectUtils.defaultIfNull(command.getClientOrBusinessName(), clientData.clientOrBusinessName);
		final String externalId = (String) ObjectUtils.defaultIfNull(command.getExternalId(), clientData.externalId);
		final LocalDate joinedDate = (LocalDate) ObjectUtils.defaultIfNull(command.getJoiningDate(), clientData.joinedDate);
		
		return new ClientData(officeId, clientData.officeName, clientData.id, firstname, lastname, clientOrBusinessName, externalId, joinedDate, clientData.imageKey, clientData.allowedOffices);
	}
	
	public static ClientData template(final Long officeId, final LocalDate joinedDate, final List<OfficeLookup> allowedOffices) {
		return new ClientData(officeId, null, null, null, null, null, null, joinedDate, null, allowedOffices);
	}
	
	public static ClientData templateOnTop(final ClientData clientData, final List<OfficeLookup> allowedOffices) {
		return new ClientData(clientData.officeId, clientData.officeName, clientData.id, clientData.firstname, clientData.lastname, clientData.clientOrBusinessName, clientData.externalId, clientData.joinedDate, clientData.imageKey, allowedOffices);
	}
	
	public static ClientData clientIdentifier(
			final Long id, 
			final String firstname,
			final String lastname, 
			final Long officeId, 
			final String officeName) {
		
		StringBuilder displayNameBuilder = new StringBuilder(firstname);
		if (StringUtils.isNotBlank(displayNameBuilder.toString())) {
			displayNameBuilder.append(' ');
		}
		displayNameBuilder.append(lastname);
		
		return new ClientData(officeId, officeName, id, firstname, lastname, displayNameBuilder.toString(), null, null, null, null);
	}

	public ClientData(final Long officeId, final String officeName,
			final Long id, final String firstname, final String lastname,
			final String displayName, final String externalId,
			final LocalDate joinedDate,
			final String imageKey,
			final List<OfficeLookup> allowedOffices) {
		this.officeId = officeId;
		this.officeName = officeName;
		this.id = id;
		this.firstname = firstname;
		this.displayName = displayName;

		/*** unset last name for business name **/
		if (StringUtils.isBlank(this.firstname)) {
			this.lastname = null;
			this.clientOrBusinessName = lastname;
		} else {
			this.lastname = lastname;
			this.clientOrBusinessName = null;
		}

		this.externalId = externalId;
		this.joinedDate = joinedDate;
		this.imageKey = imageKey;
		if (imageKey == null) {
			imagePresent = false;
		}else{
			imagePresent = true;
		}
		
		this.allowedOffices = allowedOffices;
	}

	public String displayName() {
		return this.displayName;
	}

	public String officeName() {
		return this.officeName;
	}
	
	private boolean imageKeyExists() {
		return StringUtils.isNotBlank(this.imageKey);
	}

	public boolean imageKeyDoesNotExist() {
		return !imageKeyExists();
	}

	public String imageKey() {
		return this.imageKey;
	}

	public Long id() {
		return this.id;
	}

	public Long officeId() {
		return this.officeId;
	}
}