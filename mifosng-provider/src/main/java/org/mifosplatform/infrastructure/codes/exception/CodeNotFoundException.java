package org.mifosplatform.infrastructure.codes.exception;

import org.mifosng.platform.exceptions.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when a code is not found.
 */
public class CodeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public CodeNotFoundException(final String name) {
        super("error.msg.code.not.found", "Code with name `" + name + "` does not exist", name);
    }
}