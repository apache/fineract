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
package org.apache.fineract.adhocquery.domain;

import static org.apache.fineract.query.QMAdhoc.mAdhoc;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.MappingProjection;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.query.QMAppuser;

public class AdHocProjection extends MappingProjection<AdHocData> {

    private final QMAppuser creatingUser;

    public AdHocProjection(final QMAppuser creatingUser) {
        super(AdHocData.class, mAdhoc.id, mAdhoc.name, mAdhoc.query, mAdhoc.tableName, mAdhoc.tableFields, mAdhoc.isActive, mAdhoc.email,
                mAdhoc.reportRunFrequencyCode, mAdhoc.reportRunEvery, mAdhoc.lastRun, mAdhoc.createdDate, mAdhoc.createdbyId,
                creatingUser.username, mAdhoc.lastmodifiedbyId, mAdhoc.lastmodifiedDate);
        this.creatingUser = creatingUser;
    }

    @Override
    protected AdHocData map(final Tuple row) {
        return new AdHocData().setId(row.get(mAdhoc.id)).setName(row.get(mAdhoc.name)).setQuery(row.get(mAdhoc.query))
                .setTableName(row.get(mAdhoc.tableName)).setTableFields(row.get(mAdhoc.tableFields)).setEmail(row.get(mAdhoc.email))
                .setActive(Boolean.TRUE.equals(row.get(mAdhoc.isActive))).setCreatedOn(toZonedDateTime(row.get(mAdhoc.createdDate)))
                .setCreatedById(row.get(mAdhoc.createdbyId)).setUpdatedById(row.get(mAdhoc.lastmodifiedbyId))
                .setUpdatedOn(toZonedDateTime(row.get(mAdhoc.lastmodifiedDate))).setCreatedBy(row.get(creatingUser.username))
                .setReportRunFrequencies(AdHocData.template().getReportRunFrequencies())
                .setReportRunFrequency(toLong(row.get(mAdhoc.reportRunFrequencyCode)))
                .setReportRunEvery(toLong(row.get(mAdhoc.reportRunEvery))).setLastRun(toZonedDateTime(row.get(mAdhoc.lastRun)));
    }

    private static ZonedDateTime toZonedDateTime(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return ZonedDateTime.of(timestamp.toLocalDateTime(), DateUtils.getDateTimeZoneOfTenant());
    }

    private static Long toLong(final Integer value) {
        return value != null ? value.longValue() : null;
    }

}
