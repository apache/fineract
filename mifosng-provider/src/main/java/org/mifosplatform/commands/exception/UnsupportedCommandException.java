package org.mifosplatform.commands.exception;

/**
 * A {@link RuntimeException} that is thrown in the case where an invalid or
 * unknown command is attempted to be processed by platform.
 */
public class UnsupportedCommandException extends RuntimeException {

    private final String unsupportedCommandName;

    public UnsupportedCommandException(final String unsupportedCommandName) {
        this.unsupportedCommandName = unsupportedCommandName;
    }

    public String getUnsupportedCommandName() {
        return unsupportedCommandName;
    }
}