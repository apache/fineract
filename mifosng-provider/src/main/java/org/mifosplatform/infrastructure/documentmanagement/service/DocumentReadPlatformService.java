/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.documentmanagement.data.DocumentData;

public interface DocumentReadPlatformService {

    Collection<DocumentData> retrieveAllDocuments(String entityType, Long entityId);

    DocumentData retrieveDocument(String entityType, Long entityId, Long documentId);
}