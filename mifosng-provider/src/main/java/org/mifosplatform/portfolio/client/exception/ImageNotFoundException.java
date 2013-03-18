/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ImageNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ImageNotFoundException(final String resource, final Long resourceId) {
        super("error.msg.entity.image.invalid", "Image for resource " + resource + " with Identifier " + resourceId + " does not exist",
                resource, resourceId);
    }
}