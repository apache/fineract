package org.mifosplatform.infrastructure.documentmanagement.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * Runtime exception for invalid image types
 */
public class InvalidImageTypeException extends AbstractPlatformResourceNotFoundException {

    public InvalidImageTypeException(String imageType) {
        super("error.documentmanagement.imagetype.invalid", "Image type not supported: " + imageType, imageType);
    }
}
