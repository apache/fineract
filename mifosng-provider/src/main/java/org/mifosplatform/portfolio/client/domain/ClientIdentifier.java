package org.mifosplatform.portfolio.client.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;

@Entity
@Table(name = "m_client_identifier", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "document_type_id", "document_key" }, name = "unique_identifier_key"),
        @UniqueConstraint(columnNames = { "client_id", "document_type_id" }, name = "unique_client_identifier") })
public class ClientIdentifier extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "document_type_id", nullable = false)
    private CodeValue documentType;

    @Column(name = "document_key", length = 1000)
    private String documentKey;

    @Column(name = "description", length = 1000)
    private String description;

    public ClientIdentifier() {
        this.client = null;
        this.documentType = null;
        this.documentKey = null;
        this.description = null;
    }

    public static ClientIdentifier createNew(final Client client, final CodeValue documentType, final String documentKey,
            final String description) {
        return new ClientIdentifier(client, documentType, documentKey, description);
    }

    private ClientIdentifier(final Client client, final CodeValue documentType, final String documentKey, final String description) {
        this.client = client;
        this.documentType = documentType;
        this.documentKey = StringUtils.defaultIfEmpty(documentKey, null);
        this.description = StringUtils.defaultIfEmpty(description, null);
    }

    public void update(final ClientIdentifierCommand command, final CodeValue documentType) {

        if (command.isDocumentTypeChanged()) {
            this.documentType = documentType;
        }

        if (command.isDocumentKeyChanged()) {
            this.documentKey = StringUtils.defaultIfEmpty(command.getDocumentKey(), null);
        }

        if (command.isDescriptionChanged()) {
            this.description = StringUtils.defaultIfEmpty(command.getDescription(), null);
        }
    }

    public Client getClient() {
        return client;
    }

    public CodeValue getDocumentType() {
        return documentType;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public String getDescription() {
        return description;
    }
}