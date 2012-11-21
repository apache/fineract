package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when permission resources are not found.
 */
public class PermissionNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public PermissionNotFoundException(String code) {
		super("error.msg.permission.code.invalid", "Permission with Code "
				+ code + " does not exist", code);
	}
}