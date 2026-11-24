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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.adhocquery.data.AdHocData;
import org.apache.fineract.adhocquery.exception.AdHocNotFoundException;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.query.QMAppuser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AdHocReadPlatformServiceImpl implements AdHocReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final AdHocMapper adHocRowMapper;
    private final SQLQueryFactory queryFactory;

    @Override
    public List<AdHocData> retrieveAllAdHocQuery() {
        final String sql = "select " + this.adHocRowMapper.schema() + " order by r.id";

        return this.jdbcTemplate.query(sql, this.adHocRowMapper); // NOSONAR
    }

    @Override
    public List<AdHocData> retrieveAllActiveAdHocQuery() {
        final String sql = "select " + adHocRowMapper.schema() + " where r." + sqlGenerator.escape("is_active") + " = true order by r.id";

        return jdbcTemplate.query(sql, adHocRowMapper); // NOSONAR
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

        return new AdHocData().setId(row.get(mAdhoc.id)).setName(row.get(mAdhoc.name)).setQuery(row.get(mAdhoc.query))
                .setTableName(row.get(mAdhoc.tableName)).setTableFields(row.get(mAdhoc.tableFields)).setEmail(row.get(mAdhoc.email))
                .setActive(Boolean.TRUE.equals(row.get(mAdhoc.isActive))).setCreatedOn(toZonedDateTime(row.get(mAdhoc.createdDate)))
                .setCreatedById(row.get(mAdhoc.createdbyId)).setUpdatedById(row.get(mAdhoc.lastmodifiedbyId))
                .setUpdatedOn(toZonedDateTime(row.get(mAdhoc.lastmodifiedDate))).setCreatedBy(row.get(createdBy.username))
                .setReportRunFrequencies(AdHocData.template().getReportRunFrequencies())
                .setReportRunFrequency(toLong(row.get(mAdhoc.reportRunFrequencyCode)))
                .setReportRunEvery(toLong(row.get(mAdhoc.reportRunEvery))).setLastRun(toZonedDateTime(row.get(mAdhoc.lastRun)));
    }

    private static ZonedDateTime toZonedDateTime(final Timestamp timestamp) {
        return timestamp == null ? null : ZonedDateTime.of(timestamp.toLocalDateTime(), DateUtils.getDateTimeZoneOfTenant());
    }

    private static Long toLong(final Integer value) {
        return value == null ? null : value.longValue();
    }

    public static final class AdHocMapper implements RowMapper<AdHocData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        public AdHocMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        @Override
        public AdHocData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");
            final String query = rs.getString("query");
            final String tableName = rs.getString("tableName");
            final String tableFields = rs.getString("tableField");
            final Boolean isActive = rs.getBoolean("isActive");
            final ZonedDateTime createdDate = JdbcSupport.getDateTime(rs, "createdDate");
            final Long createdById = JdbcSupport.getLong(rs, "createdById");
            final Long updatedById = JdbcSupport.getLong(rs, "updatedById");
            final ZonedDateTime updatedOn = JdbcSupport.getDateTime(rs, "updatedOn");
            final String createdByUsername = rs.getString("createdBy");
            final String email = rs.getString("email");
            final Long reportRunFrequency = JdbcSupport.getLong(rs, "report_run_frequency_code");
            final Long reportRunEvery = JdbcSupport.getLong(rs, "report_run_every");
            final ZonedDateTime lastRun = JdbcSupport.getDateTime(rs, "last_run");

            return new AdHocData().setId(id).setName(name).setQuery(query).setTableName(tableName).setTableFields(tableFields)
                    .setEmail(email).setActive(isActive).setCreatedOn(createdDate).setCreatedById(createdById).setUpdatedById(updatedById)
                    .setUpdatedOn(updatedOn).setCreatedBy(createdByUsername)
                    .setReportRunFrequencies(AdHocData.template().getReportRunFrequencies()).setReportRunFrequency(reportRunFrequency)
                    .setReportRunEvery(reportRunEvery).setLastRun(lastRun);
        }

        public String schema() {
            return " r.id as id, r.name as name, r.query as query, r.table_name as tableName,r.table_fields as tableField ,r.is_active as isActive ,r.email as email ,"
                    + " r.report_run_frequency_code, r.report_run_every, r.last_run, "
                    + " r.created_date as createdDate, r.createdby_id as createdById,cb.username as createdBy,r.lastmodifiedby_id as updatedById ,r.lastmodified_date as updatedOn "
                    + " from m_adhoc r left join m_appuser cb on cb.id=r.createdby_id left join m_appuser mb on mb.id=r.lastmodifiedby_id";

        }
    }

    @Override
    public AdHocData retrieveNewAdHocDetails() {
        return AdHocData.template();
    }

}
