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
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.notification.data.TopicSubscriberData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class TopicSubscriberReadPlatformServiceImpl implements TopicSubscriberReadPlatformService{

	private final JdbcTemplate jdbcTemplate;
	
	@Autowired
	public TopicSubscriberReadPlatformServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private static final class TopicSubscriberMapper implements RowMapper<TopicSubscriberData> {

		private final String schema;

		public TopicSubscriberMapper() {
			final StringBuilder sqlBuilder = new StringBuilder(200);
			sqlBuilder.append("ts.id as id, ts.topic_id as topicId, ts.user_id as userId, ");
			sqlBuilder.append("ts.subscription_date as subscriptionDate from topic_subscriber ts ");
			sqlBuilder.append("WHERE ts.topic_id = ( SELECT id from topic WHERE entity_id = ? ");
			sqlBuilder.append("AND entity_type = ? AND member_type = ? )");
			this.schema = sqlBuilder.toString();
		}

		public String schema() {
			return this.schema;
		}

		@Override
		public TopicSubscriberData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
			final Long id = rs.getLong("id");
			final Long topicId = rs.getLong("topicId");
			final Long userId = rs.getLong("userId");
			final LocalDate subscriptionDate = JdbcSupport.getLocalDate(rs, "subscriptionDate");

			return new TopicSubscriberData(id, topicId, userId, subscriptionDate);
		}

	}

	@Override
	public Collection<TopicSubscriberData> getSubscribers(Long entityId, String entityType, String memberType) {
		final TopicSubscriberMapper tsm = new TopicSubscriberMapper();
		String sql = "SELECT " + tsm.schema();
		return this.jdbcTemplate.query(sql, tsm, new Object[] { entityId, entityType, memberType });
	}
}
