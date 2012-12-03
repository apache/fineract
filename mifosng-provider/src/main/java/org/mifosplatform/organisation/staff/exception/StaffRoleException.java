package org.mifosplatform.organisation.staff.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when staff resources are not found.
 */
public class StaffRoleException extends
		AbstractPlatformResourceNotFoundException {

	public static enum STAFF_ROLE {
		LOAN_OFFICER, BRANCH_MANAGER;
		@Override
		public String toString() {
			return name().toString().replaceAll("-", " ").toLowerCase();
		}
	}

	public StaffRoleException(final Long id, STAFF_ROLE role) {
		super("error.msg.staff.id.invalid.role", "Staff with identifier " + id
				+ " is not a " + role.toString(), id);
	}
}