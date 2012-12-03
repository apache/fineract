package org.mifosplatform.useradministration.exception;

import org.springframework.security.core.context.SecurityContext;

/**
 * A {@link RuntimeException} that is thrown in the case where no authenticated
 * user exists within the platform {@link SecurityContext}.
 */
public class UnAuthenticatedUserException extends RuntimeException {
    // no added behaviour
}
