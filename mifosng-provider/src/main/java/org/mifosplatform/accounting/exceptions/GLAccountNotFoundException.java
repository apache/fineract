package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when GL account resources are not found.
 */
public class GLAccountNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GLAccountNotFoundException(final Long id) {
        super("error.msg.glaccount.id.invalid", "General Ledger account with identifier " + id + " does not exist", id);
    }
}