package org.mifosplatform.organisation.staff.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when staff resources are not found.
 */
public class StaffNotFoundException extends
		AbstractPlatformResourceNotFoundException {

	public StaffNotFoundException(final Long id) {
		super("error.msg.staff.id.invalid", "Staff with identifier " + id
				+ " does not exist", id);
	}
}