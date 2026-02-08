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
package org.apache.fineract.infrastructure.documentmanagement.command;

import java.util.Set;

@Deprecated(forRemoval = true)
public class DocumentCommand {

    private final Long id;
    private final String parentEntityType;
    private final Long parentEntityId;
    private final String name;
    private final String description;

    private String fileName;
    private Long size;
    private String type;
    private String location;

    public DocumentCommand(final Set<String> modifiedParameters, final Long id, final String parentEntityType, final Long parentEntityId,
            final String name, final String fileName, final Long size, final String type, final String description, final String location) {
        this.id = id;
        this.parentEntityType = parentEntityType;
        this.parentEntityId = parentEntityId;
        this.name = name;
        this.fileName = fileName;
        this.size = size;
        this.type = type;
        this.description = description;
        this.location = location;
    }

    public Long getId() {
        return this.id;
    }

    public String getParentEntityType() {
        return this.parentEntityType;
    }

    public Long getParentEntityId() {
        return this.parentEntityId;
    }

    public String getName() {
        return this.name;
    }

    public String getFileName() {
        return this.fileName;
    }

    public Long getSize() {
        return this.size;
    }

    public String getType() {
        return this.type;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLocation() {
        return this.location;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setLocation(final String location) {
        this.location = location;
    }
}
