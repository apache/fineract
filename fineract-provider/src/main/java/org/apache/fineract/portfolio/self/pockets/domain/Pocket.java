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

package org.apache.fineract.portfolio.self.pockets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@SuppressWarnings("serial")
@Entity
@Table(name = "m_pocket", uniqueConstraints = { @UniqueConstraint(columnNames = { "app_user_id" }, name = "unique_app_user") })
public class Pocket extends AbstractPersistableCustom<Long> {

    @Column(name = "app_user_id", length = 20, nullable = false)
    private Long appUserId;

    public Long getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(Long appUserId) {
        this.appUserId = appUserId;
    }

    protected Pocket() {}

    private Pocket(final Long appUserId) {
        this.appUserId = appUserId;

    }

    public static Pocket instance(final Long appUserId) {
        return new Pocket(appUserId);

    }

}
