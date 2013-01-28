package org.mifosplatform.organisation.office.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when office transaction resources are not found.
 */
public class OfficeTransactionNotFoundException extends AbstractPlatformResourceNotFoundException {

    public OfficeTransactionNotFoundException(final Long id) {
        super("error.msg.officetransaction.id.invalid", "Office transaction with identifier " + id + " does not exist", id);
    }
}