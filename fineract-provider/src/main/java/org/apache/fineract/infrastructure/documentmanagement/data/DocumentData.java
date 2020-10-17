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

import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;

/**
 * Immutable data object representing documents being managed on platform.
 */
public class DocumentData {

    private final String fileName;
    private final String contentType;
    private final String location;
    private final StorageType storageType;

    public DocumentData(final String fileName, final String type, final String location, final StorageType storageType) {
        this.fileName = fileName;
        this.contentType = type;
        this.location = location;
        this.storageType = storageType;
    }

    public String contentType() {
        return this.contentType;
    }

    public String fileName() {
        return this.fileName;
    }

    public String fileLocation() {
        return this.location;
    }

    public StorageType storageType() {
        return this.storageType;
    }
}
