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
package org.apache.fineract.accounting.provisioning.domain;

import static org.apache.fineract.query.QMProvisioningHistory.mProvisioningHistory;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import java.sql.Date;
import java.util.Objects;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.query.QMAppuser;

public final class ProvisioningEntryProjection extends MappingProjection<ProvisioningEntryData> {

    private final QMAppuser createdUser;
    private final QMAppuser modifiedUser;

    public ProvisioningEntryProjection(final QMAppuser createdUser, final QMAppuser modifiedUser) {
        super(ProvisioningEntryData.class, mProvisioningHistory.id, mProvisioningHistory.journalEntryCreated,
                mProvisioningHistory.createdbyId, mProvisioningHistory.createdDate, createdUser.username,
                mProvisioningHistory.lastmodifiedbyId, modifiedUser.username);
        this.createdUser = createdUser;
        this.modifiedUser = modifiedUser;
    }

    @Override
    protected ProvisioningEntryData map(final Tuple row) {
        final Date createdDate = row.get(mProvisioningHistory.createdDate);
        return new ProvisioningEntryData().setId(row.get(mProvisioningHistory.id))
                .setJournalEntry(Boolean.TRUE.equals(row.get(mProvisioningHistory.journalEntryCreated)))
                .setCreatedById(Objects.requireNonNullElse(row.get(mProvisioningHistory.createdbyId), 0L))
                .setCreatedDate(createdDate == null ? null : createdDate.toLocalDate()).setCreatedUser(row.get(createdUser.username))
                .setModifiedById(Objects.requireNonNullElse(row.get(mProvisioningHistory.lastmodifiedbyId), 0L))
                .setModifiedUser(row.get(modifiedUser.username));
    }
}
