/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.exception;

import org.springframework.security.core.context.SecurityContext;

/**
 * A {@link RuntimeException} that is thrown in the case where no authenticated
 * user exists within the platform {@link SecurityContext}.
 */
public class UnAuthenticatedUserException extends RuntimeException {
    // no added behaviour
}
