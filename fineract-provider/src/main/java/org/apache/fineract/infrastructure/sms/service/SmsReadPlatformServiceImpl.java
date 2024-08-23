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
package org.apache.fineract.infrastructure.sms.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageEnumerations;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.infrastructure.sms.exception.SmsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SmsReadPlatformServiceImpl implements SmsReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final SmsMapper smsRowMapper;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;

    @Autowired
    public SmsReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlGenerator = sqlGenerator;
        this.smsRowMapper = new SmsMapper();
        this.columnValidator = columnValidator;
        this.paginationHelper = paginationHelper;
    }

    private static final class SmsMapper implements RowMapper<SmsData> {

        final String schema;

        SmsMapper() {
            final StringBuilder sql = new StringBuilder(300);
            sql.append("smo.id as id, ");
            sql.append("smo.group_id as groupId, ");
            sql.append("smo.client_id as clientId, ");
            sql.append("smo.staff_id as staffId, ");
            sql.append("smo.status_enum as statusId, ");
            sql.append("smo.mobile_no as mobileNo, ");
            sql.append("smo.message as message, ");
            sql.append("smc.provider_id as providerId, ");
            sql.append("smc.campaign_name as campaignName ");
            sql.append("from sms_messages_outbound smo ");
            sql.append("join sms_campaign smc on smc.id = smo.campaign_id ");

            this.schema = sql.toString();
        }

        public String schema() {
            return this.schema;
        }

        public String tableName() {
            return "sms_messages_outbound";
        }

        @Override
        public SmsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");

            final String mobileNo = rs.getString("mobileNo");
            final String message = rs.getString("message");

            final Integer statusId = JdbcSupport.getInteger(rs, "statusId");
            final EnumOptionData status = SmsMessageEnumerations.status(statusId);

            final Long providerId = JdbcSupport.getLong(rs, "providerId");

            final String campaignName = rs.getString("campaignName");

            return SmsData.instance(id, groupId, clientId, staffId, status, mobileNo, message, providerId, campaignName);
        }
    }

    @Override
    public Collection<SmsData> retrieveAll() {

        final String sql = "select " + this.smsRowMapper.schema();

        return this.jdbcTemplate.query(sql, this.smsRowMapper); // NOSONAR
    }

    @Override
    public SmsData retrieveOne(final Long resourceId) {
        try {
            final String sql = "select " + this.smsRowMapper.schema() + " where smo.id = ?";
            return this.jdbcTemplate.queryForObject(sql, this.smsRowMapper, new Object[] { resourceId }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new SmsNotFoundException(resourceId, e);
        }
    }

    @Override
    public Collection<SmsData> retrieveAllPending(final Long campaignId, final Integer limit) {
        final String sqlPlusLimit = limit > 0 ? " " + sqlGenerator.limit(limit) : "";
        String sql = "select " + this.smsRowMapper.schema() + " where smo.status_enum = " + SmsMessageStatusType.PENDING.getValue();
        if (campaignId != null) {
            sql += " and smo.campaign_id = " + campaignId;
        }

        sql += sqlPlusLimit;

        return this.jdbcTemplate.query(sql, this.smsRowMapper); // NOSONAR
    }

    @Override
    public Collection<SmsData> retrieveAllSent(final Integer limit) {
        final String sqlPlusLimit = limit > 0 ? " " + sqlGenerator.limit(limit) : "";
        final String sql = "select " + this.smsRowMapper.schema() + " where smo.status_enum IN ("
                + SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue() + "," + SmsMessageStatusType.SENT.getValue() + ")"
                + sqlPlusLimit;

        return this.jdbcTemplate.query(sql, this.smsRowMapper); // NOSONAR
    }

    @Override
    public List<Long> retrieveExternalIdsOfAllSent(final Integer limit) {
        final String sqlPlusLimit = limit > 0 ? " " + sqlGenerator.limit(limit) : "";
        final String sql = "select external_id from " + this.smsRowMapper.tableName() + " where status_enum = "
                + SmsMessageStatusType.SENT.getValue() + sqlPlusLimit;

        return this.jdbcTemplate.queryForList(sql, Long.class); // NOSONAR
    }

    @Override
    public Page<Long> retrieveAllWaitingForDeliveryReport(final Integer limit) {
        final String sqlPlusLimit = limit > 0 ? " " + sqlGenerator.limit(limit) : "";
        final String sql = "select id from " + this.smsRowMapper.tableName() + " where status_enum = "
                + SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT.getValue() + sqlPlusLimit;
        return paginationHelper.fetchPage(jdbcTemplate, sql, Long.class);
    }

    @Override
    public List<Long> retrieveAllPending(final Integer limit) {
        final String sqlPlusLimit = limit > 0 ? " " + sqlGenerator.limit(limit) : "";
        final String sql = "select external_id from " + this.smsRowMapper.tableName() + " where status_enum = "
                + SmsMessageStatusType.PENDING.getValue() + sqlPlusLimit;

        return this.jdbcTemplate.queryForList(sql, Long.class); // NOSONAR
    }

    @Override
    public Collection<SmsData> retrieveAllDelivered(final Integer limit) {
        final String sqlPlusLimit = limit > 0 ? " " + sqlGenerator.limit(limit) : "";
        final String sql = "select " + this.smsRowMapper.schema() + " where smo.status_enum = " + SmsMessageStatusType.DELIVERED.getValue()
                + sqlPlusLimit;

        return this.jdbcTemplate.query(sql, this.smsRowMapper); // NOSONAR
    }

    @Override
    public Collection<SmsData> retrieveAllFailed(final Integer limit) {
        final String sqlPlusLimit = limit > 0 ? " " + sqlGenerator.limit(limit) : "";
        final String sql = "select " + this.smsRowMapper.schema() + " where smo.status_enum = " + SmsMessageStatusType.FAILED.getValue()
                + sqlPlusLimit;

        return this.jdbcTemplate.query(sql, this.smsRowMapper); // NOSONAR
    }

    @Override
    public Page<SmsData> retrieveSmsByStatus(final Long campaignId, final SearchParameters searchParameters, final Integer status,
            final LocalDate dateFrom, final LocalDate dateTo) {
        final StringBuilder sqlBuilder = new StringBuilder(200);
        final Object[] objectArray = new Object[10];
        int arrayPos = 0;
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(this.smsRowMapper.schema());
        if (status != null) {
            sqlBuilder.append(" where smo.campaign_id = ? and smo.status_enum= ? ");
            objectArray[arrayPos] = campaignId;
            arrayPos = arrayPos + 1;
            objectArray[arrayPos] = status;
            arrayPos = arrayPos + 1;
        }
        String fromDateString = null;
        String toDateString = null;
        if (dateFrom != null && dateTo != null) {
            fromDateString = DateUtils.DEFAULT_DATE_FORMATTER.format(dateFrom);
            toDateString = DateUtils.DEFAULT_DATE_FORMATTER.format(dateTo);
            sqlBuilder.append(" and smo.submittedon_date >= ? and smo.submittedon_date <= ? ");
            objectArray[arrayPos] = fromDateString;
            arrayPos = arrayPos + 1;

            objectArray[arrayPos] = toDateString;
            arrayPos = arrayPos + 1;
        }

        if (searchParameters.hasOrderBy()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
            if (searchParameters.hasSortOrder()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
            }
        } else {
            sqlBuilder.append(" order by smo.submittedon_date, smo.id");
        }

        if (searchParameters.hasLimit()) {
            sqlBuilder.append(" ");
            if (searchParameters.hasOffset()) {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
            } else {
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
            }
        }
        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), finalObjectArray, this.smsRowMapper);
    }

}
