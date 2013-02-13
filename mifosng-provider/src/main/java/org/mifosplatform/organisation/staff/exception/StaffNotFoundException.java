/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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