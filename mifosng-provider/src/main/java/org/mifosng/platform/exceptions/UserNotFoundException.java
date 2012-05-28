package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when user resources are not found.
 */
public class UserNotFoundException extends PlatformResourceNotFoundException {

	public UserNotFoundException(Long id) {
		super("error.msg.user.id.invalid", "User with identifier " + id + " does not exist", id);
	}
}