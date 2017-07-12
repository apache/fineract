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
package org.apache.fineract.notification.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.notification.data.TopicData;
import org.apache.fineract.notification.exception.TopicNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class TopicReadPlatformServiceImpl implements TopicReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public TopicReadPlatformServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
    
	private static final class TopicMapper implements RowMapper<TopicData> {

        private final String schema;

        public TopicMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("t.id as id, t.title as title, t.enabled as enabled, ");
            sqlBuilder.append("t.entity_id as entityId, t.entity_type as entityType, ");
            sqlBuilder.append("t.member_type as memberType, from topic t");
            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public TopicData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String title = rs.getString("title");
            final Boolean enabled = rs.getBoolean("enabled");
            final Long entityId = rs.getLong("entityId");
            final String entityType = rs.getString("entityType");
            final String memberType = rs.getString("memberType");

            return new TopicData(id, title, enabled, entityId, entityType, memberType);
        }
        
    }

	@Override
	public Collection<TopicData> getAllTopics() {
		final TopicMapper tm = new TopicMapper();
		String sql = "select " + tm.schema();
		return this.jdbcTemplate.query(sql, tm, new Object[] {});
	}

	@Override
	public Collection<TopicData> getAllEnabledTopics() {
		final TopicMapper tm = new TopicMapper();
        final String sql = "select " + tm.schema() + " where t.is_active = ?";
        return this.jdbcTemplate.query(sql, tm, new Object[] { true });
	}

	@Override
	public TopicData findById(Long topicId) {
		try {
            final TopicMapper tm = new TopicMapper();
            final String sql = "select " + tm.schema() + " where t.id = ?";
            return this.jdbcTemplate.queryForObject(sql, tm, new Object[] { topicId });
        } catch (final EmptyResultDataAccessException e) {
        	throw new TopicNotFoundException(topicId);
        }
	}	
}
