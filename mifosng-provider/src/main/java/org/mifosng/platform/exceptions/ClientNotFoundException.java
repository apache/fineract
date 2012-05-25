package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class ClientNotFoundException extends PlatformResourceNotFoundException {

	public ClientNotFoundException(Long id) {
		super("error.msg.client.id.invalid", "Client with identifier " + id + " does not exist", id);
	}
}