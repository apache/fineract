/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.domain;

public class Base64EncodedImage {

    private final String base64EncodedString;
    private final String fileExtension;

    public Base64EncodedImage(String base64EncodedString, String fileExtension) {
        this.base64EncodedString = base64EncodedString;
        this.fileExtension = fileExtension;
    }

    public String getBase64EncodedString() {
        return base64EncodedString;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}