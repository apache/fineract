/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import org.mifosplatform.infrastructure.core.service.DocumentStoreType;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;

@Entity
@Table(name = "m_image")
public final class Image extends AbstractPersistable<Long> {

    //    @Column(name = "`client_id`")
    @OneToOne()
    @JoinColumn(name = "`client_id`")
    private Client client;

    @Column(name = "`key`", length = 500)
    private String key;

    @Column(name = "`storage_type`", length = 50)
    private String storeType;

    public Image(Client client, String image_key, DocumentStoreType storeType) {
        this.client = client;
        this.key = image_key;
        this.storeType = storeType.getValue();
    }

    public Image() {

    }


    public Image(Client client) {
        this.client = client;
        this.key = null;
        this.storeType = null;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String updateKey(String key) {
        setKey(key);
        return this.key;
    }

    public void updateStorageType(String documentStoreType) {
        this.storeType = documentStoreType;
    }
}