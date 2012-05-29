package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when role resources are not found.
 */
public class RoleNotFoundException extends AbstractPlatformResourceNotFoundException {

	public RoleNotFoundException(Long id) {
		super("error.msg.role.id.invalid", "Role with identifier " + id + " does not exist", id);
	}
}