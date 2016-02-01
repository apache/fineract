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
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.sms.data.SmsData;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageEnumerations;
import org.apache.fineract.infrastructure.sms.exception.SmsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class SmsReadPlatformServiceImpl implements SmsReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final SmsMapper smsRowMapper;

    @Autowired
    public SmsReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.smsRowMapper = new SmsMapper();
    }

    private static final class SmsMapper implements RowMapper<SmsData> {

        final String schema;

        public SmsMapper() {
            final StringBuilder sql = new StringBuilder(300);
            sql.append("smo.id as id, ");
            sql.append("smo.group_id as groupId, ");
            sql.append("smo.client_id as clientId, ");
            sql.append("smo.staff_id as staffId, ");
            sql.append("smo.status_enum as statusId, ");
            sql.append("smo.mobile_no as mobileNo, ");
            sql.append("smo.message as message ");
            sql.append("from sms_messages_outbound smo");

            this.schema = sql.toString();
        }

        public String schema() {
            return this.schema;
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

            return SmsData.instance(id, groupId, clientId, staffId, status, mobileNo, message);
        }
    }

    @Override
    public Collection<SmsData> retrieveAll() {

        final String sql = "select " + this.smsRowMapper.schema();

        return this.jdbcTemplate.query(sql, this.smsRowMapper, new Object[] {});
    }

    @Override
    public SmsData retrieveOne(final Long resourceId) {
        try {
            final String sql = "select " + this.smsRowMapper.schema() + " where smo.id = ?";

            return this.jdbcTemplate.queryForObject(sql, this.smsRowMapper, new Object[] { resourceId });
        } catch (final EmptyResultDataAccessException e) {
            throw new SmsNotFoundException(resourceId);
        }
    }
}