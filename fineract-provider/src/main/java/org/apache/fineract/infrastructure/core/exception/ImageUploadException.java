/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.exception;

public class ImageUploadException extends AbstractPlatformDomainRuleException {

    public ImageUploadException() {
        super("error.msg.image.type.upload", "Only image files of type GIF,PNG and JPG are allowed ");
    }
}
