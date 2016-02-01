/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when document management functionality is
 * invoked for invalid Entity Types
 */
public class InvalidEntityTypeForDocumentManagementException extends AbstractPlatformResourceNotFoundException {

    public InvalidEntityTypeForDocumentManagementException(final String entityType) {
        super("error.documentmanagement.entitytype.invalid", "Document Management is not support for the Entity Type: " + entityType,
                entityType);
    }
}