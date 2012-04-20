package org.mifosng.platform.infrastructure;

public class UsernameAlreadyExistsException extends RuntimeException {
	public UsernameAlreadyExistsException(final Throwable e) {
		super(e);
	}
}