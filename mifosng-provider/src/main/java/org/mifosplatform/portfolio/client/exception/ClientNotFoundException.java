package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class ClientNotFoundException extends AbstractPlatformResourceNotFoundException {

	public ClientNotFoundException(Long id) {
		super("error.msg.client.id.invalid", "Client with identifier " + id + " does not exist", id);
	}
}