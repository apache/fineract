/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.documentmanagement.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ContentManagementException extends AbstractPlatformDomainRuleException {

    public ContentManagementException(final String filename, final String message) {
        super("error.msg.document.save", "Error while manipulating file " + filename + " due to a ContentRepository issue " + message,
                filename, message);
    }

    public ContentManagementException(final String name, final Long fileSize, final int maxFileSize) {
        super("error.msg.document.file.too.big", "Unable to save the document with name" + name + " since its file Size of "
                + fileSize / (1024 * 1024) + " MB exceeds the max permissable file size  of " + maxFileSize + " MB", name, fileSize);
    }

    public ContentManagementException(String filename, String message, Exception exception) {
        super("error.msg.document.save", "Error while manipulating file " + filename + " due to a ContentRepository issue " + message,
                filename, message, exception);
    }
}
