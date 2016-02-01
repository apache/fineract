/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.service;

import java.io.InputStream;

import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DocumentWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'CREATE_DOCUMENT')")
    Long createDocument(DocumentCommand documentCommand, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'UPDATE_DOCUMENT')")
    CommandProcessingResult updateDocument(DocumentCommand documentCommand, InputStream inputStream);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'DELETE_DOCUMENT')")
    CommandProcessingResult deleteDocument(DocumentCommand documentCommand);

}