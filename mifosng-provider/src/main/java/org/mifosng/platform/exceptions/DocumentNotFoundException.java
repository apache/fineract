package org.mifosng.platform.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class DocumentNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public DocumentNotFoundException(final String entityType,
			final Long entityId, final Long id) {
		super("error.msg.document.id.invalid", "Document with identifier " + id
				+ " does not exist for the " + entityType + " with Identifier "
				+ entityId, id);
	}
}
