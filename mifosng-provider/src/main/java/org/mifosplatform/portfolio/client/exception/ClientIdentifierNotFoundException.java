package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when client Identifier resources are not
 * found.
 */
public class ClientIdentifierNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public ClientIdentifierNotFoundException(Long id) {
		super("error.msg.clientIdentifier.id.invalid",
				"Client Identifier with the primary key " + id
						+ " does not exist", id);
	}
}