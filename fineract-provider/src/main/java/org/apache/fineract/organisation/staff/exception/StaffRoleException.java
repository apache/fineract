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
public class StaffRoleException extends AbstractPlatformResourceNotFoundException {

    public static enum STAFF_ROLE {
        LOAN_OFFICER, BRANCH_MANAGER,SAVINGS_OFFICER;

        @Override
        public String toString() {
            return name().toString().replaceAll("-", " ").toLowerCase();
        }
    }

    public StaffRoleException(final Long id, final STAFF_ROLE role) {
        super("error.msg.staff.id.invalid.role", "Staff with identifier " + id + " is not a " + role.toString(), id);
    }
}