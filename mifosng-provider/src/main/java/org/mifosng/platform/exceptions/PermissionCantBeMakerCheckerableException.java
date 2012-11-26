package org.mifosng.platform.exceptions;

/**
 * A {@link RuntimeException} thrown when permission resources are not found.
 */
public class PermissionCantBeMakerCheckerableException extends
		AbstractPlatformResourceNotFoundException {

	public PermissionCantBeMakerCheckerableException(String code) {
		super("error.msg.permission.code.not.makercheckerable", "Permission with Code "
				+ code + " can't be Maker Checkerable", code);
	}
}