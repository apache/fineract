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
package org.apache.fineract.infrastructure.gcm.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.gcm.domain.DeviceRegistrationData;
import org.apache.fineract.infrastructure.gcm.exception.DeviceRegistrationNotFoundException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DeviceRegistrationReadPlatformServiceImpl implements
		DeviceRegistrationReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public DeviceRegistrationReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final RoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class DeviceRegistrationDataMapper implements
			RowMapper<DeviceRegistrationData> {

		private final String schema;

		public DeviceRegistrationDataMapper() {
			final StringBuilder sqlBuilder = new StringBuilder(200);
			sqlBuilder
					.append(" cdr.id as id, cdr.registration_id as registrationId, cdr.updatedon_date as updatedOnDate, ");
			sqlBuilder
					.append(" c.id as clientId, c.display_name as clientName ");
			sqlBuilder.append(" from client_device_registration cdr ");
			sqlBuilder.append(" left join m_client c on c.id = cdr.client_id ");
			this.schema = sqlBuilder.toString();
		}

		public String schema() {
			return this.schema;
		}

		@Override
		public DeviceRegistrationData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final Long id = JdbcSupport.getLong(rs, "id");
			final LocalDate updatedOnDate = JdbcSupport.getLocalDate(rs,
					"updatedOnDate");
			final String registrationId = rs.getString("registrationId");
			final Long clientId = rs.getLong("clientId");
			final String clientName = rs.getString("clientName");
			ClientData clientData = ClientData.instance(clientId, clientName);
			return DeviceRegistrationData.instance(id, clientData,
					registrationId, updatedOnDate.toDate());
		}
	}

	@Override
	public Collection<DeviceRegistrationData> retrieveAllDeviceRegiistrations() {
		this.context.authenticatedUser();
		DeviceRegistrationDataMapper drm = new DeviceRegistrationDataMapper();
		String sql = "select " + drm.schema();
		return this.jdbcTemplate.query(sql, drm, new Object[] {});
	}

	@Override
	public DeviceRegistrationData retrieveDeviceRegiistration(Long id) {
		try {
			this.context.authenticatedUser();
			DeviceRegistrationDataMapper drm = new DeviceRegistrationDataMapper();
			String sql = "select " + drm.schema() + " where cdr.id = ? ";
			return this.jdbcTemplate.queryForObject(sql, drm,
					new Object[] { id });
		} catch (final EmptyResultDataAccessException e) {
			throw new DeviceRegistrationNotFoundException(id);
		}
	}

	@Override
	public DeviceRegistrationData retrieveDeviceRegiistrationByClientId(
			Long clientId) {
		try {
			this.context.authenticatedUser();
			DeviceRegistrationDataMapper drm = new DeviceRegistrationDataMapper();
			String sql = "select " + drm.schema() + " where c.id = ? ";
			return this.jdbcTemplate.queryForObject(sql, drm,
					new Object[] { clientId });
		} catch (final EmptyResultDataAccessException e) {
			throw new DeviceRegistrationNotFoundException(clientId, "client");
		}
	}

}
