/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

public class ImageDataURLNotValidException extends AbstractPlatformDomainRuleException {

    public ImageDataURLNotValidException() {
        super("error.msg.dataURL.save", "Only GIF, PNG and JPEG Data URL's are allowed");
    }
}
