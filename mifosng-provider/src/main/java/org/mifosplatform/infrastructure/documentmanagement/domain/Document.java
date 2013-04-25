/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.domain;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.service.DocumentStore;
import org.mifosplatform.infrastructure.core.service.DocumentStoreType;
import org.mifosplatform.infrastructure.documentmanagement.command.DocumentCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "m_document")
public class Document extends AbstractPersistable<Long> {

    @Column(name = "parent_entity_type", length = 50)
    private String parentEntityType;

    @Column(name = "parent_entity_id", length = 1000)
    private Long parentEntityId;

    @Column(name = "name", length = 250)
    private String name;

    @Column(name = "file_name", length = 250)
    private String fileName;

    @Column(name = "size")
    private Long size;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "storage_type", length = 50)
    private String storageType;

    public Document() {
    }

    public static Document createNew(final String parentEntityType, final Long parentEntityId, final String name, final String fileName,
                                     final Long size, final String type, final String description, final String location, final DocumentStoreType storageType) {
        return new Document(parentEntityType, parentEntityId, name, fileName, size, type, description, location, storageType);
    }

    private Document(final String parentEntityType, final Long parentEntityId, final String name, final String fileName, final Long size,
                     final String type, final String description, final String location, final DocumentStoreType storageType) {
        this.parentEntityType = StringUtils.defaultIfEmpty(parentEntityType, null);
        this.parentEntityId = parentEntityId;
        this.name = StringUtils.defaultIfEmpty(name, null);
        this.fileName = StringUtils.defaultIfEmpty(fileName, null);
        this.size = size;
        this.type = StringUtils.defaultIfEmpty(type, null);
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.location = StringUtils.defaultIfEmpty(location, null);
        this.storageType = storageType.getValue();
    }

    public void update(final DocumentCommand command) {
        if (command.isDescriptionChanged()) {
            this.description = command.getDescription();
        }
        if (command.isFileNameChanged()) {
            this.fileName = command.getFileName();
        }
        if (command.isFileTypeChanged()) {
            this.type = command.getType();
        }
        if (command.isLocationChanged()) {
            this.location = command.getLocation();
        }
        if (command.isNameChanged()) {
            this.name = command.getName();
        }
        if (command.isSizeChanged()) {
            this.size = command.getSize();
        }
        this.storageType = command.getStorageType();
    }

    public String getParentEntityType() {
        return this.parentEntityType;
    }

    public void setParentEntityType(final String parentEntityType) {
        this.parentEntityType = parentEntityType;
    }

    public Long getParentEntityId() {
        return this.parentEntityId;
    }

    public void setParentEntityId(final Long parentEntityId) {
        this.parentEntityId = parentEntityId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public String getType() {
        return this.type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public DocumentStoreType storageType(){
        return DocumentStore.getDocumentStoreType(this.storageType);
    }
}