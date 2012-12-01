package org.mifosplatform.infrastructure.documentmanagement.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;

public interface DocumentReadPlatformService {

    Collection<DocumentData> retrieveAllDocuments(String entityType, Long entityId);

    DocumentData retrieveDocument(String entityType, Long entityId, Long documentId);
}