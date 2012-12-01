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