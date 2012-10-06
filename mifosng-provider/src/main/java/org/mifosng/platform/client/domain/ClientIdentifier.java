package org.mifosng.platform.client.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.ClientIdentifierCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.CodeValue;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "m_client_identifier")
public class ClientIdentifier extends AbstractAuditableCustom<AppUser, Long> {

	@ManyToOne
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@ManyToOne
	@JoinColumn(name = "document_type_id", nullable = false)
	private CodeValue documentType;

	@SuppressWarnings("unused")
	@Column(name = "document_key", length = 1000)
	private String documentKey;

	@SuppressWarnings("unused")
	@Column(name = "description", length = 1000)
	private String description;

	public ClientIdentifier() {
		this.client = null;
		this.documentType = null;
		this.documentKey = null;
		this.description = null;
	}

	public static ClientIdentifier createNew(Client client,
			final CodeValue documentType, final String documentKey,
			final String description) {
		return new ClientIdentifier(client, documentType, documentKey,
				description);
	}

	private ClientIdentifier(Client client, final CodeValue documentType,
			final String documentKey, final String description) {
		this.client = client;
		this.documentType = documentType;
		this.documentKey = StringUtils.defaultIfEmpty(documentKey, null);
		this.description = StringUtils.defaultIfEmpty(description, null);
	}

	public void update(final ClientIdentifierCommand command,
			final CodeValue documentType) {
		if (command.isDocumentTypeChanged()) {
			this.documentType = documentType;
		}

		if (command.isDocumentKeyChanged()) {
			this.documentKey = StringUtils.defaultIfEmpty(
					command.getDocumentKey(), null);
		}

		if (command.isDescriptionChanged()) {
			this.description = StringUtils.defaultIfEmpty(
					command.getDescription(), null);
		}

	}

}