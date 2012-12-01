package org.mifosplatform.accounting.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when GL account closure resources are not
 * found.
 */
public class GLClosureNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GLClosureNotFoundException(final Long id) {
        super("error.msg.glclosure.id.invalid", "Accounting Closure with identifier " + id + " does not exist", id);
    }
}