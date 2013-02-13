/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class DocumentManagementException extends AbstractPlatformDomainRuleException {

    public DocumentManagementException(final String name) {
        super("error.msg.document.save", "Error while manipulating file " + name + " due to a File system / Amazon S3 issue", name);
    }

    public DocumentManagementException(final String name, final Long fileSize, final int maxFileSize) {
        super("error.msg.document.file.too.big", "Unable to save the document with name" + name + " since its file Size of " + fileSize
                / (1024 * 1024) + " MB exceeds the max permissable file size  of " + maxFileSize + " MB", name, fileSize);
    }

}
