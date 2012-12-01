package org.mifosplatform.portfolio.client.command;

import java.util.Set;

/**
 * Immutable command for creating or updating details of a client identifier.
 */
public class ClientIdentifierCommand {

    
    private final Long clientId;
    private final Long documentTypeId;

    private final String documentKey;
    private final String description;

    private final transient Set<String> modifiedParameters;
    private final transient boolean makerCheckerApproval;
    private final transient Long id;
    
    public ClientIdentifierCommand(final Set<String> modifiedParameters, final boolean makerCheckerApproval, final Long id, final Long clientId, final Long documentTypeId,
            final String documentKey, final String description) {
        this.modifiedParameters = modifiedParameters;
        this.makerCheckerApproval = makerCheckerApproval;
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

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }
}