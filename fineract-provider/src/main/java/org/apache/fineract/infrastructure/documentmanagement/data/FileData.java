/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileData {

    private static final Logger logger = LoggerFactory.getLogger(FileData.class);

    private final File file;
    private final String fileName;
    private final String contentType;
    private final InputStream inputStream;

    public FileData(final File file, final String fileName, final String contentType) {
        this.file = file;
        this.fileName = fileName;
        this.contentType = contentType;
        this.inputStream = null;
    }

    public FileData(final InputStream inputStream, final String fileName, final String contentType) {
        this.file = null;
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public String contentType() {
        return this.contentType;
    }

    public String name() {
        return this.fileName;
    }

    public InputStream file() {
        try {
            if (this.inputStream == null) { return new FileInputStream(this.file); }
            return this.inputStream;
        } catch (final FileNotFoundException e) {
            logger.error(e.toString());
            return null;
        }
    }
}
