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

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import java.sql.Date;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.query.QMProvisioningHistory;

public final class ProvisioningEntryIdDateProjection extends MappingProjection<ProvisioningEntryData> {

    private final QMProvisioningHistory history;

    public ProvisioningEntryIdDateProjection(final QMProvisioningHistory history) {
        super(ProvisioningEntryData.class, history.id, history.createdDate);
        this.history = history;
    }

    @Override
    protected ProvisioningEntryData map(final Tuple row) {
        final Date createdDate = row.get(history.createdDate);
        return new ProvisioningEntryData().setId(row.get(history.id)).setJournalEntry(true)
                .setCreatedDate(createdDate == null ? null : createdDate.toLocalDate());
    }
}
