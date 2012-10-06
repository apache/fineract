package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

public class ClientIdentifierData {

	private final Long id;
	private final Long clientId;
	private final Long documentTypeId;
	private final String documentTypeName;

	private final String documentKey;
	private final String description;
	private List<CodeValueData> allowedDocumentTypes = new ArrayList<CodeValueData>();

	public ClientIdentifierData(final Long id, final Long clientId,
			final Long documentTypeId, final String documentKey,
			final String description, final String documentTypeName) {
		this.id = id;
		this.clientId = clientId;
		this.documentTypeId = documentTypeId;
		this.documentKey = documentKey;
		this.description = description;
		this.documentTypeName = documentTypeName;
	}

	public ClientIdentifierData(final Long clientId, final Long documentTypeId,
			final String documentKey, final String description,
			final String documentTypeName,
			List<CodeValueData> allowedDocumentTypes) {
		this.id = null;
		this.clientId = clientId;
		this.documentTypeId = documentTypeId;
		this.documentKey = documentKey;
		this.description = description;
		this.documentTypeName = documentTypeName;
		this.allowedDocumentTypes = allowedDocumentTypes;
	}

	public Long getId() {
		return id;
	}

	public Long getClientId() {
		return clientId;
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

	public List<CodeValueData> getAllowedDocumentTypes() {
		return allowedDocumentTypes;
	}

	public void setAllowedDocumentTypes(List<CodeValueData> allowedDocumentTypes) {
		this.allowedDocumentTypes = allowedDocumentTypes;
	}

	public String getDocumentTypeName() {
		return documentTypeName;
	}

}