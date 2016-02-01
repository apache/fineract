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
package org.apache.fineract.infrastructure.documentmanagement.data;

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
