package org.mifosng.platform.api.commands;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a client identifier.
 */
public class ClientIdentifierCommand {

	private final Long id;
	private final Long clientId;
	private final Long documentTypeId;

	private final String documentKey;
	private final String description;

	private final Set<String> modifiedParameters;

	public ClientIdentifierCommand(final Set<String> modifiedParameters,
			final Long id, final Long clientId, final Long documentTypeId,
			final String documentKey, final String description) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.clientId = clientId;
		this.documentTypeId = documentTypeId;
		this.documentKey = documentKey;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public Long getDocumentTypeId() {
		return documentTypeId;
	}

	public String getDocumentKey() {
		return documentKey;
	}

	public String getDescription() {
		return description;
	}

	public Set<String> getModifiedParameters() {
		return modifiedParameters;
	}

	public Long getClientId() {
		return clientId;
	}

	public boolean isDocumentTypeChanged() {
		return this.modifiedParameters.contains("documentTypeId");
	}

	public boolean isDocumentKeyChanged() {
		return this.modifiedParameters.contains("documentKey");
	}

	public boolean isDescriptionChanged() {
		return this.modifiedParameters.contains("description");
	}

}