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
package org.apache.fineract.useradministration.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.client.domain.Client;

@Entity
@Table(name = "m_selfservice_user_client_mapping")
public class AppUserClientMapping extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "appuser_id", nullable = false)
    private AppUser appUser;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    public AppUserClientMapping() {

    }

    public AppUserClientMapping(AppUser appUser, Client client) {
        this.appUser = appUser;
        this.client = client;
    }

    public Client getClient() {
        return this.client;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AppUserClientMapping)) {
            return false;
        }

        AppUserClientMapping that = (AppUserClientMapping) obj;

        return null == this.client.getId() ? false : this.client.getId().equals(that.client.getId());
    }

    @Override
    public int hashCode() {

        int hashCode = 17;

        hashCode += null == this.client ? 0 : this.client.getId().hashCode() * 31;

        return hashCode;
    }

}
