package org.mifosng.platform.documentmanagement.service;

import java.util.Collection;

import org.mifosng.platform.api.data.DocumentData;

public interface DocumentReadPlatformService {

	Collection<DocumentData> retrieveAllDocuments(String entityType,
			Long entityId);

	DocumentData retrieveDocument(String entityType, Long entityId,
			Long documentId);

}