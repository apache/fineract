/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * Runtime exception for invalid image types
 */
public class InvalidEntityTypeForImageManagementException extends AbstractPlatformResourceNotFoundException {

    public InvalidEntityTypeForImageManagementException(String imageType) {
        super("error.imagemanagement.entitytype.invalid", "Image Management is not support for the Entity Type: " + imageType);
    }
}
