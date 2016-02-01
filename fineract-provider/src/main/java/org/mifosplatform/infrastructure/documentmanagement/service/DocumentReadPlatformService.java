/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;
import org.mifosplatform.infrastructure.documentmanagement.data.FileData;

public interface DocumentReadPlatformService {

    Collection<DocumentData> retrieveAllDocuments(String entityType, Long entityId);

    FileData retrieveFileData(String entityType, Long entityId, Long documentId);

    DocumentData retrieveDocument(String entityType, Long entityId, Long documentId);

}