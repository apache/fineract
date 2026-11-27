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
package org.apache.fineract.adhocquery.service;

import static org.apache.fineract.query.QMAdhoc.mAdhoc;

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQueryFactory;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.adhocquery.exception.AdHocNotFoundException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.query.QMAppuser;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AdHocReadPlatformServiceImpl implements AdHocReadPlatformService {

    private final SQLQueryFactory queryFactory;

    private static AdHocData toAdHocData(final Tuple row, final QMAppuser createdBy) {

        return new AdHocData().setId(row.get(mAdhoc.id)).setName(row.get(mAdhoc.name)).setQuery(row.get(mAdhoc.query))
                .setTableName(row.get(mAdhoc.tableName)).setTableFields(row.get(mAdhoc.tableFields)).setEmail(row.get(mAdhoc.email))
                .setActive(Boolean.TRUE.equals(row.get(mAdhoc.isActive))).setCreatedOn(toZonedDateTime(row.get(mAdhoc.createdDate)))
                .setCreatedById(row.get(mAdhoc.createdbyId)).setUpdatedById(row.get(mAdhoc.lastmodifiedbyId))
                .setUpdatedOn(toZonedDateTime(row.get(mAdhoc.lastmodifiedDate))).setCreatedBy(row.get(createdBy.username))
                .setReportRunFrequencies(AdHocData.template().getReportRunFrequencies())
                .setReportRunFrequency(toLong(row.get(mAdhoc.reportRunFrequencyCode)))
                .setReportRunEvery(toLong(row.get(mAdhoc.reportRunEvery))).setLastRun(toZonedDateTime(row.get(mAdhoc.lastRun)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdHocData> retrieveAllAdHocQuery() {
        final QMAppuser createdBy = new QMAppuser("cb");

        final List<Tuple> rows = this.queryFactory
                .select(mAdhoc.id, mAdhoc.name, mAdhoc.query, mAdhoc.tableName, mAdhoc.tableFields, mAdhoc.isActive, mAdhoc.email,
                        mAdhoc.reportRunFrequencyCode, mAdhoc.reportRunEvery, mAdhoc.lastRun, mAdhoc.createdDate, mAdhoc.createdbyId,
                        createdBy.username, mAdhoc.lastmodifiedbyId, mAdhoc.lastmodifiedDate)
                .from(mAdhoc).leftJoin(createdBy).on(createdBy.id.eq(mAdhoc.createdbyId)).orderBy(mAdhoc.id.asc()).fetch();

        return rows.stream().map(row -> toAdHocData(row, createdBy)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdHocData> retrieveAllActiveAdHocQuery() {
        final QMAppuser createdBy = new QMAppuser("cb");

        final List<Tuple> rows = this.queryFactory
                .select(mAdhoc.id, mAdhoc.name, mAdhoc.query, mAdhoc.tableName, mAdhoc.tableFields, mAdhoc.isActive, mAdhoc.email,
                        mAdhoc.reportRunFrequencyCode, mAdhoc.reportRunEvery, mAdhoc.lastRun, mAdhoc.createdDate, mAdhoc.createdbyId,
                        createdBy.username, mAdhoc.lastmodifiedbyId, mAdhoc.lastmodifiedDate)
                .from(mAdhoc).leftJoin(createdBy).on(createdBy.id.eq(mAdhoc.createdbyId)).where(mAdhoc.isActive.isTrue())
                .orderBy(mAdhoc.id.asc()).fetch();

        return rows.stream().map(row -> toAdHocData(row, createdBy)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdHocData retrieveOne(final Long id) {
        final QMAppuser createdBy = new QMAppuser("cb");

        final Tuple row = this.queryFactory
                .select(mAdhoc.id, mAdhoc.name, mAdhoc.query, mAdhoc.tableName, mAdhoc.tableFields, mAdhoc.isActive, mAdhoc.email,
                        mAdhoc.reportRunFrequencyCode, mAdhoc.reportRunEvery, mAdhoc.lastRun, mAdhoc.createdDate, mAdhoc.createdbyId,
                        createdBy.username, mAdhoc.lastmodifiedbyId, mAdhoc.lastmodifiedDate) //
                .from(mAdhoc) //
                .leftJoin(createdBy).on(createdBy.id.eq(mAdhoc.createdbyId)) //
                .where(mAdhoc.id.eq(id)) //
                .fetchOne();

        if (row == null) {
            throw new AdHocNotFoundException(id);
        }

        return toAdHocData(row, createdBy);
    }

    private static ZonedDateTime toZonedDateTime(final Timestamp timestamp) {
        return timestamp == null ? null : ZonedDateTime.of(timestamp.toLocalDateTime(), DateUtils.getDateTimeZoneOfTenant());
    }

    private static Long toLong(final Integer value) {
        return value == null ? null : value.longValue();
    }

    @Override
    public AdHocData retrieveNewAdHocDetails() {
        return AdHocData.template();
    }

}
