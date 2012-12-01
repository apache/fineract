package org.mifosplatform.portfolio.client.data;

import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object represent client identity data.
 */
public class ClientIdentifierData {

    private final Long id;
    private final Long clientId;
    private final Long documentTypeId;
    private final String documentTypeName;
    private final String documentKey;
    private final String description;
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> allowedDocumentTypes;

    public static ClientIdentifierData singleItem(final Long id, final Long clientId, final Long documentTypeId, final String documentKey,
            final String description, final String documentTypeName) {
        return new ClientIdentifierData(id, clientId, documentTypeId, documentKey, description, documentTypeName, null);
    }

    public static ClientIdentifierData template(final Collection<CodeValueData> codeValues) {
        return new ClientIdentifierData(null, null, null, null, null, null, codeValues);
    }

    public static ClientIdentifierData template(final ClientIdentifierData data, final Collection<CodeValueData> codeValues) {
        return new ClientIdentifierData(data.id, data.clientId, data.documentTypeId, data.documentKey, data.description,
                data.documentTypeName, codeValues);
    }

    public ClientIdentifierData(final Long id, final Long clientId, final Long documentTypeId, final String documentKey,
            final String description, final String documentTypeName, final Collection<CodeValueData> allowedDocumentTypes) {
        this.id = id;
        this.clientId = clientId;
        this.documentTypeId = documentTypeId;
        this.documentKey = documentKey;
        this.description = description;
        this.documentTypeName = documentTypeName;
        this.allowedDocumentTypes = allowedDocumentTypes;
    }
}