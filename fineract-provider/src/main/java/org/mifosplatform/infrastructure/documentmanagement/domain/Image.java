/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.documentmanagement.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_image")
public final class Image extends AbstractPersistable<Long> {

    @Column(name = "location", length = 500)
    private String location;

    @Column(name = "storage_type_enum")
    private Integer storageType;

    public Image(final String location, final StorageType storageType) {
        this.location = location;
        this.storageType = storageType.getValue();
    }

    protected Image() {

    }

    public String getLocation() {
        return this.location;
    }

    public Integer getStorageType() {
        return this.storageType;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setStorageType(final Integer storageType) {
        this.storageType = storageType;
    }

}