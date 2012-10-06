package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class CodeValueNotFoundException extends AbstractPlatformResourceNotFoundException {

	public CodeValueNotFoundException(Long id) {
		super("error.msg.client.id.invalid", "Code value with identifier " + id + " does not exist", id);
	}
}