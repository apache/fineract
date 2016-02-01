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
 * Immutable data object represent document being managed on platform.
 */
public class DocumentData {

    private final Long id;
    private final String parentEntityType;
    private final Long parentEntityId;
    @SuppressWarnings("unused")
    private final String name;
    private final String fileName;
    @SuppressWarnings("unused")
    private final Long size;
    private final String type;
    @SuppressWarnings("unused")
    private final String description;
    private final String location;
    private final Integer storageType;

    public DocumentData(final Long id, final String parentEntityType, final Long parentEntityId, final String name, final String fileName,
            final Long size, final String type, final String description, final String location, final Integer storageType) {
        this.id = id;
        this.parentEntityType = parentEntityType;
        this.parentEntityId = parentEntityId;
        this.name = name;
        this.fileName = fileName;
        this.size = size;
        this.type = type;
        this.description = description;
        this.location = location;
        this.storageType = storageType;
    }

    public String contentType() {
        return this.type;
    }

    public String fileName() {
        return this.fileName;
    }

    public String fileLocation() {
        return this.location;
    }

    public StorageType storageType() {
        return StorageType.fromInt(this.storageType);
    }

    public String getParentEntityType() {
        return this.parentEntityType;
    }

    public Long getParentEntityId() {
        return this.parentEntityId;
    }

    public Long getId() {
        return this.id;
    }

}