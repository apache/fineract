/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.sms.data.SmsData;
import org.mifosplatform.infrastructure.sms.domain.SmsMessageEnumerations;
import org.mifosplatform.infrastructure.sms.exception.SmsNotFoundException;
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